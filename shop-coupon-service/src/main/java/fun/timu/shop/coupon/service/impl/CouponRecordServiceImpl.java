package fun.timu.shop.coupon.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.enums.CouponStateEnum;
import fun.timu.shop.common.enums.OrderStateEnum;
import fun.timu.shop.common.enums.StockTaskStateEnum;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.CouponRecordMessage;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.config.RabbitMQConfig;
import fun.timu.shop.coupon.controller.request.LockCouponRecordRequest;
import fun.timu.shop.coupon.feign.ProductOrderFeignService;
import fun.timu.shop.coupon.manager.CouponRecordManager;
import fun.timu.shop.coupon.manager.CouponTaskManager;
import fun.timu.shop.coupon.model.DO.CouponRecordDO;
import fun.timu.shop.coupon.model.DO.CouponTaskDO;
import fun.timu.shop.coupon.model.VO.CouponRecordVO;
import fun.timu.shop.coupon.service.CouponRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhengke
 * @description 针对表【coupon_record】的数据库操作Service实现
 * @createDate 2025-07-26 11:16:47
 */
@Slf4j
@Service
@AllArgsConstructor
public class CouponRecordServiceImpl implements CouponRecordService {
    private final CouponRecordManager recordManager;
    private final CouponTaskManager taskManager;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final ProductOrderFeignService feignService;

    @Override
    public JsonData page(int page, int size) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 参数校验
        if (page <= 0) page = 1;
        if (size <= 0 || size > 100) size = 10; // 限制分页大小，防止大量数据查询

        log.info("分页查询用户优惠券记录: userId={}, page={}, size={}", loginUser.getId(), page, size);

        // 调用Manager层进行分页查询，不限制状态（查询用户所有的优惠券记录）
        IPage<CouponRecordDO> recordPage = recordManager.getUserCouponRecords(loginUser.getId(), null, page, size);

        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", recordPage.getTotal());
        pageMap.put("total_page", recordPage.getPages());
        pageMap.put("current_data", recordPage.getRecords().stream().map(this::convertToCouponRecordVO).collect(Collectors.toList()));

        log.info("分页查询用户优惠券记录完成: userId={}, totalRecord={}, totalPage={}, currentDataSize={}", loginUser.getId(), recordPage.getTotal(), recordPage.getPages(), recordPage.getRecords().size());

        return JsonData.buildSuccess(pageMap);
    }

    @Override
    public JsonData findById(long recordId) {
        // 参数校验
        if (recordId <= 0) {
            log.warn("查询优惠券记录详情参数错误: recordId={}", recordId);
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        log.info("查询优惠券记录详情: userId={}, recordId={}", loginUser.getId(), recordId);

        // 根据ID查询优惠券记录
        CouponRecordDO recordDO = recordManager.getById(recordId);

        if (recordDO == null || recordDO.getDelFlag() == 1) {
            log.warn("优惠券记录不存在: recordId={}", recordId);
            return JsonData.buildResult(BizCodeEnum.COUPON_NO_EXITS);
        }

        // 验证记录是否属于当前用户
        if (!recordDO.getUserId().equals(loginUser.getId())) {
            log.warn("用户无权限访问该优惠券记录: userId={}, recordId={}, recordUserId={}", loginUser.getId(), recordId, recordDO.getUserId());
            return JsonData.buildResult(BizCodeEnum.COUPON_NO_EXITS);
        }

        // 转换为VO对象
        CouponRecordVO recordVO = convertToCouponRecordVO(recordDO);

        log.info("查询优惠券记录详情成功: userId={}, recordId={}, couponTitle={}", loginUser.getId(), recordId, recordDO.getCouponTitle());

        return JsonData.buildSuccess(recordVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData lockCouponRecords(LockCouponRecordRequest recordRequest) {
        // 1. 登录用户校验
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 2. 参数校验
        if (recordRequest == null) {
            log.warn("锁定优惠券记录请求参数为空: userId={}", loginUser.getId());
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        String orderOutTradeNo = recordRequest.getOrderOutTradeNo();
        List<Long> lockCouponRecordIds = recordRequest.getLockCouponRecordIds();

        if (orderOutTradeNo == null || orderOutTradeNo.trim().isEmpty()) {
            log.warn("订单号为空: userId={}", loginUser.getId());
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        if (lockCouponRecordIds == null || lockCouponRecordIds.isEmpty()) {
            log.warn("锁定优惠券记录ID列表为空: userId={}, orderOutTradeNo={}", loginUser.getId(), orderOutTradeNo);
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        // 限制批量操作数量，防止大批量操作影响性能
        if (lockCouponRecordIds.size() > 50) {
            log.warn("批量锁定优惠券数量超限: userId={}, count={}, orderOutTradeNo={}",
                    loginUser.getId(), lockCouponRecordIds.size(), orderOutTradeNo);
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        log.info("开始锁定优惠券记录: userId={}, orderOutTradeNo={}, recordIds={}",
                loginUser.getId(), orderOutTradeNo, lockCouponRecordIds);

        try {
            // 3. 批量锁定优惠券记录状态
            int updateRows = recordManager.lockUseStateBatch(loginUser.getId(), CouponStateEnum.USED.name(), lockCouponRecordIds);
            log.info("优惠券记录锁定完成: userId={}, updateRows={}, expectedRows={}",
                    loginUser.getId(), updateRows, lockCouponRecordIds.size());

            // 4. 预设过期时间（默认30分钟后过期）
            Date expireTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000);

            // 5. 批量创建优惠券任务记录
            List<CouponTaskDO> couponTaskDOList = lockCouponRecordIds.stream().map(recordId -> {
                CouponTaskDO couponTaskDO = new CouponTaskDO();
                couponTaskDO.setCreateTime(new Date());
                couponTaskDO.setOutTradeNo(orderOutTradeNo);
                couponTaskDO.setCouponRecordId(recordId);
                couponTaskDO.setUserId(loginUser.getId());
                couponTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
                couponTaskDO.setExpireTime(expireTime);
                return couponTaskDO;
            }).collect(Collectors.toList());

            int insertRows = taskManager.insertBatch(couponTaskDOList);
            log.info("优惠券任务记录创建完成: userId={}, insertRows={}, expectedRows={}",
                    loginUser.getId(), insertRows, lockCouponRecordIds.size());

            // 6. 校验操作结果的一致性
            if (lockCouponRecordIds.size() != insertRows || insertRows != updateRows) {
                log.error("优惠券锁定操作数据不一致: userId={}, expectedCount={}, updateRows={}, insertRows={}",
                        loginUser.getId(), lockCouponRecordIds.size(), updateRows, insertRows);
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }

            // 7. 批量发送延迟消息，优化消息发送性能
            sendDelayedReleaseMessages(couponTaskDOList, orderOutTradeNo);

            log.info("优惠券记录锁定成功: userId={}, orderOutTradeNo={}, recordCount={}",
                    loginUser.getId(), orderOutTradeNo, lockCouponRecordIds.size());

            return JsonData.buildSuccess();

        } catch (BizException e) {
            log.error("优惠券记录锁定业务异常: userId={}, orderOutTradeNo={}", loginUser.getId(), orderOutTradeNo, e);
            throw e;
        } catch (Exception e) {
            log.error("优惠券记录锁定系统异常: userId={}, orderOutTradeNo={}", loginUser.getId(), orderOutTradeNo, e);
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }
    }

    /**
     * 批量发送延迟释放消息
     */
    private void sendDelayedReleaseMessages(List<CouponTaskDO> couponTaskDOList, String orderOutTradeNo) {
        int successCount = 0;
        int failCount = 0;

        for (CouponTaskDO couponTaskDO : couponTaskDOList) {
            try {
                CouponRecordMessage couponRecordMessage = buildCouponRecordMessage(couponTaskDO, orderOutTradeNo);

                rabbitTemplate.convertAndSend(
                        rabbitMQConfig.getEventExchange(),
                        rabbitMQConfig.getCouponReleaseDelayRoutingKey(),
                        couponRecordMessage
                );

                successCount++;
                log.debug("优惠券锁定延迟消息发送成功: taskId={}, recordId={}",
                        couponTaskDO.getId(), couponTaskDO.getCouponRecordId());

            } catch (Exception e) {
                failCount++;
                log.error("优惠券锁定延迟消息发送失败: taskId={}, recordId={}",
                        couponTaskDO.getId(), couponTaskDO.getCouponRecordId(), e);
            }
        }

        log.info("延迟消息发送完成: orderOutTradeNo={}, total={}, success={}, fail={}",
                orderOutTradeNo, couponTaskDOList.size(), successCount, failCount);

        // 如果有消息发送失败，记录警告但不影响主流程
        if (failCount > 0) {
            log.warn("部分延迟消息发送失败，可能影响优惠券自动释放: orderOutTradeNo={}, failCount={}",
                    orderOutTradeNo, failCount);
        }
    }

    /**
     * 构建优惠券记录消息
     */
    private CouponRecordMessage buildCouponRecordMessage(CouponTaskDO couponTaskDO, String orderOutTradeNo) {
        CouponRecordMessage message = new CouponRecordMessage();
        message.setOutTradeNo(orderOutTradeNo);
        message.setTaskId(couponTaskDO.getId());
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        // 1. 参数校验
        if (recordMessage == null) {
            log.warn("释放优惠券记录消息为空");
            return true; // 消息为空直接确认，避免重复投递
        }

        if (recordMessage.getTaskId() == null || recordMessage.getOutTradeNo() == null) {
            log.warn("释放优惠券记录消息参数不完整: taskId={}, outTradeNo={}", 
                    recordMessage.getTaskId(), recordMessage.getOutTradeNo());
            return true; // 参数不完整直接确认
        }

        Long taskId = recordMessage.getTaskId();
        String outTradeNo = recordMessage.getOutTradeNo();

        log.info("开始处理优惠券释放: taskId={}, outTradeNo={}", taskId, outTradeNo);

        try {
            // 2. 查询任务记录
            CouponTaskDO taskDO = taskManager.selectById(taskId);
            if (taskDO == null) {
                log.warn("优惠券任务记录不存在，直接确认消息: taskId={}, outTradeNo={}", taskId, outTradeNo);
                return true; // 任务不存在直接确认
            }

            // 3. 检查任务状态，只处理LOCK状态的任务
            if (!StockTaskStateEnum.LOCK.name().equalsIgnoreCase(taskDO.getLockState())) {
                log.info("优惠券任务状态非LOCK，无需处理: taskId={}, currentState={}, outTradeNo={}", 
                        taskId, taskDO.getLockState(), outTradeNo);
                return true; // 非LOCK状态直接确认
            }

            // 4. 查询订单状态
            OrderStateEnum orderState = queryOrderState(outTradeNo);
            
            // 5. 根据订单状态处理任务
            return processTaskByOrderState(taskDO, orderState, recordMessage);

        } catch (Exception e) {
            log.error("处理优惠券释放异常: taskId={}, outTradeNo={}", taskId, outTradeNo, e);
            return false; // 异常情况重新投递
        }
    }

    /**
     * 查询订单状态
     */
    private OrderStateEnum queryOrderState(String outTradeNo) {
        try {
            JsonData jsonData = feignService.queryProductOrderState(outTradeNo);
            
            if (jsonData == null || jsonData.getCode() != 0) {
                log.warn("查询订单状态失败或订单不存在: outTradeNo={}, response={}", 
                        outTradeNo, jsonData != null ? jsonData.getCode() : "null");
                return null; // 返回null表示查询失败或订单不存在
            }

            String stateStr = jsonData.getData() != null ? jsonData.getData().toString() : null;
            if (stateStr == null || stateStr.trim().isEmpty()) {
                log.warn("订单状态为空: outTradeNo={}", outTradeNo);
                return null;
            }

            // 解析订单状态，直接使用现有枚举
            if (OrderStateEnum.NEW.name().equalsIgnoreCase(stateStr)) {
                return OrderStateEnum.NEW;
            } else if (OrderStateEnum.PAY.name().equalsIgnoreCase(stateStr)) {
                return OrderStateEnum.PAY;
            } else {
                log.info("订单状态为其他状态: outTradeNo={}, state={}", outTradeNo, stateStr);
                return null; // 其他状态视为无效状态
            }

        } catch (Exception e) {
            log.error("查询订单状态异常: outTradeNo={}", outTradeNo, e);
            return null; // 异常情况返回null
        }
    }

    /**
     * 根据订单状态处理任务
     */
    private boolean processTaskByOrderState(CouponTaskDO taskDO, OrderStateEnum orderState, CouponRecordMessage recordMessage) {
        String outTradeNo = recordMessage.getOutTradeNo();
        Long taskId = taskDO.getId();

        // 如果订单状态为null，表示查询失败、订单不存在或已取消
        if (orderState == null) {
            log.warn("订单状态无效，释放优惠券: taskId={}, outTradeNo={}", taskId, outTradeNo);
            return handleCancelledOrder(taskDO, recordMessage);
        }

        switch (orderState) {
            case NEW:
                // 订单仍是新建状态，返回false重新投递
                log.info("订单状态为NEW，重新投递消息: taskId={}, outTradeNo={}", taskId, outTradeNo);
                return false;

            case PAY:
                // 订单已支付，将任务状态改为FINISH
                log.info("订单已支付，完成任务: taskId={}, outTradeNo={}", taskId, outTradeNo);
                return handlePaidOrder(taskDO, recordMessage);

            default:
                // 其他状态（如CANCEL等），释放优惠券
                log.info("订单状态为{}，释放优惠券: taskId={}, outTradeNo={}", orderState.name(), taskId, outTradeNo);
                return handleCancelledOrder(taskDO, recordMessage);
        }
    }

    /**
     * 处理已支付订单
     */
    private boolean handlePaidOrder(CouponTaskDO taskDO, CouponRecordMessage recordMessage) {
        try {
            // 更新任务状态为FINISH
            taskDO.setLockState(StockTaskStateEnum.FINISH.name());
            taskDO.setUpdateTime(new Date());
            
            boolean updateSuccess = taskManager.updateEntity(taskDO, taskDO.getId());
            if (!updateSuccess) {
                log.error("更新任务状态为FINISH失败: taskId={}, outTradeNo={}", 
                        taskDO.getId(), recordMessage.getOutTradeNo());
                return false; // 更新失败重新投递
            }

            log.info("订单已支付，任务状态更新为FINISH: taskId={}, outTradeNo={}", 
                    taskDO.getId(), recordMessage.getOutTradeNo());
            return true;

        } catch (Exception e) {
            log.error("处理已支付订单异常: taskId={}, outTradeNo={}", 
                    taskDO.getId(), recordMessage.getOutTradeNo(), e);
            return false;
        }
    }

    /**
     * 处理已取消订单，释放优惠券
     */
    private boolean handleCancelledOrder(CouponTaskDO taskDO, CouponRecordMessage recordMessage) {
        try {
            Long taskId = taskDO.getId();
            Long couponRecordId = taskDO.getCouponRecordId();
            String outTradeNo = recordMessage.getOutTradeNo();

            // 1. 更新任务状态为CANCEL
            taskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            taskDO.setUpdateTime(new Date());
            
            boolean taskUpdateSuccess = taskManager.updateEntity(taskDO, taskId);
            if (!taskUpdateSuccess) {
                log.error("更新任务状态为CANCEL失败: taskId={}, outTradeNo={}", taskId, outTradeNo);
                return false; // 更新失败重新投递
            }

            // 2. 恢复优惠券记录状态为NEW
            recordManager.updateState(couponRecordId, CouponStateEnum.NEW.name());

            log.info("订单取消，优惠券已释放: taskId={}, couponRecordId={}, outTradeNo={}", 
                    taskId, couponRecordId, outTradeNo);
            return true;

        } catch (Exception e) {
            log.error("处理已取消订单异常: taskId={}, outTradeNo={}", 
                    taskDO.getId(), recordMessage.getOutTradeNo(), e);
            return false;
        }
    }

    /**
     * 转换为CouponRecordVO
     */
    private CouponRecordVO convertToCouponRecordVO(CouponRecordDO record) {
        CouponRecordVO vo = new CouponRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}





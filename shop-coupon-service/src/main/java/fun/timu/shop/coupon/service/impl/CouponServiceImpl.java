package fun.timu.shop.coupon.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.enums.CouponCategoryEnum;
import fun.timu.shop.common.enums.CouponPublishEnum;
import fun.timu.shop.common.enums.CouponStateEnum;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.manager.CouponManager;
import fun.timu.shop.coupon.manager.CouponRecordManager;
import fun.timu.shop.coupon.model.DO.CouponDO;
import fun.timu.shop.coupon.model.DO.CouponRecordDO;
import fun.timu.shop.coupon.model.VO.CouponRecordVO;
import fun.timu.shop.coupon.model.VO.CouponVO;
import fun.timu.shop.coupon.service.CouponService;
import fun.timu.shop.coupon.mapper.CouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 分布式锁相关导入
import fun.timu.shop.common.components.DistributedLockComponent;

import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhengke
 * @description 针对表【coupon】的数据库操作Service实现
 * @createDate 2025-07-26 11:16:47
 */
@Slf4j
@Service
public class CouponServiceImpl implements CouponService {
    private final CouponManager couponManager;
    private final CouponRecordManager couponRecordManager;
    private final DistributedLockComponent distributedLockComponent;

    public CouponServiceImpl(CouponManager couponManager, CouponRecordManager couponRecordManager, DistributedLockComponent distributedLockComponent) {
        this.couponManager = couponManager;
        this.couponRecordManager = couponRecordManager;
        this.distributedLockComponent = distributedLockComponent;
    }

    @Override
    public Map<String, Object> pageCouponActivity(int page, int size) {
        // 参数校验
        if (page <= 0) page = 1;
        if (size <= 0 || size > 100) size = 10; // 限制分页大小，防止大量数据查询

        Page<CouponDO> pageInfo = new Page<>(page, size);
        IPage<CouponDO> couponDOIPage = couponManager.selectPage(pageInfo);

        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", couponDOIPage.getTotal());
        pageMap.put("total_page", couponDOIPage.getPages());
        pageMap.put("current_data", couponDOIPage.getRecords().stream().map(this::beanProcess).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData addCoupon(long couponId, CouponCategoryEnum category) {
        // 参数校验
        if (couponId <= 0 || category == null) {
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        return addCouponInternal(couponId, category, loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData grantNewUserBenefits(Long userId) {
        // 参数校验
        if (userId == null) {
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        log.info("开始为新用户发放注册福利: userId={}", userId);

        // 从数据库动态查询所有可用的新用户优惠券
        List<CouponDO> availableCoupons = couponManager.getAvailableNewUserCoupons();

        if (availableCoupons == null || availableCoupons.isEmpty()) {
            log.warn("没有找到可用的新用户优惠券: userId={}", userId);
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", userId);
            resultData.put("totalCoupons", 0);
            resultData.put("successCount", 0);
            resultData.put("failCount", 0);
            resultData.put("message", "当前没有可用的新用户优惠券");
            return JsonData.buildSuccess(resultData);
        }

        log.info("查询到{}张可用的新用户优惠券: userId={}, couponIds={}",
                availableCoupons.size(), userId,
                availableCoupons.stream().map(CouponDO::getId).collect(Collectors.toList()));

        // 为RPC调用构建虚拟的LoginUser对象
        LoginUser rpcUser = new LoginUser();
        rpcUser.setId(userId);
        rpcUser.setName("NEW-USER-" + userId);

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorDetails = new StringBuilder();

        // 逐个发放优惠券
        for (CouponDO coupon : availableCoupons) {
            try {
                log.info("为新用户发放优惠券: userId={}, couponId={}, couponTitle={}",
                        userId, coupon.getId(), coupon.getCouponTitle());

                JsonData result = addCouponInternal(coupon.getId(), CouponCategoryEnum.NEW_USER, rpcUser);

                if (result != null && result.getCode() == 0) {
                    successCount++;
                    log.info("新用户优惠券发放成功: userId={}, couponId={}, couponTitle={}",
                            userId, coupon.getId(), coupon.getCouponTitle());
                } else {
                    failCount++;
                    String error = String.format("优惠券[%s]发放失败:%s; ",
                            coupon.getCouponTitle(),
                            result != null ? result.getMsg() : "未知错误");
                    errorDetails.append(error);
                    log.warn("新用户优惠券发放失败: userId={}, couponId={}, couponTitle={}, error={}",
                            userId, coupon.getId(), coupon.getCouponTitle(), error);
                }

                // 添加短暂延迟，避免对系统造成压力
                Thread.sleep(50);

            } catch (Exception e) {
                failCount++;
                String error = String.format("优惠券[%s]发放异常:%s; ",
                        coupon.getCouponTitle(), e.getMessage());
                errorDetails.append(error);
                log.error("新用户优惠券发放异常: userId={}, couponId={}, couponTitle={}",
                        userId, coupon.getId(), coupon.getCouponTitle(), e);
            }
        }

        // 构建返回结果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("totalCoupons", availableCoupons.size());
        resultData.put("successCount", successCount);
        resultData.put("failCount", failCount);

        // 添加成功发放的优惠券信息
        if (successCount > 0) {
            List<Map<String, Object>> successCoupons = availableCoupons.stream()
                    .limit(successCount)
                    .map(coupon -> {
                        Map<String, Object> couponInfo = new HashMap<>();
                        couponInfo.put("couponId", coupon.getId());
                        couponInfo.put("couponTitle", coupon.getCouponTitle());
                        couponInfo.put("price", coupon.getPrice());
                        couponInfo.put("conditionPrice", coupon.getConditionPrice());
                        return couponInfo;
                    })
                    .collect(Collectors.toList());
            resultData.put("successCoupons", successCoupons);
        }

        if (errorDetails.length() > 0) {
            resultData.put("errors", errorDetails.toString());
        }

        log.info("新用户注册福利发放完成: userId={}, 总计={}, 成功={}, 失败={}",
                userId, availableCoupons.size(), successCount, failCount);

        if (failCount == 0) {
            return JsonData.buildSuccess(resultData);
        } else if (successCount == 0) {
            log.error("所有优惠券发放失败: userId={}, 总计={}, 错误详情={}", userId, availableCoupons.size(), errorDetails.toString());
            return JsonData.buildResult(BizCodeEnum.COUPON_GET_FAIL);
        } else {
            // 部分成功的情况也返回成功，但包含错误信息
            return JsonData.buildSuccess(resultData);
        }
    }

    /**
     * 内部方法：领取优惠券的核心逻辑
     */
    private JsonData addCouponInternal(long couponId, CouponCategoryEnum category, LoginUser loginUser) {
        // 分层锁机制实现
        // 第一层：用户锁，防止同一用户重复提交
        String userLockKey = "coupon:user:" + couponId + ":" + loginUser.getId();

        try {
            return distributedLockComponent.executeWithLock(userLockKey, 1,    // 等待时间1秒（快速失败）
                    30,   // 锁持有时间30秒
                    TimeUnit.SECONDS, () -> {
                        // 1. 检查用户是否已领取过该优惠券
                        Long recordCount = couponRecordManager.selectCount(couponId, loginUser.getId());
                        if (recordCount > 0) {
                            throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMIT);
                        }

                        // 第二层：库存锁，防止库存超卖
                        String stockLockKey = "coupon:stock:" + couponId;
                        return distributedLockComponent.executeWithLock(stockLockKey, 2,    // 等待时间2秒
                                10,   // 锁持有时间10秒（短暂持有）
                                TimeUnit.SECONDS, () -> {
                                    // 2. 获取优惠券信息并进行校验
                                    CouponDO couponDO = couponManager.selectOne(couponId, category.name());
                                    checkCoupon(couponDO, loginUser.getId());

                                    // 3. 扣减库存（使用数据库悲观锁）
                                    int rows = couponManager.reduceStockWithLock(couponId);
                                    if (rows <= 0) {
                                        log.warn("优惠券库存不足，发放失败. couponId:{}, userId:{}", couponId, loginUser.getId());
                                        throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
                                    }

                                    // 4. 构建并保存领券记录
                                    CouponRecordDO couponRecordDO = buildCouponRecord(couponDO, loginUser);
                                    couponRecordManager.insert(couponRecordDO);

                                    log.info("用户领取优惠券成功. couponId:{}, userId:{}", couponId, loginUser.getId());
                                    return JsonData.buildSuccess();
                                });
                    });
        } catch (Exception e) {
            log.error("领取优惠券异常. couponId:{}, userId:{}", couponId, loginUser.getId(), e);
            if (e instanceof BizException) {
                throw e;
            }
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }
    }

    private void checkCoupon(CouponDO couponDO, Long userId) {
        if (couponDO == null) {
            throw new BizException(BizCodeEnum.COUPON_NO_EXITS);
        }

        // 判断是否是发布状态
        if (!CouponPublishEnum.PUBLISH.name().equals(couponDO.getPublish())) {
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }

        // 库存是否足够
        if (couponDO.getStock() <= 0) {
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }

        // 检查领取时间范围（优先检查领取时间）
        Date now = new Date();
        if (couponDO.getReceiveStartTime() != null && now.before(couponDO.getReceiveStartTime())) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }
        if (couponDO.getReceiveEndTime() != null && now.after(couponDO.getReceiveEndTime())) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }

        // 检查优惠券使用时间范围
        if (couponDO.getStartTime() != null && couponDO.getEndTime() != null) {
            if (now.before(couponDO.getStartTime()) || now.after(couponDO.getEndTime())) {
                throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
            }
        }

        // 检查每日限量
        if (couponDO.getDailyLimit() != null && couponDO.getDailyLimit() > 0) {
            // 需要查询今日已发放数量
            Long todayCount = couponRecordManager.getTodayReceiveCount(couponDO.getId());
            if (todayCount >= couponDO.getDailyLimit()) {
                throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
            }
        }

        // 用户是否超过限制
        if (couponDO.getUserLimit() != null && couponDO.getUserLimit() > 0) {
            Long recordNum = couponRecordManager.selectCount(couponDO.getId(), userId);
            if (recordNum >= couponDO.getUserLimit()) {
                throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMIT);
            }
        }

        // 检查是否仅限首单用户（需要查询用户订单记录）
        if (couponDO.getFirstOrderOnly() != null && couponDO.getFirstOrderOnly() == 1) {
            // 这里需要调用订单服务检查用户是否有历史订单
            // boolean hasOrders = orderService.hasUserOrders(userId);
            // if (hasOrders) {
            //     throw new BizException(BizCodeEnum.COUPON_FIRST_ORDER_ONLY);
            // }
        }
    }

    /**
     * 构建优惠券记录
     */
    private CouponRecordDO buildCouponRecord(CouponDO couponDO, LoginUser loginUser) {
        CouponRecordDO couponRecordDO = new CouponRecordDO();

        // 只复制必要的字段，避免敏感信息泄露
        couponRecordDO.setCouponId(couponDO.getId());
        couponRecordDO.setCouponTitle(couponDO.getCouponTitle());
        couponRecordDO.setPrice(couponDO.getPrice());
        couponRecordDO.setConditionPrice(couponDO.getConditionPrice());
        couponRecordDO.setStartTime(couponDO.getStartTime());
        couponRecordDO.setEndTime(couponDO.getEndTime());

        // 设置用户信息
        couponRecordDO.setUserId(loginUser.getId());
        couponRecordDO.setUserName(loginUser.getName());

        // 设置状态和时间
        couponRecordDO.setUseState(CouponStateEnum.NEW.name());
        couponRecordDO.setCreateTime(new Date());
        couponRecordDO.setReceiveChannel("ACTIVITY"); // 默认活动页面领取

        return couponRecordDO;
    }

    /**
     * 获取用户的优惠券记录
     *
     * @param useState 使用状态
     * @param page     页码
     * @param size     页面大小
     * @return 分页结果
     */
    public Map<String, Object> getUserCouponRecords(String useState, int page, int size) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 参数校验
        if (page <= 0) page = 1;
        if (size <= 0 || size > 100) size = 10;

        IPage<CouponRecordDO> recordPage = couponRecordManager.getUserCouponRecords(loginUser.getId(), useState, page, size);

        Map<String, Object> result = new HashMap<>(3);
        result.put("total_record", recordPage.getTotal());
        result.put("total_page", recordPage.getPages());
        result.put("current_data", recordPage.getRecords().stream().map(this::convertToCouponRecordVO).collect(Collectors.toList()));

        return result;
    }

    /**
     * 使用优惠券
     *
     * @param recordId             优惠券记录ID
     * @param orderId              订单ID
     * @param actualDiscountAmount 实际优惠金额
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonData useCoupon(Long recordId, Long orderId, BigDecimal actualDiscountAmount) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 参数校验
        if (recordId == null || recordId <= 0) {
            throw new BizException(BizCodeEnum.COUPON_CONDITION_ERROR);
        }

        // 查询优惠券记录
        CouponRecordDO record = couponRecordManager.getById(recordId);
        if (record == null || !record.getUserId().equals(loginUser.getId())) {
            throw new BizException(BizCodeEnum.COUPON_NO_EXITS);
        }

        // 检查优惠券状态
        if (!CouponStateEnum.NEW.name().equals(record.getUseState())) {
            throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
        }

        // 检查是否过期
        if (record.getEndTime() != null && record.getEndTime().before(new Date())) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }

        // 更新优惠券状态为已使用
        int rows = couponRecordManager.updateUseState(recordId, CouponStateEnum.USED.name(), new Date(), orderId, actualDiscountAmount);

        if (rows <= 0) {
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }

        log.info("用户使用优惠券成功. recordId:{}, userId:{}, orderId:{}", recordId, loginUser.getId(), orderId);
        return JsonData.buildSuccess();
    }

    /**
     * 定时任务：批量更新过期优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateExpiredCoupons() {
        int count = couponRecordManager.batchUpdateExpiredCoupons();
        log.info("批量更新过期优惠券完成，共更新{}条记录", count);
    }

    /**
     * 转换为CouponRecordVO
     */
    private Object convertToCouponRecordVO(CouponRecordDO record) {
        CouponRecordVO vo = new CouponRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 安全的对象转换，避免敏感信息泄露
     */
    private CouponVO beanProcess(CouponDO couponDO) {
        CouponVO couponVO = new CouponVO();
        BeanUtils.copyProperties(couponDO, couponVO);
        return couponVO;
    }
}





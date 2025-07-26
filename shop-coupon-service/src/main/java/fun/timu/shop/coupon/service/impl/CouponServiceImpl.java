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

import java.math.BigDecimal;

import java.util.Date;
import java.util.HashMap;
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

    public CouponServiceImpl(CouponManager couponManager, CouponRecordManager couponRecordManager) {
        this.couponManager = couponManager;
        this.couponRecordManager = couponRecordManager;
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
        pageMap.put("current_data", couponDOIPage.getRecords().stream()
                .map(this::beanProcess)
                .collect(Collectors.toList()));

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

        // 分布式锁防止并发领取同一张优惠券
        String lockKey = "coupon:receive:" + couponId + ":" + loginUser.getId();
        try {
            // 假设使用Redis分布式锁，这里需要引入相关组件
            // if (!distributedLock.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
            //     throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
            // }

            CouponDO couponDO = couponManager.selectOne(couponId, category.name());

            // 优惠券是否可以领取（包含所有校验）
            this.checkCoupon(couponDO, loginUser.getId());

            // 先扣减库存，使用乐观锁
            int rows = couponManager.reduceStockWithCheck(couponId, couponDO.getStock());

            if (rows <= 0) {
                log.warn("优惠券库存不足，发放失败. couponId:{}, userId:{}", couponId, loginUser.getId());
                throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
            }

            // 构建领券记录
            CouponRecordDO couponRecordDO = buildCouponRecord(couponDO, loginUser);

            // 保存领券记录
            couponRecordManager.insert(couponRecordDO);

            log.info("用户领取优惠券成功. couponId:{}, userId:{}", couponId, loginUser.getId());
            return JsonData.buildSuccess();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("领取优惠券异常. couponId:{}, userId:{}", couponId, loginUser.getId(), e);
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        } finally {
            // distributedLock.unlock(lockKey);
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

        IPage<CouponRecordDO> recordPage = couponRecordManager.getUserCouponRecords(
                loginUser.getId(), useState, page, size);

        Map<String, Object> result = new HashMap<>(3);
        result.put("total_record", recordPage.getTotal());
        result.put("total_page", recordPage.getPages());
        result.put("current_data", recordPage.getRecords().stream()
                .map(this::convertToCouponRecordVO)
                .collect(Collectors.toList()));

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
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }

        // 检查是否过期
        if (record.getEndTime() != null && record.getEndTime().before(new Date())) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }

        // 更新优惠券状态为已使用
        int rows = couponRecordManager.updateUseState(
                recordId,
                CouponStateEnum.USED.name(),
                new Date(),
                orderId,
                actualDiscountAmount
        );

        if (rows <= 0) {
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }

        log.info("用户使用优惠券成功. recordId:{}, userId:{}, orderId:{}",
                recordId, loginUser.getId(), orderId);
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





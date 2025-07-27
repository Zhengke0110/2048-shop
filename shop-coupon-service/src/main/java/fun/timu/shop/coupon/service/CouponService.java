package fun.timu.shop.coupon.service;

import fun.timu.shop.common.enums.CouponCategoryEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.model.DO.CouponDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zhengke
 * @description 针对表【coupon】的数据库操作Service
 * @createDate 2025-07-26 11:16:47
 */
public interface CouponService {
    /**
     * 分页查询优惠券
     *
     * @param page
     * @param size
     * @return
     */
    Map<String, Object> pageCouponActivity(int page, int size);

    /**
     * 领取优惠券接口
     *
     * @param couponId 优惠券ID
     * @param category 优惠券类别
     * @return 结果
     */
    JsonData addCoupon(long couponId, CouponCategoryEnum category);

    /**
     * 新用户注册福利发放
     * 为新注册用户发放所有配置的新用户福利优惠券
     *
     * @param userId 用户ID
     * @return 发放结果
     */
    JsonData grantNewUserBenefits(Long userId);

    /**
     * 获取用户优惠券记录
     *
     * @param useState 使用状态
     * @param page 页码
     * @param size 页面大小
     * @return 分页结果
     */
    Map<String, Object> getUserCouponRecords(String useState, int page, int size);

    /**
     * 使用优惠券
     *
     * @param recordId 优惠券记录ID
     * @param orderId 订单ID
     * @param actualDiscountAmount 实际优惠金额
     * @return 结果
     */
    JsonData useCoupon(Long recordId, Long orderId, BigDecimal actualDiscountAmount);

    /**
     * 批量更新过期优惠券（定时任务）
     */
    void batchUpdateExpiredCoupons();
}

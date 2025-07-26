package fun.timu.shop.coupon.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.coupon.model.DO.CouponRecordDO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface CouponRecordManager extends IService<CouponRecordDO> {
    /**
     * 插入优惠券记录
     * @param couponRecordDO 优惠券记录对象
     * @return 影响行数
     */
    int insert(CouponRecordDO couponRecordDO);

    /**
     * 查询用户领取指定优惠券的数量
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 领取数量
     */
    Long selectCount(Long couponId, Long userId);

    /**
     * 查询今日指定优惠券的发放数量
     * @param couponId 优惠券ID
     * @return 今日发放数量
     */
    Long getTodayReceiveCount(Long couponId);

    /**
     * 查询用户的优惠券记录列表（分页）
     * @param userId 用户ID
     * @param useState 使用状态（可选）
     * @param page 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<CouponRecordDO> getUserCouponRecords(Long userId, String useState, int page, int size);

    /**
     * 更新优惠券记录状态
     * @param recordId 记录ID
     * @param useState 新状态
     * @param useTime 使用时间（可选）
     * @param orderId 订单ID（可选）
     * @param actualDiscountAmount 实际优惠金额（可选）
     * @return 影响行数
     */
    int updateUseState(Long recordId, String useState, Date useTime, Long orderId, BigDecimal actualDiscountAmount);

    /**
     * 查询即将过期的优惠券记录
     * @param expireDays 即将过期的天数
     * @return 即将过期的记录列表
     */
    List<CouponRecordDO> getExpiringSoonRecords(int expireDays);

    /**
     * 批量更新过期优惠券状态
     * @return 影响行数
     */
    int batchUpdateExpiredCoupons();
}

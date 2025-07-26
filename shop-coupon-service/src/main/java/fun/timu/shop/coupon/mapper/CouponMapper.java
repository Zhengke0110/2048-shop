package fun.timu.shop.coupon.mapper;

import fun.timu.shop.coupon.model.DO.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhengke
 * @description 针对表【coupon】的数据库操作Mapper
 * @createDate 2025-07-26 11:16:47
 * @Entity fun.timu.shop.coupon.model.DO.Coupon
 */
public interface CouponMapper extends BaseMapper<CouponDO> {
    /**
     * 扣减库存（已废弃，存在并发问题）
     */
    @Deprecated
    int reduceStock(@Param("couponId") long couponId);
    
    /**
     * 带库存检查的扣减库存（乐观锁）
     * @param couponId 优惠券ID
     * @param expectedStock 期望的库存数量
     * @return 影响行数
     */
    int reduceStockWithCheck(@Param("couponId") long couponId, @Param("expectedStock") int expectedStock);
    
    /**
     * 使用悲观锁扣减库存
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    int reduceStockWithLock(@Param("couponId") long couponId);
}





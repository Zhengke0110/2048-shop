package fun.timu.shop.coupon.mapper;

import fun.timu.shop.coupon.model.DO.CouponRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【coupon_record】的数据库操作Mapper
 * @createDate 2025-07-26 11:16:47
 * @Entity fun.timu.shop.coupon.model.DO.CouponRecord
 */
public interface CouponRecordMapper extends BaseMapper<CouponRecordDO> {

    int lockUseStateBatch(@Param("userId") Long userId, @Param("useState") String useState, @Param("lockCouponRecordIds") List<Long> lockCouponRecordIds);

    void updateState(@Param("couponRecordId") Long couponRecordId, @Param("useState") String useState);

}





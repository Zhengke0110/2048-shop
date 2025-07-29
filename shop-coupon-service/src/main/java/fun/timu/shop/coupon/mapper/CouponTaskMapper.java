package fun.timu.shop.coupon.mapper;

import fun.timu.shop.coupon.model.DO.CouponTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【coupon_task(优惠券锁定任务表)】的数据库操作Mapper
 * @createDate 2025-07-29 13:40:06
 * @Entity fun.timu.shop.coupon.model.DO.CouponTask
 */
public interface CouponTaskMapper extends BaseMapper<CouponTaskDO> {
    // 批量插入
    int insertBatch(@Param("couponTaskList") List<CouponTaskDO> couponTaskDOList);
}





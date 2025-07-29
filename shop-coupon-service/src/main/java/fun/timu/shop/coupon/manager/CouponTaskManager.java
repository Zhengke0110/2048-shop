package fun.timu.shop.coupon.manager;

import fun.timu.shop.coupon.model.DO.CouponTaskDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponTaskManager {
    int insertBatch(List<CouponTaskDO> couponTaskDOList);

    CouponTaskDO selectById(Long taskId);

    boolean updateEntity(CouponTaskDO taskDO, Long taskId);
}

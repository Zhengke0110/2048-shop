package fun.timu.shop.coupon.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.timu.shop.coupon.manager.CouponTaskManager;
import fun.timu.shop.coupon.mapper.CouponTaskMapper;
import fun.timu.shop.coupon.model.DO.CouponTaskDO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class CouponTaskMapperImpl implements CouponTaskManager {
    private final CouponTaskMapper couponTaskMapper;


    @Override
    public int insertBatch(List<CouponTaskDO> couponTaskDOList) {
        return couponTaskMapper.insertBatch(couponTaskDOList);
    }

    @Override
    public CouponTaskDO selectById(Long taskId) {
        return couponTaskMapper.selectOne(new QueryWrapper<CouponTaskDO>().eq("id", taskId));
    }

    @Override
    public boolean updateEntity(CouponTaskDO taskDO, Long taskId) {
        int row = couponTaskMapper.update(taskDO, new QueryWrapper<CouponTaskDO>().eq("id", taskDO));
        return row > 0;
    }


}

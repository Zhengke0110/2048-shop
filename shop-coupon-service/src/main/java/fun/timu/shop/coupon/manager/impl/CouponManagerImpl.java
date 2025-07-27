package fun.timu.shop.coupon.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.enums.CouponCategoryEnum;
import fun.timu.shop.common.enums.CouponPublishEnum;
import fun.timu.shop.coupon.manager.CouponManager;
import fun.timu.shop.coupon.mapper.CouponMapper;
import fun.timu.shop.coupon.model.DO.CouponDO;
import fun.timu.shop.coupon.model.DO.CouponRecordDO;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class CouponManagerImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponManager {
    private final CouponMapper couponMapper;

    public CouponManagerImpl(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }

    @Override
    public IPage<CouponDO> selectPage(Page<CouponDO> pageInfo) {
        IPage<CouponDO> couponDOIPage = couponMapper.selectPage(pageInfo, new QueryWrapper<CouponDO>()
                .eq("publish", CouponPublishEnum.PUBLISH)
                .eq("category", CouponCategoryEnum.PROMOTION)
                .orderByDesc("create_time"));
        return couponDOIPage;
    }

    @Override
    public CouponDO selectOne(Long couponId, String category) {
        CouponDO couponDO = couponMapper.selectOne(new QueryWrapper<CouponDO>()
                .eq("id", couponId)
                .eq("category", category));

        return couponDO;
    }

    @Override
    public int reduceStock(Long couponId) {
        return couponMapper.reduceStock(couponId);
    }

    @Override
    public int reduceStockWithCheck(Long couponId, int expectedStock) {
        return couponMapper.reduceStockWithCheck(couponId, expectedStock);
    }

    @Override
    public int reduceStockWithLock(Long couponId) {
        return couponMapper.reduceStockWithLock(couponId);
    }

    @Override
    public List<CouponDO> getAvailableNewUserCoupons() {
        Date now = new Date();

        return couponMapper.selectList(new QueryWrapper<CouponDO>()
                .eq("category", CouponCategoryEnum.NEW_USER.name())
                .eq("publish", CouponPublishEnum.PUBLISH.name())
                .gt("stock", 0)  // 库存大于0
                .eq("del_flag", 0)  // 未删除
                // 检查领取时间范围
                .and(wrapper -> wrapper
                        .isNull("receive_start_time")
                        .or()
                        .le("receive_start_time", now)
                )
                .and(wrapper -> wrapper
                        .isNull("receive_end_time")
                        .or()
                        .ge("receive_end_time", now)
                )
                // 检查优惠券有效期
                .and(wrapper -> wrapper
                        .isNull("start_time")
                        .or()
                        .le("start_time", now)
                )
                .and(wrapper -> wrapper
                        .isNull("end_time")
                        .or()
                        .ge("end_time", now)
                )
                .orderByAsc("id")  // 按ID排序，确保发放顺序一致
        );
    }
}

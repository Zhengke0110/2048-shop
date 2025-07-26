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
}

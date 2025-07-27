package fun.timu.shop.coupon.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.coupon.model.DO.CouponDO;

import java.util.List;

public interface CouponManager extends IService<CouponDO> {
    /**
     * 分页查询优惠券活动列表
     *
     * @param pageInfo 分页信息
     * @return 分页结果
     */
    IPage<CouponDO> selectPage(Page<CouponDO> pageInfo);

    /**
     * 根据ID和类别查询单个优惠券
     *
     * @param id       优惠券ID
     * @param category 优惠券类别
     * @return 优惠券信息
     */
    CouponDO selectOne(Long id, String category);

    /**
     * 扣减库存（已废弃，存在并发问题）
     *
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    @Deprecated
    int reduceStock(Long couponId);

    /**
     * 带库存检查的扣减库存（乐观锁）
     *
     * @param couponId      优惠券ID
     * @param expectedStock 期望的库存数量
     * @return 影响行数
     */
    int reduceStockWithCheck(Long couponId, int expectedStock);

    /**
     * 使用悲观锁扣减库存
     *
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    int reduceStockWithLock(Long couponId);

    /**
     * 查询所有可用的新用户优惠券
     *
     * @return 新用户优惠券列表
     */
    List<CouponDO> getAvailableNewUserCoupons();
}
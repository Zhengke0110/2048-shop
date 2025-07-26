package fun.timu.shop.coupon.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.enums.CouponStateEnum;
import fun.timu.shop.coupon.manager.CouponRecordManager;
import fun.timu.shop.coupon.mapper.CouponRecordMapper;
import fun.timu.shop.coupon.model.DO.CouponRecordDO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class CouponRecordManagerImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordManager {
    private final CouponRecordMapper couponRecordMapper;

    public CouponRecordManagerImpl(CouponRecordMapper couponRecordMapper) {
        this.couponRecordMapper = couponRecordMapper;
    }

    @Override
    public int insert(CouponRecordDO couponRecordDO) {
        return couponRecordMapper.insert(couponRecordDO);
    }

    @Override
    public Long selectCount(Long couponId, Long userId) {
        return couponRecordMapper.selectCount(new QueryWrapper<CouponRecordDO>()
                .eq("coupon_id", couponId)
                .eq("user_id", userId)
                .eq("del_flag", 0));
    }

    @Override
    public Long getTodayReceiveCount(Long couponId) {
        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        return couponRecordMapper.selectCount(new QueryWrapper<CouponRecordDO>()
                .eq("coupon_id", couponId)
                .between("create_time", startOfDay, endOfDay)
                .eq("del_flag", 0));
    }

    @Override
    public IPage<CouponRecordDO> getUserCouponRecords(Long userId, String useState, int page, int size) {
        Page<CouponRecordDO> pageInfo = new Page<>(page, size);
        QueryWrapper<CouponRecordDO> queryWrapper = new QueryWrapper<CouponRecordDO>()
                .eq("user_id", userId)
                .eq("del_flag", 0)
                .orderByDesc("create_time");

        if (useState != null && !useState.trim().isEmpty()) {
            queryWrapper.eq("use_state", useState);
        }

        return couponRecordMapper.selectPage(pageInfo, queryWrapper);
    }

    @Override
    public int updateUseState(Long recordId, String useState, Date useTime, Long orderId, BigDecimal actualDiscountAmount) {
        UpdateWrapper<CouponRecordDO> updateWrapper = new UpdateWrapper<CouponRecordDO>()
                .eq("id", recordId)
                .set("use_state", useState)
                .set("update_time", new Date());

        if (useTime != null) {
            updateWrapper.set("use_time", useTime);
        }
        if (orderId != null) {
            updateWrapper.set("order_id", orderId);
        }
        if (actualDiscountAmount != null) {
            updateWrapper.set("actual_discount_amount", actualDiscountAmount);
        }

        return couponRecordMapper.update(null, updateWrapper);
    }

    @Override
    public List<CouponRecordDO> getExpiringSoonRecords(int expireDays) {
        // 计算即将过期的时间点
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(expireDays);
        Date expireDate = Date.from(expireDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return couponRecordMapper.selectList(new QueryWrapper<CouponRecordDO>()
                .eq("use_state", CouponStateEnum.NEW.name())
                .le("end_time", expireDate)
                .gt("end_time", new Date())
                .eq("del_flag", 0));
    }

    @Override
    public int batchUpdateExpiredCoupons() {
        return couponRecordMapper.update(null, new UpdateWrapper<CouponRecordDO>()
                .eq("use_state", CouponStateEnum.NEW.name())
                .lt("end_time", new Date())
                .eq("del_flag", 0)
                .set("use_state", CouponStateEnum.EXPIRED.name())
                .set("update_time", new Date()));
    }
}

package fun.timu.shop.coupon.config;

import fun.timu.shop.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 优惠券定时任务配置
 */
@Slf4j
@Component
public class CouponScheduledTask {
    
    private final CouponService couponService;

    public CouponScheduledTask(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 每天凌晨1点执行批量更新过期优惠券
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void batchUpdateExpiredCoupons() {
        log.info("开始执行定时任务：批量更新过期优惠券");
        try {
            couponService.batchUpdateExpiredCoupons();
            log.info("定时任务执行完成：批量更新过期优惠券");
        } catch (Exception e) {
            log.error("定时任务执行失败：批量更新过期优惠券", e);
        }
    }

    /**
     * 每小时执行一次，用于实时更新过期优惠券
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyUpdateExpiredCoupons() {
        log.debug("开始执行每小时定时任务：更新过期优惠券");
        try {
            couponService.batchUpdateExpiredCoupons();
            log.debug("每小时定时任务执行完成：更新过期优惠券");
        } catch (Exception e) {
            log.error("每小时定时任务执行失败：更新过期优惠券", e);
        }
    }
}

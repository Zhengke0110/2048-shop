package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.service.CouponService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理接口（管理员使用）
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon/v1/admin")
@AllArgsConstructor
public class CouponAdminController {

    private final CouponService couponService;

    /**
     * 手动触发批量更新过期优惠券
     * 注意：实际生产环境中，这个应该通过定时任务自动执行
     */
    @PostMapping("/batch-update-expired")
    public JsonData batchUpdateExpiredCoupons() {
        try {
            couponService.batchUpdateExpiredCoupons();
            log.info("手动触发批量更新过期优惠券完成");
            return JsonData.buildSuccess("批量更新过期优惠券完成");
        } catch (Exception e) {
            log.error("批量更新过期优惠券失败", e);
            return JsonData.buildError("操作失败");
        }
    }
}

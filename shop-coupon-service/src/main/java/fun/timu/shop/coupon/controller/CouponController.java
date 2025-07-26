package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.enums.CouponCategoryEnum;
import fun.timu.shop.common.enums.CouponStateEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.model.request.UseCouponRequest;
import fun.timu.shop.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon/v1/coupon")
@Validated
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 分页查询优惠券活动列表
     */
    @GetMapping("pageCoupon")
    public JsonData pageCouponList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.pageCouponActivity(page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 领取促销优惠券
     */
    @GetMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotionCoupon(@PathVariable("coupon_id") long couponId) {
        return couponService.addCoupon(couponId, CouponCategoryEnum.PROMOTION);
    }

    /**
     * 领取新用户优惠券
     */
    @GetMapping("/add/new-user/{coupon_id}")
    public JsonData addNewUserCoupon(@PathVariable("coupon_id") long couponId) {
        return couponService.addCoupon(couponId, CouponCategoryEnum.NEW_USER);
    }

    /**
     * 领取任务优惠券
     */
    @GetMapping("/add/task/{coupon_id}")
    public JsonData addTaskCoupon(@PathVariable("coupon_id") long couponId) {
        return couponService.addCoupon(couponId, CouponCategoryEnum.TASK);
    }

    /**
     * 获取用户优惠券记录
     */
    @GetMapping("/user/records")
    public JsonData getUserCouponRecords(
            @RequestParam(value = "use_state", required = false) String useState,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        // 如果传入了状态参数，验证其有效性
        if (useState != null && !useState.trim().isEmpty()) {
            try {
                CouponStateEnum.valueOf(useState.toUpperCase());
            } catch (IllegalArgumentException e) {
                return JsonData.buildError("无效的优惠券状态: " + useState);
            }
        }

        Map<String, Object> pageMap = couponService.getUserCouponRecords(useState, page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 使用优惠券
     */
    @PostMapping("/use")
    public JsonData useCoupon(@Valid @RequestBody UseCouponRequest request) {
        return couponService.useCoupon(
                request.getRecordId(),
                request.getOrderId(),
                request.getActualDiscountAmount()
        );
    }

    /**
     * 使用优惠券（兼容旧版本的表单提交方式）
     */
    @PostMapping("/use-form")
    public JsonData useCouponByForm(
            @RequestParam("record_id") Long recordId,
            @RequestParam("order_id") Long orderId,
            @RequestParam(value = "actual_discount_amount", required = false) BigDecimal actualDiscountAmount) {

        return couponService.useCoupon(recordId, orderId, actualDiscountAmount);
    }

    /**
     * 获取用户可用优惠券记录
     */
    @GetMapping("/user/available")
    public JsonData getUserAvailableCoupons(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.NEW.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 获取用户已使用优惠券记录
     */
    @GetMapping("/user/used")
    public JsonData getUserUsedCoupons(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.USED.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 获取用户已过期优惠券记录
     */
    @GetMapping("/user/expired")
    public JsonData getUserExpiredCoupons(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.EXPIRED.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }
}

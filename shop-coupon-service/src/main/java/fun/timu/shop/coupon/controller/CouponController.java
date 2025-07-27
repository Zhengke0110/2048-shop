package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.enums.CouponCategoryEnum;
import fun.timu.shop.common.enums.CouponStateEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.controller.request.UseCouponRequest;
import fun.timu.shop.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
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
    public JsonData pageCouponList(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {

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
     * 领取新用户优惠券（普通用户调用）
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
     * RPC - 新用户注册福利发放
     * 该接口用于为新注册用户自动发放所有新用户福利优惠券
     *
     * @param request HTTP请求对象，用于获取调用方信息
     * @return 发放结果
     */
    @PostMapping("/rpc/new-user-benefits")
    public JsonData grantNewUserBenefits(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"user-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        // 从请求体中获取用户ID
        Long userId = null;
        try {
            Object userIdObj = requestBody.get("userId");
            if (userIdObj != null) {
                userId = Long.valueOf(userIdObj.toString());
            }
        } catch (Exception e) {
            log.error("解析用户ID失败", e);
            return JsonData.buildError("用户ID格式错误");
        }

        if (userId == null) {
            return JsonData.buildError("用户ID不能为空");
        }

        log.info("RPC接口被调用 - 新用户注册福利发放: userId={}, rpcSource={}", userId, rpcSource);

        try {
            // 调用服务层方法，为新用户发放所有福利优惠券
            return couponService.grantNewUserBenefits(userId);
        } catch (Exception e) {
            log.error("新用户福利发放失败: userId={}", userId, e);
            return JsonData.buildError("福利发放失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户优惠券记录
     */
    @GetMapping("/user/records")
    public JsonData getUserCouponRecords(@RequestParam(value = "use_state", required = false) String useState, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {

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
        return couponService.useCoupon(request.getRecordId(), request.getOrderId(), request.getActualDiscountAmount());
    }

    /**
     * 使用优惠券（兼容旧版本的表单提交方式）
     */
    @PostMapping("/use-form")
    public JsonData useCouponByForm(@RequestParam("record_id") Long recordId, @RequestParam("order_id") Long orderId, @RequestParam(value = "actual_discount_amount", required = false) BigDecimal actualDiscountAmount) {

        return couponService.useCoupon(recordId, orderId, actualDiscountAmount);
    }

    /**
     * 获取用户可用优惠券记录
     */
    @GetMapping("/user/available")
    public JsonData getUserAvailableCoupons(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.NEW.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 获取用户已使用优惠券记录
     */
    @GetMapping("/user/used")
    public JsonData getUserUsedCoupons(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.USED.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 获取用户已过期优惠券记录
     */
    @GetMapping("/user/expired")
    public JsonData getUserExpiredCoupons(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageMap = couponService.getUserCouponRecords(CouponStateEnum.EXPIRED.name(), page, size);
        return JsonData.buildSuccess(pageMap);
    }
}

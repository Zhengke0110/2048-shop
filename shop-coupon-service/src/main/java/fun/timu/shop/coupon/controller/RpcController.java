package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.LockCouponRecordRequest;
import fun.timu.shop.common.request.NewUserBenefitsRequest;
import fun.timu.shop.coupon.service.CouponRecordService;
import fun.timu.shop.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 优惠券服务 RPC 接口控制器
 * 专门处理服务间调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon/v1/rpc")
@RequiredArgsConstructor
public class RpcController {

    private final CouponService couponService;
    private final CouponRecordService couponRecordService;

    /**
     * RPC - 新用户注册福利发放
     * 该接口用于为新注册用户自动发放所有新用户福利优惠券
     *
     * @param benefitsRequest 新用户福利发放请求
     * @param request         HTTP请求对象，用于获取调用方信息
     * @return 发放结果
     */
    @PostMapping("/new-user-benefits")
    public JsonData grantNewUserBenefits(@RequestBody NewUserBenefitsRequest benefitsRequest, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"shop-user-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        if (benefitsRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        Long userId = benefitsRequest.getUserId();
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
     * RPC - 锁定优惠券记录（下单时调用）
     * 该接口用于订单服务在下单时锁定用户选择的优惠券
     *
     * @param lockRequest 锁定优惠券记录请求
     * @param request     HTTP请求对象，用于获取调用方信息
     * @return 锁定结果
     */
    @PostMapping("/lock")
    public JsonData lockCouponRecords(@RequestBody LockCouponRecordRequest lockRequest, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"shop-order-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        if (lockRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        if (lockRequest.getOrderOutTradeNo() == null || lockRequest.getOrderOutTradeNo().trim().isEmpty()) {
            return JsonData.buildError("订单号不能为空");
        }

        if (lockRequest.getLockCouponRecordIds() == null || lockRequest.getLockCouponRecordIds().isEmpty()) {
            return JsonData.buildError("优惠券记录ID列表不能为空");
        }

        log.info("RPC接口被调用 - 锁定优惠券记录: orderOutTradeNo={}, recordIds={}, rpcSource={}",
                lockRequest.getOrderOutTradeNo(), lockRequest.getLockCouponRecordIds(), rpcSource);

        try {
            // 调用服务层方法
            return couponRecordService.lockCouponRecords(lockRequest);
        } catch (Exception e) {
            log.error("锁定优惠券记录失败: lockRequest={}", lockRequest, e);
            return JsonData.buildError("锁定优惠券失败: " + e.getMessage());
        }
    }

    /**
     * RPC - 根据ID查询优惠券记录详情
     * 该接口用于其他服务查询优惠券记录的详细信息
     *
     * @param recordId 优惠券记录ID
     * @param request  HTTP请求对象，用于获取调用方信息
     * @return 优惠券记录详情
     */
    @GetMapping("/detail/{recordId}")
    public JsonData findById(@PathVariable Long recordId, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        // 允许订单服务和其他合法服务调用
        if (!"shop-order-service".equals(rpcSource) && !"shop-user-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        if (recordId == null || recordId <= 0) {
            return JsonData.buildError("记录ID必须大于0");
        }

        log.info("RPC接口被调用 - 查询优惠券记录详情: recordId={}, rpcSource={}", recordId, rpcSource);

        try {
            return couponRecordService.findById(recordId);
        } catch (Exception e) {
            log.error("查询优惠券记录详情失败: recordId={}", recordId, e);
            return JsonData.buildError("查询优惠券记录失败: " + e.getMessage());
        }
    }
}

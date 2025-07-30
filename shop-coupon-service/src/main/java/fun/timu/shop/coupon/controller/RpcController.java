package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.controller.request.LockCouponRecordRequest;
import fun.timu.shop.coupon.service.CouponRecordService;
import fun.timu.shop.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * @param requestBody 请求体，包含用户ID
     * @param request HTTP请求对象，用于获取调用方信息
     * @return 发放结果
     */
    @PostMapping("/new-user-benefits")
    public JsonData grantNewUserBenefits(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"shop-user-service".equals(rpcSource)) {
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
     * RPC - 锁定优惠券记录（下单时调用）
     * 该接口用于订单服务在下单时锁定用户选择的优惠券
     *
     * @param requestBody 包含锁定请求参数的Map
     * @param request     HTTP请求对象，用于获取调用方信息
     * @return 锁定结果
     */
    @PostMapping("/lock")
    public JsonData lockCouponRecords(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"shop-order-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        log.info("RPC接口被调用 - 锁定优惠券记录: rpcSource={}, requestBody={}", rpcSource, requestBody);

        try {
            // 构建LockCouponRecordRequest对象
            LockCouponRecordRequest lockRequest = buildLockRequest(requestBody);
            
            if (lockRequest == null) {
                return JsonData.buildError("请求参数格式错误");
            }

            log.info("锁定优惠券记录: orderOutTradeNo={}, recordIds={}", 
                    lockRequest.getOrderOutTradeNo(), lockRequest.getLockCouponRecordIds());

            // 调用服务层方法
            return couponRecordService.lockCouponRecords(lockRequest);

        } catch (Exception e) {
            log.error("锁定优惠券记录失败: requestBody={}", requestBody, e);
            return JsonData.buildError("锁定优惠券失败: " + e.getMessage());
        }
    }

    /**
     * 构建锁定请求对象
     */
    private LockCouponRecordRequest buildLockRequest(Map<String, Object> requestBody) {
        try {
            LockCouponRecordRequest request = new LockCouponRecordRequest();
            
            // 获取订单号
            Object orderOutTradeNoObj = requestBody.get("orderOutTradeNo");
            if (orderOutTradeNoObj != null) {
                request.setOrderOutTradeNo(orderOutTradeNoObj.toString());
            }

            // 获取用户ID（RPC调用必须传递）
            Object userIdObj = requestBody.get("userId");
            if (userIdObj != null) {
                request.setUserId(Long.valueOf(userIdObj.toString()));
            }

            // 获取优惠券记录ID列表
            @SuppressWarnings("unchecked")
            List<Object> recordIdObjs = (List<Object>) requestBody.get("lockCouponRecordIds");
            if (recordIdObjs != null) {
                List<Long> recordIds = recordIdObjs.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .toList();
                request.setLockCouponRecordIds(recordIds);
            }

            return request;
        } catch (Exception e) {
            log.error("构建锁定请求对象失败", e);
            return null;
        }
    }
}

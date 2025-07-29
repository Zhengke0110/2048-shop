package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.controller.request.LockCouponRecordRequest;
import fun.timu.shop.coupon.service.CouponRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/coupon/v1/record")
@Validated
@AllArgsConstructor
public class CouponRecordController {
    private final CouponRecordService couponRecordService;

    /**
     * 分页查询用户优惠券记录
     *
     * @param page 页码，从1开始
     * @param size 每页大小，最大100
     * @return 分页数据
     */
    @GetMapping("/page")
    public JsonData page(@RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
                         @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        log.info("分页查询优惠券记录: page={}, size={}", page, size);
        return couponRecordService.page(page, size);
    }

    /**
     * 根据ID查询优惠券记录详情
     *
     * @param recordId 优惠券记录ID
     * @return 优惠券记录详情
     */
    @GetMapping("/detail/{recordId}")
    public JsonData findById(@PathVariable @Min(value = 1, message = "记录ID必须大于0") long recordId) {
        log.info("查询优惠券记录详情: recordId={}", recordId);
        return couponRecordService.findById(recordId);
    }

    /**
     * RPC - 锁定优惠券记录（下单时调用）
     * 该接口用于订单服务在下单时锁定用户选择的优惠券
     *
     * @param requestBody 包含锁定请求参数的Map
     * @param request     HTTP请求对象，用于获取调用方信息
     * @return 锁定结果
     */
    @PostMapping("/rpc/lock")
    public JsonData lockCouponRecords(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (!"order-service".equals(rpcSource)) {
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

            // 获取优惠券记录ID列表
            Object lockCouponRecordIdsObj = requestBody.get("lockCouponRecordIds");
            if (lockCouponRecordIdsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> idList = (java.util.List<Object>) lockCouponRecordIdsObj;
                java.util.List<Long> longIdList = new java.util.ArrayList<>();
                
                for (Object idObj : idList) {
                    if (idObj != null) {
                        longIdList.add(Long.valueOf(idObj.toString()));
                    }
                }
                request.setLockCouponRecordIds(longIdList);
            }

            // 验证必要参数
            if (request.getOrderOutTradeNo() == null || request.getOrderOutTradeNo().trim().isEmpty()) {
                log.warn("订单号为空");
                return null;
            }

            if (request.getLockCouponRecordIds() == null || request.getLockCouponRecordIds().isEmpty()) {
                log.warn("优惠券记录ID列表为空");
                return null;
            }

            return request;

        } catch (Exception e) {
            log.error("构建锁定请求对象失败", e);
            return null;
        }
    }
}

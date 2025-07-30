package fun.timu.shop.order.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.QueryOrderStateRequest;
import fun.timu.shop.order.service.ProductOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务 RPC 接口控制器
 * 专门处理服务间调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/order/v1/rpc")
@RequiredArgsConstructor
public class RpcController {

    private final ProductOrderService productOrderService;

    /**
     * RPC - 查询订单状态
     * 该接口用于其他微服务查询订单状态
     *
     * @param queryRequest 查询订单状态请求
     * @param request      HTTP请求对象，用于获取调用方信息
     * @return 订单状态信息
     */
    @PostMapping("/query-state")
    public JsonData queryProductOrderState(@RequestBody QueryOrderStateRequest queryRequest, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        if (rpcSource == null || rpcSource.trim().isEmpty()) {
            log.warn("缺少RPC调用来源标识");
            return JsonData.buildError("非法的RPC调用");
        }

        // 验证允许的RPC调用来源
        if (!"shop-coupon-service".equals(rpcSource) && !"shop-user-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        if (queryRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        String outTradeNo = queryRequest.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            return JsonData.buildError("订单号不能为空");
        }

        log.info("RPC接口被调用 - 查询订单状态: outTradeNo={}, rpcSource={}", outTradeNo, rpcSource);

        try {
            String state = productOrderService.queryProductOrderState(outTradeNo);

            if (state == null || state.isEmpty()) {
                log.warn("订单不存在: outTradeNo={}", outTradeNo);
                return JsonData.buildError("订单不存在");
            }

            log.info("查询订单状态成功: outTradeNo={}, state={}", outTradeNo, state);
            return JsonData.buildSuccess(state);
        } catch (Exception e) {
            log.error("查询订单状态失败: outTradeNo={}", outTradeNo, e);
            return JsonData.buildError("查询订单状态失败: " + e.getMessage());
        }
    }
}

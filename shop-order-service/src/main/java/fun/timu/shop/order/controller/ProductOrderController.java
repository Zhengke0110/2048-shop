package fun.timu.shop.order.controller;

import fun.timu.shop.common.enums.OrderPayTypeEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.controller.request.ConfirmOrderRequest;
import fun.timu.shop.order.service.ProductOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order/v1/order")
@AllArgsConstructor
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    /**
     * RPC - 查询订单状态
     * 该接口用于其他微服务查询订单状态
     *
     * @param requestBody 请求体，包含订单号
     * @param request HTTP请求对象，用于获取调用方信息
     * @return 订单状态信息
     */
    @PostMapping("/rpc/query-state")
    public JsonData queryProductOrderState(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");
        
        if (rpcSource == null || rpcSource.trim().isEmpty()) {
            log.warn("缺少RPC调用来源标识");
            return JsonData.buildError("非法的RPC调用");
        }

        // 从请求体中获取订单号
        String outTradeNo = null;
        try {
            Object outTradeNoObj = requestBody.get("outTradeNo");
            if (outTradeNoObj != null) {
                outTradeNo = outTradeNoObj.toString();
            }
        } catch (Exception e) {
            log.error("解析订单号失败", e);
            return JsonData.buildError("订单号格式错误");
        }

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

    /**
     * 提交订单
     *
     * @param orderRequest 订单对象
     * @param response     HTTP响应对象
     */
    @PostMapping("confirm")
    public void confirmOrder(@RequestBody ConfirmOrderRequest orderRequest, HttpServletResponse response) {

        JsonData jsonData = productOrderService.confirmOrder(orderRequest);

        if (jsonData.getCode() == 0) {

            String client = orderRequest.getClientType();
            String payType = orderRequest.getPayType();

            //如果是支付宝网页支付，都是跳转网页，APP除外
            if (OrderPayTypeEnum.ALIPAY.getType().equalsIgnoreCase(payType)) {

                log.info("创建支付宝订单成功:{}", orderRequest.toString());

                if ("H5".equalsIgnoreCase(client)) {
                    writeData(response, jsonData);

                } else if ("APP".equalsIgnoreCase(client)) {
                    //APP SDK支付  TODO
                }

            } else if (OrderPayTypeEnum.WECHAT.getType().equalsIgnoreCase(payType)) {

                //微信支付 TODO
            }

        } else {

            log.error("创建订单失败{}", jsonData.toString());

        }
    }

    private void writeData(HttpServletResponse response, JsonData jsonData) {

        try {
            response.setContentType("text/html;charset=UTF8");
            response.getWriter().write(jsonData.getData().toString());
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("写出Html异常：{}", e);
        }

    }
}

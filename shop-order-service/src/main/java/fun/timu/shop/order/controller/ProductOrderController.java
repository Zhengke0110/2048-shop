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

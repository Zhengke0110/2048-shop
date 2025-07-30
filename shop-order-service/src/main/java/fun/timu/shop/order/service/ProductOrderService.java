package fun.timu.shop.order.service;

import fun.timu.shop.common.model.OrderMessage;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.controller.request.ConfirmOrderRequest;
import fun.timu.shop.order.model.DO.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zhengke
 * @description 针对表【product_order(订单表)】的数据库操作Service
 * @createDate 2025-07-29 10:49:11
 */
public interface ProductOrderService {

    /**
     * 创建订单
     *
     * @param orderRequest
     * @return
     */
    JsonData confirmOrder(ConfirmOrderRequest orderRequest);

    /**
     * 查询订单状态
     *
     * @param outTradeNo
     * @return
     */
    String queryProductOrderState(String outTradeNo);

    /**
     * 队列监听，定时关单
     * @param orderMessage
     * @return
     */
    boolean closeProductOrder(OrderMessage orderMessage);
}

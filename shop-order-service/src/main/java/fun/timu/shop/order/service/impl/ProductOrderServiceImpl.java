package fun.timu.shop.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.controller.request.ConfirmOrderRequest;
import fun.timu.shop.order.manager.ProductOrderItemManager;
import fun.timu.shop.order.manager.ProductOrderManager;
import fun.timu.shop.order.model.DO.ProductOrderDO;
import fun.timu.shop.order.service.ProductOrderService;
import fun.timu.shop.order.mapper.ProductOrderMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhengke
 * @description 针对表【product_order(订单表)】的数据库操作Service实现
 * @createDate 2025-07-29 10:49:11
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProductOrderServiceImpl implements ProductOrderService {
    private final ProductOrderManager orderManager;
    private final ProductOrderItemManager orderItemManager;

    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        return null;
    }

    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = orderManager.selectOne(outTradeNo);
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }
}





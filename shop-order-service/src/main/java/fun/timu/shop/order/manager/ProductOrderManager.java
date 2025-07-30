package fun.timu.shop.order.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.order.model.DO.ProductOrderDO;

public interface ProductOrderManager extends IService<ProductOrderDO> {
    ProductOrderDO selectOne(String outTradeNo);

    boolean insert(ProductOrderDO productOrderDO);
}

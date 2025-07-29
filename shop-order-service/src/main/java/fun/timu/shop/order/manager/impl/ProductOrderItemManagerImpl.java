package fun.timu.shop.order.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.order.manager.ProductOrderItemManager;
import fun.timu.shop.order.mapper.ProductOrderItemMapper;
import fun.timu.shop.order.model.DO.ProductOrderItemDO;
import org.springframework.stereotype.Component;

@Component
public class ProductOrderItemManagerImpl extends ServiceImpl<ProductOrderItemMapper, ProductOrderItemDO> implements ProductOrderItemManager {
}

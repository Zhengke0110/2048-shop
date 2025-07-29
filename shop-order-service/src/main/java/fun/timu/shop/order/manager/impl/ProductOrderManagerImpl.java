package fun.timu.shop.order.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.order.manager.ProductOrderManager;
import fun.timu.shop.order.mapper.ProductOrderMapper;
import fun.timu.shop.order.model.DO.ProductOrderDO;
import org.springframework.stereotype.Component;

@Component
public class ProductOrderManagerImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderManager {
}

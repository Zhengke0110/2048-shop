package fun.timu.shop.product.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.product.manager.ProductManager;
import fun.timu.shop.product.mapper.ProductMapper;
import fun.timu.shop.product.model.DO.ProductDO;
import org.springframework.stereotype.Component;

@Component
public class ProductManagerImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductManager {
}

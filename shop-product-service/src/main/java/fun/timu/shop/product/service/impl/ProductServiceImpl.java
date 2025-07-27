package fun.timu.shop.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.product.manager.ProductManager;
import fun.timu.shop.product.model.DO.ProductDO;
import fun.timu.shop.product.service.ProductService;
import fun.timu.shop.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

/**
 * @author zhengke
 * @description 针对表【product(商品表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:38:53
 */
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductManager productManager;

    public ProductServiceImpl(ProductManager productManager) {
        this.productManager = productManager;
    }
}





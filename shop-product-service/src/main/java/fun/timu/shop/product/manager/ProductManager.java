package fun.timu.shop.product.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.product.model.DO.ProductDO;
import org.springframework.stereotype.Component;

@Component
public interface ProductManager extends IService<ProductDO> {
}

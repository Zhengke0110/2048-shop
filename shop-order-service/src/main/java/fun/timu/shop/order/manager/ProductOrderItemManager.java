package fun.timu.shop.order.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.order.model.DO.ProductOrderItemDO;

import java.util.List;

public interface ProductOrderItemManager extends IService<ProductOrderItemDO> {
    void insertBatch(List<ProductOrderItemDO> list);
}

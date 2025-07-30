package fun.timu.shop.product.manager;

import fun.timu.shop.product.model.DO.ProductTaskDO;

public interface ProductTaskManager {
    boolean insert(ProductTaskDO productTaskDO);

    ProductTaskDO selectById(Long id);

    boolean updateEntity(ProductTaskDO productTaskDO, Long taskId);
}

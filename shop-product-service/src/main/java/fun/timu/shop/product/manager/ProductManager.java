package fun.timu.shop.product.manager;

import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.product.model.DO.ProductDO;

import java.util.List;

/**
 * 商品管理器接口
 *
 * @author zhengke
 */
public interface ProductManager {

    /**
     * 根据查询条件获取商品列表
     */
    List<ProductDO> listByQuery(ProductQueryRequest queryRequest);

    /**
     * 根据ID获取未删除的商品
     */
    ProductDO getByIdNotDeleted(Long id);

    /**
     * 保存商品
     */
    boolean save(ProductDO productDO);

    /**
     * 根据ID更新商品
     */
    boolean updateById(ProductDO productDO);

    /**
     * 逻辑删除商品
     */
    boolean logicDeleteById(Long id);

    /**
     * 批量逻辑删除商品
     */
    boolean logicDeleteBatchByIds(List<Long> ids);

    /**
     * 更新商品状态
     */
    boolean updateStatusById(Long id, Integer status);

    /**
     * 批量更新商品状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 扣减库存
     */
    boolean decreaseStock(Long id, Integer quantity);

    /**
     * 增加库存
     */
    boolean increaseStock(Long id, Integer quantity);

    /**
     * 锁定库存
     */
    boolean lockStock(Long id, Integer quantity);

    /**
     * 释放锁定库存
     */
    boolean releaseLockStock(Long id, Integer quantity);

    /**
     * 更新销售数量
     */
    boolean updateSalesCount(Long id, Integer salesCount);
}

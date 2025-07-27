package fun.timu.shop.product.service;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.ProductCreateRequest;
import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.product.controller.request.ProductUpdateRequest;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【product(商品表)】的数据库操作Service
 * @createDate 2025-07-27 16:38:53
 */
public interface ProductService {

    /**
     * 查询商品列表
     */
    JsonData list(ProductQueryRequest queryRequest);

    /**
     * 根据ID获取商品详情
     */
    JsonData getById(Long id);

    /**
     * 创建商品
     */
    JsonData create(ProductCreateRequest createRequest);

    /**
     * 更新商品
     */
    JsonData update(Long id, ProductUpdateRequest updateRequest);

    /**
     * 删除商品
     */
    JsonData delete(Long id);

    /**
     * 批量删除商品
     */
    JsonData batchDelete(List<Long> ids);

    /**
     * 更新商品状态
     */
    JsonData updateStatus(Long id, Integer status);

    /**
     * 批量更新商品状态
     */
    JsonData batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 扣减库存
     */
    JsonData decreaseStock(Long id, Integer quantity);

    /**
     * 增加库存
     */
    JsonData increaseStock(Long id, Integer quantity);

    /**
     * 锁定库存
     */
    JsonData lockStock(Long id, Integer quantity);

    /**
     * 释放锁定库存
     */
    JsonData releaseLockStock(Long id, Integer quantity);

    // ==================== RPC 相关方法 ====================

    /**
     * 批量获取商品详情（RPC接口用）
     */
    JsonData getBatchProductDetails(List<Long> productIds);

    /**
     * 验证商品库存（RPC接口用）
     */
    JsonData validateStock(Long productId, Integer quantity);
}

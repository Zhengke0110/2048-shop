package fun.timu.shop.order.service;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.controller.request.AddToCartRequest;
import fun.timu.shop.order.controller.request.BatchCartRequest;
import fun.timu.shop.order.controller.request.UpdateCartRequest;

import java.util.List;

/**
 * 购物车服务接口
 * 
 * @author zhengke
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * 
     * @param request 添加请求
     * @return 操作结果
     */
    JsonData addToCart(AddToCartRequest request);

    /**
     * 更新购物车商品数量
     * 
     * @param request 更新请求
     * @return 操作结果
     */
    JsonData updateCart(UpdateCartRequest request);

    /**
     * 从购物车删除商品
     * 
     * @param productId 商品ID
     * @return 操作结果
     */
    JsonData removeFromCart(Long productId);

    /**
     * 批量删除购物车商品
     * 
     * @param request 批量删除请求
     * @return 操作结果
     */
    JsonData batchRemove(BatchCartRequest request);

    /**
     * 清空购物车
     * 
     * @return 操作结果
     */
    JsonData clearCart();

    /**
     * 获取购物车列表
     * 
     * @return 购物车数据
     */
    JsonData getCartList();

    /**
     * 获取购物车商品数量
     * 
     * @return 商品数量
     */
    JsonData getCartCount();

    /**
     * 获取购物车详情（包含商品信息）
     * 
     * @return 购物车详情
     */
    JsonData getCartDetails();

    /**
     * 验证购物车商品有效性
     * 
     * @return 验证结果
     */
    JsonData validateCartItems();

    /**
     * 确认购物车商品信息
     * 根据指定的商品ID列表过滤购物车商品，并清空对应的购物项
     * 
     * @param productIds 需要确认的商品ID列表
     * @return 确认的商品列表
     */
    JsonData confirmCartItems(List<Long> productIds);
}

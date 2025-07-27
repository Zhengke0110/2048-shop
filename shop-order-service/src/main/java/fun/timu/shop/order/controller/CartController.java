package fun.timu.shop.order.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.controller.request.AddToCartRequest;
import fun.timu.shop.order.controller.request.BatchCartRequest;
import fun.timu.shop.order.controller.request.UpdateCartRequest;
import fun.timu.shop.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 购物车控制器
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/order/v1/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车
     *
     * @param request 添加请求
     * @return 操作结果
     */
    @PostMapping("/add")
    public JsonData addToCart(@Valid @RequestBody AddToCartRequest request) {
        return cartService.addToCart(request);
    }

    /**
     * 更新购物车商品数量
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @PutMapping("/update")
    public JsonData updateCart(@Valid @RequestBody UpdateCartRequest request) {
        return cartService.updateCart(request);
    }

    /**
     * 从购物车删除商品
     *
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/remove/{productId}")
    public JsonData removeFromCart(@PathVariable Long productId) {
        return cartService.removeFromCart(productId);
    }

    /**
     * 批量删除购物车商品
     *
     * @param request 批量删除请求
     * @return 操作结果
     */
    @DeleteMapping("/batch/remove")
    public JsonData batchRemove(@Valid @RequestBody BatchCartRequest request) {
        return cartService.batchRemove(request);
    }

    /**
     * 清空购物车
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public JsonData clearCart() {
        return cartService.clearCart();
    }

    /**
     * 获取购物车商品列表（简单列表）
     *
     * @return 购物车商品列表
     */
    @GetMapping("/list")
    public JsonData getCartList() {
        return cartService.getCartList();
    }

    /**
     * 获取购物车商品数量
     *
     * @return 商品数量
     */
    @GetMapping("/count")
    public JsonData getCartCount() {
        return cartService.getCartCount();
    }

    /**
     * 获取购物车详情（包含商品信息）
     *
     * @return 购物车详情
     */
    @GetMapping("/details")
    public JsonData getCartDetails() {
        return cartService.getCartDetails();
    }

    /**
     * 验证购物车商品有效性
     *
     * @return 验证结果
     */
    @GetMapping("/validate")
    public JsonData validateCartItems() {
        return cartService.validateCartItems();
    }
}

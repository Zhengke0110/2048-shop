package fun.timu.shop.order.feign;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.BatchProductRequest;
import fun.timu.shop.common.request.LockProductRequest;
import fun.timu.shop.common.request.ValidateStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商品服务Feign客户端
 * 用于调用商品服务的远程接口
 *
 * @author zhengke
 */
@FeignClient(name = "shop-product-service", path = "/api/product/v1")
public interface ProductFeignService {

    /**
     * 根据商品ID获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/product/{productId}")
    JsonData getProductById(@PathVariable("productId") Long productId);

    /**
     * 批量获取商品详情
     *
     * @param request 批量获取商品详情请求
     * @return 商品详情列表
     */
    @PostMapping("/rpc/batch")
    JsonData getBatchProductDetails(@RequestBody BatchProductRequest request);

    /**
     * RPC - 验证商品库存
     *
     * @param request 验证库存请求
     * @return 验证结果
     */
    @PostMapping("/rpc/stock/validate")
    JsonData validateStock(@RequestBody ValidateStockRequest request);

    /**
     * RPC - 锁定商品库存
     *
     * @param lockProductRequest 锁定库存请求
     * @return 锁定结果
     */
    @PostMapping("/rpc/stock/lock")
    JsonData lockProductStock(@RequestBody LockProductRequest lockProductRequest);
}

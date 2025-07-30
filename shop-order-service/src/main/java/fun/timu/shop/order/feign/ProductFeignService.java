package fun.timu.shop.order.feign;

import fun.timu.shop.common.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

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
     * @param request 请求参数，包含productIds
     * @return 商品详情列表
     */
    @PostMapping("/rpc/batch")
    JsonData getBatchProductDetails(@RequestBody Map<String, Object> request);

    /**
     * RPC - 验证商品库存
     *
     * @param request 请求参数，包含productId和quantity
     * @return 验证结果
     */
    @PostMapping("/rpc/stock/validate")
    JsonData validateStock(@RequestBody Map<String, Object> request);
}

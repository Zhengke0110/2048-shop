package fun.timu.shop.order.client;

import fun.timu.shop.common.components.HttpRpcClient;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.util.RpcSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务RPC客户端
 * 用于调用商品相关的远程服务
 *
 * @author zhengke
 */
@Slf4j
@Component
public class ProductRpcClient {

    private final HttpRpcClient httpRpcClient;

    @Value("${rpc.product.service.url:http://localhost:9004}")
    private String productServiceUrl;

    public ProductRpcClient(HttpRpcClient httpRpcClient) {
        this.httpRpcClient = httpRpcClient;
    }

    /**
     * 根据商品ID获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    public JsonData getProductById(Long productId) {
        String url = productServiceUrl + "/api/product/v1/product/" + productId;

        log.info("发送RPC请求 - 获取商品详情: productId={}", productId);

        JsonData result = httpRpcClient.get(url);

        if (result != null && result.getCode() == 0) {
            log.info("获取商品详情成功: productId={}", productId);
        } else {
            log.error("获取商品详情失败: productId={}, result={}", productId, result);
        }

        return result;
    }

    /**
     * 批量获取商品详情
     *
     * @param productIds 商品ID列表
     * @return 商品详情列表
     */
    public JsonData getBatchProductDetails(List<Long> productIds) {
        String url = productServiceUrl + "/api/product/v1/product/batch";

        Map<String, Object> request = new HashMap<>();
        request.put("productIds", productIds);

        // 生成安全的RPC头部
        Map<String, String> headers = RpcSecurityUtil.generateSecurityHeaders("order-service", "POST", "/api/product/v1/product/batch");
        headers.put("Content-Type", "application/json");

        log.info("发送安全RPC请求 - 批量获取商品详情: productIds={}", productIds);

        JsonData result = httpRpcClient.postWithHeaders(url, request, headers);

        if (result != null && result.getCode() == 0) {
            log.info("批量获取商品详情成功: count={}", productIds.size());
        } else {
            log.error("批量获取商品详情失败: productIds={}, result={}", productIds, result);
        }

        return result;
    }

    /**
     * 验证商品库存
     *
     * @param productId 商品ID
     * @param quantity  需要的数量
     * @return 验证结果
     */
    public JsonData validateStock(Long productId, Integer quantity) {
        String url = productServiceUrl + "/api/product/v1/product/rpc/stock/validate";

        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        request.put("quantity", quantity);

        // 生成安全的RPC头部
        Map<String, String> headers = RpcSecurityUtil.generateSecurityHeaders("order-service", "POST", "/api/product/v1/product/rpc/stock/validate");
        headers.put("Content-Type", "application/json");

        log.info("发送安全RPC请求 - 验证商品库存: productId={}, quantity={}", productId, quantity);

        JsonData result = httpRpcClient.postWithHeaders(url, request, headers);

        if (result != null && result.getCode() == 0) {
            log.info("验证商品库存成功: productId={}, quantity={}", productId, quantity);
        } else {
            log.error("验证商品库存失败: productId={}, quantity={}, result={}", productId, quantity, result);
        }

        return result;
    }
}

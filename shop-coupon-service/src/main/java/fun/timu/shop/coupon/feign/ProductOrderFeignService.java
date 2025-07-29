package fun.timu.shop.coupon.feign;

import fun.timu.shop.common.components.HttpRpcClient;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.util.RpcSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务RPC客户端
 * 用于调用订单相关的远程服务，使用Nacos服务发现
 *
 * @author zhengke
 */
@Slf4j
@Component
public class ProductOrderFeignService {

    private final HttpRpcClient httpRpcClient;
    
    // 使用服务名而不是硬编码URL，通过Nacos服务发现进行解析
    private static final String ORDER_SERVICE_NAME = "shop-order-service";

    public ProductOrderFeignService(HttpRpcClient httpRpcClient) {
        this.httpRpcClient = httpRpcClient;
    }

    /**
     * RPC调用 - 查询订单状态
     * 用于其他微服务查询订单状态
     *
     * @param outTradeNo 订单号
     * @return 订单状态信息
     */
    public JsonData queryProductOrderState(String outTradeNo) {
        // 使用服务名格式，启用负载均衡
        String url = "http://" + ORDER_SERVICE_NAME + "/api/order/v1/order/rpc/query-state";

        Map<String, Object> request = new HashMap<>();
        request.put("outTradeNo", outTradeNo);

        // 生成安全的RPC头部
        Map<String, String> headers = RpcSecurityUtil.generateSecurityHeaders("coupon-service", "POST", "/api/order/v1/order/rpc/query-state");
        headers.put("Content-Type", "application/json");

        log.info("发送安全RPC请求 - 查询订单状态: outTradeNo={}, targetService={}", outTradeNo, ORDER_SERVICE_NAME);

        JsonData result = httpRpcClient.postWithHeaders(url, request, headers);

        if (result != null && result.getCode() == 0) {
            log.info("查询订单状态成功: outTradeNo={}, state={}", outTradeNo, result.getData());
        } else {
            log.error("查询订单状态失败: outTradeNo={}, result={}", outTradeNo, result);
        }

        return result;
    }
}

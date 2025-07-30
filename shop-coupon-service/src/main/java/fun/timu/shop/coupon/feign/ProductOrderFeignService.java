package fun.timu.shop.coupon.feign;

import fun.timu.shop.common.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 订单服务Feign客户端
 * 使用Spring Cloud OpenFeign调用订单服务，通过Nacos进行服务发现
 *
 * @author zhengke
 */
@FeignClient(name = "shop-order-service",  // Nacos中注册的服务名
        path = "/api/order/v1/rpc"  // 服务的基础路径
)
public interface ProductOrderFeignService {

    /**
     * RPC调用 - 查询订单状态
     * 用于查询指定订单的当前状态
     * 注意：RPC安全头部由FeignRpcSecurityInterceptor自动添加
     *
     * @param requestBody 包含订单号的请求体
     * @return 订单状态信息
     */
    @PostMapping("/query-state")
    JsonData queryProductOrderState(@RequestBody Map<String, Object> requestBody);
}

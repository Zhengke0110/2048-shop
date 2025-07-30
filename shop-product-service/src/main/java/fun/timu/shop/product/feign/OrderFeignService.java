package fun.timu.shop.product.feign;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.QueryOrderStateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 订单服务Feign客户端
 * 用于调用订单服务的远程接口
 *
 * @author zhengke
 */
@FeignClient(name = "shop-order-service", path = "/api/order/v1")
public interface OrderFeignService {

    /**
     * 查询订单状态
     *
     * @param request 查询订单状态请求
     * @return 订单状态
     */
    @PostMapping("/rpc/query-state")
    JsonData queryProductOrderState(@RequestBody QueryOrderStateRequest request);
}

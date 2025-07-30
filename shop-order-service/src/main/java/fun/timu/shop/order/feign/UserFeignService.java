package fun.timu.shop.order.feign;

import fun.timu.shop.common.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端
 * 用于调用用户服务的远程接口
 *
 * @author zhengke
 */
@FeignClient(name = "shop-user-service", path = "/api/user/v1/rpc")
public interface UserFeignService {

    /**
     * RPC - 根据ID获取收货地址详情
     * 该接口用于订单服务查询用户收货地址的详细信息
     *
     * @param addressId 收货地址ID
     * @return 收货地址详情
     */
    @GetMapping("/address/detail/{addressId}")
    JsonData getAddressById(@PathVariable("addressId") Long addressId);
}

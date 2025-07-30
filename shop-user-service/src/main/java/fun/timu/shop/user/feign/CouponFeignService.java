package fun.timu.shop.user.feign;

import fun.timu.shop.common.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 优惠券服务Feign客户端
 * 用于调用优惠券服务的远程接口
 *
 * @author zhengke
 */
@FeignClient(name = "shop-coupon-service", path = "/api/coupon/v1/rpc")
public interface CouponFeignService {

    /**
     * RPC调用 - 新用户注册福利发放
     * 为新注册用户发放所有配置的福利优惠券
     *
     * @param request 请求参数，包含userId
     * @return 发放结果
     */
    @PostMapping("/new-user-benefits")
    JsonData grantNewUserBenefits(@RequestBody Map<String, Object> request);
}

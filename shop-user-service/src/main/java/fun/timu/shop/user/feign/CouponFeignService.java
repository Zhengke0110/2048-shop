package fun.timu.shop.user.feign;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.NewUserBenefitsRequest;
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
@FeignClient(name = "shop-coupon-service", path = "/api/coupon/v1")
public interface CouponFeignService {

    /**
     * RPC调用 - 新用户注册福利发放
     * 为新注册用户发放所有配置的福利优惠券
     *
     * @param request 新用户福利发放请求
     * @return 发放结果
     */
    @PostMapping("/rpc/new-user-benefits")
    JsonData grantNewUserBenefits(@RequestBody NewUserBenefitsRequest request);
}

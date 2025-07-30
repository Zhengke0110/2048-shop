package fun.timu.shop.coupon.config;

import fun.timu.shop.common.config.FeignRpcSecurityInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 优惠券服务Feign配置类
 * 继承通用配置，添加服务特定的配置
 *
 * @author zhengke
 */
@Configuration
public class CouponFeignConfig {
    
    /**
     * 为优惠券服务注册专用的Feign RPC安全拦截器
     * 明确指定服务名为 "shop-coupon-service"
     */
    @Bean
    public RequestInterceptor couponServiceFeignRpcSecurityInterceptor() {
        return new FeignRpcSecurityInterceptor("shop-coupon-service");
    }
}

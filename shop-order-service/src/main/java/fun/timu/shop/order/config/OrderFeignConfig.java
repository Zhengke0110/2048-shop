package fun.timu.shop.order.config;

import fun.timu.shop.common.config.FeignRpcSecurityInterceptor;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单服务Feign配置类
 * 继承通用配置，添加服务特定的配置
 *
 * @author zhengke
 */
@Configuration
@EnableFeignClients(basePackages = "fun.timu.shop.order.feign")
public class OrderFeignConfig {

    /**
     * 为订单服务注册专用的Feign RPC安全拦截器
     * 明确指定服务名为 "shop-order-service"
     */
    @Bean
    public RequestInterceptor orderServiceFeignRpcSecurityInterceptor() {
        return new FeignRpcSecurityInterceptor("shop-order-service");
    }
}

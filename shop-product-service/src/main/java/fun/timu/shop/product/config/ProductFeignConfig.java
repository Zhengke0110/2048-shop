package fun.timu.shop.product.config;

import fun.timu.shop.common.config.FeignRpcSecurityInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品服务Feign配置类
 * 继承通用配置，添加服务特定的配置
 *
 * @author zhengke
 */
@Configuration
public class ProductFeignConfig {
    
    /**
     * 为商品服务注册专用的Feign RPC安全拦截器
     * 明确指定服务名为 "shop-product-service"
     */
    @Bean
    public RequestInterceptor productServiceFeignRpcSecurityInterceptor() {
        return new FeignRpcSecurityInterceptor("shop-product-service");
    }
}

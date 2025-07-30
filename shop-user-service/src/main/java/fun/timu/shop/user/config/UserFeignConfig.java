package fun.timu.shop.user.config;

import fun.timu.shop.common.config.FeignRpcSecurityInterceptor;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户服务Feign配置类
 * 继承通用配置，添加服务特定的配置
 *
 * @author zhengke
 */
@Configuration
@EnableFeignClients(basePackages = "fun.timu.shop.user.feign")
public class UserFeignConfig {

    /**
     * 为用户服务注册专用的Feign RPC安全拦截器
     * 明确指定服务名为 "shop-user-service"
     */
    @Bean
    public RequestInterceptor userServiceFeignRpcSecurityInterceptor() {
        return new FeignRpcSecurityInterceptor("shop-user-service");
    }
}

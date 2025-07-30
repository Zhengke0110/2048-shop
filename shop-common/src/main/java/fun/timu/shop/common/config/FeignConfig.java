package fun.timu.shop.common.config;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Feign通用配置类
 * 提供Feign客户端的通用配置，供所有微服务使用
 *
 * @author zhengke
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
public class FeignConfig {
    
    /**
     * 为指定服务创建Feign RPC安全拦截器
     * 
     * @param serviceName 服务名
     * @return Feign RPC安全拦截器实例
     */
    public static RequestInterceptor createFeignRpcSecurityInterceptor(String serviceName) {
        return new FeignRpcSecurityInterceptor(serviceName);
    }
}

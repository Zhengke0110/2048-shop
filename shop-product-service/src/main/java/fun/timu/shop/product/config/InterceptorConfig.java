package fun.timu.shop.product.config;


import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.interceptor.RpcSecurityInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final RpcSecurityInterceptor rpcSecurityInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 添加RPC安全校验拦截器 - 只拦截RPC接口
        registry.addInterceptor(rpcSecurityInterceptor)
                .addPathPatterns("/api/product/v1/product/batch", "/api/product/v1/product/rpc/**")
                .order(1); // 优先级最高

        // 2. 添加登录拦截器 - 拦截其他需要认证的接口
        registry.addInterceptor(new LoginInterceptor())
                // 拦截的路径 - 包含所有需要认证的API
                .addPathPatterns("/api/**")

                // 排除不需要拦截的路径（精确匹配具体接口）
                .excludePathPatterns(
                        // 轮播图查询接口 - 普通用户可以访问
                        "/api/product/v1/banner/list",
                        "/api/product/v1/banner/list/**",
                        "/api/product/v1/banner/home",
                        "/api/product/v1/banner/*",
                        
                        // 商品查询接口 - 普通用户可以访问
                        "/api/product/v1/product/list",
                        "/api/product/v1/product/list/**",
                        "/api/product/v1/product/hot",
                        "/api/product/v1/product/recommend",
                        "/api/product/v1/product/search",
                        "/api/product/v1/product/price-range",
                        "/api/product/v1/product/*",
                        
                        // RPC接口 - 微服务间调用，已通过RPC安全拦截器验证
                        "/api/product/v1/product/batch",
                        "/api/product/v1/product/rpc/**"
                )
                .order(2); // 较低优先级

        log.info("拦截器注册成功 - RPC安全拦截器优先处理RPC接口，LoginInterceptor处理其他需要认证的接口");
    }
}


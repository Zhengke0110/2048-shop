package fun.timu.shop.product.config;


import fun.timu.shop.common.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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
                        "/api/product/v1/product/*"
                );

        log.info("LoginInterceptor 注册成功 - 拦截所有/api/**路径，排除轮播图、商品查询等公共接口");
    }
}


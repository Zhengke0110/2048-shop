package fun.timu.shop.coupon.config;


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
                        "/api/coupon/v1/coupon/pageCoupon",
                        "/api/coupon/v1/coupon/rpc/new-user-benefits"  // 排除新用户福利发放RPC接口
                );

        log.info("LoginInterceptor 注册成功 - 拦截所有/api/**路径，排除认证和公共接口");
    }
}


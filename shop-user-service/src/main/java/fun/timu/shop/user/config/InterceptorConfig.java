package fun.timu.shop.user.config;


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
                        // === AccountController 不需要认证的接口 ===
                        "/api/user/v1/account/register",   // 用户注册
                        "/api/user/v1/account/login",      // 用户登录
                        "/api/user/v1/account/refresh",    // 刷新Token
                        "/api/user/v1/account/upload",     // 文件上传（头像上传，注册时需要）

                        // === NotifyController 不需要认证的接口 ===
                        "/api/user/v1/notify/captcha",     // 获取图形验证码
                        "/api/user/v1/notify/sendCode"    // 发送短信验证码
                );

        log.info("LoginInterceptor 注册成功 - 拦截所有/api/**路径，排除认证和公共接口");
    }
}


package fun.timu.shop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * HTTP客户端配置类
 * 用于RPC远程调用
 *
 * @author zhengke
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置WebClient用于HTTP RPC调用
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
}

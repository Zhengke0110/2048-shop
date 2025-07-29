package fun.timu.shop.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * HTTP客户端配置类
 * 用于RPC远程调用，支持服务发现和负载均衡
 *
 * @author zhengke
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置WebClient用于HTTP RPC调用，支持负载均衡
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)); // 1MB
    }

    /**
     * 普通的WebClient实例
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}

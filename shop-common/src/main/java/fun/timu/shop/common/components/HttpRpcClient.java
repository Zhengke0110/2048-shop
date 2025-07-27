package fun.timu.shop.common.components;

import fun.timu.shop.common.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP RPC客户端组件
 * 用于服务间远程调用
 *
 * @author zhengke
 */
@Component("httpRpcClientComponent")
@Slf4j
public class HttpRpcClient {

    private final WebClient webClient;
    
    public HttpRpcClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 发送GET请求
     */
    public JsonData get(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonData.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("HTTP GET请求失败: url={}", url, e);
            return JsonData.buildError("RPC调用失败");
        }
    }

    /**
     * 发送GET请求（带超时时间）
     */
    public JsonData getWithTimeout(String url, Duration timeout) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonData.class)
                    .timeout(timeout)
                    .block();
        } catch (Exception e) {
            log.error("HTTP GET请求超时: url={}, timeout={}", url, timeout, e);
            return JsonData.buildError("RPC调用超时");
        }
    }

    /**
     * 发送POST请求
     */
    public JsonData post(String url, Object requestBody) {
        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestBody), Object.class)
                    .retrieve()
                    .bodyToMono(JsonData.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("HTTP POST请求失败: url={}", url, e);
            return JsonData.buildError("RPC调用失败");
        }
    }

    /**
     * 发送GET请求（带Header）
     */
    public JsonData getWithHeaders(String url, Map<String, String> headers) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            
            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }
            
            return request
                    .retrieve()
                    .bodyToMono(JsonData.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("HTTP GET请求失败: url={}", url, e);
            return JsonData.buildError("RPC调用失败");
        }
    }

    /**
     * 发送POST请求（带Header）
     */
    public JsonData postWithHeaders(String url, Object requestBody, Map<String, String> headers) {
        try {
            WebClient.RequestBodySpec request = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON);

            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }

            return request
                    .body(Mono.just(requestBody), Object.class)
                    .retrieve()
                    .bodyToMono(JsonData.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("HTTP POST请求失败: url={}", url, e);
            return JsonData.buildError("RPC调用失败");
        }
    }
}

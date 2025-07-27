package fun.timu.shop.common.interceptor;

import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JsonData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC接口安全校验拦截器
 * 用于验证微服务之间的RPC调用安全性
 *
 * @author zhengke
 */
@Slf4j
@Component
public class RpcSecurityInterceptor implements HandlerInterceptor {

    /**
     * RPC密钥配置 - 生产环境应从配置中心获取
     */
    private static final Map<String, String> RPC_SECRETS = new HashMap<>();

    /**
     * 时间戳有效期（毫秒）- 5分钟
     */
    private static final long TIMESTAMP_VALID_PERIOD = 5 * 60 * 1000;

    /**
     * 防重放攻击的请求缓存
     */
    private static final ConcurrentHashMap<String, Long> REQUEST_CACHE = new ConcurrentHashMap<>();

    static {
        // 初始化各个微服务的RPC密钥
        RPC_SECRETS.put("order-service", "order_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("user-service", "user_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("coupon-service", "coupon_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("gateway-service", "gateway_rpc_secret_2025_secure_key");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 验证RPC调用安全性
        if (!validateRpcSecurity(request)) {
            log.warn("RPC安全校验失败 - URI: {}, RemoteAddr: {}", request.getRequestURI(), request.getRemoteAddr());
            sendErrorResponse(response, "RPC安全校验失败");
            return false;
        }

        return true;
    }

    /**
     * 验证RPC调用的安全性
     */
    private boolean validateRpcSecurity(HttpServletRequest request) {
        try {
            // 1. 获取必要的头部信息
            String rpcSource = request.getHeader("RPC-Source");
            String timestamp = request.getHeader("RPC-Timestamp");
            String nonce = request.getHeader("RPC-Nonce");
            String signature = request.getHeader("RPC-Signature");

            if (StringUtils.isAnyBlank(rpcSource, timestamp, nonce, signature)) {
                log.warn("RPC请求缺少必要的安全头部信息");
                return false;
            }

            // 2. 验证调用方是否为合法的微服务
            if (!RPC_SECRETS.containsKey(rpcSource)) {
                log.warn("未知的RPC调用方: {}", rpcSource);
                return false;
            }

            // 3. 验证时间戳是否在有效期内
            long requestTimestamp;
            try {
                requestTimestamp = Long.parseLong(timestamp);
            } catch (NumberFormatException e) {
                log.warn("无效的时间戳格式: {}", timestamp);
                return false;
            }

            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - requestTimestamp) > TIMESTAMP_VALID_PERIOD) {
                log.warn("请求时间戳超出有效期: requestTime={}, currentTime={}, diff={}ms",
                        requestTimestamp, currentTime, Math.abs(currentTime - requestTimestamp));
                return false;
            }

            // 4. 防重放攻击 - 检查nonce是否已使用
            String requestKey = rpcSource + "_" + nonce + "_" + timestamp;
            if (REQUEST_CACHE.containsKey(requestKey)) {
                log.warn("检测到重放攻击 - requestKey: {}", requestKey);
                return false;
            }

            // 5. 验证签名
            if (!validateSignature(request, rpcSource, timestamp, nonce, signature)) {
                log.warn("RPC签名验证失败");
                return false;
            }

            // 6. 缓存请求以防重放（带过期时间）
            REQUEST_CACHE.put(requestKey, currentTime);
            cleanExpiredCache();

            log.info("RPC安全校验通过 - Source: {}, URI: {}", rpcSource, request.getRequestURI());
            return true;

        } catch (Exception e) {
            log.error("RPC安全校验异常", e);
            return false;
        }
    }

    /**
     * 验证请求签名
     */
    private boolean validateSignature(HttpServletRequest request, String rpcSource,
                                      String timestamp, String nonce, String signature) {
        try {
            String secret = RPC_SECRETS.get(rpcSource);
            String method = request.getMethod();
            String uri = request.getRequestURI();

            // 构建签名原文：method + uri + rpcSource + timestamp + nonce + secret
            String signData = method + uri + rpcSource + timestamp + nonce + secret;

            // 使用MD5生成签名（生产环境建议使用更安全的算法如HMAC-SHA256）
            String expectedSignature = DigestUtils.md5DigestAsHex(signData.getBytes(StandardCharsets.UTF_8));

            boolean isValid = expectedSignature.equalsIgnoreCase(signature);
            if (!isValid) {
                log.debug("签名验证失败 - Expected: {}, Actual: {}, SignData: {}",
                        expectedSignature, signature, signData);
            }

            return isValid;
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 清理过期的请求缓存
     */
    private void cleanExpiredCache() {
        if (REQUEST_CACHE.size() > 1000) { // 当缓存过多时进行清理
            long currentTime = System.currentTimeMillis();
            REQUEST_CACHE.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > TIMESTAMP_VALID_PERIOD);
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        JsonData errorResult = JsonData.buildError(message);
        CommonUtil.sendJsonMessage(response, errorResult);
    }

    /**
     * 获取RPC密钥（供测试使用）
     */
    public static String getRpcSecret(String serviceName) {
        return RPC_SECRETS.get(serviceName);
    }
}

package fun.timu.shop.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RPC安全工具类
 * 用于生成RPC调用的安全头部信息
 *
 * @author zhengke
 */
@Slf4j
public class RpcSecurityUtil {

    /**
     * RPC密钥配置 - 应与RpcSecurityInterceptor保持一致
     */
    private static final Map<String, String> RPC_SECRETS = new HashMap<>();
    
    static {
        // 初始化各个微服务的RPC密钥
        RPC_SECRETS.put("shop-order-service", "order_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("shop-user-service", "user_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("shop-coupon-service", "coupon_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("shop-product-service", "product_rpc_secret_2025_secure_key");
        RPC_SECRETS.put("shop-gateway", "gateway_rpc_secret_2025_secure_key");
    }

    /**
     * 生成RPC安全头部
     *
     * @param serviceName 调用方服务名称
     * @param method HTTP方法
     * @param uri 请求URI
     * @return 包含安全头部的Map
     */
    public static Map<String, String> generateSecurityHeaders(String serviceName, String method, String uri) {
        Map<String, String> headers = new HashMap<>();
        
        try {
            // 1. 基本头部信息
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = generateNonce();
            
            headers.put("RPC-Source", serviceName);
            headers.put("RPC-Timestamp", timestamp);
            headers.put("RPC-Nonce", nonce);
            
            // 2. 生成签名
            String signature = generateSignature(serviceName, method, uri, timestamp, nonce);
            headers.put("RPC-Signature", signature);
            
            log.debug("生成RPC安全头部成功 - Service: {}, Method: {}, URI: {}", serviceName, method, uri);
            
        } catch (Exception e) {
            log.error("生成RPC安全头部失败", e);
            throw new RuntimeException("生成RPC安全头部失败", e);
        }
        
        return headers;
    }

    /**
     * 生成随机nonce值
     */
    private static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成签名
     */
    private static String generateSignature(String serviceName, String method, String uri, 
                                          String timestamp, String nonce) {
        String secret = RPC_SECRETS.get(serviceName);
        if (secret == null) {
            throw new IllegalArgumentException("未知的服务名称: " + serviceName);
        }
        
        // 构建签名原文：method + uri + serviceName + timestamp + nonce + secret
        String signData = method + uri + serviceName + timestamp + nonce + secret;
        
        // 使用MD5生成签名（生产环境建议使用更安全的算法如HMAC-SHA256）
        String signature = DigestUtils.md5DigestAsHex(signData.getBytes(StandardCharsets.UTF_8));
        
        log.info("RPC签名生成成功 - ServiceName: {}, Method: {}, URI: {}, Signature: {}", 
                serviceName, method, uri, signature);
        log.debug("签名详细信息 - Timestamp: {}, Nonce: {}, SignData: {}, SecretLength: {}", 
                timestamp, nonce, signData, secret.length());
        
        return signature;
    }

    /**
     * 验证服务名称是否有效
     */
    public static boolean isValidServiceName(String serviceName) {
        return RPC_SECRETS.containsKey(serviceName);
    }

    /**
     * 获取所有支持的服务名称
     */
    public static String[] getSupportedServices() {
        return RPC_SECRETS.keySet().toArray(new String[0]);
    }
}

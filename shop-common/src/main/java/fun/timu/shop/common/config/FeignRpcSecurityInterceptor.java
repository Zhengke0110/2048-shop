package fun.timu.shop.common.config;

import fun.timu.shop.common.util.RpcSecurityUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Feign RPC安全拦截器
 * 自动为Feign请求添加RPC安全头部信息
 * 通用组件，供所有微服务使用
 *
 * @author zhengke
 */
@Slf4j
public class FeignRpcSecurityInterceptor implements RequestInterceptor {

    /**
     * 当前服务名，从环境变量或配置中获取
     * 各个微服务需要配置 RPC_SERVICE_NAME 环境变量或在配置文件中指定
     */
    private final String currentServiceName;

    public FeignRpcSecurityInterceptor() {
        // 优先从环境变量获取服务名
        this.currentServiceName = System.getProperty("rpc.service.name",
                System.getenv("RPC_SERVICE_NAME"));

        if (currentServiceName == null || currentServiceName.trim().isEmpty()) {
            log.warn("未配置RPC服务名，请设置 rpc.service.name 系统属性或 RPC_SERVICE_NAME 环境变量");
        } else {
            log.info("Feign RPC安全拦截器初始化完成，当前服务: {}", currentServiceName);
        }
    }

    /**
     * 构造函数，允许指定服务名
     */
    public FeignRpcSecurityInterceptor(String serviceName) {
        this.currentServiceName = serviceName;
        log.info("Feign RPC安全拦截器初始化完成，当前服务: {}", currentServiceName);
    }

    @Override
    public void apply(RequestTemplate template) {
        // 检查是否为RPC接口：包含/rpc/或特殊的batch路径
        String url = template.url();
        boolean isRpcCall = url.contains("/rpc/") || url.endsWith("/batch");
        
        if (isRpcCall) {
            try {
                // 检查服务名是否已配置
                if (currentServiceName == null || currentServiceName.trim().isEmpty()) {
                    log.error("RPC服务名未配置，无法生成安全头部");
                    return;
                }

                // 提取HTTP方法和路径
                String method = template.method();
                String relativePath = template.url();

                // 直接使用相对路径进行签名
                // 服务端验证时会自动标准化URI路径，确保签名一致性
                log.info("Feign RPC调用开始 - Service: {}, Method: {}, Path: {}",
                        currentServiceName, method, relativePath);

                // 生成RPC安全头部，使用相对路径
                Map<String, String> securityHeaders = RpcSecurityUtil.generateSecurityHeaders(
                        currentServiceName,
                        method,
                        relativePath
                );

                // 添加安全头部到请求
                for (Map.Entry<String, String> entry : securityHeaders.entrySet()) {
                    template.header(entry.getKey(), entry.getValue());
                }

                log.info("Feign RPC安全头部已添加 - Service: {}, Path: {}, Signature: {}",
                        currentServiceName, relativePath, securityHeaders.get("RPC-Signature"));

            } catch (Exception e) {
                log.error("生成Feign RPC安全头部失败", e);
                throw e; // 抛出异常以便更好地跟踪问题
            }
        }
    }
}

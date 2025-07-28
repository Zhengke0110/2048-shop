package fun.timu.shop.common.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 配置类
 * 统一管理服务注册与发现配置
 * 
 * @author zhengke
 */
@Configuration
@EnableDiscoveryClient
public class NacosConfig {
    
    // 这里可以添加 Nacos 相关的自定义配置
    // 比如服务元数据、健康检查等
    
}

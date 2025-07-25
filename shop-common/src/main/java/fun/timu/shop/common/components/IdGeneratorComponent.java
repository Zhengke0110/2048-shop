package fun.timu.shop.common.components;

import fun.timu.shop.common.util.SnowflakeIdGenerator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * 分布式ID生成器组件
 * 
 * @author zhengke
 */
@Slf4j
@Component
public class IdGeneratorComponent {
    
    /**
     * 机器ID，可通过配置文件配置
     */
    @Value("${app.snowflake.machine-id:#{null}}")
    private Long machineId;
    
    /**
     * Snowflake ID生成器实例
     */
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    /**
     * 初始化ID生成器
     */
    @PostConstruct
    public void init() {
        if (machineId != null) {
            snowflakeIdGenerator = SnowflakeIdGenerator.getInstance(machineId);
            log.info("使用配置的机器ID初始化ID生成器: {}", machineId);
        } else {
            snowflakeIdGenerator = SnowflakeIdGenerator.getInstance();
            log.info("使用默认机器ID初始化ID生成器");
        }
    }
    
    /**
     * 生成分布式ID
     * 
     * @return 分布式ID
     */
    public Long generateId() {
        return snowflakeIdGenerator.nextId();
    }
    
    /**
     * 生成分布式ID（String类型）
     * 
     * @return 分布式ID字符串
     */
    public String generateIdString() {
        return String.valueOf(snowflakeIdGenerator.nextId());
    }
    
    /**
     * 解析ID信息（用于调试）
     * 
     * @param id 分布式ID
     * @return ID解析信息
     */
    public SnowflakeIdGenerator.IdInfo parseId(long id) {
        return SnowflakeIdGenerator.parseId(id);
    }
}

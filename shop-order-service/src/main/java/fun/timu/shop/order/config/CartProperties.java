package fun.timu.shop.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 购物车配置属性
 * 
 * @author zhengke
 */
@Data
@Component
@ConfigurationProperties(prefix = "cart")
public class CartProperties {

    private Redis redis = new Redis();
    private Startup startup = new Startup();

    @Data
    public static class Redis {
        /**
         * Redis Key前缀
         */
        private String keyPrefix = "cart:user:";
        
        /**
         * 过期时间（天）
         */
        private Integer expireDays = 30;
    }

    @Data
    public static class Startup {
        /**
         * 启动时是否从数据库加载数据到Redis
         */
        private Boolean loadFromDb = true;
        
        /**
         * 加载策略：all(全量) / hot(热点数据)
         */
        private String loadStrategy = "hot";
        
        /**
         * 热点用户定义：最近N天有活动
         */
        private Integer hotUserDays = 7;
        
        /**
         * 批量处理大小
         */
        private Integer batchSize = 1000;
        
        /**
         * 并行加载线程数
         */
        private Integer parallelThreads = 4;
    }
}

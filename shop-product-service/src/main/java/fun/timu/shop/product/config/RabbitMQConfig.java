package fun.timu.shop.product.config;

import fun.timu.shop.common.config.BaseRabbitMQConfig;
import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RabbitMQConfig extends BaseRabbitMQConfig {

    /**
     * 交换机
     */
    @Value("${mqconfig.stock_event_exchange}")
    private String eventExchange;

    /**
     * 库存释放延迟队列
     */
    @Value("${mqconfig.stock_release_delay_queue}")
    private String stockReleaseDelayQueue;

    /**
     * 库存释放延迟队列路由key
     */
    @Value("${mqconfig.stock_release_delay_routing_key}")
    private String stockReleaseDelayRoutingKey;

    /**
     * 库存释放队列
     */
    @Value("${mqconfig.stock_release_queue}")
    private String stockReleaseQueue;

    /**
     * 库存释放路由key
     */
    @Value("${mqconfig.stock_release_routing_key}")
    private String stockReleaseRoutingKey;

    /**
     * 消息过期时间
     */
    @Value("${mqconfig.ttl}")
    private Integer ttl;

    /**
     * 消息转换器
     *
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return createMessageConverter();
    }

    /**
     * 创建交换机 Topic类型
     * 库存服务专用交换机
     *
     * @return Exchange
     */
    @Bean
    public Exchange stockEventExchange() {
        return createTopicExchange(eventExchange, true, false);
    }

    /**
     * 延迟队列 - 用于库存释放延迟处理
     *
     * @return Queue
     */
    @Bean
    public Queue stockReleaseDelayQueue() {
        return createDelayQueue(
                stockReleaseDelayQueue,
                ttl,
                eventExchange,
                stockReleaseRoutingKey,
                true,
                false,
                false
        );
    }

    /**
     * 死信队列，普通队列，用于被监听
     *
     * @return Queue
     */
    @Bean
    public Queue stockReleaseQueue() {
        return createQueue(stockReleaseQueue, true, false, false);
    }

    /**
     * 延迟队列的绑定关系建立
     *
     * @return Binding
     */
    @Bean
    public Binding stockReleaseDelayBinding() {
        return createBinding(stockReleaseDelayQueue, eventExchange, stockReleaseDelayRoutingKey);
    }

    /**
     * 死信队列绑定关系建立
     *
     * @return Binding
     */
    @Bean
    public Binding stockReleaseBinding() {
        return createBinding(stockReleaseQueue, eventExchange, stockReleaseRoutingKey);
    }
}

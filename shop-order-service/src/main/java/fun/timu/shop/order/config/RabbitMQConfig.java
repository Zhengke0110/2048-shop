package fun.timu.shop.order.config;

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
    @Value("${mqconfig.order_event_exchange}")
    private String eventExchange;

    /**
     * 订单关闭延迟队列
     */
    @Value("${mqconfig.order_close_delay_queue}")
    private String orderCloseDelayQueue;

    /**
     * 订单关闭延迟路由key
     */
    @Value("${mqconfig.order_close_delay_routing_key}")
    private String orderCloseDelayRoutingKey;

    /**
     * 订单关闭队列
     */
    @Value("${mqconfig.order_close_queue}")
    private String orderCloseQueue;

    /**
     * 订单关闭路由key
     */
    @Value("${mqconfig.order_close_routing_key}")
    private String orderCloseRoutingKey;

    /**
     * 第一个队列  延迟队列，
     */
    @Value("${mqconfig.stock_release_delay_queue}")
    private String stockReleaseDelayQueue;

    /**
     * 第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.stock_release_delay_routing_key}")
    private String stockReleaseDelayRoutingKey;

    /**
     * 第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.stock_release_queue}")
    private String stockReleaseQueue;

    /**
     * 第二个队列的路由key
     * <p>
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.stock_release_routing_key}")
    private String stockReleaseRoutingKey;

    /**
     * 过期时间
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
     * 订单服务专用交换机
     *
     * @return Exchange
     */
    @Bean
    public Exchange orderEventExchange() {
        return createTopicExchange(eventExchange, true, false);
    }

    /**
     * 订单关闭延迟队列
     *
     * @return Queue
     */
    @Bean
    public Queue orderCloseDelayQueue() {
        return createDelayQueue(
                orderCloseDelayQueue,
                ttl,
                eventExchange,
                orderCloseRoutingKey,
                true,
                false,
                false
        );
    }

    /**
     * 订单关闭队列
     *
     * @return Queue
     */
    @Bean
    public Queue orderCloseQueue() {
        return createQueue(orderCloseQueue, true, false, false);
    }

    /**
     * 订单关闭延迟队列绑定
     *
     * @return Binding
     */
    @Bean
    public Binding orderCloseDelayBinding() {
        return createBinding(orderCloseDelayQueue, eventExchange, orderCloseDelayRoutingKey);
    }

    /**
     * 订单关闭队列绑定
     *
     * @return Binding
     */
    @Bean
    public Binding orderCloseBinding() {
        return createBinding(orderCloseQueue, eventExchange, orderCloseRoutingKey);
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

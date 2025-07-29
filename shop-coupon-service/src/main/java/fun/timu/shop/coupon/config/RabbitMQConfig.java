package fun.timu.shop.coupon.config;

import fun.timu.shop.common.config.BaseRabbitMQConfig;
import lombok.Data;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 优惠券服务 RabbitMQ 配置类
 * 继承基础配置，专门处理优惠券相关的队列配置
 */
@Configuration
@Data
public class RabbitMQConfig extends BaseRabbitMQConfig {

    /**
     * 交换机
     */
    @Value("${mqconfig.coupon_event_exchange}")
    private String eventExchange;


    /**
     * 第一个队列  延迟队列，
     */
    @Value("${mqconfig.coupon_release_delay_queue}")
    private String couponReleaseDelayQueue;

    /**
     * 第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.coupon_release_delay_routing_key}")
    private String couponReleaseDelayRoutingKey;


    /**
     * 第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.coupon_release_queue}")
    private String couponReleaseQueue;

    /**
     * 第二个队列的路由key
     * <p>
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.coupon_release_routing_key}")
    private String couponReleaseRoutingKey;

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
     * 优惠券服务专用交换机
     *
     * @return Exchange
     */
    @Bean
    public Exchange couponEventExchange() {
        return createTopicExchange(eventExchange, true, false);
    }

    /**
     * 延迟队列 - 用于优惠券释放延迟处理
     *
     * @return Queue
     */
    @Bean
    public Queue couponReleaseDelayQueue() {
        return createDelayQueue(
                couponReleaseDelayQueue,
                ttl,
                eventExchange,
                couponReleaseRoutingKey,
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
    public Queue couponReleaseQueue() {
        return createQueue(couponReleaseQueue, true, false, false);
    }

    /**
     * 延迟队列的绑定关系建立
     *
     * @return Binding
     */
    @Bean
    public Binding couponReleaseDelayBinding() {
        return createBinding(couponReleaseDelayQueue, eventExchange, couponReleaseDelayRoutingKey);
    }

    /**
     * 死信队列绑定关系建立
     *
     * @return Binding
     */
    @Bean
    public Binding couponReleaseBinding() {
        return createBinding(couponReleaseQueue, eventExchange, couponReleaseRoutingKey);
    }
}

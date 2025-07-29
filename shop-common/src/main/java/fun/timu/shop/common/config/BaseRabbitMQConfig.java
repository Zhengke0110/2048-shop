package fun.timu.shop.common.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ基础配置类
 * 提供通用的消息转换器和工具方法
 * 各个微服务可以继承此类并扩展自己的队列配置
 */
@Configuration
public class BaseRabbitMQConfig {

    /**
     * 获取消息转换器 - 统一使用JSON格式
     * 子类可以调用此方法来创建 MessageConverter Bean
     * @return Jackson2JsonMessageConverter
     */
    protected MessageConverter createMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 创建Topic类型交换机的通用方法
     * @param exchangeName 交换机名称
     * @param durable 是否持久化
     * @param autoDelete 是否自动删除
     * @return TopicExchange
     */
    protected TopicExchange createTopicExchange(String exchangeName, boolean durable, boolean autoDelete) {
        return new TopicExchange(exchangeName, durable, autoDelete);
    }

    /**
     * 创建普通队列的通用方法
     * @param queueName 队列名称
     * @param durable 是否持久化
     * @param exclusive 是否独占
     * @param autoDelete 是否自动删除
     * @return Queue
     */
    protected Queue createQueue(String queueName, boolean durable, boolean exclusive, boolean autoDelete) {
        return new Queue(queueName, durable, exclusive, autoDelete);
    }

    /**
     * 创建延迟队列的通用方法（基于死信队列实现）
     * @param queueName 队列名称
     * @param ttl 过期时间（毫秒）
     * @param deadLetterExchange 死信交换机
     * @param deadLetterRoutingKey 死信路由键
     * @param durable 是否持久化
     * @param exclusive 是否独占
     * @param autoDelete 是否自动删除
     * @return Queue
     */
    protected Queue createDelayQueue(String queueName, Integer ttl, String deadLetterExchange, 
                                   String deadLetterRoutingKey, boolean durable, boolean exclusive, boolean autoDelete) {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-message-ttl", ttl);
        args.put("x-dead-letter-exchange", deadLetterExchange);
        args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        
        return new Queue(queueName, durable, exclusive, autoDelete, args);
    }

    /**
     * 创建绑定关系的通用方法
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param routingKey 路由键
     * @return Binding
     */
    protected Binding createBinding(String queueName, String exchangeName, String routingKey) {
        return new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);
    }
}

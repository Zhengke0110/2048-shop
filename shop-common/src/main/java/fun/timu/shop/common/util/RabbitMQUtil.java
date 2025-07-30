package fun.timu.shop.common.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ 消息发送工具类
 * 封装消息发送的通用方法
 */
@Slf4j
@Component
@AllArgsConstructor
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换机和路由键
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            log.info("消息发送成功：exchange={}, routingKey={}, message={}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("消息发送失败：exchange={}, routingKey={}, message={}", exchange, routingKey, message, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送延迟消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayTime  延迟时间（毫秒）
     */
    public void sendDelayMessage(String exchange, String routingKey, Object message, Long delayTime) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                // 设置消息的延迟时间
                msg.getMessageProperties().setExpiration(String.valueOf(delayTime));
                return msg;
            }, correlationData);
            log.info("延迟消息发送成功：exchange={}, routingKey={}, message={}, delayTime={}ms",
                    exchange, routingKey, message, delayTime);
        } catch (Exception e) {
            log.error("延迟消息发送失败：exchange={}, routingKey={}, message={}, delayTime={}ms",
                    exchange, routingKey, message, delayTime, e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    /**
     * 发送消息带自定义属性
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param properties 消息属性
     */
    public void sendMessageWithProperties(String exchange, String routingKey, Object message, MessageProperties properties) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            Message msg = new Message(rabbitTemplate.getMessageConverter().toMessage(message, properties).getBody(), properties);
            rabbitTemplate.send(exchange, routingKey, msg, correlationData);
            log.info("带属性消息发送成功：exchange={}, routingKey={}, message={}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("带属性消息发送失败：exchange={}, routingKey={}, message={}", exchange, routingKey, message, e);
            throw new RuntimeException("带属性消息发送失败", e);
        }
    }
}

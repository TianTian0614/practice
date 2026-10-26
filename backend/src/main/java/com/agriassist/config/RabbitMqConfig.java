package com.agriassist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑与客户端配置（仅在 {@code app.mq.enabled=true} 时生效）。
 *
 * <p>本地无 broker 时此配置整体不加载，应用零依赖启动；docker-compose 起好 broker 后
 * 把开关置 true 即可无缝切换到真实消息队列。生产拓扑：
 * <pre>
 *   producer --(agri.direct / routing=notify)--> Queue(agri.notify) --[consumer]--> NotificationService
 * </pre>
 */
@Configuration
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String EXCHANGE = "agri.direct";
    public static final String QUEUE = "agri.notify";
    public static final String ROUTING_KEY = "notify";

    @Bean
    public DirectExchange agriExchange() {
        // durable=true：broker 重启后交换机不丢失
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        // durable=true：队列持久化；保证消息在 broker 重启后仍可消费
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding notificationBinding(DirectExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /** JSON 序列化，保证 NotificationEvent 跨进程可读（默认是 Java 原生序列化，不可跨语言） */
    @Bean
    public MessageConverter notificationMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 自定义 RabbitTemplate（同名覆盖 Spring Boot 自动配置的 bean）：装上 JSON 转换器。
     * 仅在 mq.enabled=true 时加载；本地无 broker 时使用自动配置的默认模板，不影响降级逻辑。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        // 发送失败（broker 不可达）时由 publisher 捕获并降级，不抛到主流程
        template.setMandatory(false);
        return template;
    }

    /**
     * 消费端监听容器工厂：显式 autoStartup=true（覆盖 yml 中无 broker 时的 false），
     * 并装配 JSON 转换器，使 {@link com.agriassist.mq.NotificationConsumer} 能反序列化事件。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory agriListenerFactory(
            ConnectionFactory connectionFactory, MessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAutoStartup(true);
        factory.setConcurrentConsumers(2);
        return factory;
    }
}

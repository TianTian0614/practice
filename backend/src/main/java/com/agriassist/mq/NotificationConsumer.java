package com.agriassist.mq;

import com.agriassist.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 通知事件消费者（仅在 {@code app.mq.enabled=true} 且 broker 可达时生效）。
 *
 * <p>监听 {@code agri.notify} 队列，收到 {@link NotificationEvent} 后落库为系统通知。
 * 通过 {@link ConditionalOnProperty} 控制：本地无 broker 时此 Bean 不创建，
 * 由 {@link AsyncNotificationHandler} 承担落库，二者不会重复执行。
 */
@Component
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "agri.notify", containerFactory = "agriListenerFactory")
    public void onMessage(NotificationEvent event) {
        log.info("[MQ-CONSUMER] 收到通知事件: userId={}, title={}", event.getUserId(), event.getTitle());
        notificationService.create(event.getUserId(), event.getType(),
                event.getTitle(), event.getContent(), event.getLink());
    }
}

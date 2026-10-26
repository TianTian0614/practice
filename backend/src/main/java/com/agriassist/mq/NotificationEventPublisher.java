package com.agriassist.mq;

import com.agriassist.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 通知事件发布者（生产者）。
 *
 * <p>投递策略（对应岗位3「异步解耦」能力）：
 * <ol>
 *   <li>当 {@code app.mq.enabled=true} 且 broker 可达时，通过 RabbitMQ 异步投递到队列，
 *       由 {@link NotificationConsumer} 消费并落库，真正解耦主流程；</li>
 *   <li>当 MQ 未启用或发送失败（broker 不可达/超时）时，降级到
 *       {@link AsyncNotificationHandler} 使用本地线程池异步落库，保证功能不中断。</li>
 * </ol>
 */
@Service
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    public static final String EXCHANGE = "agri.direct";
    public static final String ROUTING_KEY = "notify";

    @Value("${app.mq.enabled:false}")
    private boolean mqEnabled;

    private final RabbitTemplate rabbitTemplate;
    private final AsyncNotificationHandler asyncHandler;
    private final NotificationService notificationService;

    @Autowired
    public NotificationEventPublisher(RabbitTemplate rabbitTemplate,
                                      AsyncNotificationHandler asyncHandler,
                                      NotificationService notificationService) {
        this.rabbitTemplate = rabbitTemplate;
        this.asyncHandler = asyncHandler;
        this.notificationService = notificationService;
    }

    /**
     * 发布一条通知事件。主流程（如审核通过）调用此方法来替代同步落库，
     * 从而将「写通知」这一非核心链路异步化。
     */
    public void publish(NotificationEvent event) {
        if (mqEnabled) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
                log.info("[MQ] 通知事件已投递到队列: userId={}, title={}", event.getUserId(), event.getTitle());
                return;
            } catch (Exception e) {
                // broker 不可达 / 超时 / 序列化异常：降级到本地线程池，避免通知丢失
                log.warn("[MQ] 投递失败，降级到 @Async 本地处理: {} | {}", event.getTitle(), e.getMessage());
            }
        }
        // 本地降级路径：在独立线程中直接落库，不阻塞主流程
        asyncHandler.handle(event);
    }
}

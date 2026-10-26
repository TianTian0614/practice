package com.agriassist.mq;

import com.agriassist.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * MQ 不可用时的降级处理：在独立线程池中异步执行通知落库。
 *
 * <p>使用 {@code @Async("mqFallbackExecutor")} 指定专用线程池（见 {@code AsyncConfig}），
 * 避免占用 Web 请求线程，同时与「真·MQ 异步」在语义上保持一致。
 */
@Component
public class AsyncNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationHandler.class);

    private final NotificationService notificationService;

    public AsyncNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("mqFallbackExecutor")
    public void handle(NotificationEvent event) {
        try {
            notificationService.create(event.getUserId(), event.getType(),
                    event.getTitle(), event.getContent(), event.getLink());
            log.info("[ASYNC] 通知已本地落库: userId={}, title={}", event.getUserId(), event.getTitle());
        } catch (Exception e) {
            // 降级路径仍失败：记录日志，避免影响主流程事务
            log.error("[ASYNC] 通知落库失败: userId={}, title={}", event.getUserId(), event.getTitle(), e);
        }
    }
}

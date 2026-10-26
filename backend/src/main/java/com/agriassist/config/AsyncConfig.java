package com.agriassist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置。
 *
 * <p>提供名为 {@code mqFallbackExecutor} 的专用线程池，供 {@link com.agriassist.mq.AsyncNotificationHandler}
 * 在无 MQ 时异步落库通知使用，独立于 Web 请求线程，避免阻塞主流程。当 MQ 启用时该池仍作为
 * broker 不可达时的兜底，保证通知不丢。
 */
@Configuration
public class AsyncConfig {

    @Bean("mqFallbackExecutor")
    public Executor mqFallbackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(64);
        executor.setThreadNamePrefix("mq-fallback-");
        // 队列满时由调用线程直接执行，避免消息丢失（与降级语义一致）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}

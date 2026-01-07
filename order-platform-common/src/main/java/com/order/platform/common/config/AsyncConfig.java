package com.order.platform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置
 *
 * 功能说明：
 * - 配置操作日志异步执行的线程池
 * - 独立线程池，避免影响业务性能
 *
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志异步执行器
     *
     * 配置说明：
     * - 核心线程数：2
     * - 最大线程数：5
     * - 队列容量：100
     * - 拒绝策略：CallerRunsPolicy（调用者线程执行）
     */
    @Bean("operationLogExecutor")
    public Executor operationLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(2);

        // 最大线程数
        executor.setMaxPoolSize(5);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("op-log-");

        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}

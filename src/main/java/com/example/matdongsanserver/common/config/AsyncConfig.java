package com.example.matdongsanserver.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // CPU 코어 수 * 2
        executor.setMaxPoolSize(20); // CPU 코어 수 * 5
        executor.setQueueCapacity(50); // 예상 동시 요청 수
        executor.setKeepAliveSeconds(30); // 비활성 스레드 유지 시간
        executor.setThreadNamePrefix("async-executor-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            throw new IllegalArgumentException("더 이상 요청을 처리할 수 없습니다.");
        });
        executor.initialize();
        return executor;
    }
}

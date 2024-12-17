package cherish.backend.common.config;

import cherish.backend.common.constant.CommonConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "alarmExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(60);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix(CommonConstants.ALARM_TASK_EXECUTOR_NAME);
        executor.initialize();
        return executor;
    }

}

package net.cserny.backsantaspring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@EnableAsync
@Configuration
@EnableJpaRepositories
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Bean(name = "eventVirtualThreadExecutor")
    public AsyncTaskExecutor eventVirtualThreadExecutor() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("fast_track-vt-", 0)
                .factory();
        ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);
        return new TaskExecutorAdapter(executor);
    }
}

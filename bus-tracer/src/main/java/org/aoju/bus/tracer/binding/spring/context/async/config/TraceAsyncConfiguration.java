package org.aoju.bus.tracer.binding.spring.context.async.config;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.binding.spring.context.async.PostTpicAsyncBeanPostProcessor;
import org.aoju.bus.tracer.binding.spring.context.async.PreTpicAsyncBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.task.AsyncTaskExecutor;


@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class TraceAsyncConfiguration {

    @Bean
    public PreTpicAsyncBeanPostProcessor preTpicAsyncBeanPostProcessor(AsyncTaskExecutor executor, Backend backend) {
        return new PreTpicAsyncBeanPostProcessor(executor, backend);
    }

    @Bean
    public PostTpicAsyncBeanPostProcessor postTpicAsyncBeanPostProcessor(AsyncTaskExecutor executor, Backend backend) {
        return new PostTpicAsyncBeanPostProcessor(executor, backend);
    }

}

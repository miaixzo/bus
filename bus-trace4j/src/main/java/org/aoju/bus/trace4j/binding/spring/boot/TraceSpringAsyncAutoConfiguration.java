package org.aoju.bus.trace4j.binding.spring.boot;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.binding.spring.context.async.config.TraceAsyncConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;

@Configuration
@ConditionalOnClass(Trace.class)
@ConditionalOnBean(AsyncTaskExecutor.class)
@AutoConfigureBefore(TraceContextAutoConfiguration.class)
@Import(TraceAsyncConfiguration.class)
public class TraceSpringAsyncAutoConfiguration {

}

package org.aoju.bus.tracer.binding.spring.web.config;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.binding.spring.web.TraceInterceptor;
import org.aoju.bus.tracer.binding.spring.web.TraceResponseBodyAdvice;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @since 2.0
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class TraceSpringMvcConfiguration {

    public static final String Trace_WEBMVCCONFIGURERADAPTER_INTERNAL = "org.aoju.bus.tracer.WebMvcConfigurerAdapter_internal";

    @Bean
    TraceInterceptor TraceInterceptor(Backend backend) {
        return new TraceInterceptor(backend);
    }

    @Bean(name = Trace_WEBMVCCONFIGURERADAPTER_INTERNAL)
    WebMvcConfigurerAdapter traceSpringMvcWebMvcConfigurerAdapter(final TraceInterceptor TraceInterceptor) {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(TraceInterceptor);
            }
        };
    }

    @Bean
    TraceResponseBodyAdvice TraceSpringMvcResponseBodyAdvice() {
        return new TraceResponseBodyAdvice();
    }

}

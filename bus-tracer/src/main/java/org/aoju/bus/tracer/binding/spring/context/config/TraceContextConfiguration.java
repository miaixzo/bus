package org.aoju.bus.tracer.binding.spring.context.config;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Role(BeanDefinition.ROLE_SUPPORT)
@Configuration
public class TraceContextConfiguration {

    @Bean
    Backend TraceBackend() {
        return Builder.getBackend();
    }

}

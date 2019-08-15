package org.aoju.bus.trace4j.binding.spring.context.config;

import org.aoju.bus.trace4j.Builder;
import org.aoju.bus.trace4j.Backend;
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

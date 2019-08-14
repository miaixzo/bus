package org.aoju.bus.trace4j.binding.spring.boot;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.binding.spring.context.config.TraceContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@ConditionalOnClass(Trace.class)
@EnableConfigurationProperties(TraceProperties.class)
public class TraceContextAutoConfiguration {

    @Autowired
    TraceProperties TraceProperties;

    @Configuration
    @ConditionalOnMissingBean(TraceBackend.class)
    public static class TraceBackendAutoConfiguration extends TraceContextConfiguration {
    }

    @Configuration
    @ConditionalOnMissingBean(TraceFilterConfiguration.class)
    public static class TracePropertiesAutoConfiguration {

        @Role(BeanDefinition.ROLE_SUPPORT)
        @Bean
        public TraceFilterConfiguration filterConfiguration(TraceProperties TraceProperties) {
            return TraceProperties.getAsFilterConfiguration();
        }

    }

}

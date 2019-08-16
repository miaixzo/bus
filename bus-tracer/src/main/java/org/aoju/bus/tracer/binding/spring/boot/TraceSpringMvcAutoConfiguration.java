package org.aoju.bus.tracer.binding.spring.boot;

import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.binding.spring.web.config.TraceSpringMvcConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(Builder.class)
@AutoConfigureBefore(TraceContextAutoConfiguration.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@Import(TraceSpringMvcConfiguration.class)
public class TraceSpringMvcAutoConfiguration {

}

package org.aoju.bus.tracer.binding.spring.boot;

import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.binding.spring.http.TraceSpringWebConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass({Builder.class, RestTemplate.class})
@ConditionalOnBean(RestTemplate.class)
@AutoConfigureBefore(TraceContextAutoConfiguration.class)
@Import(TraceSpringWebConfiguration.class)
public class TraceSpringWebAutoConfiguration {

}

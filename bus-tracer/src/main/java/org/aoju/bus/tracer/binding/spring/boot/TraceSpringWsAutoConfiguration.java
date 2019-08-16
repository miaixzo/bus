package org.aoju.bus.tracer.binding.spring.boot;

import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.binding.spring.soap.config.TraceSpringWsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

@Configuration
@ConditionalOnClass({Builder.class, EnableWs.class, WsConfigurerAdapter.class})
@AutoConfigureBefore(TraceContextAutoConfiguration.class)
@Import(TraceSpringWsConfiguration.class)
public class TraceSpringWsAutoConfiguration {

}

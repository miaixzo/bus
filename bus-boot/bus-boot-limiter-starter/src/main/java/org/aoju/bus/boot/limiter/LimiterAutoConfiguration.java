package org.aoju.bus.boot.limiter;


import org.aoju.bus.spring.limiter.LimiterConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Limiter 自动配置
 */
@Configuration
@Import(value = {LimiterConfiguration.class})
public class LimiterAutoConfiguration {

}

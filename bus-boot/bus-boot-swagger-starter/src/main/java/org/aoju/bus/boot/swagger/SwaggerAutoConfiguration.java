package org.aoju.bus.boot.swagger;


import org.aoju.bus.spring.druid.DruidConfiguration;
import org.aoju.bus.spring.druid.DruidMonitorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Druid自动配置
 */
@Configuration
@Import(value = {DruidConfiguration.class, DruidMonitorConfiguration.class})
public class SwaggerAutoConfiguration {
}

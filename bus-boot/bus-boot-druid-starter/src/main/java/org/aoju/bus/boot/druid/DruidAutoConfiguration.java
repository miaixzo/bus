package org.aoju.bus.boot.druid;


import org.aoju.bus.spring.druid.DruidConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Druid自动配置
 */
@Configuration
@Import(DruidConfiguration.class)
public class DruidAutoConfiguration {
}

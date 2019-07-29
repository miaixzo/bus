package org.aoju.bus.boot.cors;


import org.aoju.bus.spring.cors.CorsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Cors 自动配置
 */
@Configuration
@Import(value = {CorsConfiguration.class})
public class CorsAutoConfiguration {

}

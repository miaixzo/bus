package org.aoju.bus.boot.swagger;


import org.aoju.bus.spring.swagger.SwaggerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Swagger 自动配置
 */
@Configuration
@Import(value = {SwaggerConfiguration.class})
public class SwaggerAutoConfiguration {

}

package org.aoju.bus.boot.validate;


import org.aoju.bus.spring.validate.ValidateConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Validate 自动配置
 */
@Configuration
@Import(value = {ValidateConfiguration.class})
public class ValidateAutoConfiguration {

}

package org.aoju.bus.boot.sensitive;


import org.aoju.bus.spring.sensitive.SensitiveConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Sensitive 自动配置
 */
@Configuration
@Import(value = {SensitiveConfiguration.class})
public class SensitiveAutoConfiguration {

}

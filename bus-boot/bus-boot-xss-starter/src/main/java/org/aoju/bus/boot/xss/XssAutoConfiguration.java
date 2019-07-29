package org.aoju.bus.boot.xss;


import org.aoju.bus.spring.xss.XssConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Xss 自动配置
 */
@Configuration
@Import(value = {XssConfiguration.class})
public class XssAutoConfiguration {

}

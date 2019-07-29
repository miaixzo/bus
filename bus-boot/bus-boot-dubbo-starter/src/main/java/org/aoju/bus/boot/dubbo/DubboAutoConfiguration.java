package org.aoju.bus.boot.dubbo;


import org.aoju.bus.spring.dubbo.DubboConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Dubbo 自动配置
 */
@Configuration
@Import(value = {DubboConfiguration.class})
public class DubboAutoConfiguration {

}

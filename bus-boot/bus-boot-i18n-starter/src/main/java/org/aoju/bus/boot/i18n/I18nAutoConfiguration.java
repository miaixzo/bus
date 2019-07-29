package org.aoju.bus.boot.i18n;


import org.aoju.bus.spring.i18n.I18nConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * I18n 自动配置
 */
@Configuration
@Import(value = {I18nConfiguration.class})
public class I18nAutoConfiguration {

}

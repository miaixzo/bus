package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.i18n.I18nConfiguration;
import org.aoju.bus.spring.i18n.LocaleMessage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启国际化支持
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({I18nConfiguration.class, LocaleMessage.class})
public @interface EnableI18n {

}

package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.xss.XssConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启防 Xss 攻击
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
@Import({XssConfiguration.class})
public @interface EnableXssFilter {

}

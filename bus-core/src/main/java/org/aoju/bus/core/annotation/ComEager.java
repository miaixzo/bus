package org.aoju.bus.core.annotation;

import java.lang.annotation.*;

/**
 * 公共注解
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComEager {
}

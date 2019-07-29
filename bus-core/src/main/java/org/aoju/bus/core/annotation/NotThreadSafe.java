package org.aoju.bus.core.annotation;

import java.lang.annotation.*;

/**
 * 线程不安全安全注解
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotThreadSafe {

}

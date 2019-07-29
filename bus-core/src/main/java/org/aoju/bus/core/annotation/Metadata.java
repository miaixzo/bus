package org.aoju.bus.core.annotation;

import java.lang.annotation.*;

/**
 * 元注解
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface Metadata {

}

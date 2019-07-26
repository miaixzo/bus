package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 校验组注解，标记当前位置被激活的校验组
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Group {

    /**
     * 表示被激活的校验组
     */
    String[] value() default {};

}

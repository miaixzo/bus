package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 标记注解，标明要被拦截的类或方法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {


    /**
     * 校验属性 {"id","name"}
     */
    String[] value() default {};

    /**
     * 忽略属性 {"created","creator"}
     */
    String[] skip() default {};

    /**
     * 内部校验:true/false
     */
    boolean inside() default true;

}

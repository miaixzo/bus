package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 自定义校验注解元注解，在任意的注解定义上，增加该注解标明这是一个校验注解
 *
 * <p>
 * 在校验环境
 * </P>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Complex {

    /**
     * 校验器名称， 优先使用类型匹配
     */
    String value() default "";

    /**
     * 校验器类， 优先使用类型匹配
     */
    Class<? extends org.aoju.bus.validate.validators.Complex> clazz() default org.aoju.bus.validate.validators.Complex.class;

}

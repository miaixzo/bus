package org.aoju.bus.validate.annotation;


import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.InStrategy;

import java.lang.annotation.*;

/**
 * 字符串在数组中
 *
 * <p>
 * 默认被校验对象是null时，通过校验
 * </P>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Complex(value = Builder._IN, clazz = InStrategy.class)
public @interface In {

    @Filler("value")
    String[] value() default {};

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}必须在指定字符串数组中: ${value}";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

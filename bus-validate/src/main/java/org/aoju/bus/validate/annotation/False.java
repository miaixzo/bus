package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.FalseStrategy;

import java.lang.annotation.*;

/**
 * boolean校验, 当校验值为false时通过校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Complex(value = Builder._FALSE, clazz = FalseStrategy.class)
public @interface False {

    /**
     * 当参数为null时，是否允许通过校验。true：校验通过
     */
    boolean nullable() default true;

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}只能为false";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

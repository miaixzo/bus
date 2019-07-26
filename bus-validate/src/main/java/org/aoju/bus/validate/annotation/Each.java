package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.EachStrategy;
import org.aoju.bus.validate.validators.Complex;

import java.lang.annotation.*;

/**
 * 对数组、集合、Map元素进行校验， 注意，Map对象，只校验内部的值列表
 *
 * <p>
 * 对象为null， 忽略校验
 * </P>
 * <p>
 * 如果不是数组或集合、Map，则忽略校验
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
@org.aoju.bus.validate.annotation.Complex(value = Builder._EACH, clazz = EachStrategy.class)
public @interface Each {

    /**
     * 校验器名称数组，优先使用校验器名称中的校验器，并忽略校验器类中的校验器
     */
    String[] value() default {};

    /**
     * 校验器类数组， 当校验器名称数组为空时，使用校验器类数组中的校验器
     */
    Class<? extends Complex>[] classes() default {};

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}参数校验失败";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

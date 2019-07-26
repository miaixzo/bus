package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.ReflectStrategy;

import java.lang.annotation.*;

/**
 * 通过反射调用被校验参数，并判断反射方法的结果
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
@Complex(value = Builder._REFLECT, clazz = ReflectStrategy.class)
public @interface Reflect {

    /**
     * 反射要执行的类
     */
    Class<?> target();

    /**
     * 反射要执行的方法
     */
    String method();

    /**
     * 校验器名称数组，将会校验反射的执行结果
     */
    String[] validator() default {};

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

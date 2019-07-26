package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.LengthStrategy;

import java.lang.annotation.*;

/**
 * 字符串、数组、集合的长度校验
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
@Complex(value = Builder._LENGTH, clazz = LengthStrategy.class)
public @interface Length {

    /**
     * 最小长度, 小于等于
     */
    @Filler("min")
    int min() default Integer.MIN_VALUE;

    /**
     * 最大长度，大于等于
     */
    @Filler("max")
    int max() default Integer.MAX_VALUE;

    /**
     * 如果长度为0，判断能否通过校验。
     * <p>
     * 默认为false
     * </P>
     * <p>
     * true：表示长度为零，默认通过校验；false：表示长度为0，仍然要进行长度验证
     * </P>
     */
    boolean zeroAble() default false;

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}长度必须在指定范围内, 最小: ${min}, 最大: ${max}";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

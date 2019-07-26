package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.IntRangeStrategy;

import java.lang.annotation.*;

/**
 * 判断数字在int范围内
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
@Complex(value = Builder._INT_RANGE, clazz = IntRangeStrategy.class)
public @interface IntRange {

    /**
     * 小于等于
     */
    @Filler("min")
    int min() default Integer.MIN_VALUE;

    /**
     * 大于等于
     */
    @Filler("max")
    int max() default Integer.MAX_VALUE;

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}数字必须在指定范围内, 最小: ${min}, 最大: ${max}";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

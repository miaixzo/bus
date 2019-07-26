package org.aoju.bus.validate.annotation;

import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.RegexStrategy;

import java.lang.annotation.*;

/**
 * 正则表达式校验
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
@Complex(value = Builder._REGEX, clazz = RegexStrategy.class)
public @interface Regex {

    @Filler("pattern")
    String pattern() default "";

    /**
     * 如果长度为0，判断能否通过校验。
     * <p>
     * 默认为false
     * </P>
     * <p>
     * true：表示长度为零，默认通过校验；false：表示长度为0，仍然要进行正则验证
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
    String errmsg() default "${field}验证失败,请检查数据格式";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

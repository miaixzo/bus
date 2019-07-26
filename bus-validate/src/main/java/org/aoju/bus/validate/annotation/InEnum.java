package org.aoju.bus.validate.annotation;


import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.InEnumStrategy;

import java.lang.annotation.*;

/**
 * 校验对象在枚举中，默认将对象与枚举名称匹配。
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
@Complex(value = Builder._IN_ENUM, clazz = InEnumStrategy.class)
public @interface InEnum {

    /**
     * 枚举类型
     */
    @Filler("enumClass")
    Class<? extends Enum> enumClass();

    /**
     * 枚举中的方法，将枚举方法的结果与被校验参数进行equals判断校验结果
     */
    String method() default "name";

    /**
     * 默认使用的异常码
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}必须属于指定枚举类型:${enumClass}";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;

}

package org.aoju.bus.validate.validators;

import org.aoju.bus.validate.Context;

/**
 * 校验器接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@FunctionalInterface
public interface Complex<T, K> {

    /**
     * 将Validator转为Complex
     *
     * @param validator Validator对象
     * @param <T>       Validator泛型
     * @return ComplexValidator对象
     */
    static <T> Complex<T, ?> of(Validator<T> validator) {
        return (object, annotation, context) -> validator.on(object, context);
    }

    /**
     * 根据校验器，创建相对立的一个校验器
     *
     * @param complex 校验器
     * @param <T>     校验对象泛型
     * @param <K>     校验器注解泛型
     * @return 新的校验器，永远与传入参数的校验器的校验结果相反
     */
    static <T, K> Complex<T, K> not(Complex<T, K> complex) {
        return (object, anno, context) -> !complex.on(object, anno, context);
    }

    /**
     * 校验对象
     *
     * @param object     被校验的对象
     * @param annotation 被校验对象的注解
     * @param context    校验环境上下文
     * @return 校验结果，true：校验通过
     */
    boolean on(T object, K annotation, Context context);

}

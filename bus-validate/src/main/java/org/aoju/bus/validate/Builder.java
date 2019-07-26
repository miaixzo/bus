package org.aoju.bus.validate;

import org.aoju.bus.core.instance.Instances;
import org.aoju.bus.core.lang.Validator;

import java.lang.annotation.Annotation;

/**
 * 当前框架内预定义的校验器名称
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Builder extends Validator {

    // 默认错误码
    public static final String DEFAULT_ERRCODE = "-1";
    // 默认属性名
    public static final String DEFAULT_FIELD = "field";

    // 校验对象参数
    public static final String VAL = "val";
    public static final String FIELD = "field";
    public static final String GROUP = "group";
    public static final String ERRCODE = "errcode";
    public static final String ERRMSG = "errmsg";

    /**
     * 参数校验
     */
    public static final String _ALWAYS = "Always";
    public static final String _BLANK = "Blank";
    public static final String _EACH = "Each";
    public static final String _EQUALS = "Equals";
    public static final String _FALSE = "False";
    public static final String _IN = "In";
    public static final String _IN_ENUM = "InEnum";
    public static final String _INT_RANGE = "IntRange";
    public static final String _LENGTH = "Length";
    public static final String _MULTI = "Multi";
    public static final String _NOT_BLANK = "NotBlank";
    public static final String _NOT_IN = "NotIn";
    public static final String _NOT_NULL = "NotNull";
    public static final String _NULL = "Null";
    public static final String _REFLECT = "Reflect";
    public static final String _REGEX = "Regex";
    public static final String _TRUE = "True";

    /**
     * 被校验对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object 原始对象
     */
    public static <T> T on(Object object) {
        return (T) Instances.singletion(Provider.class).on(object);
    }

    /**
     * 被校验对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object  原始对象
     * @param context 上下文信息
     */
    public static <T> T on(Object object, Context context) {
        return (T) Instances.singletion(Provider.class).on(object, context);
    }

    /**
     * 被校验对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object      原始对象
     * @param annotations 注解信息
     */
    public static <T> T on(Object object, Annotation[] annotations) {
        return (T) Instances.singletion(Provider.class).on(object, annotations);
    }

    /**
     * 被校验对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object      原始对象
     * @param annotations 注解信息
     * @param context     上下文信息
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context) {
        return (T) Instances.singletion(Provider.class).on(object, annotations, context);
    }

    /**
     * 被校验对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object      原始对象
     * @param annotations 注解信息
     * @param context     上下文信息
     * @param field       当前属性
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context, String field) {
        return (T) Instances.singletion(Provider.class).on(object, annotations, context, field);
    }

}

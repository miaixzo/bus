package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 字段错误消息插值填充的功能注解
 * <p>
 * 当前的框架内，默认提供的两个插值是：field: 注解内定义的字段名称， val: 被校验对象的字符串格式
 * </P>
 * <p>
 * 在校验注解内部提供的方法上标记该注解，表示可以在校验注解的emsg中使用该字符串插值
 * </P>
 * 示例：
 *
 * <pre>
 * public @interface EqualsStrategy {
 *
 *      <code>@Filler("value")</code>
 *       String value();
 *
 *       String errmsg() "${field}字符串不能与${value}相同"
 * }
 * </code>
 * </pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filler {

    String value();

}

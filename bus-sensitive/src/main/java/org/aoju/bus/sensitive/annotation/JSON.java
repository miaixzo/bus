package org.aoju.bus.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 对json内的key_value进行脱敏
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON {
    /**
     * 需要脱敏的字段的数组
     *
     * @return 返回结果
     */
    Field[] value() default {};
}

package org.aoju.bus.sensitive.annotation;

import org.aoju.bus.sensitive.provider.ConditionProvider;

import java.lang.annotation.*;

/**
 * 用于自定义策略生效条件的注解
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {

    /**
     * 策略生效的条件
     *
     * @return 对应的条件实现
     */
    Class<? extends ConditionProvider> value();

}

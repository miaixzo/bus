package org.aoju.bus.mapper.annotation;

import org.aoju.bus.mapper.criteria.Style;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 名字转换样式，注解的优先级高于全局配置
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NameStyle {

    Style value() default Style.normal;

}

package org.ukettle.www.xstream.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述：注解属性
 * 
 * @author Kimi Liu
 * @Date May 10, 2014
 * @Time 18:21:53
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface XStream2Field {

}

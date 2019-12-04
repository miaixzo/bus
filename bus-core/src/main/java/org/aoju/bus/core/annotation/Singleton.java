package org.aoju.bus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当您希望仅对该绑定的所有注入重用一个实例时,将此应用于实现类
 *
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
@Scope
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {

}

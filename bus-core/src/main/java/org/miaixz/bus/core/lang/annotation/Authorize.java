package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

/**
 * 该方法在映射时会注入当前登录对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
public @interface Authorize {

}
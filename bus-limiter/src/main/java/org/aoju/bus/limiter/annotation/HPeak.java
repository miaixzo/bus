package org.aoju.bus.limiter.annotation;

import java.lang.annotation.*;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HPeak {

    String limiter() default "";

    String key() default "";

    String fallback() default "defaultFallbackResolver";

    String errorHandler() default "defaultErrorHandler";

    String[] argumentInjectors() default {};

    /**
     * 最大并发数
     *
     * @return
     */
    int max() default 10;
}

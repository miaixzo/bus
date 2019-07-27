package org.aoju.bus.limiter.annotation;

import java.lang.annotation.*;

/**
 * 锁
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HLocks.class)
public @interface HLock {

    String limiter() default "";

    String key() default "";

    String fallback() default "defaultFallbackResolver";

    String errorHandler() default "defaultErrorHandler";

    String[] argumentInjectors() default {};

}

package org.aoju.bus.limiter.annotation;

import java.lang.annotation.*;

/**
 * 限流
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HRateLimiter {

    String limiter() default "";

    String key() default "";

    String fallback() default "defaultFallbackResolver";

    String errorHandler() default "defaultErrorHandler";

    String[] argumentInjectors() default {};

    /**
     * 限制的频率 默认 1次/秒
     *
     * @return
     */
    double rate() default 10.0d;

    /**
     * 最大可累计的令牌容量
     * 默认为 1 且最小为1
     *
     * @return
     */
    long capacity() default 10;

}

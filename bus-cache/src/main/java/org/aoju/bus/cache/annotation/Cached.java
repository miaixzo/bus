package org.aoju.bus.cache.annotation;

import org.aoju.bus.cache.entity.Expire;

import java.lang.annotation.*;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {

    /**
     * @return Specifies the <b>Used cache implementation</b>,
     * default the first {@code caches} config in {@code CacheAspect}
     */
    String value() default "";

    /**
     * @return Specifies the start prefix on every key,
     * if the {@code Method} have non {@code param},
     * {@code prefix} is the <b>consts key</b> used by this {@code Method}
     */
    String prefix() default "";

    /**
     * @return use <b>SpEL</b>,
     * when this spel is {@code true}, this {@code Method} will go through by cache
     */
    String condition() default "";

    /**
     * @return expire time, time unit: <b>seconds</b>
     */
    int expire() default Expire.FOREVER;
}
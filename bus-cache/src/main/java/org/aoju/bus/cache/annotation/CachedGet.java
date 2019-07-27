package org.aoju.bus.cache.annotation;


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
public @interface CachedGet {

    /**
     * @return Specifies the <b>Used cache implementation</b>,
     * default the first {@code caches} config in {@code CacheAspect}
     * @since 0.3
     */
    String value() default "";

    /**
     * @return Specifies the start keyExp on every key,
     * if the {@code Method} have non {@code param},
     * {@code keyExp} is the <b>consts key</b> used by this {@code Method}
     * @since 0.3
     */
    String prefix() default "";

    /**
     * @return use <b>SpEL</b>,
     * when this spel is {@code true}, this {@Code Method} will go through by cache
     * @since 0.3
     */
    String condition() default "";
}

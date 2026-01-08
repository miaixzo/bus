/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.magic.annotation;

import java.lang.annotation.*;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.core.lang.Normal;

/**
 * Enables read-through caching for a method.
 * <p>
 * When a method annotated with {@code @Cached} is invoked, the framework will first attempt to retrieve the result from
 * the cache. If the result is not found (a cache miss), the original method is executed, and its return value is stored
 * in the cache before being returned.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Cached {

    /**
     * The name of the cache to use.
     * <p>
     * If not specified, the default cache (the first one configured in the cache manager) will be used.
     * </p>
     *
     * @return The name of the cache implementation.
     */
    String value() default Normal.EMPTY;

    /**
     * A static prefix for the generated cache key.
     * <p>
     * If the annotated method has no parameters, this prefix can serve as the complete cache key.
     * </p>
     *
     * @return The prefix for the cache key.
     */
    String prefix() default Normal.EMPTY;

    /**
     * A SpEL (Spring Expression Language) expression for conditional caching.
     * <p>
     * The method will only be cached if this expression evaluates to {@code true}.
     * </p>
     *
     * @return The SpEL condition.
     */
    String condition() default Normal.EMPTY;

    /**
     * The time-to-live (TTL) for the cached entry, in milliseconds.
     * <p>
     * Defaults to {@link CacheExpire#FOREVER}, meaning the entry will not expire based on time.
     * </p>
     *
     * @return The expiration time in milliseconds.
     */
    int expire() default CacheExpire.FOREVER;

}

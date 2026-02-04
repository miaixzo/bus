/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

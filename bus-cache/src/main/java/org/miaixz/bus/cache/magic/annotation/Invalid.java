/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.magic.annotation;

import java.lang.annotation.*;
import org.miaixz.bus.core.lang.Normal;

/**
 * Triggers cache invalidation after the annotated method is executed.
 * <p>
 * When the method is invoked, the framework will remove one or more cache entries based on the keying rules derived
 * from this annotation and any {@link CacheKey} annotations on the parameters.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Invalid {

    /**
     * The name of the cache from which to invalidate entries.
     * <p>
     * This functions identically to the {@code value} attribute in the {@link Cached} annotation.
     * </p>
     *
     * @return The name of the cache implementation.
     */
    String value() default Normal.EMPTY;

    /**
     * A static prefix for the cache key(s) to be invalidated.
     * <p>
     * This functions identically to the {@code prefix} attribute in the {@link Cached} annotation.
     * </p>
     *
     * @return The prefix for the cache key.
     */
    String prefix() default Normal.EMPTY;

    /**
     * A SpEL (Spring Expression Language) expression for conditional invalidation.
     * <p>
     * The invalidation will only occur if this expression evaluates to {@code true}. This functions identically to the
     * {@code condition} attribute in the {@link Cached} annotation.
     * </p>
     *
     * @return The SpEL condition.
     */
    String condition() default Normal.EMPTY;

}

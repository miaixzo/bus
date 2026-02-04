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
import org.miaixz.bus.core.lang.Normal;

/**
 * Marks a method parameter as a component of a cache key.
 * <p>
 * This annotation is used on parameters of a method annotated with {@link Cached}, {@link CachedGet}, or
 * {@link Invalid} to specify how the parameter contributes to the cache key.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CacheKey {

    /**
     * A SpEL (Spring Expression Language) expression to extract a value from the annotated parameter.
     * <p>
     * The result of this expression will be used as a part of the cache key. If the expression is empty, the string
     * representation of the entire parameter object will be used.
     * </p>
     *
     * @return The SpEL expression to select a portion of the parameter for the key.
     */
    String value() default Normal.EMPTY;

    /**
     * Specifies the name of a field to be used for mapping in multi-key operations.
     * <p>
     * This is typically used when a cached method returns a {@code Collection} of objects. The {@code field} indicates
     * which property of the objects in the returned collection corresponds to the ID used to generate the cache key.
     * This allows the framework to correctly map the returned values back to the cache keys for writing.
     * </p>
     *
     * @return The name of the field to be used for key mapping.
     */
    String field() default Normal.EMPTY;

}

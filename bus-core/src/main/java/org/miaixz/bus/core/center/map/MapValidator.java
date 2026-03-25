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
package org.miaixz.bus.core.center.map;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class for validating {@link Map} objects, primarily providing convenient checks for their empty state.
 * <p>
 * A map is considered <b>empty</b> if it is {@code null} or contains no key-value mappings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapValidator {

    /**
     * Checks if the given {@link Map} is empty.
     *
     * @param map The map to check.
     * @return {@code true} if the map is {@code null} or has no key-value mappings; {@code false} otherwise.
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * Checks if the given {@link Map} is not empty.
     *
     * @param map The map to check.
     * @return {@code true} if the map is not {@code null} and contains at least one key-value mapping; {@code false}
     *         otherwise.
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Returns an unmodifiable empty map if the provided map is {@code null}, otherwise returns the original map. This
     * is useful for safely handling potentially {@code null} map inputs without creating new empty maps unnecessarily.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The map to check, may be {@code null}.
     * @return The original map if it's not {@code null}, or an unmodifiable empty map if it is {@code null}.
     */
    public static <K, V> Map<K, V> emptyIfNull(final Map<K, V> map) {
        return (null == map) ? Collections.emptyMap() : map;
    }

}

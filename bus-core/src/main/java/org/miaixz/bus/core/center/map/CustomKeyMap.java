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

import java.io.Serial;
import java.util.Map;

/**
 * An abstract {@link Map} implementation that allows for custom key handling while keeping values untransformed. This
 * class extends {@link TransMap} and provides a default implementation for {@link #customValue(Object)} that returns
 * the value as is. Subclasses must implement {@link #customKey(Object)} to define their key transformation logic.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CustomKeyMap<K, V> extends TransMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852272718897L;

    /**
     * Constructs a {@code CustomKeyMap} that wraps a pre-existing empty map. It is crucial that the provided map is
     * empty, as existing entries will not have their keys transformed, which could lead to inconsistent behavior.
     *
     * @param emptyMap The empty map to be wrapped. Must be empty to ensure custom key transformations are effective.
     */
    public CustomKeyMap(final Map<K, V> emptyMap) {
        super(emptyMap);
    }

    /**
     * Customizes the value. In this implementation, no transformation is applied to the value; the original value is
     * returned as is.
     *
     * @param value The original value.
     * @return The original value, untransformed.
     */
    @Override
    protected V customValue(final Object value) {
        return (V) value;
    }

}

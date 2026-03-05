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
import java.util.function.Function;

/**
 * A {@link CustomKeyMap} implementation that allows for custom key transformation using a {@link Function}. This class
 * provides a flexible way to define how keys are stored and retrieved in the map, while leaving the values
 * untransformed.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionKeyMap<K, V> extends CustomKeyMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852273768203L;

    /**
     * The function used to transform keys.
     */
    private final Function<Object, K> keyFunc;

    /**
     * Constructs a new {@code FunctionKeyMap} with a pre-existing empty map and a custom key transformation function.
     * It is crucial that the provided map is empty, as existing entries will not have their keys transformed, which
     * could lead to inconsistent behavior.
     *
     * @param emptyMap The empty map to be wrapped. Must be empty to ensure custom key transformations are effective.
     * @param keyFunc  The function to customize keys. Must not be {@code null}.
     */
    public FunctionKeyMap(final Map<K, V> emptyMap, final Function<Object, K> keyFunc) {
        super(emptyMap);
        this.keyFunc = keyFunc;
    }

    /**
     * Transforms the given key using the provided {@link #keyFunc}. This method is called by the parent
     * {@link CustomKeyMap} for all key-based operations.
     *
     * @param key The original key.
     * @return The transformed key.
     */
    @Override
    protected K customKey(final Object key) {
        if (null != this.keyFunc) {
            return keyFunc.apply(key);
        }
        return (K) key;
    }

}

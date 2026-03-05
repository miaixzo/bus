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
import java.util.function.Supplier;

/**
 * A concrete implementation of {@link TransMap} that uses provided {@link Function}s to transform keys and values. This
 * class is useful for creating maps with specific, consistent transformation rules, such as case-insensitive keys or
 * trimmed string values.
 * <p>
 * Example: Creating a case-insensitive map for keys.
 * 
 * <pre>{@code
 * Map<String, String> map = new FunctionMap<>(new HashMap<>(), (key) -> key.toString().toLowerCase(), // Key function
 *         null // No value transformation
 * );
 * map.put("Key1", "value1");
 * map.get("key1"); // Returns "value1"
 * }</pre>
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionMap<K, V> extends TransMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852273919816L;

    /**
     * The function used to transform keys.
     */
    private final Function<Object, K> keyFunc;
    /**
     * The function used to transform values.
     */
    private final Function<Object, V> valueFunc;

    /**
     * Constructs a new {@code FunctionMap} with a map factory and transformation functions. The factory should supply a
     * new, empty map, as existing entries in a non-empty map will not be transformed, potentially leading to an
     * inconsistent state.
     *
     * @param mapFactory A supplier that provides an empty {@link Map} instance for internal use.
     * @param keyFunc    The function to apply to keys; if {@code null}, no transformation is applied.
     * @param valueFunc  The function to apply to values; if {@code null}, no transformation is applied.
     */
    public FunctionMap(final Supplier<Map<K, V>> mapFactory, final Function<Object, K> keyFunc,
            final Function<Object, V> valueFunc) {
        this(mapFactory.get(), keyFunc, valueFunc);
    }

    /**
     * Constructs a new {@code FunctionMap} with a pre-existing empty map and transformation functions. The provided map
     * must be empty to ensure that all entries are correctly transformed upon insertion.
     *
     * @param emptyMap  The empty map to be wrapped.
     * @param keyFunc   The function to apply to keys; if {@code null}, no transformation is applied.
     * @param valueFunc The function to apply to values; if {@code null}, no transformation is applied.
     */
    public FunctionMap(final Map<K, V> emptyMap, final Function<Object, K> keyFunc,
            final Function<Object, V> valueFunc) {
        super(emptyMap);
        this.keyFunc = keyFunc;
        this.valueFunc = valueFunc;
    }

    /**
     * Applies the custom key transformation function. This method is called by the parent {@link TransMap} for all
     * key-based operations.
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

    /**
     * Applies the custom value transformation function. This method is called by the parent {@link TransMap} for all
     * operations that involve values.
     *
     * @param value The original value.
     * @return The transformed value.
     */
    @Override
    protected V customValue(final Object value) {
        if (null != this.valueFunc) {
            return valueFunc.apply(value);
        }
        return (V) value;
    }

}

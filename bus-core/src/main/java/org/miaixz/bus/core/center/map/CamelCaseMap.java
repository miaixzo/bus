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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A {@link Map} implementation that automatically converts keys to camel case when they are stored or retrieved. This
 * allows for flexible key access, where keys like "user_name" and "userName" can refer to the same entry.
 * <p>
 * When a key is put into the map, it is converted to camel case. If a camel case version of the key already exists, its
 * value will be overwritten. Retrieval operations also convert the lookup key to camel case.
 *
 * @param <K> The type of keys in the map (typically {@code String}).
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 21+
 */
public class CamelCaseMap<K, V> extends FunctionKeyMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852268690056L;

    /**
     * Constructs an empty {@code CamelCaseMap} with the default initial capacity (16).
     */
    public CamelCaseMap() {
        this(Normal._16);
    }

    /**
     * Constructs an empty {@code CamelCaseMap} with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the map.
     */
    public CamelCaseMap(final int initialCapacity) {
        this(initialCapacity, Normal.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code CamelCaseMap} with the same mappings as the specified map. Keys from the input map will
     * be converted to camel case upon insertion.
     *
     * @param m The map whose mappings are to be placed in this map.
     */
    public CamelCaseMap(final Map<? extends K, ? extends V> m) {
        this(Normal.DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * Constructs a new {@code CamelCaseMap} with the specified load factor and the same mappings as the specified map.
     * Keys from the input map will be converted to camel case upon insertion.
     *
     * @param loadFactor The load factor for the map.
     * @param m          The map whose mappings are to be placed in this map. Its data will be copied into a new
     *                   {@link HashMap}.
     */
    public CamelCaseMap(final float loadFactor, final Map<? extends K, ? extends V> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * Constructs an empty {@code CamelCaseMap} with the specified initial capacity and load factor.
     *
     * @param initialCapacity The initial capacity of the map.
     * @param loadFactor      The load factor for the map.
     */
    public CamelCaseMap(final int initialCapacity, final float loadFactor) {
        this(MapBuilder.of(new HashMap<>(initialCapacity, loadFactor)));
    }

    /**
     * Constructs a {@code CamelCaseMap} by wrapping a map provided by a {@link MapBuilder}. The {@code MapBuilder}
     * should provide an empty map, as existing entries in a non-empty map will not have their keys transformed,
     * potentially leading to inconsistent behavior.
     *
     * @param emptyMapBuilder A {@link MapBuilder} that provides an empty {@link Map} instance.
     */
    public CamelCaseMap(final MapBuilder<K, V> emptyMapBuilder) {
        // The Function is made Serializable to allow the map to be serialized.
        super(emptyMapBuilder.build(), (Function<Object, K> & Serializable) (key) -> {
            if (key instanceof CharSequence) {
                key = StringKit.toCamelCase(key.toString());
            }
            return (K) key;
        });
    }

}

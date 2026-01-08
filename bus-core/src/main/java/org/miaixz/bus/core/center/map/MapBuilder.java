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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * A fluent builder for creating and populating {@link Map} instances. This class provides a chainable API to simplify
 * map creation and initialization.
 * <p>
 * Example:
 * 
 * <pre>{@code
 * 
 * Map<String, Integer> map = MapBuilder.<String, Integer>of().put("one", 1).put("two", 2).build();
 * }</pre>
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapBuilder<K, V> implements Builder<Map<K, V>> {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852275393666L;

    /**
     * The internal map instance being built.
     */
    private final Map<K, V> map;

    /**
     * Constructs a new {@code MapBuilder} that wraps the given map instance.
     *
     * @param map The {@link Map} implementation to use as the backing store.
     */
    public MapBuilder(final Map<K, V> map) {
        this.map = map;
    }

    /**
     * Creates a new {@code MapBuilder} with a {@link HashMap} and adds an initial key-value pair.
     *
     * @param <K>   The type of keys.
     * @param <V>   The type of values.
     * @param key   The initial key.
     * @param value The initial value.
     * @return A new {@code MapBuilder} instance.
     */
    public static <K, V> MapBuilder<K, V> of(final K key, final V value) {
        return MapBuilder.<K, V>of().put(key, value);
    }

    /**
     * Creates a new {@code MapBuilder} with a default {@link HashMap}.
     *
     * @param <K> The type of keys.
     * @param <V> The type of values.
     * @return A new {@code MapBuilder} instance.
     */
    public static <K, V> MapBuilder<K, V> of() {
        return of(false);
    }

    /**
     * Creates a new {@code MapBuilder} with a specified map implementation.
     *
     * @param <K>      The type of keys.
     * @param <V>      The type of values.
     * @param isLinked If {@code true}, a {@link LinkedHashMap} is used to preserve insertion order; otherwise, a
     *                 {@link HashMap} is used.
     * @return A new {@code MapBuilder} instance.
     */
    public static <K, V> MapBuilder<K, V> of(final boolean isLinked) {
        return of(MapKit.newHashMap(isLinked));
    }

    /**
     * Creates a new {@code MapBuilder} that wraps an existing {@link Map} instance.
     *
     * @param <K> The type of keys.
     * @param <V> The type of values.
     * @param map The map instance to wrap.
     * @return A new {@code MapBuilder} instance.
     */
    public static <K, V> MapBuilder<K, V> of(final Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * Adds a key-value pair to the map.
     *
     * @param k The key.
     * @param v The value.
     * @return This {@code MapBuilder} instance for method chaining.
     */
    public MapBuilder<K, V> put(final K k, final V v) {
        map.put(k, v);
        return this;
    }

    /**
     * Adds a key-value pair to the map only if the given condition is {@code true}.
     *
     * @param condition The boolean condition to evaluate.
     * @param k         The key to add if the condition is true.
     * @param v         The value to add if the condition is true.
     * @return This {@code MapBuilder} instance for method chaining.
     */
    public MapBuilder<K, V> put(final boolean condition, final K k, final V v) {
        if (condition) {
            put(k, v);
        }
        return this;
    }

    /**
     * Adds a key-value pair to the map only if the given condition is {@code true}, with the value provided by a
     * {@link Supplier}. The supplier is only invoked if the condition is met.
     *
     * @param condition The boolean condition to evaluate.
     * @param k         The key to add if the condition is true.
     * @param supplier  A {@link Supplier} that provides the value.
     * @return This {@code MapBuilder} instance for method chaining.
     */
    public MapBuilder<K, V> put(final boolean condition, final K k, final Supplier<V> supplier) {
        if (condition) {
            put(k, supplier.get());
        }
        return this;
    }

    /**
     * Copies all mappings from the specified map into the map being built.
     *
     * @param map The map whose mappings are to be added.
     * @return This {@code MapBuilder} instance for method chaining.
     */
    public MapBuilder<K, V> putAll(final Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * Removes all mappings from the map being built.
     *
     * @return This {@code MapBuilder} instance for method chaining.
     */
    public MapBuilder<K, V> clear() {
        this.map.clear();
        return this;
    }

    /**
     * Returns the underlying map instance that is being built.
     *
     * @return The built {@link Map}.
     */
    public Map<K, V> map() {
        return map;
    }

    /**
     * Builds and returns the final map instance.
     *
     * @return The built {@link Map}.
     */
    @Override
    public Map<K, V> build() {
        return map();
    }

    /**
     * Joins the map entries into a string using the specified separators.
     *
     * @param separator         The separator to use between each entry.
     * @param keyValueSeparator The separator to use between each key and its value.
     * @return The joined string.
     */
    public String join(final String separator, final String keyValueSeparator) {
        return MapKit.join(this.map, separator, keyValueSeparator);
    }

    /**
     * Joins the map entries into a string, ignoring entries where the key or value is {@code null}.
     *
     * @param separator         The separator to use between each entry.
     * @param keyValueSeparator The separator to use between each key and its value.
     * @return The joined string.
     */
    public String joinIgnoreNull(final String separator, final String keyValueSeparator) {
        return MapKit.joinIgnoreNull(this.map, separator, keyValueSeparator);
    }

    /**
     * Joins the map entries into a string with an option to ignore nulls.
     *
     * @param separator         The separator to use between each entry.
     * @param keyValueSeparator The separator to use between each key and its value.
     * @param isIgnoreNull      If {@code true}, entries with a {@code null} key or value are skipped.
     * @return The joined string.
     */
    public String join(final String separator, final String keyValueSeparator, final boolean isIgnoreNull) {
        return MapKit.join(this.map, separator, keyValueSeparator, isIgnoreNull);
    }

}

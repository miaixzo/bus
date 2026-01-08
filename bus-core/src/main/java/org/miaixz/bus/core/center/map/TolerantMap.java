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
import java.util.Map;
import java.util.Objects;

/**
 * A decorator for a {@link Map} that returns a default value instead of {@code null} when a key is not found. This
 * provides a "tolerant" or forgiving alternative to a standard map, preventing {@code NullPointerException}s in chains
 * of operations.
 * <p>
 * Example:
 * 
 * <pre>{@code
 * 
 * Map<String, Integer> map = new TolerantMap<>(new HashMap<>(), -1);
 * Integer value = map.get("nonexistent_key"); // returns -1 instead of null
 * }</pre>
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TolerantMap<K, V> extends MapWrapper<K, V> {

    @Serial
    private static final long serialVersionUID = 2852276071699L;

    /**
     * The default value to return when a key is not found in the map.
     */
    private final V defaultValue;

    /**
     * Constructs a new {@code TolerantMap} wrapping a {@link HashMap} with the specified default value.
     *
     * @param defaultValue The value to return for non-existent keys.
     */
    public TolerantMap(final V defaultValue) {
        this(new HashMap<>(), defaultValue);
    }

    /**
     * Constructs a new {@code TolerantMap} with a specified capacity, load factor, and default value.
     *
     * @param initialCapacity The initial capacity of the underlying {@link HashMap}.
     * @param loadFactor      The load factor of the underlying {@link HashMap}.
     * @param defaultValue    The value to return for non-existent keys.
     */
    public TolerantMap(final int initialCapacity, final float loadFactor, final V defaultValue) {
        this(new HashMap<>(initialCapacity, loadFactor), defaultValue);
    }

    /**
     * Constructs a new {@code TolerantMap} with a specified initial capacity and default value.
     *
     * @param initialCapacity The initial capacity of the underlying {@link HashMap}.
     * @param defaultValue    The value to return for non-existent keys.
     */
    public TolerantMap(final int initialCapacity, final V defaultValue) {
        this(new HashMap<>(initialCapacity), defaultValue);
    }

    /**
     * Constructs a new {@code TolerantMap} that wraps an existing map.
     *
     * @param map          The underlying {@link Map} to wrap. Must not be {@code null}.
     * @param defaultValue The value to return for non-existent keys.
     */
    public TolerantMap(final Map<K, V> map, final V defaultValue) {
        super(map);
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a new {@code TolerantMap} that wraps the given map and provides a default value.
     *
     * @param <K>          The type of keys.
     * @param <V>          The type of values.
     * @param map          The underlying {@link Map} to wrap.
     * @param defaultValue The value to return for non-existent keys.
     * @return A new {@code TolerantMap} instance.
     */
    public static <K, V> TolerantMap<K, V> of(final Map<K, V> map, final V defaultValue) {
        return new TolerantMap<>(map, defaultValue);
    }

    /**
     * Returns the value for the specified key. If the key is not found, it returns the predefined default value instead
     * of {@code null}.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the key, or the default value if the key is not present.
     */
    @Override
    public V get(final Object key) {
        return getOrDefault(key, defaultValue);
    }

    /**
     * Compares this {@code TolerantMap} with another object for equality. The result is {@code true} if and only if the
     * argument is also a {@code TolerantMap} with an equal underlying map and an equal default value.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final TolerantMap<?, ?> that = (TolerantMap<?, ?>) o;
        return getRaw().equals(that.getRaw()) && Objects.equals(defaultValue, that.defaultValue);
    }

    /**
     * Returns the hash code for this map. The hash code is derived from the underlying map and the default value.
     *
     * @return The hash code for this map.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getRaw(), defaultValue);
    }

    /**
     * Returns a string representation of this map, including the default value.
     *
     * @return A string representation of the map.
     */
    @Override
    public String toString() {
        return "TolerantMap{" + "map=" + getRaw() + ", defaultValue=" + defaultValue + '}';
    }

}

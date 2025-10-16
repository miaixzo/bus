/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.map.multiple;

import java.io.Serial;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.map.MapWrapper;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An abstract base implementation of the {@link MultipleValueMap} interface. This class provides the core logic for
 * managing a map where each key can be associated with multiple values, stored in a {@link Collection}. Subclasses must
 * implement {@link #createCollection()} to define the specific type of collection used for storing values.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values stored in the collections.
 * @author Kimi Liu
 * @see CollectionValueMap
 * @see SetValueMap
 * @see ListValueMap
 * @since Java 17+
 */
public abstract class AbstractCollValueMap<K, V> extends MapWrapper<K, Collection<V>>
        implements MultipleValueMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852276903237L;

    /**
     * The default initial capacity for the collections created to hold multiple values.
     */
    protected static final int DEFAULT_COLLECTION_INITIAL_CAPACITY = 3;

    /**
     * Constructs an {@code AbstractCollValueMap} using a factory to create the underlying map.
     *
     * @param mapFactory A supplier that provides a {@link Map} to store the key-collection pairs.
     */
    protected AbstractCollValueMap(final Supplier<Map<K, Collection<V>>> mapFactory) {
        super(mapFactory);
    }

    /**
     * Constructs an {@code AbstractCollValueMap} with initial data from the provided map. A new {@link HashMap} is
     * created to store the data.
     *
     * @param map The map providing initial key-collection pairs.
     */
    protected AbstractCollValueMap(final Map<K, Collection<V>> map) {
        super(new HashMap<>(map));
    }

    /**
     * Constructs an empty {@code AbstractCollValueMap} with a default {@link HashMap} as its underlying storage.
     */
    protected AbstractCollValueMap() {
        super(new HashMap<>(16));
    }

    /**
     * Appends all elements from the given collection to the collection of values associated with the specified key. If
     * no collection is associated with the key, a new one is created using {@link #createCollection()}.
     *
     * @param key  The key to which the values are to be added.
     * @param coll The collection of values to add.
     * @return {@code true} if any values were added, {@code false} otherwise.
     */
    @Override
    public boolean putAllValues(final K key, final Collection<V> coll) {
        if (ObjectKit.isNull(coll)) {
            return false;
        }
        return super.computeIfAbsent(key, k -> createCollection()).addAll(coll);
    }

    /**
     * Appends a single value to the collection of values associated with the specified key. If no collection is
     * associated with the key, a new one is created using {@link #createCollection()}.
     *
     * @param key   The key to which the value is to be added.
     * @param value The value to add.
     * @return {@code true} if the value was added, {@code false} otherwise.
     */
    @Override
    public boolean putValue(final K key, final V value) {
        return super.computeIfAbsent(key, k -> createCollection()).add(value);
    }

    /**
     * Removes a single occurrence of the specified value from the collection associated with the given key. If the
     * collection becomes empty after removal, the key-collection mapping is also removed from the map.
     *
     * @param key   The key from which the value is to be removed.
     * @param value The value to remove.
     * @return {@code true} if the value was removed, {@code false} otherwise.
     */
    @Override
    public boolean removeValue(final K key, final V value) {
        return Optional.ofNullable(super.get(key)).map(t -> t.remove(value)).orElse(false);
    }

    /**
     * Removes all occurrences of the specified values from the collection associated with the given key. If the
     * collection becomes empty after removal, the key-collection mapping is also removed from the map.
     *
     * @param key    The key from which the values are to be removed.
     * @param values A collection of values to remove.
     * @return {@code true} if any values were removed, {@code false} otherwise.
     */
    @Override
    public boolean removeAllValues(final K key, final Collection<V> values) {
        if (CollKit.isEmpty(values)) {
            return false;
        }
        final Collection<V> coll = get(key);
        return ObjectKit.isNotNull(coll) && coll.removeAll(values);
    }

    /**
     * Filters all values in the map based on a given key-value predicate and returns a new {@code MultipleValueMap}
     * containing only the values that satisfy the predicate. The type of the value collections in the new map will be
     * consistent with the default collection type of this instance.
     *
     * @param filter The predicate to apply to each key-value pair. Values for which the predicate returns {@code true}
     *               are retained.
     * @return A new {@code MultipleValueMap} instance with filtered values.
     */
    @Override
    public MultipleValueMap<K, V> filterAllValues(final BiPredicate<K, V> filter) {
        entrySet().forEach(e -> {
            final K k = e.getKey();
            final Collection<V> coll = e.getValue().stream().filter(v -> filter.test(k, v))
                    .collect(Collectors.toCollection(this::createCollection));
            e.setValue(coll);
        });
        return this;
    }

    /**
     * Replaces all values in the map by applying a given key-value binary operator to each value. Returns a new
     * {@code MultipleValueMap} with the transformed values. The type of the value collections in the new map will be
     * consistent with the default collection type of this instance.
     *
     * @param operate The binary operator to apply to each key-value pair to produce a new value.
     * @return A new {@code MultipleValueMap} instance with replaced values.
     */
    @Override
    public MultipleValueMap<K, V> replaceAllValues(final BiFunction<K, V, V> operate) {
        entrySet().forEach(e -> {
            final K k = e.getKey();
            final Collection<V> coll = e.getValue().stream().map(v -> operate.apply(k, v))
                    .collect(Collectors.toCollection(this::createCollection));
            e.setValue(coll);
        });
        return this;
    }

    /**
     * Creates a new, empty {@link Collection} instance to hold multiple values for a key. Subclasses must implement
     * this method to specify the concrete type of collection (e.g., {@link ArrayList}, {@link HashSet}).
     *
     * @return A new {@link Collection} instance.
     */
    protected abstract Collection<V> createCollection();

}

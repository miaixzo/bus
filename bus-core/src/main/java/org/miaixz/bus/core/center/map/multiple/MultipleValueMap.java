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
package org.miaixz.bus.core.center.map.multiple;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * A {@link Map} extension where each key can be associated with multiple values, stored in a {@link Collection}. This
 * interface provides convenient operations that act on the collection of values associated with a key, rather than
 * directly on the collection itself.
 * <p>
 * <strong>Value Collection Type:</strong> The specific type of the value collection (e.g., {@link List}, {@link Set})
 * is determined by the implementing class. When using methods defined in {@code MultipleValueMap} (e.g.,
 * {@link #putValue(Object, Object)}), the implementing class ensures consistency of the collection type. However, if
 * the underlying map's {@code put} or {@code putAll} methods are used directly with arbitrary {@code Collection} types,
 * the consistency of the value collection type cannot be guaranteed.
 * 
 * <p>
 * <strong>Modifying Value Collections:</strong> When a value collection is retrieved via {@link #get(Object)} or
 * {@link #getValues(Object)}, modifications to this returned collection will directly affect the
 * {@code MultipleValueMap} instance, and vice versa. Therefore, when iterating over the map or its value collections,
 * if write operations are performed, care must be taken to avoid potential {@link ConcurrentModificationException}s.
 * 
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values stored in the collections.
 * @author Kimi Liu
 * @see AbstractCollValueMap
 * @see CollectionValueMap
 * @see ListValueMap
 * @see SetValueMap
 * @since Java 17+
 */
public interface MultipleValueMap<K, V> extends Map<K, Collection<V>> {

    /**
     * Replaces the entire collection of values associated with the specified key.
     * <p>
     * Note: This operation removes any old collection of values associated with the key. If you intend to append values
     * to an existing collection, use {@link #putAllValues(Object, Collection)} instead.
     * 
     *
     * @param key   The key with which the specified value collection is to be associated.
     * @param value The new collection of values to be associated with the specified key.
     * @return The previous collection of values associated with {@code key}, or {@code null} if there was no mapping
     *         for {@code key}.
     */
    @Override
    Collection<V> put(K key, Collection<V> value);

    /**
     * Copies all of the mappings from the specified map to this map.
     * <p>
     * Note: This operation replaces any old collection of values associated with the keys. If you intend to append
     * values to existing collections, use {@link #putAllValues(Map)} instead.
     * 
     *
     * @param map The map whose mappings are to be placed in this map.
     */
    @Override
    void putAll(Map<? extends K, ? extends Collection<V>> map);

    /**
     * Appends all key-value pairs from the given map to this instance. This is equivalent to iterating through the
     * input map and calling {@link #putAllValues(Object, Collection)} for each entry.
     *
     * <pre>{@code
     * for (Entry<K, Collection<V>> entry : m.entrySet()) {
     *     K key = entry.getKey();
     *     Collection<V> coll = entry.getValue();
     *     for (V val : coll) {
     *         map.putValue(key, val);
     *     }
     * }
     * }</pre>
     *
     * @param m The map containing key-collection pairs to add.
     */
    default void putAllValues(final Map<? extends K, ? extends Collection<V>> m) {
        if (CollKit.isNotEmpty(m)) {
            m.forEach(this::putAllValues);
        }
    }

    /**
     * Appends all elements from the given collection to the collection of values associated with the specified key. If
     * no collection is associated with the key, a new one is created.
     *
     * <pre>{@code
     * Collection<V> existing = get(key);
     * if (existing == null) {
     *     put(key, newCollectionContaining(coll));
     * } else {
     *     existing.addAll(coll);
     * }
     * }</pre>
     *
     * @param key  The key to which the values are to be added.
     * @param coll The collection of values to add.
     * @return {@code true} if any values were added, {@code false} otherwise.
     */
    boolean putAllValues(K key, final Collection<V> coll);

    /**
     * Appends all elements from the given array to the collection of values associated with the specified key. If no
     * collection is associated with the key, a new one is created.
     *
     * <pre>{@code
     * for (V val : values) {
     *     putValue(key, val);
     * }
     * }</pre>
     *
     * @param key    The key to which the values are to be added.
     * @param values An array of values to add.
     * @return {@code true} if any values were added, {@code false} otherwise.
     */
    default boolean putValues(final K key, final V... values) {
        return ArrayKit.isNotEmpty(values) && putAllValues(key, Arrays.asList(values));
    }

    /**
     * Appends a single value to the collection of values associated with the specified key. If no collection is
     * associated with the key, a new one is created.
     *
     * <pre>{@code
     * Collection<V> coll = get(key);
     * if (null == coll) {
     *     coll = createNewCollection(); // internal method
     *     coll.add(value);
     *     put(key, coll);
     * } else {
     *     coll.add(value);
     * }
     * }</pre>
     *
     * @param key   The key to which the value is to be added.
     * @param value The value to add.
     * @return {@code true} if the value was added, {@code false} otherwise.
     */
    boolean putValue(final K key, final V value);

    /**
     * Removes a single occurrence of the specified value from the collection associated with the given key. If the
     * collection becomes empty after removal, the key-collection mapping is also removed from the map.
     *
     * @param key   The key from which the value is to be removed.
     * @param value The value to remove.
     * @return {@code true} if the value was removed, {@code false} otherwise.
     */
    boolean removeValue(final K key, final V value);

    /**
     * Removes all occurrences of the specified values from the collection associated with the given key. If the
     * collection becomes empty after removal, the key-collection mapping is also removed from the map.
     *
     * @param key    The key from which the values are to be removed.
     * @param values An array of values to remove.
     * @return {@code true} if any values were removed, {@code false} otherwise.
     */
    default boolean removeValues(final K key, final V... values) {
        return ArrayKit.isNotEmpty(values) && removeAllValues(key, Arrays.asList(values));
    }

    /**
     * Removes all occurrences of the specified values from the collection associated with the given key. If the
     * collection becomes empty after removal, the key-collection mapping is also removed from the map.
     *
     * @param key    The key from which the values are to be removed.
     * @param values A collection of values to remove.
     * @return {@code true} if any values were removed, {@code false} otherwise.
     */
    boolean removeAllValues(final K key, final Collection<V> values);

    /**
     * Filters all values in the map based on a given predicate and returns a new {@code MultipleValueMap} containing
     * only the values that satisfy the predicate. The type of the value collections in the new map will be consistent
     * with the default collection type of this instance.
     *
     * @param filter The predicate to apply to each value. Values for which the predicate returns {@code true} are
     *               retained.
     * @return A new {@code MultipleValueMap} instance with filtered values.
     */
    default MultipleValueMap<K, V> filterAllValues(final Predicate<V> filter) {
        return filterAllValues((k, v) -> filter.test(v));
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
    MultipleValueMap<K, V> filterAllValues(BiPredicate<K, V> filter);

    /**
     * Replaces all values in the map by applying a given unary operator to each value. Returns a new
     * {@code MultipleValueMap} with the transformed values. The type of the value collections in the new map will be
     * consistent with the default collection type of this instance.
     *
     * @param operate The unary operator to apply to each value to produce a new value.
     * @return A new {@code MultipleValueMap} instance with replaced values.
     */
    default MultipleValueMap<K, V> replaceAllValues(final UnaryOperator<V> operate) {
        return replaceAllValues((k, v) -> operate.apply(v));
    }

    /**
     * Replaces all values in the map by applying a given key-value binary operator to each value. Returns a new
     * {@code MultipleValueMap} with the transformed values. The type of the value collections in the new map will be
     * consistent with the default collection type of this instance.
     *
     * @param operate The binary operator to apply to each key-value pair to produce a new value.
     * @return A new {@code MultipleValueMap} instance with replaced values.
     */
    MultipleValueMap<K, V> replaceAllValues(BiFunction<K, V, V> operate);

    /**
     * Retrieves a specific value from the collection associated with the given key by its index. If the key does not
     * exist or the index is out of bounds, {@code null} is returned.
     *
     * @param key   The key whose associated collection is to be accessed.
     * @param index The index of the value to retrieve within the collection.
     * @return The value at the specified index, or {@code null} if not found or out of bounds.
     */
    default V getValue(final K key, final int index) {
        final Collection<V> collection = get(key);
        return CollKit.get(collection, index);
    }

    /**
     * Retrieves the collection of values associated with the given key. If the key does not exist, an empty,
     * unmodifiable collection is returned.
     *
     * @param key The key whose associated values are to be returned.
     * @return The collection of values associated with the key, or an empty, unmodifiable collection if the key is not
     *         found.
     */
    default Collection<V> getValues(final K key) {
        return getOrDefault(key, Collections.emptyList());
    }

    /**
     * Retrieves the number of values associated with the given key. If the key does not exist, {@code 0} is returned.
     *
     * @param key The key whose associated values' count is to be returned.
     * @return The number of values associated with the key.
     */
    default int size(final K key) {
        return getValues(key).size();
    }

    /**
     * Iterates over all key-value pairs in the map, where each value in the collection associated with a key is
     * processed individually.
     *
     * <pre>{@code
     * for (Entry<K, Collection<V>> entry : entrySet()) {
     *     K key = entry.getKey();
     *     Collection<V> coll = entry.getValue();
     *     for (V val : coll) {
     *         consumer.accept(key, val);
     *     }
     * }
     * }</pre>
     *
     * @param consumer The {@link BiConsumer} to apply to each key-value pair.
     */
    default void allForEach(final BiConsumer<K, V> consumer) {
        forEach((k, coll) -> coll.forEach(v -> consumer.accept(k, v)));
    }

    /**
     * Retrieves all values from all collections in the map, flattened into a single collection.
     *
     * <pre>{@code
     * List<V> results = new ArrayList<>();
     * for (Collection<V> coll : values()) {
     *     results.addAll(coll);
     * }
     * }</pre>
     *
     * @return A {@link Collection} containing all values from all key-associated collections.
     */
    default Collection<V> allValues() {
        return values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

}

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
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A list-backed {@link Map} implementation that allows duplicate keys. This data structure maintains two parallel lists
 * for keys and values, preserving the insertion order of elements.
 * <p>
 * Because it relies on list iteration for lookups, its performance is O(n) for most operations, making it suitable for
 * smaller datasets where duplicate keys are required.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852275915009L;

    /**
     * The default initial capacity for the internal lists.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * The internal list for storing keys, allowing duplicates.
     */
    private final List<K> keys;
    /**
     * The internal list for storing values.
     */
    private final List<V> values;

    /**
     * Constructs an empty {@code TableMap} with default initial capacity.
     */
    public TableMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty {@code TableMap} with the specified initial capacity.
     *
     * @param size The initial capacity for the internal key and value lists.
     */
    public TableMap(final int size) {
        this.keys = new ArrayList<>(size);
        this.values = new ArrayList<>(size);
    }

    /**
     * Constructs a {@code TableMap} from the given arrays of keys and values. The keys and values are mapped based on
     * their corresponding indices.
     *
     * @param keys   An array of keys.
     * @param values An array of values.
     */
    public TableMap(final K[] keys, final V[] values) {
        this.keys = ListKit.of(keys);
        this.values = ListKit.of(values);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public int size() {
        return keys.size();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean isEmpty() {
        return CollKit.isEmpty(keys);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean containsKey(final Object key) {
        return keys.contains(key);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean containsValue(final Object value) {
        return values.contains(value);
    }

    /**
     * Returns the value associated with the first occurrence of the specified key. If multiple entries exist for the
     * same key, only the value of the first one is returned.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value for the first matching key, or {@code null} if the key is not found.
     */
    @Override
    public V get(final Object key) {
        final int index = keys.indexOf(key);
        if (index > -1) {
            return values.get(index);
        }
        return null;
    }

    /**
     * Retrieves the key associated with the first occurrence of the specified value.
     *
     * @param value The value whose associated key is to be returned.
     * @return The key for the first matching value, or {@code null} if the value is not found.
     */
    public K getKey(final V value) {
        final int index = values.indexOf(value);
        if (index > -1) {
            return keys.get(index);
        }
        return null;
    }

    /**
     * Retrieves all values associated with the specified key.
     *
     * @param key The key whose associated values are to be returned.
     * @return A {@link List} of all values associated with the key.
     */
    public List<V> getValues(final K key) {
        return CollKit.getAny(this.values, CollKit.indexOfAll(this.keys, (ele) -> ObjectKit.equals(ele, key)));
    }

    /**
     * Retrieves all keys associated with the specified value.
     *
     * @param value The value whose associated keys are to be returned.
     * @return A {@link List} of all keys associated with the value.
     */
    public List<K> getKeys(final V value) {
        return CollKit.getAny(this.keys, CollKit.indexOfAll(this.values, (ele) -> ObjectKit.equals(ele, value)));
    }

    /**
     * Adds a new key-value pair to the map. This method always adds a new entry, even if the key already exists. It
     * deviates from the standard {@link Map#put} contract, as it does not replace an existing value.
     *
     * @param key   The key to add.
     * @param value The value to add.
     * @return Always returns {@code null} because no existing value is replaced.
     */
    @Override
    public V put(final K key, final V value) {
        keys.add(key);
        values.add(value);
        return null;
    }

    /**
     * Removes all entries associated with the specified key.
     *
     * @param key The key whose mappings are to be removed.
     * @return The value of the last removed entry, or {@code null} if no mapping was found.
     */
    @Override
    public V remove(final Object key) {
        V lastValue = null;
        int index;
        while ((index = keys.indexOf(key)) > -1) {
            lastValue = removeByIndex(index);
        }
        return lastValue;
    }

    /**
     * Removes the key-value pair at the specified index.
     *
     * @param index The index of the entry to remove.
     * @return The value that was removed.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     */
    public V removeByIndex(final int index) {
        keys.remove(index);
        return values.remove(index);
    }

    /**
     * Adds all mappings from the specified map to this map. Each entry is added as a new key-value pair, preserving
     * duplicates.
     *
     * @param m The map whose entries are to be added.
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    /**
     * Returns a {@link Set} view of the unique keys contained in this map.
     *
     * @return A set containing the unique keys from this map.
     */
    @Override
    public Set<K> keySet() {
        return new HashSet<>(this.keys);
    }

    /**
     * Returns an unmodifiable {@link List} view of all keys in this map, including duplicates.
     *
     * @return A list of all keys in insertion order.
     */
    public List<K> keys() {
        return Collections.unmodifiableList(this.keys);
    }

    /**
     * Returns an unmodifiable {@link Collection} view of the values contained in this map.
     *
     * @return A collection of all values in insertion order.
     */
    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(this.values);
    }

    /**
     * Returns a {@link Set} view of the unique key-value mappings in this map. If the map contains duplicate key-value
     * pairs (e.g., two entries of {@code (key1, value1)}), the returned set will contain only one such entry.
     *
     * @return A set view of the unique entries in this map.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> hashSet = new LinkedHashSet<>();
        for (int i = 0; i < size(); i++) {
            hashSet.add(MapKit.entry(keys.get(i), values.get(i)));
        }
        return hashSet;
    }

    /**
     * Returns an iterator over the entries in this map.
     *
     * @return An {@link Iterator} over the map entries.
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {

            private final Iterator<K> keysIter = keys.iterator();
            private final Iterator<V> valuesIter = values.iterator();

            /**
             * Returns true if the iteration has more elements.
             *
             * @return true if the iteration has more elements
             */
            @Override
            public boolean hasNext() {
                return keysIter.hasNext() && valuesIter.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                return MapKit.entry(keysIter.next(), valuesIter.next());
            }

            /**
             * Removes from the underlying collection the last element returned by this iterator.
             */
            @Override
            public void remove() {
                keysIter.remove();
                valuesIter.remove();
            }
        };
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < size(); i++) {
            action.accept(keys.get(i), values.get(i));
        }
    }

    /**
     * Removes all entries that match the specified key and value.
     *
     * @param key   The key of the entries to remove.
     * @param value The value of the entries to remove.
     * @return {@code true} if any entries were removed, {@code false} otherwise.
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        boolean removed = false;
        for (int i = 0; i < size(); i++) {
            if (ObjectKit.equals(key, keys.get(i)) && ObjectKit.equals(value, values.get(i))) {
                removeByIndex(i);
                removed = true;
                i--; // Re-check the current index since the list has shifted.
            }
        }
        return removed;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        for (int i = 0; i < size(); i++) {
            final V newValue = function.apply(keys.get(i), values.get(i));
            values.set(i, newValue);
        }
    }

    /**
     * Replaces the value of all entries that have the specified key.
     *
     * @param key   The key whose associated values are to be replaced.
     * @param value The new value to be associated with the key.
     * @return The last old value that was replaced, or {@code null} if the key was not found.
     */
    @Override
    public V replace(final K key, final V value) {
        V lastValue = null;
        for (int i = 0; i < size(); i++) {
            if (ObjectKit.equals(key, keys.get(i))) {
                lastValue = values.set(i, value);
            }
        }
        return lastValue;
    }

    /**
     * For each entry with the specified key, computes a new value using the given remapping function. If the function
     * returns {@code null}, the entry is removed.
     *
     * @param key               The key to compute a new value for.
     * @param remappingFunction The function to compute the new value.
     * @return The last computed value, or {@code null} if no value was computed or the last one was {@code null}.
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (null == remappingFunction) {
            return null;
        }

        V lastValue = null;
        for (int i = 0; i < size(); i++) {
            if (ObjectKit.equals(key, keys.get(i))) {
                final V newValue = remappingFunction.apply(key, values.get(i));
                if (null != newValue) {
                    lastValue = values.set(i, newValue);
                } else {
                    removeByIndex(i);
                    i--; // Re-check the current index since the list has shifted.
                }
            }
        }
        return lastValue;
    }

}

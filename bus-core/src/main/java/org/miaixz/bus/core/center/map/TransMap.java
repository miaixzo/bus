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
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.SupplierX;

/**
 * An abstract map implementation that automatically transforms keys and values. Subclasses must implement the
 * {@link #customKey(Object)} and {@link #customValue(Object)} methods to define the transformation logic. Key
 * normalization must be stable and idempotent. New value inputs and results are transformed once before storage;
 * existing values, conditional expected values, value queries and lookup defaults are already in the stored-value
 * domain and are not transformed again. Computation callbacks receive normalized keys and stored old values.
 * <p>
 * A null result from {@code compute*} or {@code merge} retains its removal/no-insertion meaning without being passed to
 * the value transformer. In contrast, {@code replaceAll} and {@link Entry#setValue(Object)} are ordinary writes: their
 * results pass through the transformer and remain subject to the backing map's null-value policy.
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 */
public abstract class TransMap<K, V> extends MapWrapper<K, V> {

    @Serial
    private static final long serialVersionUID = 2535880303653L;

    /**
     * Constructs a {@code TransMap} using a map factory to create the underlying map instance. The factory should
     * provide a new, empty map.
     *
     * @param mapFactory A {@link SupplierX} that creates an empty map for internal use.
     */
    public TransMap(final SupplierX<Map<K, V>> mapFactory) {
        super(mapFactory);
    }

    /**
     * Constructs a {@code TransMap} by wrapping a provided empty map instance. It is crucial that the provided map is
     * empty, as existing entries will not be transformed, which could lead to inconsistent behavior.
     *
     * @param emptyMap The map to be wrapped. Must be an empty map.
     */
    public TransMap(final Map<K, V> emptyMap) {
        super(emptyMap);
    }

    /**
     * Looks up the normalized key without transforming the stored result.
     *
     * @param key the lookup key before normalization
     * @return the stored value, or {@code null} when no value is associated
     */
    @Override
    public V get(final Object key) {
        return super.get(customKey(key));
    }

    /**
     * Normalizes the key and transforms the new value once before storing it.
     *
     * @param key   the key before normalization
     * @param value the new value before transformation
     * @return the previous stored value, or {@code null} if there was no mapping
     */
    @Override
    public V put(final K key, final V value) {
        return super.put(customKey(key), customValue(value));
    }

    /**
     * Stores each input mapping through {@link #put(Object, Object)} so that each key and new value is transformed.
     *
     * @param m the source mappings, whose values are in the input rather than stored-value domain
     * @throws NullPointerException if the source map is {@code null}
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    /**
     * Tests the backing map for the normalized key.
     *
     * @param key the key before normalization
     * @return whether a mapping exists for the normalized key
     */
    @Override
    public boolean containsKey(final Object key) {
        return super.containsKey(customKey(key));
    }

    /**
     * Removes the mapping for the normalized key.
     *
     * @param key the key before normalization
     * @return the previous stored value, or {@code null} if no value was associated
     */
    @Override
    public V remove(final Object key) {
        return super.remove(customKey(key));
    }

    /**
     * Removes a mapping only when its stored value equals the supplied expected value.
     *
     * @param key   the key before normalization
     * @param value the expected value in the stored-value domain; it is not transformed
     * @return whether the matching mapping was removed
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return super.remove(customKey(key), value);
    }

    /**
     * Replaces a matching mapping after normalizing the key and transforming only the new value.
     *
     * @param key      the key before normalization
     * @param oldValue the expected stored value, compared without transformation
     * @param newValue the replacement value before transformation
     * @return whether the matching mapping was replaced
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return super.replace(customKey(key), oldValue, customValue(newValue));
    }

    /**
     * Replaces a present mapping using the normalized key and transformed new value.
     *
     * @param key   the key before normalization
     * @param value the replacement value before transformation
     * @return the previous stored value, or {@code null} if no value was associated
     */
    @Override
    public V replace(final K key, final V value) {
        return super.replace(customKey(key), customValue(value));
    }

    /**
     * Looks up a normalized key, returning the caller's default unchanged when the key is absent.
     *
     * @param key          the key before normalization
     * @param defaultValue the fallback value, which is not transformed or stored
     * @return the stored value, or the unchanged fallback when the key is absent
     */
    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return super.getOrDefault(customKey(key), defaultValue);
    }

    /**
     * Computes a present non-null mapping using the normalized key and stored old value. A non-null callback result is
     * transformed before storage; a null callback result removes the mapping.
     *
     * @param key               the key before normalization
     * @param remappingFunction the function receiving the normalized key and stored old value
     * @return the resulting stored value, or {@code null} if no value remains
     * @throws NullPointerException if the remapping function is {@code null}
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return super.computeIfPresent(customKey(key), (k, current) -> {
            final V value = remappingFunction.apply(k, current);
            return value == null ? null : customValue(value);
        });
    }

    /**
     * Computes a mapping using the normalized key and stored old value. Only a non-null callback result is transformed;
     * null removes an existing mapping or leaves the key absent.
     *
     * @param key               the key before normalization
     * @param remappingFunction the function receiving the normalized key and stored old value, possibly null
     * @return the resulting stored value, or {@code null} if no value remains
     * @throws NullPointerException if the remapping function is {@code null}
     */
    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return super.compute(customKey(key), (k, current) -> {
            final V value = remappingFunction.apply(k, current);
            return value == null ? null : customValue(value);
        });
    }

    /**
     * Merges the incoming value with the stored old value using the backing map's computation implementation. The
     * callback receives the stored old value and original input; only the final non-null result is transformed. No
     * stronger atomicity or callback-execution guarantee is added beyond that provided by the backing map.
     *
     * @param key               the key before normalization
     * @param value             the non-null incoming value before transformation
     * @param remappingFunction the function combining the stored old value and original incoming value
     * @return the resulting stored value, or {@code null} if the callback removed the mapping
     * @throws NullPointerException if the value or remapping function is {@code null}
     */
    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return super.compute(customKey(key), (k, current) -> {
            final V result = current == null ? value : remappingFunction.apply(current, value);
            return result == null ? null : customValue(result);
        });
    }

    /**
     * Supplies a normalized key and transformed candidate value to the backing map's put-if-absent operation.
     *
     * @param key   the key before normalization
     * @param value the candidate value before transformation
     * @return the previous stored value, or {@code null} if no non-null value was associated
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        return super.putIfAbsent(customKey(key), customValue(value));
    }

    /**
     * Computes an absent value from the normalized key. Only a non-null callback result is transformed and considered
     * for storage; a null result preserves the no-insertion control flow.
     *
     * @param key             the key before normalization
     * @param mappingFunction the function receiving the normalized key
     * @return the existing or computed stored value, or {@code null} when no value is produced
     * @throws NullPointerException if the mapping function is {@code null}
     */
    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        return super.computeIfAbsent(customKey(key), k -> {
            final V value = mappingFunction.apply(k);
            return value == null ? null : customValue(value);
        });
    }

    /**
     * Applies a replacement to stored values and transforms each resulting value once. Unlike compute operations, null
     * is a write value here and is handled by the transformer and backing map rather than interpreted as removal.
     *
     * @param function replacement function receiving normalized keys and stored values
     * @throws NullPointerException if the function is {@code null}
     */
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function, "function");
        super.replaceAll((key, value) -> customValue(function.apply(key, value)));
    }

    /**
     * Returns a backed entry view whose value updates pass through the write transformation. Stored keys and expected
     * values are not transformed again; iterator and conditional bulk removals retain the backing view's behavior.
     *
     * @return a backed entry view preserving value-write transformation
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> entries = super.entrySet();
        return new AbstractSet<>() {

            /**
             * Creates an iterator that wraps backing entries so their value updates are transformed.
             *
             * @return a backed iterator retaining the underlying traversal and removal behavior
             */
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<K, V>> iterator = entries.iterator();
                return new Iterator<>() {

                    /**
                     * Tests whether the backing entry iterator has another mapping.
                     *
                     * @return whether another backing entry is available
                     */
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    /**
                     * Returns the next backing entry through a value-transforming entry wrapper.
                     *
                     * @return the next write-through entry
                     * @throws java.util.NoSuchElementException if the backing iterator is exhausted
                     */
                    @Override
                    public Entry<K, V> next() {
                        return transformedEntry(iterator.next());
                    }

                    /**
                     * Removes the last returned mapping through the backing iterator.
                     *
                     * @throws IllegalStateException         if the backing iterator has no removable last entry
                     * @throws UnsupportedOperationException if the backing iterator does not support removal
                     */
                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }

            /**
             * Returns the entry count reported by the backing entry view.
             *
             * @return the backing entry count
             */
            @Override
            public int size() {
                return entries.size();
            }

            /**
             * Tests the backing entry view for the supplied candidate without transforming its key or expected value.
             *
             * @param entry candidate mapping expressed in the stored key/value domain
             * @return whether the backing entry view contains the candidate
             */
            @Override
            public boolean contains(final Object entry) {
                return entries.contains(entry);
            }

            /**
             * Removes a matching backing entry without transforming its key or expected value.
             *
             * @param entry candidate mapping expressed in the stored key/value domain
             * @return whether a matching mapping was removed
             * @throws UnsupportedOperationException if the backing entry view does not support removal
             */
            @Override
            public boolean remove(final Object entry) {
                return entries.remove(entry);
            }

            /**
             * Removes all mappings through the backing entry view.
             *
             * @throws UnsupportedOperationException if the backing entry view does not support clearing
             */
            @Override
            public void clear() {
                entries.clear();
            }

            /**
             * Removes entries selected by a predicate that sees value-transforming entry wrappers. The backing entry
             * view performs the removal and retains its own consistency and conditional-removal behavior.
             *
             * @param filter predicate selecting entries to remove
             * @return whether the backing view removed at least one entry
             * @throws NullPointerException          if the predicate is null
             * @throws UnsupportedOperationException if removal is required but unsupported by the backing view
             */
            @Override
            public boolean removeIf(final Predicate<? super Entry<K, V>> filter) {
                Objects.requireNonNull(filter, "filter");
                return entries.removeIf(entry -> filter.test(transformedEntry(entry)));
            }
        };
    }

    /**
     * Wraps an existing entry without re-normalizing its already stored key.
     *
     * @param entry backing entry
     * @return entry that transforms only new values
     */
    private Entry<K, V> transformedEntry(final Entry<K, V> entry) {
        return new AbstractEntry<>() {

            /**
             * Returns the backing entry's already-normalized key without applying key conversion again.
             *
             * @return the key in the stored-key domain
             */
            @Override
            public K getKey() {
                return entry.getKey();
            }

            /**
             * Returns the backing entry's stored value without applying value conversion again.
             *
             * @return the value in the stored-value domain
             */
            @Override
            public V getValue() {
                return entry.getValue();
            }

            /**
             * Transforms the supplied replacement once and writes it through the backing entry.
             *
             * @param value the replacement value before transformation
             * @return the previous stored value reported by the backing entry
             * @throws UnsupportedOperationException if the backing entry does not support replacement
             * @throws NullPointerException          if the transformed value is null and the backing entry rejects null
             *                                       values
             */
            @Override
            public V setValue(final V value) {
                return entry.setValue(customValue(value));
            }
        };
    }

    /**
     * Transforms a key before it is used in any map operation. Subclasses must implement this method to define their
     * key conversion logic. The conversion must be stable and idempotent because stored keys can be presented again to
     * key-based operations. For example, a case-insensitive map can convert string keys to lowercase.
     *
     * @param key The original key.
     * @return The transformed key.
     */
    protected abstract K customKey(Object key);

    /**
     * Transforms a value before it is stored in the map. Subclasses must implement this method to define their value
     * conversion logic. For example, this method could trim whitespace from string values.
     *
     * @param value The original value.
     * @return The transformed value.
     */
    protected abstract V customValue(Object value);

}

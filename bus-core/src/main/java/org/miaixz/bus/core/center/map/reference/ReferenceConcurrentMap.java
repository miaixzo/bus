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
package org.miaixz.bus.core.center.map.reference;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.center.map.AbstractEntry;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.mutable.MutableBoolean;
import org.miaixz.bus.core.lang.mutable.MutableObject;
import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.xyz.ReferKit;

/**
 * An abstract, thread-safe {@link ConcurrentMap} implementation that uses Java's reference objects (e.g.,
 * {@link java.lang.ref.SoftReference}, {@link java.lang.ref.WeakReference}) for its keys and/or values. This class
 * provides a foundation for building caches that automatically evict entries based on memory pressure or reachability.
 * <p>
 * Subclasses must implement {@link #wrapKey(Object, ReferenceQueue)} and {@link #wrapValue(Object, ReferenceQueue)} to
 * define the specific type of reference (e.g., soft, weak, strong) used for keys and values. Null keys and stored
 * values are not supported; computation results use null only as the documented control signal. Views skip cleared
 * entries and retain observed pairs strongly while in use. Backing-entry counts remain approximate during collection or
 * delayed queue processing. Serialization supports the three built-in policies and restores a fresh default backing
 * store, without runtime listeners.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 */
public abstract class ReferenceConcurrentMap<K, V>
        implements ConcurrentMap<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2535929800001L;

    /**
     * The underlying {@link ConcurrentMap} that stores the wrapped keys and values.
     */
    transient ConcurrentMap<Ref<K>, Ref<V>> raw;

    /**
     * The {@link ReferenceQueue} for keys that have been garbage-collected.
     */
    private transient ReferenceQueue<K> lastKeyQueue;

    /**
     * The {@link ReferenceQueue} for values that have been garbage-collected.
     */
    private transient ReferenceQueue<V> lastValueQueue;

    /**
     * An optional listener that is invoked when a key or value is purged from the map due to garbage collection.
     */
    private transient volatile BiConsumer<Ref<? extends K>, Ref<? extends V>> purgeListener;

    /**
     * Constructs a new {@code ReferenceConcurrentMap} that wraps the given {@link ConcurrentMap}.
     *
     * @param raw The underlying {@link ConcurrentMap} to be wrapped. Must not be {@code null}.
     */
    public ReferenceConcurrentMap(final ConcurrentMap<Ref<K>, Ref<V>> raw) {
        this.raw = Objects.requireNonNull(raw, "raw");
        lastKeyQueue = new ReferenceQueue<>();
        lastValueQueue = new ReferenceQueue<>();
    }

    /**
     * Unwraps the actual object from its {@link Ref} wrapper.
     *
     * @param <T>    The type of the object.
     * @param object The {@link Ref} object to unwrap.
     * @return The actual object, or {@code null} if the reference has been cleared.
     */
    private static <T> T unwrap(final Ref<T> object) {
        return ReferKit.get(object);
    }

    /**
     * Sets a listener to be notified when entries are purged from the map due to garbage collection.
     *
     * @param purgeListener The {@link BiConsumer} to be called with the purged key and value references.
     */
    public void setPurgeListener(final BiConsumer<Ref<? extends K>, Ref<? extends V>> purgeListener) {
        this.purgeListener = purgeListener;
    }

    /**
     * Processes a bounded batch of queued references and returns the backing entry count. Cleared but unprocessed
     * entries can still contribute to this approximate count, even when views no longer expose their referents.
     *
     * @return The number of key-value mappings.
     */
    @Override
    public int size() {
        this.purgeStale();
        return this.raw.size();
    }

    /**
     * Processes a bounded batch of queued references and tests the backing map for emptiness. Unprocessed cleared
     * references can keep this result false even when a live-entry traversal is empty.
     *
     * @return {@code true} if this map is empty.
     */
    @Override
    public boolean isEmpty() {
        this.purgeStale();
        return this.raw.isEmpty();
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key. Stale entries are purged before retrieval.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value for the key, or {@code null} if the key is not found or its reference has been cleared.
     */
    @Override
    public V get(final Object key) {
        this.purgeStale();
        final Ref<K> referenceKey = lookupKey(key);
        final Ref<V> referenceValue = this.raw.get(referenceKey);
        final V value = unwrap(referenceValue);
        Reference.reachabilityFence(key);
        return value;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key. Stale entries are purged before
     * checking.
     *
     * @param key The key whose presence in this map is to be tested.
     * @return {@code true} if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value. Stale entries are purged before
     * checking.
     *
     * @param value The value whose presence in this map is to be tested.
     * @return {@code true} if this map maps one or more keys to the specified value.
     */
    @Override
    public boolean containsValue(final Object value) {
        Objects.requireNonNull(value, "value");
        for (Entry<K, V> entry : this) {
            if (Objects.equals(value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously contained a mapping for
     * the key, the old value is replaced. Stale entries are purged before insertion.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V put(final K key, final V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        this.purgeStale();
        try {
            return unwrap(this.raw.put(wrapKey(key), wrapValue(value)));
        } finally {
            Reference.reachabilityFence(key);
            Reference.reachabilityFence(value);
        }
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to {@code null}), associates it with
     * the given value and returns {@code null}, else returns the current value. Stale entries are purged before
     * operation.
     *
     * @param key   Key with which the specified value is to be associated.
     * @param value Value to be associated with the specified key.
     * @return The previous value associated with the specified key, or {@code null} if there was no mapping for the
     *         key.
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        Objects.requireNonNull(value, "value");
        final MutableObject<V> previous = new MutableObject<>();
        updateValue(key, (k, current) -> current == null ? value : current, previous);
        return previous.get();
    }

    /**
     * Copies all of the mappings from the specified map to this map. Stale entries are purged before operation.
     *
     * @param m Mappings to be stored in this map.
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    /**
     * Replaces the entry for the specified key only if currently mapped to some value. Stale entries are purged before
     * operation.
     *
     * @param key   The key with which the specified value is associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with the specified key, or {@code null} if there was no mapping for the
     *         key.
     */
    @Override
    public V replace(final K key, final V value) {
        Objects.requireNonNull(value, "value");
        final MutableObject<V> previous = new MutableObject<>();
        updateValue(key, (k, current) -> current == null ? null : value, previous);
        return previous.get();
    }

    /**
     * Replaces the entry for the specified key only if currently mapped to the {@code oldValue}. Stale entries are
     * purged before operation.
     *
     * @param key      The key with which the specified value is associated.
     * @param oldValue The value expected to be associated with the specified key.
     * @param newValue The value to be associated with the specified key.
     * @return {@code true} if the entry was replaced, {@code false} otherwise.
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        Objects.requireNonNull(oldValue, "oldValue");
        Objects.requireNonNull(newValue, "newValue");
        final MutableBoolean matched = new MutableBoolean();
        updateValue(key, (k, current) -> {
            final boolean matches = current != null && Objects.equals(current, oldValue);
            matched.set(matches);
            return matches ? newValue : current;
        }, null);
        return matched.get();
    }

    /**
     * Replaces each entry's value with the result of applying the given function to that entry, until all entries have
     * been processed or the function throws an exception. Stale entries are purged before operation.
     *
     * @param function The function to apply to each entry.
     */
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        this.purgeStale();
        Objects.requireNonNull(function, "function");
        for (Entry<K, V> entry : this) {
            updateValue(
                    entry.getKey(),
                    (key, value) -> value == null ? null
                            : Objects.requireNonNull(function.apply(key, value), "replacement"),
                    null);
        }
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to {@code null}), attempts to compute
     * its value using the given mapping function and enters it into this map unless {@code null}. Stale entries are
     * purged before operation.
     *
     * @param key             The key with which the specified value is to be associated.
     * @param mappingFunction The function to compute a value.
     * @return The current (existing or computed) value associated with the specified key, or {@code null} if the
     *         computed value is {@code null}.
     */
    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        final V current = get(key);
        if (current != null) {
            return current;
        }
        return updateValue(key, (k, value) -> value == null ? mappingFunction.apply(k) : value, null);
    }

    /**
     * If the value for the specified key is present and non-null, attempts to compute a new mapping given the key and
     * its current mapped value. Stale entries are purged before operation.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param remappingFunction The function to compute a replacement value.
     * @return The new value associated with the specified key, or {@code null} if no value is associated.
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return updateValue(key, (k, current) -> current == null ? null : remappingFunction.apply(k, current), null);
    }

    /**
     * Removes the mapping for a key from this map if it is present. Stale entries are purged before removal.
     *
     * @param key The key whose mapping is to be removed from the map.
     * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V remove(final Object key) {
        final Ref<K> lookup = lookupKey(key);
        this.purgeStale();
        try {
            return unwrap(this.raw.remove(lookup));
        } finally {
            Reference.reachabilityFence(key);
        }
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the specified value. Stale entries are
     * purged before removal.
     *
     * @param key   The key with which the specified value is associated.
     * @param value The value expected to be associated with the specified key.
     * @return {@code true} if the entry was removed, {@code false} otherwise.
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        Objects.requireNonNull(key, "key");
        if (value == null) {
            return false;
        }
        final MutableBoolean matched = new MutableBoolean();
        updateValue((K) key, (k, current) -> {
            final boolean matches = current != null && Objects.equals(current, value);
            matched.set(matches);
            return matches ? null : current;
        }, null);
        return matched.get();
    }

    /**
     * Removes the mappings from this map. Concurrent insertions follow the backing map's clear semantics. Queue
     * notifications are retained: draining them here could discard events belonging to concurrent new mappings.
     */
    @Override
    public void clear() {
        this.raw.clear();
    }

    /**
     * Returns a backed key view whose iterators omit cleared pairs. Traversal processes queued references when it
     * starts, retains each observed key strongly, and does not advertise an exact spliterator size.
     *
     * @return A set view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        return new AbstractSet<>() {

            /**
             * Creates a key projection of the live-entry traversal, processing a bounded queue batch at traversal
             * start. Iterator removal is by the last returned key and can remove a newer value for that same key.
             *
             * @return a weakly consistent iterator over strongly retained observed keys
             */
            @Override
            public Iterator<K> iterator() {
                return new TransIterator<>(entryIterator(), Entry::getKey);
            }

            /**
             * Creates a concurrent, distinct, non-null key traversal without advertising an exact size.
             *
             * @return an unknown-size spliterator over live keys
             */
            @Override
            public Spliterator<K> spliterator() {
                return Spliterators.spliteratorUnknownSize(
                        iterator(),
                        Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.DISTINCT);
            }

            /**
             * Returns the map's backing count after bounded queue processing, rather than counting live view elements.
             *
             * @return the approximate size, which can include cleared mappings not yet processed
             */
            @Override
            public int size() {
                return ReferenceConcurrentMap.this.size();
            }

            /**
             * Looks up the candidate key and tests whether its mapped value remains live.
             *
             * @param key non-null key to test
             * @return whether a live value is observed for the key
             * @throws NullPointerException if the key is null
             */
            @Override
            public boolean contains(Object key) {
                return containsKey(key);
            }

            /**
             * Removes the candidate key's mapping, including a mapping whose value was replaced after traversal.
             *
             * @param key non-null key whose mapping should be removed
             * @return whether the removed mapping had a live value
             * @throws NullPointerException if the key is null
             */
            @Override
            public boolean remove(Object key) {
                return ReferenceConcurrentMap.this.remove(key) != null;
            }

            /**
             * Clears the backing map through this view, retaining queued notifications for subsequent processing.
             */
            @Override
            public void clear() {
                ReferenceConcurrentMap.this.clear();
            }
        };
    }

    /**
     * Returns a backed value view whose traversals omit cleared key/value pairs. Queue processing occurs when a
     * traversal or count operation accesses the map, rather than when this view is created.
     *
     * @return A collection view of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        return new AbstractCollection<>() {

            /**
             * Creates a value projection of the live-entry traversal, processing queued references at traversal start.
             * Iterator removal is by the observed entry's key, not by its previously observed value.
             *
             * @return a weakly consistent iterator over live observed values
             */
            @Override
            public Iterator<V> iterator() {
                return new TransIterator<>(entryIterator(), Entry::getValue);
            }

            /**
             * Creates a concurrent, non-null value traversal without advertising an exact size or distinct values.
             *
             * @return an unknown-size spliterator over live values
             */
            @Override
            public Spliterator<V> spliterator() {
                return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.CONCURRENT | Spliterator.NONNULL);
            }

            /**
             * Returns the map's backing count after bounded queue processing, rather than counting live view elements.
             *
             * @return the approximate size, which can include cleared mappings not yet processed
             */
            @Override
            public int size() {
                return ReferenceConcurrentMap.this.size();
            }

            /**
             * Searches live entries for a value equal to the candidate.
             *
             * @param value non-null value to find
             * @return whether an equal live value is observed
             * @throws NullPointerException if the value is null
             */
            @Override
            public boolean contains(Object value) {
                return containsValue(value);
            }

            /**
             * Clears the backing map through this view, retaining queued notifications for subsequent processing.
             */
            @Override
            public void clear() {
                ReferenceConcurrentMap.this.clear();
            }

            /**
             * Removes the first observed equal value whose key still maps to that observed value.
             *
             * @param value value to match; null does not match any stored value
             * @return whether one matching mapping was conditionally removed
             */
            @Override
            public boolean remove(Object value) {
                return removeEntries(entry -> Objects.equals(value, entry.getValue()), true);
            }

            /**
             * Tests observed live values and conditionally removes matching key/value pairs. A different value
             * installed after observation is not removed by that conditional attempt.
             *
             * @param filter non-null predicate evaluated on live observed values
             * @return whether at least one observed mapping was removed
             * @throws NullPointerException if the predicate is null
             */
            @Override
            public boolean removeIf(Predicate<? super V> filter) {
                Objects.requireNonNull(filter, "filter");
                return removeEntries(entry -> filter.test(entry.getValue()), false);
            }
        };
    }

    /**
     * Returns a backed entry view whose traversals omit cleared key/value pairs. Queue processing occurs when a
     * traversal or count operation accesses the map, rather than when this view is created.
     *
     * @return A set view of the mappings contained in this map.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<>() {

            /**
             * Creates the live-entry traversal and processes a bounded queue batch before returning it. Iterator
             * removal is by the last returned key and is not conditional on its observed value.
             *
             * @return a weakly consistent iterator over strongly retained key/value pairs
             */
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return entryIterator();
            }

            /**
             * Creates a concurrent, distinct, non-null entry traversal without advertising an exact size.
             *
             * @return an unknown-size spliterator over live write-through entries
             */
            @Override
            public Spliterator<Entry<K, V>> spliterator() {
                return Spliterators.spliteratorUnknownSize(
                        iterator(),
                        Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.DISTINCT);
            }

            /**
             * Returns the map's backing count after bounded queue processing, rather than counting live view elements.
             *
             * @return the approximate size, which can include cleared mappings not yet processed
             */
            @Override
            public int size() {
                return ReferenceConcurrentMap.this.size();
            }

            /**
             * Clears the backing map through this view, retaining queued notifications for subsequent processing.
             */
            @Override
            public void clear() {
                ReferenceConcurrentMap.this.clear();
            }

            /**
             * Tests whether a non-null candidate key/value pair equals the key's current live mapping.
             *
             * @param candidate map entry to test; other objects and entries containing null do not match
             * @return whether an equal live mapping is observed
             */
            @Override
            public boolean contains(Object candidate) {
                return candidate instanceof Entry<?, ?> entry && entry.getKey() != null && entry.getValue() != null
                        && Objects.equals(get(entry.getKey()), entry.getValue());
            }

            /**
             * Conditionally removes a candidate entry only while its non-null key maps to an equal value.
             *
             * @param candidate map entry to remove; non-entry objects and null keys do not match
             * @return whether the matching live mapping was removed
             */
            @Override
            public boolean remove(Object candidate) {
                return candidate instanceof Entry<?, ?> entry && entry.getKey() != null
                        && ReferenceConcurrentMap.this.remove(entry.getKey(), entry.getValue());
            }

            /**
             * Tests observed live entries and conditionally removes each matching key/value pair.
             *
             * @param filter non-null predicate evaluated on observed write-through entries
             * @return whether at least one matching mapping was removed
             * @throws NullPointerException if the predicate is null
             */
            @Override
            public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
                return removeEntries(Objects.requireNonNull(filter, "filter"), false);
            }
        };
    }

    /**
     * Performs the given action for each entry in this map until all entries have been processed or the action throws
     * an exception. Stale entries are purged before iteration.
     *
     * @param action The action to be performed for each entry.
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        this.purgeStale();
        Objects.requireNonNull(action, "action");
        for (Entry<K, V> entry : this) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns an iterator over the entries in this map. Stale entries are purged before returning the iterator.
     *
     * @return An {@link Iterator} over {@link Map.Entry} objects.
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entrySet().iterator();
    }

    /**
     * Attempts to compute a mapping for the specified key and its current mapped value (or {@code null} if there is no
     * current mapping). Stale entries are purged before operation.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param remappingFunction The function to compute a value.
     * @return The new value associated with the specified key, or {@code null} if no value is associated.
     */
    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return updateValue(key, remappingFunction, null);
    }

    /**
     * If the specified key is not already associated with a value or is associated with {@code null}, associates it
     * with the given non-null value. Otherwise, replaces the associated value with the results of the given remapping
     * function. Stale entries are purged before operation.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param value             The value to be associated with the specified key.
     * @param remappingFunction The function to recompute a value if present.
     * @return The new value associated with the specified key, or {@code null} if no value is associated.
     */
    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(remappingFunction, "remappingFunction");
        return updateValue(
                key,
                (k, current) -> current == null ? value : remappingFunction.apply(current, value),
                null);
    }

    /**
     * Compares observed live mappings using the standard backing-count precondition. As with weak maps in general,
     * delayed reference processing can temporarily make the approximate size larger than the visible traversal.
     *
     * @param object map to compare
     * @return whether the observed live mappings have equal contents
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Map<?, ?> other)) {
            return false;
        }
        if (size() != other.size()) {
            return false;
        }
        int count = 0;
        try {
            for (Entry<K, V> entry : this) {
                if (!Objects.equals(entry.getValue(), other.get(entry.getKey()))) {
                    return false;
                }
                count++;
            }
            if (other instanceof ReferenceConcurrentMap<?, ?> referenceMap) {
                int otherCount = 0;
                for (Entry<?, ?> ignored : referenceMap) {
                    otherCount++;
                }
                return count == otherCount;
            }
            return count == other.size();
        } catch (ClassCastException | NullPointerException incompatible) {
            return false;
        }
    }

    /**
     * Returns the sum of standard live-entry hash codes.
     *
     * @return the sum of the hashes observed during a weakly consistent traversal
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (Entry<K, V> entry : this) {
            hash += entry.hashCode();
        }
        return hash;
    }

    /**
     * Removes observed matching values conditionally, without deleting a different concurrent replacement.
     *
     * @param filter    predicate applied to live observed entries
     * @param firstOnly whether to stop after the first successful conditional removal
     * @return whether at least one observed mapping was removed
     */
    private boolean removeEntries(final Predicate<? super Entry<K, V>> filter, final boolean firstOnly) {
        boolean changed = false;
        for (Entry<K, V> entry : this) {
            if (filter.test(entry) && remove(entry.getKey(), entry.getValue())) {
                changed = true;
                if (firstOnly) {
                    break;
                }
            }
        }
        return changed;
    }

    /**
     * Builds a weakly consistent traversal using the shared computational iterator. Lookahead pairs and the last
     * returned pair remain separate, so hasNext cannot change the target of iterator removal.
     *
     * @return iterator retaining only the necessary observed key/value pairs strongly
     */
    private Iterator<Entry<K, V>> entryIterator() {
        this.purgeStale();
        final Iterator<Entry<Ref<K>, Ref<V>>> references = raw.entrySet().iterator();
        return new ComputeIterator<>() {

            /**
             * Last returned pair, independent of ComputeIterator's lookahead.
             */
            private Entry<K, V> last;

            /**
             * Advances through reference wrappers until both key and value can be retained strongly.
             *
             * @return the next live write-through entry, or null when the reference traversal is exhausted
             */
            @Override
            protected Entry<K, V> computeNext() {
                while (references.hasNext()) {
                    final Entry<Ref<K>, Ref<V>> reference = references.next();
                    final K key = unwrap(reference.getKey());
                    final V value = unwrap(reference.getValue());
                    if (key != null && value != null) {
                        return new AbstractEntry<>() {

                            /**
                             * Observed value, updated when this entry writes through to the map.
                             */
                            private V current = value;

                            /**
                             * Returns the business key retained when this entry was observed.
                             *
                             * @return the strongly retained non-null key
                             */
                            @Override
                            public K getKey() {
                                return key;
                            }

                            /**
                             * Returns this entry's retained value, including any replacement written through this
                             * entry.
                             *
                             * @return the retained non-null value
                             */
                            @Override
                            public V getValue() {
                                return current;
                            }

                            /**
                             * Writes a replacement mapping and updates this entry's retained value after the write
                             * succeeds. The write can recreate a mapping removed since the entry was observed.
                             *
                             * @param replacement non-null value to associate with this entry's key
                             * @return the value previously retained by this entry
                             * @throws NullPointerException if the replacement is null
                             */
                            @Override
                            public V setValue(V replacement) {
                                ReferenceConcurrentMap.this.put(key, replacement);
                                final V previous = current;
                                current = replacement;
                                return previous;
                            }
                        };
                    }
                }
                return null;
            }

            /**
             * Returns the next live pair and records it independently from lookahead for subsequent removal.
             *
             * @return the next strongly retained write-through entry
             * @throws NoSuchElementException if no live pair remains
             */
            @Override
            public Entry<K, V> next() {
                last = super.next();
                return last;
            }

            /**
             * Removes the mapping for the last returned key, even if that key now has a different value. This is
             * key-based iterator removal, not conditional key/value removal.
             *
             * @throws IllegalStateException if no entry is eligible for removal or the last one was already removed
             */
            @Override
            public void remove() {
                if (last == null) {
                    throw new IllegalStateException();
                }
                ReferenceConcurrentMap.this.remove(last.getKey());
                last = null;
            }
        };
    }

    /**
     * Applies one logical update with the backing map's atomic compute semantics. Cleared values are absent. Strong
     * holders preserve the old and returned values through reference wrapping; null results are never wrapped.
     * Callbacks receive the caller's original key and must not modify this map. Custom backing maps retain their own
     * callback retry guarantees; this method does not introduce retries.
     *
     * @param key      original caller key
     * @param function logical update function
     * @param previous optional holder for the observed old value
     * @return the strongly retained computed value, or null
     */
    private V updateValue(
            final K key,
            final BiFunction<? super K, ? super V, ? extends V> function,
            final MutableObject<V> previous) {
        Objects.requireNonNull(key, "key");
        this.purgeStale();
        final MutableObject<V> result = new MutableObject<>();
        this.raw.compute(wrapKey(key), (referenceKey, referenceValue) -> {
            final V current = unwrap(referenceValue);
            if (previous != null) {
                previous.set(current);
            }
            final V value = function.apply(key, current);
            result.set(value);
            return value == null ? null : value == current ? referenceValue : wrapValue(value);
        });
        Reference.reachabilityFence(key);
        return result.get();
    }

    /**
     * Removes a mapping by canonical key-reference identity. A collected key permits removing its current value even
     * after replacement; a value-only event removes the mapping only if the stored value reference is still identical.
     * Wrapper equality is insufficient because distinct cleared references may compare equal. Notification belongs to
     * the caller.
     *
     * @param keyReference      actual key reference observed in the backing table
     * @param expectedReference exact value reference observed before the operation
     * @param keyCollected      whether the canonical key itself has been collected, irrespective of value replacement
     * @return the removed reference, or null if the entry changed or disappeared
     */
    private Ref<V> removeReference(
            final Ref<K> keyReference,
            final Ref<V> expectedReference,
            final boolean keyCollected) {
        // An operation-only probe: never installed in raw and never registered with a reference queue.
        final Ref<K> identityKey = new Ref<>() {

            /**
             * Reads the canonical key reference without adding another strong or queued reference wrapper.
             *
             * @return the canonical key's current referent, or null if it has been reclaimed
             */
            @Override
            public K get() {
                return keyReference.get();
            }

            /**
             * Reuses the canonical key reference's stable hash to select the same backing-map bucket.
             *
             * @return the canonical key wrapper's hash
             */
            @Override
            public int hashCode() {
                return keyReference.hashCode();
            }

            /**
             * Matches only this probe or the exact canonical key-reference object, never another equal referent.
             *
             * @param candidate object to compare with the identity probe
             * @return whether the candidate is this probe or its canonical key reference
             */
            @Override
            public boolean equals(Object candidate) {
                return candidate == this || candidate == keyReference;
            }
        };
        final MutableObject<Ref<V>> removed = new MutableObject<>();
        this.raw.computeIfPresent(identityKey, (key, current) -> {
            final boolean matches = keyCollected || current == expectedReference;
            removed.set(matches ? current : null);
            return matches ? null : current;
        });
        return removed.get();
    }

    /**
     * Consumes bounded batches of queue notifications, then matches reference identities in one table traversal.
     * Cleanup is completed before listeners run, so a listener failure cannot strand an already-consumed event. Queue
     * cleanup is an optimization; logical computations also handle references cleared without enqueue.
     */
    private void purgeStale() {
        Set<Ref<?>> stale = null;
        for (int index = 0; index < Normal._64; index++) {
            final Ref<?> key = (Ref<?>) this.lastKeyQueue.poll();
            final Ref<?> value = (Ref<?>) this.lastValueQueue.poll();
            if (key == null && value == null) {
                break;
            }
            if (stale == null) {
                stale = Collections.newSetFromMap(new IdentityHashMap<>());
            }
            if (key != null)
                stale.add(key);
            if (value != null)
                stale.add(value);
        }
        if (stale == null) {
            return;
        }
        final List<Entry<Ref<K>, Ref<V>>> removed = new ArrayList<>();
        for (Entry<Ref<K>, Ref<V>> entry : this.raw.entrySet()) {
            final Ref<K> key = entry.getKey();
            final Ref<V> value = entry.getValue();
            if (stale.contains(key) || stale.contains(value)) {
                final Ref<V> removedValue = removeReference(key, value, stale.contains(key));
                if (removedValue != null) {
                    removed.add(new AbstractMap.SimpleImmutableEntry<>(key, removedValue));
                }
            }
        }
        final BiConsumer<Ref<? extends K>, Ref<? extends V>> listener = this.purgeListener;
        if (listener != null) {
            removed.forEach(entry -> listener.accept(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Abstract method to wrap a key in a {@link Ref} implementation. Subclasses must implement this to specify the type
     * of reference (e.g., soft, weak).
     *
     * @param key   The key to wrap.
     * @param queue The {@link ReferenceQueue} to register the reference with.
     * @return A {@link Ref} containing the key.
     */
    abstract Ref<K> wrapKey(final K key, final ReferenceQueue<? super K> queue);

    /**
     * Abstract method to wrap a value in a {@link Ref} implementation. Subclasses must implement this to specify the
     * type of reference (e.g., soft, weak, strong).
     *
     * @param value The value to wrap.
     * @param queue The {@link ReferenceQueue} to register the reference with.
     * @return A {@link Ref} containing the value.
     */
    abstract Ref<V> wrapValue(final V value, final ReferenceQueue<? super V> queue);

    /**
     * Wraps the given key for internal storage.
     *
     * @param key The key to wrap.
     * @return A {@link Ref} containing the key.
     */
    private Ref<K> wrapKey(final Object key) {
        return wrapKey((K) Objects.requireNonNull(key, "key"), this.lastKeyQueue);
    }

    /**
     * Wraps the given value for internal storage.
     *
     * @param value The value to wrap.
     * @return A {@link Ref} containing the value.
     */
    private Ref<V> wrapValue(final Object value) {
        return wrapValue((V) Objects.requireNonNull(value, "value"), this.lastValueQueue);
    }

    /**
     * Creates a lookup-only wrapper without registering a temporary query reference with a collection queue.
     *
     * @param key caller's lookup key
     * @return unregistered reference wrapper
     */
    private Ref<K> lookupKey(final Object key) {
        return wrapKey((K) Objects.requireNonNull(key, "key"), null);
    }

    /**
     * Writes live logical entries without serializing queues, reference wrappers, or runtime listeners. Only built-in
     * policies with an ordinary ConcurrentHashMap store have a defined serialized form. A fresh default store is
     * restored; custom backing-store policies are rejected rather than silently discarded.
     *
     * @param output target stream
     * @throws IOException if contents or the reference policy cannot be serialized
     */
    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException {
        if ((getClass() != WeakConcurrentMap.class && getClass() != SoftConcurrentMap.class
                && getClass() != WeakKeyConcurrentMap.class) || raw.getClass() != ConcurrentHashMap.class) {
            throw new NotSerializableException("Custom reference policy or backing map: " + getClass().getName());
        }
        output.defaultWriteObject();
        for (Entry<K, V> entry : this) {
            output.writeBoolean(true);
            output.writeObject(entry.getKey());
            output.writeObject(entry.getValue());
        }
        output.writeBoolean(false);
    }

    /**
     * Restores logical entries into new queues and the built-in backing store. The stream is entry-delimited rather
     * than sized from the approximate backing count, and does not allocate a second full-map snapshot.
     *
     * @param input source stream
     * @throws IOException            if the serialized form is invalid
     * @throws ClassNotFoundException if a logical key or value class is unavailable
     */
    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        if (getClass() != WeakConcurrentMap.class && getClass() != SoftConcurrentMap.class
                && getClass() != WeakKeyConcurrentMap.class) {
            throw new InvalidObjectException("Unsupported reference policy");
        }
        input.defaultReadObject();
        raw = new ConcurrentHashMap<>();
        lastKeyQueue = new ReferenceQueue<>();
        lastValueQueue = new ReferenceQueue<>();
        while (input.readBoolean()) {
            final K key = (K) input.readObject();
            final V value = (V) input.readObject();
            if (key == null || value == null) {
                throw new InvalidObjectException("Null logical mapping");
            }
            put(key, value);
        }
    }

}

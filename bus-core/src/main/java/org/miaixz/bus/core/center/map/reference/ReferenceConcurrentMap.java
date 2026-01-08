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
package org.miaixz.bus.core.center.map.reference;

import java.io.Serial;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ReferKit;

/**
 * An abstract, thread-safe {@link ConcurrentMap} implementation that uses Java's reference objects (e.g.,
 * {@link java.lang.ref.SoftReference}, {@link java.lang.ref.WeakReference}) for its keys and/or values. This class
 * provides a foundation for building caches that automatically evict entries based on memory pressure or reachability.
 * <p>
 * Subclasses must implement {@link #wrapKey(Object, ReferenceQueue)} and {@link #wrapValue(Object, ReferenceQueue)} to
 * define the specific type of reference (e.g., soft, weak, strong) used for keys and values.
 * 
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ReferenceConcurrentMap<K, V>
        implements ConcurrentMap<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852269377051L;

    /**
     * The underlying {@link ConcurrentMap} that stores the wrapped keys and values.
     */
    final ConcurrentMap<Ref<K>, Ref<V>> raw;
    /**
     * The {@link ReferenceQueue} for keys that have been garbage-collected.
     */
    private final ReferenceQueue<K> lastKeyQueue;
    /**
     * The {@link ReferenceQueue} for values that have been garbage-collected.
     */
    private final ReferenceQueue<V> lastValueQueue;
    /**
     * An optional listener that is invoked when a key or value is purged from the map due to garbage collection.
     */
    private BiConsumer<Ref<? extends K>, Ref<? extends V>> purgeListener;

    /**
     * Constructs a new {@code ReferenceConcurrentMap} that wraps the given {@link ConcurrentMap}.
     *
     * @param raw The underlying {@link ConcurrentMap} to be wrapped. Must not be {@code null}.
     */
    public ReferenceConcurrentMap(final ConcurrentMap<Ref<K>, Ref<V>> raw) {
        this.raw = raw;
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
     * Returns the number of key-value mappings in this map. Stale entries are purged before counting.
     *
     * @return The number of key-value mappings.
     */
    @Override
    public int size() {
        this.purgeStale();
        return this.raw.size();
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings. Stale entries are purged before checking.
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
        return unwrap(this.raw.get(wrapKey(key)));
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
        this.purgeStale();
        return this.raw.containsKey(wrapKey(key));
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
        this.purgeStale();
        return this.raw.containsValue(wrapValue(value));
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
        this.purgeStale();
        final Ref<V> vReference = this.raw.put(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
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
        this.purgeStale();
        final Ref<V> vReference = this.raw.putIfAbsent(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
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
        this.purgeStale();
        final Ref<V> vReference = this.raw.replace(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
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
        this.purgeStale();
        return this.raw.replace(wrapKey(key), wrapValue(oldValue), wrapValue(newValue));
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
        this.raw.replaceAll((rKey, rValue) -> wrapValue(function.apply(unwrap(rKey), unwrap(rValue))));
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
        V result = null;
        while (null == result) {
            this.purgeStale();
            final Ref<V> vReference = this.raw
                    .computeIfAbsent(wrapKey(key), kReference -> wrapValue(mappingFunction.apply(unwrap(kReference))));

            // If vReference is collected by GC at this point, unwrap will return null, requiring re-computation.
            // However, if the user-provided value is null, it should be returned directly.
            if (NullRef.NULL == vReference) {
                // User provided null value
                return null;
            }
            result = unwrap(vReference);
        }
        return result;
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
        V result = null;
        while (null == result) {
            this.purgeStale();
            final Ref<V> vReference = this.raw.computeIfPresent(
                    wrapKey(key),
                    (kReference, vReference1) -> wrapValue(
                            remappingFunction.apply(unwrap(kReference), unwrap(vReference1))));

            // If vReference is collected by GC at this point, unwrap will return null, requiring re-computation.
            // However, if the user-provided value is null, it should be returned directly.
            if (NullRef.NULL == vReference) {
                // User provided null value
                return null;
            }
            result = unwrap(vReference);
        }
        return result;
    }

    /**
     * Removes the mapping for a key from this map if it is present. Stale entries are purged before removal.
     *
     * @param key The key whose mapping is to be removed from the map.
     * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V remove(final Object key) {
        this.purgeStale();
        return unwrap(this.raw.remove(wrapKey(key)));
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
        this.purgeStale();
        return this.raw.remove(wrapKey((K) key, null), value);
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns. Also clears the
     * reference queues.
     */
    @Override
    public void clear() {
        this.raw.clear();
        while (lastKeyQueue.poll() != null)
            ;
        while (lastValueQueue.poll() != null)
            ;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. Stale entries are purged before returning the set.
     *
     * @return A set view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        this.purgeStale();
        final Set<Ref<K>> referenceSet = this.raw.keySet();
        return new AbstractSet<K>() {

            /**
             * Returns an iterator over elements of type T.
             *
             * @return an Iterator
             */
            @Override
            public Iterator<K> iterator() {
                final Iterator<Ref<K>> referenceIter = referenceSet.iterator();
                return new Iterator<K>() {

                    /**
                     * Returns true if the iteration has more elements.
                     *
                     * @return true if the iteration has more elements
                     */
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    /**
                     * Returns the next element in the iteration.
                     *
                     * @return the next element
                     */
                    @Override
                    public K next() {
                        return unwrap(referenceIter.next());
                    }
                };
            }

            /**
             * Returns the number of elements in this collection.
             *
             * @return the number of elements
             */
            @Override
            public int size() {
                return referenceSet.size();
            }
        };
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. Stale entries are purged before returning
     * the collection.
     *
     * @return A collection view of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        this.purgeStale();
        final Collection<Ref<V>> referenceValues = this.raw.values();
        return new AbstractCollection<>() {

            /**
             * Returns an iterator over elements of type T.
             *
             * @return an Iterator
             */
            @Override
            public Iterator<V> iterator() {
                final Iterator<Ref<V>> referenceIter = referenceValues.iterator();
                return new Iterator<>() {

                    /**
                     * Returns true if the iteration has more elements.
                     *
                     * @return true if the iteration has more elements
                     */
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    /**
                     * Returns the next element in the iteration.
                     *
                     * @return the next element
                     */
                    @Override
                    public V next() {
                        return unwrap(referenceIter.next());
                    }
                };
            }

            /**
             * Returns the number of elements in this collection.
             *
             * @return the number of elements
             */
            @Override
            public int size() {
                return referenceValues.size();
            }
        };
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. Stale entries are purged before returning the
     * set.
     *
     * @return A set view of the mappings contained in this map.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        this.purgeStale();
        final Set<Entry<Ref<K>, Ref<V>>> referenceEntrySet = this.raw.entrySet();
        return new AbstractSet<>() {

            /**
             * Returns an iterator over elements of type T.
             *
             * @return an Iterator
             */
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<Ref<K>, Ref<V>>> referenceIter = referenceEntrySet.iterator();
                return new Iterator<>() {

                    /**
                     * Returns true if the iteration has more elements.
                     *
                     * @return true if the iteration has more elements
                     */
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    /**
                     * Returns the next element in the iteration.
                     *
                     * @return the next element
                     */
                    @Override
                    public Entry<K, V> next() {
                        final Entry<Ref<K>, Ref<V>> next = referenceIter.next();
                        return new Entry<>() {

                            /**
                             * Gets the key of this entry.
                             *
                             * @return the key
                             */
                            @Override
                            public K getKey() {
                                return unwrap(next.getKey());
                            }

                            /**
                             * Gets the value of this entry.
                             *
                             * @return the value
                             */
                            @Override
                            public V getValue() {
                                return unwrap(next.getValue());
                            }

                            /**
                             * Sets the value of this entry.
                             *
                             * @param value the new value
                             * @return the old value
                             */
                            @Override
                            public V setValue(final V value) {
                                return unwrap(next.setValue(wrapValue(value)));
                            }
                        };
                    }
                };
            }

            /**
             * Returns the number of elements in this collection.
             *
             * @return the number of elements
             */
            @Override
            public int size() {
                return referenceEntrySet.size();
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
        this.raw.forEach((key, rValue) -> action.accept(key.get(), unwrap(rValue)));
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
        this.purgeStale();
        return unwrap(
                this.raw.compute(
                        wrapKey(key),
                        (kReference, vReference) -> wrapValue(
                                remappingFunction.apply(unwrap(kReference), unwrap(vReference)))));
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
        this.purgeStale();
        return unwrap(
                this.raw.merge(
                        wrapKey(key),
                        wrapValue(value),
                        (vReference, vReference2) -> wrapValue(
                                remappingFunction.apply(unwrap(vReference), unwrap(vReference2)))));
    }

    /**
     * Purges stale entries from the map by processing the reference queues. This method is called before most map
     * operations to ensure consistency.
     */
    private void purgeStale() {
        Ref<? extends K> key;
        Ref<? extends V> value;

        // Purge entries whose keys have been garbage-collected
        while ((key = (Ref<? extends K>) this.lastKeyQueue.poll()) != null) {
            value = this.raw.remove(key);
            if (null != purgeListener) {
                purgeListener.accept(key, value);
            }
        }

        // Purge entries whose values have been garbage-collected
        while ((value = (Ref<? extends V>) this.lastValueQueue.poll()) != null) {
            MapKit.removeByValue(this.raw, (Ref<V>) value);
            if (null != purgeListener) {
                purgeListener.accept(null, value);
            }
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
        return wrapKey((K) key, this.lastKeyQueue);
    }

    /**
     * Wraps the given value for internal storage.
     *
     * @param value The value to wrap.
     * @return A {@link Ref} containing the value.
     */
    private Ref<V> wrapValue(final Object value) {
        return wrapValue((V) value, this.lastValueQueue);
    }

    /**
     * A special {@link Ref} implementation for representing {@code null} values. This is used internally to distinguish
     * between a key/value that was explicitly mapped to {@code null} and a key/value that has been garbage-collected.
     */
    private static class NullRef implements Ref {

        public static final Object NULL = new NullRef();

        /**
         * Returns the value to which the specified key is mapped, or null if this cache contains no mapping for the
         * key.
         *
         * @param key the key whose associated value is to be returned
         * @return the value associated with the key, or null if no mapping
         */
        @Override
        public Object get() {
            return null;
        }

    }

}

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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.cache.CacheListener;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * A cache implementation that weakly references business keys and cache entries.
 * <p>
 * The presence of a mapping for a given key will not prevent the key from being reclaimed by the garbage collector.
 * Keys are the caller's objects, not the temporary Mutable wrappers used by the parent cache API. Cache entries may
 * also be collected independently: an external reference to a value alone does not retain its CacheObject.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 */
public class WeakCache<K, V> extends TimedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2535633306695L;

    /**
     * Constructs a weak-referenced cache with a specified timeout.
     *
     * @param timeout The timeout in milliseconds. A value of -1 or 0 means no timeout.
     */
    public WeakCache(final long timeout) {
        super(timeout, new BusinessKeyMap<>());
        final BusinessKeyMap<K, CacheObject<K, V>> map = (BusinessKeyMap<K, CacheObject<K, V>>) cacheMap;
        map.delegate.setPurgeListener((key, value) -> {
            final CacheObject<K, V> cached = value == null ? null : value.get();
            onRemove(key == null ? null : key.get(), cached == null ? null : cached.getValue());
        });
    }

    /**
     * Sets the current cache listener. Collection events resolve this listener when they are delivered, so replacing or
     * disabling a listener does not leave an obsolete callback attached to the backing storage.
     * <p>
     * Collected-entry notifications are delivered when later map operations process reference queues, not by the
     * garbage collector itself. Cleared keys or values are reported as {@code null} to
     * {@link CacheListener#onRemove(Object, Object)}; the listener must tolerate either referent already being gone.
     *
     * @param listener The listener for cache events.
     * @return This {@link WeakCache} instance.
     */
    @Override
    public WeakCache<K, V> setListener(final CacheListener<K, V> listener) {
        super.setListener(listener);

        return this;
    }

    /**
     * Clears live entries through the normal listener path, then discards any already-cleared storage wrappers.
     */
    @Override
    public void clear() {
        super.clear();
        cacheMap.clear();
    }

    /**
     * Private adapter preserving the parent's Mutable-key API while references target actual business keys. It
     * maintains no second key index and does not change the delegate's weak-key/weak-value policy.
     *
     * @param <K> business key type
     * @param <V> stored entry type
     */
    private static final class BusinessKeyMap<K, V> extends AbstractMap<Mutable<K>, V>
            implements ConcurrentMap<Mutable<K>, V> {

        /**
         * Actual weak storage; Mutable wrappers are created only for API-facing views.
         */
        private final WeakConcurrentMap<K, V> delegate = new WeakConcurrentMap<>();

        /**
         * Extracts a business key without retaining a temporary key wrapper in storage.
         *
         * @param key non-null Mutable key supplied through the parent cache API
         * @return the wrapped business key, which must be non-null for delegate operations
         * @throws NullPointerException if {@code key} is {@code null}
         * @throws ClassCastException   if {@code key} is not a Mutable instance
         */
        private K businessKey(final Object key) {
            return ((Mutable<K>) Objects.requireNonNull(key, "key")).get();
        }

        /**
         * Looks up the delegate using the business key extracted from the supplied wrapper.
         *
         * @param key Mutable wrapper around the business key
         * @return the live stored value, or null if absent or reclaimed
         * @throws NullPointerException if the wrapper or its business key is null
         * @throws ClassCastException   if the supplied key is not a Mutable wrapper
         */
        @Override
        public V get(final Object key) {
            return delegate.get(businessKey(key));
        }

        /**
         * Tests whether the extracted business key currently has a live stored value.
         *
         * @param key Mutable wrapper around the business key
         * @return whether a live mapping is observed
         * @throws NullPointerException if the wrapper or its business key is null
         * @throws ClassCastException   if the supplied key is not a Mutable wrapper
         */
        @Override
        public boolean containsKey(final Object key) {
            return delegate.containsKey(businessKey(key));
        }

        /**
         * Stores a non-null value under the wrapped business key, without retaining the Mutable wrapper.
         *
         * @param key   wrapper around the non-null business key
         * @param value non-null value held weakly by the delegate
         * @return the previous live value, or null if none is observed
         * @throws NullPointerException if the wrapper, business key, or value is null
         */
        @Override
        public V put(final Mutable<K> key, final V value) {
            return delegate.put(businessKey(key), value);
        }

        /**
         * Removes the delegate mapping for the wrapped business key.
         *
         * @param key Mutable wrapper around the business key
         * @return the removed live value, or null if no live value is observed
         * @throws NullPointerException if the wrapper or its business key is null
         * @throws ClassCastException   if the supplied key is not a Mutable wrapper
         */
        @Override
        public V remove(final Object key) {
            return delegate.remove(businessKey(key));
        }

        /**
         * Atomically installs a value when the business-key mapping has no live value.
         *
         * @param key   wrapper around the non-null business key
         * @param value non-null value to install
         * @return the existing live value, or null when no previous live value exists
         * @throws NullPointerException if the wrapper, business key, or value is null
         */
        @Override
        public V putIfAbsent(final Mutable<K> key, final V value) {
            return delegate.putIfAbsent(businessKey(key), value);
        }

        /**
         * Removes a business-key mapping only when its live value equals the expected value.
         *
         * @param key   Mutable wrapper around the business key
         * @param value expected value; null never matches a stored value
         * @return whether the matching mapping was removed
         * @throws NullPointerException if the wrapper or its business key is null
         * @throws ClassCastException   if the supplied key is not a Mutable wrapper
         */
        @Override
        public boolean remove(final Object key, final Object value) {
            return delegate.remove(businessKey(key), value);
        }

        /**
         * Atomically replaces a live business-key mapping without creating a mapping for an absent value.
         *
         * @param key   wrapper around the non-null business key
         * @param value non-null replacement value
         * @return the previous live value, or null if no live mapping exists
         * @throws NullPointerException if the wrapper, business key, or value is null
         */
        @Override
        public V replace(final Mutable<K> key, final V value) {
            return delegate.replace(businessKey(key), value);
        }

        /**
         * Atomically replaces a business-key mapping only when its live value equals the expected value.
         *
         * @param key      wrapper around the non-null business key
         * @param oldValue non-null expected value
         * @param newValue non-null replacement value
         * @return whether the expected live mapping was replaced
         * @throws NullPointerException if the wrapper, business key, or either value is null
         */
        @Override
        public boolean replace(final Mutable<K> key, final V oldValue, final V newValue) {
            return delegate.replace(businessKey(key), oldValue, newValue);
        }

        /**
         * Returns the delegate's approximate mapping count after its bounded stale-reference processing.
         *
         * @return the backing count, which may temporarily include already-cleared mappings
         */
        @Override
        public int size() {
            return delegate.size();
        }

        /**
         * Clears all delegate mappings, including wrappers whose referents have already been reclaimed.
         */
        @Override
        public void clear() {
            delegate.clear();
        }

        /**
         * Returns the delegate's backed value view without creating a second collection of stored values.
         *
         * @return a view whose traversals omit reclaimed key/value pairs
         */
        @Override
        public Collection<V> values() {
            return delegate.values();
        }

        /**
         * Returns a backed key view that creates Mutable wrappers only for exposed business keys.
         *
         * @return the live key view, with an approximate size and no exact spliterator size
         */
        @Override
        public Set<Mutable<K>> keySet() {
            return new AbstractSet<>() {

                /**
                 * Creates a weakly consistent traversal of live business keys exposed through fresh Mutable wrappers.
                 *
                 * @return the delegate-backed key iterator
                 */
                @Override
                public Iterator<Mutable<K>> iterator() {
                    final Iterator<K> keys = delegate.keySet().iterator();
                    return new Iterator<>() {

                        /**
                         * Reports whether the delegate key iterator has another live key.
                         *
                         * @return whether another key is available
                         */
                        @Override
                        public boolean hasNext() {
                            return keys.hasNext();
                        }

                        /**
                         * Wraps and returns the next live business key without installing the wrapper in storage.
                         *
                         * @return a fresh Mutable wrapper around the next business key
                         * @throws NoSuchElementException if the key traversal is exhausted
                         */
                        @Override
                        public Mutable<K> next() {
                            return MutableObject.of(keys.next());
                        }

                        /**
                         * Delegates removal of the last returned key to the backing key iterator; removal is by key,
                         * not by the value previously observed.
                         *
                         * @throws IllegalStateException if no key is currently eligible for iterator removal
                         */
                        @Override
                        public void remove() {
                            keys.remove();
                        }
                    };
                }

                /**
                 * Returns the delegate's approximate mapping count, not an exact count of currently live keys.
                 *
                 * @return the backing count after bounded stale-reference processing
                 */
                @Override
                public int size() {
                    return delegate.size();
                }

                /**
                 * Tests whether a Mutable key wrapper refers to a currently live business-key mapping.
                 *
                 * @param key candidate wrapper; other object types do not match
                 * @return whether the wrapped business key has a live mapping
                 * @throws NullPointerException if a Mutable candidate contains a null business key
                 */
                @Override
                public boolean contains(final Object key) {
                    return key instanceof Mutable<?> && BusinessKeyMap.this.containsKey(key);
                }

                /**
                 * Removes the mapping for a candidate Mutable wrapper's business key.
                 *
                 * @param key candidate wrapper; other object types do not match
                 * @return whether removal observed a live stored value
                 * @throws NullPointerException if a Mutable candidate contains a null business key
                 */
                @Override
                public boolean remove(final Object key) {
                    return key instanceof Mutable<?> && BusinessKeyMap.this.remove(key) != null;
                }

                /**
                 * Clears every mapping from the underlying weak store through this key view.
                 */
                @Override
                public void clear() {
                    delegate.clear();
                }

                /**
                 * Creates a concurrent, distinct, non-null key traversal without claiming an exact size.
                 *
                 * @return an unknown-size spliterator over API-facing key wrappers
                 */
                @Override
                public Spliterator<Mutable<K>> spliterator() {
                    return Spliterators.spliteratorUnknownSize(
                            iterator(),
                            Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.CONCURRENT);
                }
            };
        }

        /**
         * Returns a backed entry view that exposes temporary Mutable key wrappers and live value snapshots.
         *
         * @return the write-through entry view, with approximate size and conditional iterator removal
         */
        @Override
        public Set<Map.Entry<Mutable<K>, V>> entrySet() {
            return new AbstractSet<>() {

                /**
                 * Creates a weakly consistent entry traversal with separate lookahead and last-returned state.
                 *
                 * @return an iterator that retains only observed live pairs and removes them conditionally
                 */
                @Override
                public Iterator<Map.Entry<Mutable<K>, V>> iterator() {
                    final Iterator<Map.Entry<K, V>> entries = delegate.entrySet().iterator();
                    return new Iterator<>() {

                        /**
                         * Strong lookahead pair, excluding already-cleared delegate referents.
                         */
                        private Map.Entry<Mutable<K>, V> next;
                        /**
                         * Last returned pair retained separately from lookahead for conditional removal.
                         */
                        private Map.Entry<Mutable<K>, V> last;

                        /**
                         * Finds the next live key/value pair while retaining it strongly until it is returned.
                         * Lookahead does not change the last-returned entry used for removal.
                         *
                         * @return whether another live pair is available
                         */
                        @Override
                        public boolean hasNext() {
                            while (next == null && entries.hasNext()) {
                                final Map.Entry<K, V> entry = entries.next();
                                final K key = entry.getKey();
                                final V value = entry.getValue();
                                if (key != null && value != null) {
                                    next = new SimpleEntry<>(MutableObject.of(key), value) {

                                        /**
                                         * Writes a replacement to the delegate and then updates this entry's retained
                                         * value.
                                         *
                                         * @param replacement non-null replacement value
                                         * @return the value previously retained by this entry
                                         * @throws NullPointerException if the replacement is null
                                         */
                                        @Override
                                        public V setValue(final V replacement) {
                                            delegate.put(key, replacement);
                                            return super.setValue(replacement);
                                        }
                                    };
                                }
                            }
                            return next != null;
                        }

                        /**
                         * Returns the retained live pair and records it as the target of a subsequent iterator removal.
                         *
                         * @return the next write-through entry
                         * @throws NoSuchElementException if no live pair remains
                         */
                        @Override
                        public Map.Entry<Mutable<K>, V> next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            last = next;
                            next = null;
                            return last;
                        }

                        /**
                         * Removes the last returned mapping only if its business key still maps to the entry's retained
                         * value. A lookahead call does not change that removal target.
                         *
                         * @throws IllegalStateException if next has not succeeded or the last entry was already removed
                         */
                        @Override
                        public void remove() {
                            if (last == null) {
                                throw new IllegalStateException();
                            }
                            delegate.remove(last.getKey().get(), last.getValue());
                            last = null;
                        }
                    };
                }

                /**
                 * Returns the delegate's approximate mapping count, which can exceed the number of live entries.
                 *
                 * @return the backing count after bounded stale-reference processing
                 */
                @Override
                public int size() {
                    return delegate.size();
                }

                /**
                 * Creates a concurrent, distinct, non-null entry traversal without claiming an exact size.
                 *
                 * @return an unknown-size spliterator over live write-through entries
                 */
                @Override
                public Spliterator<Map.Entry<Mutable<K>, V>> spliterator() {
                    return Spliterators.spliteratorUnknownSize(
                            iterator(),
                            Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.CONCURRENT);
                }

                /**
                 * Tests whether a candidate entry's Mutable business key currently maps to an equal live value.
                 *
                 * @param candidate entry to test; non-entry and non-Mutable-key objects do not match
                 * @return whether an equal live mapping is observed
                 * @throws NullPointerException if a Mutable candidate key contains a null business key
                 */
                @Override
                public boolean contains(final Object candidate) {
                    if (!(candidate instanceof Map.Entry<?, ?> entry) || !(entry.getKey() instanceof Mutable<?>)) {
                        return false;
                    }
                    final V value = get(entry.getKey());
                    return value != null && Objects.equals(value, entry.getValue());
                }

                /**
                 * Conditionally removes a mapping equal to the candidate entry's business key and value.
                 *
                 * @param candidate entry to remove; non-entry and non-Mutable-key objects do not match
                 * @return whether the matching live mapping was removed
                 * @throws NullPointerException if a Mutable candidate key contains a null business key
                 */
                @Override
                public boolean remove(final Object candidate) {
                    return candidate instanceof Map.Entry<?, ?> entry && entry.getKey() instanceof Mutable<?>
                            && BusinessKeyMap.this.remove(entry.getKey(), entry.getValue());
                }

                /**
                 * Clears every mapping from the underlying weak store through this entry view.
                 */
                @Override
                public void clear() {
                    delegate.clear();
                }
            };
        }
    }

}

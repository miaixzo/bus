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
package org.miaixz.bus.core.center.map.concurrent;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.IteratorKit;

/**
 * Provides a collection of common {@link Weigher} and {@link EntryWeigher} implementations. These weighers are useful
 * for configuring size-bounded caches, allowing capacity to be measured by factors other than just the number of
 * entries.
 *
 * @author Kimi Liu
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">ConcurrentLinkedHashMap Project</a>
 * @since Java 17+
 */
public final class Weighers {

    /**
     * Constructs a new Weighers. Utility class constructor for static access.
     */
    private Weighers() {
        throw new AssertionError("No " + Weighers.class.getName() + " instances for you!");
    }

    /**
     * Returns an {@link EntryWeigher} that delegates to the specified {@link Weigher}. The weight of an entry is
     * determined solely by the weight of its value.
     *
     * @param weigher The {@link Weigher} to be wrapped.
     * @param <K>     The type of keys in the map entry.
     * @param <V>     The type of values in the map entry.
     * @return An {@link EntryWeigher} that uses the provided value weigher.
     */
    public static <K, V> EntryWeigher<K, V> asEntryWeigher(final Weigher<? super V> weigher) {
        return (weigher == singleton()) ? Weighers.entrySingleton() : new EntryWeigherView<>(weigher);
    }

    /**
     * Returns an {@link EntryWeigher} where each entry has a weight of <b>1</b>. A map bounded with this weigher will
     * evict entries when the number of key-value pairs exceeds the capacity.
     *
     * @param <K> The type of keys in the map entry.
     * @param <V> The type of values in the map entry.
     * @return An {@link EntryWeigher} where each entry contributes one unit to the total weight.
     */
    public static <K, V> EntryWeigher<K, V> entrySingleton() {
        return (EntryWeigher<K, V>) SingletonEntryWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where each value has a weight of <b>1</b>. A map bounded with this weigher will evict
     * entries when the number of values exceeds the capacity.
     *
     * @param <V> The type of the value.
     * @return A {@link Weigher} where each value contributes one unit to the total weight.
     */
    public static <V> Weigher<V> singleton() {
        return (Weigher<V>) SingletonWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is a byte array and its weight is the number of bytes in the array. A
     * map bounded with this weigher will evict entries when the total byte size exceeds the capacity.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @return A {@link Weigher} where each byte array's weight is its length.
     */
    public static Weigher<byte[]> byteArray() {
        return ByteArrayWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is an {@link Iterable} and its weight is the number of elements. This
     * weigher should only be used when the {@link #collection()} weigher is not applicable, as evaluation takes O(n)
     * time for non-{@code Collection} iterables.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @param <E> The type of elements in the {@link Iterable}.
     * @return A {@link Weigher} where each {@link Iterable}'s weight is its size.
     */
    public static <E> Weigher<? super Iterable<E>> iterable() {
        return IterableWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is a {@link Collection} and its weight is the number of elements. A map
     * bounded with this weigher will evict entries when the total number of elements across all values exceeds the
     * capacity.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @param <E> The type of elements in the {@link Collection}.
     * @return A {@link Weigher} where each {@link Collection}'s weight is its size.
     */
    public static <E> Weigher<? super Collection<E>> collection() {
        return CollectionWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is a {@link List} and its weight is the number of elements. A map
     * bounded with this weigher will evict entries when the total number of elements across all values exceeds the
     * capacity.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @param <E> The type of elements in the {@link List}.
     * @return A {@link Weigher} where each {@link List}'s weight is its size.
     */
    public static <E> Weigher<? super List<E>> list() {
        return ListWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is a {@link Set} and its weight is the number of elements. A map
     * bounded with this weigher will evict entries when the total number of elements across all values exceeds the
     * capacity.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @param <E> The type of elements in the {@link Set}.
     * @return A {@link Weigher} where each {@link Set}'s weight is its size.
     */
    public static <E> Weigher<? super Set<E>> set() {
        return SetWeigher.INSTANCE;
    }

    /**
     * Returns a {@link Weigher} where the value is a {@link Map} and its weight is the number of entries. A map bounded
     * with this weigher will evict entries when the total number of entries across all values exceeds the capacity.
     * <p>
     * A value with a weight of <b>0</b> will be rejected by the map. If a value with this weight can occur, the caller
     * should handle it as a removal operation. Alternatively, a custom weigher may be specified to assign an empty
     * value a positive weight.
     *
     * @param <K> The type of keys in the inner map.
     * @param <V> The type of values in the inner map.
     * @return A {@link Weigher} where each {@link Map}'s weight is its size.
     */
    public static <K, V> Weigher<? super Map<K, V>> map() {
        return MapWeigher.INSTANCE;
    }

    /**
     * An {@link EntryWeigher} implementation that assigns a weight of 1 to every entry.
     */
    enum SingletonEntryWeigher implements EntryWeigher<Object, Object> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the entry. Always returns {@code 1}.
         *
         * @param key   The key (not used in weight calculation).
         * @param value The value (not used in weight calculation).
         * @return {@code 1} as the weight.
         */
        @Override
        public int weightOf(final Object key, final Object value) {
            return 1;
        }
    }

    /**
     * A {@link Weigher} implementation that assigns a weight of 1 to every value.
     */
    enum SingletonWeigher implements Weigher<Object> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the value. Always returns {@code 1}.
         *
         * @param value The value (not used in weight calculation).
         * @return {@code 1} as the weight.
         */
        @Override
        public int weightOf(final Object value) {
            return 1;
        }
    }

    /**
     * A {@link Weigher} implementation for byte arrays, where the weight is the length of the array.
     */
    enum ByteArrayWeigher implements Weigher<byte[]> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the byte array value.
         *
         * @param value The byte array whose length determines the weight.
         * @return The length of the byte array.
         */
        @Override
        public int weightOf(final byte[] value) {
            return value.length;
        }
    }

    /**
     * A {@link Weigher} implementation for {@link Iterable} objects, where the weight is the number of elements.
     */
    enum IterableWeigher implements Weigher<Iterable<?>> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the iterable value (number of elements).
         *
         * @param values The iterable whose size determines the weight.
         * @return The number of elements in the iterable.
         */
        @Override
        public int weightOf(final Iterable<?> values) {
            if (values instanceof Collection<?>) {
                return ((Collection<?>) values).size();
            }
            return IteratorKit.size(values);
        }
    }

    /**
     * A {@link Weigher} implementation for {@link Collection} objects, where the weight is the number of elements.
     */
    enum CollectionWeigher implements Weigher<Collection<?>> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the collection value (number of elements).
         *
         * @param values The collection whose size determines the weight.
         * @return The number of elements in the collection.
         */
        @Override
        public int weightOf(final Collection<?> values) {
            return values.size();
        }
    }

    /**
     * A {@link Weigher} implementation for {@link List} objects, where the weight is the number of elements.
     */
    enum ListWeigher implements Weigher<List<?>> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the list value (number of elements).
         *
         * @param values The list whose size determines the weight.
         * @return The number of elements in the list.
         */
        @Override
        public int weightOf(final List<?> values) {
            return values.size();
        }
    }

    /**
     * A {@link Weigher} implementation for {@link Set} objects, where the weight is the number of elements.
     */
    enum SetWeigher implements Weigher<Set<?>> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the set value (number of elements).
         *
         * @param values The set whose size determines the weight.
         * @return The number of elements in the set.
         */
        @Override
        public int weightOf(final Set<?> values) {
            return values.size();
        }
    }

    /**
     * A {@link Weigher} implementation for {@link Map} objects, where the weight is the number of entries.
     */
    enum MapWeigher implements Weigher<Map<?, ?>> {

        /** Singleton instance. */
        INSTANCE;

        /**
         * Returns the weight of the map value (number of entries).
         *
         * @param values The map whose size determines the weight.
         * @return The number of entries in the map.
         */
        @Override
        public int weightOf(final Map<?, ?> values) {
            return values.size();
        }
    }

    /**
     * An {@link EntryWeigher} that wraps a {@link Weigher} for values.
     *
     * @param <K> The type of keys.
     * @param <V> The type of values.
     */
    static final class EntryWeigherView<K, V> implements EntryWeigher<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 2852276671792L;

        final Weigher<? super V> weigher;

        EntryWeigherView(final Weigher<? super V> weigher) {
            Assert.notNull(weigher, "Weigher must not be null");
            this.weigher = weigher;
        }

        /**
         * Returns the weight of the map entry based on the wrapped weigher.
         *
         * @param key   The key (not used in weight calculation).
         * @param value The value whose weight is determined by the wrapped weigher.
         * @return The weight of the value as determined by the wrapped weigher.
         */
        @Override
        public int weightOf(final K key, final V value) {
            return weigher.weightOf(value);
        }
    }

}

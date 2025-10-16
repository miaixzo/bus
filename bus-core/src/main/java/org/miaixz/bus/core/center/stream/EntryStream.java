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
package org.miaixz.bus.core.center.stream;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.map.multiple.RowKeyTable;
import org.miaixz.bus.core.center.map.multiple.Table;
import org.miaixz.bus.core.center.set.ConcurrentHashSet;
import org.miaixz.bus.core.xyz.CollectorKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An enhanced stream implementation specialized for key-value pairs, {@link Map.Entry}, inspired by StreamEx's
 * EntryStream and vavr's Map. It can be viewed as a {@link Stream} whose elements are of type {@link Map.Entry}, used
 * to support stream processing of data from {@link Map} collections or other key-value pair types.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntryStream<K, V> extends EnhancedWrappedStream<Map.Entry<K, V>, EntryStream<K, V>> {

    /**
     * The default empty key-value pair.
     */
    private static final Map.Entry<?, ?> EMPTY_ENTRY = new AbstractMap.SimpleImmutableEntry<>(null, null);

    /**
     * Constructs an {@code EntryStream} from a given {@link Stream} of {@link Map.Entry} objects.
     *
     * @param stream the {@link Stream} of {@link Map.Entry} objects to wrap
     */
    EntryStream(final Stream<Map.Entry<K, V>> stream) {
        super(stream);
    }

    /**
     * Creates an {@code EntryStream} by merging a collection of keys and a collection of values. If a key or value is
     * not found at a corresponding index in the other collection, {@code null} will be used as a placeholder. For
     * example, merging {@code [1, 2, 3]} and {@code [1, 2]} will result in {@code [{1=1}, {2=2}, {3=null}]}.
     *
     * @param <K>    the type of keys
     * @param <V>    the type of values
     * @param keys   the collection of keys
     * @param values the collection of values
     * @return an {@code EntryStream} instance
     */
    public static <K, V> EntryStream<K, V> merge(final Iterable<K> keys, final Iterable<V> values) {
        final boolean hasKeys = ObjectKit.isNotNull(keys);
        final boolean hasValues = ObjectKit.isNotNull(values);
        // Both empty
        if (!hasKeys && !hasValues) {
            return empty();
        }
        // Values empty
        if (hasKeys && !hasValues) {
            return of(keys, Function.identity(), k -> null);
        }
        // Keys empty
        if (!hasKeys) {
            return of(values, v -> null, Function.identity());
        }
        // Both not empty
        final List<Map.Entry<K, V>> entries = new ArrayList<>();
        final Iterator<K> keyItr = keys.iterator();
        final Iterator<V> valueItr = values.iterator();
        while (keyItr.hasNext() || valueItr.hasNext()) {
            entries.add(ofEntry(keyItr.hasNext() ? keyItr.next() : null, valueItr.hasNext() ? valueItr.next() : null));
        }
        return of(entries);
    }

    /**
     * Creates a sequential {@code EntryStream} from the key-value pairs of a {@link Map}. Operations on the stream will
     * not affect the input {@code map} instance itself.
     *
     * @param map the {@link Map} to create the stream from
     * @param <K> the type of keys
     * @param <V> the type of values
     * @return an {@code EntryStream} instance
     */
    public static <K, V> EntryStream<K, V> of(final Map<K, V> map) {
        return ObjectKit.isNull(map) ? empty() : of(map.entrySet());
    }

    /**
     * Creates a sequential {@code EntryStream} from an {@link Iterable} of {@link Map.Entry} objects. Operations on the
     * stream will not affect the input {@code entries} instance itself. If the input stream contains {@code null}
     * elements, they will be mapped to key-value pairs where both key and value are {@code null}.
     *
     * @param entries the {@link Iterable} of {@link Map.Entry} objects
     * @param <K>     the type of keys
     * @param <V>     the type of values
     * @return an {@code EntryStream} instance
     */
    public static <K, V> EntryStream<K, V> of(final Iterable<? extends Map.Entry<K, V>> entries) {
        return ObjectKit.isNull(entries) ? empty() : of(StreamSupport.stream(entries.spliterator(), false));
    }

    /**
     * Creates a sequential {@code EntryStream} from a {@link Collection}.
     *
     * @param source      the source collection
     * @param keyMapper   the function to map elements to keys
     * @param valueMapper the function to map elements to values
     * @param <T>         the type of elements in the source collection
     * @param <K>         the type of keys
     * @param <V>         the type of values
     * @return an {@code EntryStream} instance
     */
    public static <T, K, V> EntryStream<K, V> of(
            final Iterable<T> source,
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        if (ObjectKit.isNull(source)) {
            return empty();
        }
        final Stream<Map.Entry<K, V>> stream = StreamSupport.stream(source.spliterator(), false)
                .map(t -> ofEntry(keyMapper.apply(t), valueMapper.apply(t)));
        return new EntryStream<>(stream);
    }

    /**
     * Wraps an existing {@link Stream} of {@link Map.Entry} objects into an {@code EntryStream}. If the input stream is
     * {@code null}, an empty sequential stream is returned. If the input stream contains {@code null} elements, they
     * will be mapped to key-value pairs where both key and value are {@code null}.
     *
     * @param stream the {@link Stream} to wrap
     * @param <K>    the type of keys
     * @param <V>    the type of values
     * @return an {@code EntryStream} instance
     */
    public static <K, V> EntryStream<K, V> of(final Stream<? extends Map.Entry<K, V>> stream) {
        return ObjectKit.isNull(stream) ? empty() : new EntryStream<>(stream.map(EntryStream::ofEntry));
    }

    /**
     * Creates an empty sequential {@code EntryStream}.
     *
     * @param <K> the type of keys
     * @param <V> the type of values
     * @return an empty {@code EntryStream} instance
     */
    public static <K, V> EntryStream<K, V> empty() {
        return new EntryStream<>(Stream.empty());
    }

    /**
     * Converts a {@link Map.Entry} to an {@link AbstractMap.SimpleImmutableEntry}.
     *
     * @param entry the {@link Map.Entry} to convert
     * @param <K>   the type of keys
     * @param <V>   the type of values
     * @return an {@link AbstractMap.SimpleImmutableEntry}
     */
    static <K, V> Map.Entry<K, V> ofEntry(final Map.Entry<K, V> entry) {
        return ObjectKit.defaultIfNull(entry, e -> ofEntry(e.getKey(), e.getValue()), (Map.Entry<K, V>) EMPTY_ENTRY);
    }

    /**
     * Creates an {@link AbstractMap.SimpleImmutableEntry} from a key and a value.
     *
     * @param key   the key
     * @param value the value
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return an {@link AbstractMap.SimpleImmutableEntry}
     */
    static <K, V> Map.Entry<K, V> ofEntry(final K key, final V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Returns a {@link BinaryOperator} that throws an {@link IllegalStateException} if a duplicate key is encountered
     * during a merge operation.
     *
     * @param <T> the type of the elements
     * @return a {@link BinaryOperator} for throwing exceptions on duplicate keys
     */
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate data %s", u));
        };
    }

    /**
     * Returns an {@code EntryStream} consisting of the distinct key-value pairs based on their keys. If duplicate keys
     * are found, the later occurrences are discarded.
     *
     * @return an {@code EntryStream} instance with distinct keys
     */
    public EntryStream<K, V> distinctByKey() {
        // FIXME fix happen NPE when has null data
        final Set<K> accessed = new ConcurrentHashSet<>(16);
        return wrap(stream.filter(e -> {
            final K key = e.getKey();
            if (accessed.contains(key)) {
                return false;
            }
            accessed.add(key);
            return true;
        }));
    }

    /**
     * Returns an {@code EntryStream} consisting of the distinct key-value pairs based on their values. If duplicate
     * values are found, the later occurrences are discarded.
     *
     * @return an {@code EntryStream} instance with distinct values
     */
    public EntryStream<K, V> distinctByValue() {
        // FIXME fix happen NPE when has null value
        final Set<V> accessed = new ConcurrentHashSet<>(16);
        return wrap(stream.filter(e -> {
            final V val = e.getValue();
            if (accessed.contains(val)) {
                return false;
            }
            accessed.add(val);
            return true;
        }));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs that match the given predicate.
     *
     * @param filter a {@link BiPredicate} to apply to each key-value pair to determine if it should be included
     * @return an {@code EntryStream} instance containing only the matching key-value pairs
     */
    public EntryStream<K, V> filter(final BiPredicate<? super K, ? super V> filter) {
        Objects.requireNonNull(filter);
        return super.filter(e -> filter.test(e.getKey(), e.getValue()));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs whose keys match the given predicate.
     *
     * @param filter a {@link Predicate} to apply to each key to determine if its corresponding key-value pair should be
     *               included
     * @return an {@code EntryStream} instance containing only the key-value pairs with matching keys
     */
    public EntryStream<K, V> filterByKey(final Predicate<? super K> filter) {
        Objects.requireNonNull(filter);
        return super.filter(e -> filter.test(e.getKey()));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs whose values match the given predicate.
     *
     * @param filter a {@link Predicate} to apply to each value to determine if its corresponding key-value pair should
     *               be included
     * @return an {@code EntryStream} instance containing only the key-value pairs with matching values
     */
    public EntryStream<K, V> filterByValue(final Predicate<? super V> filter) {
        Objects.requireNonNull(filter);
        return super.filter(e -> filter.test(e.getValue()));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs where neither the entry itself, nor its key, nor
     * its value is {@code null}.
     *
     * @return an {@code EntryStream} instance with non-null entries, keys, and values
     */
    public EntryStream<K, V> nonNullKeyValue() {
        return super.filter(
                e -> ObjectKit.isNotNull(e) && ObjectKit.isNotNull(e.getKey()) && ObjectKit.isNotNull(e.getValue()));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs where neither the entry itself nor its key is
     * {@code null}.
     *
     * @return an {@code EntryStream} instance with non-null entries and keys
     */
    public EntryStream<K, V> nonNullKey() {
        return super.filter(e -> ObjectKit.isNotNull(e) && ObjectKit.isNotNull(e.getKey()));
    }

    /**
     * Returns an {@code EntryStream} consisting of the key-value pairs where neither the entry itself nor its value is
     * {@code null}.
     *
     * @return an {@code EntryStream} instance with non-null entries and values
     */
    public EntryStream<K, V> nonNullValue() {
        return super.filter(e -> ObjectKit.isNotNull(e) && ObjectKit.isNotNull(e.getValue()));
    }

    /**
     * Performs the given action on each key of the key-value pairs in the stream. This is an intermediate operation.
     *
     * @param consumer the {@link Consumer} to apply to each key
     * @return this {@code EntryStream} instance
     */
    public EntryStream<K, V> peekKey(final Consumer<? super K> consumer) {
        Objects.requireNonNull(consumer);
        return super.peek(e -> consumer.accept(e.getKey()));
    }

    /**
     * Performs the given action on each value of the key-value pairs in the stream. This is an intermediate operation.
     *
     * @param consumer the {@link Consumer} to apply to each value
     * @return this {@code EntryStream} instance
     */
    public EntryStream<K, V> peekValue(final Consumer<? super V> consumer) {
        Objects.requireNonNull(consumer);
        return super.peek(e -> consumer.accept(e.getValue()));
    }

    /**
     * Returns a sorted {@code EntryStream} by the keys of its key-value pairs.
     *
     * @param comparator the {@link Comparator} to use for comparing keys
     * @return a sorted {@code EntryStream} instance
     */
    public EntryStream<K, V> sortByKey(final Comparator<? super K> comparator) {
        Objects.requireNonNull(comparator);
        return sorted(Map.Entry.comparingByKey(comparator));
    }

    /**
     * Returns a sorted {@code EntryStream} by the values of its key-value pairs.
     *
     * @param comparator the {@link Comparator} to use for comparing values
     * @return a sorted {@code EntryStream} instance
     */
    public EntryStream<K, V> sortByValue(final Comparator<? super V> comparator) {
        Objects.requireNonNull(comparator);
        return sorted(Map.Entry.comparingByValue(comparator));
    }

    /**
     * Appends a new key-value pair to the end of the current stream.
     *
     * @param key   the key of the new entry
     * @param value the value of the new entry
     * @return a new {@code EntryStream} instance with the appended entry
     */
    public EntryStream<K, V> push(final K key, final V value) {
        return wrap(Stream.concat(stream, Stream.of(ofEntry(key, value))));
    }

    /**
     * Prepends a new key-value pair to the beginning of the current stream.
     *
     * @param key   the key of the new entry
     * @param value the value of the new entry
     * @return a new {@code EntryStream} instance with the prepended entry
     */
    public EntryStream<K, V> unshift(final K key, final V value) {
        return wrap(Stream.concat(Stream.of(ofEntry(key, value)), stream));
    }

    /**
     * Appends the elements from the given {@link Iterable} of {@link Map.Entry} objects to the end of the current
     * stream.
     *
     * @param entries the {@link Iterable} of {@link Map.Entry} objects to append
     * @return a new {@code EntryStream} instance with the appended elements
     */
    @Override
    public EntryStream<K, V> append(final Iterable<? extends Map.Entry<K, V>> entries) {
        if (IteratorKit.isEmpty(entries)) {
            return this;
        }
        final Stream<Map.Entry<K, V>> contacted = StreamSupport.stream(entries.spliterator(), isParallel())
                .map(EntryStream::ofEntry);
        return wrap(Stream.concat(stream, contacted));
    }

    /**
     * Prepends the elements from the given {@link Iterable} of {@link Map.Entry} objects to the beginning of the
     * current stream.
     *
     * @param entries the {@link Iterable} of {@link Map.Entry} objects to prepend
     * @return a new {@code EntryStream} instance with the prepended elements
     */
    @Override
    public EntryStream<K, V> prepend(final Iterable<? extends Map.Entry<K, V>> entries) {
        if (IteratorKit.isEmpty(entries)) {
            return this;
        }
        final Stream<Map.Entry<K, V>> contacted = StreamSupport.stream(entries.spliterator(), isParallel())
                .map(EntryStream::ofEntry);
        return wrap(Stream.concat(contacted, stream));
    }

    /**
     * Converts this {@code EntryStream} into an {@link EasyStream} of its values.
     *
     * @return an {@link EasyStream} of values
     */
    public EasyStream<V> toValueStream() {
        return EasyStream.of(stream.map(Map.Entry::getValue));
    }

    /**
     * Converts this {@code EntryStream} into an {@link EasyStream} of its keys.
     *
     * @return an {@link EasyStream} of keys
     */
    public EasyStream<K> toKeyStream() {
        return EasyStream.of(stream.map(Map.Entry::getKey));
    }

    /**
     * Maps the keys of the key-value pairs in this stream to a new type.
     *
     * @param mapper a function to apply to each key to produce a new key
     * @param <N>    the type of the new keys
     * @return a new {@code EntryStream} with the mapped keys
     */
    public <N> EntryStream<N, V> mapKeys(final Function<? super K, ? extends N> mapper) {
        Objects.requireNonNull(mapper);
        return new EntryStream<>(stream.map(e -> ofEntry(mapper.apply(e.getKey()), e.getValue())));
    }

    /**
     * Maps the values of the key-value pairs in this stream to a new type.
     *
     * @param mapper a function to apply to each value to produce a new value
     * @param <N>    the type of the new values
     * @return a new {@code EntryStream} with the mapped values
     */
    public <N> EntryStream<K, N> mapValues(final Function<? super V, ? extends N> mapper) {
        Objects.requireNonNull(mapper);
        return new EntryStream<>(stream.map(e -> ofEntry(e.getKey(), mapper.apply(e.getValue()))));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of applying the given function to the elements of this
     * stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a new element
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    @Override
    public <R> EasyStream<R> map(final Function<? super Map.Entry<K, V>, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return EasyStream.of(stream.map(mapper));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of applying the given {@link BiFunction} to the key and
     * value of each element in this stream.
     *
     * @param mapper a {@link BiFunction} to apply to each key-value pair to produce a new element
     * @param <N>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    public <N> EasyStream<N> map(final BiFunction<? super K, ? super V, ? extends N> mapper) {
        Objects.requireNonNull(mapper);
        return EasyStream.of(stream.map(e -> mapper.apply(e.getKey(), e.getValue())));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of replacing each element of this stream with the
     * contents of a mapped stream produced by applying the provided mapping function to each element. Each mapped
     * stream is closed after its contents have been placed into this stream. This is a stateless intermediate
     * operation.
     * <p>
     * For example, to combine the IDs and parent IDs of all users in a list into a new stream:
     * 
     * <pre>{@code
     * 
     * FastStream<Long> ids = FastStream.of(users).flatMap(user -> FastStream.of(user.getId(), user.getParentId()));
     * }</pre>
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces a stream of new values
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    @Override
    public <R> EasyStream<R> flatMap(final Function<? super Map.Entry<K, V>, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return EasyStream.of(stream.flatMap(mapper));
    }

    /**
     * Returns an {@code EntryStream} consisting of the results of replacing each key of this stream with the contents
     * of a mapped stream produced by applying the provided mapping function to each key. The original values are
     * retained for each new key.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * // unwrap = [{a = 1}, {b = 2}, {c = 3}]
     * unwrap.flatMapKey(data -> Stream.of(data + "1", data + "2"));
     * // unwrap = [{a1 = 1}, {a2 = 1}, {b1 = 2}, {b2 = 2}, {c1 = 3}, {c2 = 3}]
     * }</pre>
     *
     * @param keyMapper a function to apply to each key which produces a stream of new keys
     * @param <N>       the type of the new keys
     * @return the new {@code EntryStream}
     */
    public <N> EntryStream<N, V> flatMapKey(final Function<? super K, Stream<? extends N>> keyMapper) {
        Objects.requireNonNull(keyMapper);
        return new EntryStream<>(
                stream.flatMap(e -> keyMapper.apply(e.getKey()).map(newKey -> ofEntry(newKey, e.getValue()))));
    }

    /**
     * Returns an {@code EntryStream} consisting of the results of replacing each value of this stream with the contents
     * of a mapped stream produced by applying the provided mapping function to each value. The original keys are
     * retained for each new value.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * // unwrap = [{a = 1}, {b = 2}, {c = 3}]
     * unwrap.flatMapValue(num -> Stream.of(num, num + 1));
     * // unwrap = [{a = 1}, {a = 2}, {b = 2}, {b = 3}, {c = 3}, {c = 4}]
     * }</pre>
     *
     * @param valueMapper a function to apply to each value which produces a stream of new values
     * @param <N>         the type of the new values
     * @return the new {@code EntryStream}
     */
    public <N> EntryStream<K, N> flatMapValue(final Function<? super V, Stream<? extends N>> valueMapper) {
        Objects.requireNonNull(valueMapper);
        return new EntryStream<>(
                stream.flatMap(e -> valueMapper.apply(e.getValue()).map(newVal -> ofEntry(e.getKey(), newVal))));
    }

    /**
     * Collects the elements of this stream into a {@link Map} using the provided factory and merge operator.
     *
     * @param mapFactory a {@link Supplier} that returns a new, empty {@link Map} of the appropriate type
     * @param operator   a {@link BinaryOperator} used to resolve collisions between values associated with the same key
     * @return a {@link Map} containing the collected elements
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public Map<K, V> toMap(final Supplier<Map<K, V>> mapFactory, final BinaryOperator<V> operator) {
        Objects.requireNonNull(mapFactory);
        Objects.requireNonNull(operator);
        return super.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, operator, mapFactory));
    }

    /**
     * Collects the elements of this stream into a {@link Map} using the provided factory. If duplicate keys are
     * encountered, the later value overwrites the earlier one.
     *
     * @param mapFactory a {@link Supplier} that returns a new, empty {@link Map} of the appropriate type
     * @return a {@link Map} containing the collected elements
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public Map<K, V> toMap(final Supplier<Map<K, V>> mapFactory) {
        Objects.requireNonNull(mapFactory);
        return super.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (t1, t2) -> t2, mapFactory));
    }

    /**
     * Collects the elements of this stream into a {@link HashMap}.
     *
     * @return a {@link HashMap} containing the collected elements
     * @throws IllegalArgumentException if duplicate keys are encountered
     * @see Collectors#toMap(Function, Function)
     */
    public Map<K, V> toMap() {
        return super.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Groups the key-value pairs into a two-dimensional {@link Map} and returns a {@link Table}.
     *
     * @param rowKeyMapper  a {@link BiFunction} to map keys and values to the row key of the parent map
     * @param colMapFactory a {@link Supplier} that returns a new, empty {@link Map} for the inner map
     * @param operator      a {@link BinaryOperator} used to resolve collisions between values associated with the same
     *                      key in the inner map
     * @param <N>           the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     * @see Collectors#groupingBy(Function, Supplier, Collector)
     */
    public <N> Table<N, K, V> toTable(
            final BiFunction<? super K, ? super V, ? extends N> rowKeyMapper,
            final Supplier<Map<K, V>> colMapFactory,
            final BinaryOperator<V> operator) {
        Objects.requireNonNull(rowKeyMapper);
        Objects.requireNonNull(colMapFactory);
        Objects.requireNonNull(operator);
        final Map<N, Map<K, V>> rawMap = collect(
                Collectors.groupingBy(
                        e -> rowKeyMapper.apply(e.getKey(), e.getValue()),
                        HashMap::new,
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, operator, colMapFactory)));
        return new RowKeyTable<>(rawMap, colMapFactory::get);
    }

    /**
     * Groups the key-value pairs into a two-dimensional {@link HashMap} and returns a {@link Table}.
     *
     * @param rowKeyMapper a {@link BiFunction} to map keys and values to the row key of the parent map
     * @param <N>          the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     * @throws IllegalArgumentException if duplicate keys are encountered in either the parent or inner maps
     */
    public <N> Table<N, K, V> toTable(final BiFunction<? super K, ? super V, ? extends N> rowKeyMapper) {
        return toTable(rowKeyMapper, HashMap::new, throwingMerger());
    }

    /**
     * Groups the key-value pairs by key into a two-dimensional {@link Map} and returns a {@link Table}.
     *
     * @param rowKeyMapper  a {@link Function} to map keys to the row key of the parent map
     * @param colMapFactory a {@link Supplier} that returns a new, empty {@link Map} for the inner map
     * @param operator      a {@link BinaryOperator} used to resolve collisions between values associated with the same
     *                      key in the inner map
     * @param <N>           the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     */
    public <N> Table<N, K, V> toTableByKey(
            final Function<? super K, ? extends N> rowKeyMapper,
            final Supplier<Map<K, V>> colMapFactory,
            final BinaryOperator<V> operator) {
        return toTable((k, v) -> rowKeyMapper.apply(k), colMapFactory, operator);
    }

    /**
     * Groups the key-value pairs by key into a two-dimensional {@link HashMap} and returns a {@link Table}.
     *
     * @param rowKeyMapper a {@link Function} to map keys to the row key of the parent map
     * @param <N>          the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     * @throws IllegalArgumentException if duplicate keys are encountered in either the parent or inner maps
     */
    public <N> Table<N, K, V> toTableByKey(final Function<? super K, ? extends N> rowKeyMapper) {
        return toTable((k, v) -> rowKeyMapper.apply(k));
    }

    /**
     * Groups the key-value pairs by value into a two-dimensional {@link Map} and returns a {@link Table}.
     *
     * @param rowKeyMapper  a {@link Function} to map values to the row key of the parent map
     * @param colMapFactory a {@link Supplier} that returns a new, empty {@link Map} for the inner map
     * @param operator      a {@link BinaryOperator} used to resolve collisions between values associated with the same
     *                      key in the inner map
     * @param <N>           the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     */
    public <N> Table<N, K, V> toTableByValue(
            final Function<? super V, ? extends N> rowKeyMapper,
            final Supplier<Map<K, V>> colMapFactory,
            final BinaryOperator<V> operator) {
        return toTable((k, v) -> rowKeyMapper.apply(v), colMapFactory, operator);
    }

    /**
     * Groups the key-value pairs by value into a two-dimensional {@link HashMap} and returns a {@link Table}.
     *
     * @param rowKeyMapper a {@link Function} to map values to the row key of the parent map
     * @param <N>          the type of the row keys in the parent map
     * @return a {@link Table} containing the grouped elements
     * @throws IllegalArgumentException if duplicate keys are encountered in either the parent or inner maps
     */
    public <N> Table<N, K, V> toTableByValue(final Function<? super V, ? extends N> rowKeyMapper) {
        return toTable((k, v) -> rowKeyMapper.apply(v));
    }

    /**
     * Groups the key-value pairs by key, collecting values into a {@link List}.
     *
     * @return a {@link Map} where keys are the original keys and values are lists of corresponding values
     */
    public Map<K, List<V>> groupByKey() {
        return groupByKey(Collectors.toList());
    }

    /**
     * Groups the key-value pairs by key, collecting values using the provided {@link Collector}.
     *
     * @param collector a {@link Collector} to accumulate the values for each key
     * @param <C>       the type of the collection holding the values
     * @return a {@link Map} where keys are the original keys and values are collections of corresponding values
     */
    public <C extends Collection<V>> Map<K, C> groupByKey(final Collector<V, ?, C> collector) {
        return groupByKey((Supplier<Map<K, C>>) HashMap::new, collector);
    }

    /**
     * Groups the key-value pairs by key, collecting values using the provided {@link Collector} and {@link Map}
     * factory.
     *
     * @param mapFactory a {@link Supplier} that returns a new, empty {@link Map} of the appropriate type
     * @param collector  a {@link Collector} to accumulate the values for each key
     * @param <C>        the type of the collection holding the values
     * @param <M>        the type of the resulting map
     * @return a {@link Map} where keys are the original keys and values are collections of corresponding values
     */
    public <C extends Collection<V>, M extends Map<K, C>> M groupByKey(
            final Supplier<M> mapFactory,
            final Collector<V, ?, C> collector) {
        return super.collect(
                Collectors.groupingBy(
                        Map.Entry::getKey,
                        mapFactory,
                        CollectorKit.transform(
                                ArrayList::new,
                                s -> s.stream().map(Map.Entry::getValue).collect(collector))));
    }

    /**
     * Performs the given action for each key-value pair in the stream.
     *
     * @param consumer the {@link BiConsumer} to apply to each key-value pair
     */
    public void forEach(final BiConsumer<K, V> consumer) {
        Objects.requireNonNull(consumer);
        super.forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    /**
     * Returns a new {@code EntryStream} with keys and values swapped.
     *
     * @return a new {@code EntryStream} with inverted key-value pairs
     */
    public EntryStream<V, K> inverse() {
        return new EntryStream<>(stream.map(e -> ofEntry(e.getValue(), e.getKey())));
    }

    /**
     * Collects the keys of this stream using the provided {@link Collector}.
     *
     * @param collector the {@link Collector} to accumulate the keys
     * @param <R>       the type of the result from the collector
     * @return the result of the collection
     */
    public <R> R collectKeys(final Collector<K, ?, R> collector) {
        return toKeyStream().collect(collector);
    }

    /**
     * Collects the values of this stream using the provided {@link Collector}.
     *
     * @param collector the {@link Collector} to accumulate the values
     * @param <R>       the type of the result from the collector
     * @return the result of the collection
     */
    public <R> R collectValues(final Collector<V, ?, R> collector) {
        return toValueStream().collect(collector);
    }

    /**
     * Returns whether any key-value pair in this stream matches the given predicate.
     *
     * @param predicate a {@link BiPredicate} to apply to each key-value pair to determine if a match exists
     * @return {@code true} if any key-value pair matches the predicate, {@code false} otherwise
     */
    public boolean anyMatch(final BiPredicate<? super K, ? super V> predicate) {
        return super.anyMatch(e -> predicate.test(e.getKey(), e.getValue()));
    }

    /**
     * Returns whether all key-value pairs in this stream match the given predicate.
     *
     * @param predicate a {@link BiPredicate} to apply to each key-value pair to determine if all match
     * @return {@code true} if all key-value pairs match the predicate, {@code false} otherwise
     */
    public boolean allMatch(final BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate);
        return super.allMatch(e -> predicate.test(e.getKey(), e.getValue()));
    }

    /**
     * Returns whether no key-value pairs in this stream match the given predicate.
     *
     * @param predicate a {@link BiPredicate} to apply to each key-value pair to determine if none match
     * @return {@code true} if no key-value pair matches the predicate, {@code false} otherwise
     */
    public boolean noneMatch(final BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate);
        return super.noneMatch(e -> predicate.test(e.getKey(), e.getValue()));
    }

    /**
     * Wraps a given {@link Stream} of {@link Map.Entry} objects into a new {@code EntryStream} instance.
     *
     * @param stream the {@link Stream} of {@link Map.Entry} objects to wrap
     * @return a new {@code EntryStream} instance wrapping the provided stream
     */
    @Override
    public EntryStream<K, V> wrap(final Stream<Map.Entry<K, V>> stream) {
        return new EntryStream<>(stream);
    }

}

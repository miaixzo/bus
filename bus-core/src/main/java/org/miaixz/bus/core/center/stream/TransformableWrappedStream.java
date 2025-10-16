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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.mutable.MutableInt;
import org.miaixz.bus.core.lang.mutable.MutableObject;
import org.miaixz.bus.core.xyz.*;

/**
 * An extension of {@link WrappedStream} that provides additional intermediate operations for implementing classes. The
 * methods provided by this interface return a {@link Stream}.
 *
 * @param <T> the type of the elements in the stream
 * @param <S> the type of the {@link TransformableWrappedStream} implementation itself
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TransformableWrappedStream<T, S extends TransformableWrappedStream<T, S>> extends WrappedStream<T, S> {

    /**
     * Combines the current elements with elements from a given {@link Iterable} using a zipper function. The new stream
     * will have a number of elements equal to the smaller of the two collections, meaning only elements at matching
     * indices are combined.
     *
     * @param other  the {@link Iterable} to zip with
     * @param zipper a {@link BiFunction} that combines an element from this stream and an element from the other
     *               iterable
     * @param <U>    the type of elements in the other iterable
     * @param <R>    the type of elements in the resulting stream after zipping
     * @return a new {@link EasyStream} containing the combined elements
     */
    default <U, R> EasyStream<R> zip(
            final Iterable<U> other,
            final BiFunction<? super T, ? super U, ? extends R> zipper) {
        Objects.requireNonNull(zipper);
        final Map<Integer, T> idxIdentityMap = mapIdx((e, idx) -> MapKit.entry(idx, e))
                .collect(CollectorKit.entryToMap());
        final Map<Integer, U> idxOtherMap = EasyStream.of(other).mapIdx((e, idx) -> MapKit.entry(idx, e))
                .collect(CollectorKit.entryToMap());
        if (idxIdentityMap.size() <= idxOtherMap.size()) {
            return EasyStream.of(idxIdentityMap.keySet(), isParallel())
                    .map(k -> zipper.apply(idxIdentityMap.get(k), idxOtherMap.get(k)));
        }
        return EasyStream.of(idxOtherMap.keySet(), isParallel())
                .map(k -> zipper.apply(idxIdentityMap.get(k), idxOtherMap.get(k)));
    }

    /**
     * Splits the stream into sub-streams of a specified batch size.
     * <p>
     * Example: {@code [1,2,3,4,5]} -> {@code [[1,2], [3,4], [5]]}
     * 
     *
     * @param batchSize the desired size of each sub-stream (must be a positive integer)
     * @return an {@link EasyStream} of {@link EasyStream}s, where each inner stream contains elements of the specified
     *         batch size
     */
    default EasyStream<EasyStream<T>> split(final int batchSize) {
        final List<T> list = this.collect(Collectors.toList());
        final int size = list.size();
        // If batchSize is greater than or equal to the list size
        if (size <= batchSize) {
            // Return a stream with a single inner stream containing all elements
            return EasyStream.<EasyStream<T>>of(EasyStream.of(list, isParallel()));
        }
        return EasyStream.iterate(0, i -> i < size, i -> i + batchSize)
                .map(skip -> EasyStream.of(list.subList(skip, Math.min(size, skip + batchSize)), isParallel()))
                .parallel(isParallel()).onClose(unwrap()::close);
    }

    /**
     * Splits the stream into sub-lists of a specified batch size.
     * <p>
     * Example: {@code [1,2,3,4,5]} -> {@code [[1,2], [3,4], [5]]}
     * 
     *
     * @param batchSize the desired size of each sub-list (must be a positive integer)
     * @return an {@link EasyStream} of {@link List}s, where each list contains elements of the specified batch size
     */
    default EasyStream<List<T>> splitList(final int batchSize) {
        return split(batchSize).map(EasyStream::toList);
    }

    /**
     * Converts the current stream into an {@link EntryStream} using the provided key and value mappers.
     *
     * @param keyMapper   a function to extract the key from each element
     * @param valueMapper a function to extract the value from each element
     * @param <K>         the type of the keys in the resulting {@link EntryStream}
     * @param <V>         the type of the values in the resulting {@link EntryStream}
     * @return an {@link EntryStream} instance
     */
    default <K, V> EntryStream<K, V> toEntries(final Function<T, K> keyMapper, final Function<T, V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        return new EntryStream<>(map(t -> EntryStream.ofEntry(keyMapper.apply(t), valueMapper.apply(t))));
    }

    /**
     * Converts the current stream into an {@link EntryStream} using the provided key mapper, with the elements
     * themselves serving as values.
     *
     * @param keyMapper a function to extract the key from each element
     * @param <K>       the type of the keys in the resulting {@link EntryStream}
     * @return an {@link EntryStream} instance
     */
    default <K> EntryStream<K, T> toEntries(final Function<T, K> keyMapper) {
        return toEntries(keyMapper, Function.identity());
    }

    /**
     * Returns a stream with the elements in reverse order.
     *
     * @return a new stream with elements in reverse order
     */
    default S reverse() {
        final T[] array = (T[]) toArray();
        ArrayKit.reverse(array);
        return wrap(Stream.of(array)).parallel(isParallel());
    }

    /**
     * Changes the parallel status of the stream.
     *
     * @param parallel {@code true} to make the stream parallel, {@code false} to make it sequential
     * @return the stream with the updated parallel status
     */
    default S parallel(final boolean parallel) {
        return parallel ? parallel() : sequential();
    }

    /**
     * Modifies the stream by deleting or replacing existing elements or adding new elements in place, similar to
     * JavaScript's <a href=
     * "https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Array/splice">splice</a>
     * function. This method does not modify the original list.
     *
     * @param start       the index at which to start changing the array
     * @param deleteCount the number of elements to remove from the start index
     * @param items       the elements to add to the array, beginning at the start index
     * @return a new stream containing the modified elements
     */
    default S splice(final int start, final int deleteCount, final T... items) {
        final List<T> elements = unwrap().collect(Collectors.toList());
        return wrap(ListKit.splice(elements, start, deleteCount, items).stream()).parallel(isParallel());
    }

    /**
     * Returns a stream consisting of the longest prefix of elements taken from this stream that match the given
     * predicate. The stream terminates when the first non-matching element is encountered.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * EasyStream.of(1, 2, 3, 4, 5).takeWhile(i -> i < 3) // Get elements until the first element >= 3 is encountered
     *         .toList(); // = [1, 2]
     * }</pre>
     * <p>
     * Unlike {@code JDK9}'s {@code takeWhile} method, this operation is a sequential and stateful intermediate
     * operation. Even in parallel streams, this operation is executed sequentially and does not affect subsequent
     * parallel operations:
     * 
     * <pre>{@code
     * EasyStream.iterate(1, i -> i + 1).parallel().takeWhile(e -> e < 50) // Sequential execution
     *         .map(e -> e + 1) // Concurrent
     *         .map(String::valueOf) // Concurrent
     *         .toList();
     * }</pre>
     * 
     * If not strictly necessary, it is not recommended to perform this operation in parallel streams.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to elements of
     *                  this stream
     * @return a stream consisting of the longest prefix of elements taken from this stream that match the given
     *         predicate
     */
    default S takeWhile(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return wrap(StreamKit.takeWhile(unwrap(), predicate));
    }

    /**
     * Returns a stream consisting of the remaining elements of this stream after discarding the longest prefix of
     * elements that match the given predicate. The stream starts emitting elements when the first non-matching element
     * is encountered.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * EasyStream.of(1, 2, 3, 4, 5).dropWhile(i -> i < 3) // Drop elements until the first element >= 3 is encountered
     *         .toList(); // = [3, 4, 5]
     * }</pre>
     * <p>
     * Unlike {@code JDK9}'s {@code dropWhile} method, this operation is a sequential and stateful intermediate
     * operation. Even in parallel streams, this operation is executed sequentially and does not affect subsequent
     * parallel operations:
     * 
     * <pre>{@code
     * EasyStream.iterate(1, i -> i + 1).parallel().dropWhile(e -> e < 50) // Sequential execution
     *         .map(e -> e + 1) // Concurrent
     *         .map(String::valueOf) // Concurrent
     *         .toList();
     * }</pre>
     * 
     * If not strictly necessary, it is not recommended to perform this operation in parallel streams.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to elements of
     *                  this stream
     * @return a stream consisting of the remaining elements of this stream after discarding the longest matching prefix
     */
    default S dropWhile(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return wrap(StreamKit.dropWhile(unwrap(), predicate));
    }

    /**
     * Returns a stream consisting of the distinct elements of this stream based on a key extracted by the
     * {@code keyExtractor}. For non-parallel (sequential) streams, in case of duplicate keys, the first encountered
     * element is retained. For parallel streams, there is no guarantee which specific duplicate element will be
     * retained. This is a stateful intermediate operation.
     *
     * @param <F>          the type of the key extracted from the elements
     * @param keyExtractor a function to extract the key from each element for distinctness comparison
     * @return a stream consisting of the distinct elements of this stream
     */
    default <F> EasyStream<T> distinct(final Function<? super T, F> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        if (isParallel()) {
            final ConcurrentHashMap<F, Boolean> exists = new ConcurrentHashMap<>();
            // Flag to mark if a null value has been encountered, used to retain the first null.
            // Since ConcurrentHashMap keys cannot be null, this variable is used to track nulls.
            final AtomicBoolean hasNull = new AtomicBoolean(false);
            return EasyStream.of(unwrap().filter(e -> {
                final F key = keyExtractor.apply(e);
                if (key == null) {
                    // If a null value has already been encountered, skip this value
                    if (hasNull.get()) {
                        return false;
                    }
                    hasNull.set(Boolean.TRUE);
                    return true;
                } else {
                    // Return true for the first occurrence of a key
                    return null == exists.putIfAbsent(key, Boolean.TRUE);
                }
            })).parallel();
        } else {
            final Set<F> exists = new HashSet<>();
            return EasyStream.of(unwrap().filter(e -> exists.add(keyExtractor.apply(e))));
        }
    }

    /**
     * Returns a stream consisting of the results of applying the given action to each element of this stream, providing
     * the element and its index. This is a stateless intermediate operation.
     * <p>
     * Example:
     * 
     * <pre>
     *     {@code
     * Stream.of("one", "two", "three", "four").filter(e -> e.length() > 3)
     *         .peekIdx((e, i) -> System.out.println("Filtered value: " + e + " Filtered idx:" + i))
     *         .map(String::toUpperCase)
     *         .peekIdx((e, i) -> System.out.println("Mapped value: " + e + " Mapped idx:" + i))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * @param action a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> action to perform on the elements and
     *               their indices as they are consumed from the stream
     * @return the new stream
     */
    default S peekIdx(final BiConsumer<? super T, Integer> action) {
        Objects.requireNonNull(action);
        final AtomicInteger index = new AtomicInteger(NOT_FOUND_ELEMENT_INDEX);
        return peek(e -> action.accept(e, index.incrementAndGet()));
    }

    /**
     * Returns a stream that prints each element to the console using {@link Console#log(Object)}. This is a stateless
     * intermediate operation.
     *
     * @return the new stream
     */
    default S log() {
        return peek(Console::log);
    }

    /**
     * Appends the given elements to the end of the current stream.
     *
     * @param object the elements to append
     * @return a new stream with the appended elements
     */
    default S push(final T... object) {
        Stream<T> result = unwrap();
        if (ArrayKit.isNotEmpty(object)) {
            result = Stream.concat(unwrap(), Stream.of(object));
        }
        return wrap(result);
    }

    /**
     * Prepends the given elements to the beginning of the current stream.
     *
     * @param object the elements to prepend
     * @return a new stream with the prepended elements
     */
    default S unshift(final T... object) {
        Stream<T> result = unwrap();
        if (ArrayKit.isNotEmpty(object)) {
            result = Stream.concat(Stream.of(object), unwrap());
        }
        return wrap(result);
    }

    /**
     * Appends the elements from the given {@link Iterable} to the end of the current stream.
     *
     * @param iterable the {@link Iterable} whose elements are to be appended
     * @return a new stream with the appended elements
     */
    default S append(final Iterable<? extends T> iterable) {
        if (IteratorKit.isEmpty(iterable)) {
            return wrap(this);
        }
        final Stream<? extends T> contacted = StreamSupport.stream(iterable.spliterator(), isParallel());
        return wrap(Stream.concat(this, contacted));
    }

    /**
     * Prepends the elements from the given {@link Iterable} to the beginning of the current stream.
     *
     * @param iterable the {@link Iterable} whose elements are to be prepended
     * @return a new stream with the prepended elements
     */
    default S prepend(final Iterable<? extends T> iterable) {
        if (IteratorKit.isEmpty(iterable)) {
            return wrap(this);
        }
        final Stream<? extends T> contacted = StreamSupport.stream(iterable.spliterator(), isParallel());
        return wrap(Stream.concat(contacted, this));
    }

    /**
     * Returns a stream consisting of the non-null elements of this stream.
     *
     * @return a new stream with non-null elements
     */
    default S nonNull() {
        return filter(Objects::nonNull);
    }

    /**
     * Returns a stream consisting of the elements of this stream that match the given predicate, providing the element
     * and its index. This is a stateless intermediate operation.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to each element
     *                  and its index to determine if it should be included
     * @return the new stream
     */
    default S filterIdx(final BiPredicate<? super T, Integer> predicate) {
        Objects.requireNonNull(predicate);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        return filter(e -> predicate.test(e, index.incrementAndGet()));
    }

    /**
     * Returns a stream consisting of the elements of this stream whose mapped value matches the specified value. This
     * is a stateless intermediate operation.
     *
     * @param <R>    the type of the result of the mapper function
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a value for comparison
     * @param value  the value to match against the mapped elements
     * @return a new stream containing only the elements whose mapped value matches the specified value
     */
    default <R> S filter(final Function<? super T, ? extends R> mapper, final R value) {
        Objects.requireNonNull(mapper);
        return filter(e -> Objects.equals(mapper.apply(e), value));
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
     * EasyStream<Long> ids = EasyStream.of(users).flatMap(user -> FastStream.of(user.getId(), user.getParentId()));
     * }</pre>
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces a stream of new values
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    @Override
    default <R> EasyStream<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return new EasyStream<>(unwrap().flatMap(mapper));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of replacing each element of this stream with the
     * contents of a mapped stream produced by applying the provided mapping function to each element and its index.
     * Each mapped stream is closed after its contents have been placed into this stream. This is a stateless
     * intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element and
     *               its index which produces a stream of new values
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    default <R> EasyStream<R> flatMapIdx(final BiFunction<? super T, Integer, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        return flatMap(e -> mapper.apply(e, index.incrementAndGet()));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of replacing each element of this stream with the
     * contents of a mapped {@link Iterable} produced by applying the provided mapping function to each element. This is
     * a stateless intermediate operation.
     * <p>
     * For example, to combine the IDs and parent IDs of all users in a list into a new stream:
     * 
     * <pre>{@code
     * 
     * EasyStream<Long> ids = EasyStream.of(users).flat(user -> FastStream.of(user.getId(), user.getParentId()));
     * }</pre>
     *
     * @param mapper a non-interfering stateless function to apply to each element which produces an {@link Iterable} of
     *               new values
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    default <R> EasyStream<R> flat(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return flatMap(w -> EasyStream.of(mapper.apply(w)));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of replacing each non-null element of this stream with
     * the contents of a mapped {@link Iterable} produced by applying the provided mapping function to each element, and
     * then filtering out any null elements from the resulting stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each non-null
     *               element which produces an {@link Iterable} of new values
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream} with non-null flattened elements
     * @see #flat(Function)
     * @see #nonNull()
     */
    default <R> EasyStream<R> flatNonNull(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return nonNull().flat(mapper).nonNull();
    }

    /**
     * Flattens a hierarchical structure (e.g., a tree) into a single stream. This is a stateless intermediate
     * operation.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * 
     * List<Student> students = EasyStream.of(studentTree).flatTree(Student::getChildren, Student::setChildren)
     *         .toList();
     * }</pre>
     *
     * @param childrenGetter a function to get the children of a node
     * @param childrenSetter a function to set the children of a node (used to clear children after flattening)
     * @return an {@link EasyStream} containing all nodes from the flattened tree
     */
    default S flatTree(final Function<T, List<T>> childrenGetter, final BiConsumer<T, List<T>> childrenSetter) {
        Objects.requireNonNull(childrenGetter);
        Objects.requireNonNull(childrenSetter);
        final MutableObject<Function<T, EasyStream<T>>> recursiveRef = new MutableObject<>();
        final Function<T, EasyStream<T>> recursive = e -> EasyStream.of(childrenGetter.apply(e))
                .flat(recursiveRef.get()).unshift(e);
        recursiveRef.set(recursive);
        return wrap(flatMap(recursive).peek(e -> childrenSetter.accept(e, null)));
    }

    /**
     * Flattens a stream of collections into a stream of their elements. For example, a {@code List<List<List<String>>>}
     * can be flattened into a {@code List<String>}.
     *
     * @param <R> the type of elements in the flattened stream
     * @return an {@link EasyStream} containing all elements from the flattened collections
     */
    default <R> EasyStream<R> flat() {
        return EasyStream.of(CollKit.flat(nonNull().collect(Collectors.toList())));
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
    default <R> EasyStream<R> map(final Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new EasyStream<>(unwrap().map(mapper));
    }

    /**
     * Returns an {@link EasyStream} consisting of the non-null results of applying the given function to the elements
     * of this stream. This is a stateless intermediate operation.
     * <p>
     * This is equivalent to calling {@code nonNull().map(...).nonNull()...}
     * 
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a new element
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream} with non-null mapped elements
     */
    default <R> EasyStream<R> mapNonNull(final Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new EasyStream<>(nonNull().<R>map(mapper).nonNull());
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of applying the given function to the elements of this
     * stream and their indices. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element and
     *               its index to produce a new element
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    default <R> EasyStream<R> mapIdx(final BiFunction<? super T, Integer, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        return map(e -> mapper.apply(e, index.incrementAndGet()));
    }

    /**
     * Returns an {@link EasyStream} consisting of the results of applying the given function to each element of this
     * stream, where the function may produce zero, one, or more elements. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces zero, one, or more elements
     * @param <R>    the element type of the new stream
     * @return the new {@link EasyStream}
     */
    default <R> EasyStream<R> mapMulti(final BiConsumer<? super T, ? super Consumer<R>> mapper) {
        Objects.requireNonNull(mapper);
        return flatMap(e -> {
            final EasyStream.Builder<R> buffer = EasyStream.builder();
            mapper.accept(e, buffer);
            return buffer.build();
        });
    }

}

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
package org.miaixz.bus.core.center.stream;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * A wrapper for {@link Stream} instances, used to enhance the original {@link Stream} by providing additional
 * intermediate and terminal operations. Two primary implementations are provided:
 * <ul>
 * <li>{@link EasyStream}: A general-purpose enhanced stream implementation for single elements;</li>
 * <li>{@link EntryStream}: An enhanced stream implementation for key-value pair elements;</li>
 * </ul>
 *
 * @param <T> the type of the elements in the stream
 * @param <S> the type of the {@link WrappedStream} implementation itself
 * @author Kimi Liu
 * @see TerminableWrappedStream
 * @see TransformableWrappedStream
 * @see EnhancedWrappedStream
 * @see EasyStream
 * @see EntryStream
 * @since Java 17+
 */
public interface WrappedStream<T, S extends WrappedStream<T, S>> extends Stream<T>, Iterable<T> {

    /**
     * Represents a non-existent index or an index when an element is not found.
     */
    int NOT_FOUND_ELEMENT_INDEX = -1;

    /**
     * Retrieves the underlying {@link Stream} object wrapped by this instance.
     *
     * @return the {@link Stream} object wrapped by this instance
     */
    Stream<T> unwrap();

    /**
     * Wraps a source {@link Stream} into a new enhanced stream of the specified type. If the {@code source} stream is
     * different from the stream wrapped by the current instance, the new enhanced stream will not be associated with
     * the current instance.
     *
     * @param source the {@link Stream} to be wrapped
     * @return the wrapped stream instance of type {@code S}
     */
    S wrap(final Stream<T> source);

    /**
     * Returns a stream consisting of the elements of this stream that match the given predicate. This is a stateless
     * intermediate operation.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to each element to
     *                  determine if it should be included
     * @return the new stream
     */
    @Override
    default S filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return wrap(unwrap().filter(predicate));
    }

    /**
     * Returns an {@link IntStream} consisting of the results of applying the given function to the elements of this
     * stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce an {@code int} value
     * @return the new {@link IntStream}
     */
    @Override
    default IntStream mapToInt(final ToIntFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().mapToInt(mapper);
    }

    /**
     * Returns a {@link LongStream} consisting of the results of applying the given function to the elements of this
     * stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a {@code long} value
     * @return the new {@link LongStream}
     */
    @Override
    default LongStream mapToLong(final ToLongFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().mapToLong(mapper);
    }

    /**
     * Returns a {@link DoubleStream} consisting of the results of applying the given function to the elements of this
     * stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a {@code double} value
     * @return the new {@link DoubleStream}
     */
    @Override
    default DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().mapToDouble(mapper);
    }

    /**
     * Returns an {@link IntStream} consisting of the results of replacing each element of this stream with the contents
     * of a mapped {@link IntStream} produced by applying the provided mapping function to each element. Each mapped
     * stream is closed after its contents have been placed into this stream. This is a stateless intermediate
     * operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces an {@link IntStream} of new values
     * @return the new {@link IntStream}
     */
    @Override
    default IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().flatMapToInt(mapper);
    }

    /**
     * Returns a {@link LongStream} consisting of the results of replacing each element of this stream with the contents
     * of a mapped {@link LongStream} produced by applying the provided mapping function to each element. Each mapped
     * stream is closed after its contents have been placed into this stream. This is a stateless intermediate
     * operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces a {@link LongStream} of new values
     * @return the new {@link LongStream}
     */
    @Override
    default LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().flatMapToLong(mapper);
    }

    /**
     * Returns a {@link DoubleStream} consisting of the results of replacing each element of this stream with the
     * contents of a mapped {@link DoubleStream} produced by applying the provided mapping function to each element.
     * Each mapped stream is closed after its contents have been placed into this stream. This is a stateless
     * intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element which
     *               produces a {@link DoubleStream} of new values
     * @return the new {@link DoubleStream}
     */
    @Override
    default DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        return unwrap().flatMapToDouble(mapper);
    }

    /**
     * Returns a stream consisting of the distinct elements of this stream. For non-parallel (sequential) streams, in
     * case of duplicate elements, the first encountered element is retained. For parallel streams, there is no
     * guarantee which specific duplicate element will be retained. This is a stateful intermediate operation.
     *
     * @return a stream consisting of the distinct elements of this stream
     */
    @Override
    default S distinct() {
        return wrap(unwrap().distinct());
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted according to natural order. If the elements of
     * this stream are not {@code Comparable}, a {@code java.lang.ClassCastException} may be thrown when a terminal
     * operation is executed. For sequential streams, the sort is stable. For unordered streams, no stability guarantees
     * are made. This is a stateful intermediate operation.
     *
     * @return a stream consisting of the elements of this stream, sorted according to natural order
     */
    @Override
    default S sorted() {
        return wrap(unwrap().sorted());
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted according to the provided {@link Comparator}.
     * If the elements of this stream are not {@code Comparable}, a {@code java.lang.ClassCastException} may be thrown
     * when a terminal operation is executed. For sequential streams, the sort is stable. For unordered streams, no
     * stability guarantees are made. This is a stateful intermediate operation.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a> {@link Comparator} to be used to
     *                   compare elements in this stream
     * @return a stream consisting of the elements of this stream, sorted according to the provided {@link Comparator}
     */
    @Override
    default S sorted(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return wrap(unwrap().sorted(comparator));
    }

    /**
     * Performs an action for each element of this stream. This is a stateless intermediate operation.
     * <p>
     * For example, to observe elements at various points in a pipeline:
     * 
     * <pre>{@code
     *     .of("one", "two", "three", "four")
     *         .filter(e -> e.length() > 3)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(String::toUpperCase)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * @param action a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> action to perform on the elements as
     *               they are consumed from the stream
     * @return the new stream
     */
    @Override
    default S peek(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        return wrap(unwrap().peek(action));
    }

    /**
     * Returns a stream consisting of the elements of this stream, truncated to be no longer than {@code maxSize} in
     * length. This is a short-circuiting intermediate operation.
     *
     * @param maxSize the maximum number of elements to be returned in the new stream
     * @return the new stream
     */
    @Override
    default S limit(final long maxSize) {
        return wrap(unwrap().limit(maxSize));
    }

    /**
     * Returns a stream consisting of the remaining elements of this stream after discarding the first {@code n}
     * elements. If this stream contains fewer than {@code n} elements then an empty stream will be returned. This is a
     * stateful intermediate operation.
     *
     * @param n the number of leading elements to skip
     * @return the new stream
     */
    @Override
    default S skip(final long n) {
        return wrap(unwrap().skip(n));
    }

    /**
     * Performs an action for each element of this stream. This is a terminal operation.
     *
     * @param action a <a href="package-summary.html#NonInterference">non-interfering</a> action to perform on the
     *               elements
     */
    @Override
    default void forEach(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        unwrap().forEach(action);
    }

    /**
     * Performs an action for each element of this stream, in encounter order. This is a terminal operation.
     *
     * @param action a <a href="package-summary.html#NonInterference">non-interfering</a> action to perform on the
     *               elements
     */
    @Override
    default void forEachOrdered(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        unwrap().forEachOrdered(action);
    }

    /**
     * Returns an array containing the elements of this stream. This is a terminal operation.
     *
     * @return an array containing the elements of this stream
     */
    @Override
    default Object[] toArray() {
        return unwrap().toArray();
    }

    /**
     * Returns an array containing the elements of this stream, using the provided {@code generator} function to
     * allocate the returned array, as well as any additional arrays that might be required for computation or buffering
     * during the operation. This is a terminal operation.
     * <p>
     * For example, the following code compiles but throws an {@link ArrayStoreException} at runtime:
     * 
     * <pre>{@code
     *
     * String[] strings = Stream.<Integer>builder().add(1).build().toArray(String[]::new);
     * }</pre>
     *
     * @param generator a function which produces a new array of the desired type and the provided length (for example
     *                  {@code MyClass[]::new})
     * @param <A>       the type of the elements of the array
     * @return an array containing the elements of this stream
     * @throws ArrayStoreException if the runtime type of the array returned by the array supplier is not a supertype of
     *                             the runtime type of every element in this stream
     */
    @Override
    default <A> A[] toArray(final IntFunction<A[]> generator) {
        Objects.requireNonNull(generator);
        return unwrap().toArray(generator);
    }

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the elements of this stream, using the
     * provided identity value and an <a href="package-summary.html#Associativity">associative</a> accumulation
     * function, and returns the reduced value. This is a terminal operation.
     * <p>
     * For example, to sum integers:
     * 
     * <pre>{@code
     *
     * Integer sum = integers.reduce(0, (a, b) -> a + b);
     * }</pre>
     * <p>
     * Or equivalently:
     * 
     * <pre>{@code
     *
     * Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * @param identity    the identity value for the accumulating function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for combining two values
     * @return the result of the reduction
     */
    @Override
    default T reduce(final T identity, final BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator);
        return unwrap().reduce(identity, accumulator);
    }

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the elements of this stream, using an
     * <a href="package-summary.html#Associativity">associative</a> accumulation function, and returns an
     * {@link Optional} describing the reduced value, if any. This is a terminal operation.
     * <p>
     * This is equivalent to:
     * 
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this unwrap) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     * <p>
     * However, it is not limited to sequential execution, for example, in parallel streams.
     * <p>
     * An example scenario where {@link NullPointerException} might be thrown:
     * 
     * <pre>{@code
     *
     * Optional<Integer> reduce = Stream.<Integer>builder().add(1).add(1).build().reduce((a, b) -> null);
     * }</pre>
     *
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for combining two values
     * @return an {@link Optional} describing the result of the reduction
     * @throws NullPointerException if the result of the accumulator function is {@code null} and used for the next
     *                              operation
     * @see #reduce(Object, BinaryOperator)
     * @see #min(Comparator)
     * @see #max(Comparator)
     */
    @Override
    default Optional<T> reduce(final BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator);
        return unwrap().reduce(accumulator);
    }

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the elements of this stream, using the
     * provided identity, accumulation and combining functions. This is a terminal operation.
     * <p>
     * When executed in parallel, the initial value obtained from the accumulator might be unstable.
     *
     * @param identity    the identity value for the accumulating function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for combining an
     *                    accumulated result and an element
     * @param combiner    an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for combining two
     *                    accumulated results
     * @param <U>         the type of the result
     * @return the result of the reduction
     * @see #reduce(BinaryOperator)
     * @see #reduce(Object, BinaryOperator)
     */
    @Override
    default <U> U reduce(
            final U identity,
            final BiFunction<U, ? super T, U> accumulator,
            final BinaryOperator<U> combiner) {
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return unwrap().reduce(identity, accumulator, combiner);
    }

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable reduction</a> operation on the elements of
     * this stream using a {@link Collector}. This is a terminal operation.
     *
     * @param supplier    a function that creates a new mutable result container. For a parallel execution, this
     *                    function may be called multiple times, and must return a fresh value each time.
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for incorporating an
     *                    additional element into a result
     * @param combiner    an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a> function for combining two partial
     *                    results
     * @param <R>         the type of the result
     * @return the result of the mutable reduction
     * 
     *         <pre>{@code
     *  List<Integer> collect = Stream.iterate(1, i -> ++i).limit(10).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
     * }</pre>
     */
    @Override
    default <R> R collect(
            final Supplier<R> supplier,
            final BiConsumer<R, ? super T> accumulator,
            final BiConsumer<R, R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return unwrap().collect(supplier, accumulator, combiner);
    }

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable reduction</a> operation on the elements of
     * this stream using a {@link Collector}. This is a terminal operation.
     *
     * @param collector the {@link Collector} describing the reduction
     * @param <R>       the type of the result
     * @param <A>       the intermediate accumulation type of the {@link Collector}
     * @return the result of the reduction
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        Objects.requireNonNull(collector);
        return unwrap().collect(collector);
    }

    /**
     * Returns an {@link Optional} describing the minimum element of this stream according to the provided
     * {@link Comparator}. This is a terminal operation.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a> {@link Comparator} to compare
     *                   elements of this stream
     * @return an {@link Optional} describing the minimum element of this stream, or an empty {@link Optional} if the
     *         stream is empty
     */
    @Override
    default Optional<T> min(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return unwrap().min(comparator);
    }

    /**
     * Returns an {@link Optional} describing the maximum element of this stream according to the provided
     * {@link Comparator}. This is a terminal operation.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a> {@link Comparator} to compare
     *                   elements of this stream
     * @return an {@link Optional} describing the maximum element of this stream, or an empty {@link Optional} if the
     *         stream is empty
     */
    @Override
    default Optional<T> max(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return unwrap().max(comparator);
    }

    /**
     * Returns the count of elements in this stream. This is a terminal operation.
     *
     * @return the count of elements in this stream
     */
    @Override
    default long count() {
        return unwrap().count();
    }

    /**
     * Returns whether any elements of this stream match the provided predicate. This is a short-circuiting terminal
     * operation.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to elements of
     *                  this stream
     * @return {@code true} if any elements of the stream match the provided predicate, otherwise {@code false}
     */
    @Override
    default boolean anyMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return unwrap().anyMatch(predicate);
    }

    /**
     * Returns whether all elements of this stream match the provided predicate. This is a short-circuiting terminal
     * operation.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to elements of
     *                  this stream
     * @return {@code true} if all elements of the stream match the provided predicate, otherwise {@code false}
     */
    @Override
    default boolean allMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return unwrap().allMatch(predicate);
    }

    /**
     * Returns whether no elements of this stream match the provided predicate. This is a short-circuiting terminal
     * operation.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to elements of
     *                  this stream
     * @return {@code true} if no elements of the stream match the provided predicate, otherwise {@code false}
     */
    @Override
    default boolean noneMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return unwrap().noneMatch(predicate);
    }

    /**
     * Returns an {@link Optional} describing the first element of this stream, or an empty {@link Optional} if the
     * stream is empty. If the stream has no encounter order, then any element may be returned. This is a
     * short-circuiting terminal operation.
     *
     * @return an {@link Optional} describing the first element of this stream, or an empty {@link Optional} if the
     *         stream is empty
     */
    @Override
    default Optional<T> findFirst() {
        return unwrap().findFirst();
    }

    /**
     * Returns an {@link Optional} describing some element of the stream, or an empty {@link Optional} if the stream is
     * empty. This is a short-circuiting terminal operation.
     * <p>
     * The behavior of this operation is explicitly nondeterministic; it is free to select any element in the stream.
     * This is to allow for maximal performance in parallel operations; the cost of obtaining a first element is often
     * higher in a parallel stream than obtaining an arbitrary element.
     *
     * @return an {@link Optional} describing some element of this stream, or an empty {@link Optional} if the stream is
     *         empty
     */
    @Override
    default Optional<T> findAny() {
        return unwrap().findAny();
    }

    /**
     * Returns an {@link Iterator} for the elements of this stream.
     *
     * @return an {@link Iterator} for the elements of this stream
     */
    @Override
    default Iterator<T> iterator() {
        return unwrap().iterator();
    }

    /**
     * Returns a {@link Spliterator} for the elements of this stream.
     *
     * @return a {@link Spliterator} for the elements of this stream
     */
    @Override
    default Spliterator<T> spliterator() {
        return unwrap().spliterator();
    }

    /**
     * Returns {@code true} if this stream is parallel, {@code false} otherwise.
     *
     * @return {@code true} if this stream is parallel, {@code false} otherwise
     */
    @Override
    default boolean isParallel() {
        return unwrap().isParallel();
    }

    /**
     * Returns an equivalent sequential stream. This method can be used to convert a parallel stream to a sequential
     * stream.
     *
     * @return a sequential stream
     */
    @Override
    default S sequential() {
        return wrap(unwrap().sequential());
    }

    /**
     * Returns an equivalent parallel stream.
     *
     * @return a parallel stream
     */
    @Override
    default S parallel() {
        return wrap(unwrap().parallel());
    }

    /**
     * Returns an equivalent stream that is unordered.
     * <p>
     * A stream that is unordered does not have an encounter order. This can sometimes improve the performance of
     * parallel operations.
     * 
     *
     * @return an unordered stream
     */
    @Override
    default S unordered() {
        return wrap(unwrap().unordered());
    }

    /**
     * Returns an equivalent stream with an additional close-handler. Close handlers are run when the {@link #close()}
     * method is called on the stream.
     *
     * @param closeHandler a {@link Runnable} to execute when the stream is closed
     * @return a stream with an additional close-handler
     */
    @Override
    default S onClose(final Runnable closeHandler) {
        return wrap(unwrap().onClose(closeHandler));
    }

    /**
     * Closes this stream, causing all close handlers for this stream pipeline to be called.
     *
     * @see AutoCloseable#close()
     */
    @Override
    default void close() {
        unwrap().close();
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    int hashCode();

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param object the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    boolean equals(final Object object);

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    String toString();

    /**
     * Converts the current instance to an {@link EasyStream}.
     *
     * @return the converted {@link EasyStream}
     */
    default EasyStream<T> easyStream() {
        if (this instanceof EasyStream) {
            return (EasyStream<T>) this;
        } else if (this instanceof Iterator) {
            return (EasyStream<T>) EasyStream.of((Iterator<T>) this);
        } else {
            return EasyStream.of(collect(Collectors.toList()));
        }
    }

}

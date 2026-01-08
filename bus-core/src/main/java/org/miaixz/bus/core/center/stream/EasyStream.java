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

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.*;

/**
 * An extended stream implementation for single elements. It encapsulates and enhances the native Stream API. This
 * implementation is inspired by comparisons with vavr, eclipse-collections, stream-ex, and other language APIs,
 * combining daily usage habits for encapsulation and extension. Stream provides easy-to-use APIs for collections,
 * allowing developers to write code in a declarative programming style.
 *
 * <p>
 * Intermediate Operations and Terminal Operations
 * 
 * <p>
 * Stream operations are divided into <em>intermediate operations</em> and <em>terminal operations</em>. A stream only
 * truly executes its previous <em>intermediate operations</em> when a <em>terminal operation</em> is performed.
 * <strong>Intermediate Operations</strong>:
 * <ul>
 * <li>Stateless Intermediate Operations: Operations that do not require all elements to complete their current
 * operation before proceeding, and do not depend on the state of previous stream operations.</li>
 * <li>Stateful Intermediate Operations: Operations that require all elements to complete their current operation before
 * proceeding, and depend on the state of previous stream operations.</li>
 * </ul>
 * <strong>Terminal Operations</strong>:
 * <ul>
 * <li>Short-circuiting Terminal Operations: Operations that do not require all elements to complete their current
 * operation before returning a result.</li>
 * <li>Non-short-circuiting Terminal Operations: Operations that require all elements to complete their current
 * operation before returning a result.</li>
 * </ul>
 *
 * <p>
 * Sequential Streams and Parallel Streams
 * 
 * Streams are divided into two types: <em>sequential streams</em> and <em>parallel streams</em>:
 * <ul>
 * <li>Sequential Streams: All operations on the stream are performed by the current thread.</li>
 * <li>Parallel Streams: Operations on the stream are split into multiple asynchronous tasks by a {@link Spliterator},
 * and these asynchronous tasks are managed by a default {@link java.util.concurrent.ForkJoinPool} thread pool.</li>
 * </ul>
 * Different types of streams can be converted to each other using {@link #sequential()} or {@link #parallel()}.
 *
 * @param <T> the type of the elements in the stream
 * @author Kimi Liu
 * @see java.util.stream.Stream
 * @since Java 17+
 */
public class EasyStream<T> extends EnhancedWrappedStream<T, EasyStream<T>> {

    /**
     * Constructs an {@code EasyStream} from a given {@link Stream}. If the provided stream is {@code null}, an empty
     * stream will be used.
     *
     * @param stream the {@link Stream} to wrap
     */
    EasyStream(final Stream<T> stream) {
        super(ObjectKit.isNull(stream) ? Stream.empty() : stream);
    }

    /**
     * Returns a builder for {@code EasyStream}.
     *
     * @param <T> the type of the elements
     * @return a new {@link Builder} instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>() {

            /**
             * The serial version UID.
             */
            @Serial
            private static final long serialVersionUID = 2852271123932L;

            private final Stream.Builder<T> streamBuilder = Stream.builder();

            /**
             * Adds an element to the stream being built.
             *
             * @param t the element to add
             */
            @Override
            public void accept(final T t) {
                streamBuilder.accept(t);
            }

            /**
             * Builds the {@code EasyStream}.
             *
             * @return the built {@code EasyStream}
             */
            @Override
            public EasyStream<T> build() {
                return new EasyStream<>(streamBuilder.build());
            }
        };
    }

    /**
     * Returns an empty sequential {@code EasyStream}.
     *
     * @param <T> the type of the elements
     * @return an empty sequential {@code EasyStream}
     */
    public static <T> EasyStream<T> empty() {
        return new EasyStream<>(Stream.empty());
    }

    /**
     * Returns a sequential {@code EasyStream} containing a single element.
     *
     * @param t   the single element
     * @param <T> the type of the element
     * @return a sequential {@code EasyStream} containing a single element
     */
    public static <T> EasyStream<T> of(final T t) {
        return new EasyStream<>(Stream.of(t));
    }

    /**
     * Returns a sequential {@code EasyStream} containing the specified elements. If the input array is {@code null} or
     * empty, an empty sequential {@code EasyStream} is returned.
     *
     * @param values the elements to be contained in the stream
     * @param <T>    the type of the elements
     * @return a sequential {@code EasyStream} containing the specified elements
     */
    @SafeVarargs
    public static <T> EasyStream<T> of(final T... values) {
        return ArrayKit.isEmpty(values) ? EasyStream.empty() : new EasyStream<>(Stream.of(values));
    }

    /**
     * Creates a sequential {@code EasyStream} from an object implementing the {@link Iterable} interface. If the input
     * object is {@code null}, an empty sequential {@code EasyStream} is returned.
     *
     * @param iterable the object implementing the {@link Iterable} interface
     * @param <T>      the type of the elements
     * @return an {@code EasyStream}
     */
    public static <T> EasyStream<T> of(final Iterable<T> iterable) {
        return of(iterable, false);
    }

    /**
     * Creates an {@code EasyStream} from the given {@link Iterable}. If the input object is {@code null}, an empty
     * sequential {@code EasyStream} is returned.
     *
     * @param iterable the {@link Iterable} to create the stream from
     * @param parallel {@code true} if the stream should be parallel, {@code false} otherwise
     * @param <T>      the type of the elements
     * @return an {@code EasyStream}
     */
    public static <T> EasyStream<T> of(final Iterable<T> iterable, final boolean parallel) {
        return Optional.ofNullable(iterable).map(Iterable::spliterator)
                .map(spliterator -> StreamSupport.stream(spliterator, parallel)).map(EasyStream::new)
                .orElseGet(EasyStream::empty);
    }

    /**
     * Creates an {@code EasyStream} from the given {@link Stream}. If the input object is {@code null}, an empty
     * sequential {@code EasyStream} is returned.
     *
     * @param stream the {@link Stream} to create the {@code EasyStream} from
     * @param <T>    the type of the elements
     * @return an {@code EasyStream}
     */
    public static <T> EasyStream<T> of(final Stream<T> stream) {
        return new EasyStream<>(stream);
    }

    /**
     * Returns an infinite sequential ordered {@code EasyStream}. This stream is generated by an initial element and a
     * unary operator function that is applied to the previous element to produce a new element.
     * <p>
     * For example, {@code FastStream.iterate(0, i -> i + 1)} can create an infinite stream starting from 0 and
     * incrementing by 1. Use {@link EasyStream#limit(long)} to limit the number of elements.
     * 
     *
     * @param <T>  the type of the elements
     * @param seed the initial element
     * @param f    a function that applies to the previous element to produce a new element
     * @return an infinite sequential ordered {@code EasyStream}
     */
    public static <T> EasyStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return new EasyStream<>(Stream.iterate(seed, f));
    }

    /**
     * Returns an infinite sequential ordered {@code EasyStream}. This stream is generated by an initial element, a
     * predicate to determine if there are more elements, and a unary operator function that is applied to the previous
     * element to produce a new element.
     * <p>
     * For example, {@code FastStream.iterate(0, i -> i < 3, i -> ++i)} can create a stream containing elements 0, 1, 2.
     * Use {@link EasyStream#limit(long)} to limit the number of elements.
     * 
     *
     * @param <T>     the type of the elements
     * @param seed    the initial element
     * @param hasNext a predicate to apply to elements to determine if the iteration should continue
     * @param next    a function that applies to the previous element to produce a new element
     * @return an infinite sequential ordered {@code EasyStream}
     */
    public static <T> EasyStream<T> iterate(
            final T seed,
            final Predicate<? super T> hasNext,
            final UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        return new EasyStream<>(StreamKit.iterate(seed, hasNext, next));
    }

    /**
     * Specifies a root node of a hierarchical structure (typically a tree or graph), and then obtains a stream
     * consisting of all nodes in the hierarchical structure, including the root node. This method is used to access
     * graph or tree nodes in a flattened manner, and parallel streams can be used to improve efficiency.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Tree root = // Build tree structure
     * // Search for all nodes at level 3 in the tree structure and sort by weight
     * List<Tree> thirdLevelNodes = StreamKit.iterateHierarchies(root, Tree::getChildren)
     * 	.filter(node -> node.getLevel() == 3)
     * 	.sorted(Comparator.comparing(Tree::getWeight))
     * 	.toList();
     * }</pre>
     *
     * @param root       the root node
     * @param discoverer a function to get the next level of nodes
     * @param filter     a node filter; nodes that do not match and their subtrees will be ignored
     * @param <T>        the type of the object
     * @return a stream consisting of all nodes in the hierarchical structure, including the root node
     */
    public static <T> EasyStream<T> iterateHierarchies(
            final T root,
            final Function<T, Collection<T>> discoverer,
            final Predicate<T> filter) {
        return of(StreamKit.iterateHierarchies(root, discoverer, filter));
    }

    /**
     * Specifies a root node of a hierarchical structure (typically a tree or graph), and then obtains a stream
     * consisting of all nodes in the hierarchical structure, including the root node. This method is used to access
     * graph or tree nodes in a flattened manner, and parallel streams can be used to improve efficiency.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Tree root = // Build tree structure
     * // Search for all nodes at level 3 in the tree structure and sort by weight
     * List<Tree> thirdLevelNodes = StreamKit.iterateHierarchies(root, Tree::getChildren)
     * 	.filter(node -> node.getLevel() == 3)
     * 	.sorted(Comparator.comparing(Tree::getWeight))
     * 	.toList();
     * }</pre>
     *
     * @param root       the root node
     * @param discoverer a function to get the next level of nodes
     * @param <T>        the type of the object
     * @return a stream consisting of all nodes in the hierarchical structure, including the root node
     */
    public static <T> EasyStream<T> iterateHierarchies(final T root, final Function<T, Collection<T>> discoverer) {
        return of(StreamKit.iterateHierarchies(root, discoverer));
    }

    /**
     * Returns an infinite sequential unordered {@code EasyStream} where each element is generated by the given
     * {@link Supplier}. This is suitable for scenarios like generating constant streams or random elements.
     *
     * @param <T> the type of the elements
     * @param s   the {@link Supplier} to generate elements
     * @return an infinite sequential unordered {@code EasyStream}
     */
    public static <T> EasyStream<T> generate(final Supplier<T> s) {
        return new EasyStream<>(Stream.generate(s));
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the elements of the first stream, followed by all the
     * elements of the second stream. If both input streams are ordered, the result stream is ordered. If either input
     * stream is parallel, the result stream is parallel. When the result stream is closed, the close handlers of both
     * input streams are invoked.
     *
     * <p>
     * Concatenating from repetitive sequential streams may lead to deep call chains or even
     * {@code StackOverflowException}.
     * 
     *
     * @param <T> the type of the elements
     * @param a   the first stream
     * @param b   the second stream
     * @return a stream that concatenates the two input streams
     */
    public static <T> EasyStream<T> concat(final Stream<? extends T> a, final Stream<? extends T> b) {
        return new EasyStream<>(Stream.concat(a, b));
    }

    /**
     * Splits a character sequence into a sequential {@code EasyStream} of strings.
     *
     * @param text  the character sequence to split
     * @param regex the regular expression to use for splitting
     * @return an {@code EasyStream} of strings resulting from the split
     */
    public static EasyStream<String> split(final CharSequence text, final String regex) {
        return Optional.ofBlankAble(text).map(CharSequence::toString).map(s -> s.split(regex)).map(EasyStream::of)
                .orElseGet(EasyStream::empty);
    }

    /**
     * Returns an {@code EasyStream} consisting of the results of applying the given function to the elements of this
     * stream. This is a stateless intermediate operation.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> function to apply to each element to
     *               produce a new element
     * @param <R>    the element type of the new stream
     * @return the new {@code EasyStream}
     */
    @Override
    public <R> EasyStream<R> map(final Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new EasyStream<>(stream.map(mapper));
    }

    /**
     * Wraps a given {@link Stream} into a new {@code EasyStream} instance.
     *
     * @param stream the {@link Stream} to wrap
     * @return a new {@code EasyStream} instance wrapping the provided stream
     */
    @Override
    public EasyStream<T> wrap(final Stream<T> stream) {
        return new EasyStream<>(stream);
    }

    /**
     * Calculates the sum of {@code int} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code int}
     * @return the sum of {@code int} values
     */
    public int sum(final ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper).sum();
    }

    /**
     * Calculates the sum of {@code long} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code long}
     * @return the sum of {@code long} values
     */
    public long sum(final ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper).sum();
    }

    /**
     * Calculates the sum of {@code double} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code double}
     * @return the sum of {@code double} values
     */
    public double sum(final ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper).sum();
    }

    /**
     * Calculates the sum of {@link Number} values.
     *
     * @param <R>    the type of {@link Number}
     * @param mapper a {@link Function} to convert objects to {@link Number}
     * @return the sum as {@link BigDecimal}. If the stream is empty, returns {@link BigDecimal#ZERO}.
     */
    public <R extends Number> BigDecimal sum(final Function<? super T, R> mapper) {
        return stream.map(mapper).reduce(BigDecimal.ZERO, MathKit::add, MathKit::add);
    }

    /**
     * Calculates the average of {@link BigDecimal} values, retaining 2 decimal places with half-up rounding.
     *
     * @param mapper a {@link Function} to convert objects to {@link BigDecimal}
     * @return an {@link Optional} containing the calculated average. If the stream is empty, returns
     *         {@link Optional#empty()}.
     */
    public Optional<BigDecimal> avg(final Function<? super T, BigDecimal> mapper) {
        return avg(mapper, 2);
    }

    /**
     * Calculates the average of {@link BigDecimal} values, retaining a specified number of decimal places with half-up
     * rounding.
     *
     * @param mapper a {@link Function} to convert objects to {@link BigDecimal}
     * @param scale  the number of decimal places to retain
     * @return an {@link Optional} containing the calculated average. If the stream is empty, returns
     *         {@link Optional#empty()}.
     */
    public Optional<BigDecimal> avg(final Function<? super T, BigDecimal> mapper, final int scale) {
        return avg(mapper, scale, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the average of {@link BigDecimal} values with specified scale and rounding mode.
     *
     * @param mapper       a {@link Function} to convert objects to {@link BigDecimal}
     * @param scale        the number of decimal places to retain
     * @param roundingMode the rounding mode to apply
     * @return an {@link Optional} containing the calculated average. If the stream is empty, returns
     *         {@link Optional#empty()}.
     */
    public Optional<BigDecimal> avg(
            final Function<? super T, BigDecimal> mapper,
            final int scale,
            final RoundingMode roundingMode) {
        // List of elements
        final List<BigDecimal> bigDecimalList = stream.map(mapper).collect(Collectors.toList());
        if (CollKit.isEmpty(bigDecimalList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                EasyStream.of(bigDecimalList).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(MathKit.toBigDecimal(bigDecimalList.size()), scale, roundingMode));
    }

    /**
     * Calculates the average of {@code int} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code int}
     * @return an {@link OptionalDouble} containing the calculated average. If the stream is empty, returns
     *         {@link OptionalDouble#empty()}.
     */
    public OptionalDouble avg(final ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper).average();
    }

    /**
     * Calculates the average of {@code double} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code double}
     * @return an {@link OptionalDouble} containing the calculated average. If the stream is empty, returns
     *         {@link OptionalDouble#empty()}.
     */
    public OptionalDouble avg(final ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper).average();
    }

    /**
     * Calculates the average of {@code long} values.
     *
     * @param mapper a {@link Function} to convert objects to {@code long}
     * @return an {@link OptionalDouble} containing the calculated average. If the stream is empty, returns
     *         {@link OptionalDouble#empty()}.
     */
    public OptionalDouble avg(final ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper).average();
    }

    /**
     * A builder for {@code EasyStream} instances.
     *
     * @param <T> the type of the elements
     */
    public interface Builder<T> extends Consumer<T>, org.miaixz.bus.core.Builder<EasyStream<T>> {

        /**
         * Adds an element to the object being built.
         *
         * @param t the element to add
         * @return this builder
         * @throws IllegalStateException if the builder has already transitioned to the built state
         */
        default Builder<T> add(final T t) {
            accept(t);
            return this;
        }
    }

}

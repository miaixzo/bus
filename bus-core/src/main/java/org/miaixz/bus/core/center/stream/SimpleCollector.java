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

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A simple implementation of the {@link Collector} interface.
 *
 * @param <T> the type of input elements to the reduction operation
 * @param <A> the mutable accumulation type of the reduction operation (often hidden as an implementation detail)
 * @param <R> the result type of the reduction operation
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleCollector<T, A, R> implements Collector<T, A, R> {

    /**
     * A function that creates a new mutable result container.
     */
    private final Supplier<A> supplier;
    /**
     * A function that folds a value into a mutable result container.
     */
    private final BiConsumer<A, T> accumulator;
    /**
     * A function that accepts two partial results and merges them. This is used in parallel streams to combine results
     * from different threads.
     */
    private final BinaryOperator<A> combiner;
    /**
     * A function that performs the final transformation from the intermediate accumulation type A to the final result
     * type R.
     */
    private final Function<A, R> finisher;
    /**
     * A {@code Set} of {@link Characteristics} indicating properties of the Collector.
     * <ul>
     * <li>{@code CONCURRENT}: Indicates that this collector is concurrent, meaning that the result container can be
     * modified by multiple threads simultaneously. If a collector is concurrent, then the combiner function may be
     * called to merge results from different threads. If a collector is concurrent and not {@code UNORDERED}, then it
     * is only guaranteed to preserve the encounter order of input elements if the collector is also
     * {@code IDENTITY_FINISH}.</li>
     * <li>{@code UNORDERED}: Indicates that the collection performed by this collector does not enforce any encounter
     * order of the input elements.</li>
     * <li>{@code IDENTITY_FINISH}: Indicates that the finisher function is an identity function and can be omitted. In
     * this case, the accumulator also serves as the finisher, and the intermediate accumulation type A is the same as
     * the result type R.</li>
     * </ul>
     */
    private final Set<Characteristics> characteristics;

    /**
     * Constructs a {@code SimpleCollector} with the specified supplier, accumulator, combiner, finisher, and
     * characteristics.
     *
     * @param supplier        a function that creates a new mutable result container
     * @param accumulator     a function that folds a value into a mutable result container
     * @param combiner        a function that accepts two partial results and merges them
     * @param finisher        a function that performs the final transformation from the intermediate accumulation type
     *                        A to the final result type R
     * @param characteristics a {@code Set} of {@link Characteristics} indicating properties of the Collector
     */
    public SimpleCollector(final Supplier<A> supplier, final BiConsumer<A, T> accumulator,
            final BinaryOperator<A> combiner, final Function<A, R> finisher,
            final Set<Characteristics> characteristics) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    /**
     * Constructs a {@code SimpleCollector} with the specified supplier, accumulator, combiner, and characteristics. The
     * finisher function is assumed to be an identity function ({@code IDENTITY_FINISH}).
     *
     * @param supplier        a function that creates a new mutable result container
     * @param accumulator     a function that folds a value into a mutable result container
     * @param combiner        a function that accepts two partial results and merges them
     * @param characteristics a {@code Set} of {@link Characteristics} indicating properties of the Collector
     */
    public SimpleCollector(final Supplier<A> supplier, final BiConsumer<A, T> accumulator,
            final BinaryOperator<A> combiner, final Set<Characteristics> characteristics) {
        this(supplier, accumulator, combiner, i -> (R) i, characteristics);
    }

    /**
     * Returns a {@code BiConsumer} that folds a value into a mutable result container.
     *
     * @return the accumulator function
     */
    @Override
    public BiConsumer<A, T> accumulator() {
        return accumulator;
    }

    /**
     * Returns a {@code Supplier} that creates and returns a new mutable result container.
     *
     * @return the supplier function
     */
    @Override
    public Supplier<A> supplier() {
        return supplier;
    }

    /**
     * Returns a {@code BinaryOperator} that accepts two partial results and merges them.
     *
     * @return the combiner function
     */
    @Override
    public BinaryOperator<A> combiner() {
        return combiner;
    }

    /**
     * Returns a {@code Function} that performs the final transformation from the intermediate accumulation type A to
     * the final result type R.
     *
     * @return the finisher function
     */
    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    /**
     * Returns a {@code Set} of {@link Characteristics} indicating properties of the Collector.
     *
     * @return the characteristics of this collector
     */
    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }

}

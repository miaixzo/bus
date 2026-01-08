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
package org.miaixz.bus.core.center.stream.spliterators;

import java.util.Comparator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A {@link Spliterator} implementation that drops elements from the beginning of a stream as long as a given predicate
 * holds true. Once the predicate returns {@code false}, all subsequent elements are returned. This implementation is
 * inspired by StreamEx.
 *
 * @param <T> the type of elements returned by this Spliterator
 * @author Kimi Liu
 * @since Java 17+
 */
public class DropWhileSpliterator<T> implements Spliterator<T> {

    /**
     * The source Spliterator from which elements are obtained.
     */
    private final Spliterator<T> source;
    /**
     * The predicate to apply to elements. Elements are dropped as long as this predicate is true.
     */
    private final Predicate<? super T> predicate;
    /**
     * A flag indicating whether the first element for which the predicate returned false has been found.
     */
    private boolean isFound = false;

    /**
     * Constructs a {@code DropWhileSpliterator}.
     *
     * @param source    the source {@link Spliterator}
     * @param predicate the predicate to determine which elements to drop
     */
    private DropWhileSpliterator(final Spliterator<T> source, final Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /**
     * Creates a new {@code DropWhileSpliterator}.
     *
     * @param source    the source {@link Spliterator}
     * @param predicate the predicate to determine which elements to drop
     * @param <T>       the type of elements
     * @return a new {@code DropWhileSpliterator} instance
     */
    public static <T> DropWhileSpliterator<T> of(final Spliterator<T> source, final Predicate<? super T> predicate) {
        return new DropWhileSpliterator<>(source, predicate);
    }

    /**
     * If a remaining element exists, performs the given action on it, returning {@code true} if one existed, else
     * {@code false}. Elements are dropped from the beginning of the stream as long as the predicate holds true. Once
     * the predicate returns {@code false} for an element, that element and all subsequent elements are processed.
     *
     * @param action The action to perform
     * @return {@code false} if no remaining elements existed upon entry to this method, else {@code true}.
     */
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        boolean hasNext = true;
        // Continue searching as long as the first non-matching element hasn't been found and there are more elements in
        // the source.
        while (!isFound && hasNext) {
            hasNext = source.tryAdvance(e -> {
                if (!predicate.test(e)) {
                    // First non-matching element found
                    isFound = true;
                    action.accept(e);
                }
            });
        }

        // If the first non-matching element was found, process the rest of the elements.
        if (isFound) {
            source.forEachRemaining(action);
        }

        // This stage of processing is complete.
        return false;
    }

    /**
     * Attempts to partition the source Spliterator into two. This implementation does not support splitting.
     *
     * @return {@code null} as this Spliterator does not support splitting
     */
    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    /**
     * Returns an estimate of the number of elements that would be encountered by a {@link #forEachRemaining(Consumer)}
     * traversal, or a negative value if infinite, unknown, or too expensive to compute.
     *
     * @return the estimated number of elements, or {@link Long#MAX_VALUE} if infinite
     */
    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    /**
     * Returns a set of characteristics of this Spliterator and its elements.
     *
     * @return a set of characteristics
     */
    @Override
    public int characteristics() {
        return source.characteristics() & ~Spliterator.SIZED;
    }

    /**
     * If this Spliterator's source is {@link Spliterator#SORTED}, returns an {@link Optional} containing the
     * {@link Comparator} that maintains the sort order. Otherwise, returns {@link Optional#empty()}.
     *
     * @return an {@link Optional} containing the {@link Comparator} or {@link Optional#empty()} if not sorted
     */
    @Override
    public Comparator<? super T> getComparator() {
        return source.getComparator();
    }

}

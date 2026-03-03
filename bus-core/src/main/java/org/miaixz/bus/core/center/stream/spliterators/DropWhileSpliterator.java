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

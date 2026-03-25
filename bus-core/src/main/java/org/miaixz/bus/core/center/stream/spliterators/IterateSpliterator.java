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

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A {@link Spliterator} for infinite ordered streams.
 *
 * @param <T> the type of elements returned by this Spliterator
 * @author Kimi Liu
 * @since Java 21+
 */
public class IterateSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

    /**
     * The initial value of the iteration.
     */
    private final T seed;
    /**
     * A predicate to determine if there are more elements.
     */
    private final Predicate<? super T> hasNext;
    /**
     * A unary operator to produce the next element from the previous one.
     */
    private final UnaryOperator<T> next;
    /**
     * The previous element in the iteration.
     */
    private T prev;
    /**
     * Flag indicating if the iteration has started.
     */
    private boolean started;
    /**
     * Flag indicating if the iteration has finished.
     */
    private boolean finished;

    /**
     * Constructs an {@code IterateSpliterator}.
     *
     * @param seed    the initial value
     * @param hasNext a predicate to determine if there are more elements
     * @param next    a unary operator to produce the next element from the previous one
     */
    IterateSpliterator(final T seed, final Predicate<? super T> hasNext, final UnaryOperator<T> next) {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        this.seed = seed;
        this.hasNext = hasNext;
        this.next = next;
    }

    /**
     * Creates an {@code IterateSpliterator}.
     *
     * @param seed    the initial value
     * @param hasNext a predicate to determine if there are more elements
     * @param next    a unary operator to produce the next element from the previous one
     * @param <T>     the type of elements returned by this Spliterator
     * @return a new {@code IterateSpliterator} instance
     */
    public static <T> IterateSpliterator<T> of(
            final T seed,
            final Predicate<? super T> hasNext,
            final UnaryOperator<T> next) {
        return new IterateSpliterator<>(seed, hasNext, next);
    }

    /**
     * If a remaining element exists, performs the given action on it, returning {@code true} if one existed, else
     * {@code false}.
     *
     * @param action The action to perform
     * @return {@code false} if no remaining elements existed upon entry to this method, else {@code true}.
     */
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (finished) {
            return false;
        }
        final T t;
        if (started) {
            t = next.apply(prev);
        } else {
            t = seed;
            started = true;
        }
        if (!hasNext.test(t)) {
            prev = null;
            finished = true;
            return false;
        }
        prev = t;
        action.accept(prev);
        return true;
    }

    /**
     * Performs the given action for each remaining element, sequentially in the current thread, until all elements have
     * been processed or the action throws an exception.
     *
     * @param action The action to perform
     */
    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (finished) {
            return;
        }
        finished = true;
        T t = started ? next.apply(prev) : seed;
        prev = null;
        while (hasNext.test(t)) {
            action.accept(t);
            t = next.apply(t);
        }
    }

}

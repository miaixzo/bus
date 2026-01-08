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
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link BiPredicate} interface that supports throwing exceptions and logical combination operations.
 *
 * @param <T> The type of the first input to the predicate.
 * @param <U> The type of the second input to the predicate.
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiPredicateX<T, U> extends BiPredicate<T, U>, Serializable {

    /**
     * Evaluates this predicate on the given arguments, potentially throwing an exception.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     * @return {@code true} if the input arguments match the predicate, otherwise {@code false}.
     * @throws Throwable Any throwable exception that might occur during the evaluation.
     */
    boolean testing(T t, U u) throws Throwable;

    /**
     * Evaluates this predicate on the given arguments, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     * @return {@code true} if the input arguments match the predicate, otherwise {@code false}.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default boolean test(final T t, final U u) {
        try {
            return testing(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another. If
     * this predicate is {@code false}, the other predicate is not evaluated.
     *
     * @param other A predicate that will be logically ANDed with this predicate.
     * @return A composed predicate that represents a short-circuiting logical AND of this predicate and the
     *         {@code other} predicate.
     * @throws NullPointerException If {@code other} is {@code null}.
     */
    default BiPredicateX<T, U> and(final BiPredicateX<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) && other.test(t, u);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return A predicate that represents the logical negation of this predicate.
     */
    @Override
    default BiPredicateX<T, U> negate() {
        return (T t, U u) -> !test(t, u);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another. If this
     * predicate is {@code true}, the other predicate is not evaluated.
     *
     * @param other A predicate that will be logically ORed with this predicate.
     * @return A composed predicate that represents a short-circuiting logical OR of this predicate and the
     *         {@code other} predicate.
     * @throws NullPointerException If {@code other} is {@code null}.
     */
    default BiPredicateX<T, U> or(final BiPredicateX<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) || other.test(t, u);
    }

}

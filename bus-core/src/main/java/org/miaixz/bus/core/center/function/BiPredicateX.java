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

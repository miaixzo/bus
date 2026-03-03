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
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link Predicate} interface that supports throwing exceptions and logical combination operations.
 *
 * @param <T> The type of the input to the predicate.
 * @author Kimi Liu
 * @see Predicate
 * @since Java 17+
 */
@FunctionalInterface
public interface PredicateX<T> extends Predicate<T>, Serializable {

    /**
     * Combines multiple {@code PredicateX} instances to perform a short-circuiting logical AND operation.
     *
     * @param predicates An array of {@code PredicateX} instances to combine.
     * @param <T>        The type of the input to the predicate.
     * @return A combined {@code PredicateX} instance that performs a short-circuiting logical AND.
     */
    @SafeVarargs
    static <T> PredicateX<T> multiAnd(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * Combines multiple {@code PredicateX} instances to perform a short-circuiting logical OR operation.
     *
     * @param predicates An array of {@code PredicateX} instances to combine.
     * @param <T>        The type of the input to the predicate.
     * @return A combined {@code PredicateX} instance that performs a short-circuiting logical OR.
     */
    @SafeVarargs
    static <T> PredicateX<T> multiOr(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * Returns a predicate that tests if the input argument is equal to the target object.
     *
     * @param <T>       The type of the input to the predicate.
     * @param targetRef The target object reference for comparison, which may be null.
     * @return A {@code PredicateX} that tests if the input argument is equal to the target object.
     */
    static <T> PredicateX<T> isEqual(final Object... targetRef) {
        return (null == targetRef) ? Objects::isNull
                : object -> Stream.of(targetRef).allMatch(target -> target.equals(object));
    }

    /**
     * Evaluates this predicate on the given argument, potentially throwing an exception.
     *
     * @param t The input argument.
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}.
     * @throws Throwable Any throwable exception that might occur during the evaluation.
     */
    boolean testing(T t) throws Throwable;

    /**
     * Evaluates this predicate on the given argument, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The input argument.
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default boolean test(final T t) {
        try {
            return testing(t);
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
    default PredicateX<T> and(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return A predicate that represents the logical negation of this predicate.
     */
    @Override
    default PredicateX<T> negate() {
        return t -> !test(t);
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
    default PredicateX<T> or(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

}

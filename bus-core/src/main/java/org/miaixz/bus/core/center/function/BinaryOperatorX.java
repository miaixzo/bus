/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link BinaryOperator} interface that supports throwing exceptions and operations based on comparators
 * for finding minimum and maximum elements.
 *
 * @param <T> The type of the operands and result of the operator.
 * @author Kimi Liu
 * @see BinaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface BinaryOperatorX<T> extends BinaryOperator<T>, Serializable {

    /**
     * Returns a {@code BinaryOperatorX} that returns the lesser of two elements according to the specified comparator.
     *
     * @param <T>        The type of the input parameters.
     * @param comparator A {@link Comparator} to compare the two values.
     * @return A {@code BinaryOperatorX} that returns the lesser element.
     * @throws NullPointerException If the comparator is {@code null}.
     */
    static <T> BinaryOperatorX<T> minBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Returns a {@code BinaryOperatorX} that returns the greater of two elements according to the specified comparator.
     *
     * @param <T>        The type of the input parameters.
     * @param comparator A {@link Comparator} to compare the two values.
     * @return A {@code BinaryOperatorX} that returns the greater element.
     * @throws NullPointerException If the comparator is {@code null}.
     */
    static <T> BinaryOperatorX<T> maxBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns a {@code BinaryOperatorX} that always returns the first argument.
     *
     * @param <T> The type of the parameters.
     * @return A {@code BinaryOperatorX} that returns the first argument.
     */
    static <T> BinaryOperatorX<T> justBefore() {
        return (l, r) -> l;
    }

    /**
     * Returns a {@code BinaryOperatorX} that always returns the second argument.
     *
     * @param <T> The type of the parameters.
     * @return A {@code BinaryOperatorX} that returns the second argument.
     */
    static <T> BinaryOperatorX<T> justAfter() {
        return (l, r) -> r;
    }

    /**
     * Applies this operation to the given arguments, potentially throwing an exception.
     *
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The result of the operation.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    T applying(T t, T u) throws Throwable;

    /**
     * Applies this operation to the given arguments, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The result of the operation.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default T apply(final T t, final T u) {
        try {
            return this.applying(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}

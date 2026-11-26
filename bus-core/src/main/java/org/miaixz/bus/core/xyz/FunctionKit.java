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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.center.function.*;

/**
 * Functional operations.
 *
 * @author Kimi Liu
 */
public class FunctionKit {

    /**
     * Constructs a new FunctionKit instance.
     */
    public FunctionKit() {
        // No initialization required.
    }

    /**
     * Gets a value from the supplier.
     * <p>
     * Returns {@code null} when the supplier is {@code null}.
     * </p>
     *
     * @param supplier SupplierX function.
     * @param <T>      Value type.
     * @return The supplied value, or {@code null}.
     */
    public static <T> T get(final SupplierX<T> supplier) {
        return supplier == null ? null : supplier.get();
    }

    /**
     * Gets the supplied value and propagates the original failure unchanged.
     *
     * @param supplier Value supplier.
     * @param <T>      Value type.
     * @return The supplied value.
     */
    public static <T> T getUnwrapped(final SupplierX<T> supplier) {
        try {
            return supplier.getting();
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Runs the task and propagates the original failure unchanged.
     *
     * @param task Task to run.
     */
    public static void runUnwrapped(final RunnableX task) {
        try {
            task.running();
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Applies the unary operator.
     * <p>
     * Returns {@code null} when the operator is {@code null}.
     * </p>
     *
     * @param operator Unary operator.
     * @param t        Input argument.
     * @param <T>      Value type.
     * @return The result value, or {@code null}.
     */
    public static <T> T apply(final UnaryOperatorX<T> operator, final T t) {
        return operator == null ? null : operator.apply(t);
    }

    /**
     * Applies the unary operator and propagates the original failure unchanged.
     *
     * @param operator Operator to apply.
     * @param value    Input argument.
     * @param <T>      Operand and result type.
     * @return The result.
     */
    public static <T> T applyUnaryUnwrapped(final UnaryOperatorX<T> operator, final T value) {
        try {
            return operator.applying(value);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Applies the binary operator and propagates the original failure unchanged.
     *
     * @param operator Operator to apply.
     * @param first    First operand.
     * @param second   Second operand.
     * @param <T>      Operand and result type.
     * @return The result.
     */
    public static <T> T applyBinaryUnwrapped(final BinaryOperatorX<T> operator, final T first, final T second) {
        try {
            return operator.applying(first, second);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Applies the function.
     * <p>
     * Returns {@code null} when the function is {@code null}.
     * </p>
     *
     * @param function FunctionX.
     * @param t        Input argument.
     * @param <T>      Input type.
     * @param <R>      Result type.
     * @return The result value, or {@code null}.
     */
    public static <T, R> R apply(final FunctionX<T, R> function, final T t) {
        return function == null ? null : function.apply(t);
    }

    /**
     * Applies the function and propagates the original failure unchanged.
     *
     * @param function Function to apply.
     * @param value    Input argument.
     * @param <T>      Input type.
     * @param <R>      Result type.
     * @return The function result.
     */
    public static <T, R> R applyUnwrapped(final FunctionX<? super T, ? extends R> function, final T value) {
        try {
            return function.applying(value);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Applies the two-argument function and propagates the original failure unchanged.
     *
     * @param function Function to apply.
     * @param first    First input argument.
     * @param second   Second input argument.
     * @param <T>      First input type.
     * @param <U>      Second input type.
     * @param <R>      Result type.
     * @return The function result.
     */
    public static <T, U, R> R applyUnwrapped(
            final BiFunctionX<? super T, ? super U, ? extends R> function,
            final T first,
            final U second) {
        try {
            return function.applying(first, second);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Executes the consumer.
     * <p>
     * Does nothing when the consumer is {@code null}.
     * </p>
     *
     * @param consumer ConsumerX.
     * @param t        Input argument.
     * @param <T>      Value type.
     */
    public static <T> void accept(final ConsumerX<T> consumer, final T t) {
        if (consumer != null) {
            consumer.accept(t);
        }
    }

    /**
     * Executes the consumer and propagates the original failure unchanged.
     *
     * @param consumer Consumer to execute.
     * @param value    Input argument.
     * @param <T>      Input type.
     */
    public static <T> void acceptUnwrapped(final ConsumerX<? super T> consumer, final T value) {
        try {
            consumer.accepting(value);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Rethrows the original failure for callers with an unchecked signature.
     *
     * @param failure Failure to propagate.
     * @return Never returns.
     */
    private static RuntimeException rethrow(final Throwable failure) {
        IoKit.sneakyRethrow(failure);
        throw new AssertionError(failure);
    }

    /**
     * Executes the bi-consumer.
     * <p>
     * Does nothing when the consumer is {@code null}.
     * </p>
     *
     * @param consumer Bi-consumer.
     * @param t        First argument.
     * @param u        Second argument.
     * @param <T>      First value type.
     * @param <U>      Second value type.
     */
    public static <T, U> void accept(final BiConsumerX<T, U> consumer, final T t, final U u) {
        if (consumer != null) {
            consumer.accept(t, u);
        }
    }

    /**
     * Executes the two-argument consumer and propagates the original failure unchanged.
     *
     * @param consumer Consumer to execute.
     * @param first    First input argument.
     * @param second   Second input argument.
     * @param <T>      First input type.
     * @param <U>      Second input type.
     */
    public static <T, U> void acceptUnwrapped(
            final BiConsumerX<? super T, ? super U> consumer,
            final T first,
            final U second) {
        try {
            consumer.accepting(first, second);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

    /**
     * Executes the 3-argument consumer.
     * <p>
     * Does nothing when the consumer is {@code null}.
     * </p>
     *
     * @param consumer 3-argument consumer.
     * @param p1       First argument.
     * @param p2       Second argument.
     * @param p3       Third argument.
     * @param <P1>     First argument type.
     * @param <P2>     Second argument type.
     * @param <P3>     Third argument type.
     */
    public static <P1, P2, P3> void accept(
            final Consumer3X<P1, P2, P3> consumer,
            final P1 p1,
            final P2 p2,
            final P3 p3) {
        if (consumer != null) {
            consumer.accept(p1, p2, p3);
        }
    }

    /**
     * Evaluates the predicate.
     * <p>
     * Returns {@code false} when the predicate is {@code null}.
     * </p>
     *
     * @param predicate PredicateX.
     * @param t         Input argument.
     * @param <T>       Value type.
     * @return {@code true} if matched, otherwise {@code false}.
     */
    public static <T> boolean test(final PredicateX<T> predicate, final T t) {
        return predicate != null && predicate.test(t);
    }

    /**
     * Evaluates the bi-predicate.
     * <p>
     * Returns {@code false} when the predicate is {@code null}.
     * </p>
     *
     * @param predicate Bi-predicate.
     * @param t         First argument.
     * @param u         Second argument.
     * @param <T>       First value type.
     * @param <U>       Second value type.
     * @return {@code true} if matched, otherwise {@code false}.
     */
    public static <T, U> boolean test(final BiPredicateX<T, U> predicate, final T t, final U u) {
        return predicate != null && predicate.test(t, u);
    }

    /**
     * Evaluates the two-argument predicate and propagates the original failure unchanged.
     *
     * @param predicate Predicate to evaluate.
     * @param first     First input argument.
     * @param second    Second input argument.
     * @param <T>       First input type.
     * @param <U>       Second input type.
     * @return Whether the inputs satisfy the predicate.
     */
    public static <T, U> boolean testUnwrapped(
            final BiPredicateX<? super T, ? super U> predicate,
            final T first,
            final U second) {
        try {
            return predicate.testing(first, second);
        } catch (final Throwable failure) {
            throw rethrow(failure);
        }
    }

}

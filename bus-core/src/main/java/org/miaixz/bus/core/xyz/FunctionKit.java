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

import java.util.function.*;

import org.miaixz.bus.core.center.function.Consumer3X;

/**
 * Functional operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FunctionKit {

    /**
     * Gets a value from the supplier.
     * <p>
     * Returns {@code null} when the supplier is {@code null}.
     * </p>
     *
     * @param supplier Supplier function.
     * @param <T>      Value type.
     * @return The supplied value, or {@code null}.
     */
    public static <T> T get(final Supplier<T> supplier) {
        return supplier == null ? null : supplier.get();
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
    public static <T> T apply(final UnaryOperator<T> operator, final T t) {
        return operator == null ? null : operator.apply(t);
    }

    /**
     * Applies the function.
     * <p>
     * Returns {@code null} when the function is {@code null}.
     * </p>
     *
     * @param function Function.
     * @param t        Input argument.
     * @param <T>      Input type.
     * @param <R>      Result type.
     * @return The result value, or {@code null}.
     */
    public static <T, R> R apply(final Function<T, R> function, final T t) {
        return function == null ? null : function.apply(t);
    }

    /**
     * Executes the consumer.
     * <p>
     * Does nothing when the consumer is {@code null}.
     * </p>
     *
     * @param consumer Consumer.
     * @param t        Input argument.
     * @param <T>      Value type.
     */
    public static <T> void accept(final Consumer<T> consumer, final T t) {
        if (consumer != null) {
            consumer.accept(t);
        }
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
    public static <T, U> void accept(final BiConsumer<T, U> consumer, final T t, final U u) {
        if (consumer != null) {
            consumer.accept(t, u);
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
     * @param predicate Predicate.
     * @param t         Input argument.
     * @param <T>       Value type.
     * @return {@code true} if matched, otherwise {@code false}.
     */
    public static <T> boolean test(final Predicate<T> predicate, final T t) {
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
    public static <T, U> boolean test(final BiPredicate<T, U> predicate, final T t, final U u) {
        return predicate != null && predicate.test(t, u);
    }

}

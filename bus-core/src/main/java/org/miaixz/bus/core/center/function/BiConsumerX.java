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
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link BiConsumer} interface that supports throwing exceptions and combining multiple consumers.
 *
 * @param <T> The type of the first input argument to the operation.
 * @param <U> The type of the second input argument to the operation.
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiConsumerX<T, U> extends BiConsumer<T, U>, Serializable {

    /**
     * Combines multiple {@code BiConsumerX} instances to be executed in sequence.
     *
     * @param consumers An array of {@code BiConsumerX} instances to combine.
     * @param <T>       The type of the first input argument.
     * @param <U>       The type of the second input argument.
     * @return A combined {@code BiConsumerX} instance that executes the given consumers in order.
     */
    @SafeVarargs
    static <T, U> BiConsumerX<T, U> multi(final BiConsumerX<T, U>... consumers) {
        return Stream.of(consumers).reduce(BiConsumerX::andThen).orElseGet(() -> (o, q) -> {
        });
    }

    /**
     * Returns a no-operation {@code BiConsumerX} that does nothing.
     *
     * @param <T> The type of the first input argument.
     * @param <U> The type of the second input argument.
     * @return A no-operation {@code BiConsumerX} instance.
     */
    static <T, U> BiConsumerX<T, U> nothing() {
        return (l, r) -> {
        };
    }

    /**
     * Performs this operation on the given arguments, potentially throwing an exception.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    void accepting(T t, U u) throws Throwable;

    /**
     * Performs this operation on the given arguments, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default void accept(final T t, final U u) {
        try {
            accepting(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Returns a composed {@code BiConsumerX} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after The operation to perform after this operation.
     * @return A composed {@code BiConsumerX} that performs in sequence this operation followed by the {@code after}
     *         operation.
     * @throws NullPointerException If {@code after} is {@code null}.
     */
    default BiConsumerX<T, U> andThen(final BiConsumerX<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accepting(l, r);
            after.accepting(l, r);
        };
    }

}

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

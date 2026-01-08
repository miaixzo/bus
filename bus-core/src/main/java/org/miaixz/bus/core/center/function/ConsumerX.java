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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link Consumer} interface that supports throwing exceptions and combining multiple consumers.
 *
 * @param <T> The type of the input argument to the operation.
 * @author Kimi Liu
 * @see Consumer
 * @since Java 17+
 */
@FunctionalInterface
public interface ConsumerX<T> extends Consumer<T>, Serializable {

    /**
     * Combines multiple {@code ConsumerX} instances to be executed in sequence.
     *
     * @param consumers An array of {@code ConsumerX} instances to combine.
     * @param <T>       The type of the input argument to the operation.
     * @return A combined {@code ConsumerX} instance that executes the given consumers in order.
     */
    @SafeVarargs
    static <T> ConsumerX<T> multi(final ConsumerX<T>... consumers) {
        return Stream.of(consumers).reduce(ConsumerX::andThen).orElseGet(() -> o -> {
        });
    }

    /**
     * Returns a no-operation {@code ConsumerX} that does nothing.
     *
     * @param <T> The type of the input argument to the operation.
     * @return A no-operation {@code ConsumerX} instance.
     */
    static <T> ConsumerX<T> nothing() {
        return t -> {
        };
    }

    /**
     * Performs this operation on the given argument, potentially throwing an exception.
     *
     * @param t The input argument.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    void accepting(T t) throws Throwable;

    /**
     * Performs this operation on the given argument, handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The input argument.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default void accept(final T t) {
        try {
            accepting(t);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Returns a composed {@code ConsumerX} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after The operation to perform after this operation.
     * @return A composed {@code ConsumerX} that performs in sequence this operation followed by the {@code after}
     *         operation.
     * @throws NullPointerException If {@code after} is {@code null}.
     */
    default ConsumerX<T> andThen(final ConsumerX<? super T> after) {
        Objects.requireNonNull(after);
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }

}

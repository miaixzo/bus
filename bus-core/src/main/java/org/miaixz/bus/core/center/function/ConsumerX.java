/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.function;

import org.miaixz.bus.core.toolkit.ExceptionKit;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 可序列化的Consumer
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @see Consumer
 * @since Java 17+
 */
@FunctionalInterface
public interface ConsumerX<T> extends Consumer<T>, Serializable {

    /**
     * multi
     *
     * @param consumers lambda
     * @param <T>       type
     * @return lambda
     */
    @SafeVarargs
    static <T> ConsumerX<T> multi(final ConsumerX<T>... consumers) {
        return Stream.of(consumers).reduce(ConsumerX::andThen).orElseGet(() -> o -> {
        });
    }

    /**
     * nothing
     *
     * @param <T> type
     * @return nothing
     */
    static <T> ConsumerX<T> nothing() {
        return t -> {
        };
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Exception wrapped checked exception
     */
    void accepting(T t) throws Throwable;

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
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
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ConsumerX<T> andThen(final ConsumerX<? super T> after) {
        Objects.requireNonNull(after);
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }

}

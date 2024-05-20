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

import org.miaixz.bus.core.xyz.ExceptionKit;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * BiConsumerX
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiConsumerX<T, U> extends BiConsumer<T, U>, Serializable {

    /**
     * multi
     *
     * @param consumers lambda
     * @param <T>       type
     * @param <U>       return type
     * @return lambda
     */
    @SafeVarargs
    static <T, U> BiConsumerX<T, U> multi(final BiConsumerX<T, U>... consumers) {
        return Stream.of(consumers).reduce(BiConsumerX::andThen).orElseGet(() -> (o, q) -> {
        });
    }

    /**
     * 什么也不做，用于一些需要传入lambda的方法占位使用
     *
     * @param <T> 参数1类型
     * @param <U> 参数2类型
     * @return 什么也不做
     */
    static <T, U> BiConsumerX<T, U> nothing() {
        return (l, r) -> {
        };
    }

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws Exception wrapped checked exception for easy using
     */
    void accepting(T t, U u) throws Throwable;

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
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
     * Returns a composed {@code SerBiCons} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code SerBiCons} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default BiConsumerX<T, U> andThen(final BiConsumerX<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accepting(l, r);
            after.accepting(l, r);
        };
    }

}

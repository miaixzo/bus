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
package org.miaixz.bus.core.lang.function;

import org.miaixz.bus.core.exception.InternalException;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * 表示接受两个输入参数且不返回结果的操作
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
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
     * 对给定参数执行此操作
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @throws Exception 包装检查异常，方便使用
     */
    void accepting(T t, U u) throws Exception;

    /**
     * 对给定参数执行此操作
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     */
    @Override
    default void accept(final T t, final U u) {
        try {
            accepting(t, u);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回一个组合的{@code BiConsumerX}，它依次执行该操作和{@code after}操作
     * 如果执行任一操作引发异常，则将其传递给组合操作的调用方
     * 如果执行此操作引发异常，则不会执行{@code after}操作
     *
     * @param after 执行该操作后需要执行的操作
     * @return 一个组合的{@code BiConsumerX}，它按顺序执行此操作，然后执行{@code after}操作
     * @throws NullPointerException 如果{@code after}为空
     */
    default BiConsumerX<T, U> andThen(final BiConsumerX<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accepting(l, r);
            after.accepting(l, r);
        };
    }

}

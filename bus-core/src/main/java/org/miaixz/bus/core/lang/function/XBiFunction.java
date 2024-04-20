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
import java.util.function.BiFunction;

/**
 * 接受两个参数并产生结果的函数
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
 * @param <R> 函数结果的类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface XBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 参数1类型
     * @param u 参数2类型
     * @return 函数的结果
     * @throws Exception 包裹已检查的异常
     */
    R applying(T t, U u) throws Exception;

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 参数1类型
     * @param u 参数2类型
     * @return 函数的结果
     */
    @Override
    default R apply(final T t, final U u) {
        try {
            return this.applying(t, u);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回一个复合函数，该函数首先将该函数应用于其输入，然后将{@code after}函数应用于结果。
     * 如果任意一个函数的求值引发异常，则将其传递给组合函数的调用方
     *
     * @param <V>   {@code after}函数和复合函数的输出类型
     * @param after 应用此函数后要应用的函数
     * @return 一个组合函数，它首先应用这个函数，然后应用{@code after}函数
     * @throws NullPointerException 如果after为null
     */
    default <V> XBiFunction<T, U, V> andThen(final XFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(this.apply(t, u));
    }

}


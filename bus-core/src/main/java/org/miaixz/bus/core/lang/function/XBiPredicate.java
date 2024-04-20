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
import java.util.function.BiPredicate;

/**
 * 表示两个参数(布尔值函数)。这就是谓词的二元专门化
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface XBiPredicate<T, U> extends BiPredicate<T, U>, Serializable {

    /**
     * 根据给定的参数评估
     *
     * @param t 参数1类型
     * @param u 参数2类型
     * @return {@code true} 如果输入参数匹配谓词，否则 {@code false}
     * @throws Exception 包装的检查异常
     */
    boolean testing(T t, U u) throws Exception;

    /**
     * 根据给定的参数评估
     *
     * @param t 参数1类型
     * @param u 参数2类型
     * @return 包装的检查异常
     */
    @Override
    default boolean test(final T t, final U u) {
        try {
            return testing(t, u);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }


    /**
     * 返回一个组合断言，该断言表示此断言与另一个断言的短路逻辑与
     * 在评估组合谓词时，如果此断言为 {@code false}，则不评估 {@code other} 断言
     *
     * @param other 将与该断言进行逻辑与运算的断言
     * @return 一个组合断言，表示此断言与 {@code other} 断言的短路逻辑与
     * @throws NullPointerException if other is null
     */
    default XBiPredicate<T, U> and(final XBiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) && other.test(t, u);
    }

    /**
     * 返回表示此断言的逻辑否定的断言
     *
     * @return 表示此断言的逻辑否定的断言
     */
    @Override
    default XBiPredicate<T, U> negate() {
        return (T t, U u) -> !test(t, u);
    }

    /**
     * 返回一个组合断言，该断言表示此谓词与另一个断言的短路逻辑或
     * 在评估组合断言时，如果此断言为 {@code true}，则不评估 {@code other} 断言
     *
     * @param other 将与此断言进行逻辑或的断言
     * @return 表示此断言与 {@code other} 断言的短路逻辑 OR 的组合断言
     * @throws NullPointerException 如果其他为空
     */
    default XBiPredicate<T, U> or(final XBiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) || other.test(t, u);
    }

}

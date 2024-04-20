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
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * 表示对两个相同类型的操作数进行操作，产生与操作数相同类型的结果
 * 这是XBiFunction的专门化，用于操作数和结果都是相同类型的情况
 *
 * @param <T> 操作数的类型和运算符的结果
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface XBinaryOperator<T> extends BinaryOperator<T>, Serializable {

    /**
     * 返回一个{@link XBinaryOperator}，根据指定的{@code Comparator}返回两个元素中较小的那个
     *
     * @param <T>        比较器的输入参数的类型
     * @param comparator 一个用于比较两个值的{@code Comparator}
     * @return 根据提供的{@code Comparator}返回其操作数的较小的部分
     * @throws NullPointerException 如果参数为null
     */
    static <T> XBinaryOperator<T> minBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * 返回一个{@link XBinaryOperator}，根据指定的{@code Comparator}返回两个元素中较大的那个.
     *
     * @param <T>        比较器的输入参数的类型
     * @param comparator 一个用于比较两个值的{@code Comparator}
     * @return 根据提供的{@code Comparator}返回较大的操作数
     * @throws NullPointerException 如果参数为null
     */
    static <T> XBinaryOperator<T> maxBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    /**
     * 比较之前的操作
     *
     * @param <T> 比较器的输入参数的类型
     * @return the object
     */
    static <T> XBinaryOperator<T> justBefore() {
        return (l, r) -> l;
    }

    /**
     * 比较之后的操作
     *
     * @param <T> 比较器的输入参数的类型
     * @return the object
     */
    static <T> XBinaryOperator<T> justAfter() {
        return (l, r) -> r;
    }

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 函数的第一个参数
     * @param u 函数的第二个参数
     * @return 函数的结果
     * @throws Exception 包裹已检查的异常
     */
    T applying(T t, T u) throws Exception;

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 函数的第一个参数
     * @param u 函数的第二个参数
     * @return 函数的结果
     */
    @Override
    default T apply(final T t, final T u) {
        try {
            return this.applying(t, u);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

}


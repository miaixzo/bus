/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的BinaryOperator接口，支持异常抛出和基于比较器的最大最小值操作。
 *
 * @param <T> 参数和返回值类型
 * @author Kimi Liu
 * @see BinaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface BinaryOperatorX<T> extends BinaryOperator<T>, Serializable {

    /**
     * 返回一个BinaryOperatorX，根据指定的比较器返回两个元素中较小的那个。
     *
     * @param <T>        输入参数类型
     * @param comparator 用于比较两个值的比较器
     * @return BinaryOperatorX，返回较小的元素
     * @throws NullPointerException 如果比较器为null
     */
    static <T> BinaryOperatorX<T> minBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * 返回一个BinaryOperatorX，根据指定的比较器返回两个元素中较大的那个。
     *
     * @param <T>        输入参数类型
     * @param comparator 用于比较两个值的比较器
     * @return BinaryOperatorX，返回较大的元素
     * @throws NullPointerException 如果比较器为null
     */
    static <T> BinaryOperatorX<T> maxBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    /**
     * 返回一个BinaryOperatorX，始终返回第一个参数。
     *
     * @param <T> 参数类型
     * @return BinaryOperatorX，返回第一个参数
     */
    static <T> BinaryOperatorX<T> justBefore() {
        return (l, r) -> l;
    }

    /**
     * 返回一个BinaryOperatorX，始终返回第二个参数。
     *
     * @param <T> 参数类型
     * @return BinaryOperatorX，返回第二个参数
     */
    static <T> BinaryOperatorX<T> justAfter() {
        return (l, r) -> r;
    }

    /**
     * 对给定的两个参数应用此操作，可能抛出异常。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 操作结果
     * @throws Throwable 可能抛出的异常
     */
    T applying(T t, T u) throws Throwable;

    /**
     * 对给定的两个参数应用此操作，自动处理异常。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 操作结果
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default T apply(final T t, final T u) {
        try {
            return this.applying(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}
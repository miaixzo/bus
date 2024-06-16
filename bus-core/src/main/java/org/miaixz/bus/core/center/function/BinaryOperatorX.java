/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.ExceptionKit;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * BinaryOperatorX
 *
 * @param <T> 参数和返回值类型
 * @author Kimi Liu
 * @see BinaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface BinaryOperatorX<T> extends BinaryOperator<T>, Serializable {

    /**
     * Returns a {@code BinaryOperatorX} which returns the lesser of two elements
     * according to the specified {@code Comparator}.
     *
     * @param <T>        the type of the input arguments of the compare
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code SerBiUnOp} which returns the lesser of its operands,
     * according to the supplied {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> BinaryOperatorX<T> minBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Returns a {@code BinaryOperatorX} which returns the greater of two elements
     * according to the specified {@code Comparator}.
     *
     * @param <T>        the type of the input arguments of the compare
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code SerBiUnOp} which returns the greater of its operands,
     * according to the supplied {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> BinaryOperatorX<T> maxBy(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    /**
     * just before
     *
     * @param <T> type
     * @return before
     */
    static <T> BinaryOperatorX<T> justBefore() {
        return (l, r) -> l;
    }

    /**
     * just after
     *
     * @param <T> type
     * @return after
     */
    static <T> BinaryOperatorX<T> justAfter() {
        return (l, r) -> r;
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws Exception wrapped checked exception
     */
    T applying(T t, T u) throws Throwable;

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
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


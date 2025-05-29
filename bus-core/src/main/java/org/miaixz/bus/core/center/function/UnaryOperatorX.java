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
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的UnaryOperator接口，支持异常抛出和类型转换操作。
 *
 * @param <T> 参数和返回值类型
 * @author Kimi Liu
 * @see UnaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface UnaryOperatorX<T> extends UnaryOperator<T>, Serializable {

    /**
     * 返回一个恒等UnaryOperator，始终返回输入参数。
     *
     * @param <T> 输入和输出的类型
     * @return 恒等UnaryOperator
     */
    static <T> UnaryOperatorX<T> identity() {
        return t -> t;
    }

    /**
     * 返回一个支持类型转换的UnaryOperator。
     *
     * @param function 源函数
     * @param <T>      输入参数类型
     * @param <R>      返回值类型
     * @param <F>      函数类型
     * @return 类型转换后的UnaryOperator
     */
    static <T, R, F extends Function<T, R>> UnaryOperatorX<T> casting(final F function) {
        return t -> (T) function.apply(t);
    }

    /**
     * 对给定参数应用此操作，可能抛出异常。
     *
     * @param t 输入参数
     * @return 操作结果
     * @throws Throwable 可能抛出的异常
     */
    T applying(T t) throws Throwable;

    /**
     * 对给定参数应用此操作，自动处理异常。
     *
     * @param t 输入参数
     * @return 操作结果
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default T apply(final T t) {
        try {
            return applying(t);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}
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
import java.util.Objects;
import java.util.function.BiFunction;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的BiFunction接口，支持异常抛出和函数组合。
 *
 * @param <T> 参数1的类型
 * @param <U> 参数2的类型
 * @param <R> 返回值类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiFunctionX<T, U, R> extends BiFunction<T, U, R>, Serializable {

    /**
     * 对给定两个参数应用此函数，可能抛出异常。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 函数执行结果
     * @throws Throwable 可能抛出的异常
     */
    R applying(T t, U u) throws Throwable;

    /**
     * 对给定两个参数应用此函数，自动处理异常。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 函数执行结果
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default R apply(final T t, final U u) {
        try {
            return this.applying(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 返回一个组合函数，先应用此函数，再将结果传递给after函数。
     *
     * @param <V>   after函数输出类型及组合函数的输出类型
     * @param after 在此函数应用后执行的函数
     * @return 组合函数，先应用此函数再应用after函数
     * @throws NullPointerException 如果after为null
     */
    default <V> BiFunctionX<T, U, V> andThen(final FunctionX<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(this.apply(t, u));
    }

}
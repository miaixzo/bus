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
import java.util.function.Function;

/**
 * 表示接受一个参数并产生结果的函数
 *
 * @param <T> 函数的输入类型
 * @param <R> 函数结果的类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface XFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * 返回一个始终返回其输入参数的函数
     *
     * @param <T> 函数的输入和输出对象的类型
     * @return 始终返回其输入参数的函数
     */
    static <T> XFunction<T, T> identity() {
        return t -> t;
    }

    /**
     * 执行函数
     *
     * @param <T> 函数的输入类型
     * @param <R> 函数结果的类型
     * @return 执行后的结果
     */
    static <T, R> Function<T, R> castingIdentity() {
        return t -> (R) t;
    }

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 函数参数
     * @return 函数结果
     * @throws Exception 包装的检查异常
     */
    R applying(T t) throws Exception;

    /**
     * 将此函数应用于给定的参数
     *
     * @param t 函数参数
     * @return 函数结果
     */
    @Override
    default R apply(T t) {
        try {
            return applying(t);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

}

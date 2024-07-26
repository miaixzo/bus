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
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * 可序列化的UnaryOperator
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @see UnaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface UnaryOperatorX<T> extends UnaryOperator<T>, Serializable {

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @param <T> the type of the input and output of the operator
     * @return a unary operator that always returns its input argument
     */
    static <T> UnaryOperatorX<T> identity() {
        return t -> t;
    }

    /**
     * casting identity
     *
     * @param function source function
     * @param <T>      param type
     * @param <R>      result type
     * @param <F>      lambda type
     * @return identity after casting
     */
    static <T, R, F extends Function<T, R>> UnaryOperatorX<T> casting(final F function) {
        return t -> (T) function.apply(t);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Exception wrapped checked exception
     */
    T applying(T t) throws Throwable;

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
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

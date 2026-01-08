/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link Function} interface that supports throwing exceptions.
 *
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 * @author Kimi Liu
 * @see Function
 * @since Java 17+
 */
@FunctionalInterface
public interface FunctionX<T, R> extends Function<T, R>, Serializable {

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> The type of the input and output of the function.
     * @return A function that always returns its input argument.
     */
    static <T> FunctionX<T, T> identity() {
        return t -> t;
    }

    /**
     * Returns an identity function that supports type casting.
     *
     * @param <T> The type of the input argument.
     * @param <R> The type of the return value.
     * @return An identity function with type casting.
     */
    static <T, R> Function<T, R> castingIdentity() {
        return t -> (R) t;
    }

    /**
     * Applies this function to the given argument, potentially throwing an exception.
     *
     * @param t The function input argument.
     * @return The function result.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    R applying(T t) throws Throwable;

    /**
     * Applies this function to the given argument, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The function input argument.
     * @return The function result.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default R apply(final T t) {
        try {
            return applying(t);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link UnaryOperator} interface that supports throwing exceptions and type casting operations.
 *
 * @param <T> The type of the input and output of the operator.
 * @author Kimi Liu
 * @see UnaryOperator
 * @since Java 17+
 */
@FunctionalInterface
public interface UnaryOperatorX<T> extends UnaryOperator<T>, Serializable {

    /**
     * Returns an identity {@code UnaryOperator} that always returns its input argument.
     *
     * @param <T> The type of the input and output.
     * @return An identity {@code UnaryOperator}.
     */
    static <T> UnaryOperatorX<T> identity() {
        return t -> t;
    }

    /**
     * Returns a {@code UnaryOperator} that supports type casting.
     *
     * @param function The source function.
     * @param <T>      The type of the input argument.
     * @param <R>      The type of the return value.
     * @param <F>      The type of the function.
     * @return A {@code UnaryOperator} with type casting.
     */
    static <T, R, F extends Function<T, R>> UnaryOperatorX<T> casting(final F function) {
        return t -> (T) function.apply(t);
    }

    /**
     * Applies this operation to the given argument, potentially throwing an exception.
     *
     * @param t The input argument.
     * @return The result of the operation.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    T applying(T t) throws Throwable;

    /**
     * Applies this operation to the given argument, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @param t The input argument.
     * @return The result of the operation.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
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

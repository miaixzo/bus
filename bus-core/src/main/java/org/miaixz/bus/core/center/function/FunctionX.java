/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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

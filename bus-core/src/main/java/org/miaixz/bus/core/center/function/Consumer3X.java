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
import java.util.Objects;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A functional interface representing a consumer that accepts three arguments. This is a three-arity specialization of
 * {@link java.util.function.Consumer}.
 *
 * @param <P1> The type of the first argument.
 * @param <P2> The type of the second argument.
 * @param <P3> The type of the third argument.
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface Consumer3X<P1, P2, P3> extends Serializable {

    /**
     * Performs this operation on the given arguments, allowing for checked exceptions.
     *
     * @param p1 The first input argument.
     * @param p2 The second input argument.
     * @param p3 The third input argument.
     * @throws Throwable if an error occurs.
     */
    void accepting(P1 p1, P2 p2, P3 p3) throws Throwable;

    /**
     * Performs this operation on the given arguments, wrapping any checked exceptions in a runtime exception.
     *
     * @param p1 The first input argument.
     * @param p2 The second input argument.
     * @param p3 The third input argument.
     */
    default void accept(final P1 p1, final P2 p2, final P3 p3) {
        try {
            accepting(p1, p2, p3);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Returns a composed {@code Consumer3X} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation.
     * @return a composed {@code Consumer3X} that performs in sequence this operation followed by the {@code after}
     *         operation.
     * @throws NullPointerException if {@code after} is null.
     */
    default Consumer3X<P1, P2, P3> andThen(final Consumer3X<P1, P2, P3> after) {
        Objects.requireNonNull(after);
        return (final P1 p1, final P2 p2, final P3 p3) -> {
            accept(p1, p2, p3);
            after.accept(p1, p2, p3);
        };
    }

}

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
 * @since Java 17+
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

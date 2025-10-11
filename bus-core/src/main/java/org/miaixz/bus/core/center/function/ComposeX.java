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

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;

/**
 * A composite function that combines two functions. Composes {@code f: A->B} and {@code g: B->C}, such that the effect
 * is equivalent to: {@code h(a) == g(f(a))}.
 *
 * @param <A> The input parameter type of the first function.
 * @param <B> The return type of the first function (and the input parameter type of the second function).
 * @param <C> The final result type.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ComposeX<A, B, C> implements Function<A, C>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852258708782L;

    /**
     * The second function in the composition, which takes input of type B and returns type C.
     */
    private final Function<B, C> g;
    /**
     * The first function in the composition, which takes input of type A and returns type B.
     */
    private final Function<A, ? extends B> f;

    /**
     * Constructs a {@code ComposeX} instance with the given functions.
     *
     * @param g The second function.
     * @param f The first function.
     */
    public ComposeX(final Function<B, C> g, final Function<A, ? extends B> f) {
        this.g = Assert.notNull(g);
        this.f = Assert.notNull(f);
    }

    /**
     * Creates a composite function that combines two functions. Composes {@code f: A->B} and {@code g: B->C}, such that
     * the effect is equivalent to: {@code h(a) == g(f(a))}.
     *
     * @param g   The second function.
     * @param f   The first function.
     * @param <A> The input parameter type of the first function.
     * @param <B> The return type of the first function (and the input parameter type of the second function).
     * @param <C> The final result type.
     * @return A new {@code ComposeX} instance representing the composite function.
     */
    public static <A, B, C> ComposeX<A, B, C> of(final Function<B, C> g, final Function<A, ? extends B> f) {
        return new ComposeX<>(g, f);
    }

    /**
     * Applies this composite function to the given argument.
     *
     * @param a The input argument of type A.
     * @return The result of applying the composite function, of type C.
     */
    @Override
    public C apply(final A a) {
        return g.apply(f.apply(a));
    }

}

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
 * @since Java 21+
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

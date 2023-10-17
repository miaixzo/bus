/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang.function;

import org.aoju.bus.core.lang.Assert;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 两个函数的叠加函数. 叠加 {@code f: A->B} 和 {@code g: B->C}，效果等同于：{@code h(a) == g(f(a))}
 *
 * @param <A> 第一个函数的传入参数类型
 * @param <B> 第一个函数的返回类型（第二个函数有的参数类型）
 * @param <C> 最终结果类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class XCompose<A, B, C> implements Function<A, C>, Serializable {

    private static final long serialVersionUID = 1L;
    private final Function<B, C> g;
    private final Function<A, ? extends B> f;

    public XCompose(final Function<B, C> g, final Function<A, ? extends B> f) {
        this.g = Assert.notNull(g);
        this.f = Assert.notNull(f);
    }

    /**
     * 两个函数的叠加函数. 叠加 {@code f: A->B} 和 {@code g: B->C}，效果等同于：{@code h(a) == g(f(a))}
     *
     * @param g   第二个函数
     * @param f   第一个函数
     * @param <A> 第一个函数的传入参数类型
     * @param <B> 第一个函数的返回类型（第二个函数有的参数类型）
     * @param <C> 最终结果类型
     * @return 叠加函数
     */
    public static <A, B, C> XCompose<A, B, C> of(final Function<B, C> g, final Function<A, ? extends B> f) {
        return new XCompose<>(g, f);
    }

    @Override
    public C apply(final A a) {
        return g.apply(f.apply(a));
    }

}

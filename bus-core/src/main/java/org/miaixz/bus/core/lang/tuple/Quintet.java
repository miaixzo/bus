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
package org.miaixz.bus.core.lang.tuple;

import org.miaixz.bus.core.annotation.ThreadSafe;

/**
 * 从方法返回多个对象的便利类
 *
 * @param <A> 第一个元素的类型
 * @param <B> 第二个元素的类型
 * @param <C> 第三个元素的类型
 * @param <D> 第四个元素的类型
 * @param <E> 第五个元素的类型
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class Quintet<A, B, C, D, E> {

    private final A a;
    private final B b;
    private final C c;
    private final D d;
    private final E e;

    /**
     * 创建一个五重奏并存储五个对象
     *
     * @param a 要存储的第一个对象
     * @param b 要存储的第二个对象
     * @param c 要存储的第三个对象
     * @param d 要存储的第四个对象
     * @param e 要存储的第五个对象
     */
    public Quintet(A a, B b, C c, D d, E e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    /**
     * 返回第一个存储的对象
     *
     * @return 第一个对象存储
     */
    public final A getA() {
        return a;
    }

    /**
     * 返回第二个存储对象
     *
     * @return 第二个对象存储
     */
    public final B getB() {
        return b;
    }

    /**
     * 返回第三个存储对象
     *
     * @return 第三个对象存储
     */
    public final C getC() {
        return c;
    }

    /**
     * 返回第四个存储对象
     *
     * @return 第四个对象存储
     */
    public final D getD() {
        return d;
    }

    /**
     * 返回第五个存储对象
     *
     * @return 第五个对象存储
     */
    public final E getE() {
        return e;
    }

}
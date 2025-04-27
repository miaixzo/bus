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

import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 3参数Consumer
 *
 * @param <P1> 参数一类型
 * @param <P2> 参数二类型
 * @param <P3> 参数三类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Consumer3X<P1, P2, P3> extends Serializable {

    /**
     * 接收参数方法
     *
     * @param p1 参数一
     * @param p2 参数二
     * @param p3 参数三
     * @throws Exception 包装检查异常
     */
    void accepting(P1 p1, P2 p2, P3 p3) throws Throwable;

    /**
     * 接收参数方法
     *
     * @param p1 参数一
     * @param p2 参数二
     * @param p3 参数三
     */
    default void accept(final P1 p1, final P2 p2, final P3 p3) {
        try {
            accepting(p1, p2, p3);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 返回一个组合的{@link Consumer3X}，按顺序执行此操作，然后执行{@code after}操作。
     * 如果执行任何操作都会抛出异常，则将其传递给组合操作的调用者。如果执行此操作会抛出异常，则不会执行{@code}操作。
     *
     * @param after 后续要要执行的操作
     * @return 一个组合 {@link Consumer3X}，按顺序执行此操作，然后执行{@code after}操作
     * @throws NullPointerException if {@code after} is null
     */
    default Consumer3X<P1, P2, P3> andThen(final Consumer3X<P1, P2, P3> after) {
        Objects.requireNonNull(after);
        return (final P1 p1, final P2 p2, final P3 p3) -> {
            accept(p1, p2, p3);
            after.accept(p1, p2, p3);
        };
    }

}

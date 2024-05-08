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
package org.miaixz.bus.core.lang.function;

import org.miaixz.bus.core.exception.InternalException;

import java.io.Serializable;
import java.util.Objects;

/**
 * 表示接受三个参数且不返回结果的操作
 * 与大多数其他功能接口不同，消费者预计将通过副作用进行操作
 *
 * @param <L> 左元素类型
 * @param <M> 中间元素类型
 * @param <R> 右元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface MultipleX<L, M, R> extends Serializable {

    /**
     * 接收参数方法
     *
     * @param l 左元素类型
     * @param m 中间元素类型
     * @param r 右元素类型
     * @throws Exception w包装的检查异常
     */
    void accepting(L l, M m, R r) throws Exception;

    /**
     * 接收参数方法
     *
     * @param l 左元素类型
     * @param m 中间元素类型
     * @param r 右元素类型
     */
    default void accept(L l, M m, R r) {
        try {
            accepting(l, m, r);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回一个组合的 {@code MultipleX}，它按顺序执行此操作，然后是 {@code after} 操作
     * 如果执行任一操作引发异常，则将其转发给组合操作的调用者
     * 如果执行此操作引发异常，则不会执行 {@code after} 操作
     *
     * @param after 此操作后要执行的操作
     * @return 一个组合的 {@code MultipleX} 按顺序执行此操作，然后是 {@code after} 操作
     * @throws NullPointerException 如果 {@code after} 为空
     */
    default MultipleX<L, M, R> andThen(MultipleX<L, M, R> after) {
        Objects.requireNonNull(after);
        return (L l, M m, R r) -> {
            accept(l, m, r);
            after.accept(l, m, r);
        };
    }

}

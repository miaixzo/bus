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
package org.miaixz.bus.core.lang.mutable;

import org.miaixz.bus.core.lang.tuple.Triplet;

/**
 * 可变三元组对象
 *
 * @param <L> 左值类型
 * @param <M> 中值类型
 * @param <R> 右值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableTriplet<L, M, R> extends Triplet<L, M, R> implements Mutable<MutableTriplet<L, M, R>> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param left   左值
     * @param middle 中值
     * @param right  右值
     */
    public MutableTriplet(final L left, final M middle, final R right) {
        super(left, middle, right);
    }

    /**
     * 构建MutableTriple对象
     *
     * @param <L>    左值类型
     * @param <M>    中值类型
     * @param <R>    右值类型
     * @param left   左值
     * @param middle 中值
     * @param right  右值
     * @return MutableTriplet
     */
    public static <L, M, R> MutableTriplet<L, M, R> of(final L left, final M middle, final R right) {
        return new MutableTriplet<>(left, middle, right);
    }

    @Override
    public MutableTriplet<L, M, R> get() {
        return this;
    }

    @Override
    public void set(final MutableTriplet<L, M, R> value) {
        this.left = value.left;
        this.middle = value.middle;
        this.right = value.right;
    }

    /**
     * 设置左值
     *
     * @param left 左值
     */
    public void setLeft(final L left) {
        this.left = left;
    }

    /**
     * 设置中值
     *
     * @param middle 中值
     */
    public void setMiddle(final M middle) {
        this.middle = middle;
    }

    /**
     * 设置右值
     *
     * @param right 右值
     */
    public void setRight(final R right) {
        this.right = right;
    }

}

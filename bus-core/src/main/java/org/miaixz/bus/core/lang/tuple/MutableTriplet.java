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

/**
 * 由三个{@code Object}元素组成的可变三元组
 *
 * @param <L> 左元素类型
 * @param <M> 中间元素类型
 * @param <R> 左元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableTriplet<L, M, R> extends Triplet<L, M, R> {

    /**
     * 左边对象
     */
    public L left;
    /**
     * 中间对象
     */
    public M middle;
    /**
     * 右边对象
     */
    public R right;

    /**
     * 创建一个包含三个空值的新三元组实例.
     */
    public MutableTriplet() {
        super();
    }

    /**
     * 创建一个新的三元组实例.
     *
     * @param left   左值可以为null
     * @param middle 中间可以为null
     * @param right  右值可以为null
     */
    public MutableTriplet(final L left, final M middle, final R right) {
        super();
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    /**
     * 获取由三个推断泛型类型的对象组成的可变三元组
     *
     * @param <L>    左元素类型
     * @param <M>    中间元素类型
     * @param <R>    右元素类型
     * @param left   左值可以为null
     * @param middle 中间可以为null
     * @param right  右值可以为null
     * @return a triple formed from the three parameters, not null
     */
    public static <L, M, R> MutableTriplet<L, M, R> of(final L left, final M middle, final R right) {
        return new MutableTriplet<>(left, middle, right);
    }

    @Override
    public L getLeft() {
        return left;
    }

    /**
     * 设置对的左元素.
     *
     * @param left 左边元素的新值可以是null
     */
    public void setLeft(final L left) {
        this.left = left;
    }

    @Override
    public M getMiddle() {
        return middle;
    }

    /**
     * 设置对的中间元素.
     *
     * @param middle 中间元素的新值可以是null
     */
    public void setMiddle(final M middle) {
        this.middle = middle;
    }

    @Override
    public R getRight() {
        return right;
    }

    /**
     * 设置对的右元素.
     *
     * @param right 右边元素的新值可以是null
     */
    public void setRight(final R right) {
        this.right = right;
    }

}


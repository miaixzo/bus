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
 * 由两个{@code Object}元素组成的可变对
 *
 * @param <L> 左元素类型
 * @param <R> 左元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutablePair<L, R> extends Pair<L, R> {

    /**
     * 左边对象
     */
    public L left;
    /**
     * 右边对象
     */
    public R right;

    /**
     * 创建一个包含二个空值的实例
     */
    public MutablePair() {
        super();
    }

    /**
     * 创建一个新的pair实例
     *
     * @param left  左值可以为null
     * @param right 右值可以为null
     */
    public MutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    /**
     * 获取两个推断泛型类型的对象的可变对
     *
     * @param <L>   左元素类型
     * @param <R>   右元素类型
     * @param left  左值可以为null
     * @param right 右值可以为null
     * @return 由两个参数组成的一对
     */
    public static <L, R> MutablePair<L, R> of(final L left, final R right) {
        return new MutablePair<>(left, right);
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

    /**
     * 设置{@code Map.Entry}的值.
     * 这将设置对的正确元素
     *
     * @param value 要设置的正确值，而不是null
     * @return 右边元素的旧值
     */
    @Override
    public R setValue(final R value) {
        final R result = getRight();
        setRight(value);
        return result;
    }

}

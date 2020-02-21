/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang.tuple;

import org.aoju.bus.core.builder.CompareBuilder;
import org.aoju.bus.core.lang.Symbol;

import java.io.Serializable;
import java.util.Objects;

/**
 * 由三个元素组成的三元组
 * 这个类是一个定义基本API的抽象实现
 * 表示元素为'left'、'middle'和'right'.
 * 子类实现可以是可变的,也可以是不可变的
 * 但是,对可能存储的存储对象的类型没有限制
 * 如果可变对象存储在三元组中,那么三元组本身就会变成可变的
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
public abstract class Triple<L, M, R> implements Comparable<Triple<L, M, R>>, Serializable {

    /**
     * 获取由三个推断泛型类型的对象组成的不可变三元组
     *
     * @param <L>    左元素类型
     * @param <M>    中间元素类型
     * @param <R>    右元素类型
     * @param left   左值可以为null
     * @param middle 中间可以为null
     * @param right  右值可以为null
     * @return 由三个参数组成的三元组，不为空
     */
    public static <L, M, R> Triple<L, M, R> of(final L left, final M middle, final R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    public abstract L getLeft();

    public abstract M getMiddle();

    public abstract R getRight();

    @Override
    public int compareTo(final Triple<L, M, R> other) {
        return new CompareBuilder().append(getLeft(), other.getLeft())
                .append(getMiddle(), other.getMiddle())
                .append(getRight(), other.getRight()).toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Triple<?, ?, ?>) {
            final Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
            return Objects.equals(getLeft(), other.getLeft())
                    && Objects.equals(getMiddle(), other.getMiddle())
                    && Objects.equals(getRight(), other.getRight());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getLeft() == null ? 0 : getLeft().hashCode()) ^
                (getMiddle() == null ? 0 : getMiddle().hashCode()) ^
                (getRight() == null ? 0 : getRight().hashCode());
    }

    @Override
    public String toString() {
        return Symbol.PARENTHESE_LEFT + getLeft() + Symbol.COMMA + getMiddle() + Symbol.COMMA + getRight() + Symbol.PARENTHESE_RIGHT;
    }

    public String toString(final String format) {
        return String.format(format, getLeft(), getMiddle(), getRight());
    }

}


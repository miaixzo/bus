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
import org.miaixz.bus.core.builder.CompareBuilder;
import org.miaixz.bus.core.lang.Symbol;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 由两个元素组成
 * <p>
 * 这个类是一个定义基本API的抽象实现
 * 它表示元素为“left”和“right” 它还实现了
 * {@code Map.Entry}接口,其中键为'left',值为'right'.
 * 子类实现可以是可变的,也可以是不可变的
 * 但是,对可能存储的存储对象的类型没有限制
 * 如果可变对象存储在对中,那么对本身就会有效地变成可变的
 *
 * @param <L> 左元素类型
 * @param <R> 右元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {

    /**
     * 获取两个推断泛型类型的对象的不可变对
     * 这个工厂允许使用推理来创建对，以获得泛型类型
     *
     * @param <L>   左元素类型
     * @param <R>   右元素类型
     * @param left  左值可以为null
     * @param right 右值可以为null
     * @return 由两个参数组成的一对，不是空
     */
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new ImmutablePair<>(left, right);
    }

    /**
     * 从这一对中获取左元素
     *
     * @return 左边的元素可能是空的
     */
    public abstract L getLeft();

    /**
     * 从这一对中获取右元素
     *
     * @return 右边的元素可能是空的
     */
    public abstract R getRight();

    /**
     * 从这对中获取密钥
     *
     * @return 作为键的左元素可以为空
     */
    @Override
    public final L getKey() {
        return getLeft();
    }

    /**
     * 从这对中获取值
     *
     * @return 右边的元素作为值，可以是null
     */
    @Override
    public R getValue() {
        return getRight();
    }

    /**
     * 比较基于左元素和右元素的对。类型必须是{@code Comparable}
     *
     * @param other 另一对，不为空
     * @return 如果这个小，就是负的;如果相等，就是零;如果大，就是正的
     */
    @Override
    public int compareTo(final Pair<L, R> other) {
        return new CompareBuilder().append(getLeft(), other.getLeft())
                .append(getRight(), other.getRight()).toComparison();
    }

    /**
     * 据这两个元素，将这一对与另一对进行比较
     *
     * @param object 要比较的对象null返回false
     * @return 如果这一对的元素相等，则为true
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Map.Entry<?, ?>) {
            final Map.Entry<?, ?> other = (Map.Entry<?, ?>) object;
            return Objects.equals(getKey(), other.getKey())
                    && Objects.equals(getValue(), other.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (null == getKey() ? 0 : getKey().hashCode()) ^
                (null == getValue() ? 0 : getValue().hashCode());
    }

    @Override
    public String toString() {
        return Symbol.PARENTHESE_LEFT + getLeft() + Symbol.C_COMMA + getRight() + Symbol.C_PARENTHESE_RIGHT;
    }

    public String toString(final String format) {
        return String.format(format, getLeft(), getRight());
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.tuple;

import org.miaixz.bus.core.lang.exception.CloneException;

import java.io.Serializable;
import java.util.Objects;

/**
 * 不可变二元组对象
 *
 * @param <L> 左值类型
 * @param <R> 右值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pair<L, R> implements Serializable, Cloneable {

    private static final long serialVersionUID = -1L;

    /**
     * 左值（第一个值）
     */
    protected L left;
    /**
     * 右值（第二个值）
     */
    protected R right;

    /**
     * 构造
     *
     * @param left  左值
     * @param right 右值
     */
    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 构建Pair对象
     *
     * @param <L>   左值类型
     * @param <R>   右值类型
     * @param left  左值
     * @param right 右值
     * @return Pair
     */
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    /**
     * 获取左值
     *
     * @return 左值
     */
    public L getLeft() {
        return this.left;
    }

    /**
     * 获取右值
     *
     * @return 右值
     */
    public R getRight() {
        return this.right;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Pair{" + "left=" + left + ", right=" + right + '}';
    }

    @Override
    public Pair<L, R> clone() {
        try {
            return (Pair<L, R>) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}

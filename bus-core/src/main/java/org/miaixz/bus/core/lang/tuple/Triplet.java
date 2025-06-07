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
package org.miaixz.bus.core.lang.tuple;

import java.io.Serial;
import java.util.Objects;

/**
 * 不可变三元组对象
 *
 * @param <L> 左值类型
 * @param <M> 中值类型
 * @param <R> 右值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Triplet<L, M, R> extends Pair<L, R> {

    @Serial
    private static final long serialVersionUID = 2852281035663L;

    /**
     * 中值
     */
    protected M middle;

    /**
     * 构造
     *
     * @param left   左值
     * @param middle 中值
     * @param right  右值
     */
    public Triplet(final L left, final M middle, final R right) {
        super(left, right);
        this.middle = middle;
    }

    /**
     * 构建Triple对象
     *
     * @param <L>    左值类型
     * @param <M>    中值类型
     * @param <R>    右值类型
     * @param left   左值
     * @param middle 中值
     * @param right  右值
     * @return Triplet
     */
    public static <L, M, R> Triplet<L, M, R> of(final L left, final M middle, final R right) {
        return new Triplet<>(left, middle, right);
    }

    /**
     * 获取中值
     *
     * @return 中值
     */
    public M getMiddle() {
        return this.middle;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Triplet) {
            final Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
            return Objects.equals(getLeft(), triplet.getLeft()) && Objects.equals(getMiddle(), triplet.getMiddle())
                    && Objects.equals(getRight(), triplet.getRight());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.middle, this.right);
    }

    @Override
    public String toString() {
        return "Triplet{" + "left=" + getLeft() + ", middle=" + getMiddle() + ", right=" + getRight() + '}';
    }

    @Override
    public Triplet<L, M, R> clone() {
        return (Triplet<L, M, R>) super.clone();
    }

}

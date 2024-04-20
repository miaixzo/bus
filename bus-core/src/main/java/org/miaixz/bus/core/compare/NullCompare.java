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
package org.miaixz.bus.core.compare;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * {@code null}友好的比较器包装，如果nullGreater，则{@code null} &gt; non-null，否则反之
 * 如果二者皆为{@code null}，则为相等，返回0
 * 如果二者都非{@code null}，则使用传入的比较器排序
 * 传入比较器为{@code null}，则看被比较的两个对象是否都实现了{@link Comparable}实现则调用{@link Comparable#compareTo(Object)}
 * 如果两者至少一个未实现，则视为所有元素相等
 *
 * @param <T> 被比较的对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullCompare<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final boolean nullGreater;
    protected final Comparator<T> comparator;

    /**
     * 构造
     *
     * @param nullGreater 是否{@code null}最大，排在最后
     * @param comparator  实际比较器
     */
    public NullCompare(boolean nullGreater, Comparator<? super T> comparator) {
        this.nullGreater = nullGreater;
        this.comparator = (Comparator<T>) comparator;
    }

    @Override
    public int compare(T a, T b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return nullGreater ? 1 : -1;
        } else if (b == null) {
            return nullGreater ? -1 : 1;
        } else {
            return doCompare(a, b);
        }
    }

    @Override
    public Comparator<T> thenComparing(Comparator<? super T> other) {
        Objects.requireNonNull(other);
        return new NullCompare<>(nullGreater, comparator == null ? other : comparator.thenComparing(other));
    }

    @Override
    public Comparator<T> reversed() {
        return new NullCompare<>((false == nullGreater), comparator == null ? null : comparator.reversed());
    }

    /**
     * 不检查{@code null}的比较方法
     * 用户可自行重写此方法自定义比较方式
     *
     * @param a A值
     * @param b B值
     * @return 比较结果，-1:a小于b，0:相等，1:a大于b
     */
    protected int doCompare(T a, T b) {
        if (null == comparator) {
            if (a instanceof Comparable && b instanceof Comparable) {
                return ((Comparable) a).compareTo(b);
            }
            return 0;
        }

        return comparator.compare(a, b);
    }

}

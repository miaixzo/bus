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

/**
 * 反转比较器
 *
 * @param <E> 被比较对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReverseCompare<E> implements Comparator<E>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原始比较器
     */
    private final Comparator<? super E> comparator;

    public ReverseCompare(Comparator<? super E> comparator) {
        this.comparator = (null == comparator) ? NormalCompare.INSTANCE : comparator;
    }

    @Override
    public int compare(E o1, E o2) {
        return comparator.compare(o2, o1);
    }

    @Override
    public int hashCode() {
        return "ReverseComparator".hashCode() ^ comparator.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (object.getClass().equals(this.getClass())) {
            final ReverseCompare<?> thatrc = (ReverseCompare<?>) object;
            return comparator.equals(thatrc.comparator);
        }
        return false;
    }

}

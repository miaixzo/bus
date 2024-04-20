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
 * 针对 {@link java.lang.Comparable}对象的默认比较器
 *
 * @param <E> 比较对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class NormalCompare<E extends Comparable<? super E>> implements Comparator<E>, Serializable {

    /**
     * 单例
     */
    public static final NormalCompare INSTANCE = new NormalCompare<>();
    private static final long serialVersionUID = 1L;

    /**
     * 构造
     */
    public NormalCompare() {

    }

    /**
     * 比较两个{@link java.lang.Comparable}对象
     *
     * <pre>
     * obj1.compareTo(obj2)
     * </pre>
     *
     * @param obj1 被比较的第一个对象
     * @param obj2 the second object to compare
     * @return obj1小返回负数, 大返回正数, 否则返回0
     */
    @Override
    public int compare(final E obj1, final E obj2) {
        return obj1.compareTo(obj2);
    }

    @Override
    public int hashCode() {
        return "Comparables".hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return this == object || null != object && object.getClass().equals(this.getClass());
    }

}

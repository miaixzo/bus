/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.compare;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.ObjectKit;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Bean属性排序器
 * 支持读取Bean多层次下的属性
 *
 * @param <T> 被比较的Bean
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
public class PropertyCompare<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String property;
    private final boolean isNullGreater;

    /**
     * 构造
     *
     * @param property 属性名
     */
    public PropertyCompare(String property) {
        this(property, true);
    }

    /**
     * 构造
     *
     * @param property      属性名
     * @param isNullGreater null值是否排在后(从小到大排序)
     */
    public PropertyCompare(String property, boolean isNullGreater) {
        this.property = property;
        this.isNullGreater = isNullGreater;
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        java.lang.Comparable<?> v1;
        java.lang.Comparable<?> v2;
        try {
            v1 = (java.lang.Comparable<?>) BeanKit.getProperty(o1, property);
            v2 = (java.lang.Comparable<?>) BeanKit.getProperty(o2, property);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }

        return compare(o1, o2, v1, v2);
    }

    private int compare(T o1, T o2, java.lang.Comparable fieldValue1, java.lang.Comparable fieldValue2) {
        int result = ObjectKit.compare(fieldValue1, fieldValue2, isNullGreater);
        if (0 == result) {
            result = ObjectKit.compare(o1, o2, this.isNullGreater);
        }
        return result;
    }

}

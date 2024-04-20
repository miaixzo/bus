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

import org.miaixz.bus.core.lang.Assert;

import java.util.Comparator;

/**
 * 按照指定类型顺序排序，对象顺序取决于对象对应的类在数组中的位置
 *
 * @param <T> 用于比较的对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class InstanceCompare<T> implements Comparator<T> {

    private final boolean atEndIfMiss;
    private final Class<?>[] instanceOrder;

    /**
     * 构造
     *
     * @param instanceOrder 用于比较排序的对象类型数组，排序按照数组位置排序
     */
    public InstanceCompare(Class<?>... instanceOrder) {
        this(false, instanceOrder);
    }

    /**
     * 构造
     *
     * @param atEndIfMiss   如果不在列表中是否排在后边
     * @param instanceOrder 用于比较排序的对象类型数组，排序按照数组位置排序
     */
    public InstanceCompare(boolean atEndIfMiss, Class<?>... instanceOrder) {
        Assert.notNull(instanceOrder, "'instanceOrder' array must not be null");
        this.atEndIfMiss = atEndIfMiss;
        this.instanceOrder = instanceOrder;
    }

    @Override
    public int compare(T o1, T o2) {
        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return Integer.compare(i1, i2);
    }

    /**
     * 查找对象类型所在列表的位置
     *
     * @param object 对象
     * @return 位置，未找到位置根据{@link #atEndIfMiss}取不同值，false返回-1，否则返回列表长度
     */
    private int getOrder(T object) {
        if (null != object) {
            for (int i = 0; i < this.instanceOrder.length; i++) {
                if (this.instanceOrder[i].isInstance(object)) {
                    return i;
                }
            }
        }
        return this.atEndIfMiss ? this.instanceOrder.length : -1;
    }

}

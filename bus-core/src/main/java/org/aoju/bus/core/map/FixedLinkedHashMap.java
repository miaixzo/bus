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
package org.aoju.bus.core.map;

import java.util.LinkedHashMap;

/**
 * 固定大小的{@link LinkedHashMap} 实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public class FixedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    /**
     * 容量,超过此容量自动删除末尾元素
     */
    private int capacity;

    /**
     * 构造
     *
     * @param capacity 容量,实际初始容量比容量大1
     */
    public FixedLinkedHashMap(int capacity) {
        super(capacity + 1, 1.0f, true);
        this.capacity = capacity;
    }

    /**
     * 获取容量
     *
     * @return 容量
     */
    public int getCapacity() {
        return this.capacity;
    }

    /**
     * 设置容量
     *
     * @param capacity 容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        //当链表元素大于容量时,移除最老(最久未被使用)的元素
        return size() > this.capacity;
    }

}

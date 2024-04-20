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
package org.miaixz.bus.core.collection;

import java.util.*;

/**
 * 有界优先队列
 * 按照给定的排序规则,排序元素,当队列满时,
 * 按照给定的排序规则淘汰末尾元素
 *
 * @param <E> 对象泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PriorityQueue<E> extends java.util.PriorityQueue<E> {

    private static final long serialVersionUID = 1L;

    /**
     * 容量
     */
    private final int capacity;
    private final Comparator<? super E> comparator;

    public PriorityQueue(int capacity) {
        this(capacity, null);
    }

    /**
     * 构造
     *
     * @param capacity   容量
     * @param comparator 比较器
     */
    public PriorityQueue(int capacity, final Comparator<? super E> comparator) {
        super(capacity, (o1, o2) -> {
            int cResult;
            if (null != comparator) {
                cResult = comparator.compare(o1, o2);
            } else {
                Comparable<E> o1c = (Comparable<E>) o1;
                cResult = o1c.compareTo(o2);
            }

            return -cResult;
        });
        this.capacity = capacity;
        this.comparator = comparator;
    }

    /**
     * 加入元素,当队列满时,淘汰末尾元素
     *
     * @param e 元素
     * @return 加入成功与否
     */
    @Override
    public boolean offer(E e) {
        if (size() >= capacity) {
            E head = peek();
            if (this.comparator().compare(e, head) <= 0) {
                return true;
            }
            //当队列满时,就要淘汰顶端队列
            poll();
        }
        return super.offer(e);
    }

    /**
     * 添加多个元素
     * 参数为集合的情况请使用{@link PriorityQueue#addAll}
     *
     * @param c 元素数组
     * @return 是否发生改变
     */
    public boolean addAll(E[] c) {
        return this.addAll(Arrays.asList(c));
    }

    /**
     * @return 返回排序后的列表
     */
    public ArrayList<E> toList() {
        final ArrayList<E> list = new ArrayList<>(this);
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public Iterator<E> iterator() {
        return toList().iterator();
    }

}

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
package org.miaixz.bus.core.center.queue;

import org.miaixz.bus.core.center.BoundedCollection;

import java.util.*;

/**
 * 有界优先队列
 * 按照给定的排序规则，排序元素，当队列满时，按照给定的排序规则淘汰末尾元素（去除末尾元素）
 *
 * @param <E> 成员类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> implements BoundedCollection<E> {

    private static final long serialVersionUID = -1L;

    /**
     * 容量
     */
    private final int capacity;
    private final Comparator<? super E> comparator;

    /**
     * 构造
     *
     * @param capacity 容量
     */
    public BoundedPriorityQueue(final int capacity) {
        this(capacity, null);
    }

    /**
     * 构造
     *
     * @param capacity   容量
     * @param comparator 比较器
     */
    public BoundedPriorityQueue(final int capacity, final Comparator<? super E> comparator) {
        super(capacity, (o1, o2) -> {
            final int cResult;
            if (comparator != null) {
                cResult = comparator.compare(o1, o2);
            } else {
                final Comparable<E> o1c = (Comparable<E>) o1;
                cResult = o1c.compareTo(o2);
            }

            return -cResult;
        });
        this.capacity = capacity;
        this.comparator = comparator;
    }

    @Override
    public boolean isFull() {
        return size() == capacity;
    }

    @Override
    public int maxSize() {
        return capacity;
    }

    /**
     * 加入元素，当队列满时，淘汰末尾元素
     *
     * @param e 元素
     * @return 加入成功与否
     */
    @Override
    public boolean offer(final E e) {
        if (isFull()) {
            final E head = peek();
            if (this.comparator().compare(e, head) <= 0) {
                return true;
            }
            // 当队列满时，就要淘汰顶端队列
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
    public boolean addAll(final E[] c) {
        return this.addAll(Arrays.asList(c));
    }

    /**
     * @return 返回排序后的列表
     */
    public ArrayList<E> toList() {
        final ArrayList<E> list = new ArrayList<>(this);
        list.sort(comparator);
        return list;
    }

    @Override
    public Iterator<E> iterator() {
        return toList().iterator();
    }

}

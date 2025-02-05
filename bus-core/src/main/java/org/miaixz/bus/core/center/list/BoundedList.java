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
package org.miaixz.bus.core.center.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.center.BoundedCollection;

/**
 * 指定边界大小的List 通过指定边界，可以限制List的最大容量
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundedList<E> extends ListWrapper<E> implements BoundedCollection<E> {

    private final int maxSize;

    /**
     * 构造
     *
     * @param maxSize 最大容量
     */
    public BoundedList(final int maxSize) {
        this(new ArrayList<>(maxSize), maxSize);
    }

    /**
     * 构造，限制集合的最大容量为提供的List
     *
     * @param raw     原始对象
     * @param maxSize 最大容量
     */
    public BoundedList(final List<E> raw, final int maxSize) {
        super(raw);
        this.maxSize = maxSize;
    }

    @Override
    public boolean isFull() {
        return size() == this.maxSize;
    }

    @Override
    public int maxSize() {
        return this.maxSize;
    }

    @Override
    public boolean add(final E e) {
        checkFull(1);
        return super.add(e);
    }

    @Override
    public void add(final int index, final E element) {
        checkFull(1);
        super.add(index, element);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        checkFull(c.size());
        return super.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        checkFull(c.size());
        return super.addAll(index, c);
    }

    private void checkFull(final int addSize) {
        if (size() + addSize > this.maxSize) {
            throw new IndexOutOfBoundsException("List is no space to add " + addSize + " elements!");
        }
    }
}

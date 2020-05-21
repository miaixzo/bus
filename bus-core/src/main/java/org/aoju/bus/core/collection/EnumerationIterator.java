/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.collection;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * 适配器使{@link  Enumeration Enumeration}
 * 实例显示为{@link Iterator Iterator}实例.
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class EnumerationIterator<E> implements Iterator<E> {

    /**
     * 要从中删除元素的集合
     */
    private final Collection<? super E> collection;
    /**
     * 正在转换的枚举
     */
    private Enumeration<? extends E> enumeration;
    /**
     * 最后检索到的对象
     */
    private E last;

    /**
     * 构造一个新的EnumerationIterator，
     * 在调用{@link #setEnumeration(Enumeration)}之前不会起作用.
     */
    public EnumerationIterator() {
        this(null, null);
    }

    /**
     * 构造一个新的EnumerationIterator，提供给定枚举的迭代器视图.
     *
     * @param enumeration 要使用的枚举
     */
    public EnumerationIterator(final Enumeration<? extends E> enumeration) {
        this(enumeration, null);
    }

    /**
     * 构造一个新的EnumerationIterator，它将从指定集合中删除元素.
     *
     * @param enumeration 要使用的枚举
     * @param collection  要从中删除元素的集合
     */
    public EnumerationIterator(final Enumeration<? extends E> enumeration, final Collection<? super E> collection) {
        super();
        this.enumeration = enumeration;
        this.collection = collection;
        this.last = null;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public E next() {
        last = enumeration.nextElement();
        return last;
    }

    @Override
    public void remove() {
        if (collection != null) {
            if (last != null) {
                collection.remove(last);
            } else {
                throw new IllegalStateException("next() must have been called for remove() to function");
            }
        } else {
            throw new UnsupportedOperationException("No Collection associated with this Iterator");
        }
    }

    public Enumeration<? extends E> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(final Enumeration<? extends E> enumeration) {
        this.enumeration = enumeration;
    }

}

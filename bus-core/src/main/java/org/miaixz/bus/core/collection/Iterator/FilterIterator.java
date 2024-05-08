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
package org.miaixz.bus.core.collection.Iterator;

import org.miaixz.bus.core.lang.Assert;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * 包装 {@link Iterator}并根据{@link Predicate}定义，过滤元素输出
 * 类实现来自Apache Commons Collection
 *
 * @param <E> 对象泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public class FilterIterator<E> implements Iterator<E> {

    private final Iterator<? extends E> iterator;
    private final Predicate<? super E> filter;

    /**
     * 下一个元素
     */
    private E nextObject;
    /**
     * 标记下一个元素是否被计算
     */
    private boolean nextObjectSet = false;

    /**
     * 构造
     *
     * @param iterator 被包装的{@link Iterator}
     * @param filter   过滤函数，{@code null}表示不过滤
     */
    public FilterIterator(final Iterator<? extends E> iterator, final Predicate<? super E> filter) {
        this.iterator = Assert.notNull(iterator);
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        return nextObjectSet || setNextObject();
    }

    @Override
    public E next() {
        if (false == nextObjectSet && false == setNextObject()) {
            throw new NoSuchElementException();
        }
        nextObjectSet = false;
        return nextObject;
    }

    @Override
    public void remove() {
        if (nextObjectSet) {
            throw new IllegalStateException("remove() cannot be called");
        }
        iterator.remove();
    }

    /**
     * 获取被包装的{@link Iterator}
     *
     * @return {@link Iterator}
     */
    public Iterator<? extends E> getIterator() {
        return iterator;
    }

    /**
     * 获取过滤函数
     *
     * @return 过滤函数，可能为{@code null}
     */
    public Predicate<? super E> getFilter() {
        return filter;
    }

    /**
     * 设置下一个元素，如果存在返回{@code true}，否则{@code false}
     */
    private boolean setNextObject() {
        while (iterator.hasNext()) {
            final E object = iterator.next();
            if (null == filter || filter.test(object)) {
                nextObject = object;
                nextObjectSet = true;
                return true;
            }
        }
        return false;
    }

}

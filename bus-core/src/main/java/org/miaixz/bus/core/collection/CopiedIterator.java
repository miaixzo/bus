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

import org.miaixz.bus.core.toolkit.CollKit;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * 复制 {@link Iterator}为了解决并发情况下{@link Iterator}遍历导致的问题,当Iterator
 * 被修改会抛出ConcurrentModificationException),故使用复制原Iterator的方式解决此问题
 *
 * <p>
 * 解决方法为：在构造方法中遍历Iterator中的元素,装入新的List中然后遍历之
 * 当然,修改这个复制后的Iterator是没有意义的,因此remove方法将会抛出异常
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CopiedIterator<E> implements IterableIterator<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Iterator<E> listIterator;

    /**
     * 构造
     *
     * @param iterator 被复制的Iterator
     */
    public CopiedIterator(Iterator<E> iterator) {
        final List<E> eleList = CollKit.toList(iterator);
        this.listIterator = eleList.iterator();
    }

    /**
     * 根据已有{@link Iterator}，返回新的{@code CopiedIter}
     *
     * @param iterator {@link Iterator}
     * @param <E>      元素类型
     * @return {@code CopiedIter}
     */
    public static <E> CopiedIterator<E> copyOf(Iterator<E> iterator) {
        return new CopiedIterator<>(iterator);
    }

    @Override
    public boolean hasNext() {
        return this.listIterator.hasNext();
    }

    @Override
    public E next() {
        return this.listIterator.next();
    }

    /**
     * 此对象不支持移除元素
     *
     * @throws UnsupportedOperationException 当调用此方法时始终抛出此异常
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This is a read-only iterator.");
    }

}

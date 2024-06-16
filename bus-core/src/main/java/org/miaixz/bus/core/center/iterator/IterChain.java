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
package org.miaixz.bus.core.center.iterator;

import org.miaixz.bus.core.lang.Chain;
import org.miaixz.bus.core.xyz.ArrayKit;

import java.util.*;

/**
 * 组合{@link Iterator}，将多个{@link Iterator}组合在一起，便于集中遍历
 *
 * @param <T> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class IterChain<T> implements Iterator<T>, Chain<Iterator<T>, IterChain<T>> {

    protected final List<Iterator<T>> allIterators = new ArrayList<>();
    protected int currentIter = -1;

    /**
     * 构造
     * 可以使用 {@link #addChain(Iterator)} 方法加入更多的集合。
     */
    public IterChain() {

    }

    /**
     * 构造
     *
     * @param iterators 多个{@link Iterator}
     * @throws IllegalArgumentException 当存在重复的迭代器，或添加的迭代器中存在{@code null}时抛出
     */
    @SafeVarargs
    public IterChain(final Iterator<T>... iterators) {
        if (ArrayKit.isNotEmpty(iterators)) {
            for (final Iterator<T> iterator : iterators) {
                addChain(iterator);
            }
        }
    }

    /**
     * 添加迭代器
     *
     * @param iterator 迭代器
     * @return 当前实例
     * @throws IllegalArgumentException 当迭代器被重复添加，或待添加的迭代器为{@code null}时抛出
     */
    @Override
    public IterChain<T> addChain(final Iterator<T> iterator) {
        Objects.requireNonNull(iterator);
        if (allIterators.contains(iterator)) {
            throw new IllegalArgumentException("Duplicate iterator");
        }
        allIterators.add(iterator);
        return this;
    }

    @Override
    public boolean hasNext() {
        if (currentIter == -1) {
            currentIter = 0;
        }

        final int size = allIterators.size();
        for (int i = currentIter; i < size; i++) {
            final Iterator<T> iterator = allIterators.get(i);
            if (iterator.hasNext()) {
                currentIter = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return allIterators.get(currentIter).next();
    }

    @Override
    public void remove() {
        if (-1 == currentIter) {
            throw new IllegalStateException("next() has not yet been called");
        }

        allIterators.get(currentIter).remove();
    }

    @Override
    public Iterator<Iterator<T>> iterator() {
        return this.allIterators.iterator();
    }

}

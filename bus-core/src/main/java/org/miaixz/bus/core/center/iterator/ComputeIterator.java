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
package org.miaixz.bus.core.center.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 带有计算属性的遍历器
 * 通过继承此抽象遍历器，实现{@link #computeNext()}计算下一个节点，即可完成节点遍历
 * 当调用{@link #hasNext()}时将此方法产生的节点缓存，直到调用{@link #next()}取出
 * 当无下一个节点时，须返回{@code null}表示遍历结束
 *
 * @param <T> 节点类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ComputeIterator<T> implements Iterator<T> {

    private T next;
    /**
     * A flag indicating if the iterator has been fully read.
     */
    private boolean finished;

    /**
     * 计算新的节点，通过实现此方法，当调用{@link #hasNext()}时将此方法产生的节点缓存，直到调用{@link #next()}取出
     * 当无下一个节点时，须返回{@code null}表示遍历结束
     *
     * @return 节点值
     */
    protected abstract T computeNext();

    @Override
    public boolean hasNext() {
        if (null != next) {
            // 用户读取了节点，但是没有使用
            return true;
        } else if (finished) {
            // 读取结束
            return false;
        }

        final T result = computeNext();
        if (null == result) {
            // 不再有新的节点，结束
            this.finished = true;
            return false;
        } else {
            this.next = result;
            return true;
        }

    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more lines");
        }

        final T result = this.next;
        // 清空cache，表示此节点读取完毕，下次计算新节点
        this.next = null;
        return result;
    }

    /**
     * 手动结束遍历器，用于关闭操作等
     */
    public void finish() {
        this.finished = true;
        this.next = null;
    }

}

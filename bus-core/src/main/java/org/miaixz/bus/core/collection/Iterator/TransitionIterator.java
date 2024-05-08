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
import java.util.function.Function;

/**
 * 转换迭代器
 *
 * @param <F> 对象
 * @param <T> 对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransitionIterator<F, T> implements Iterator<T> {

    private final Iterator<? extends F> backingIterator;
    private final Function<? super F, ? extends T> func;

    /**
     * 构造
     *
     * @param backingIterator 源{@link Iterator}
     * @param func            转换函数
     */
    public TransitionIterator(final Iterator<? extends F> backingIterator, final Function<? super F, ? extends T> func) {
        this.backingIterator = Assert.notNull(backingIterator);
        this.func = Assert.notNull(func);
    }

    @Override
    public final boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public final T next() {
        return func.apply(backingIterator.next());
    }

    @Override
    public final void remove() {
        backingIterator.remove();
    }

}

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
package org.miaixz.bus.core.cache.provider;

import java.io.Serializable;
import java.util.Iterator;

/**
 * {@link AbstractCache} 的值迭代器.
 *
 * @param <V> 迭代对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheValuesIterator<V> implements Iterator<V>, Serializable {

    private static final long serialVersionUID = -1L;

    private final CacheObjectIterator<?, V> cacheObjIter;

    /**
     * 构造
     *
     * @param iterator 原{@link CacheObjectIterator}
     */
    CacheValuesIterator(final CacheObjectIterator<?, V> iterator) {
        this.cacheObjIter = iterator;
    }

    /**
     * @return 是否有下一个值
     */
    @Override
    public boolean hasNext() {
        return this.cacheObjIter.hasNext();
    }

    /**
     * @return 下一个值
     */
    @Override
    public V next() {
        return cacheObjIter.next().getValue();
    }

    /**
     * 从缓存中移除没有过期的当前值，不支持此方法
     */
    @Override
    public void remove() {
        cacheObjIter.remove();
    }

}

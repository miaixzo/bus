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
package org.miaixz.bus.core.cache.provider;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * FIFO(first in first out) 先进先出缓存.
 *
 * <p>
 * 元素不停的加入缓存直到缓存满为止，当缓存满时，清理过期缓存对象，清理后依旧满则删除先入的缓存（链表首部对象） 优点：简单快速 缺点：不灵活，不能保证最常用的对象总是被保留
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class FIFOCache<K, V> extends StampedCache<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，默认对象不过期
     *
     * @param capacity 容量
     */
    public FIFOCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * 构造
     *
     * @param capacity 容量
     * @param timeout  过期时长
     */
    public FIFOCache(final int capacity, final long timeout) {
        this.capacity = capacity;
        this.timeout = timeout;
        cacheMap = new LinkedHashMap<>(capacity + 1, 1.0f, false);
    }

    /**
     * 先进先出的清理策略 先遍历缓存清理过期的缓存对象，如果清理后还是满的，则删除第一个缓存对象
     */
    @Override
    protected int pruneCache() {
        int count = 0;
        CacheObject<K, V> first = null;

        // 清理过期对象并找出链表头部元素（先入元素）
        final Iterator<CacheObject<K, V>> values = cacheObjIter();
        if (isPruneExpiredActive()) {
            // 清理过期对象并找出链表头部元素（先入元素）
            while (values.hasNext()) {
                final CacheObject<K, V> co = values.next();
                if (co.isExpired()) {
                    values.remove();
                    onRemove(co.key, co.obj);
                    count++;
                    continue;
                }
                if (first == null) {
                    first = co;
                }
            }
        } else {
            first = values.hasNext() ? values.next() : null;
        }

        // 清理结束后依旧是满的，则删除第一个被缓存的对象
        if (isFull() && null != first) {
            removeWithoutLock(first.key);
            onRemove(first.key, first.obj);
            count++;
        }
        return count;
    }

}

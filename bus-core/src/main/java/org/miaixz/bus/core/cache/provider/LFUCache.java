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

import java.io.Serial;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.thread.lock.NoLock;

/**
 * LFU(least frequently used) 最少使用率缓存 根据使用次数来判定对象是否被持续缓存 使用率是通过访问次数计算的。 当缓存满时清理过期对象。
 * 清理后依旧满的情况下清除最少访问（访问计数最小）的对象并将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class LFUCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852239098371L;

    /**
     * 构造
     *
     * @param capacity 容量
     */
    public LFUCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * 构造
     *
     * @param capacity 容量
     * @param timeout  过期时长
     */
    public LFUCache(int capacity, final long timeout) {
        if (Integer.MAX_VALUE == capacity) {
            capacity -= 1;
        }

        this.capacity = capacity;
        this.timeout = timeout;
        this.lock = NoLock.INSTANCE;
        this.cacheMap = new ConcurrentHashMap<>(capacity + 1, 1.0f);
    }

    /**
     * 清理过期对象。 清理后依旧满的情况下清除最少访问（访问计数最小）的对象并将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
     *
     * @return 清理个数
     */
    @Override
    protected int pruneCache() {
        int count = 0;
        CacheObject<K, V> comin = null;

        // 清理过期对象并找出访问最少的对象
        Iterator<CacheObject<K, V>> values = cacheObjIter();
        CacheObject<K, V> co;
        while (values.hasNext()) {
            co = values.next();
            if (co.isExpired() == true) {
                values.remove();
                onRemove(co.key, co.object);
                count++;
                continue;
            }

            // 找出访问最少的对象
            if (comin == null || co.accessCount.get() < comin.accessCount.get()) {
                comin = co;
            }
        }

        // 减少所有对象访问量，并清除减少后为0的访问对象
        if (isFull() && comin != null) {
            final long minAccessCount = comin.accessCount.get();

            values = cacheObjIter();
            CacheObject<K, V> co1;
            while (values.hasNext()) {
                co1 = values.next();
                if (co1.accessCount.addAndGet(-minAccessCount) <= 0) {
                    values.remove();
                    onRemove(co1.key, co1.object);
                    count++;
                }
            }
        }

        return count;
    }

}

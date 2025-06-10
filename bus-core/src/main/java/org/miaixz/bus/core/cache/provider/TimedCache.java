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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.cache.GlobalPruneTimer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.thread.lock.NoLock;

/**
 * 定时缓存, 此缓存没有容量限制，对象只有在过期后才会被移除, 此缓存采用读写乐观锁，用于可脏读的场景，不能使用LinkedHashMap
 *
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimedCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232272208L;

    /**
     * 正在执行的定时任务
     */
    private ScheduledFuture<?> pruneJobFuture;

    /**
     * 构造
     *
     * @param timeout 超时（过期）时长，单位毫秒
     */
    public TimedCache(final long timeout) {
        this(timeout, new HashMap<>());
    }

    /**
     * 构造
     *
     * @param timeout 过期时长
     * @param map     存储缓存对象的map
     */
    public TimedCache(final long timeout, final Map<Mutable<K>, CacheObject<K, V>> map) {
        this.capacity = 0;
        // 如果使用线程安全的Map，则不加锁，否则默认使用ReentrantLock
        this.lock = map instanceof ConcurrentMap ? NoLock.INSTANCE : new ReentrantLock();
        this.cacheMap = Assert.isNotInstanceOf(LinkedHashMap.class, map);
    }

    /**
     * 清理过期对象
     *
     * @return 清理数
     */
    @Override
    protected int pruneCache() {
        int count = 0;
        final Iterator<CacheObject<K, V>> values = cacheObjIter();
        CacheObject<K, V> co;
        while (values.hasNext()) {
            co = values.next();
            if (co.isExpired()) {
                values.remove();
                onRemove(co.key, co.object);
                count++;
            }
        }
        return count;
    }

    /**
     * 定时清理
     *
     * @param delay 间隔时长，单位毫秒
     * @return this
     */
    public TimedCache<K, V> schedulePrune(final long delay) {
        this.pruneJobFuture = GlobalPruneTimer.INSTANCE.schedule(this::prune, delay);
        return this;
    }

    /**
     * 取消定时清理
     */
    public void cancelPruneSchedule() {
        if (null != pruneJobFuture) {
            pruneJobFuture.cancel(true);
        }
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * LFU (Least Frequently Used) cache.
 * <p>
 * This cache determines which objects to evict based on their usage frequency, which is calculated from the access
 * count. When the cache is full, it first removes any expired objects. If the cache remains full, it removes the object
 * with the lowest access count. The access counts of all remaining objects are then reduced by this minimum count to
 * ensure fair competition for new objects.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class LFUCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852231672235L;

    /**
     * Constructs an LFU cache with a specified capacity and no default timeout.
     *
     * @param capacity The cache capacity.
     */
    public LFUCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs an LFU cache with a specified capacity and timeout.
     *
     * @param capacity The cache capacity.
     * @param timeout  The default timeout for cache entries in milliseconds.
     */
    public LFUCache(int capacity, final long timeout) {
        if (Integer.MAX_VALUE == capacity) {
            // Prevent potential overflow issues by reducing capacity by one.
            capacity -= 1;
        }

        this.capacity = capacity;
        this.timeout = timeout;
        // LFU cache uses ConcurrentHashMap, which is thread-safe, so no external lock is needed.
        this.lock = NoLock.INSTANCE;
        this.cacheMap = new ConcurrentHashMap<>(capacity + 1, 1.0f);
    }

    /**
     * Prunes the cache by removing expired objects. If the cache is still full, it removes the least frequently used
     * object and normalizes the access counts of the remaining objects.
     *
     * @return The number of items pruned.
     */
    @Override
    protected int pruneCache() {
        int count = 0;
        CacheObject<K, V> leastUsed = null;

        // First, remove all expired objects and find the one with the minimum access count.
        Iterator<CacheObject<K, V>> values = cacheObjIter();
        while (values.hasNext()) {
            CacheObject<K, V> co = values.next();
            if (co.isExpired()) {
                values.remove();
                onRemove(co.key, co.object);
                count++;
                continue;
            }

            // Find the object with the lowest access count among non-expired items.
            if (leastUsed == null || co.accessCount.get() < leastUsed.accessCount.get()) {
                leastUsed = co;
            }
        }

        // If the cache is still full and a least-used object was found, normalize access counts.
        if (isFull() && leastUsed != null) {
            final long minAccessCount = leastUsed.accessCount.get();

            values = cacheObjIter();
            while (values.hasNext()) {
                CacheObject<K, V> co = values.next();
                // Reduce the access count of all objects by the minimum count.
                // If an object's count becomes zero or less, it is a candidate for removal.
                if (co.accessCount.addAndGet(-minAccessCount) <= 0) {
                    values.remove();
                    onRemove(co.key, co.object);
                    count++;
                }
            }
        }

        return count;
    }

}

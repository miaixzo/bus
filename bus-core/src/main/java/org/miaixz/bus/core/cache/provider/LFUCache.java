/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

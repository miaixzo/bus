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
import java.util.LinkedHashMap;

/**
 * FIFO (First-In, First-Out) cache.
 * <p>
 * Elements are continuously added to the cache until it is full. When the cache is full, it first attempts to remove
 * expired objects. If the cache is still full after pruning, the first object that was added is removed.
 *
 * <p>
 * <strong>Advantages:</strong> Simple and fast.
 *
 * <p>
 * <strong>Disadvantages:</strong> Inflexible; does not guarantee that the most frequently used objects are retained.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FIFOCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852231588982L;

    /**
     * Constructs a FIFO cache with a specified capacity and no default timeout.
     *
     * @param capacity The cache capacity.
     */
    public FIFOCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs a FIFO cache with a specified capacity and timeout.
     *
     * @param capacity The cache capacity.
     * @param timeout  The default timeout for cache entries in milliseconds.
     */
    public FIFOCache(final int capacity, final long timeout) {
        this.capacity = capacity;
        this.timeout = timeout;
        // Note: The accessOrder parameter is false for FIFO behavior.
        cacheMap = new LinkedHashMap<>(capacity + 1, 1.0f, false);
    }

    /**
     * Prunes the cache using a FIFO (First-In, First-Out) strategy.
     * <p>
     * It first iterates through the cache to remove any expired objects. If the cache is still full after this, it
     * removes the oldest entry (the first one that was added).
     *
     * @return The number of items pruned from the cache.
     */
    @Override
    protected int pruneCache() {
        int count = 0;
        CacheObject<K, V> first = null;

        // Iterate through the cache to remove expired items and find the first-in (oldest) entry.
        final Iterator<CacheObject<K, V>> values = cacheObjIter();
        if (isPruneExpiredActive()) {
            while (values.hasNext()) {
                final CacheObject<K, V> co = values.next();
                if (co.isExpired()) {
                    values.remove();
                    onRemove(co.key, co.object);
                    count++;
                    continue; // Continue to the next item after removal
                }

                // Identify the first non-expired element, which is the oldest.
                if (first == null) {
                    first = co;
                }
            }
        } else {
            // If expiration is not active, the first element is simply the first in the iteration.
            first = values.hasNext() ? values.next() : null;
        }

        // If the cache is still full after pruning expired items, remove the oldest entry.
        if (isFull() && null != first) {
            removeWithoutLock(first.key);
            onRemove(first.key, first.object);
            count++;
        }
        return count;
    }

}

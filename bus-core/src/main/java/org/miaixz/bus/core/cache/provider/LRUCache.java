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

import org.miaixz.bus.core.center.map.FixedLinkedHashMap;
import org.miaixz.bus.core.lang.mutable.Mutable;

/**
 * LRU (Least Recently Used) cache.
 * <p>
 * This cache evicts the least recently used items first. When an object is accessed, it is moved to the head of a
 * linked list. When the cache is full, the object at the tail of the list (the least recently used) is removed.
 *
 * <p>
 * This implementation is based on {@link FixedLinkedHashMap}, which provides an efficient LRU mechanism.
 *
 * <p>
 * <strong>Advantages:</strong> Simple, fast, and frequently used objects are less likely to be evicted.
 *
 * <p>
 * <strong>Disadvantages:</strong> Performance can degrade if the cache is frequently full and access patterns are
 * random.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class LRUCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852231786866L;

    /**
     * Constructs an LRU cache with a specified capacity and no default timeout.
     *
     * @param capacity The cache capacity.
     */
    public LRUCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs an LRU cache with a specified capacity and timeout.
     *
     * @param capacity The cache capacity.
     * @param timeout  The default timeout for cache entries in milliseconds.
     */
    public LRUCache(int capacity, final long timeout) {
        if (Integer.MAX_VALUE == capacity) {
            // Prevent potential overflow issues.
            capacity -= 1;
        }

        this.capacity = capacity;
        this.timeout = timeout;

        // The underlying map is a FixedLinkedHashMap, which automatically handles LRU eviction.
        // When an item is accessed, it is moved to the head of the list.
        final FixedLinkedHashMap<Mutable<K>, CacheObject<K, V>> fixedLinkedHashMap = new FixedLinkedHashMap<>(capacity);
        // Set a listener to propagate removal events to the main cache listener.
        fixedLinkedHashMap.setRemoveListener(entry -> {
            if (null != listener) {
                listener.onRemove(entry.getKey().get(), entry.getValue().getValue());
            }
        });
        cacheMap = fixedLinkedHashMap;
    }

    /**
     * Prunes the cache by removing only expired objects. The LRU eviction logic is handled automatically by the
     * underlying {@link FixedLinkedHashMap}.
     *
     * @return The number of expired items removed.
     */
    @Override
    protected int pruneCache() {
        if (!isPruneExpiredActive()) {
            return 0;
        }
        int count = 0;
        final Iterator<CacheObject<K, V>> values = cacheObjIter();
        while (values.hasNext()) {
            CacheObject<K, V> co = values.next();
            if (co.isExpired()) {
                values.remove();
                onRemove(co.key, co.object);
                count++;
            }
        }
        return count;
    }

}

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
 * A cache with a timeout but no capacity limit. Objects are removed only when they expire.
 * <p>
 * This cache uses an optimistic locking approach, making it suitable for scenarios where dirty reads are acceptable. It
 * is not compatible with {@link LinkedHashMap} because read operations on a {@code LinkedHashMap} can modify its
 * internal structure, which would conflict with the locking strategy.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimedCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232272208L;

    /**
     * The scheduled task for the pruning job.
     */
    private ScheduledFuture<?> pruneJobFuture;

    /**
     * Constructs a timed cache with a specified timeout.
     *
     * @param timeout The timeout for cache entries in milliseconds.
     */
    public TimedCache(final long timeout) {
        this(timeout, new HashMap<>());
    }

    /**
     * Constructs a timed cache with a specified timeout and a custom underlying map.
     *
     * @param timeout The timeout for cache entries in milliseconds.
     * @param map     The map to use for storing cache objects.
     */
    public TimedCache(final long timeout, final Map<Mutable<K>, CacheObject<K, V>> map) {
        this.capacity = 0; // No capacity limit
        this.timeout = timeout;
        // Use NoLock for thread-safe maps, otherwise default to ReentrantLock.
        this.lock = map instanceof ConcurrentMap ? NoLock.INSTANCE : new ReentrantLock();
        this.cacheMap = Assert.isNotInstanceOf(
                LinkedHashMap.class,
                map,
                "LinkedHashMap is not supported for TimedCache due to its structural modification on get().");
    }

    /**
     * Prunes the cache by removing all expired objects.
     *
     * @return The number of items pruned.
     */
    @Override
    protected int pruneCache() {
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

    /**
     * Schedules a periodic pruning task to remove expired objects.
     *
     * @param delay The interval in milliseconds between pruning tasks.
     * @return This {@link TimedCache} instance.
     */
    public TimedCache<K, V> schedulePrune(final long delay) {
        this.pruneJobFuture = GlobalPruneTimer.INSTANCE.schedule(this::prune, delay);
        return this;
    }

    /**
     * Cancels the scheduled pruning task.
     */
    public void cancelPruneSchedule() {
        if (null != pruneJobFuture) {
            pruneJobFuture.cancel(true);
        }
    }

}

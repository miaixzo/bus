/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.miaixz.bus.core.cache.GlobalPruneTimer;
import org.miaixz.bus.core.lang.mutable.Mutable;

/**
 * A timed cache with no capacity limit, where objects are removed only upon expiration.
 * <p>
 * This implementation uses a reentrant lock for all read and write operations, ensuring thread safety even with
 * non-thread-safe underlying maps like {@link HashMap}.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimedReentrantCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232313669L;

    /**
     * The scheduled task for the pruning job.
     */
    private ScheduledFuture<?> pruneJobFuture;

    /**
     * Constructs a timed cache with a specified timeout, using a {@link HashMap} as the underlying storage.
     *
     * @param timeout The timeout for cache entries in milliseconds.
     */
    public TimedReentrantCache(final long timeout) {
        this(timeout, new HashMap<>());
    }

    /**
     * Constructs a timed cache with a specified timeout and a custom underlying map.
     *
     * @param timeout The timeout for cache entries in milliseconds.
     * @param map     The map to use for storing cache objects.
     */
    public TimedReentrantCache(final long timeout, final Map<Mutable<K>, CacheObject<K, V>> map) {
        this.capacity = 0; // No capacity limit
        this.timeout = timeout;
        this.cacheMap = map;
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
     * @return This {@link TimedReentrantCache} instance.
     */
    public TimedReentrantCache<K, V> schedulePrune(final long delay) {
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

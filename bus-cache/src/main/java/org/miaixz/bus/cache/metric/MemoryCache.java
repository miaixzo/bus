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
package org.miaixz.bus.cache.metric;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An in-memory cache implementation based on {@link ConcurrentHashMap} and {@link ReentrantReadWriteLock}.
 * <p>
 * This class provides a thread-safe, in-memory caching solution that supports various eviction policies, including
 * maximum size, time-to-live (expire after write), and time-to-idle (expire after access). It also features a periodic
 * cleanup task to prune expired entries.
 * </p>
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryCache<K, V> implements CacheX<K, V> {

    /**
     * The default cache expiration time: 3 minutes (in milliseconds).
     * <p>
     * This default is chosen to accommodate processes that may take a few minutes, such as user authorization flows.
     * </p>
     */
    public static long timeout = 180_000;

    /**
     * A global flag to enable or disable the scheduled task for pruning expired cache entries.
     */
    public static boolean schedulePrune = true;

    /**
     * The underlying map for storing cache entries.
     */
    private final Map<K, CacheState> map;

    /**
     * A read-write lock to ensure thread-safe access to the cache.
     */
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock(true);

    /**
     * The lock for write operations.
     */
    private final Lock writeLock = cacheLock.writeLock();

    /**
     * The lock for read operations.
     */
    private final Lock readLock = cacheLock.readLock();

    /**
     * The maximum number of entries the cache can hold.
     */
    private final long maximumSize;

    /**
     * The expiration time in milliseconds after the last access.
     */
    private final long expireAfterAccess;

    /**
     * The expiration time in milliseconds after the last write.
     */
    private final long expireAfterWrite;

    /**
     * A counter for the total number of cache requests.
     */
    private final AtomicLong requestCount = new AtomicLong();

    /**
     * A counter for the number of cache hits.
     */
    private final AtomicLong hitCount = new AtomicLong();

    /**
     * Constructs a {@code MemoryCache} with default settings.
     * <p>
     * Default configuration:
     * <ul>
     * <li>Maximum size: 1000 entries</li>
     * <li>Expire after write: 3 minutes</li>
     * <li>Expire after access: Disabled</li>
     * </ul>
     * If {@link #schedulePrune} is enabled, a cleanup task is scheduled.
     */
    public MemoryCache() {
        this.map = new ConcurrentHashMap<>(16);
        this.maximumSize = 1000;
        this.expireAfterWrite = timeout;
        this.expireAfterAccess = 0;
        if (schedulePrune) {
            this.schedulePrune(timeout);
        }
    }

    /**
     * Constructs a {@code MemoryCache} with a specified maximum size and expiration time.
     *
     * @param size   The maximum number of entries the cache can hold.
     * @param expire The expiration time in milliseconds after the last write.
     */
    public MemoryCache(long size, long expire) {
        this.map = new ConcurrentHashMap<>(16);
        this.maximumSize = size;
        this.expireAfterWrite = expire;
        this.expireAfterAccess = 0;
        if (schedulePrune) {
            this.schedulePrune(expire);
        }
    }

    /**
     * Constructs a {@code MemoryCache} from a {@link Properties} object.
     * <p>
     * Supported properties:
     * <ul>
     * <li>`maximumSize`: Max entries (default: 1000).</li>
     * <li>`expireAfterWrite`: TTL in ms (default: 3 minutes).</li>
     * <li>`expireAfterAccess`: TTI in ms (default: 0, disabled).</li>
     * <li>`initialCapacity`: Initial map size (default: 16).</li>
     * </ul>
     *
     * @param properties The configuration properties.
     */
    public MemoryCache(Properties properties) {
        String prefix = properties.getProperty("prefix", Normal.EMPTY);
        String maximumSize = properties.getProperty(prefix + "maximumSize");
        String expireAfterAccess = properties.getProperty(prefix + "expireAfterAccess");
        String expireAfterWrite = properties.getProperty(prefix + "expireAfterWrite");
        String initialCapacity = properties.getProperty(prefix + "initialCapacity");

        this.maximumSize = StringKit.isNotEmpty(maximumSize) ? Long.parseLong(maximumSize) : 1000;
        this.expireAfterWrite = StringKit.isNotEmpty(expireAfterWrite) ? Long.parseLong(expireAfterWrite) : timeout;
        this.expireAfterAccess = StringKit.isNotEmpty(expireAfterAccess) ? Long.parseLong(expireAfterAccess) : 0;
        int initCapacity = StringKit.isNotEmpty(initialCapacity) ? Integer.parseInt(initialCapacity) : 16;

        this.map = new ConcurrentHashMap<>(initCapacity);
        if (schedulePrune) {
            long effectiveExpire = Math
                    .min(this.expireAfterWrite, this.expireAfterAccess > 0 ? this.expireAfterAccess : Long.MAX_VALUE);
            this.schedulePrune(effectiveExpire);
        }
    }

    /**
     * Reads a single value from the cache.
     * <p>
     * Returns the value associated with the key if it exists and has not expired. Accessing the key updates its last
     * access time, relevant for the `expireAfterAccess` policy.
     * </p>
     *
     * @param key The key whose value to retrieve.
     * @return The value, or {@code null} if the key is not found or has expired.
     */
    @Override
    public V read(K key) {
        readLock.lock();
        try {
            requestCount.incrementAndGet();
            CacheState cacheState = map.get(key);
            if (cacheState == null || cacheState.isExpired(expireAfterWrite, expireAfterAccess)) {
                return null;
            }
            cacheState.updateAccessTime();
            hitCount.incrementAndGet();
            return (V) cacheState.getState();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reads multiple values from the cache in a batch.
     *
     * @param keys A collection of keys to retrieve.
     * @return A map of keys to their corresponding values. If a key is not found or has expired, its value in the map
     *         will be {@code null}.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        Map<K, V> subCache = new HashMap<>(keys.size());
        for (K key : keys) {
            subCache.put(key, read(key));
        }
        return subCache;
    }

    /**
     * Writes multiple key-value pairs to the cache.
     *
     * @param keyValueMap A map of key-value pairs to store.
     * @param expire      The expiration time in milliseconds. This is ignored by this implementation, as expiration is
     *                    set globally.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        if (MapKit.isNotEmpty(keyValueMap)) {
            keyValueMap.forEach((key, value) -> write(key, value, expire));
        }
    }

    /**
     * Writes a single key-value pair to the cache.
     * <p>
     * If the cache is full (i.e., at `maximumSize`), the oldest entry is evicted to make space.
     * </p>
     *
     * @param key    The key to write.
     * @param value  The value to associate with the key.
     * @param expire The expiration time in milliseconds. This is used to calculate the entry's expiry.
     */
    @Override
    public void write(K key, V value, long expire) {
        writeLock.lock();
        try {
            if (map.size() >= maximumSize && !map.containsKey(key)) {
                evictOldest();
            }
            map.put(key, new CacheState(value, expire));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if a key exists in the cache and has not expired.
     *
     * @param key The key to check.
     * @return {@code true} if the key exists and is not expired, otherwise {@code false}.
     */
    @Override
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            CacheState cacheState = map.get(key);
            return cacheState != null && !cacheState.isExpired(expireAfterWrite, expireAfterAccess);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Removes all expired entries from the cache.
     * <p>
     * <strong>Note:</strong> This method does not clear all entries. It only prunes entries that have expired based on
     * the `expireAfterWrite` or `expireAfterAccess` policies.
     * </p>
     */
    @Override
    public void clear() {
        writeLock.lock();
        try {
            map.entrySet().removeIf(entry -> entry.getValue().isExpired(expireAfterWrite, expireAfterAccess));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes one or more entries from the cache.
     *
     * @param keys The keys of the entries to remove.
     */
    @Override
    public void remove(K... keys) {
        writeLock.lock();
        try {
            for (K key : keys) {
                map.remove(key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Schedules a periodic task to prune expired entries from the cache.
     *
     * @param delay The interval in milliseconds at which to run the pruning task.
     */
    public void schedulePrune(long delay) {
        CacheScheduler.INSTANCE.schedule(this::clear, delay);
    }

    /**
     * Gets a string representation of the current cache statistics.
     *
     * @return A string containing statistics like request count, hit count, hit rate, and current size.
     */
    public String getStats() {
        long requests = requestCount.get();
        long hits = hitCount.get();
        double hitRate = requests == 0 ? 0.0 : (double) hits / requests;
        return String.format(
                "MemoryCacheStats[requests=%d, hits=%d, hitRate=%.2f%%, size=%d]",
                requests,
                hits,
                hitRate * 100,
                map.size());
    }

    /**
     * Returns the approximate number of entries in this cache.
     *
     * @return The number of entries.
     */
    public long estimatedSize() {
        return map.size();
    }

    /**
     * Returns the underlying {@link Map} instance used by the cache.
     *
     * @return The native cache map.
     */
    public Map<K, CacheState> getNativeCache() {
        return map;
    }

    /**
     * Evicts the oldest entry from the cache, determined by its write time.
     */
    private void evictOldest() {
        writeLock.lock();
        try {
            map.entrySet().stream().min(Comparator.comparingLong(entry -> entry.getValue().getWriteTime()))
                    .ifPresent(oldest -> map.remove(oldest.getKey()));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * A singleton scheduler for handling periodic cache maintenance tasks.
     */
    private enum CacheScheduler {

        INSTANCE;

        private final AtomicInteger cacheTaskNumber = new AtomicInteger(1);
        private ScheduledExecutorService scheduler;

        CacheScheduler() {
            of();
        }

        private void of() {
            this.shutdown();
            this.scheduler = new ScheduledThreadPoolExecutor(10,
                    r -> new Thread(r, String.format("Cache-Task-%s", cacheTaskNumber.getAndIncrement())));
        }

        public void shutdown() {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }

        public void schedule(Runnable task, long delay) {
            this.scheduler.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * An internal class to hold the cached value along with its metadata.
     */
    @Getter
    @Setter
    private static class CacheState implements Serializable {

        private final Object state;
        private final long writeTime;
        private long lastAccessTime;
        private final long expireAfterWrite;

        CacheState(Object state, long expire) {
            this.state = state;
            this.writeTime = System.currentTimeMillis();
            this.lastAccessTime = this.writeTime;
            this.expireAfterWrite = this.writeTime + expire;
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired(long globalExpireAfterWrite, long globalExpireAfterAccess) {
            long currentTime = System.currentTimeMillis();
            // Check entry-specific expiration
            if (this.expireAfterWrite > this.writeTime && currentTime > this.expireAfterWrite) {
                return true;
            }
            // Check global expiration policies
            if (globalExpireAfterWrite > 0 && currentTime > this.writeTime + globalExpireAfterWrite) {
                return true;
            }
            return globalExpireAfterAccess > 0 && currentTime > this.lastAccessTime + globalExpireAfterAccess;
        }
    }

}

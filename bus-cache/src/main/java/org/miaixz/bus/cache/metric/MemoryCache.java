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
import org.miaixz.bus.cache.magic.CacheExpire;
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
     * Independent counter map for {@link #increment(Object)} operations.
     * <p>
     * Kept separate from {@link #map} because the value type is generic {@code V} and cannot hold {@link AtomicLong}.
     * Counters are not subject to TTL expiry: they persist until {@link #remove(Object[])} is called explicitly.
     * </p>
     */
    private final ConcurrentHashMap<K, AtomicLong> counters = new ConcurrentHashMap<>();

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
            if (cacheState == null) {
                return null;
            }
            if (cacheState.isExpired(expireAfterWrite, expireAfterAccess)) {
                // Lazy eviction: remove the stale entry on first access rather than waiting for the background pruner.
                // Upgrade to write lock is not supported by ReentrantReadWriteLock, so release read lock first.
                readLock.unlock();
                writeLock.lock();
                try {
                    // Re-check under write lock to avoid race with another thread that may have already evicted.
                    CacheState recheck = map.get(key);
                    if (recheck != null && recheck.isExpired(expireAfterWrite, expireAfterAccess)) {
                        map.remove(key);
                    }
                } finally {
                    // Downgrade: re-acquire read lock before releasing write lock so caller stays within a lock.
                    readLock.lock();
                    writeLock.unlock();
                }
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
     * <p>
     * Acquires the read lock once for the entire batch to avoid N repeated lock/unlock cycles that would occur if each
     * key were looked up via the single-key {@link #read(Object)} method. Expired keys detected during the scan are
     * collected and lazily removed under a write lock after the read phase completes.
     * </p>
     *
     * @param keys A collection of keys to retrieve.
     * @return A map of keys to their corresponding values. Missing or expired entries are omitted.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        Map<K, V> subCache = new HashMap<>(keys.size());
        List<K> expiredKeys = null;
        readLock.lock();
        try {
            for (K key : keys) {
                requestCount.incrementAndGet();
                CacheState cacheState = map.get(key);
                if (cacheState == null) {
                    continue;
                }
                if (cacheState.isExpired(expireAfterWrite, expireAfterAccess)) {
                    if (expiredKeys == null) {
                        expiredKeys = new ArrayList<>();
                    }
                    expiredKeys.add(key);
                } else {
                    cacheState.updateAccessTime();
                    hitCount.incrementAndGet();
                    subCache.put(key, (V) cacheState.getState());
                }
            }
        } finally {
            readLock.unlock();
        }
        // Lazily evict expired keys collected during the read pass.
        if (expiredKeys != null) {
            writeLock.lock();
            try {
                for (K key : expiredKeys) {
                    CacheState recheck = map.get(key);
                    if (recheck != null && recheck.isExpired(expireAfterWrite, expireAfterAccess)) {
                        map.remove(key);
                    }
                }
            } finally {
                writeLock.unlock();
            }
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
                counters.remove(key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Scans and returns all non-expired entries whose keys start with the given prefix.
     *
     * @param prefix the key prefix to match
     * @return a map of matching key-value pairs
     */
    @Override
    public Map<K, V> scan(K prefix) {
        readLock.lock();
        try {
            Map<K, V> result = new HashMap<>();
            map.forEach((k, state) -> {
                if (k.toString().startsWith(prefix.toString())
                        && !state.isExpired(expireAfterWrite, expireAfterAccess)) {
                    result.put(k, (V) state.getState());
                }
            });
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Atomically increments the counter stored at the given key and returns the new value.
     * <p>
     * The counter is maintained in a separate {@link ConcurrentHashMap} of {@link AtomicLong} values and is not subject
     * to TTL expiry. If the key does not exist the counter is initialised to {@code 0} and then incremented, returning
     * {@code 1}.
     * </p>
     *
     * @param key the counter key
     * @return the new counter value after increment
     */
    @Override
    public long increment(K key) {
        return counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Schedules a periodic task to prune expired entries from the cache.
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
     * <p>
     * Must be called while the caller already holds {@link #writeLock}.
     * </p>
     */
    private void evictOldest() {
        map.entrySet().stream().min(Comparator.comparingLong(entry -> entry.getValue().getWriteTime()))
                .ifPresent(oldest -> map.remove(oldest.getKey()));
    }

    /**
     * A singleton scheduler for handling periodic cache maintenance tasks.
     */
    private enum CacheScheduler {

        /**
         * The singleton instance of the cache scheduler.
         */
        INSTANCE;

        /**
         * A counter for generating unique cache task numbers.
         */
        private final AtomicInteger cacheTaskNumber = new AtomicInteger(1);

        /**
         * The scheduled executor service for running periodic tasks.
         */
        private ScheduledExecutorService scheduler;

        CacheScheduler() {
            of();
        }

        private void of() {
            this.shutdown();
            this.scheduler = new ScheduledThreadPoolExecutor(1,
                    r -> new Thread(r, String.format("Cache-Task-%s", cacheTaskNumber.getAndIncrement())));
        }

        public void shutdown() {
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
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

        /**
         * The cached value.
         */
        private final Object state;

        /**
         * The timestamp when the entry was written.
         */
        private final long writeTime;

        /**
         * The timestamp of the last access to this entry.
         */
        private long lastAccessTime;

        /**
         * The absolute expiration timestamp. Set to {@link Long#MAX_VALUE} for FOREVER entries.
         */
        private final long expireAfterWrite;

        /**
         * True when the entry was written with {@code expire == CacheExpire.FOREVER} and must never be evicted.
         */
        private final boolean forever;

        CacheState(Object state, long expire) {
            this.state = state;
            this.writeTime = System.currentTimeMillis();
            this.lastAccessTime = this.writeTime;
            this.forever = (expire == CacheExpire.FOREVER);
            this.expireAfterWrite = forever ? Long.MAX_VALUE : this.writeTime + expire;
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired(long globalExpireAfterWrite, long globalExpireAfterAccess) {
            if (this.forever) {
                return false;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime > this.expireAfterWrite) {
                return true;
            }
            if (globalExpireAfterWrite > 0 && currentTime > this.writeTime + globalExpireAfterWrite) {
                return true;
            }
            return globalExpireAfterAccess > 0 && currentTime > this.lastAccessTime + globalExpireAfterAccess;
        }
    }

}

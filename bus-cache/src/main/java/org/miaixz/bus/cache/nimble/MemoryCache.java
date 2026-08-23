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
package org.miaixz.bus.cache.nimble;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongSupplier;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * A concurrent in-memory cache with per-entry TTL, optional TTI, bounded capacity, atomic operations, and cancellable
 * periodic pruning.
 * <p>
 * Ordinary reads use {@link ConcurrentHashMap} without a global read lock. Ordinary writes are serialized only while
 * updating the entry map and FIFO write-order metadata. Capacity eviction removes the oldest current write, not the
 * least recently read entry. Instances created with {@link #MemoryCache(LongSupplier)} additionally expose the atomic
 * asynchronous methods and coordinate those methods with terminal close.
 * </p>
 *
 * @param <K> key type
 * @param <V> value type
 * @author Kimi Liu
 */
public class MemoryCache<K, V> implements CacheX<K, V> {

    /**
     * Maximum live entry count.
     */
    private final long maximumSize;
    /**
     * Global expire-after-write duration in milliseconds.
     */
    private final long expireAfterWrite;
    /**
     * Global expire-after-access duration in milliseconds.
     */
    private final long expireAfterAccess;
    /**
     * Millisecond time source.
     */
    private final LongSupplier clock;
    /**
     * Whether asynchronous atomic operations are enabled.
     */
    private final boolean atomicMode;
    /**
     * Current periodic prune task.
     */
    private ScheduledFuture<?> pruneFuture;
    /**
     * Atomic-mode terminal state.
     */
    private boolean closed;
    /**
     * Approximate number of queued write tokens.
     */
    private long writeTokenCount;
    /**
     * Total read request count.
     */
    private final LongAdder requestCount = new LongAdder();
    /**
     * Successful read count.
     */
    private final LongAdder hitCount = new LongAdder();
    /**
     * Monotonic sequence assigned to write tokens.
     */
    private final AtomicLong writeSequence = new AtomicLong();
    /**
     * Monitor protecting the per-instance prune future.
     */
    private final Object pruneMonitor = new Object();
    /**
     * Stored cache entries.
     */
    private final ConcurrentHashMap<K, Entry<V>> cache;
    /**
     * Independent numeric counters.
     */
    private final ConcurrentHashMap<K, AtomicLong> counters = new ConcurrentHashMap<>();
    /**
     * Write-order tokens used for bounded eviction.
     */
    private final Queue<WriteToken<K, V>> writeOrder = new ConcurrentLinkedQueue<>();
    /**
     * Serializes mutations that update entry and eviction state together.
     */
    private final ReentrantLock mutationLock = new ReentrantLock();
    /**
     * Coordinates atomic operations with atomic-mode close.
     */
    private final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();

    /**
     * Constructs a cache with 1000 entries, a three-minute write lifetime, and periodic pruning. Expire-after-access is
     * disabled and the initial map capacity is 16.
     */
    public MemoryCache() {
        this(1000L, 180_000L, 0L, 16, true, System::currentTimeMillis, false);
    }

    /**
     * Constructs a cache with a maximum size and global write lifetime.
     *
     * @param size   maximum number of entries
     * @param expire non-negative global write lifetime in milliseconds; zero disables the global write limit
     * @throws IllegalArgumentException when {@code size} is not positive or {@code expire} is negative
     */
    public MemoryCache(long size, long expire) {
        this(size, expire, 0L, 16, true, System::currentTimeMillis, false);
    }

    /**
     * Constructs a cache with explicit periodic pruning behavior.
     *
     * @param size         maximum number of entries
     * @param expire       non-negative global write lifetime in milliseconds; zero disables the global write limit
     * @param pruneEnabled whether periodic pruning is enabled
     * @throws IllegalArgumentException when {@code size} is not positive or {@code expire} is negative
     */
    public MemoryCache(long size, long expire, boolean pruneEnabled) {
        this(size, expire, 0L, 16, pruneEnabled, System::currentTimeMillis, false);
    }

    /**
     * Constructs a cache from properties. The optional {@code prefix} property is prepended to {@code maximumSize},
     * {@code expireAfterWrite}, {@code expireAfterAccess}, {@code initialCapacity}, and {@code schedulePrune}. Defaults
     * are respectively 1000, 180000, 0, 16, and {@code true}.
     *
     * @param properties non-null cache properties
     * @throws NullPointerException     when {@code properties} is {@code null}
     * @throws NumberFormatException    when a numeric property is malformed
     * @throws IllegalArgumentException when a parsed size or capacity is not positive, or an expiration is negative
     */
    public MemoryCache(Properties properties) {
        this(configuration(properties));
    }

    /**
     * Constructs an unbounded atomic cache with a caller-controlled millisecond clock. Periodic pruning, global TTL,
     * and TTI are disabled; atomic methods require their own positive per-entry TTL.
     *
     * @param clock non-null millisecond time source
     * @throws NullPointerException when {@code clock} is {@code null}
     */
    public MemoryCache(LongSupplier clock) {
        this(Long.MAX_VALUE, 0L, 0L, 16, false, clock, true);
    }

    /**
     * Constructs a cache from validated configuration.
     *
     * @param configuration validated configuration
     */
    private MemoryCache(Configuration configuration) {
        this(configuration.maximumSize, configuration.expireAfterWrite, configuration.expireAfterAccess,
                configuration.initialCapacity, configuration.pruneEnabled, System::currentTimeMillis, false);
    }

    /**
     * Constructs a fully specified cache.
     *
     * @param maximumSize       positive maximum entry count
     * @param expireAfterWrite  non-negative global write expiration in milliseconds
     * @param expireAfterAccess non-negative global access expiration in milliseconds
     * @param initialCapacity   positive initial map capacity
     * @param pruneEnabled      periodic prune flag
     * @param clock             non-null millisecond time source
     * @param atomicMode        atomic-operation flag
     * @throws NullPointerException     when {@code clock} is {@code null}
     * @throws IllegalArgumentException when a size or capacity is not positive, or an expiration is negative
     */
    MemoryCache(long maximumSize, long expireAfterWrite, long expireAfterAccess, int initialCapacity,
            boolean pruneEnabled, LongSupplier clock, boolean atomicMode) {
        requirePositive(maximumSize, "maximumSize");
        requireNonNegative(expireAfterWrite, "expireAfterWrite");
        requireNonNegative(expireAfterAccess, "expireAfterAccess");
        requirePositive(initialCapacity, "initialCapacity");
        this.maximumSize = maximumSize;
        this.expireAfterWrite = expireAfterWrite;
        this.expireAfterAccess = expireAfterAccess;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.atomicMode = atomicMode;
        this.cache = new ConcurrentHashMap<>(initialCapacity);
        if (pruneEnabled) {
            schedulePrune(pruneDelay(expireAfterWrite, expireAfterAccess));
        }
        Logger.info(
                false,
                "Cache",
                "Memory cache initialized: maximumSize={}, expireAfterWriteMs={}, expireAfterAccessMs={}, initialCapacity={}, schedulePrune={}",
                maximumSize,
                expireAfterWrite,
                expireAfterAccess,
                initialCapacity,
                pruneEnabled);
    }

    /**
     * Reads one live value and refreshes its last-access time when expire-after-access is enabled. An expired entry is
     * removed lazily before this method returns.
     *
     * @param key non-null cache key
     * @return cached value, or {@code null} when the key is absent or expired
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public V read(K key) {
        requestCount.increment();
        Entry<V> entry = liveEntry(Objects.requireNonNull(key, "key"), clock.getAsLong(), true, false);
        if (entry == null) {
            return null;
        }
        hitCount.increment();
        return entry.value;
    }

    /**
     * Reads all live values associated with the supplied keys using one time snapshot. Missing and expired entries are
     * omitted, expired entries are removed lazily, and live entries refresh their last-access time when
     * expire-after-access is enabled. Each supplied key contributes one request to the statistics, including duplicate
     * keys.
     *
     * @param keys non-null collection of non-null cache keys
     * @return map containing every requested key that had a live value; an empty map when {@code keys} is empty
     * @throws NullPointerException when the collection or one of its keys is {@code null}
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        Objects.requireNonNull(keys, "keys");
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        long now = clock.getAsLong();
        long hits = 0L;
        Map<K, V> result = new HashMap<>(keys.size());
        for (K key : keys) {
            Entry<V> entry = liveEntry(Objects.requireNonNull(key, "key"), now, true, false);
            if (entry != null) {
                result.put(key, entry.value);
                hits++;
            }
        }
        requestCount.add(keys.size());
        hitCount.add(hits);
        return result;
    }

    /**
     * Writes all entries with the same per-entry lifetime. The batch is serialized with other ordinary writes, uses one
     * write timestamp, and restores the configured capacity after all entries have been installed. When {@code expire}
     * is {@link CacheExpire#NO}, existing values and numeric counters for the supplied keys are removed instead. A
     * {@code null} or empty map is ignored.
     *
     * @param keyValueMap key-value pairs to write; keys and values must not be {@code null}
     * @param expire      per-entry lifetime in milliseconds; {@link CacheExpire#NO} removes the keys and
     *                    {@link CacheExpire#FOREVER} creates entries exempt from expiration
     * @throws NullPointerException     when a supplied key or value is {@code null}
     * @throws IllegalArgumentException when {@code expire} is less than {@link CacheExpire#NO}
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        validateEntryExpire(expire);
        keyValueMap.forEach((key, value) -> {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
        });
        if (expire == CacheExpire.NO) {
            keyValueMap.keySet().forEach(key -> {
                cache.remove(key);
                counters.remove(key);
            });
            return;
        }
        long now = clock.getAsLong();
        mutationLock.lock();
        try {
            keyValueMap.forEach((key, value) -> writeLocked(key, value, expire, now));
            evictOverflow();
            compactWriteOrderIfNeeded();
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Writes or replaces one value and records the write as the newest capacity-eviction candidate. When the insertion
     * exceeds {@code maximumSize}, the oldest current write is evicted. When {@code expire} is {@link CacheExpire#NO},
     * the cached value and independent numeric counter are removed instead.
     *
     * @param key    non-null cache key
     * @param value  non-null value to cache
     * @param expire per-entry lifetime in milliseconds; {@link CacheExpire#NO} removes the key and
     *               {@link CacheExpire#FOREVER} creates an entry exempt from expiration
     * @throws NullPointerException     when {@code key} or {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code expire} is less than {@link CacheExpire#NO}
     */
    @Override
    public void write(K key, V value, long expire) {
        K checkedKey = Objects.requireNonNull(key, "key");
        V checkedValue = Objects.requireNonNull(value, "value");
        validateEntryExpire(expire);
        if (expire == CacheExpire.NO) {
            cache.remove(checkedKey);
            counters.remove(checkedKey);
            return;
        }
        mutationLock.lock();
        try {
            writeLocked(checkedKey, checkedValue, expire, clock.getAsLong());
            evictOverflow();
            compactWriteOrderIfNeeded();
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Tests whether a key currently has a live entry without refreshing its last-access time. An expired entry is
     * removed lazily.
     *
     * @param key non-null cache key
     * @return {@code true} when the key maps to a live entry; otherwise {@code false}
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public boolean containsKey(K key) {
        return liveEntry(Objects.requireNonNull(key, "key"), clock.getAsLong(), false, false) != null;
    }

    /**
     * Scans the cache once and removes entries that are expired at the scan's time snapshot. Unlike the conventional
     * meaning of {@code clear}, this compatibility method preserves all live entries and does not remove numeric
     * counters. The scan does not refresh expire-after-access timestamps.
     */
    @Override
    public void clear() {
        long now = clock.getAsLong();
        int before = cache.size();
        cache.forEach((key, ignored) -> liveEntry(key, now, false, false));
        Logger.debug(
                false,
                "Cache",
                "Memory cache prune completed: beforeSize={}, afterSize={}, removedCount={}",
                before,
                cache.size(),
                before - cache.size());
    }

    /**
     * Removes the cached value and independent numeric counter for every supplied key. Missing keys are ignored. Stale
     * write-order tokens are retained temporarily and skipped or compacted by later writes.
     *
     * @param keys non-null array of non-null keys to remove
     * @throws NullPointerException when the array or one of its keys is {@code null}
     */
    @Override
    public void remove(K... keys) {
        Objects.requireNonNull(keys, "keys");
        for (K key : keys) {
            K checkedKey = Objects.requireNonNull(key, "key");
            cache.remove(checkedKey);
            counters.remove(checkedKey);
        }
    }

    /**
     * Returns live entries whose key string begins with the supplied prefix string. This operation uses one time
     * snapshot, lazily removes matching expired entries, and does not refresh last-access timestamps. Expired entries
     * outside the requested prefix are not inspected.
     *
     * @param prefix non-null key-like prefix; matching uses {@link Object#toString()}
     * @return mutable map containing the matching live entries
     * @throws NullPointerException when {@code prefix} is {@code null}
     */
    @Override
    public Map<K, V> scan(K prefix) {
        String prefixText = Objects.requireNonNull(prefix, "prefix").toString();
        long now = clock.getAsLong();
        Map<K, V> result = new HashMap<>();
        cache.forEach((key, ignored) -> {
            if (key.toString().startsWith(prefixText)) {
                Entry<V> entry = liveEntry(key, now, false, false);
                if (entry != null) {
                    result.put(key, entry.value);
                }
            }
        });
        return result;
    }

    /**
     * Atomically increments the independent numeric counter associated with a key. A missing counter starts at zero, so
     * its first increment returns one. Counters are separate from cached values and do not expire automatically.
     *
     * @param key non-null counter key
     * @return counter value after the increment
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public long increment(K key) {
        return counters.computeIfAbsent(Objects.requireNonNull(key, "key"), ignored -> new AtomicLong())
                .incrementAndGet();
    }

    /**
     * Atomically creates an entry when the key has no entry or its current entry has reached its deadline. This
     * operation is available only to instances created with the {@link #MemoryCache(LongSupplier)} constructor. Mutable
     * byte-array values are copied before storage.
     *
     * @param key       non-null cache key
     * @param value     non-null value to create
     * @param ttlMillis positive per-entry lifetime in milliseconds
     * @return completed stage containing {@code true} when the value was created; a failed unsupported-operation stage
     *         for ordinary cache instances; or a failed stage after an atomic cache is closed
     * @throws NullPointerException     when {@code key} or {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code ttlMillis} is not positive
     */
    @Override
    public CompletionStage<Boolean> create(K key, V value, long ttlMillis) {
        if (!atomicMode) {
            return CacheX.super.create(key, value, ttlMillis);
        }
        requirePositiveTtl(ttlMillis);
        K checkedKey = Objects.requireNonNull(key, "key");
        V checkedValue = Objects.requireNonNull(value, "value");
        return atomicOperation(() -> {
            long now = clock.getAsLong();
            Entry<V> replacement = newEntry(copyValue(checkedValue), ttlMillis, now);
            Entry<V> result = cache.compute(
                    checkedKey,
                    (ignored, current) -> current == null || current.isAtomicExpired(now) ? replacement : current);
            return result == replacement;
        });
    }

    /**
     * Reports whether this instance was constructed in atomic mode.
     *
     * @return {@code true} only for the explicit clock-based atomic configuration
     */
    @Override
    public boolean supports() {
        return atomicMode;
    }

    /**
     * Reads one entry at the atomic cache's deadline boundary and lazily removes it when expired. Mutable byte-array
     * values are copied before being returned. This operation is available only to instances created with the
     * {@link #MemoryCache(LongSupplier)} constructor.
     *
     * @param key non-null cache key
     * @return completed stage containing a defensive value copy or {@code null}; a failed unsupported-operation stage
     *         for ordinary cache instances; or a failed stage after an atomic cache is closed
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public CompletionStage<V> get(K key) {
        if (!atomicMode) {
            return CacheX.super.get(key);
        }
        K checkedKey = Objects.requireNonNull(key, "key");
        return atomicOperation(() -> {
            Entry<V> entry = liveEntry(checkedKey, clock.getAsLong(), false, true);
            return entry == null ? null : copyValue(entry.value);
        });
    }

    /**
     * Atomically removes and returns one entry when it is still live at the atomic deadline boundary. An expired entry
     * is removed but produces {@code null}. Mutable byte-array values are copied before being returned. This operation
     * is available only to instances created with the {@link #MemoryCache(LongSupplier)} constructor.
     *
     * @param key non-null cache key
     * @return completed stage containing the removed value or {@code null}; a failed unsupported-operation stage for
     *         ordinary cache instances; or a failed stage after an atomic cache is closed
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public CompletionStage<V> take(K key) {
        if (!atomicMode) {
            return CacheX.super.take(key);
        }
        K checkedKey = Objects.requireNonNull(key, "key");
        return atomicOperation(() -> {
            long now = clock.getAsLong();
            Entry<V> entry = cache.remove(checkedKey);
            return entry == null || entry.isAtomicExpired(now) ? null : copyValue(entry.value);
        });
    }

    /**
     * Atomically replaces a live entry when its value equals the expected value. Byte arrays are compared by content,
     * and the replacement byte array is copied before storage. An expired current entry is removed. This operation is
     * available only to instances created with the {@link #MemoryCache(LongSupplier)} constructor.
     *
     * @param key       non-null cache key
     * @param expected  non-null expected current value
     * @param update    non-null replacement value
     * @param ttlMillis positive replacement lifetime in milliseconds
     * @return completed stage containing {@code true} when replacement occurred; a failed unsupported-operation stage
     *         for ordinary cache instances; or a failed stage after an atomic cache is closed
     * @throws NullPointerException     when {@code key}, {@code expected}, or {@code update} is {@code null}
     * @throws IllegalArgumentException when {@code ttlMillis} is not positive
     */
    @Override
    public CompletionStage<Boolean> replace(K key, V expected, V update, long ttlMillis) {
        if (!atomicMode) {
            return CacheX.super.replace(key, expected, update, ttlMillis);
        }
        requirePositiveTtl(ttlMillis);
        K checkedKey = Objects.requireNonNull(key, "key");
        V checkedExpected = Objects.requireNonNull(expected, "expected");
        V checkedUpdate = Objects.requireNonNull(update, "update");
        return atomicOperation(() -> {
            long now = clock.getAsLong();
            Entry<V> replacement = newEntry(copyValue(checkedUpdate), ttlMillis, now);
            Entry<V> result = cache.compute(checkedKey, (ignored, current) -> {
                if (current == null || current.isAtomicExpired(now)) {
                    return null;
                }
                return valuesEqual(current.value, checkedExpected) ? replacement : current;
            });
            return result == replacement;
        });
    }

    /**
     * Atomically removes one entry and reports whether it was live at the atomic deadline boundary. An expired entry is
     * still removed but produces {@code false}. This operation is available only to instances created with the
     * {@link #MemoryCache(LongSupplier)} constructor.
     *
     * @param key non-null cache key
     * @return completed stage containing {@code true} when a live entry was deleted; a failed unsupported-operation
     *         stage for ordinary cache instances; or a failed stage after an atomic cache is closed
     * @throws NullPointerException when {@code key} is {@code null}
     */
    @Override
    public CompletionStage<Boolean> delete(K key) {
        if (!atomicMode) {
            return CacheX.super.delete(key);
        }
        K checkedKey = Objects.requireNonNull(key, "key");
        return atomicOperation(() -> {
            long now = clock.getAsLong();
            Entry<V> entry = cache.remove(checkedKey);
            return entry != null && !entry.isAtomicExpired(now);
        });
    }

    /**
     * Replaces this instance's periodic expiry-pruning schedule. A positive delay schedules fixed-delay executions;
     * zero or a negative value only cancels the existing task. Scheduled pruning invokes {@link #clear()} and therefore
     * preserves live entries and numeric counters.
     *
     * @param delay delay between prune runs in milliseconds; non-positive values disable periodic pruning
     */
    public void schedulePrune(long delay) {
        synchronized (pruneMonitor) {
            cancelPruneLocked();
            if (delay > 0L) {
                pruneFuture = CacheScheduler.INSTANCE.schedule(this::clear, delay);
            }
        }
    }

    /**
     * Returns a high-concurrency statistics snapshot. The request and hit values come from independent
     * {@link LongAdder} snapshots and may be momentarily inconsistent with each other while operations are concurrent.
     * Only {@link #read(Object)} and {@link #read(Collection)} contribute to these statistics.
     *
     * @return formatted cache statistics
     */
    public String getStats() {
        long requests = requestCount.sum();
        long hits = hitCount.sum();
        double hitRate = requests == 0L ? 0.0 : (double) hits / requests;
        return String.format(
                "MemoryCacheStats[requests=%d, hits=%d, hitRate=%.2f%%, size=%d]",
                requests,
                hits,
                hitRate * 100,
                cache.size());
    }

    /**
     * Returns the approximate number of physically stored entries. The count can include expired entries that have not
     * yet been pruned and can change concurrently while this method executes.
     *
     * @return approximate entry count
     */
    public long estimatedSize() {
        return cache.mappingCount();
    }

    /**
     * Returns an immutable snapshot containing only entries that are live at one time snapshot. Building the snapshot
     * lazily removes expired entries, does not refresh TTI access times, and does not expose internal entry or eviction
     * metadata. Subsequent cache changes are not reflected in the returned map.
     *
     * @return live value snapshot
     */
    public Map<K, V> getNativeCache() {
        long now = clock.getAsLong();
        Map<K, V> snapshot = new HashMap<>();
        cache.forEach((key, ignored) -> {
            Entry<V> entry = liveEntry(key, now, false, atomicMode);
            if (entry != null) {
                snapshot.put(key, entry.value);
            }
        });
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Cancels this instance's periodic maintenance task. For an ordinary cache, existing values remain available and
     * later reads and writes are still permitted. For an atomic cache, this method additionally waits for admitted
     * atomic operations, marks the instance terminally closed, clears entries and counters, and causes later atomic
     * operations to return failed stages. Repeated calls are safe.
     */
    @Override
    public void close() {
        cancelPrune();
        if (!atomicMode) {
            return;
        }
        lifecycleLock.writeLock().lock();
        try {
            if (!closed) {
                closed = true;
                mutationLock.lock();
                try {
                    cache.clear();
                    counters.clear();
                    writeOrder.clear();
                    writeTokenCount = 0L;
                } finally {
                    mutationLock.unlock();
                }
            }
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    /**
     * Resolves one live entry and performs lazy expiration. Standard TTI checks use
     * {@link ConcurrentHashMap#computeIfPresent(Object, java.util.function.BiFunction)} so expiration and an optional
     * access-time refresh are atomic for the key. Other paths use identity-based conditional removal to avoid deleting
     * a replacement installed concurrently.
     *
     * @param key            non-null cache key
     * @param now            current time in milliseconds
     * @param touch          whether a live standard entry refreshes its last-access time
     * @param atomicBoundary whether expiration uses the inclusive atomic deadline instead of standard TTL and TTI
     * @return current live entry, or {@code null} when absent or expired
     */
    private Entry<V> liveEntry(K key, long now, boolean touch, boolean atomicBoundary) {
        if (expireAfterAccess > 0L && !atomicBoundary) {
            return cache.computeIfPresent(key, (ignored, current) -> {
                if (current.isStandardExpired(now, expireAfterWrite, expireAfterAccess)) {
                    return null;
                }
                if (touch) {
                    current.lastAccessTime = now;
                }
                return current;
            });
        }
        Entry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        boolean expired = atomicBoundary ? entry.isAtomicExpired(now)
                : entry.isStandardExpired(now, expireAfterWrite, expireAfterAccess);
        if (expired) {
            cache.remove(key, entry);
            return null;
        }
        if (touch) {
            entry.lastAccessTime = now;
        }
        return entry;
    }

    /**
     * Installs one entry and appends its exact version to the write-order queue. The caller must hold
     * {@link #mutationLock}; this method does not enforce the capacity limit itself.
     *
     * @param key    non-null cache key
     * @param value  non-null cache value
     * @param expire validated per-entry lifetime in milliseconds
     * @param now    write time in milliseconds
     */
    private void writeLocked(K key, V value, long expire, long now) {
        Entry<V> entry = newEntry(value, expire, now);
        cache.put(key, entry);
        writeOrder.offer(new WriteToken<>(key, entry));
        writeTokenCount++;
    }

    /**
     * Creates an entry with a globally monotonic write sequence used to reconstruct FIFO eviction order.
     *
     * @param value  non-null cache value
     * @param expire per-entry lifetime in milliseconds
     * @param now    write time in milliseconds
     * @return newly allocated entry
     */
    private Entry<V> newEntry(V value, long expire, long now) {
        return new Entry<>(value, expire, now, writeSequence.getAndIncrement());
    }

    /**
     * Repeatedly evicts the oldest current writes until the physical entry count is at most {@link #maximumSize}. The
     * caller must hold {@link #mutationLock}. Batch writes can require several iterations; stale tokens do not count as
     * successful evictions.
     */
    private void evictOverflow() {
        while (cache.mappingCount() > maximumSize && evictOldest()) {
            // Continue until the configured capacity is restored.
        }
    }

    /**
     * Polls FIFO write tokens until one still identifies the current entry for its key, then removes that entry using
     * identity-based conditional removal. Tokens for overwritten or explicitly removed entries are discarded without
     * affecting their newer replacements. The caller must hold {@link #mutationLock}.
     *
     * @return {@code true} when one current entry was removed; {@code false} when no token remains
     */
    private boolean evictOldest() {
        WriteToken<K, V> token;
        while ((token = writeOrder.poll()) != null) {
            writeTokenCount--;
            if (cache.remove(token.key, token.entry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rebuilds FIFO write-order metadata when queued token count exceeds four times the physical entry count, with a
     * minimum threshold of 64. Current entries are sorted by their monotonic write sequence, so compaction preserves
     * eviction order. The caller must hold {@link #mutationLock}.
     */
    private void compactWriteOrderIfNeeded() {
        long threshold = Math.max(64L, cache.mappingCount() * 4L);
        if (writeTokenCount <= threshold) {
            return;
        }
        List<Map.Entry<K, Entry<V>>> entries = new ArrayList<>(cache.entrySet());
        entries.sort(Comparator.comparingLong(entry -> entry.getValue().sequence));
        writeOrder.clear();
        for (Map.Entry<K, Entry<V>> entry : entries) {
            writeOrder.offer(new WriteToken<>(entry.getKey(), entry.getValue()));
        }
        writeTokenCount = entries.size();
    }

    /**
     * Executes one atomic operation synchronously while holding the lifecycle read lock. The returned stage is already
     * completed when this method returns; thrown failures and closed-state rejection are converted to failed stages.
     * Holding the read lock prevents atomic-mode {@link #close()} from clearing state during an admitted operation.
     *
     * @param command non-null atomic cache command
     * @param <T>     result type
     * @return already completed or exceptionally completed operation stage
     */
    private <T> CompletionStage<T> atomicOperation(SupplierX<T> command) {
        lifecycleLock.readLock().lock();
        try {
            if (closed) {
                return CompletableFuture.failedFuture(new IllegalStateException("Atomic cache is closed"));
            }
            return CompletableFuture.completedFuture(command.get());
        } catch (Throwable failure) {
            return CompletableFuture.failedFuture(failure);
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * Cancels this instance's prune task while acquiring {@link #pruneMonitor}. An executing prune is allowed to
     * finish.
     */
    private void cancelPrune() {
        synchronized (pruneMonitor) {
            cancelPruneLocked();
        }
    }

    /**
     * Cancels and clears this instance's scheduled prune future. The caller must hold {@link #pruneMonitor}; an
     * executing prune is not interrupted.
     */
    private void cancelPruneLocked() {
        if (pruneFuture != null) {
            pruneFuture.cancel(false);
            pruneFuture = null;
        }
    }

    /**
     * Parses prefixed cache properties without mutating the supplied object.
     *
     * @param properties non-null cache properties
     * @return parsed configuration; numeric range validation occurs in the full constructor
     * @throws NullPointerException  when {@code properties} is {@code null}
     * @throws NumberFormatException when a numeric property is malformed
     */
    private static Configuration configuration(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        String prefix = properties.getProperty("prefix", Normal.EMPTY);
        long maximumSize = longProperty(properties, prefix + "maximumSize", 1000L);
        long expireAfterWrite = longProperty(properties, prefix + "expireAfterWrite", 180_000L);
        long expireAfterAccess = longProperty(properties, prefix + "expireAfterAccess", 0L);
        int initialCapacity = intProperty(properties, prefix + "initialCapacity", 16);
        String prune = properties.getProperty(prefix + "schedulePrune");
        boolean pruneEnabled = StringKit.isEmpty(prune) || Boolean.parseBoolean(prune);
        return new Configuration(maximumSize, expireAfterWrite, expireAfterAccess, initialCapacity, pruneEnabled);
    }

    /**
     * Reads one long property, returning its default when absent or empty.
     *
     * @param properties   cache properties
     * @param key          property key
     * @param defaultValue default value
     * @return parsed property value or {@code defaultValue}
     * @throws NumberFormatException when a non-empty value is not a valid {@code long}
     */
    private static long longProperty(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        return StringKit.isEmpty(value) ? defaultValue : Long.parseLong(value);
    }

    /**
     * Reads one integer property, returning its default when absent or empty.
     *
     * @param properties   cache properties
     * @param key          property key
     * @param defaultValue default value
     * @return parsed property value or {@code defaultValue}
     * @throws NumberFormatException when a non-empty value is not a valid {@code int}
     */
    private static int intProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        return StringKit.isEmpty(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Chooses the shortest positive global expiration as the prune delay.
     *
     * @param writeExpire  non-negative write expiration in milliseconds
     * @param accessExpire non-negative access expiration in milliseconds
     * @return shortest positive expiration, or zero when both policies are disabled
     */
    private static long pruneDelay(long writeExpire, long accessExpire) {
        if (writeExpire <= 0L) {
            return accessExpire;
        }
        if (accessExpire <= 0L) {
            return writeExpire;
        }
        return Math.min(writeExpire, accessExpire);
    }

    /**
     * Validates a per-entry expiration value accepted by ordinary writes.
     *
     * @param expire lifetime in milliseconds, {@link CacheExpire#NO}, or {@link CacheExpire#FOREVER}
     * @throws IllegalArgumentException when the value is below the no-cache sentinel
     */
    private static void validateEntryExpire(long expire) {
        if (expire < CacheExpire.NO) {
            throw new IllegalArgumentException("expire must be -1, 0, or greater than zero");
        }
    }

    /**
     * Validates the positive TTL required by atomic create and replace operations.
     *
     * @param ttlMillis time to live in milliseconds
     * @throws IllegalArgumentException when {@code ttlMillis} is not positive
     */
    private static void requirePositiveTtl(long ttlMillis) {
        if (ttlMillis <= 0L) {
            throw new IllegalArgumentException("ttlMillis must be greater than zero");
        }
    }

    /**
     * Requires one positive numeric value.
     *
     * @param value value
     * @param name  value name
     * @throws IllegalArgumentException when the value is not positive
     */
    private static void requirePositive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
    }

    /**
     * Requires one non-negative numeric value.
     *
     * @param value value
     * @param name  value name
     * @throws IllegalArgumentException when the value is negative
     */
    private static void requireNonNegative(long value, String name) {
        if (value < 0L) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }

    /**
     * Adds a positive duration to a time value and saturates at {@link Long#MAX_VALUE} instead of overflowing.
     *
     * @param value     base time in milliseconds
     * @param increment non-negative duration in milliseconds
     * @return mathematical sum or {@link Long#MAX_VALUE} when the sum would overflow
     */
    private static long safeAdd(long value, long increment) {
        return increment > 0L && value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
    }

    /**
     * Defensively copies mutable byte-array values and preserves all other value references.
     *
     * @param value source value
     * @param <T>   value type
     * @return copied byte array or the original non-array value
     */
    private static <T> T copyValue(T value) {
        if (value instanceof byte[] bytes) {
            return (T) Arrays.copyOf(bytes, bytes.length);
        }
        return value;
    }

    /**
     * Compares byte arrays by content and all other values through {@link Objects#equals(Object, Object)}.
     *
     * @param current  current value
     * @param expected expected value
     * @return equality result
     */
    private static boolean valuesEqual(Object current, Object expected) {
        if (current instanceof byte[] currentBytes && expected instanceof byte[] expectedBytes) {
            return Arrays.equals(currentBytes, expectedBytes);
        }
        return Objects.equals(current, expected);
    }

    /**
     * Immutable property-derived configuration passed to the validating full constructor.
     */
    private static final class Configuration {

        /**
         * Maximum entry count.
         */
        private final long maximumSize;
        /**
         * Global write expiration.
         */
        private final long expireAfterWrite;
        /**
         * Global access expiration.
         */
        private final long expireAfterAccess;
        /**
         * Initial map capacity.
         */
        private final int initialCapacity;
        /**
         * Periodic prune flag.
         */
        private final boolean pruneEnabled;

        /**
         * Creates one property-derived configuration value.
         *
         * @param maximumSize       maximum entry count
         * @param expireAfterWrite  write expiration in milliseconds
         * @param expireAfterAccess access expiration in milliseconds
         * @param initialCapacity   initial backing-map capacity
         * @param pruneEnabled      periodic prune flag
         */
        private Configuration(long maximumSize, long expireAfterWrite, long expireAfterAccess, int initialCapacity,
                boolean pruneEnabled) {
            this.maximumSize = maximumSize;
            this.expireAfterWrite = expireAfterWrite;
            this.expireAfterAccess = expireAfterAccess;
            this.initialCapacity = initialCapacity;
            this.pruneEnabled = pruneEnabled;
        }

    }

    /**
     * Cache entry whose value, write metadata, and deadline are immutable while its TTI access timestamp is volatile.
     *
     * @param <V> value type
     */
    private static final class Entry<V> implements Serializable {

        /**
         * Serialization identifier.
         */
        private static final long serialVersionUID = 2801356892053L;
        /**
         * Stored value.
         */
        private final V value;
        /**
         * Entry creation time.
         */
        private final long writeTime;
        /**
         * Per-entry atomic deadline.
         */
        private final long deadline;
        /**
         * Forever-entry flag.
         */
        private final boolean forever;
        /**
         * Write ordering sequence.
         */
        private final long sequence;
        /**
         * Most recent standard-cache access time.
         */
        private volatile long lastAccessTime;

        /**
         * Creates one cache entry and computes its saturated absolute per-entry deadline. A {@link CacheExpire#FOREVER}
         * entry receives {@link Long#MAX_VALUE} and is exempt from all standard expiration policies.
         *
         * @param value    stored value
         * @param expire   per-entry lifetime in milliseconds
         * @param now      creation time in milliseconds
         * @param sequence monotonic write sequence
         */
        private Entry(V value, long expire, long now, long sequence) {
            this.value = value;
            this.writeTime = now;
            this.lastAccessTime = now;
            this.forever = expire == CacheExpire.FOREVER;
            this.deadline = forever ? Long.MAX_VALUE : safeAdd(now, expire);
            this.sequence = sequence;
        }

        /**
         * Tests per-entry TTL, global write TTL, and global access TTI using the standard cache's exclusive boundary:
         * an entry expires only when {@code now} is greater than a deadline. Forever entries always remain live.
         *
         * @param now                current time in milliseconds
         * @param globalWriteExpire  non-negative global write lifetime in milliseconds
         * @param globalAccessExpire non-negative global access lifetime in milliseconds
         * @return {@code true} when any enabled expiration policy has elapsed
         */
        private boolean isStandardExpired(long now, long globalWriteExpire, long globalAccessExpire) {
            if (forever) {
                return false;
            }
            if (now > deadline) {
                return true;
            }
            if (globalWriteExpire > 0L && now > safeAdd(writeTime, globalWriteExpire)) {
                return true;
            }
            return globalAccessExpire > 0L && now > safeAdd(lastAccessTime, globalAccessExpire);
        }

        /**
         * Tests the per-entry atomic deadline using an inclusive boundary. Global standard-cache TTL and TTI policies
         * are intentionally not applied to atomic-mode entries.
         *
         * @param now current time in milliseconds
         * @return {@code true} when a non-forever entry has reached or passed its deadline
         */
        private boolean isAtomicExpired(long now) {
            return !forever && now >= deadline;
        }

    }

    /**
     * Immutable association between a key and one exact entry version in FIFO write order. Identity-based removal makes
     * a token harmless after its key is overwritten.
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static final class WriteToken<K, V> {

        /**
         * Cache key.
         */
        private final K key;
        /**
         * Exact entry written for the key.
         */
        private final Entry<V> entry;

        /**
         * Creates one version-specific write-order token.
         *
         * @param key   cache key
         * @param entry written entry
         */
        private WriteToken(K key, Entry<V> entry) {
            this.key = key;
            this.entry = entry;
        }

    }

    /**
     * Shared bounded daemon scheduler used for periodic ordinary-cache pruning. Each cache instance owns and can cancel
     * only its own returned {@link ScheduledFuture}.
     */
    private enum CacheScheduler {

        /**
         * Shared scheduler instance.
         */
        INSTANCE;

        /**
         * Worker name sequence.
         */
        private final AtomicInteger taskNumber = new AtomicInteger(1);
        /**
         * Bounded scheduled executor.
         */
        private final ScheduledThreadPoolExecutor executor;

        /**
         * Creates a one- or two-thread daemon scheduler based on available processors and configures canceled tasks for
         * immediate queue removal.
         */
        CacheScheduler() {
            int threads = Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors()));
            this.executor = new ScheduledThreadPoolExecutor(threads, runnable -> {
                Thread thread = new Thread(runnable, "MemoryCache-Prune-" + taskNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            });
            this.executor.setRemoveOnCancelPolicy(true);
            this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        }

        /**
         * Schedules one fixed-delay prune task. Fixed delay prevents a slow full-cache scan from accumulating catch-up
         * executions.
         *
         * @param task  non-null prune task
         * @param delay positive initial and recurring delay in milliseconds
         * @return future that controls the scheduled task
         */
        private ScheduledFuture<?> schedule(Runnable task, long delay) {
            return executor.scheduleWithFixedDelay(task, delay, delay, TimeUnit.MILLISECONDS);
        }

    }

}

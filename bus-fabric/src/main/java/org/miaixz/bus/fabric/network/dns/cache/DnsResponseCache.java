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
package org.miaixz.bus.fabric.network.dns.cache;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics;

/**
 * Sharded in-memory DNS response cache for raw wire-format responses.
 *
 * @author Kimi Liu
 */
public class DnsResponseCache {

    /**
     * Default lower bound for positive non-zero DNS TTL values.
     */
    public static final Duration DEFAULT_MIN_TTL = Duration.ofSeconds(1);

    /**
     * Default maximum estimated cache memory.
     */
    public static final long DEFAULT_MAX_ESTIMATED_BYTES = 64L * 1024L * 1024L;

    /**
     * Number of independent LRU shards.
     */
    private static final int SHARD_COUNT = 16;

    /**
     * Number of slots in each shard timing wheel.
     */
    private static final int TIMING_WHEEL_SLOTS = 64;

    /**
     * Estimated fixed memory overhead for one cache entry.
     */
    private static final long ENTRY_OVERHEAD_BYTES = 192L;

    /**
     * Estimated bytes per Java character in cache key strings.
     */
    private static final long CHARACTER_BYTES = 2L;

    /**
     * Disabled cache singleton.
     */
    private static final DnsResponseCache DISABLED = new DnsResponseCache(0, DEFAULT_MIN_TTL, Duration.ofSeconds(1),
            Duration.ZERO, Duration.ZERO, DEFAULT_MAX_ESTIMATED_BYTES, DnsMetrics.disabled());

    /**
     * Maximum retained entries.
     */
    private final int maxEntries;

    /**
     * Lower bound applied to positive non-zero DNS TTL values.
     */
    private final Duration minTtl;

    /**
     * Configured response-cache TTL cap and fallback.
     */
    private final Duration ttl;

    /**
     * Extra time during which expired entries may be served after a refresh failure.
     */
    private final Duration serveStaleTtl;

    /**
     * Time before expiry at which an active hit should trigger a background prefetch.
     */
    private final Duration prefetchBeforeExpiry;

    /**
     * Maximum estimated memory retained by entries.
     */
    private final long maxEstimatedBytes;

    /**
     * Optional DNS Server metrics facade.
     */
    private final DnsMetrics metrics;

    /**
     * Nanosecond duration represented by one timing-wheel slot.
     */
    private final long wheelTickNanos;

    /**
     * Response-cache shards.
     */
    private final Shard[] shards;

    /**
     * Global entry count across all shards.
     */
    private final AtomicInteger entryCount;

    /**
     * Global estimated memory across all shards.
     */
    private final AtomicLong estimatedBytes;

    /**
     * Creates a cache.
     *
     * @param maxEntries maximum retained entries, or zero to disable caching
     * @param ttl        response-cache TTL cap and fallback
     */
    public DnsResponseCache(final int maxEntries, final Duration ttl) {
        this(maxEntries, ttl, Duration.ZERO, Duration.ZERO);
    }

    /**
     * Creates a cache.
     *
     * @param maxEntries           maximum retained entries, or zero to disable caching
     * @param ttl                  response-cache TTL cap and fallback
     * @param serveStaleTtl        extra time during which expired entries may be served after a refresh failure
     * @param prefetchBeforeExpiry time before expiry at which active hits should trigger background prefetch
     */
    public DnsResponseCache(final int maxEntries, final Duration ttl, final Duration serveStaleTtl,
            final Duration prefetchBeforeExpiry) {
        this(maxEntries, defaultMinTtl(ttl), ttl, serveStaleTtl, prefetchBeforeExpiry, DEFAULT_MAX_ESTIMATED_BYTES,
                DnsMetrics.disabled());
    }

    /**
     * Creates a cache with optional DNS metrics.
     *
     * @param maxEntries           maximum retained entries, or zero to disable caching
     * @param ttl                  response-cache TTL cap and fallback
     * @param serveStaleTtl        extra time during which expired entries may be served after a refresh failure
     * @param prefetchBeforeExpiry time before expiry at which active hits should trigger background prefetch
     * @param metrics              optional DNS Server metrics facade
     */
    public DnsResponseCache(final int maxEntries, final Duration ttl, final Duration serveStaleTtl,
            final Duration prefetchBeforeExpiry, final DnsMetrics metrics) {
        this(maxEntries, defaultMinTtl(ttl), ttl, serveStaleTtl, prefetchBeforeExpiry, DEFAULT_MAX_ESTIMATED_BYTES,
                metrics);
    }

    /**
     * Creates a cache with a custom memory ceiling.
     *
     * @param maxEntries           maximum retained entries, or zero to disable caching
     * @param ttl                  response-cache TTL cap and fallback
     * @param serveStaleTtl        extra time during which expired entries may be served after a refresh failure
     * @param prefetchBeforeExpiry time before expiry at which active hits should trigger background prefetch
     * @param maxEstimatedBytes    maximum estimated entry memory
     */
    public DnsResponseCache(final int maxEntries, final Duration ttl, final Duration serveStaleTtl,
            final Duration prefetchBeforeExpiry, final long maxEstimatedBytes) {
        this(maxEntries, defaultMinTtl(ttl), ttl, serveStaleTtl, prefetchBeforeExpiry, maxEstimatedBytes,
                DnsMetrics.disabled());
    }

    /**
     * Creates a cache with full cache policy controls.
     *
     * @param maxEntries           maximum retained entries, or zero to disable caching
     * @param minTtl               lower bound for positive non-zero DNS TTL values
     * @param ttl                  response-cache TTL cap and fallback
     * @param serveStaleTtl        extra time during which expired entries may be served after a refresh failure
     * @param prefetchBeforeExpiry time before expiry at which active hits should trigger background prefetch
     * @param maxEstimatedBytes    maximum estimated entry memory
     */
    public DnsResponseCache(final int maxEntries, final Duration minTtl, final Duration ttl,
            final Duration serveStaleTtl, final Duration prefetchBeforeExpiry, final long maxEstimatedBytes) {
        this(maxEntries, minTtl, ttl, serveStaleTtl, prefetchBeforeExpiry, maxEstimatedBytes, DnsMetrics.disabled());
    }

    /**
     * Creates a cache with full cache policy controls and optional DNS metrics.
     *
     * @param maxEntries           maximum retained entries, or zero to disable caching
     * @param minTtl               lower bound for positive non-zero DNS TTL values
     * @param ttl                  response-cache TTL cap and fallback
     * @param serveStaleTtl        extra time during which expired entries may be served after a refresh failure
     * @param prefetchBeforeExpiry time before expiry at which active hits should trigger background prefetch
     * @param maxEstimatedBytes    maximum estimated entry memory
     * @param metrics              optional DNS Server metrics facade
     */
    public DnsResponseCache(final int maxEntries, final Duration minTtl, final Duration ttl,
            final Duration serveStaleTtl, final Duration prefetchBeforeExpiry, final long maxEstimatedBytes,
            final DnsMetrics metrics) {
        if (maxEntries < 0) {
            throw new ValidateException("DNS cache max entries must be non-negative");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new ValidateException("DNS cache ttl must be positive");
        }
        if (minTtl == null || minTtl.isNegative() || minTtl.isZero()) {
            throw new ValidateException("DNS cache min ttl must be positive");
        }
        if (minTtl.compareTo(ttl) > 0) {
            throw new ValidateException("DNS cache min ttl must not exceed cache ttl");
        }
        if (serveStaleTtl == null || serveStaleTtl.isNegative()) {
            throw new ValidateException("DNS cache serve stale ttl must be non-negative");
        }
        if (prefetchBeforeExpiry == null || prefetchBeforeExpiry.isNegative()) {
            throw new ValidateException("DNS cache prefetch window must be non-negative");
        }
        if (!prefetchBeforeExpiry.isZero() && prefetchBeforeExpiry.compareTo(ttl) >= 0) {
            throw new ValidateException("DNS cache prefetch window must be shorter than cache ttl");
        }
        if (maxEstimatedBytes <= 0L) {
            throw new ValidateException("DNS cache max estimated bytes must be positive");
        }
        if (metrics == null) {
            throw new ValidateException("DNS cache metrics must not be null");
        }
        this.maxEntries = maxEntries;
        this.minTtl = minTtl;
        this.ttl = ttl;
        this.serveStaleTtl = serveStaleTtl;
        this.prefetchBeforeExpiry = prefetchBeforeExpiry;
        this.maxEstimatedBytes = maxEstimatedBytes;
        this.metrics = metrics;
        this.wheelTickNanos = wheelTickNanos(ttl, serveStaleTtl);
        this.entryCount = new AtomicInteger();
        this.estimatedBytes = new AtomicLong();
        this.shards = newShards();
    }

    /**
     * Returns a disabled cache.
     *
     * @return disabled cache
     */
    public static DnsResponseCache disabled() {
        return DISABLED;
    }

    /**
     * Estimates memory retained by one response-cache entry.
     *
     * @param key      cache key
     * @param response response bytes
     * @return estimated retained bytes
     */
    public static long estimateEntryBytes(final DnsCacheKey key, final byte[] response) {
        if (key == null) {
            throw new ValidateException("DNS cache memory estimate key must not be null");
        }
        if (response == null) {
            throw new ValidateException("DNS cache memory estimate response must not be null");
        }
        return saturatedAdd(
                ENTRY_OVERHEAD_BYTES,
                saturatedAdd(
                        response.length,
                        CHARACTER_BYTES
                                * (long) (key.name().length() + key.viewName().length() + key.ecsScope().length())));
    }

    /**
     * Returns a cached response with the current query id.
     *
     * @param query  current query
     * @param stream true for TCP-style full responses
     * @return cached response bytes, or {@code null}
     */
    public byte[] get(final DnsQuery query, final boolean stream) {
        return get(query, stream, Normal.EMPTY);
    }

    /**
     * Returns a cached response with the current query id.
     *
     * @param query  current query
     * @param stream true for TCP-style full responses
     * @param scope  cache scope such as the selected view name
     * @return cached response bytes, or {@code null}
     */
    public byte[] get(final DnsQuery query, final boolean stream, final String scope) {
        final CachedResponse cached = lookup(query, stream, scope);
        return cached == null || cached.stale() ? null : cached.response();
    }

    /**
     * Returns a cached response, including a stale response within the configured stale window.
     *
     * @param query  current query
     * @param stream true for TCP-style full responses
     * @param scope  cache scope such as the selected view name
     * @return cache hit metadata, or {@code null}
     */
    public CachedResponse lookup(final DnsQuery query, final boolean stream, final String scope) {
        if (maxEntries == 0) {
            metrics.cacheMiss();
            return null;
        }
        final DnsCacheKey key = DnsCacheKey.from(query, stream, scope);
        final CachedResponse cached = shard(key).lookup(key, query, System.nanoTime());
        if (cached == null) {
            metrics.cacheMiss();
        } else if (cached.stale()) {
            metrics.cacheStale();
        } else {
            metrics.cacheHit();
        }
        return cached;
    }

    /**
     * Stores a response for a query.
     *
     * @param query    decoded query
     * @param stream   true for TCP-style full responses
     * @param response response bytes
     */
    public void put(final DnsQuery query, final boolean stream, final byte[] response) {
        put(query, stream, Normal.EMPTY, response);
    }

    /**
     * Stores a response for a query.
     *
     * @param query    decoded query
     * @param stream   true for TCP-style full responses
     * @param scope    cache scope such as the selected view name
     * @param response response bytes
     */
    public void put(final DnsQuery query, final boolean stream, final String scope, final byte[] response) {
        if (maxEntries == 0 || response == null || response.length < 2) {
            return;
        }
        final DnsCacheKey key = DnsCacheKey.from(query, stream, scope);
        final byte[] copy = Arrays.copyOf(response, response.length);
        copy[0] = 0;
        copy[1] = 0;
        final Duration effectiveTtl = effectiveTtl(response);
        if (effectiveTtl.isZero() || effectiveTtl.isNegative()) {
            return;
        }
        final long entryBytes = estimateEntryBytes(key, copy);
        if (entryBytes > maxEstimatedBytes) {
            return;
        }
        final long now = System.nanoTime();
        final long ttlNanos = durationToNanos(effectiveTtl);
        final long expiresAtNanos = saturatedAdd(now, ttlNanos);
        final long staleExpiresAtNanos = saturatedAdd(expiresAtNanos, durationToNanos(serveStaleTtl));
        final long prefetchAtNanos = prefetchBeforeExpiry.isZero() ? Long.MAX_VALUE
                : saturatedAdd(expiresAtNanos, -Math.min(durationToNanos(prefetchBeforeExpiry), ttlNanos));
        shard(key)
                .put(key, new Entry(copy, expiresAtNanos, staleExpiresAtNanos, prefetchAtNanos, entryBytes, now), now);
        evictOverflow();
    }

    /**
     * Clears all entries.
     */
    public void clear() {
        for (final Shard shard : shards) {
            shard.clear();
        }
        entryCount.set(0);
        estimatedBytes.set(0L);
    }

    /**
     * Returns the current number of retained entries.
     *
     * @return retained entry count
     */
    public int entryCount() {
        return entryCount.get();
    }

    /**
     * Returns the current estimated retained bytes.
     *
     * @return estimated retained bytes
     */
    public long estimatedBytes() {
        return estimatedBytes.get();
    }

    /**
     * Returns the configured maximum estimated retained bytes.
     *
     * @return maximum estimated retained bytes
     */
    public long maxEstimatedBytes() {
        return maxEstimatedBytes;
    }

    /**
     * Determines the effective cache TTL for a wire-format response.
     *
     * <p>
     * DNS resource-record TTL values define the normal cache lifetime. The configured cache TTL is retained as an upper
     * bound and as a fallback for malformed responses that are still safe to return to the caller. Positive non-zero
     * TTL values are lifted to the configured lower bound to avoid hot-loop refreshes.
     * </p>
     *
     * @param response wire-format response bytes
     * @return effective cache TTL, or {@link Duration#ZERO} when the response must not be cached
     */
    private Duration effectiveTtl(final byte[] response) {
        try {
            final DnsDecodedResponse decoded = DnsCodec.decodeResponse(response);
            final long responseTtlSeconds = decoded.cacheTtlSeconds();
            if (responseTtlSeconds <= 0L) {
                return Duration.ZERO;
            }
            final Duration responseTtl = Duration.ofSeconds(responseTtlSeconds);
            final Duration capped = responseTtl.compareTo(ttl) < 0 ? responseTtl : ttl;
            return capped.compareTo(minTtl) < 0 ? minTtl : capped;
        } catch (final RuntimeException e) {
            return ttl;
        }
    }

    /**
     * Evicts entries until global entry-count and memory limits are satisfied.
     */
    private void evictOverflow() {
        while (entryCount.get() > maxEntries || estimatedBytes.get() > maxEstimatedBytes) {
            final EvictionCandidate candidate = coldestCandidate();
            if (candidate == null || !candidate.shard().evict(candidate.key(), candidate.entry())) {
                return;
            }
        }
    }

    /**
     * Finds the coldest LRU candidate across all shards.
     *
     * @return coldest eviction candidate, or {@code null}
     */
    private EvictionCandidate coldestCandidate() {
        EvictionCandidate coldest = null;
        for (final Shard shard : shards) {
            final EvictionCandidate candidate = shard.eldest();
            if (candidate != null
                    && (coldest == null || candidate.entry().lastAccessNanos < coldest.entry().lastAccessNanos)) {
                coldest = candidate;
            }
        }
        return coldest;
    }

    /**
     * Returns the shard that owns a key.
     *
     * @param key cache key
     * @return owning shard
     */
    private Shard shard(final DnsCacheKey key) {
        return shards[Math.floorMod(key.hashCode(), shards.length)];
    }

    /**
     * Creates all response-cache shards.
     *
     * @return cache shards
     */
    private Shard[] newShards() {
        final Shard[] created = new Shard[SHARD_COUNT];
        for (int index = 0; index < created.length; index++) {
            created[index] = new Shard();
        }
        return created;
    }

    /**
     * Returns the default lower TTL for a cache TTL.
     *
     * @param ttl cache TTL cap
     * @return default lower TTL not exceeding the cache TTL cap
     */
    private static Duration defaultMinTtl(final Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return DEFAULT_MIN_TTL;
        }
        return DEFAULT_MIN_TTL.compareTo(ttl) <= 0 ? DEFAULT_MIN_TTL : ttl;
    }

    /**
     * Computes the timing-wheel tick size for a cache TTL and stale window.
     *
     * @param ttl           cache TTL cap
     * @param serveStaleTtl serve-stale window
     * @return timing-wheel tick size in nanoseconds
     */
    private static long wheelTickNanos(final Duration ttl, final Duration serveStaleTtl) {
        return Math.max(1L, saturatedAdd(durationToNanos(ttl), durationToNanos(serveStaleTtl)) / TIMING_WHEEL_SLOTS);
    }

    /**
     * Converts a duration to nanoseconds with saturation.
     *
     * @param duration source duration
     * @return nanoseconds, or {@link Long#MAX_VALUE} when conversion overflows
     */
    private static long durationToNanos(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Adds a nanosecond delta to a monotonic timestamp with saturation.
     *
     * @param timestamp monotonic timestamp
     * @param delta     nanosecond delta
     * @return saturated timestamp
     */
    private static long saturatedAdd(final long timestamp, final long delta) {
        if (delta > 0L && timestamp > Long.MAX_VALUE - delta) {
            return Long.MAX_VALUE;
        }
        if (delta < 0L && timestamp < Long.MIN_VALUE - delta) {
            return Long.MIN_VALUE;
        }
        return timestamp + delta;
    }

    /**
     * Copies a cached response and restores the active query identifier.
     *
     * @param entry cached response entry
     * @param query current query
     * @return response bytes for the current query id
     */
    private static byte[] responseWithQueryId(final Entry entry, final DnsQuery query) {
        final byte[] copy = Arrays.copyOf(entry.response, entry.response.length);
        copy[0] = (byte) ((query.id() >>> 8) & 0xff);
        copy[1] = (byte) (query.id() & 0xff);
        return copy;
    }

    /**
     * Cached response metadata returned to the DNS server hot path.
     *
     * @author Kimi Liu
     */
    public static class CachedResponse {

        /**
         * Response bytes with the active query identifier.
         */
        private final byte[] response;

        /**
         * Whether the response is outside the normal TTL and inside the stale window.
         */
        private final boolean stale;

        /**
         * Entry that may be refreshed asynchronously, or {@code null} when prefetch is not due.
         */
        private final Entry prefetchEntry;

        /**
         * Creates cached response metadata.
         *
         * @param response      response bytes with the active query identifier
         * @param stale         whether the response is outside the normal TTL and inside the stale window
         * @param prefetchEntry entry that may be refreshed asynchronously, or {@code null}
         */
        public CachedResponse(final byte[] response, final boolean stale, final Entry prefetchEntry) {
            this.response = response;
            this.stale = stale;
            this.prefetchEntry = prefetchEntry;
        }

        /**
         * Returns the response bytes.
         *
         * @return response bytes with the active query identifier
         */
        public byte[] response() {
            return Arrays.copyOf(response, response.length);
        }

        /**
         * Returns whether the response is stale.
         *
         * @return true when the response is outside the normal TTL and inside the stale window
         */
        public boolean stale() {
            return stale;
        }

        /**
         * Returns whether this hit is inside the automatic prefetch window.
         *
         * @return true when a caller may attempt prefetch
         */
        public boolean prefetchDue() {
            return prefetchEntry != null;
        }

        /**
         * Attempts to acquire the single prefetch slot for this cache entry.
         *
         * @return true when the caller should start a background refresh
         */
        public boolean beginPrefetch() {
            return prefetchEntry != null && prefetchEntry.prefetching.compareAndSet(false, true);
        }

        /**
         * Releases the prefetch slot after a background refresh completes or fails.
         */
        public void finishPrefetch() {
            if (prefetchEntry != null) {
                prefetchEntry.prefetching.set(false);
            }
        }

    }

    /**
     * One response-cache shard with access-order LRU and a lazy timing wheel.
     *
     * @author Kimi Liu
     */
    private final class Shard {

        /**
         * Shard mutation lock.
         */
        private final ReentrantLock lock;

        /**
         * Access-order entries for shard-local LRU.
         */
        private final LinkedHashMap<DnsCacheKey, Entry> entries;

        /**
         * Lazy expiration timing wheel.
         */
        private final ArrayDeque<DnsCacheKey>[] timingWheel;

        /**
         * Last processed timing-wheel tick.
         */
        private long lastWheelTick;

        /**
         * Creates a response-cache shard.
         */
        private Shard() {
            this.lock = new ReentrantLock();
            this.entries = new LinkedHashMap<>(16, 0.75F, true);
            this.timingWheel = newTimingWheel();
            this.lastWheelTick = Long.MIN_VALUE;
        }

        /**
         * Returns a cached response from this shard.
         *
         * @param key   cache key
         * @param query current query
         * @param now   current monotonic time
         * @return cached response metadata, or {@code null}
         */
        private CachedResponse lookup(final DnsCacheKey key, final DnsQuery query, final long now) {
            lock.lock();
            try {
                cleanup(now);
                final Entry entry = entries.get(key);
                if (entry == null) {
                    return null;
                }
                if (entry.staleExpiresAtNanos <= now) {
                    removeIfSame(key, entry);
                    return null;
                }
                entry.lastAccessNanos = now;
                final boolean stale = entry.expiresAtNanos <= now;
                final boolean prefetchDue = !stale && entry.prefetchAtNanos <= now;
                return new CachedResponse(responseWithQueryId(entry, query), stale, prefetchDue ? entry : null);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Stores a response entry in this shard.
         *
         * @param key   cache key
         * @param entry response entry
         * @param now   current monotonic time
         */
        private void put(final DnsCacheKey key, final Entry entry, final long now) {
            lock.lock();
            try {
                cleanup(now);
                final Entry previous = entries.put(key, entry);
                if (previous == null) {
                    entryCount.incrementAndGet();
                } else {
                    estimatedBytes.addAndGet(-previous.estimatedBytes);
                }
                estimatedBytes.addAndGet(entry.estimatedBytes);
                schedule(key, entry.staleExpiresAtNanos);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Clears this shard.
         */
        private void clear() {
            lock.lock();
            try {
                entries.clear();
                for (final ArrayDeque<DnsCacheKey> bucket : timingWheel) {
                    bucket.clear();
                }
                lastWheelTick = Long.MIN_VALUE;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns the coldest shard-local entry.
         *
         * @return eviction candidate, or {@code null}
         */
        private EvictionCandidate eldest() {
            lock.lock();
            try {
                final Iterator<Map.Entry<DnsCacheKey, Entry>> iterator = entries.entrySet().iterator();
                if (!iterator.hasNext()) {
                    return null;
                }
                final Map.Entry<DnsCacheKey, Entry> eldest = iterator.next();
                return new EvictionCandidate(this, eldest.getKey(), eldest.getValue());
            } finally {
                lock.unlock();
            }
        }

        /**
         * Evicts a matching entry.
         *
         * @param key      cache key
         * @param expected expected entry instance
         * @return true when the entry was removed
         */
        private boolean evict(final DnsCacheKey key, final Entry expected) {
            lock.lock();
            try {
                return removeIfSame(key, expected);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Cleans expired entries using the timing wheel.
         *
         * @param now current monotonic time
         */
        private void cleanup(final long now) {
            final long currentTick = now / wheelTickNanos;
            if (lastWheelTick == Long.MIN_VALUE) {
                lastWheelTick = currentTick;
                return;
            }
            if (currentTick <= lastWheelTick) {
                return;
            }
            final long elapsedTicks = currentTick - lastWheelTick;
            final long ticksToProcess = Math.min(elapsedTicks, TIMING_WHEEL_SLOTS);
            for (long offset = 1L; offset <= ticksToProcess; offset++) {
                cleanupSlot((int) Math.floorMod(lastWheelTick + offset, TIMING_WHEEL_SLOTS), now);
            }
            lastWheelTick = currentTick;
        }

        /**
         * Cleans one timing-wheel slot.
         *
         * @param slot slot index
         * @param now  current monotonic time
         */
        private void cleanupSlot(final int slot, final long now) {
            final ArrayDeque<DnsCacheKey> bucket = timingWheel[slot];
            final int scheduled = bucket.size();
            for (int index = 0; index < scheduled; index++) {
                final DnsCacheKey key = bucket.pollFirst();
                if (key == null) {
                    return;
                }
                final Entry entry = entries.get(key);
                if (entry == null) {
                    continue;
                }
                if (entry.staleExpiresAtNanos <= now) {
                    removeIfSame(key, entry);
                } else {
                    schedule(key, entry.staleExpiresAtNanos);
                }
            }
        }

        /**
         * Schedules a key for timing-wheel cleanup.
         *
         * @param key            cache key
         * @param expiresAtNanos discard timestamp
         */
        private void schedule(final DnsCacheKey key, final long expiresAtNanos) {
            timingWheel[(int) Math.floorMod(expiresAtNanos / wheelTickNanos, TIMING_WHEEL_SLOTS)].addLast(key);
        }

        /**
         * Removes an entry when it still matches the expected instance.
         *
         * @param key      cache key
         * @param expected expected entry instance
         * @return true when the entry was removed
         */
        private boolean removeIfSame(final DnsCacheKey key, final Entry expected) {
            final Entry current = entries.get(key);
            if (current != expected) {
                return false;
            }
            entries.remove(key);
            entryCount.decrementAndGet();
            estimatedBytes.addAndGet(-expected.estimatedBytes);
            return true;
        }

        /**
         * Creates an empty timing wheel.
         *
         * @return timing wheel buckets
         */
        private ArrayDeque<DnsCacheKey>[] newTimingWheel() {
            final ArrayDeque<DnsCacheKey>[] wheel = new ArrayDeque[TIMING_WHEEL_SLOTS];
            for (int index = 0; index < wheel.length; index++) {
                wheel[index] = new ArrayDeque<>();
            }
            return wheel;
        }

    }

    /**
     * Cached response entry.
     *
     * @author Kimi Liu
     */
    private static final class Entry {

        /**
         * Response bytes with a zeroed id.
         */
        private final byte[] response;

        /**
         * Monotonic expiry timestamp.
         */
        private final long expiresAtNanos;

        /**
         * Monotonic timestamp after which the stale entry is discarded.
         */
        private final long staleExpiresAtNanos;

        /**
         * Monotonic timestamp after which active hits trigger prefetch.
         */
        private final long prefetchAtNanos;

        /**
         * Estimated retained bytes for this entry.
         */
        private final long estimatedBytes;

        /**
         * Single in-flight prefetch guard.
         */
        private final AtomicBoolean prefetching;

        /**
         * Last successful access timestamp.
         */
        private volatile long lastAccessNanos;

        /**
         * Creates a cached response entry.
         *
         * @param response            response bytes with a zeroed id
         * @param expiresAtNanos      monotonic expiry timestamp
         * @param staleExpiresAtNanos monotonic timestamp after which the stale entry is discarded
         * @param prefetchAtNanos     monotonic timestamp after which active hits trigger prefetch
         * @param estimatedBytes      estimated retained bytes
         * @param lastAccessNanos     last successful access timestamp
         */
        private Entry(final byte[] response, final long expiresAtNanos, final long staleExpiresAtNanos,
                final long prefetchAtNanos, final long estimatedBytes, final long lastAccessNanos) {
            this.response = response;
            this.expiresAtNanos = expiresAtNanos;
            this.staleExpiresAtNanos = staleExpiresAtNanos;
            this.prefetchAtNanos = prefetchAtNanos;
            this.estimatedBytes = estimatedBytes;
            this.prefetching = new AtomicBoolean();
            this.lastAccessNanos = lastAccessNanos;
        }

    }

    /**
     * Candidate selected for global overflow eviction.
     *
     * @param shard owning shard
     * @param key   cache key
     * @param entry cache entry
     * @author Kimi Liu
     */
    private record EvictionCandidate(Shard shard, DnsCacheKey key, Entry entry) {

        /**
         * Creates an eviction candidate.
         *
         * @param shard owning shard
         * @param key   cache key
         * @param entry cache entry
         */
        private EvictionCandidate {
            if (shard == null || key == null || entry == null) {
                throw new ValidateException("DNS cache eviction candidate must be complete");
            }
        }

    }

}

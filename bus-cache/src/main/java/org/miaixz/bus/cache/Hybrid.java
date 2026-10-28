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
package org.miaixz.bus.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.logger.Logger;

/**
 * Cache wrapper that preserves scan, counter and renew semantics when the primary backend does not provide them
 * directly, while delegating conditional replacement to the primary backend.
 *
 * @author Kimi Liu
 */
public class Hybrid implements CacheX<String, Object> {

    /**
     * Default maximum number of local cache entries retained for extended runtime semantics.
     */
    public static final long DEFAULT_MAXIMUM_SIZE = 1024L;

    /**
     * Default expiration used by the local mirror cache.
     */
    public static final long DEFAULT_EXPIRE_MS = 180000L;

    /**
     * Primary cache backend selected by the cache module.
     */
    private final CacheX<String, Object> cache;

    /**
     * Optional local mirror used when the primary backend lacks scan or counter support.
     */
    private final CacheX<String, Object> mirror;

    /**
     * Whether scan and key lookup should be delegated to the primary backend.
     */
    private final boolean primaryScan;

    /**
     * Whether counter operations should be delegated to the primary backend.
     */
    private final boolean primaryCounter;

    /**
     * Creates a cache wrapper around a fully capable delegate.
     *
     * @param cache delegate cache
     */
    public Hybrid(CacheX<String, Object> cache) {
        this(cache, true, true, null);
    }

    /**
     * Creates a cache wrapper around a delegate plus optional capability mirror.
     *
     * @param cache          delegate cache
     * @param primaryScan    whether scan can use the primary backend
     * @param primaryCounter whether increment can use the primary backend
     * @param mirror         local mirror for capability fallback
     */
    public Hybrid(CacheX<String, Object> cache, boolean primaryScan, boolean primaryCounter,
            CacheX<String, Object> mirror) {
        this.cache = cache;
        this.primaryScan = primaryScan;
        this.primaryCounter = primaryCounter;
        this.mirror = mirror;
    }

    /**
     * Reads the authoritative primary value; mirror entries never resurrect missing primary values.
     *
     * @param key cache key
     * @return cached value or {@code null}
     */
    @Override
    public Object read(String key) {
        return cache.read(key);
    }

    /**
     * Reads authoritative values from the primary backend without filling misses from stale mirror payloads.
     *
     * @param keys cache keys
     * @return cached values
     */
    @Override
    public Map<String, Object> read(Collection<String> keys) {
        return cache.read(keys);
    }

    /**
     * Returns whether the primary backend has a live value for the key.
     *
     * @param key cache key
     * @return {@code true} when the key exists
     */
    @Override
    public boolean containsKey(String key) {
        return cache.read(key) != null;
    }

    /**
     * Writes one entry to the primary backend and mirrors it when a mirror is configured.
     *
     * @param key    cache key
     * @param value  cache value
     * @param expire expiration in milliseconds
     */
    @Override
    public void write(String key, Object value, long expire) {
        synchronized (cache) {
            cache.write(key, value, expire);
            if (mirror != null) {
                mirror.write(key, value, expire);
            }
        }
    }

    /**
     * Replaces the primary mapping conditionally and reconciles its mirror from the authoritative current value.
     *
     * @param key       cache key
     * @param expected  expected current value
     * @param update    replacement value
     * @param ttlMillis positive replacement lifetime in milliseconds
     * @return stage containing whether the primary value was replaced
     */
    @Override
    public CompletionStage<Boolean> replace(String key, Object expected, Object update, long ttlMillis) {
        Objects.requireNonNull(expected, "expected");
        return replace(key, expected, update, ttlMillis, null, null);
    }

    /**
     * Delegates both comparisons to one primary backend operation. Completion shares the local mirror mutation boundary
     * with ordinary writes and removals. Rejected commands never change mirror expiration; a delayed success mirrors
     * only its own still-current value. Unsupported primary operations remain unsupported; a mirror never substitutes
     * for backend atomicity.
     *
     * @param key        replacement key
     * @param expected   expected current value, or null to require absence
     * @param update     replacement value
     * @param ttlMillis  positive replacement lifetime in milliseconds
     * @param guardKey   additional key, or null for a single-entry comparison
     * @param guardValue expected guard value when guardKey is supplied
     * @return stage containing whether the primary accepted the replacement
     */
    @Override
    public CompletionStage<Boolean> replace(
            String key,
            Object expected,
            Object update,
            long ttlMillis,
            String guardKey,
            Object guardValue) {
        synchronized (cache) {
            CompletionStage<Boolean> result = guardKey == null && expected != null
                    ? cache.replace(key, expected, update, ttlMillis)
                    : cache.replace(key, expected, update, ttlMillis, guardKey, guardValue);
            return result.thenApply(replaced -> {
                if (replaced && mirror != null) {
                    synchronized (cache) {
                        Object current = cache.read(key);
                        if (current == null)
                            mirror.remove(key);
                        else if (Objects.deepEquals(current, update))
                            mirror.write(key, current, ttlMillis);
                    }
                }
                return replaced;
            });
        }
    }

    /**
     * Writes multiple entries to the primary backend and mirrors them when a mirror is configured.
     *
     * @param map    cache entries
     * @param expire expiration in milliseconds
     */
    @Override
    public void write(Map<String, Object> map, long expire) {
        synchronized (cache) {
            cache.write(map, expire);
            if (mirror != null) {
                mirror.write(map, expire);
            }
        }
    }

    /**
     * Removes keys from both the primary backend and the mirror when present.
     *
     * @param keys cache keys
     */
    @Override
    public void remove(String... keys) {
        synchronized (cache) {
            cache.remove(keys);
            if (mirror != null) {
                mirror.remove(keys);
            }
        }
    }

    /**
     * Clears both the primary backend and the mirror when present.
     */
    @Override
    public void clear() {
        synchronized (cache) {
            cache.clear();
            if (mirror != null) {
                mirror.clear();
            }
        }
    }

    /**
     * Scans the primary backend when supported; otherwise uses mirror keys to read current primary values.
     *
     * @param prefix cache key prefix
     * @return matching entries
     */
    @Override
    public Map<String, Object> scan(String prefix) {
        Logger.debug(
                true,
                "Cache",
                "Hybrid cache scan started: primaryScan={}, mirrorPresent={}, prefixPresent={}",
                primaryScan,
                mirror != null,
                prefix != null);
        Map<String, Object> result;
        synchronized (cache) {
            result = primaryScan ? cache.scan(prefix) : mirror == null ? Map.of() : cache.read(mirror.keys(prefix));
        }
        Logger.debug(
                false,
                "Cache",
                "Hybrid cache scan completed: primaryScan={}, mirrorPresent={}, prefixPresent={}, resultCount={}",
                primaryScan,
                mirror != null,
                prefix != null,
                result.size());
        return result;
    }

    /**
     * Returns current primary keys, using the mirror only to enumerate candidates when primary scanning is absent.
     *
     * @param prefix cache key prefix
     * @return matching keys
     */
    @Override
    public List<String> keys(String prefix) {
        Logger.debug(
                true,
                "Cache",
                "Hybrid cache key scan started: primaryScan={}, mirrorPresent={}, prefixPresent={}",
                primaryScan,
                mirror != null,
                prefix != null);
        List<String> result = primaryScan ? cache.keys(prefix) : List.copyOf(scan(prefix).keySet());
        Logger.debug(
                false,
                "Cache",
                "Hybrid cache key scan completed: primaryScan={}, mirrorPresent={}, prefixPresent={}, resultCount={}",
                primaryScan,
                mirror != null,
                prefix != null,
                result.size());
        return result;
    }

    /**
     * Increments a counter on the primary backend when supported, otherwise on the mirror.
     *
     * @param key counter key
     * @return incremented counter value
     */
    @Override
    public long increment(String key) {
        long value = primaryCounter ? cache.increment(key)
                : mirror == null ? cache.increment(key) : mirror.increment(key);
        Logger.debug(
                false,
                "Cache",
                "Hybrid cache counter incremented: primaryCounter={}, mirrorPresent={}, keyPresent={}, counterValue={}",
                primaryCounter,
                mirror != null,
                key != null,
                value);
        return value;
    }

    /**
     * Refreshes TTL on the primary backend and mirror, succeeding when either side is refreshed.
     *
     * @param key    cache key
     * @param expire expiration in milliseconds
     * @return {@code true} when at least one backend refreshed the key
     */
    @Override
    public boolean renew(String key, long expire) {
        synchronized (cache) {
            boolean renewed = cache.renew(key, expire);
            boolean mirrorRenewed = mirror != null && mirror.renew(key, expire);
            boolean result = mirrorRenewed || renewed;
            Logger.debug(
                    false,
                    "Cache",
                    "Hybrid cache ttl renewed: keyPresent={}, expireMs={}, primaryRenewed={}, mirrorRenewed={}, renewed={}",
                    key != null,
                    expire,
                    renewed,
                    mirrorRenewed,
                    result);
            return result;
        }
    }

}

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
package org.miaixz.bus.metrics.builtin;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.metrics.Metrics;

/**
 * Registers cache hit/miss metrics for a {@link CacheX} instance. Conditional: only active when bus-cache is on the
 * classpath.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheMetrics {

    /**
     * Wrap a CacheX with metric tracking. Returns a proxy that records hit/miss/write.
     *
     * @param cache     the target cache
     * @param cacheName logical name used as tag value
     * @param <K>       key type
     * @param <V>       value type
     * @return wrapped cache with metrics
     */
    public static <K, V> CacheX<K, V> instrument(CacheX<K, V> cache, String cacheName) {
        return new InstrumentedCache<>(cache, cacheName);
    }

    private static class InstrumentedCache<K, V> implements CacheX<K, V> {

        /** The underlying cache being instrumented. */
        private final CacheX<K, V> delegate;
        /** Logical cache name used as a tag value on all emitted metrics. */
        private final String cacheName;

        /**
         * Creates an InstrumentedCache wrapping the given delegate.
         *
         * @param delegate  the underlying cache to instrument
         * @param cacheName logical cache name used as a tag value
         */
        InstrumentedCache(CacheX<K, V> delegate, String cacheName) {
            this.delegate = delegate;
            this.cacheName = cacheName;
        }

        /**
         * Reads a single value and records a hit or miss counter.
         *
         * @param key the cache key
         * @return the cached value, or {@code null} if not present
         */
        @Override
        public V read(K key) {
            V value = delegate.read(key);
            if (value != null) {
                Metrics.counter("cache.requests", "cache", cacheName, "result", "hit").increment();
            } else {
                Metrics.counter("cache.requests", "cache", cacheName, "result", "miss").increment();
            }
            return value;
        }

        /**
         * Reads multiple values and records hit/miss counters for each key.
         *
         * @param keys the collection of cache keys to read
         * @return map of found key-value pairs
         */
        @Override
        public java.util.Map<K, V> read(java.util.Collection<K> keys) {
            java.util.Map<K, V> result = delegate.read(keys);
            Metrics.counter("cache.requests", "cache", cacheName, "result", "hit").increment(result.size());
            Metrics.counter("cache.requests", "cache", cacheName, "result", "miss")
                    .increment(keys.size() - result.size());
            return result;
        }

        /**
         * Writes a single key-value pair and records a write counter.
         *
         * @param key    the cache key
         * @param value  the value to cache
         * @param expire TTL in milliseconds
         */
        @Override
        public void write(K key, V value, long expire) {
            delegate.write(key, value, expire);
            Metrics.counter("cache.writes", "cache", cacheName).increment();
        }

        /**
         * Writes multiple key-value pairs and records a write counter for each entry.
         *
         * @param map    the entries to cache
         * @param expire TTL in milliseconds
         */
        @Override
        public void write(java.util.Map<K, V> map, long expire) {
            delegate.write(map, expire);
            Metrics.counter("cache.writes", "cache", cacheName).increment(map.size());
        }

        /**
         * Removes one or more keys and records a remove counter.
         *
         * @param keys the keys to remove
         */
        @Override
        public void remove(K... keys) {
            delegate.remove(keys);
            Metrics.counter("cache.removes", "cache", cacheName).increment(keys.length);
        }

        /**
         * Clears all entries from the cache and records a clear counter.
         */
        @Override
        public void clear() {
            delegate.clear();
            Metrics.counter("cache.clears", "cache", cacheName).increment();
        }

    }

}

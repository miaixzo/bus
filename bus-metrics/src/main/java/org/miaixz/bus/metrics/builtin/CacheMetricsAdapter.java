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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.cache.Collector;

/**
 * Bridges the {@link org.miaixz.bus.cache.Collector} interface to the bus-metrics observability backend.
 * <p>
 * This adapter has two responsibilities:
 * <ol>
 *   <li><b>Local resettable tracking</b> — maintains per-pattern hit/request {@link LongAdder} pairs so that
 *       {@link #getHitting()}, {@link #reset(String)}, and {@link #resetAll()} behave exactly as callers of the
 *       bus-cache {@code Collector} interface expect.</li>
 *   <li><b>Backend publishing</b> — forwards every increment to {@link org.miaixz.bus.metrics.Metrics} counters
 *       (tagged with {@code pattern}), making cache hit-rate data visible in Prometheus, Micrometer, or
 *       OpenTelemetry dashboards without any additional wiring.</li>
 * </ol>
 * <p>
 * Because bus-metrics already depends on bus-cache (not the other way around), this class can safely implement
 * {@code bus-cache.Collector} without creating a circular dependency.
 * <p>
 * Typical Spring usage:
 * <pre>{@code
 * @Bean
 * public CacheMetricsAdapter cacheMetricsAdapter() {
 *     return new CacheMetricsAdapter();
 * }
 *
 * @Bean
 * public Context cacheContext(CacheMetricsAdapter adapter) {
 *     return Context.newBuilder()
 *         .hitting(adapter)
 *         .build();
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheMetricsAdapter implements Collector {

    /**
     * Per-pattern local counters: index 0 = hit count, index 1 = request count.
     * <p>
     * {@link LongAdder} is chosen over {@link java.util.concurrent.atomic.AtomicLong} because it has lower
     * contention under high concurrency — critical for hot cache paths where many threads call
     * {@link #reqIncr}/{@link #hitIncr} simultaneously.
     */
    private final ConcurrentHashMap<String, LongAdder[]> registry = new ConcurrentHashMap<>();

    /**
     * Increments the total request count for a cache pattern and publishes to the metrics backend.
     *
     * @param pattern the cache pattern name (derived from {@code @Cached} prefix + key SpEL expressions)
     * @param count   the number of requests to add; should be positive
     */
    @Override
    public void reqIncr(String pattern, int count) {
        getOrCreate(pattern)[1].add(count);
        org.miaixz.bus.metrics.Metrics.counter("cache.requests", "pattern", pattern).increment(count);
    }

    /**
     * Increments the hit count for a cache pattern and publishes to the metrics backend.
     *
     * @param pattern the cache pattern name
     * @param count   the number of hits to add; should be positive
     */
    @Override
    public void hitIncr(String pattern, int count) {
        getOrCreate(pattern)[0].add(count);
        org.miaixz.bus.metrics.Metrics.counter("cache.hits", "pattern", pattern).increment(count);
    }

    /**
     * Returns a snapshot of current hit-rate statistics for all observed patterns, plus a global summary entry.
     * <p>
     * The returned map uses insertion order ({@link LinkedHashMap}), with per-pattern entries first and the
     * {@link #summaryName()} aggregate last.
     *
     * @return map of pattern name → {@link Snapshot}; never {@code null}
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        Map<String, Snapshot> result = new LinkedHashMap<>(registry.size() + 1);
        long totalHit = 0;
        long totalReq = 0;
        for (Map.Entry<String, LongAdder[]> entry : registry.entrySet()) {
            long hit = entry.getValue()[0].sum();
            long req = entry.getValue()[1].sum();
            result.put(entry.getKey(), Snapshot.newInstance(hit, req));
            totalHit += hit;
            totalReq += req;
        }
        result.put(summaryName(), Snapshot.newInstance(totalHit, totalReq));
        return result;
    }

    /**
     * Resets the local hit and request counters for a specific pattern.
     * <p>
     * Note: this does <em>not</em> reset the cumulative counters already published to the metrics backend
     * (Prometheus, Micrometer, etc.), as those backends treat counters as monotonically increasing.
     *
     * @param pattern the cache pattern name to reset
     */
    @Override
    public void reset(String pattern) {
        registry.remove(pattern);
    }

    /**
     * Resets the local counters for all patterns.
     * <p>
     * As with {@link #reset(String)}, previously published backend counter values are unaffected.
     */
    @Override
    public void resetAll() {
        registry.clear();
    }

    /**
     * Returns the pair of {@link LongAdder}s for the given pattern, creating them atomically if absent.
     * <p>
     * Index 0 holds the hit counter; index 1 holds the request counter.
     *
     * @param pattern the cache pattern key
     * @return a two-element {@link LongAdder} array (never {@code null})
     */
    private LongAdder[] getOrCreate(String pattern) {
        return registry.computeIfAbsent(pattern, k -> new LongAdder[] { new LongAdder(), new LongAdder() });
    }

}

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
package org.miaixz.bus.vortex.metric;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Monitor;
import org.miaixz.bus.vortex.magic.Metrics;

/**
 * Default in-memory implementation of {@link Monitor}.
 * <p>
 * Provides a simple in-memory implementation for monitoring Vortex gateway performance metrics. Zero external
 * dependencies, suitable for quick startup and development environments.
 * </p>
 *
 * <p>
 * <b>Features:</b>
 * </p>
 * <ul>
 * <li>Pure in-memory implementation with zero external dependencies</li>
 * <li>Thread-safe AtomicLong counters</li>
 * <li>Suitable for development and testing environments</li>
 * <li>Singleton instance available via {@link #INSTANCE}</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 * 
 * <pre>
 * // Use singleton instance (recommended for most cases)
 * Monitor monitor = DefaultMonitor.INSTANCE;
 * registry.setMonitor(monitor);
 *
 * // Or create a new instance if needed
 * Monitor monitor = new DefaultMonitor();
 * </pre>
 *
 * <p>
 * <b>Production Environment Recommendations:</b>
 * </p>
 * <ul>
 * <li>Integrate with Micrometer + Prometheus</li>
 * <li>Use Grafana for visualization</li>
 * <li>Configure alerting rules</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultMonitor implements Monitor {

    /**
     * Creates an in-memory monitor.
     */
    public DefaultMonitor() {
    }

    /**
     * Singleton instance of DefaultMonitor.
     * <p>
     * This instance can be shared across the application for centralized monitoring. It is thread-safe and suitable for
     * most use cases where a single monitoring instance is sufficient.
     * </p>
     * <p>
     * Example usage:
     * </p>
     * 
     * <pre>
     * Monitor monitor = DefaultMonitor.INSTANCE;
     * cacheManager.setPerformanceMonitor(monitor);
     * </pre>
     */
    public static final DefaultMonitor INSTANCE = new DefaultMonitor();

    /**
     * Total request counter.
     */
    private final AtomicLong requestCount = new AtomicLong(0);

    /**
     * Success counter.
     */
    private final AtomicLong successCount = new AtomicLong(0);

    /**
     * Failure counter.
     */
    private final AtomicLong failureCount = new AtomicLong(0);

    /**
     * Total duration in nanoseconds.
     */
    private final AtomicLong totalDurationNs = new AtomicLong(0);

    /**
     * Cache hit counter.
     */
    private final AtomicLong cacheHits = new AtomicLong(0);

    /**
     * Cache miss counter.
     */
    private final AtomicLong cacheMisses = new AtomicLong(0);

    /**
     * Database operation counter.
     */
    private final AtomicLong dbOperationCount = new AtomicLong(0);

    /**
     * Total database duration in nanoseconds.
     */
    private final AtomicLong dbDurationNs = new AtomicLong(0);

    /**
     * Records a cache access and updates hit or miss counters.
     *
     * @param key           cache key
     * @param hit           whether the lookup hit the cache
     * @param durationNanos lookup duration in nanoseconds
     */
    @Override
    public void access(String key, boolean hit, long durationNanos) {
        if (hit) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
            Logger.debug("Cache miss: key={}, duration={}ns", key, durationNanos);
        }
    }

    /**
     * Records a gateway request outcome and its total duration.
     *
     * @param duration request duration
     * @param success  whether the request completed successfully
     */
    @Override
    public void request(Duration duration, boolean success) {
        requestCount.incrementAndGet();
        totalDurationNs.addAndGet(duration.toNanos());

        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
    }

    /**
     * Records a backend or database operation observed during request processing.
     *
     * @param type     operation category
     * @param duration operation duration
     * @param rowCount affected row count or item count
     */
    @Override
    public void operation(String type, Duration duration, int rowCount) {
        dbOperationCount.incrementAndGet();
        dbDurationNs.addAndGet(duration.toNanos());

        Logger.debug(true, "Operation", "type={}, duration={}ms, rows={}", type, duration.toMillis(), rowCount);
    }

    /**
     * Builds a snapshot of the current in-memory monitoring counters.
     *
     * @return aggregated metrics snapshot
     */
    @Override
    public Metrics getSummary() {
        long requests = requestCount.get();
        long totalNs = totalDurationNs.get();
        long dbOps = dbOperationCount.get();
        long dbNs = dbDurationNs.get();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();

        return Metrics.builder().cpu(0.0).memory(0L).totalRequests(requests).successRequests(successCount.get())
                .failureRequests(failureCount.get()).avgDurationMs(requests > 0 ? totalNs / 1_000_000.0 / requests : 0)
                .p95DurationMs(0.0).p99DurationMs(0.0).cacheHits(hits).cacheMisses(misses)
                .cacheHitRate((hits + misses) > 0 ? (double) hits / (hits + misses) : 0).totalDbOperations(dbOps)
                .avgDbDurationMs(dbOps > 0 ? dbNs / 1_000_000.0 / dbOps : 0).build();
    }

    /**
     * Resets all counters maintained by this in-memory monitor.
     */
    @Override
    public void reset() {
        requestCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        totalDurationNs.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        dbOperationCount.set(0);
        dbDurationNs.set(0);

        Logger.info("Performance monitor statistics reset");
    }

}

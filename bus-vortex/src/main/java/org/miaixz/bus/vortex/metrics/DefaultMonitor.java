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
package org.miaixz.bus.vortex.metrics;

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
 * </ul>
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
 * @since Java 17+
 */
public class DefaultMonitor implements Monitor {

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

    @Override
    public void access(String key, boolean hit, long durationNanos) {
        if (hit) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
            // Log misses for debugging
            Logger.debug("Cache miss: key={}, duration={}ns", key, durationNanos);
        }
    }

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

    @Override
    public void operation(String operation, Duration duration, int rowCount) {
        dbOperationCount.incrementAndGet();
        dbDurationNs.addAndGet(duration.toNanos());

        Logger.debug("DB operation: {}, duration={}ms, rows={}", operation, duration.toMillis(), rowCount);
    }

    @Override
    public Metrics getSummary() {
        long requests = requestCount.get();
        long totalNs = totalDurationNs.get();
        long dbOps = dbOperationCount.get();
        long dbNs = dbDurationNs.get();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();

        return Metrics.builder()
                // System-level metrics (not populated by this monitor)
                .cpu(0.0).memory(0L)
                // Application-level metrics
                .totalRequests(requests).successRequests(successCount.get()).failureRequests(failureCount.get())
                .avgDurationMs(requests > 0 ? totalNs / 1_000_000.0 / requests : 0).p95DurationMs(0.0) // Not calculated
                                                                                                       // in basic
                                                                                                       // implementation
                .p99DurationMs(0.0) // Not calculated in basic implementation
                .cacheHits(hits).cacheMisses(misses)
                .cacheHitRate((hits + misses) > 0 ? (double) hits / (hits + misses) : 0).totalDbOperations(dbOps)
                .avgDbDurationMs(dbOps > 0 ? dbNs / 1_000_000.0 / dbOps : 0).build();
    }

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

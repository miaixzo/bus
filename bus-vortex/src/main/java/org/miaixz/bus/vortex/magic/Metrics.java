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
package org.miaixz.bus.vortex.magic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.vortex.Monitor;
import org.miaixz.bus.vortex.provider.MetricsProvider;

/**
 * Unified metrics data transfer object (DTO) that holds both system-level and application-level performance metrics.
 * <p>
 * This class combines two categories of metrics:
 * <ul>
 * <li><b>System-level Metrics:</b> CPU usage, memory usage - typically populated by {@link MetricsProvider}</li>
 * <li><b>Application-level Metrics:</b> Request statistics, cache performance, database operations - populated by
 * {@link Monitor}</li>
 * </ul>
 * <p>
 * Used in management APIs, health checks, and performance monitoring dashboards.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Data
@Builder
@AllArgsConstructor
public class Metrics {

    /**
     * Creates an empty metrics snapshot.
     */
    public Metrics() {
    }

    /**
     * The CPU usage of the process, represented as a percentage (e.g., 15.5 for 15.5%).
     */
    private double cpu;

    /**
     * The memory usage of the process, represented in bytes.
     */
    private long memory;

    /**
     * Total number of requests processed.
     */
    @Builder.Default
    private long totalRequests = 0;

    /**
     * Number of successful requests.
     */
    @Builder.Default
    private long successRequests = 0;

    /**
     * Number of failed requests.
     */
    @Builder.Default
    private long failureRequests = 0;

    /**
     * Average request duration in milliseconds.
     */
    @Builder.Default
    private double avgDurationMs = 0.0;

    /**
     * 95th percentile request duration in milliseconds.
     */
    @Builder.Default
    private double p95DurationMs = 0.0;

    /**
     * 99th percentile request duration in milliseconds.
     */
    @Builder.Default
    private double p99DurationMs = 0.0;

    /**
     * Total cache hits.
     */
    @Builder.Default
    private long cacheHits = 0;

    /**
     * Total cache misses.
     */
    @Builder.Default
    private long cacheMisses = 0;

    /**
     * Cache hit rate (0.0 - 1.0).
     */
    @Builder.Default
    private double cacheHitRate = 0.0;

    /**
     * Total number of database operations.
     */
    @Builder.Default
    private long totalDbOperations = 0;

    /**
     * Average database operation duration in milliseconds.
     */
    @Builder.Default
    private double avgDbDurationMs = 0.0;

    /**
     * Calculates the success rate (0.0 - 1.0).
     *
     * @return success rate, or 0.0 if no requests
     */
    public double getSuccessRate() {
        return totalRequests > 0 ? (double) successRequests / totalRequests : 0.0;
    }

    /**
     * Calculates the failure rate (0.0 - 1.0).
     *
     * @return failure rate, or 0.0 if no requests
     */
    public double getFailureRate() {
        return totalRequests > 0 ? (double) failureRequests / totalRequests : 0.0;
    }

}

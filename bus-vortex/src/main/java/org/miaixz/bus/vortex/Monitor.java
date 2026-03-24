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
package org.miaixz.bus.vortex;

import java.time.Duration;

import org.miaixz.bus.vortex.magic.Metrics;

/**
 * Performance monitor interface for tracking Vortex gateway metrics.
 * <p>
 * Framework-layer abstract interface for monitoring gateway performance metrics. Provides basic metric collection
 * capabilities, with support for integration with monitoring systems like Micrometer.
 * </p>
 *
 * <p>
 * <b>Monitored Metrics:</b>
 * </p>
 * <ul>
 * <li>Request Statistics: Total requests, success count, failure count</li>
 * <li>Response Times: Average, P95, P99</li>
 * <li>Cache Statistics: Hit rate, miss count</li>
 * <li>Database Operations: Query count, update count</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Monitor {

    /**
     * Records an access event (e.g., cache access).
     *
     * @param key           the resource key
     * @param hit           whether the access was a hit
     * @param durationNanos access duration in nanoseconds
     */
    void access(String key, boolean hit, long durationNanos);

    /**
     * Records a request event.
     *
     * @param duration request duration
     * @param success  whether the request was successful
     */
    void request(Duration duration, boolean success);

    /**
     * Records a database operation.
     *
     * @param type     the operation type (e.g., "SELECT", "INSERT", "UPDATE")
     * @param duration operation duration
     * @param rowCount number of rows affected
     */
    void operation(String type, Duration duration, int rowCount);

    /**
     * Resets all collected statistics.
     */
    void reset();

    /**
     * Retrieves a summary of collected metrics.
     *
     * @return unified metrics containing both system and application-level data
     */
    Metrics getSummary();

}

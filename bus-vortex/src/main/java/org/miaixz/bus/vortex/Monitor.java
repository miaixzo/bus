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
 * @since Java 17+
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
     * @param operation the operation type (e.g., "SELECT", "INSERT", "UPDATE")
     * @param duration  operation duration
     * @param rowCount  number of rows affected
     */
    void operation(String operation, Duration duration, int rowCount);

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

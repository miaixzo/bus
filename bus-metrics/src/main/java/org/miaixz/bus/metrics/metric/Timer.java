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
package org.miaixz.bus.metrics.metric;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * A timer that measures latency distributions.
 * <p>
 * Enhanced beyond Micrometer with:
 * <ul>
 * <li>Multi-window percentiles (1m / 5m / lifetime)</li>
 * <li>{@link #onViolation} SLA breach callback — unique to bus-metrics</li>
 * <li>{@link #snapshot()} for cross-instance aggregation via CortexExporter</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Timer {

    /**
     * Start timing. Returns a {@link Sample} handle; call {@link Sample#stop()} to record.
     *
     * @return a new in-flight timing sample
     */
    Sample start();

    /**
     * Record a duration directly.
     *
     * @param amount duration value
     * @param unit   time unit of {@code amount}
     */
    void record(long amount, TimeUnit unit);

    /** Returns the total number of recordings. */
    long count();

    /**
     * Returns the total time accumulated across all recordings.
     *
     * @param unit desired time unit
     * @return total time in the given unit
     */
    double totalTime(TimeUnit unit);

    /**
     * Returns the maximum recorded duration.
     *
     * @param unit desired time unit
     * @return max duration in the given unit
     */
    double max(TimeUnit unit);

    /**
     * Returns the percentile value over the entire lifetime of this timer.
     *
     * @param p    percentile 0.0–1.0 (e.g. 0.99 for P99)
     * @param unit desired time unit
     * @return estimated percentile value
     */
    double percentile(double p, TimeUnit unit);

    /**
     * Returns the percentile value over the specified rolling window.
     *
     * @param p      percentile 0.0–1.0
     * @param unit   desired time unit
     * @param window rolling window (ONE_MINUTE, FIVE_MINUTES, or LIFETIME)
     */
    double percentile(double p, TimeUnit unit, Window window);

    /**
     * Registers an SLA violation callback. The callback fires on the recording thread when the rolling-window
     * percentile exceeds the threshold. Checked every {@code checkEvery} recordings.
     *
     * <pre>{@code
     * timer.onViolation(0.99, 300, MILLIS, 100, e -> alertSlack("P99 breach: " + e.actualMillis() + "ms"));
     * }</pre>
     *
     * @param percentile 0.0–1.0
     * @param threshold  threshold value
     * @param unit       threshold unit
     * @param checkEvery check once every N recordings
     * @param callback   invoked when percentile exceeds threshold
     * @return this timer (fluent)
     */
    Timer onViolation(
            double percentile,
            long threshold,
            TimeUnit unit,
            int checkEvery,
            Consumer<ViolationEvent> callback);

    /** Returns an atomic snapshot of histogram state for cross-instance aggregation. */
    TimerSnapshot snapshot();

    // ── Rolling window constants ───────────────────────────────────────────

    /** Rolling time window used for multi-window percentile queries. */
    enum Window {
        /** Last 60 seconds. */
        ONE_MINUTE,
        /** Last 300 seconds. */
        FIVE_MINUTES,
        /** All recordings since creation. */
        LIFETIME
    }

    // ── ViolationEvent record ──────────────────────────────────────────────

    /**
     * Carries context about a single SLA violation detected by {@link #onViolation}.
     *
     * @param metricName     name of the timer that fired the violation
     * @param tags           tags associated with the timer
     * @param percentile     the percentile that was checked (e.g. 0.99)
     * @param actualNanos    observed percentile value in nanoseconds
     * @param thresholdNanos configured threshold in nanoseconds
     * @param violatedAt     wall-clock instant of detection
     */
    record ViolationEvent(String metricName, Tag[] tags, double percentile, long actualNanos, long thresholdNanos,
            Instant violatedAt) {

        /** Returns {@link #actualNanos} converted to milliseconds. */
        public double actualMillis() {
            return actualNanos / 1_000_000.0;
        }

        /** Returns {@link #thresholdNanos} converted to milliseconds. */
        public double thresholdMillis() {
            return thresholdNanos / 1_000_000.0;
        }
    }

}

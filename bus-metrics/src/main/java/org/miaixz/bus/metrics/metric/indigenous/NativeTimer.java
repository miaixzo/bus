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
package org.miaixz.bus.metrics.metric.indigenous;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Consumer;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.metric.Sample;
import org.miaixz.bus.metrics.metric.Timer;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Native Timer implementation using T-Digest for accurate tail percentiles and multi-window rolling percentiles
 * (1m/5m/lifetime).
 * <p>
 * Supports {@link #onViolation} SLA breach callbacks — a capability absent from all existing Java metrics libraries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeTimer implements Timer {

    /**
     * Standard Prometheus histogram bucket boundaries in seconds; converted to nanos on use.
     */
    private static final double[] BUCKET_BOUNDS_SECS = Builder.HISTOGRAM_BUCKET_BOUNDS_SECS;

    /**
     * Metric name used in snapshots and registry keys.
     */
    private final String name;
    /**
     * Tags associated with this timer instance.
     */
    private final Tag[] tags;

    /**
     * Total number of recordings.
     */
    private final AtomicLong countTotal = new AtomicLong();
    /**
     * Running sum of all recorded durations in nanoseconds.
     */
    private final DoubleAdder sumNanos = new DoubleAdder();
    /**
     * Maximum recorded duration in nanoseconds.
     */
    private volatile double maxNanos = 0;

    /**
     * T-Digest for accurate quantile estimation over the lifetime of this timer.
     */
    private final TDigest lifetimeDigest = new TDigest();
    /**
     * Rolling 1-minute T-Digest; rotated every 60 seconds by the scheduler.
     */
    private volatile TDigest digest1m = new TDigest();
    /**
     * Rolling 5-minute T-Digest; rotated every 5 minutes by the scheduler.
     */
    private volatile TDigest digest5m = new TDigest();

    /**
     * Per-bucket cumulative counts aligned to {@link #BUCKET_BOUNDS_SECS}.
     */
    private final long[] bucketCounts = new long[BUCKET_BOUNDS_SECS.length];

    /**
     * Registered SLA violation callbacks.
     */
    private final List<ViolationSpec> violations = new ArrayList<>(2);
    /**
     * Counter incremented on each recording; used to throttle violation checks.
     */
    private final AtomicInteger recordsSinceLastCheck = new AtomicInteger();

    /**
     * Create a new NativeTimer.
     *
     * @param name metric name
     * @param tags associated tags
     */
    public NativeTimer(String name, Tag[] tags) {
        this.name = name;
        this.tags = tags;
    }

    /**
     * Start timing. Returns a {@link Sample} handle; call {@link Sample#stop()} to record.
     *
     * @return a new in-flight timing sample
     */
    @Override
    public Sample start() {
        long startNs = System.nanoTime();
        return () -> {
            long durationNs = System.nanoTime() - startNs;
            record(durationNs, TimeUnit.NANOSECONDS);
            return durationNs;
        };
    }

    /**
     * Record a duration directly.
     *
     * @param amount duration value
     * @param unit   time unit of {@code amount}
     */
    @Override
    public void record(long amount, TimeUnit unit) {
        long nanos = unit.toNanos(amount);
        countTotal.incrementAndGet();
        sumNanos.add(nanos);
        synchronized (this) {
            if (nanos > maxNanos) {
                maxNanos = nanos;
            }
        }
        lifetimeDigest.add(nanos);
        digest1m.add(nanos);
        digest5m.add(nanos);
        // Histogram buckets
        double nanosD = nanos;
        for (int i = 0; i < BUCKET_BOUNDS_SECS.length; i++) {
            if (nanosD <= BUCKET_BOUNDS_SECS[i] * 1_000_000_000.0) {
                synchronized (bucketCounts) {
                    bucketCounts[i]++;
                }
            }
        }
        // Check violations
        checkViolations();
    }

    /** Returns the total number of recordings. */
    @Override
    public long count() {
        return countTotal.get();
    }

    /**
     * Returns the total time accumulated across all recordings.
     *
     * @param unit desired time unit
     * @return total time in the given unit
     */
    @Override
    public double totalTime(TimeUnit unit) {
        return sumNanos.sum() / unit.toNanos(1);
    }

    /**
     * Returns the maximum recorded duration.
     *
     * @param unit desired time unit
     * @return max duration in the given unit
     */
    @Override
    public double max(TimeUnit unit) {
        return maxNanos / unit.toNanos(1);
    }

    /**
     * Returns the percentile value over the entire lifetime of this timer.
     *
     * @param p    percentile 0.0–1.0 (e.g. 0.99 for P99)
     * @param unit desired time unit
     * @return estimated percentile value
     */
    @Override
    public double percentile(double p, TimeUnit unit) {
        double nanos = lifetimeDigest.quantile(p);
        return Double.isNaN(nanos) ? 0 : nanos / unit.toNanos(1);
    }

    /**
     * Returns the percentile value over the specified rolling window.
     *
     * @param p      percentile 0.0–1.0
     * @param unit   desired time unit
     * @param window rolling window (ONE_MINUTE, FIVE_MINUTES, or LIFETIME)
     * @return estimated percentile value
     */
    @Override
    public double percentile(double p, TimeUnit unit, Window window) {
        TDigest digest = switch (window) {
            case ONE_MINUTE -> digest1m;
            case FIVE_MINUTES -> digest5m;
            case LIFETIME -> lifetimeDigest;
        };
        double nanos = digest.quantile(p);
        return Double.isNaN(nanos) ? 0 : nanos / unit.toNanos(1);
    }

    /**
     * Registers an SLA violation callback fired when the rolling-window percentile exceeds the threshold.
     *
     * @param percentile 0.0–1.0
     * @param threshold  threshold value
     * @param unit       threshold unit
     * @param checkEvery check once every N recordings
     * @param callback   invoked when percentile exceeds threshold
     * @return this timer (fluent)
     */
    @Override
    public Timer onViolation(
            double percentile,
            long threshold,
            TimeUnit unit,
            int checkEvery,
            Consumer<ViolationEvent> callback) {
        violations.add(new ViolationSpec(percentile, unit.toNanos(threshold), checkEvery, callback));
        return this;
    }

    /** Returns an atomic snapshot of histogram state for cross-instance aggregation. */
    @Override
    public TimerSnapshot snapshot() {
        long[] bucketsCopy;
        synchronized (bucketCounts) {
            bucketsCopy = bucketCounts.clone();
        }
        double[] bounds = new double[BUCKET_BOUNDS_SECS.length];
        for (int i = 0; i < bounds.length; i++) {
            bounds[i] = BUCKET_BOUNDS_SECS[i];
        }
        return new TimerSnapshot(name, tags, countTotal.get(), sumNanos.sum(), maxNanos, bucketsCopy, bounds);
    }

    /** Called by NativeProvider's scheduler every 60 seconds to rotate the 1m digest. */
    public void rotate1m() {
        digest1m = new TDigest();
    }

    /** Called every 5 minutes to rotate the 5m digest. */
    public void rotate5m() {
        digest5m = new TDigest();
    }

    /**
     * Checks all registered violation specs and fires callbacks when thresholds are exceeded.
     */
    private void checkViolations() {
        if (violations.isEmpty()) {
            return;
        }
        int n = recordsSinceLastCheck.incrementAndGet();
        // Check if any spec's checkEvery threshold is crossed
        for (ViolationSpec spec : violations) {
            if (n % spec.checkEvery == 0) {
                double actual = lifetimeDigest.quantile(spec.percentile);
                if (!Double.isNaN(actual) && actual > spec.thresholdNanos) {
                    spec.callback.accept(
                            new ViolationEvent(name, tags, spec.percentile, (long) actual, spec.thresholdNanos,
                                    Instant.now()));
                }
            }
        }
    }

    /**
     * Holds the configuration for a single SLA violation callback.
     *
     * @param percentile     the percentile to monitor (0.0–1.0)
     * @param thresholdNanos the threshold in nanoseconds above which a violation is fired
     * @param checkEvery     fire the check once every N recordings
     * @param callback       the callback to invoke on violation
     */
    private record ViolationSpec(double percentile, long thresholdNanos, int checkEvery,
            Consumer<ViolationEvent> callback) {
    }

}

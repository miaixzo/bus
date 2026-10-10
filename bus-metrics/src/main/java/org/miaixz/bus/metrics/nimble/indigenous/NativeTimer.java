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
package org.miaixz.bus.metrics.nimble.indigenous;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.LongSupplier;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.nimble.Sample;
import org.miaixz.bus.metrics.nimble.Timer;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Native Timer implementation using T-Digest for accurate tail percentiles and multi-window rolling percentiles
 * (1m/5m/lifetime).
 * <p>
 * Supports {@link #onViolation} SLA breach callbacks — a capability absent from all existing Java metrics libraries.
 *
 * @author Kimi Liu
 */
public class NativeTimer implements Timer {

    /**
     * Number of digest buckets retained for each rolling window.
     */
    private static final int WINDOW_BUCKETS = 60;

    /**
     * Width of one bucket in the one-minute window.
     */
    private static final long ONE_MINUTE_BUCKET_NANOS = TimeUnit.SECONDS.toNanos(1);

    /**
     * Width of one bucket in the five-minute window.
     */
    private static final long FIVE_MINUTE_BUCKET_NANOS = TimeUnit.SECONDS.toNanos(5);

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
     * T-Digest for accurate quantile estimation over the lifetime of this timer.
     */
    private final TDigest lifetimeDigest = new TDigest();

    /**
     * Per-bucket cumulative counts aligned to {@link #BUCKET_BOUNDS_SECS}.
     */
    private final long[] bucketCounts = new long[BUCKET_BOUNDS_SECS.length];

    /**
     * Registered SLA violation callbacks.
     */
    private final CopyOnWriteArrayList<ViolationSpec> violations = new CopyOnWriteArrayList<>();

    /**
     * Counter incremented on each recording; used to throttle violation checks.
     */
    private final AtomicInteger recordsSinceLastCheck = new AtomicInteger();
    /**
     * One-minute rolling digest ring with one-second resolution.
     */
    private final DigestBucket[] oneMinuteBuckets = buckets();
    /**
     * Five-minute rolling digest ring with five-second resolution.
     */
    private final DigestBucket[] fiveMinuteBuckets = buckets();
    /**
     * Monotonic nanosecond source used to assign rolling buckets.
     */
    private final LongSupplier nanoClock;
    /**
     * Greatest clock value observed, preventing a custom clock rollback from reviving expired buckets.
     */
    private final AtomicLong lastObservedNanos = new AtomicLong(Long.MIN_VALUE);
    /**
     * Maximum recorded duration in nanoseconds.
     */
    private volatile double maxNanos = 0;

    /**
     * Create a new NativeTimer.
     *
     * @param name metric name
     * @param tags associated tags
     */
    public NativeTimer(String name, Tag[] tags) {
        this(name, tags, System::nanoTime);
    }

    /**
     * Creates a timer with an injectable monotonic clock for deterministic tests.
     *
     * @param name      metric name
     * @param tags      associated tags
     * @param nanoClock monotonic nanosecond source
     */
    NativeTimer(String name, Tag[] tags, LongSupplier nanoClock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Timer name must not be blank");
        }
        this.name = name;
        this.tags = tags == null ? new Tag[0] : tags.clone();
        for (Tag tag : this.tags) {
            Objects.requireNonNull(tag, "Timer tag must not be null");
        }
        this.nanoClock = Objects.requireNonNull(nanoClock, "Timer clock must not be null");
    }

    /**
     * Creates an initialized digest bucket ring.
     *
     * @return fixed-size bucket ring
     */
    private static DigestBucket[] buckets() {
        DigestBucket[] result = new DigestBucket[WINDOW_BUCKETS];
        for (int index = 0; index < result.length; index++) {
            result[index] = new DigestBucket();
        }
        return result;
    }

    /**
     * Validates a percentile argument.
     *
     * @param percentile percentile to validate
     */
    private static void validatePercentile(double percentile) {
        if (!Double.isFinite(percentile) || percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Timer percentile must be between 0 and 1");
        }
    }

    /**
     * Adds a value to the current generation in a digest ring.
     *
     * @param ring        digest ring
     * @param bucketNanos bucket width
     * @param timestamp   observation timestamp
     * @param value       observed duration
     */
    private static void addRolling(DigestBucket[] ring, long bucketNanos, long timestamp, double value) {
        long generation = Math.floorDiv(timestamp, bucketNanos);
        int index = Math.floorMod(generation, ring.length);
        ring[index].add(generation, value);
    }

    /**
     * Merges all live bucket generations into a temporary digest.
     *
     * @param ring        digest ring
     * @param bucketNanos bucket width
     * @param timestamp   read timestamp
     * @return merged rolling digest
     */
    private static TDigest rolling(DigestBucket[] ring, long bucketNanos, long timestamp) {
        long currentGeneration = Math.floorDiv(timestamp, bucketNanos);
        long firstGeneration = currentGeneration - ring.length + 1;
        TDigest merged = new TDigest();
        for (DigestBucket bucket : ring) {
            bucket.mergeInto(merged, firstGeneration, currentGeneration);
        }
        return merged;
    }

    /**
     * Clears every bucket in a digest ring.
     *
     * @param ring digest ring
     */
    private static void clear(DigestBucket[] ring) {
        for (DigestBucket bucket : ring) {
            bucket.clear();
        }
    }

    /**
     * Returns a non-decreasing clock value.
     *
     * @return monotonic nanosecond timestamp
     */
    private long now() {
        return lastObservedNanos.accumulateAndGet(nanoClock.getAsLong(), Math::max);
    }

    /**
     * Start timing. Returns a {@link Sample} handle; call {@link Sample#stop()} to record.
     *
     * @return a new in-flight timing sample
     */
    @Override
    public Sample start() {
        long startNs = now();
        return () -> {
            long durationNs = now() - startNs;
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
        if (amount < 0) {
            throw new IllegalArgumentException("Timer duration must be non-negative");
        }
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer unit must not be null");
        long nanos = checkedUnit.toNanos(amount);
        long timestamp = now();
        countTotal.incrementAndGet();
        sumNanos.add(nanos);
        synchronized (this) {
            if (nanos > maxNanos) {
                maxNanos = nanos;
            }
        }
        lifetimeDigest.add(nanos);
        addRolling(oneMinuteBuckets, ONE_MINUTE_BUCKET_NANOS, timestamp, nanos);
        addRolling(fiveMinuteBuckets, FIVE_MINUTE_BUCKET_NANOS, timestamp, nanos);
        double nanosD = nanos;
        for (int i = 0; i < BUCKET_BOUNDS_SECS.length; i++) {
            if (nanosD <= BUCKET_BOUNDS_SECS[i] * 1_000_000_000.0) {
                synchronized (bucketCounts) {
                    bucketCounts[i]++;
                }
            }
        }
        checkViolations();
    }

    /**
     * Returns the total number of recordings.
     */
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
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer unit must not be null");
        return sumNanos.sum() / checkedUnit.toNanos(1);
    }

    /**
     * Returns the maximum recorded duration.
     *
     * @param unit desired time unit
     * @return max duration in the given unit
     */
    @Override
    public double max(TimeUnit unit) {
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer unit must not be null");
        return maxNanos / checkedUnit.toNanos(1);
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
        validatePercentile(p);
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer unit must not be null");
        double nanos = lifetimeDigest.quantile(p);
        return Double.isNaN(nanos) ? Double.NaN : nanos / checkedUnit.toNanos(1);
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
        validatePercentile(p);
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer unit must not be null");
        Window checkedWindow = Objects.requireNonNull(window, "Timer window must not be null");
        long timestamp = now();
        TDigest digest = switch (checkedWindow) {
            case ONE_MINUTE -> rolling(oneMinuteBuckets, ONE_MINUTE_BUCKET_NANOS, timestamp);
            case FIVE_MINUTES -> rolling(fiveMinuteBuckets, FIVE_MINUTE_BUCKET_NANOS, timestamp);
            case LIFETIME -> lifetimeDigest;
        };
        double nanos = digest.quantile(p);
        return Double.isNaN(nanos) ? Double.NaN : nanos / checkedUnit.toNanos(1);
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
            ConsumerX<ViolationEvent> callback) {
        validatePercentile(percentile);
        if (threshold < 0) {
            throw new IllegalArgumentException("Timer violation threshold must be non-negative");
        }
        if (checkEvery <= 0) {
            throw new IllegalArgumentException("Timer violation interval must be positive");
        }
        TimeUnit checkedUnit = Objects.requireNonNull(unit, "Timer violation unit must not be null");
        ConsumerX<ViolationEvent> checkedCallback = Objects
                .requireNonNull(callback, "Timer violation callback must not be null");
        violations.add(new ViolationSpec(percentile, checkedUnit.toNanos(threshold), checkEvery, checkedCallback));
        return this;
    }

    /**
     * Returns an atomic snapshot of histogram state for cross-instance aggregation.
     */
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
        return new TimerSnapshot(name, tags.clone(), countTotal.get(), sumNanos.sum(), maxNanos, bucketsCopy, bounds);
    }

    /**
     * Explicitly clears the one-minute compatibility window.
     */
    public void rotate1m() {
        clear(oneMinuteBuckets);
    }

    /**
     * Explicitly clears the five-minute compatibility window.
     */
    public void rotate5m() {
        clear(fiveMinuteBuckets);
    }

    /**
     * Checks all registered violation specs and fires callbacks when thresholds are exceeded.
     */
    private void checkViolations() {
        if (violations.isEmpty()) {
            return;
        }
        int n = recordsSinceLastCheck.incrementAndGet();
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
     * @author Kimi Liu
     */
    private record ViolationSpec(double percentile, long thresholdNanos, int checkEvery,
            ConsumerX<ViolationEvent> callback) {

    }

    /**
     * One generation-stamped rolling digest bucket.
     *
     * @author Kimi Liu
     */
    private static final class DigestBucket {

        /**
         * Generation currently stored in this ring slot.
         */
        private long generation = Long.MIN_VALUE;

        /**
         * Digest for the current generation.
         */
        private TDigest digest = new TDigest();

        /**
         * Adds a value, replacing stale slot contents on generation change.
         *
         * @param targetGeneration generation receiving the value
         * @param value            observed value
         */
        private synchronized void add(long targetGeneration, double value) {
            if (generation != targetGeneration) {
                generation = targetGeneration;
                digest = new TDigest();
            }
            digest.add(value);
        }

        /**
         * Merges this bucket when its generation is inside the requested range.
         *
         * @param target          destination digest
         * @param firstGeneration first included generation
         * @param lastGeneration  last included generation
         */
        private synchronized void mergeInto(TDigest target, long firstGeneration, long lastGeneration) {
            if (generation >= firstGeneration && generation <= lastGeneration && digest.count() > 0) {
                target.merge(digest);
            }
        }

        /**
         * Clears this ring slot.
         */
        private synchronized void clear() {
            generation = Long.MIN_VALUE;
            digest = new TDigest();
        }
    }

}

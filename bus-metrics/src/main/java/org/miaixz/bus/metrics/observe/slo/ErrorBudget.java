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
package org.miaixz.bus.metrics.observe.slo;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Computes error budget values from a given SLO target and observed compliance.
 *
 * @author Kimi Liu
 */
public class ErrorBudget {

    /**
     * Fixed number of time buckets retained by every budget.
     */
    private static final int BUCKET_COUNT = 60;

    /**
     * SLO target fraction, e.g. 0.999 for 99.9%.
     */
    private final double target;

    /**
     * Observation window length in milliseconds.
     */
    private final long windowMillis;

    /**
     * Width of one bucket in milliseconds.
     */
    private final long bucketWidthMillis;

    /**
     * Millisecond clock used to assign observations to buckets.
     */
    private final LongSupplier clock;

    /**
     * Fixed ring of rolling-window buckets.
     */
    private final Bucket[] buckets = buckets();

    /**
     * Greatest wall-clock value observed by this budget.
     */
    private long lastObservedMillis = Long.MIN_VALUE;

    /**
     * Create a new error budget tracker.
     *
     * @param target       SLO target as a fraction, e.g. 0.999 for 99.9%
     * @param windowMillis observation window length in milliseconds
     */
    public ErrorBudget(double target, long windowMillis) {
        this(target, windowMillis, System::currentTimeMillis);
    }

    /**
     * Creates an error budget with an injectable millisecond clock for deterministic tests.
     *
     * @param target       SLO target as a fraction
     * @param windowMillis observation window in milliseconds
     * @param clock        millisecond clock
     */
    ErrorBudget(double target, long windowMillis, LongSupplier clock) {
        if (!Double.isFinite(target) || target <= 0 || target > 1) {
            throw new IllegalArgumentException("SLO target must be greater than 0 and no greater than 1");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("SLO window must be positive");
        }
        this.target = target;
        this.windowMillis = windowMillis;
        this.bucketWidthMillis = Math.max(1, 1 + (windowMillis - 1) / BUCKET_COUNT);
        this.clock = Objects.requireNonNull(clock, "SLO clock must not be null");
    }

    /**
     * Creates the fixed bucket ring.
     *
     * @return initialized buckets
     */
    private static Bucket[] buckets() {
        Bucket[] result = new Bucket[BUCKET_COUNT];
        for (int index = 0; index < result.length; index++) {
            result[index] = new Bucket();
        }
        return result;
    }

    /**
     * Record one good (successful) request.
     */
    public synchronized void recordGood() {
        record(true);
    }

    /**
     * Record one bad (failed) request.
     */
    public synchronized void recordBad() {
        record(false);
    }

    /**
     * Records one observation in the current time bucket.
     *
     * @param good whether the observation satisfied the SLO
     */
    private void record(boolean good) {
        long generation = generation(now());
        Bucket bucket = buckets[Math.floorMod(generation, buckets.length)];
        if (bucket.generation != generation) {
            bucket.generation = generation;
            bucket.good = 0;
            bucket.total = 0;
        }
        if (good) {
            bucket.good++;
        }
        bucket.total++;
    }

    /**
     * Returns the observed compliance as a fraction (1.0 = perfect, 0.0 = all bad). Returns 1.0 if no requests have
     * been recorded yet.
     *
     * @return the observed compliance fraction
     */
    public synchronized double compliance() {
        Totals totals = totals();
        return totals.total == 0 ? 1.0 : (double) totals.good / totals.total;
    }

    /**
     * Remaining error budget as a fraction (1.0 = full, 0.0 = exhausted).
     *
     * @return the remaining error budget fraction
     */
    public synchronized double errorBudgetRemaining() {
        Totals totals = totals();
        if (totals.total == 0) {
            return 1.0;
        }
        double allowed = 1.0 - target;
        double actual = 1.0 - (double) totals.good / totals.total;
        if (allowed <= 0) {
            return actual > 0 ? 0.0 : 1.0;
        }
        return Math.max(0.0, 1.0 - actual / allowed);
    }

    /**
     * Burn rate = (actual error rate) / (allowed error rate). Value > 1 means budget is burning faster than allowed.
     *
     * @return the current error budget burn rate
     */
    public synchronized double burnRate() {
        Totals totals = totals();
        if (totals.total == 0) {
            return 0.0;
        }
        double allowed = 1.0 - target;
        double actual = 1.0 - (double) totals.good / totals.total;
        if (allowed <= 0) {
            return actual > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }
        return actual / allowed;
    }

    /**
     * Returns the SLO target fraction, e.g. 0.999.
     *
     * @return the SLO target fraction
     */
    public double target() {
        return target;
    }

    /**
     * Returns the configured logical window length.
     *
     * @return window length in milliseconds
     */
    public long windowMillis() {
        return windowMillis;
    }

    /**
     * Returns a non-decreasing wall-clock value.
     *
     * @return current effective time in milliseconds
     */
    private long now() {
        lastObservedMillis = Math.max(lastObservedMillis, clock.getAsLong());
        return lastObservedMillis;
    }

    /**
     * Converts a timestamp to its bucket generation.
     *
     * @param timestamp epoch millisecond timestamp
     * @return bucket generation
     */
    private long generation(long timestamp) {
        return Math.floorDiv(timestamp, bucketWidthMillis);
    }

    /**
     * Sums all buckets in the current bounded window.
     *
     * @return current good and total counts
     */
    private Totals totals() {
        long current = generation(now());
        long first = current - buckets.length + 1;
        long good = 0;
        long total = 0;
        for (Bucket bucket : buckets) {
            if (bucket.generation >= first && bucket.generation <= current) {
                good += bucket.good;
                total += bucket.total;
            }
        }
        return new Totals(good, total);
    }

    /**
     * Mutable bucket guarded by the enclosing budget monitor.
     */
    private static final class Bucket {

        /**
         * Generation stored in this slot.
         */
        private long generation = Long.MIN_VALUE;

        /**
         * Good observations in this generation.
         */
        private long good;

        /**
         * Total observations in this generation.
         */
        private long total;
    }

    /**
     * Immutable aggregate of the live buckets.
     *
     * @param good  good observation count
     * @param total total observation count
     */
    private record Totals(long good, long total) {
    }

}

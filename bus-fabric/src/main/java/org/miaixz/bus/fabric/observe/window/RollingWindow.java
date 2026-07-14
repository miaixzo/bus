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
package org.miaixz.bus.fabric.observe.window;

import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Bounded rolling sum and count window.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RollingWindow {

    /**
     * Nanoseconds per second.
     */
    private static final double NANOS_PER_SECOND = Normal.GIGA;

    /**
     * Window duration in nanoseconds.
     */
    private final long windowNanos;

    /**
     * Bucket duration in nanoseconds.
     */
    private final long bucketNanos;

    /**
     * Ring buckets.
     */
    private final Bucket[] buckets;

    /**
     * Creates a rolling window.
     *
     * @param windowNanos window nanoseconds
     * @param bucketNanos bucket nanoseconds
     */
    private RollingWindow(final long windowNanos, final long bucketNanos) {
        this.windowNanos = windowNanos;
        this.bucketNanos = bucketNanos;
        this.buckets = new Bucket[(int) (windowNanos / bucketNanos)];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket();
        }
    }

    /**
     * Creates a rolling window.
     *
     * @param window window duration
     * @param bucket bucket duration
     * @return rolling window
     */
    public static RollingWindow of(final Duration window, final Duration bucket) {
        final long windowNanos = validateDuration(window, "Window");
        final long bucketNanos = validateDuration(bucket, "Bucket");
        Assert.isTrue(
                bucketNanos <= windowNanos && windowNanos % bucketNanos == 0,
                () -> new ValidateException("Bucket must divide window and be no larger than window"));
        return new RollingWindow(windowNanos, bucketNanos);
    }

    /**
     * Adds a sample value.
     *
     * @param value value
     * @param time  sample time
     */
    public synchronized void add(final long value, final Instant time) {
        Assert.isTrue(value >= 0, () -> new ValidateException("Window value must be non-negative"));
        final long key = bucketKey(time);
        final Bucket bucket = buckets[index(key)];
        if (bucket.key != key) {
            bucket.reset(key);
        }
        bucket.sum += value;
        bucket.count++;
    }

    /**
     * Returns window sum.
     *
     * @param now current time
     * @return sum
     */
    public synchronized long sum(final Instant now) {
        final long current = bucketKey(now);
        long sum = 0;
        for (final Bucket bucket : buckets) {
            if (active(bucket, current)) {
                sum += bucket.sum;
            }
        }
        return sum;
    }

    /**
     * Returns window count.
     *
     * @param now current time
     * @return count
     */
    public synchronized long count(final Instant now) {
        final long current = bucketKey(now);
        long count = 0;
        for (final Bucket bucket : buckets) {
            if (active(bucket, current)) {
                count += bucket.count;
            }
        }
        return count;
    }

    /**
     * Returns sum rate per second.
     *
     * @param now current time
     * @return rate
     */
    public synchronized double rate(final Instant now) {
        final long total = sum(now);
        return total == 0 ? 0D : total / (windowNanos / NANOS_PER_SECOND);
    }

    /**
     * Resets all buckets.
     */
    public synchronized void reset() {
        for (final Bucket bucket : buckets) {
            bucket.clear();
        }
    }

    /**
     * Returns whether a bucket is active for a current key.
     *
     * @param bucket  bucket
     * @param current current key
     * @return true when active
     */
    private boolean active(final Bucket bucket, final long current) {
        if (bucket.empty()) {
            return false;
        }
        final long oldest = current - buckets.length + 1L;
        if (bucket.key < oldest) {
            bucket.clear();
            return false;
        }
        return bucket.key <= current;
    }

    /**
     * Returns a bucket array index.
     *
     * @param key bucket key
     * @return index
     */
    private int index(final long key) {
        return Math.floorMod(key, buckets.length);
    }

    /**
     * Returns a bucket key.
     *
     * @param time time
     * @return bucket key
     */
    private long bucketKey(final Instant time) {
        final Instant checked = Assert.notNull(time, () -> new ValidateException("Window time must not be null"));
        final long nanos;
        try {
            nanos = Math.addExact(Math.multiplyExact(checked.getEpochSecond(), Normal.GIGA), checked.getNano());
        } catch (final ArithmeticException e) {
            throw new ValidateException("Window time is out of range", e);
        }
        return Math.floorDiv(nanos, bucketNanos);
    }

    /**
     * Validates a positive duration.
     *
     * @param duration duration
     * @param name     name
     * @return nanoseconds
     */
    private static long validateDuration(final Duration duration, final String name) {
        final Duration checked = Assert
                .notNull(duration, () -> new ValidateException(name + " duration must be positive"));
        Assert.isTrue(
                checked.compareTo(Duration.ZERO) > 0,
                () -> new ValidateException(name + " duration must be positive"));
        try {
            return checked.toNanos();
        } catch (final ArithmeticException e) {
            throw new ValidateException(name + " duration is out of range", e);
        }
    }

    /**
     * Rolling bucket state.
     */
    private static final class Bucket {

        /**
         * Bucket key.
         */
        private long key = Long.MIN_VALUE;

        /**
         * Sum value.
         */
        private long sum;

        /**
         * Sample count.
         */
        private long count;

        /**
         * Resets bucket to a key.
         *
         * @param key bucket key
         */
        private void reset(final long key) {
            this.key = key;
            this.sum = 0;
            this.count = 0;
        }

        /**
         * Clears bucket state.
         */
        private void clear() {
            this.key = Long.MIN_VALUE;
            this.sum = 0;
            this.count = 0;
        }

        /**
         * Returns whether the bucket is empty.
         *
         * @return true when empty
         */
        private boolean empty() {
            return key == Long.MIN_VALUE;
        }

    }

}

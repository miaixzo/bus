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
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Bounded rolling sum and count window.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RollingWindow {

    /**
     * Window duration in nanoseconds.
     */
    private final long windowNanos;

    /**
     * Bucket duration in nanoseconds.
     */
    private final long bucketNanos;

    /**
     * Atomic ring of bucket states sized to the configured window.
     */
    private final AtomicReferenceArray<BucketState> buckets;

    /**
     * Lifecycle lock separating ordinary access from reset.
     */
    private final StampedLock lock;

    /**
     * Creates a rolling window from validated nanosecond durations.
     *
     * @param windowNanos total window duration in nanoseconds
     * @param bucketNanos duration of each ring bucket in nanoseconds
     */
    private RollingWindow(final long windowNanos, final long bucketNanos) {
        this.windowNanos = windowNanos;
        this.bucketNanos = bucketNanos;
        this.buckets = new AtomicReferenceArray<>((int) (windowNanos / bucketNanos));
        this.lock = new StampedLock();
    }

    /**
     * Creates a rolling window whose bucket duration divides the total window exactly.
     *
     * @param window positive total window duration
     * @param bucket positive duration of each bucket
     * @return empty rolling window with the requested durations
     * @throws ValidateException if either duration is null, non-positive, outside nanosecond range, larger than the
     *                           window, or does not divide the window exactly
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
     * Adds a non-negative sample to the bucket containing its timestamp.
     * <p>
     * A sample older than a newer state already occupying the same ring slot is ignored.
     * </p>
     *
     * @param value non-negative amount added to the bucket sum
     * @param time  timestamp used to select the bucket
     * @throws ValidateException if {@code value} is negative or {@code time} is null or outside the supported range
     */
    public void add(final long value, final Instant time) {
        Assert.isTrue(value >= 0, () -> new ValidateException("Window value must be non-negative"));
        final long key = bucketKey(time);
        final int index = index(key);
        final long stamp = lock.readLock();
        try {
            while (true) {
                final BucketState state = buckets.get(index);
                if (state == null) {
                    final BucketState installed = new BucketState(key);
                    if (buckets.compareAndSet(index, null, installed)) {
                        installed.add(value);
                        return;
                    }
                    continue;
                }
                if (state.key == key) {
                    state.add(value);
                    return;
                }
                if (state.key > key) {
                    return;
                }
                final BucketState installed = new BucketState(key);
                if (buckets.compareAndSet(index, state, installed)) {
                    installed.add(value);
                    return;
                }
            }
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Returns the sum of samples in buckets active at the supplied time.
     *
     * @param now timestamp defining the newest active bucket
     * @return sum across the configured rolling window
     * @throws ValidateException if {@code now} is null or outside the supported range
     */
    public long sum(final Instant now) {
        final long current = bucketKey(now);
        final long stamp = lock.readLock();
        try {
            long sum = 0L;
            for (int i = 0; i < buckets.length(); i++) {
                sum += value(i, current, true);
            }
            return sum;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Returns the number of samples in buckets active at the supplied time.
     *
     * @param now timestamp defining the newest active bucket
     * @return sample count across the configured rolling window
     * @throws ValidateException if {@code now} is null or outside the supported range
     */
    public long count(final Instant now) {
        final long current = bucketKey(now);
        final long stamp = lock.readLock();
        try {
            long count = 0L;
            for (int i = 0; i < buckets.length(); i++) {
                count += value(i, current, false);
            }
            return count;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Returns the active sum normalized by the configured window duration in seconds.
     *
     * @param now timestamp defining the newest active bucket
     * @return window sum per configured window-second, or {@code 0.0} when the sum is zero
     * @throws ValidateException if {@code now} is null or outside the supported range
     */
    public double rate(final Instant now) {
        final long current = bucketKey(now);
        final long stamp = lock.readLock();
        try {
            long total = 0L;
            for (int i = 0; i < buckets.length(); i++) {
                total += value(i, current, true);
            }
            return total == 0L ? 0D : total / (windowNanos / Builder.ROLLING_WINDOW_NANOS_PER_SECOND);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Resets all buckets.
     */
    public void reset() {
        final long stamp = lock.writeLock();
        try {
            for (int i = 0; i < buckets.length(); i++) {
                buckets.set(i, null);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Returns whether a bucket is active for a current key.
     *
     * @param bucket  bucket state to test, or {@code null} for an empty ring slot
     * @param current key of the newest bucket included in the window
     * @return {@code true} if the bucket key lies within the current rolling window
     */
    private boolean active(final BucketState bucket, final long current) {
        if (bucket == null) {
            return false;
        }
        final long oldest = current - buckets.length() + 1L;
        if (bucket.key < oldest) {
            return false;
        }
        return bucket.key <= current;
    }

    /**
     * Reads one stable bucket slot, retrying once if its reference changes.
     *
     * @param index   ring slot index to read
     * @param current key of the newest bucket included in the window
     * @param sum     {@code true} to read the sum; {@code false} to read the sample count
     * @return selected aggregate for an active bucket, or zero for an inactive slot
     */
    private long value(final int index, final long current, final boolean sum) {
        BucketState state = buckets.get(index);
        long value = active(state, current) ? state.value(sum) : 0L;
        final BucketState after = buckets.get(index);
        if (after != state) {
            state = after;
            value = active(state, current) ? state.value(sum) : 0L;
        }
        return value;
    }

    /**
     * Returns a bucket array index.
     *
     * @param key time-derived bucket key
     * @return non-negative ring slot index for the key
     */
    private int index(final long key) {
        return Math.floorMod(key, buckets.length());
    }

    /**
     * Returns a bucket key.
     *
     * @param time timestamp to convert to a bucket key
     * @return floor-divided epoch-nanosecond bucket key
     * @throws ValidateException if {@code time} is null or its epoch nanoseconds overflow
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
     * @param duration duration to validate and convert
     * @param name     logical duration name used in validation messages
     * @return positive duration in nanoseconds
     * @throws ValidateException if the duration is null, non-positive, or outside nanosecond range
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
    private static final class BucketState {

        /**
         * Epoch-relative key identifying the time interval represented by this state.
         */
        private final long key;

        /**
         * Concurrent sum of sample values added to this bucket.
         */
        private final LongAdder sum;

        /**
         * Concurrent number of samples added to this bucket.
         */
        private final LongAdder count;

        /**
         * Creates bucket state for a fixed key.
         *
         * @param key epoch-relative bucket key represented by this state
         */
        private BucketState(final long key) {
            this.key = key;
            this.sum = new LongAdder();
            this.count = new LongAdder();
        }

        /**
         * Adds one sample.
         *
         * @param value non-negative sample amount added to the sum
         */
        private void add(final long value) {
            sum.add(value);
            count.increment();
        }

        /**
         * Returns the selected aggregate.
         *
         * @param selectSum {@code true} to read the sum; {@code false} to read the sample count
         * @return current value of the selected concurrent aggregate
         */
        private long value(final boolean selectSum) {
            return selectSum ? sum.sum() : count.sum();
        }

    }

}

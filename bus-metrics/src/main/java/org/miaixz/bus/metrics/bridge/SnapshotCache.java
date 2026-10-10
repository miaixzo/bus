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
package org.miaixz.bus.metrics.bridge;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Lock-free stale-while-refresh cache for immutable host snapshots.
 *
 * @param <T> snapshot type
 * @author Kimi Liu
 */
public final class SnapshotCache<T> {

    /**
     * Lowest supported refresh interval.
     */
    private static final long MIN_TTL_NANOS = Duration.ofMillis(100).toNanos();
    /**
     * Highest supported refresh interval.
     */
    private static final long MAX_TTL_NANOS = Duration.ofSeconds(60).toNanos();

    /**
     * Supplies a fresh immutable snapshot.
     */
    private final Supplier<T> source;
    /**
     * Supplies monotonic nanosecond timestamps.
     */
    private final LongSupplier clock;
    /**
     * Configured refresh interval in nanoseconds.
     */
    private final long ttlNanos;
    /**
     * Most recent successful snapshot.
     */
    private final AtomicReference<T> value = new AtomicReference<>();
    /**
     * Timestamp of the most recent successful refresh.
     */
    private final AtomicLong successNanos = new AtomicLong(Long.MIN_VALUE);
    /**
     * TTL window in which a refresh was most recently attempted.
     */
    private final AtomicLong attemptedWindow = new AtomicLong(Long.MIN_VALUE);
    /**
     * Ensures that only one caller performs a refresh.
     */
    private final AtomicBoolean refreshing = new AtomicBoolean();
    /**
     * Most recent refresh failure, cleared by a successful refresh.
     */
    private final AtomicReference<RuntimeException> lastFailure = new AtomicReference<>();

    /**
     * Creates a cache using {@link System#nanoTime()}.
     *
     * @param ttl    refresh interval
     * @param source snapshot supplier
     */
    public SnapshotCache(Duration ttl, Supplier<T> source) {
        this(ttl, source, System::nanoTime);
    }

    /**
     * Creates a cache with an injectable monotonic clock.
     *
     * @param ttl    refresh interval
     * @param source snapshot supplier
     * @param clock  monotonic nanosecond clock
     * @throws IllegalArgumentException if the TTL is outside the supported range from 100 milliseconds to 60 seconds
     */
    public SnapshotCache(Duration ttl, Supplier<T> source, LongSupplier clock) {
        Objects.requireNonNull(ttl, "Cache TTL must not be null");
        this.source = Objects.requireNonNull(source, "Snapshot source must not be null");
        this.clock = Objects.requireNonNull(clock, "Monotonic clock must not be null");
        this.ttlNanos = ttl.toNanos();
        if (ttlNanos < MIN_TTL_NANOS || ttlNanos > MAX_TTL_NANOS) {
            throw new IllegalArgumentException("Cache TTL must be between 100ms and 60s");
        }
    }

    /**
     * Returns the latest value, refreshing at most once per TTL window.
     *
     * @return latest successfully loaded snapshot, or empty before the first successful load
     */
    public Optional<T> get() {
        long now = clock.getAsLong();
        T current = value.get();
        long success = successNanos.get();
        if (current != null && now - success < ttlNanos) {
            return Optional.of(current);
        }
        long window = Math.floorDiv(now, ttlNanos);
        if (attemptedWindow.get() != window && refreshing.compareAndSet(false, true)) {
            try {
                attemptedWindow.set(window);
                T loaded = Objects.requireNonNull(source.get(), "Snapshot source returned null");
                value.set(loaded);
                successNanos.set(clock.getAsLong());
                lastFailure.set(null);
                current = loaded;
            } catch (RuntimeException exception) {
                lastFailure.set(exception);
                current = value.get();
            } finally {
                refreshing.set(false);
            }
        }
        return Optional.ofNullable(current);
    }

    /**
     * Returns the most recent refresh failure, if any.
     *
     * @return last refresh failure, cleared after a successful refresh
     */
    public Optional<RuntimeException> lastFailure() {
        return Optional.ofNullable(lastFailure.get());
    }

}

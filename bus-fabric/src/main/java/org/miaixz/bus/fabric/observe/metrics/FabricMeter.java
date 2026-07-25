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
package org.miaixz.bus.fabric.observe.metrics;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.ObservationMarker;

/**
 * Thread-safe fabric metric counter and timer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FabricMeter {

    /**
     * Fixed counter descriptors in ordinal order.
     */
    private static final Counter[] FIXED_COUNTERS = Counter.values();

    /**
     * Monotonic runtime clock used by active operation timers.
     */
    private final Clock clock;

    /**
     * Dynamically named counter accumulators.
     */
    private final ConcurrentHashMap<String, LongAdder> counters;

    /**
     * Preallocated fixed counter values indexed by {@link Counter#ordinal()}.
     */
    private final LongAdder[] fixedCounters;

    /**
     * Dynamically named duration aggregates.
     */
    private final ConcurrentHashMap<String, Timing> timings;

    /**
     * Active timers keyed by operation and marker family.
     */
    private final ConcurrentHashMap<TimerKey, Timer> activeTimers;

    /**
     * Creates an empty meter using the system clock.
     */
    public FabricMeter() {
        this(Clock.system());
    }

    /**
     * Creates an empty meter using an explicit runtime clock.
     *
     * @param clock clock used by operation timers
     * @throws IllegalArgumentException if {@code clock} is {@code null}
     */
    public FabricMeter(final Clock clock) {
        this.clock = Assert.notNull(clock, "Clock must not be null");
        this.counters = new ConcurrentHashMap<>();
        this.fixedCounters = new LongAdder[FIXED_COUNTERS.length];
        for (int index = 0; index < fixedCounters.length; index++) {
            fixedCounters[index] = new LongAdder();
        }
        this.timings = new ConcurrentHashMap<>();
        this.activeTimers = new ConcurrentHashMap<>();
    }

    /**
     * Creates a meter using the system clock.
     *
     * @return empty meter using the system clock
     */
    public static FabricMeter create() {
        return new FabricMeter(Clock.system());
    }

    /**
     * Creates a meter using an explicit runtime clock.
     *
     * @param clock clock used by operation timers
     * @return empty meter using the supplied clock
     * @throws IllegalArgumentException if {@code clock} is {@code null}
     */
    public static FabricMeter create(final Clock clock) {
        return new FabricMeter(clock);
    }

    /**
     * Increments a counter by one.
     *
     * @param name non-blank, single-line dynamic counter name
     * @throws ValidateException if {@code name} is blank or multi-line
     */
    public void increment(final String name) {
        add(name, 1);
    }

    /**
     * Adds a delta to a counter.
     *
     * @param name  non-blank, single-line dynamic counter name
     * @param delta signed amount added to the counter
     * @throws ValidateException if {@code name} is blank or multi-line
     */
    public void add(final String name, final long delta) {
        counters.computeIfAbsent(validateName(name), key -> new LongAdder()).add(delta);
    }

    /**
     * Increments a preallocated fixed counter by one without a map lookup or update-path allocation.
     *
     * @param counter preallocated counter descriptor to increment
     * @throws ValidateException if {@code counter} is {@code null}
     */
    public void incrementCounter(final Counter counter) {
        addCounter(counter, 1L);
    }

    /**
     * Adds a delta to a preallocated fixed counter without a map lookup or update-path allocation.
     *
     * @param counter preallocated counter descriptor to update
     * @param delta   signed amount added to the fixed counter
     * @throws ValidateException if {@code counter} is {@code null}
     */
    public void addCounter(final Counter counter, final long delta) {
        if (counter == null) {
            throw new ValidateException("Counter must not be null");
        }
        fixedCounters[counter.ordinal()].add(delta);
    }

    /**
     * Returns a counter value.
     *
     * @param name non-blank, single-line metric name
     * @return dynamic counter sum when that name exists; otherwise timing sample count or zero
     * @throws ValidateException if {@code name} is blank or multi-line
     */
    public long count(final String name) {
        final String checked = validateName(name);
        final LongAdder counter = counters.get(checked);
        if (counter != null) {
            return counter.sum();
        }
        final Timing timing = timings.get(checked);
        return timing == null ? 0L : timing.count();
    }

    /**
     * Returns a fixed counter value.
     *
     * @param counter preallocated counter descriptor to read
     * @return current sum of the fixed counter
     * @throws IllegalArgumentException if {@code counter} is {@code null}
     */
    public long counterValue(final Counter counter) {
        return fixedCounters[Assert.notNull(counter, "Counter must not be null").ordinal()].sum();
    }

    /**
     * Records a duration.
     *
     * @param name     non-blank, single-line timing name
     * @param duration non-negative duration recorded as nanoseconds
     * @throws ValidateException   if {@code name} is invalid or {@code duration} is null or negative
     * @throws ArithmeticException if the duration cannot be represented in nanoseconds
     */
    public void timing(final String name, final Duration duration) {
        final Duration checked = Assert
                .notNull(duration, () -> new ValidateException("Duration must be non-null and non-negative"));
        Assert.isFalse(checked.isNegative(), () -> new ValidateException("Duration must be non-null and non-negative"));
        timings.computeIfAbsent(validateName(name), key -> new Timing()).record(checked.toNanos());
    }

    /**
     * Applies an observation timing role for one operation and marker family.
     * <p>
     * A duplicate start, unmatched terminal event, or negative elapsed time increments the invalid-event counter. A
     * valid terminal event removes its active timer and records duration under the family duration metric.
     * </p>
     *
     * @param role        {@code NONE}, {@code START}, or terminal timing role
     * @param operationId non-blank operation identifier required for non-{@code NONE} roles
     * @param family      non-blank metric family required for non-{@code NONE} roles
     * @throws IllegalArgumentException if {@code role} is {@code null}
     * @throws ValidateException        if a non-{@code NONE} role receives an invalid operation identifier or family
     */
    public void observe(final ObservationMarker.Timing role, final String operationId, final String family) {
        final ObservationMarker.Timing checkedRole = Assert.notNull(role, "Timing role must not be null");
        if (ObservationMarker.Timing.NONE == checkedRole) {
            return;
        }
        final TimerKey key = new TimerKey(validateName(operationId), validateName(family));
        if (ObservationMarker.Timing.START == checkedRole) {
            if (activeTimers.putIfAbsent(key, new Timer(clock.nanos())) != null) {
                increment(Builder.FABRIC_METER_INVALID_EVENT);
            }
            return;
        }
        final Timer timer = activeTimers.remove(key);
        if (timer == null) {
            increment(Builder.FABRIC_METER_INVALID_EVENT);
            return;
        }
        final long elapsed = clock.nanos() - timer.startNanos();
        if (elapsed < 0L) {
            increment(Builder.FABRIC_METER_INVALID_EVENT);
            return;
        }
        timing(family + Builder.METER_EVENT_OBSERVER_DURATION, Duration.ofNanos(elapsed));
    }

    /**
     * Returns a weakly consistent immutable snapshot of dynamic counters, non-zero fixed counters, and timing count,
     * total, and maximum values.
     *
     * @return immutable metric-name to value snapshot assembled from the current concurrent accumulators
     */
    public Map<String, Long> snapshot() {
        final Map<String, Long> snapshot = MapKit
                .newHashMap(counters.size() + FIXED_COUNTERS.length + timings.size() * 3, true);
        counters.forEach((name, value) -> snapshot.put(name, value.sum()));
        for (final Counter counter : FIXED_COUNTERS) {
            final long value = fixedCounters[counter.ordinal()].sum();
            if (value != 0L) {
                snapshot.put(counter.metricName(), value);
            }
        }
        timings.forEach((name, timing) -> {
            snapshot.put(name + ".count", timing.count());
            snapshot.put(name + ".totalNanos", timing.totalNanos());
            snapshot.put(name + ".maxNanos", timing.maxNanos());
        });
        return Map.copyOf(snapshot);
    }

    /**
     * Clears dynamic counters, timing aggregates, and active timers, and resets every fixed counter.
     * <p>
     * Concurrent updates may race with this operation; the reset is not an atomic global snapshot transition.
     * </p>
     */
    public void reset() {
        counters.clear();
        for (final LongAdder counter : fixedCounters) {
            counter.reset();
        }
        timings.clear();
        activeTimers.clear();
    }

    /**
     * Returns the number of active timers for package-level verification.
     *
     * @return current number of operation-family timer keys awaiting a terminal timing event
     */
    int activeTimers() {
        return activeTimers.size();
    }

    /**
     * Validates a metric name.
     *
     * @param name metric name to validate without trimming or other normalization
     * @return unchanged non-blank, single-line metric name
     * @throws ValidateException if {@code name} is blank or contains a carriage return or line feed
     */
    private static String validateName(final String name) {
        final String checked = Assert
                .notBlank(name, () -> new ValidateException("Metric name must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Metric name must be non-blank and single-line"));
        return checked;
    }

    /**
     * Fixed low-cardinality counters used by runtime resource owners.
     *
     * <p>
     * The declaration order is the stable in-process index used by the meter. Callers must use the enum value rather
     * than persist its ordinal.
     * </p>
     */
    public enum Counter {

        /**
         * Physical connections created during this meter lifetime.
         */
        PHYSICAL_CONNECTIONS_CREATED("physicalConnectionsCreated"),

        /**
         * Physical connections that are currently active.
         */
        ACTIVE_PHYSICAL_CONNECTIONS("activePhysicalConnections"),

        /**
         * Logical connection leases acquired.
         */
        LOGICAL_LEASES_ACQUIRED("logicalLeasesAcquired"),

        /**
         * Logical connection leases released.
         */
        LOGICAL_LEASES_RELEASED("logicalLeasesReleased"),

        /**
         * Logical connection leases that are currently active.
         */
        ACTIVE_LOGICAL_LEASES("activeLogicalLeases"),

        /**
         * TLS handshakes classified as full handshakes.
         */
        TLS_FULL_HANDSHAKES("tlsFullHandshakes"),

        /**
         * TLS handshakes classified as resumed sessions.
         */
        TLS_RESUMED_HANDSHAKES("tlsResumedHandshakes"),

        /**
         * TLS handshakes whose session reuse cannot be classified safely.
         */
        TLS_UNKNOWN_HANDSHAKES("tlsUnknownHandshakes"),

        /**
         * Failed TLS handshakes.
         */
        TLS_HANDSHAKE_FAILURES("tlsHandshakeFailures"),

        /**
         * HTTP/2 streams created.
         */
        HTTP2_STREAMS_CREATED("http2StreamsCreated"),

        /**
         * HTTP/2 streams that are currently active.
         */
        ACTIVE_HTTP2_STREAMS("activeHttp2Streams"),

        /**
         * Pool waiters enqueued.
         */
        WAITERS_ENQUEUED("waitersEnqueued"),

        /**
         * Pool waiters that are currently active.
         */
        ACTIVE_WAITERS("activeWaiters"),

        /**
         * Transport bytes read from the network.
         */
        BYTES_READ("bytesRead"),

        /**
         * Transport bytes written to the network.
         */
        BYTES_WRITTEN("bytesWritten");

        /**
         * Snapshot metric name.
         */
        private final String metricName;

        /**
         * Creates a fixed counter descriptor.
         *
         * @param metricName snapshot metric name
         */
        Counter(final String metricName) {
            this.metricName = metricName;
        }

        /**
         * Returns the stable snapshot metric name.
         *
         * @return snapshot metric name
         */
        public String metricName() {
            return metricName;
        }

    }

    /**
     * Timing aggregate values.
     */
    private static final class Timing {

        /**
         * Number of durations recorded for this timing name.
         */
        private final LongAdder count = new LongAdder();

        /**
         * Sum of all recorded durations in nanoseconds.
         */
        private final LongAdder totalNanos = new LongAdder();

        /**
         * Largest recorded duration in nanoseconds.
         */
        private final AtomicLong maxNanos = new AtomicLong();

        /**
         * Records nanoseconds.
         *
         * @param nanos non-negative duration in nanoseconds
         */
        private void record(final long nanos) {
            count.increment();
            totalNanos.add(nanos);
            long current = maxNanos.get();
            while (nanos > current && !maxNanos.compareAndSet(current, nanos)) {
                current = maxNanos.get();
            }

        }

        /**
         * Returns timing count.
         *
         * @return timing count
         */
        private long count() {
            return count.sum();
        }

        /**
         * Returns total nanoseconds.
         *
         * @return total nanoseconds
         */
        private long totalNanos() {
            return totalNanos.sum();
        }

        /**
         * Returns maximum nanoseconds.
         *
         * @return maximum nanoseconds
         */
        private long maxNanos() {
            return maxNanos.get();
        }

    }

    /**
     * Immutable active timer key.
     *
     * @param operationId operation identifier
     * @param family      marker family
     */
    private record TimerKey(String operationId, String family) {
    }

    /**
     * Immutable active timer value.
     *
     * @param startNanos start time in nanoseconds
     */
    private record Timer(long startNanos) {
    }

}

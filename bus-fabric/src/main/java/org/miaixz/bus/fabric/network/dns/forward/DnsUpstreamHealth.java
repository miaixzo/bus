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
package org.miaixz.bus.fabric.network.dns.forward;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Shared upstream health index for DNS forwarding.
 *
 * <p>
 * The index tracks EWMA round-trip time, consecutive failures, circuit-open windows, and half-open probes. Selection
 * order is fixed: healthy upstreams by lowest EWMA first, half-open probe candidates next, and open-circuit upstreams
 * excluded from ordinary selection.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsUpstreamHealth {

    /**
     * Consecutive failures required to open a circuit.
     */
    public static final int CIRCUIT_FAILURE_THRESHOLD = 3;

    /**
     * Default circuit-open window.
     */
    public static final Duration CIRCUIT_WINDOW = Duration.ofSeconds(10);

    /**
     * EWMA old-sample weight used for upstream round-trip time smoothing.
     */
    private static final int RTT_EWMA_OLD_WEIGHT = 7;

    /**
     * EWMA sample divisor used for upstream round-trip time smoothing.
     */
    private static final int RTT_EWMA_DIVISOR = 8;

    /**
     * Shared health state keyed by transport, host, and port.
     */
    private static final ConcurrentHashMap<String, HealthState> STATES = new ConcurrentHashMap<>();

    /**
     * Circuit-open window used by this health view.
     */
    private final Duration circuitWindow;

    /**
     * Creates a health index using the default circuit-open window.
     */
    public DnsUpstreamHealth() {
        this(CIRCUIT_WINDOW);
    }

    /**
     * Creates a health index with an explicit circuit-open window.
     *
     * @param circuitWindow circuit-open window
     */
    public DnsUpstreamHealth(final Duration circuitWindow) {
        this.circuitWindow = validateDuration(circuitWindow, "DNS upstream circuit window");
    }

    /**
     * Selects upstreams for an ordinary forwarding request.
     *
     * @param upstreams configured upstreams
     * @return ordered selectable upstreams
     */
    public List<DnsUpstream> select(final List<DnsUpstream> upstreams) {
        validateUpstreams(upstreams);
        final long now = System.nanoTime();
        final ArrayList<DnsUpstream> healthy = new ArrayList<>(upstreams.size());
        final ArrayList<DnsUpstream> halfOpen = new ArrayList<>();
        for (final DnsUpstream upstream : upstreams) {
            final HealthState state = STATES.get(healthKey(upstream));
            if (state == null || state.healthy(now)) {
                healthy.add(upstream);
            } else if (state.halfOpen(now)) {
                halfOpen.add(upstream);
            }
        }
        healthy.sort(Comparator.comparingInt(this::failureCount).thenComparingLong(this::rttNanos));
        halfOpen.sort(Comparator.comparingLong(this::rttNanos));
        healthy.addAll(halfOpen);
        return List.copyOf(healthy);
    }

    /**
     * Marks an upstream healthy after a successful response.
     *
     * @param upstream upstream definition
     * @param rttNanos measured round-trip time in nanoseconds
     */
    public void markSuccess(final DnsUpstream upstream, final long rttNanos) {
        validateUpstream(upstream);
        STATES.compute(healthKey(upstream), (key, state) -> new HealthState(0, 0L, ewmaRttNanos(state, rttNanos)));
    }

    /**
     * Marks an upstream failed and opens the circuit after the fixed threshold.
     *
     * @param upstream upstream definition
     */
    public void markFailure(final DnsUpstream upstream) {
        validateUpstream(upstream);
        STATES.compute(healthKey(upstream), (key, state) -> {
            final int failures = state == null ? 1 : state.consecutiveFailures + 1;
            final long circuitUntil = failures >= CIRCUIT_FAILURE_THRESHOLD
                    ? System.nanoTime() + circuitWindow.toNanos()
                    : 0L;
            final long rtt = state == null ? Long.MAX_VALUE : state.rttNanos;
            return new HealthState(failures, circuitUntil, rtt);
        });
    }

    /**
     * Returns the current health snapshot for one upstream.
     *
     * @param upstream upstream definition
     * @return immutable health snapshot
     */
    public Snapshot snapshot(final DnsUpstream upstream) {
        validateUpstream(upstream);
        final long now = System.nanoTime();
        final HealthState state = STATES.get(healthKey(upstream));
        if (state == null) {
            return new Snapshot(true, false, false, 0, Long.MAX_VALUE);
        }
        return new Snapshot(state.healthy(now), state.circuitOpen(now), state.halfOpen(now), state.consecutiveFailures,
                state.rttNanos);
    }

    /**
     * Returns the observed RTT EWMA for one upstream.
     *
     * @param upstream upstream definition
     * @return RTT EWMA in nanoseconds
     */
    public long rttNanos(final DnsUpstream upstream) {
        validateUpstream(upstream);
        final HealthState state = STATES.get(healthKey(upstream));
        return state == null ? Long.MAX_VALUE : state.rttNanos;
    }

    /**
     * Returns the current consecutive failure count for one upstream.
     *
     * @param upstream upstream definition
     * @return consecutive failure count
     */
    public int failureCount(final DnsUpstream upstream) {
        validateUpstream(upstream);
        final HealthState state = STATES.get(healthKey(upstream));
        return state == null ? 0 : state.consecutiveFailures;
    }

    /**
     * Builds the health key for an upstream.
     *
     * @param upstream upstream definition
     * @return stable health key
     */
    public String healthKey(final DnsUpstream upstream) {
        validateUpstream(upstream);
        return upstream.healthKey();
    }

    /**
     * Updates an upstream RTT sample using a fixed-point EWMA.
     *
     * @param state    previous health state, or {@code null}
     * @param rttNanos latest measured round-trip time in nanoseconds
     * @return updated EWMA round-trip time in nanoseconds
     */
    private static long ewmaRttNanos(final HealthState state, final long rttNanos) {
        final long safeRtt = Math.max(0L, rttNanos);
        if (state == null || state.rttNanos == Long.MAX_VALUE) {
            return safeRtt;
        }
        return ((state.rttNanos * RTT_EWMA_OLD_WEIGHT) + safeRtt) / RTT_EWMA_DIVISOR;
    }

    /**
     * Validates upstream list.
     *
     * @param upstreams upstream list
     */
    private static void validateUpstreams(final List<DnsUpstream> upstreams) {
        if (upstreams == null || upstreams.isEmpty()) {
            throw new ValidateException("DNS upstream health selection must not be empty");
        }
        for (final DnsUpstream upstream : upstreams) {
            validateUpstream(upstream);
        }
    }

    /**
     * Validates one upstream.
     *
     * @param upstream upstream definition
     */
    private static void validateUpstream(final DnsUpstream upstream) {
        if (upstream == null) {
            throw new ValidateException("DNS upstream health upstream must not be null");
        }
    }

    /**
     * Validates a positive duration.
     *
     * @param duration duration to validate
     * @param name     diagnostic name
     * @return validated duration
     */
    private static Duration validateDuration(final Duration duration, final String name) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new ValidateException(name + " must be positive");
        }
        return duration;
    }

    /**
     * Immutable upstream health snapshot.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Snapshot {

        /**
         * Whether the upstream is in the healthy group.
         */
        private final boolean healthy;

        /**
         * Whether the upstream circuit is open.
         */
        private final boolean circuitOpen;

        /**
         * Whether the upstream is ready for a half-open probe.
         */
        private final boolean halfOpen;

        /**
         * Consecutive failure count.
         */
        private final int consecutiveFailures;

        /**
         * EWMA round-trip time in nanoseconds.
         */
        private final long rttNanos;

        /**
         * Creates a health snapshot.
         *
         * @param healthy             whether upstream is healthy
         * @param circuitOpen         whether circuit is open
         * @param halfOpen            whether half-open probe is allowed
         * @param consecutiveFailures consecutive failure count
         * @param rttNanos            EWMA round-trip time
         */
        private Snapshot(final boolean healthy, final boolean circuitOpen, final boolean halfOpen,
                final int consecutiveFailures, final long rttNanos) {
            this.healthy = healthy;
            this.circuitOpen = circuitOpen;
            this.halfOpen = halfOpen;
            this.consecutiveFailures = consecutiveFailures;
            this.rttNanos = rttNanos;
        }

        /**
         * Returns whether the upstream is healthy.
         *
         * @return true when healthy
         */
        public boolean healthy() {
            return healthy;
        }

        /**
         * Returns whether the circuit is open.
         *
         * @return true when circuit is open
         */
        public boolean circuitOpen() {
            return circuitOpen;
        }

        /**
         * Returns whether a half-open probe is allowed.
         *
         * @return true when half-open probe is allowed
         */
        public boolean halfOpen() {
            return halfOpen;
        }

        /**
         * Returns consecutive failure count.
         *
         * @return consecutive failures
         */
        public int consecutiveFailures() {
            return consecutiveFailures;
        }

        /**
         * Returns RTT EWMA in nanoseconds.
         *
         * @return RTT EWMA
         */
        public long rttNanos() {
            return rttNanos;
        }

    }

    /**
     * Internal mutable health state snapshot replaced atomically in the map.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class HealthState {

        /**
         * Consecutive failure count.
         */
        private final int consecutiveFailures;

        /**
         * Monotonic circuit-open deadline.
         */
        private final long circuitUntilNanos;

        /**
         * EWMA round-trip time in nanoseconds.
         */
        private final long rttNanos;

        /**
         * Creates internal health state.
         *
         * @param consecutiveFailures consecutive failure count
         * @param circuitUntilNanos   monotonic circuit-open deadline
         * @param rttNanos            EWMA round-trip time
         */
        private HealthState(final int consecutiveFailures, final long circuitUntilNanos, final long rttNanos) {
            this.consecutiveFailures = consecutiveFailures;
            this.circuitUntilNanos = circuitUntilNanos;
            this.rttNanos = rttNanos;
        }

        /**
         * Returns whether this state is healthy.
         *
         * @param now current monotonic time
         * @return true when healthy
         */
        private boolean healthy(final long now) {
            return consecutiveFailures < CIRCUIT_FAILURE_THRESHOLD;
        }

        /**
         * Returns whether the circuit is open.
         *
         * @param now current monotonic time
         * @return true when open
         */
        private boolean circuitOpen(final long now) {
            return consecutiveFailures >= CIRCUIT_FAILURE_THRESHOLD && circuitUntilNanos > now;
        }

        /**
         * Returns whether a half-open probe is allowed.
         *
         * @param now current monotonic time
         * @return true when half-open
         */
        private boolean halfOpen(final long now) {
            return consecutiveFailures >= CIRCUIT_FAILURE_THRESHOLD && circuitUntilNanos <= now;
        }

    }

}

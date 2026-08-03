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
package org.miaixz.bus.fabric.network.dns.recursive;

import java.time.Duration;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable retry and timeout budget shared by recursive and forwarding upstream requests.
 *
 * <p>
 * The budget reserves an attempt before an upstream request is sent. Each reservation returns a new immutable budget
 * with one fewer retry slot, and the timeout attached to that reservation is capped by total remaining time, the fixed
 * per-query timeout, and the caller-supplied upstream timeout.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsRetryBudget {

    /**
     * Fixed recursive total timeout.
     */
    public static final Duration RECURSIVE_TOTAL_TIMEOUT = Duration.ofSeconds(15);

    /**
     * Fixed forwarding total timeout.
     */
    public static final Duration FORWARD_TOTAL_TIMEOUT = Duration.ofSeconds(6);

    /**
     * Fixed single upstream request timeout.
     */
    public static final Duration PER_QUERY_TIMEOUT = Duration.ofSeconds(3);

    /**
     * Fixed maximum upstream request count.
     */
    public static final int MAX_RETRIES = 3;

    /**
     * Total timeout captured for inspection.
     */
    private final Duration totalTimeout;

    /**
     * Per-query timeout captured for inspection.
     */
    private final Duration perQueryTimeout;

    /**
     * Maximum upstream request count.
     */
    private final int maxRetries;

    /**
     * Monotonic deadline in nanoseconds.
     */
    private final long deadlineNanos;

    /**
     * Already reserved upstream request count.
     */
    private final int attempts;

    /**
     * Creates a retry budget.
     *
     * @param totalTimeout    total timeout
     * @param perQueryTimeout per-query timeout
     * @param maxRetries      maximum upstream request count
     * @param deadlineNanos   monotonic deadline
     * @param attempts        already reserved attempts
     */
    private DnsRetryBudget(final Duration totalTimeout, final Duration perQueryTimeout, final int maxRetries,
            final long deadlineNanos, final int attempts) {
        this.totalTimeout = validateDuration(totalTimeout, "DNS retry total timeout");
        this.perQueryTimeout = validateDuration(perQueryTimeout, "DNS retry per-query timeout");
        this.maxRetries = validateRetries(maxRetries);
        this.deadlineNanos = deadlineNanos;
        this.attempts = validateAttempts(attempts, maxRetries);
    }

    /**
     * Creates a default recursive retry budget.
     *
     * @return recursive retry budget
     */
    public static DnsRetryBudget recursive() {
        return start(RECURSIVE_TOTAL_TIMEOUT, PER_QUERY_TIMEOUT, MAX_RETRIES);
    }

    /**
     * Creates a default forwarding retry budget.
     *
     * @return forwarding retry budget
     */
    public static DnsRetryBudget forwarding() {
        return start(FORWARD_TOTAL_TIMEOUT, PER_QUERY_TIMEOUT, MAX_RETRIES);
    }

    /**
     * Creates a budget that allows one upstream request.
     *
     * @param timeout request timeout
     * @return single-attempt retry budget
     */
    public static DnsRetryBudget singleAttempt(final Duration timeout) {
        return start(timeout, timeout, 1);
    }

    /**
     * Starts a retry budget at the current monotonic time.
     *
     * @param totalTimeout    total timeout
     * @param perQueryTimeout per-query timeout
     * @param maxRetries      maximum upstream request count
     * @return retry budget
     */
    public static DnsRetryBudget start(
            final Duration totalTimeout,
            final Duration perQueryTimeout,
            final int maxRetries) {
        final Duration total = validateDuration(totalTimeout, "DNS retry total timeout");
        final Duration perQuery = validateDuration(perQueryTimeout, "DNS retry per-query timeout");
        final int retries = validateRetries(maxRetries);
        return new DnsRetryBudget(total, perQuery, retries, System.nanoTime() + total.toNanos(), 0);
    }

    /**
     * Reserves one upstream request attempt.
     *
     * @param upstreamTimeout upstream-specific timeout
     * @return reserved attempt and remaining immutable budget
     */
    public Attempt reserve(final Duration upstreamTimeout) {
        if (exhausted()) {
            throw new ValidateException("DNS retry budget is exhausted");
        }
        final Duration timeout = effectiveTimeout(upstreamTimeout);
        return new Attempt(timeout,
                new DnsRetryBudget(totalTimeout, perQueryTimeout, maxRetries, deadlineNanos, attempts + 1));
    }

    /**
     * Returns whether no further upstream request may be sent.
     *
     * @return true when retry count or total timeout is exhausted
     */
    public boolean exhausted() {
        return attempts >= maxRetries || remaining().isZero();
    }

    /**
     * Returns total timeout.
     *
     * @return total timeout
     */
    public Duration totalTimeout() {
        return totalTimeout;
    }

    /**
     * Returns per-query timeout.
     *
     * @return per-query timeout
     */
    public Duration perQueryTimeout() {
        return perQueryTimeout;
    }

    /**
     * Returns maximum upstream request count.
     *
     * @return maximum retries
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Returns already reserved attempt count.
     *
     * @return reserved attempts
     */
    public int attempts() {
        return attempts;
    }

    /**
     * Returns remaining retry slots.
     *
     * @return remaining retry count
     */
    public int remainingRetries() {
        return Math.max(0, maxRetries - attempts);
    }

    /**
     * Returns remaining total time.
     *
     * @return non-negative remaining time
     */
    public Duration remaining() {
        final long nanos = deadlineNanos - System.nanoTime();
        return nanos <= 0L ? Duration.ZERO : Duration.ofNanos(nanos);
    }

    /**
     * Computes the timeout for one reserved upstream request.
     *
     * @param upstreamTimeout upstream-specific timeout
     * @return effective timeout
     */
    public Duration effectiveTimeout(final Duration upstreamTimeout) {
        final Duration upstream = validateDuration(upstreamTimeout, "DNS upstream timeout");
        Duration timeout = min(perQueryTimeout, upstream);
        timeout = min(timeout, remaining());
        return timeout.isZero() ? Duration.ofNanos(1L) : timeout;
    }

    /**
     * Returns the smaller duration.
     *
     * @param first  first duration
     * @param second second duration
     * @return smaller duration
     */
    private static Duration min(final Duration first, final Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
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
     * Validates a retry count.
     *
     * @param maxRetries retry count
     * @return validated retry count
     */
    private static int validateRetries(final int maxRetries) {
        if (maxRetries < 1) {
            throw new ValidateException("DNS retry max retries must be positive");
        }
        return maxRetries;
    }

    /**
     * Validates an attempt count.
     *
     * @param attempts   attempt count
     * @param maxRetries maximum retry count
     * @return validated attempt count
     */
    private static int validateAttempts(final int attempts, final int maxRetries) {
        if (attempts < 0 || attempts > maxRetries) {
            throw new ValidateException("DNS retry attempts are out of range");
        }
        return attempts;
    }

    /**
     * Immutable reserved upstream request attempt.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Attempt {

        /**
         * Effective timeout for the reserved request.
         */
        private final Duration timeout;

        /**
         * Remaining budget after this reservation.
         */
        private final DnsRetryBudget budget;

        /**
         * Creates a reserved attempt.
         *
         * @param timeout effective request timeout
         * @param budget  remaining budget
         */
        private Attempt(final Duration timeout, final DnsRetryBudget budget) {
            this.timeout = validateDuration(timeout, "DNS retry attempt timeout");
            if (budget == null) {
                throw new ValidateException("DNS retry remaining budget must not be null");
            }
            this.budget = budget;
        }

        /**
         * Returns effective timeout for the reserved request.
         *
         * @return request timeout
         */
        public Duration timeout() {
            return timeout;
        }

        /**
         * Returns remaining budget after this reservation.
         *
         * @return remaining budget
         */
        public DnsRetryBudget budget() {
            return budget;
        }

    }

}

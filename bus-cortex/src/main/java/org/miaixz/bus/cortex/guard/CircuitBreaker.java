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
package org.miaixz.bus.cortex.guard;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple circuit breaker with CLOSED/OPEN/HALF_OPEN states.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CircuitBreaker {

    /**
     * Circuit breaker state machine states.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum State {

        /**
         * Requests flow normally; failures are counted.
         */
        CLOSED,
        /**
         * All requests are blocked until the reset timeout elapses.
         */
        OPEN,
        /**
         * A limited probe request is allowed to test whether the dependency has recovered.
         */
        HALF_OPEN
    }

    /**
     * Logical name of this circuit breaker.
     */
    private final String name;
    /**
     * Number of consecutive failures required before the circuit opens.
     */
    private final int failureThreshold;
    /**
     * Cool-down time in milliseconds before allowing a half-open probe.
     */
    private final long resetTimeoutMs;
    /**
     * Current state of the circuit breaker.
     */
    private volatile State state = State.CLOSED;
    /**
     * Counter tracking consecutive failed requests.
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);
    /**
     * Timestamp when the circuit most recently transitioned to the open state.
     */
    private volatile long openTime = 0L;

    /**
     * Creates a CircuitBreaker with default thresholds (5 failures, 60 s reset).
     *
     * @param name logical name for this breaker
     */
    public CircuitBreaker(String name) {
        this(name, 5, 60000L);
    }

    /**
     * Creates a CircuitBreaker with explicit thresholds.
     *
     * @param name             logical name for this breaker
     * @param failureThreshold number of consecutive failures before opening
     * @param resetTimeoutMs   milliseconds to wait before attempting a probe
     */
    public CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
    }

    /**
     * Returns whether the next request is allowed to pass through the breaker.
     *
     * @return {@code true} if the caller may proceed with the request
     */
    public boolean allowRequest() {
        if (state == State.CLOSED) {
            return true;
        }
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - openTime >= resetTimeoutMs) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        // HALF_OPEN: allow exactly one probe
        return true;
    }

    /**
     * Records a successful request and closes the breaker.
     */
    public void recordSuccess() {
        failureCount.set(0);
        state = State.CLOSED;
    }

    /**
     * Records a failed request and opens the breaker when the threshold is reached.
     */
    public void recordFailure() {
        int count = failureCount.incrementAndGet();
        if (state == State.HALF_OPEN || count >= failureThreshold) {
            state = State.OPEN;
            openTime = System.currentTimeMillis();
        }
    }

    /**
     * Returns the current state of this circuit breaker.
     *
     * @return current state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the logical name of this circuit breaker.
     *
     * @return circuit breaker name
     */
    public String getName() {
        return name;
    }

}

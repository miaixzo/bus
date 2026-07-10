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
package org.miaixz.bus.fabric.protocol.sse.event;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Mutable SSE retry delay policy updated by server retry directives.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseRetry {

    /**
     * Default base delay.
     */
    private static final Duration DEFAULT_CURRENT = Duration.ofSeconds(3);

    /**
     * Default maximum delay.
     */
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(30);

    /**
     * Current base retry delay.
     */
    private final AtomicReference<Duration> current;

    /**
     * Maximum retry delay.
     */
    private final Duration maxDelay;

    /**
     * Creates a retry policy.
     *
     * @param current  base retry delay
     * @param maxDelay maximum retry delay
     */
    private SseRetry(final Duration current, final Duration maxDelay) {
        this.current = new AtomicReference<>(validate(current, "SSE retry"));
        this.maxDelay = validate(maxDelay, "SSE maximum retry");
        if (this.maxDelay.compareTo(this.current.get()) < 0) {
            throw new ValidateException("SSE maximum retry must not be less than current retry");
        }
    }

    /**
     * Returns the default retry policy.
     *
     * @return default retry policy
     */
    public static SseRetry defaults() {
        return new SseRetry(DEFAULT_CURRENT, DEFAULT_MAX_DELAY);
    }

    /**
     * Returns current base retry delay.
     *
     * @return current retry delay
     */
    public Duration current() {
        return current.get();
    }

    /**
     * Updates current base retry delay.
     *
     * @param retry retry delay
     */
    public void update(final Duration retry) {
        current.set(validate(retry, "SSE retry"));
    }

    /**
     * Returns the delay for a retry attempt using capped exponential backoff.
     *
     * @param attempt attempt index
     * @return next delay
     */
    public Duration nextDelay(final int attempt) {
        if (attempt < 0) {
            throw new ValidateException("SSE retry attempt must be non-negative");
        }
        Duration delay = current();
        for (int i = 0; i < attempt && delay.compareTo(maxDelay) < 0; i++) {
            try {
                delay = delay.multipliedBy(2L);
            } catch (final ArithmeticException e) {
                return maxDelay;
            }
        }
        return delay.compareTo(maxDelay) > 0 ? maxDelay : delay;
    }

    /**
     * Validates a delay.
     *
     * @param value delay
     * @param name  field name
     * @return delay
     */
    private static Duration validate(final Duration value, final String name) {
        if (value == null || value.isNegative()) {
            throw new ValidateException(name + " must be non-null and non-negative");
        }
        return value;
    }

}

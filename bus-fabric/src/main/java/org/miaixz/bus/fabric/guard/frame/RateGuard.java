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
package org.miaixz.bus.fabric.guard.frame;

import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.guard.GuardRule;

/**
 * Token bucket guard for frame throughput.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RateGuard implements GuardRule {

    /**
     * Nanoseconds per second.
     */
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    /**
     * Rule name.
     */
    private static final String NAME = "frame-rate";

    /**
     * Maximum bytes refilled per second.
     */
    private final long bytesPerSecond;

    /**
     * Available token count.
     */
    private final AtomicLong available;

    /**
     * Last refill monotonic timestamp.
     */
    private final AtomicLong lastRefillNanos;

    /**
     * Creates a rate guard.
     *
     * @param bytesPerSecond bytes per second
     */
    private RateGuard(final long bytesPerSecond) {
        this.bytesPerSecond = validateBytesPerSecond(bytesPerSecond);
        this.available = new AtomicLong(bytesPerSecond);
        this.lastRefillNanos = new AtomicLong(Clock.system().nanos());
    }

    /**
     * Creates a rate guard.
     *
     * @param bytesPerSecond bytes per second
     * @return rate guard
     */
    public static RateGuard of(final long bytesPerSecond) {
        return new RateGuard(bytesPerSecond);
    }

    /**
     * Acquires frame byte tokens.
     *
     * @param bytes requested bytes
     * @return guard result
     */
    public GuardResult acquire(final long bytes) {
        if (bytes < 0) {
            throw new ValidateException("Requested bytes must be non-negative");
        }
        refill(Clock.system());
        while (true) {
            final long current = available.get();
            if (current < bytes) {
                return GuardResult.reject("frame rate request " + bytes + " exceeds available " + current);
            }
            if (available.compareAndSet(current, current - bytes)) {
                return GuardResult.pass();
            }
        }
    }

    /**
     * Checks a message payload length as a frame-rate request.
     *
     * @param message message
     * @return guard result
     */
    @Override
    public GuardResult check(final Message message) {
        if (message == null) {
            throw new ValidateException("Message must not be null");
        }
        final long length = message.payload().length();
        return acquire(Math.max(0L, length));
    }

    /**
     * Refills tokens from a runtime clock.
     *
     * @param clock runtime clock
     */
    public void refill(final Clock clock) {
        if (clock == null) {
            throw new ValidateException("Runtime clock must not be null");
        }
        final long now = clock.nanos();
        while (true) {
            final long previous = lastRefillNanos.get();
            final long elapsed = now - previous;
            if (elapsed < NANOS_PER_SECOND) {
                return;
            }
            final long seconds = elapsed / NANOS_PER_SECOND;
            final long next = previous + seconds * NANOS_PER_SECOND;
            if (lastRefillNanos.compareAndSet(previous, next)) {
                addTokens(safeMultiply(seconds, bytesPerSecond));
                return;
            }
        }
    }

    /**
     * Returns available tokens.
     *
     * @return available tokens
     */
    public long available() {
        return available.get();
    }

    /**
     * Returns rule name.
     *
     * @return rule name
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Adds tokens without exceeding capacity.
     *
     * @param tokens tokens to add
     */
    private void addTokens(final long tokens) {
        if (tokens <= 0) {
            return;
        }
        while (true) {
            final long current = available.get();
            final long next = Math.min(bytesPerSecond, current + Math.min(tokens, bytesPerSecond));
            if (available.compareAndSet(current, next)) {
                return;
            }
        }
    }

    /**
     * Multiplies with saturation.
     *
     * @param seconds seconds
     * @param bytes   bytes per second
     * @return token count
     */
    private static long safeMultiply(final long seconds, final long bytes) {
        if (seconds > Long.MAX_VALUE / bytes) {
            return Long.MAX_VALUE;
        }
        return seconds * bytes;
    }

    /**
     * Validates bytes per second.
     *
     * @param bytesPerSecond bytes per second
     * @return bytes per second
     */
    private static long validateBytesPerSecond(final long bytesPerSecond) {
        if (bytesPerSecond <= 0) {
            throw new ValidateException("Frame rate must be greater than zero");
        }
        return bytesPerSecond;
    }

}

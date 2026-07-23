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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.guard.GuardRule;

/**
 * Token bucket guard for frame throughput. One guard instance owns exactly one token bucket, so sessions that share an
 * instance are limited by one global aggregate balance. Per-session limiting requires creating a separate
 * {@code RateGuard} for every session; the framework never clones a guard implicitly. Rejected acquisitions return
 * immediately without waiting, sleeping, or scheduling work.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RateGuard implements GuardRule {

    /**
     * Token-bucket capacity and whole-second refill amount.
     */
    private final long bytesPerSecond;

    /**
     * Concurrent token balance shared by all callers of this guard instance.
     */
    private final AtomicLong available;

    /**
     * Last refill monotonic timestamp.
     */
    private final AtomicLong lastRefillNanos;

    /**
     * Creates a rate guard.
     *
     * @param bytesPerSecond positive bucket capacity and refill rate in bytes per second
     */
    private RateGuard(final long bytesPerSecond) {
        this.bytesPerSecond = validateBytesPerSecond(bytesPerSecond);
        this.available = new AtomicLong(bytesPerSecond);
        this.lastRefillNanos = new AtomicLong(Clock.system().nanos());
    }

    /**
     * Creates a rate guard.
     *
     * @param bytesPerSecond positive bucket capacity and whole-second refill rate
     * @return full token bucket initialized with the requested capacity
     * @throws ValidateException if {@code bytesPerSecond} is not positive
     */
    public static RateGuard of(final long bytesPerSecond) {
        return new RateGuard(bytesPerSecond);
    }

    /**
     * Refills from the system clock and atomically consumes frame-byte tokens without waiting.
     *
     * @param bytes non-negative token count requested by one frame
     * @return passing result after successful consumption, or rejection containing the observed available balance
     * @throws ValidateException if {@code bytes} is negative
     */
    public GuardResult acquire(final long bytes) {
        Assert.isTrue(bytes >= Normal._0, () -> new ValidateException("Requested bytes must be non-negative"));
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
     * @param message message whose payload length supplies the token request
     * @return acquisition result; an unknown negative payload length is treated as zero
     * @throws ValidateException if {@code message} is {@code null}
     */
    @Override
    public GuardResult check(final Message message) {
        final Message checkedMessage = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        final long length = checkedMessage.payload().length();
        return acquire(Math.max(Normal._0, length));
    }

    /**
     * Refills one rate unit for each complete elapsed second reported by a compatible monotonic clock.
     *
     * @param clock monotonic clock sharing the time base used by this guard's previous refill timestamp
     * @throws ValidateException if {@code clock} is {@code null}
     */
    public void refill(final Clock clock) {
        final Clock checkedClock = Assert.notNull(clock, () -> new ValidateException("Runtime clock must not be null"));
        final long now = checkedClock.nanos();
        while (true) {
            final long previous = lastRefillNanos.get();
            final long elapsed = now - previous;
            if (elapsed < Normal.GIGA) {
                return;
            }
            final long seconds = elapsed / Normal.GIGA;
            final long next = previous + seconds * Normal.GIGA;
            if (lastRefillNanos.compareAndSet(previous, next)) {
                addTokens(safeMultiply(seconds, bytesPerSecond));
                return;
            }
        }
    }

    /**
     * Returns available tokens.
     *
     * @return current concurrent token balance
     */
    public long available() {
        return available.get();
    }

    /**
     * Returns rule name.
     *
     * @return {@link Builder#RATE_GUARD_NAME}
     */
    @Override
    public String name() {
        return Builder.RATE_GUARD_NAME;
    }

    /**
     * Adds tokens without exceeding capacity.
     *
     * @param tokens positive refill amount, capped to one bucket capacity
     */
    private void addTokens(final long tokens) {
        if (tokens <= Normal._0) {
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
     * @param seconds non-negative complete elapsed seconds
     * @param bytes   positive bytes refilled per second
     * @return product, or {@link Long#MAX_VALUE} when multiplication would overflow
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
     * @param bytesPerSecond candidate bucket capacity and refill rate
     * @return unchanged positive rate
     * @throws ValidateException if {@code bytesPerSecond} is not positive
     */
    private static long validateBytesPerSecond(final long bytesPerSecond) {
        Assert.isTrue(bytesPerSecond > Normal._0, () -> new ValidateException("Frame rate must be greater than zero"));
        return bytesPerSecond;
    }

}

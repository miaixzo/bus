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
package org.miaixz.bus.auth.metric.shared.validation;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.auth.metric.AuthMetric.ClockSource;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Validates protocol security timestamps against exactly one product-supplied clock reading per operation. It applies
 * explicit clock skew to expiration, not-before, and issued-at comparisons, requires monotonic claim ordering and a
 * bounded issued-at-to-expiration lifetime, and derives strictly positive bounded state TTL values. Arithmetic overflow
 * is rejected and never saturated.
 * <p>
 * <strong>Bus dependencies:</strong> {@link ClockSource} is the sole source of current time, bus-core {@link Assert}
 * validates immutable configuration, and {@link ErrorCode} supplies stable date, expiration, and token failures. The
 * implementation never calls {@link Instant#now()} or another system clock.
 *
 * @author Kimi Liu
 */
public final class TimeValidator {

    /**
     * Product-supplied security clock.
     */
    private final ClockSource clock;

    /**
     * Non-negative comparison tolerance.
     */
    private final Duration skew;

    /**
     * Creates an immutable validator with explicit clock tolerance.
     *
     * @param clock product-supplied security clock
     * @param skew  non-negative clock tolerance
     */
    public TimeValidator(final ClockSource clock, final Duration skew) {
        this.clock = Assert.notNull(clock, () -> new ValidateException("Clock source must not be null"));
        final Duration current = Assert.notNull(skew, () -> new ValidateException("Clock skew must not be null"));
        Assert.isTrue(!current.isNegative(), () -> new ValidateException("Clock skew must not be negative"));
        this.skew = current;
    }

    /**
     * Validates one required instant.
     *
     * @param value source instant
     * @param label safe diagnostic label
     * @return validated instant
     */
    private static Instant instant(final Instant value, final String label) {
        return Assert.notNull(value, () -> new ValidateException(label + " must not be null"));
    }

    /**
     * Validates one positive duration.
     *
     * @param value source duration
     * @param label safe diagnostic label
     * @return positive duration
     */
    private static Duration positive(final Duration value, final String label) {
        final Duration current = Assert.notNull(value, () -> new ValidateException(label + " must not be null"));
        Assert.isTrue(
                !current.isZero() && !current.isNegative(),
                () -> new ValidateException(label + " must be positive"));
        return current;
    }

    /**
     * Adds one duration while rejecting temporal overflow.
     *
     * @param value    source instant
     * @param duration added duration
     * @return exact sum
     */
    private static Instant add(final Instant value, final Duration duration) {
        try {
            return value.plus(duration);
        } catch (final DateTimeException | ArithmeticException failure) {
            throw invalidTime(failure);
        }
    }

    /**
     * Subtracts one duration while rejecting temporal overflow.
     *
     * @param value    source instant
     * @param duration subtracted duration
     * @return exact difference
     */
    private static Instant subtract(final Instant value, final Duration duration) {
        try {
            return value.minus(duration);
        } catch (final DateTimeException | ArithmeticException failure) {
            throw invalidTime(failure);
        }
    }

    /**
     * Computes an exact duration while rejecting temporal overflow.
     *
     * @param start inclusive start instant
     * @param end   exclusive end instant
     * @return exact duration
     */
    private static Duration between(final Instant start, final Instant end) {
        try {
            return Duration.between(start, end);
        } catch (final DateTimeException | ArithmeticException failure) {
            throw invalidTime(failure);
        }
    }

    /**
     * Creates a stable invalid-date failure without exposing a temporal input.
     *
     * @param cause arithmetic cause
     * @return protocol failure
     */
    private static ProtocolException invalidTime(final RuntimeException cause) {
        return new ProtocolException(ErrorCode._100301.getKey(), ErrorCode._100301.getValue(), cause);
    }

    /**
     * Requires one timestamp security invariant.
     *
     * @param condition required invariant
     */
    private static void require(final boolean condition) {
        if (!condition) {
            throw new ProtocolException(ErrorCode._100533);
        }
    }

    /**
     * Validates required issued-at and expiration claims plus an optional not-before claim.
     *
     * @param issuedAt        required issued-at instant
     * @param notBefore       optional not-before instant
     * @param expiresAt       required expiration instant
     * @param maximumLifetime positive maximum issued-at-to-expiration lifetime
     */
    public void validate(
            final Instant issuedAt,
            final Instant notBefore,
            final Instant expiresAt,
            final Duration maximumLifetime) {
        final Instant issued = instant(issuedAt, "Issued-at time");
        final Instant expires = instant(expiresAt, "Expiration time");
        final Duration lifetime = positive(maximumLifetime, "Maximum lifetime");
        final Instant now = now();
        final Instant latestAccepted = add(now, skew);
        final Instant earliestAcceptedExpiration = subtract(now, skew);
        if (expires.isBefore(earliestAcceptedExpiration)) {
            throw new ProtocolException(ErrorCode._100507);
        }
        require(!issued.isAfter(latestAccepted));
        if (notBefore != null) {
            final Instant start = instant(notBefore, "Not-before time");
            require(!start.isAfter(latestAccepted));
            require(!start.isAfter(expires));
        }
        require(issued.isBefore(expires));
        require(between(issued, expires).compareTo(lifetime) <= 0);
    }

    /**
     * Derives a positive state lifetime from the injected clock and rejects values beyond a caller limit.
     *
     * @param expiresAt  required expiration instant
     * @param maximumTtl positive maximum state lifetime
     * @return positive lifetime no greater than the supplied maximum
     */
    public Duration ttl(final Instant expiresAt, final Duration maximumTtl) {
        final Instant expires = instant(expiresAt, "Expiration time");
        final Duration maximum = positive(maximumTtl, "Maximum TTL");
        final Duration result = between(now(), expires);
        if (result.isZero() || result.isNegative()) {
            throw new ProtocolException(ErrorCode._100507);
        }
        require(result.compareTo(maximum) <= 0);
        return result;
    }

    /**
     * Reads and validates one current security instant.
     *
     * @return current security instant
     */
    private Instant now() {
        return instant(clock.now(), "Clock result");
    }

}

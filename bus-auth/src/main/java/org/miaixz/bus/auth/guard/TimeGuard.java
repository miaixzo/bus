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
package org.miaixz.bus.auth.guard;

import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Applies one shared clock and maximum clock-skew policy to authentication timestamps and operation timeouts.
 * <p>
 * Protocol implementations remain responsible for deciding which standard timestamp fields are required. This guard
 * provides reusable checks for issued-at, not-before, expiration, and complete validity windows without reading system
 * time or inventing protocol-specific timestamp models.
 * </p>
 *
 * @author Kimi Liu
 */
public class TimeGuard {

    /**
     * Shared Fabric time source supplied by runtime assembly.
     */
    private final Clock clock;

    /**
     * Maximum accepted clock displacement in either direction.
     */
    private final Duration maximumSkew;

    /**
     * Creates a time guard with one immutable clock-skew limit.
     *
     * @param clock       shared Fabric time source
     * @param maximumSkew non-negative maximum accepted clock skew
     * @throws IllegalArgumentException if either argument is {@code null}
     * @throws ValidateException        if the maximum skew is negative
     */
    public TimeGuard(final Clock clock, final Duration maximumSkew) {
        this.clock = Assert.notNull(clock, "Time guard clock must not be null");
        this.maximumSkew = Assert.notNull(maximumSkew, "Maximum clock skew must not be null");
        if (maximumSkew.isNegative()) {
            throw new ValidateException("Maximum clock skew must not be negative");
        }
    }

    /**
     * Rejects work after the shared end-to-end deadline has been reached.
     *
     * @param timeout shared operation timeout
     * @throws IllegalArgumentException if {@code timeout} is {@code null}
     * @throws ValidateException        if the timeout is exhausted
     */
    public void validateTimeout(final Timeout timeout) {
        Assert.notNull(timeout, "Authentication timeout must not be null");
        if (timeout.expired()) {
            throw new ValidateException("Authentication timeout has expired");
        }
    }

    /**
     * Validates that an issued-at timestamp is not unacceptably far in the future.
     *
     * @param issuedAt issued-at timestamp from the protocol object
     * @param timeout  shared operation timeout
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the timeout is exhausted or the timestamp exceeds the skew allowance
     */
    public void validateIssuedAt(final Instant issuedAt, final Timeout timeout) {
        validateTimeout(timeout);
        validateIssuedAt(issuedAt);
    }

    /**
     * Validates that an issued-at timestamp is not unacceptably far in the future without requiring an operation
     * timeout.
     *
     * @param issuedAt issued-at timestamp from the protocol object
     * @throws IllegalArgumentException if {@code issuedAt} is {@code null}
     * @throws ValidateException        if the timestamp exceeds the skew allowance
     */
    public void validateIssuedAt(final Instant issuedAt) {
        Assert.notNull(issuedAt, "Issued-at timestamp must not be null");
        if (issuedAt.isAfter(clock.now().plus(maximumSkew))) {
            throw new ValidateException("Issued-at timestamp is later than the permitted clock skew");
        }
    }

    /**
     * Validates that a not-before timestamp has become active within the skew allowance.
     *
     * @param notBefore not-before timestamp from the protocol object
     * @param timeout   shared operation timeout
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the timeout is exhausted or the timestamp is still in the future
     */
    public void validateNotBefore(final Instant notBefore, final Timeout timeout) {
        validateTimeout(timeout);
        validateNotBefore(notBefore);
    }

    /**
     * Validates that a not-before timestamp has become active without requiring an operation timeout.
     *
     * @param notBefore not-before timestamp from the protocol object
     * @throws IllegalArgumentException if {@code notBefore} is {@code null}
     * @throws ValidateException        if the timestamp is still in the future beyond the skew allowance
     */
    public void validateNotBefore(final Instant notBefore) {
        Assert.notNull(notBefore, "Not-before timestamp must not be null");
        if (notBefore.isAfter(clock.now().plus(maximumSkew))) {
            throw new ValidateException("Not-before timestamp has not become active within the permitted clock skew");
        }
    }

    /**
     * Validates that an expiration timestamp remains valid within the skew allowance.
     *
     * @param expiresAt expiration timestamp from the protocol object
     * @param timeout   shared operation timeout
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the timeout is exhausted or the timestamp has expired
     */
    public void validateExpiration(final Instant expiresAt, final Timeout timeout) {
        validateTimeout(timeout);
        validateExpiration(expiresAt);
    }

    /**
     * Validates that an expiration timestamp remains valid without requiring an operation timeout.
     *
     * @param expiresAt expiration timestamp from the protocol object
     * @throws IllegalArgumentException if {@code expiresAt} is {@code null}
     * @throws ValidateException        if the timestamp has expired beyond the skew allowance
     */
    public void validateExpiration(final Instant expiresAt) {
        Assert.notNull(expiresAt, "Expiration timestamp must not be null");
        if (!expiresAt.isAfter(clock.now().minus(maximumSkew))) {
            throw new ValidateException("Expiration timestamp is outside the permitted clock skew");
        }
    }

    /**
     * Validates a complete issued-at, optional not-before, and expiration window.
     *
     * @param issuedAt  issued-at timestamp
     * @param notBefore optional not-before timestamp
     * @param expiresAt expiration timestamp
     * @param timeout   shared operation timeout
     * @throws IllegalArgumentException if an argument or optional container is {@code null}
     * @throws ValidateException        if a temporal check fails or the declared window has no valid ordering
     */
    public void validateWindow(
            final Instant issuedAt,
            final Optional<Instant> notBefore,
            final Instant expiresAt,
            final Timeout timeout) {
        validateTimeout(timeout);
        validateWindow(issuedAt, notBefore, expiresAt);
    }

    /**
     * Validates a complete issued-at, optional not-before, and expiration window without requiring an operation
     * timeout.
     *
     * @param issuedAt  issued-at timestamp
     * @param notBefore optional not-before timestamp
     * @param expiresAt expiration timestamp
     * @throws IllegalArgumentException if an argument or optional container is {@code null}
     * @throws ValidateException        if a temporal check fails or the declared window has no valid ordering
     */
    public void validateWindow(final Instant issuedAt, final Optional<Instant> notBefore, final Instant expiresAt) {
        Assert.notNull(notBefore, "Not-before timestamp container must not be null");
        validateIssuedAt(issuedAt);
        final Instant lowerBound = notBefore.getOrNull();
        if (lowerBound != null) {
            validateNotBefore(lowerBound);
        }
        validateExpiration(expiresAt);
        if (!issuedAt.isBefore(expiresAt)) {
            throw new ValidateException("Issued-at timestamp must precede expiration");
        }
        if (lowerBound != null && !lowerBound.isBefore(expiresAt)) {
            throw new ValidateException("Not-before timestamp must precede expiration");
        }
    }

    /**
     * Validates that a token age measured from one issued-at timestamp remains below an explicit maximum.
     *
     * @param issuedAt   issued-at timestamp
     * @param maximumAge positive maximum accepted age
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the maximum is not positive or the token is too old
     */
    public void validateMaximumAge(final Instant issuedAt, final Duration maximumAge) {
        Assert.notNull(issuedAt, "Issued-at timestamp must not be null");
        Assert.notNull(maximumAge, "Maximum age must not be null");
        if (maximumAge.isZero() || maximumAge.isNegative()) {
            throw new ValidateException("Maximum age must be positive");
        }
        try {
            if (!issuedAt.plus(maximumAge).isAfter(clock.now())) {
                throw new ValidateException("Issued-at timestamp exceeds the permitted maximum age");
            }
        } catch (ArithmeticException cause) {
            throw new ValidateException("Maximum-age calculation exceeds the supported Instant range", cause);
        }
    }

}

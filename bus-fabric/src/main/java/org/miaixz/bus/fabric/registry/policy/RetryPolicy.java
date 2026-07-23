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
package org.miaixz.bus.fabric.registry.policy;

import java.io.IOException;
import java.time.Duration;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Immutable retry and redirect policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RetryPolicy {

    /**
     * Exclusive upper bound applied to retry attempt indices and redirect follow-up counts.
     */
    private final int maxFollowUps;

    /**
     * Whether connection failures are retryable.
     */
    private final boolean retryOnConnectionFailure;

    /**
     * Non-negative base delay used by exponential retry backoff.
     */
    private final Duration baseDelay;

    /**
     * Creates a retry policy.
     *
     * @param maxFollowUps             non-negative retry and redirect follow-up limit
     * @param retryOnConnectionFailure whether recognized connection failures may be retried
     * @param baseDelay                non-negative base delay for retry backoff
     */
    private RetryPolicy(final int maxFollowUps, final boolean retryOnConnectionFailure, final Duration baseDelay) {
        this.maxFollowUps = maxFollowUps;
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        this.baseDelay = baseDelay;
    }

    /**
     * Returns the default retry policy.
     *
     * @return shared default policy
     */
    public static RetryPolicy defaults() {
        return Instances.get(RetryPolicy.class.getName() + ".defaults", () -> builder().build());
    }

    /**
     * Creates a retry policy builder.
     *
     * @return new builder seeded with default retry settings
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether an exception is retryable.
     *
     * @param cause   non-null failure to classify directly
     * @param attempt non-negative zero-based retry attempt index
     * @return true when connection-failure retries are enabled, the attempt is below the limit, and the cause matches
     */
    public boolean retry(final Throwable cause, final int attempt) {
        final Throwable current = Assert.notNull(
                cause,
                () -> new ValidateException("Retry cause must be non-null and attempt must not be negative"));
        Assert.isFalse(
                attempt < Normal._0,
                () -> new ValidateException("Retry cause must be non-null and attempt must not be negative"));
        return retryOnConnectionFailure && attempt < maxFollowUps && connectionFailure(current);
    }

    /**
     * Returns whether a status may redirect.
     *
     * @param status    HTTP response status code of at least 100
     * @param followUps non-negative number of follow-ups already considered
     * @return true when below the limit and the status is one of the six supported redirect codes
     */
    public boolean redirect(final int status, final int followUps) {
        Assert.isFalse(
                status < Normal._100 || followUps < Normal._0,
                () -> new ValidateException("Status and follow-up count are invalid"));
        return followUps < maxFollowUps && switch (status) {
            case Http.Status.MULTIPLE_CHOICES, Http.Status.MOVED_PERMANENTLY, Http.Status.FOUND, Http.Status.SEE_OTHER, Http.Status.TEMPORARY_REDIRECT, Http.Status.PERMANENT_REDIRECT -> true;
            default -> false;
        };
    }

    /**
     * Returns retry delay for an attempt.
     *
     * @param attempt non-negative retry attempt index
     * @return zero for attempt zero or a zero base delay; otherwise base delay multiplied by a capped power of two
     */
    public Duration delay(final int attempt) {
        Assert.isFalse(attempt < Normal._0, () -> new ValidateException("Retry attempt must not be negative"));
        if (attempt == Normal._0 || baseDelay.isZero()) {
            return Duration.ZERO;
        }
        return baseDelay.multipliedBy(1L << Math.min(attempt - Normal._1, Normal._30));
    }

    /**
     * Returns whether a cause is a connection failure.
     *
     * @param cause non-null failure instance to classify without traversing its cause chain
     * @return true for fabric, JDK socket, connect, or other I/O exceptions
     */
    private static boolean connectionFailure(final Throwable cause) {
        return cause instanceof SocketException || cause instanceof java.net.SocketException
                || cause instanceof java.net.ConnectException || cause instanceof IOException;
    }

    /**
     * Builder for retry policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Maximum follow-ups candidate.
         */
        private int maxFollowUps = Normal._20;

        /**
         * Retry flag candidate.
         */
        private boolean retryOnConnectionFailure = true;

        /**
         * Base delay candidate.
         */
        private Duration baseDelay = Duration.ZERO;

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets maximum follow-ups.
         *
         * @param max non-negative retry and redirect follow-up limit
         * @return this builder
         */
        public Builder maxFollowUps(final int max) {
            Assert.isFalse(max < Normal._0, () -> new ValidateException("Max follow-ups must not be negative"));
            this.maxFollowUps = max;
            return this;
        }

        /**
         * Sets connection failure retry behavior.
         *
         * @param enabled whether recognized connection failures may be retried
         * @return this builder
         */
        public Builder retryOnConnectionFailure(final boolean enabled) {
            this.retryOnConnectionFailure = enabled;
            return this;
        }

        /**
         * Builds an immutable retry policy.
         *
         * @return immutable retry policy containing the validated builder state
         */
        public RetryPolicy build() {
            final Duration currentDelay = Assert
                    .notNull(baseDelay, () -> new ValidateException("Base delay must be non-null and non-negative"));
            Assert.isFalse(
                    currentDelay.isNegative(),
                    () -> new ValidateException("Base delay must be non-null and non-negative"));
            return new RetryPolicy(maxFollowUps, retryOnConnectionFailure, currentDelay);
        }

    }

}

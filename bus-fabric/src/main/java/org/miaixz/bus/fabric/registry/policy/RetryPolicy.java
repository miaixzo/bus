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
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable retry and redirect policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RetryPolicy {

    /**
     * Maximum follow-up count.
     */
    private final int maxFollowUps;

    /**
     * Whether connection failures are retryable.
     */
    private final boolean retryOnConnectionFailure;

    /**
     * Base retry delay.
     */
    private final Duration baseDelay;

    /**
     * Creates a retry policy.
     *
     * @param maxFollowUps             max follow-up count
     * @param retryOnConnectionFailure retry on connection failure
     * @param baseDelay                base delay
     */
    private RetryPolicy(final int maxFollowUps, final boolean retryOnConnectionFailure, final Duration baseDelay) {
        this.maxFollowUps = maxFollowUps;
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        this.baseDelay = baseDelay;
    }

    /**
     * Returns the default retry policy.
     *
     * @return default policy
     */
    public static RetryPolicy defaults() {
        return Instances.get(RetryPolicy.class.getName() + ".defaults", () -> builder().build());
    }

    /**
     * Creates a retry policy builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether an exception is retryable.
     *
     * @param cause   failure cause
     * @param attempt attempt index
     * @return true when retry is allowed
     */
    public boolean retry(final Throwable cause, final int attempt) {
        if (cause == null || attempt < 0) {
            throw new ValidateException("Retry cause must be non-null and attempt must not be negative");
        }
        return retryOnConnectionFailure && attempt < maxFollowUps && connectionFailure(cause);
    }

    /**
     * Returns whether a status may redirect.
     *
     * @param status    HTTP status
     * @param followUps current follow-up count
     * @return true when redirect is allowed
     */
    public boolean redirect(final int status, final int followUps) {
        if (status < 100 || followUps < 0) {
            throw new ValidateException("Status and follow-up count are invalid");
        }
        return followUps < maxFollowUps && switch (status) {
            case 300, 301, 302, 303, 307, 308 -> true;
            default -> false;
        };
    }

    /**
     * Returns retry delay for an attempt.
     *
     * @param attempt attempt index
     * @return retry delay
     */
    public Duration delay(final int attempt) {
        if (attempt < 0) {
            throw new ValidateException("Retry attempt must not be negative");
        }
        if (attempt == 0 || baseDelay.isZero()) {
            return Duration.ZERO;
        }
        return baseDelay.multipliedBy(1L << Math.min(attempt - 1, 30));
    }

    /**
     * Returns whether a cause is a connection failure.
     *
     * @param cause cause
     * @return true when connection related
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
        private int maxFollowUps = 20;

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
         * @param max maximum follow-ups
         * @return this builder
         */
        public Builder maxFollowUps(final int max) {
            if (max < 0) {
                throw new ValidateException("Max follow-ups must not be negative");
            }
            this.maxFollowUps = max;
            return this;
        }

        /**
         * Sets connection failure retry behavior.
         *
         * @param enabled enabled flag
         * @return this builder
         */
        public Builder retryOnConnectionFailure(final boolean enabled) {
            this.retryOnConnectionFailure = enabled;
            return this;
        }

        /**
         * Builds an immutable retry policy.
         *
         * @return retry policy
         */
        public RetryPolicy build() {
            if (baseDelay == null || baseDelay.isNegative()) {
                throw new ValidateException("Base delay must be non-null and non-negative");
            }
            return new RetryPolicy(maxFollowUps, retryOnConnectionFailure, baseDelay);
        }

    }

}

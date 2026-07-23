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
package org.miaixz.bus.fabric.protocol.http.retry;

import java.io.IOException;
import java.time.Duration;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable retry and redirect policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpRetryPolicy implements Policy {

    /**
     * Typed option for the complete HTTP retry and redirect policy.
     */
    public static final Options.Key<HttpRetryPolicy> OPTION = Options.key("http.retry.policy", HttpRetryPolicy.class);

    /**
     * Exclusive upper bound applied to connection-failure retry attempt indices.
     */
    private final int maxAttempts;

    /**
     * Exclusive upper bound applied to redirect follow-up counts.
     */
    private final int maxRedirects;

    /**
     * Whether connection failures are retryable.
     */
    private final boolean retryOnConnectionFailure;

    /**
     * Non-negative base delay used by exponential retry backoff.
     */
    private final Duration baseDelay;

    /**
     * Maximum retry delay used to saturate exponential backoff.
     */
    private final Duration maxDelay;

    /**
     * Creates a retry policy.
     *
     * @param maxAttempts              non-negative retry attempt limit
     * @param maxRedirects             non-negative redirect follow-up limit
     * @param retryOnConnectionFailure whether recognized connection failures may be retried
     * @param baseDelay                non-negative base delay for retry backoff
     * @param maxDelay                 maximum retry backoff
     */
    private HttpRetryPolicy(final int maxAttempts, final int maxRedirects, final boolean retryOnConnectionFailure,
            final Duration baseDelay, final Duration maxDelay) {
        this.maxAttempts = maxAttempts;
        this.maxRedirects = maxRedirects;
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    /**
     * Returns the default retry policy.
     *
     * @return shared default policy
     */
    public static HttpRetryPolicy defaults() {
        return Instances.get(HttpRetryPolicy.class.getName() + ".defaults", () -> builder().build());
    }

    /**
     * Resolves the complete retry policy from options.
     *
     * @param options option source
     * @return configured policy or shared defaults
     */
    public static HttpRetryPolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final HttpRetryPolicy configured = current.get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Adds this complete policy to an option snapshot.
     *
     * @param options option source
     * @return updated option snapshot
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
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
        return retryOnConnectionFailure && attempt < maxAttempts && connectionFailure(current);
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
        return followUps < maxRedirects && switch (status) {
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
        try {
            final Duration calculated = baseDelay.multipliedBy(1L << Math.min(attempt - Normal._1, Normal._30));
            return calculated.compareTo(maxDelay) > Normal._0 ? maxDelay : calculated;
        } catch (final ArithmeticException ignored) {
            return maxDelay;
        }
    }

    /**
     * Returns the retry-attempt limit.
     *
     * @return maximum retry attempts
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the redirect-follow-up limit.
     *
     * @return maximum redirects
     */
    public int maxRedirects() {
        return maxRedirects;
    }

    /**
     * Returns whether connection failures may be retried.
     *
     * @return connection failure retry flag
     */
    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * Returns the base retry delay.
     *
     * @return base retry delay
     */
    public Duration baseDelay() {
        return baseDelay;
    }

    /**
     * Returns the maximum retry delay.
     *
     * @return maximum retry delay
     */
    public Duration maxDelay() {
        return maxDelay;
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
        private int maxAttempts = Normal._20;

        /**
         * Redirect follow-up limit candidate.
         */
        private int maxRedirects = Normal._20;

        /**
         * Retry flag candidate.
         */
        private boolean retryOnConnectionFailure = true;

        /**
         * Base delay candidate.
         */
        private Duration baseDelay = Duration.ZERO;

        /**
         * Maximum retry delay candidate.
         */
        private Duration maxDelay = Duration.ofSeconds(Long.MAX_VALUE);

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
            this.maxAttempts = max;
            this.maxRedirects = max;
            return this;
        }

        /**
         * Sets the retry-attempt limit.
         *
         * @param max non-negative retry-attempt limit
         * @return this builder
         */
        public Builder maxAttempts(final int max) {
            Assert.isFalse(max < Normal._0, () -> new ValidateException("Max attempts must not be negative"));
            this.maxAttempts = max;
            return this;
        }

        /**
         * Sets the redirect-follow-up limit.
         *
         * @param max non-negative redirect limit
         * @return this builder
         */
        public Builder maxRedirects(final int max) {
            Assert.isFalse(max < Normal._0, () -> new ValidateException("Max redirects must not be negative"));
            this.maxRedirects = max;
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
         * Sets the exponential-backoff base delay.
         *
         * @param delay non-negative base delay
         * @return this builder
         */
        public Builder baseDelay(final Duration delay) {
            this.baseDelay = duration(delay, "Base delay");
            return this;
        }

        /**
         * Sets the maximum retry delay.
         *
         * @param delay non-negative maximum delay
         * @return this builder
         */
        public Builder maxDelay(final Duration delay) {
            this.maxDelay = duration(delay, "Maximum delay");
            return this;
        }

        /**
         * Builds an immutable retry policy.
         *
         * @return immutable retry policy containing the validated builder state
         */
        public HttpRetryPolicy build() {
            final Duration currentBase = duration(baseDelay, "Base delay");
            final Duration currentMaximum = duration(maxDelay, "Maximum delay");
            if (currentMaximum.compareTo(currentBase) < Normal._0) {
                throw new ValidateException("Maximum delay must not be less than base delay");
            }
            return new HttpRetryPolicy(maxAttempts, maxRedirects, retryOnConnectionFailure, currentBase,
                    currentMaximum);
        }

        /**
         * Validates a non-negative delay.
         *
         * @param value delay candidate
         * @param name  component name
         * @return validated delay
         */
        private static Duration duration(final Duration value, final String name) {
            final Duration checked = Assert
                    .notNull(value, () -> new ValidateException(name + " must be non-null and non-negative"));
            Assert.isFalse(
                    checked.isNegative(),
                    () -> new ValidateException(name + " must be non-null and non-negative"));
            return checked;
        }

    }

}

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

import java.time.Duration;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable policy for connection pool limits and timeouts.
 *
 * @param maxIdle                      maximum idle connections, from zero through {@code maxConnections}
 * @param keepAlive                    non-negative idle keep-alive duration representable in nanoseconds
 * @param maxConnections               positive maximum total connections
 * @param maxConnectionsPerDestination per-destination limit from one through {@code maxConnections}
 * @param acquireTimeout               non-negative acquisition timeout representable in nanoseconds
 * @author Kimi Liu
 * @since Java 21+
 */
public record PoolPolicy(int maxIdle, Duration keepAlive, int maxConnections, int maxConnectionsPerDestination,
        Duration acquireTimeout) implements Policy {

    /**
     * Typed option for the connection pool policy.
     */
    public static final Options.Key<PoolPolicy> OPTION = Options.key("pool.policy", PoolPolicy.class);

    /**
     * Creates a pool policy.
     *
     * @param maxIdle                      maximum idle connections
     * @param keepAlive                    non-negative idle keep-alive duration
     * @param maxConnections               positive maximum total connections
     * @param maxConnectionsPerDestination positive per-destination connection limit
     * @param acquireTimeout               non-negative acquisition timeout
     * @throws ValidateException if a count relationship is invalid, a duration is null or negative, or a duration
     *                           cannot be represented in signed nanoseconds
     */
    public PoolPolicy {
        Assert.isTrue(
                maxConnections > Normal._0,
                () -> new ValidateException("Max connections must be greater than zero"));
        Assert.isTrue(
                maxIdle >= Normal._0 && maxIdle <= maxConnections,
                () -> new ValidateException("Max idle must be between zero and max connections"));
        Assert.isTrue(
                maxConnectionsPerDestination >= Normal._1 && maxConnectionsPerDestination <= maxConnections,
                () -> new ValidateException("Max connections per destination must be between one and max connections"));
        keepAlive = Builder.validateDuration(keepAlive, "Keep alive");
        acquireTimeout = Builder.validateDuration(acquireTimeout, "Acquire timeout");
    }

    /**
     * Returns default policy values.
     *
     * @return process-wide default policy with 14 idle, 256 total, 256 per destination, five-minute keep-alive, and a
     *         30-second acquisition timeout
     */
    public static PoolPolicy defaults() {
        return Instances.get(PoolPolicy.class.getName() + ".defaults", () -> builder().build());
    }

    /**
     * Resolves the connection pool policy from options.
     *
     * @param options option source
     * @return configured policy or shared defaults
     */
    public static PoolPolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final PoolPolicy configured = current.get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Adds this policy to an immutable option snapshot.
     *
     * @param options option source
     * @return updated option snapshot
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

    /**
     * Creates a policy builder.
     *
     * @return new builder initialized with the default policy values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns maximum idle connections.
     *
     * @return validated maximum number of retained idle connections
     */
    public int maxIdle() {
        return maxIdle;
    }

    /**
     * Returns idle keep-alive duration.
     *
     * @return non-negative idle keep-alive duration
     */
    public Duration keepAlive() {
        return keepAlive;
    }

    /**
     * Returns maximum total connections.
     *
     * @return positive global connection limit
     */
    public int maxConnections() {
        return maxConnections;
    }

    /**
     * Returns maximum connections per destination.
     *
     * @return positive per-destination limit not exceeding the global limit
     */
    @Override
    public int maxConnectionsPerDestination() {
        return maxConnectionsPerDestination;
    }

    /**
     * Returns acquire timeout.
     *
     * @return non-negative connection acquisition timeout
     */
    public Duration acquireTimeout() {
        return acquireTimeout;
    }

    /**
     * Returns the idle keep-alive duration as primitive nanoseconds.
     *
     * @return keep-alive nanoseconds, including zero for immediate expiry
     */
    public long keepAliveNanos() {
        return keepAlive.toNanos();
    }

    /**
     * Returns the acquire timeout as primitive nanoseconds.
     *
     * @return acquire timeout nanoseconds, including zero for an immediate timeout
     */
    public long acquireTimeoutNanos() {
        return acquireTimeout.toNanos();
    }

    /**
     * Builder for pool policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Maximum idle candidate, validated against the total limit at build time.
         */
        private int maxIdle = Normal._16;

        /**
         * Keep-alive candidate.
         */
        private Duration keepAlive = Duration.ofMinutes(Normal._5);

        /**
         * Maximum connections candidate.
         */
        private int maxConnections = Normal._512;

        /**
         * Per-destination limit candidate, clamped downward when the global limit is reduced.
         */
        private int maxConnectionsPerDestination = maxConnections;

        /**
         * Acquire timeout candidate.
         */
        private Duration acquireTimeout = Duration.ofSeconds(Normal._30);

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets maximum idle connections.
         *
         * @param value non-negative maximum idle connection count
         * @return this builder
         * @throws ValidateException if {@code value} is negative
         */
        public Builder maxIdle(final int value) {
            Assert.isFalse(value < 0, () -> new ValidateException("Max idle must not be negative"));
            this.maxIdle = value;
            return this;
        }

        /**
         * Sets keep-alive duration.
         *
         * @param value non-negative keep-alive duration representable in nanoseconds
         * @return this builder
         * @throws ValidateException if {@code value} is null, negative, or outside signed nanosecond range
         */
        public Builder keepAlive(final Duration value) {
            this.keepAlive = validateDuration(value, "Keep alive");
            return this;
        }

        /**
         * Sets maximum total connections.
         *
         * @param value positive maximum total connection count
         * @return this builder
         * @throws ValidateException if {@code value} is not positive
         */
        public Builder maxConnections(final int value) {
            Assert.isTrue(value > Normal._0, () -> new ValidateException("Max connections must be greater than zero"));
            this.maxConnections = value;
            this.maxConnectionsPerDestination = Math.min(maxConnectionsPerDestination, value);
            return this;
        }

        /**
         * Sets maximum connections per destination.
         *
         * @param value per-destination limit from one through the current global limit
         * @return this builder
         * @throws ValidateException if {@code value} is outside the permitted range
         */
        public Builder maxConnectionsPerDestination(final int value) {
            Assert.isTrue(
                    value >= Normal._1 && value <= maxConnections,
                    () -> new ValidateException(
                            "Max connections per destination must be between one and max connections"));
            this.maxConnectionsPerDestination = value;
            return this;
        }

        /**
         * Sets acquire timeout.
         *
         * @param value non-negative acquisition timeout representable in nanoseconds
         * @return this builder
         * @throws ValidateException if {@code value} is null, negative, or outside signed nanosecond range
         */
        public Builder acquireTimeout(final Duration value) {
            this.acquireTimeout = validateDuration(value, "Acquire timeout");
            return this;
        }

        /**
         * Builds an immutable policy.
         *
         * @return immutable policy containing the current builder values
         * @throws ValidateException if maximum idle exceeds the global limit or another policy invariant is invalid
         */
        public PoolPolicy build() {
            Assert.isTrue(maxConnections > 0, () -> new ValidateException("Max connections must be greater than zero"));
            Assert.isFalse(
                    maxIdle > maxConnections,
                    () -> new ValidateException("Max idle must not exceed max connections"));
            return new PoolPolicy(maxIdle, keepAlive, maxConnections, maxConnectionsPerDestination, acquireTimeout);
        }

        /**
         * Validates durations.
         *
         * @param value duration to validate
         * @param name  logical duration name used in validation messages
         * @return the same non-null, non-negative duration after nanosecond-range validation
         * @throws ValidateException if the duration is null, negative, or outside signed nanosecond range
         */
        private static Duration validateDuration(final Duration value, final String name) {
            final Duration current = Assert
                    .notNull(value, () -> new ValidateException(name + " must be non-null and non-negative"));
            Assert.isFalse(
                    current.isNegative(),
                    () -> new ValidateException(name + " must be non-null and non-negative"));
            try {
                current.toNanos();
            } catch (final ArithmeticException e) {
                throw new ValidateException(name + " must fit in signed nanoseconds");
            }
            return current;
        }

    }

}

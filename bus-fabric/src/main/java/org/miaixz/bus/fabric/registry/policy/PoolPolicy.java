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

/**
 * Immutable policy for connection pool limits and timeouts.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class PoolPolicy {

    /**
     * Maximum idle connections.
     */
    private final int maxIdle;

    /**
     * Idle keep-alive duration.
     */
    private final Duration keepAlive;

    /**
     * Maximum total connections.
     */
    private final int maxConnections;

    /**
     * Acquire timeout.
     */
    private final Duration acquireTimeout;

    /**
     * Creates a pool policy.
     *
     * @param maxIdle        maximum idle connections
     * @param keepAlive      keep-alive duration
     * @param maxConnections maximum total connections
     * @param acquireTimeout acquire timeout
     */
    private PoolPolicy(final int maxIdle, final Duration keepAlive, final int maxConnections,
                       final Duration acquireTimeout) {
        this.maxIdle = maxIdle;
        this.keepAlive = keepAlive;
        this.maxConnections = maxConnections;
        this.acquireTimeout = acquireTimeout;
    }

    /**
     * Returns default policy values.
     *
     * @return default policy
     */
    public static PoolPolicy defaults() {
        return Instances.get(PoolPolicy.class.getName() + ".defaults", () -> builder().build());
    }

    /**
     * Creates a policy builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns maximum idle connections.
     *
     * @return maximum idle connections
     */
    public int maxIdle() {
        return maxIdle;
    }

    /**
     * Returns idle keep-alive duration.
     *
     * @return keep-alive duration
     */
    public Duration keepAlive() {
        return keepAlive;
    }

    /**
     * Returns maximum total connections.
     *
     * @return maximum total connections
     */
    public int maxConnections() {
        return maxConnections;
    }

    /**
     * Returns acquire timeout.
     *
     * @return acquire timeout
     */
    public Duration acquireTimeout() {
        return acquireTimeout;
    }

    /**
     * Builder for pool policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Maximum idle candidate.
         */
        private int maxIdle = Normal._5;

        /**
         * Keep-alive candidate.
         */
        private Duration keepAlive = Duration.ofMinutes(Normal._5);

        /**
         * Maximum connections candidate.
         */
        private int maxConnections = Normal._64;

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
         * @param value maximum idle value
         * @return this builder
         */
        public Builder maxIdle(final int value) {
            Assert.isFalse(value < 0, () -> new ValidateException("Max idle must not be negative"));
            this.maxIdle = value;
            return this;
        }

        /**
         * Sets keep-alive duration.
         *
         * @param value keep-alive duration
         * @return this builder
         */
        public Builder keepAlive(final Duration value) {
            this.keepAlive = validateDuration(value, "Keep alive");
            return this;
        }

        /**
         * Sets maximum total connections.
         *
         * @param value maximum total connections
         * @return this builder
         */
        public Builder maxConnections(final int value) {
            Assert.isFalse(value < 0, () -> new ValidateException("Max connections must not be negative"));
            this.maxConnections = value;
            return this;
        }

        /**
         * Sets acquire timeout.
         *
         * @param value acquire timeout
         * @return this builder
         */
        public Builder acquireTimeout(final Duration value) {
            this.acquireTimeout = validateDuration(value, "Acquire timeout");
            return this;
        }

        /**
         * Builds an immutable policy.
         *
         * @return pool policy
         */
        public PoolPolicy build() {
            Assert.isTrue(maxConnections > 0, () -> new ValidateException("Max connections must be greater than zero"));
            Assert.isFalse(
                    maxIdle > maxConnections,
                    () -> new ValidateException("Max idle must not exceed max connections"));
            return new PoolPolicy(maxIdle, keepAlive, maxConnections, acquireTimeout);
        }

        /**
         * Validates durations.
         *
         * @param value value
         * @param name  name
         * @return value
         */
        private static Duration validateDuration(final Duration value, final String name) {
            final Duration current = Assert
                    .notNull(value, () -> new ValidateException(name + " must be non-null and non-negative"));
            Assert.isFalse(
                    current.isNegative(),
                    () -> new ValidateException(name + " must be non-null and non-negative"));
            return current;
        }

    }

}

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
package org.miaixz.bus.auth;

import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;

/**
 * Defines the shared deadline budget propagated through one authentication operation.
 * <p>
 * This namespace does not introduce a second timeout policy. {@link Budget} combines an absolute deadline with the
 * existing Fabric timeout policy so Registry, Store, Resolver, codec, and transport layers consume one decreasing total
 * budget instead of resetting timeouts at every boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Timeout {

    /**
     * Prevents instantiation of the timeout namespace.
     */
    private Timeout() {
        // No initialization required.
    }

    /**
     * Carries an absolute operation deadline and the maximum durations allowed by Fabric transport policy.
     *
     * @param clock    shared Fabric time source used by every remaining-budget calculation
     * @param deadline absolute deadline for the complete authentication operation
     * @param policy   existing Fabric transport timeout policy to truncate
     * @author Kimi Liu
     */
    public record Budget(Clock clock, Instant deadline, org.miaixz.bus.fabric.Timeout policy) {

        /**
         * Creates a budget without evaluating or resetting its deadline.
         *
         * @param clock    shared Fabric time source
         * @param deadline absolute operation deadline
         * @param policy   Fabric timeout policy
         * @throws IllegalArgumentException if any component is {@code null}
         */
        public Budget {
            Assert.notNull(clock, "Authentication budget clock must not be null");
            Assert.notNull(deadline, "Authentication budget deadline must not be null");
            Assert.notNull(policy, "Authentication budget Fabric policy must not be null");
        }

        /**
         * Truncates one enabled Fabric duration while preserving a zero-valued disabled duration.
         *
         * @param configured configured Fabric duration
         * @param available  remaining total operation duration
         * @return zero when disabled, otherwise the lesser duration
         */
        private static Duration cap(final Duration configured, final Duration available) {
            return configured.isZero() || configured.compareTo(available) <= 0 ? configured : available;
        }

        /**
         * Returns the non-negative duration between the shared clock and the original absolute deadline.
         *
         * @return remaining total duration, or {@link Duration#ZERO} after expiration
         */
        public Duration remaining() {
            final Duration duration = Duration.between(clock.now(), deadline);
            return duration.isNegative() ? Duration.ZERO : duration;
        }

        /**
         * Returns whether no total operation time remains.
         *
         * @return {@code true} when the absolute deadline has been reached or passed
         */
        public boolean expired() {
            return remaining().isZero();
        }

        /**
         * Produces a Fabric policy whose six durations cannot exceed the current remaining total budget.
         * <p>
         * Fabric's zero-valued connect, read, write, call, or ping durations retain their documented disabled meaning.
         * An expired budget is rejected before transport construction because Fabric requires a positive close duration
         * and no transport operation may start after the shared deadline.
         * </p>
         *
         * @return Fabric timeout policy truncated to the current remaining total budget
         * @throws ValidateException if the shared operation budget has expired
         */
        public org.miaixz.bus.fabric.Timeout forFabric() {
            final Duration available = remaining();
            if (available.isZero()) {
                throw new ValidateException("Authentication time budget has expired");
            }
            return new org.miaixz.bus.fabric.Timeout(cap(policy.connect(), available), cap(policy.read(), available),
                    cap(policy.write(), available), cap(policy.call(), available), cap(policy.ping(), available),
                    cap(policy.close(), available));
        }

    }

}

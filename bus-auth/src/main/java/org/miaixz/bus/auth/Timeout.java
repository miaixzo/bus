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

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries the shared deadline propagated through one authentication operation.
 * <p>
 * This value does not introduce a second timeout model. It combines an absolute deadline with the configured Fabric
 * timeout so Registry, worker, parser, codec, and transport layers consume one decreasing total duration instead of
 * resetting timeouts at every boundary.
 * </p>
 *
 * @param clock    shared Fabric time source used by every remaining-duration calculation
 * @param deadline absolute deadline for the complete authentication operation
 * @param timeout  configured authentication transport timeout
 * @author Kimi Liu
 */
public record Timeout(Clock clock, Instant deadline, Settings timeout) {

    /**
     * Creates a timeout without evaluating or resetting its deadline.
     *
     * @param clock    shared Fabric time source
     * @param deadline absolute deadline for the complete authentication operation
     * @param timeout  configured authentication transport timeout
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public Timeout {
        Assert.notNull(clock, "Authentication timeout clock must not be null");
        Assert.notNull(deadline, "Authentication timeout deadline must not be null");
        Assert.notNull(timeout, "Authentication transport timeout must not be null");
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
     * Produces a Fabric timeout whose six durations cannot exceed the current remaining total duration.
     * <p>
     * Fabric's zero-valued connect, read, write, call, or ping durations retain their documented disabled meaning. An
     * expired timeout is rejected before transport construction because Fabric requires a positive close duration and
     * no transport operation may start after the shared deadline.
     * </p>
     *
     * @return authentication transport timeout truncated to the current remaining total duration
     * @throws ValidateException if the shared operation timeout has expired
     */
    public Settings effective() {
        final Duration available = remaining();
        if (available.isZero()) {
            throw new ValidateException("Authentication timeout has expired");
        }
        return new Settings(cap(timeout.connect(), available), cap(timeout.read(), available),
                cap(timeout.write(), available), cap(timeout.call(), available), cap(timeout.ping(), available),
                cap(timeout.close(), available));
    }

    /**
     * Carries the six authentication transport timeout dimensions without exposing a Fabric type.
     *
     * @param connect connection-establishment timeout
     * @param read    individual read timeout
     * @param write   individual write timeout
     * @param call    complete transport-call timeout
     * @param ping    keepalive probe timeout
     * @param close   graceful-close timeout
     * @author Kimi Liu
     */
    public record Settings(Duration connect, Duration read, Duration write, Duration call, Duration ping,
            Duration close) {

        /**
         * Validates every configured timeout duration.
         */
        public Settings {
            Assert.notNull(connect, "Authentication connect timeout must not be null");
            Assert.notNull(read, "Authentication read timeout must not be null");
            Assert.notNull(write, "Authentication write timeout must not be null");
            Assert.notNull(call, "Authentication call timeout must not be null");
            Assert.notNull(ping, "Authentication ping timeout must not be null");
            Assert.notNull(close, "Authentication close timeout must not be null");
            if (connect.isNegative() || read.isNegative() || write.isNegative() || call.isNegative()
                    || ping.isNegative() || close.isNegative()) {
                throw new ValidateException("Authentication transport timeout durations must not be negative");
            }
        }

    }

}

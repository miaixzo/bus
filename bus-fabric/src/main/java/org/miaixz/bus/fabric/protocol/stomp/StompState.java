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
package org.miaixz.bus.fabric.protocol.stomp;

import java.time.Duration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable STOMP heartbeat state negotiated independently from the transport {@link org.miaixz.bus.fabric.Timeout}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class StompState {

    /**
     * Shared disabled heartbeat state.
     */
    private static final StompState DISABLED = new StompState(Duration.ZERO, Duration.ZERO);

    /**
     * Outbound heartbeat interval.
     */
    private final Duration outboundHeartbeat;

    /**
     * Inbound heartbeat deadline including tolerance.
     */
    private final Duration inboundHeartbeatDeadline;

    /**
     * Precomputed outbound interval in nanoseconds.
     */
    private final long outboundHeartbeatNanos;

    /**
     * Precomputed inbound deadline in nanoseconds.
     */
    private final long inboundHeartbeatDeadlineNanos;

    /**
     * Creates validated negotiated heartbeat state.
     *
     * @param outboundHeartbeat        outbound heartbeat interval
     * @param inboundHeartbeatDeadline inbound heartbeat deadline including tolerance
     */
    StompState(final Duration outboundHeartbeat, final Duration inboundHeartbeatDeadline) {
        this.outboundHeartbeat = duration(outboundHeartbeat, "STOMP outbound heartbeat");
        this.inboundHeartbeatDeadline = duration(inboundHeartbeatDeadline, "STOMP inbound deadline");
        this.outboundHeartbeatNanos = nanos(this.outboundHeartbeat);
        this.inboundHeartbeatDeadlineNanos = nanos(this.inboundHeartbeatDeadline);
    }

    /**
     * Returns the shared disabled heartbeat state.
     *
     * @return disabled state
     */
    static StompState disabled() {
        return DISABLED;
    }

    /**
     * Returns the outbound heartbeat interval.
     *
     * @return outbound heartbeat interval
     */
    Duration outboundHeartbeat() {
        return outboundHeartbeat;
    }

    /**
     * Returns the inbound heartbeat deadline.
     *
     * @return inbound heartbeat deadline
     */
    Duration inboundHeartbeatDeadline() {
        return inboundHeartbeatDeadline;
    }

    /**
     * Returns the precomputed outbound interval.
     *
     * @return outbound interval in nanoseconds, saturated on overflow
     */
    long outboundHeartbeatNanos() {
        return outboundHeartbeatNanos;
    }

    /**
     * Returns the precomputed inbound deadline.
     *
     * @return inbound deadline in nanoseconds, saturated on overflow
     */
    long inboundHeartbeatDeadlineNanos() {
        return inboundHeartbeatDeadlineNanos;
    }

    /**
     * Validates one non-negative heartbeat duration.
     *
     * @param value duration candidate
     * @param name  field name
     * @return validated duration
     */
    private static Duration duration(final Duration value, final String name) {
        final Duration checked = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        if (checked.isNegative()) {
            throw new ValidateException(name + " must not be negative");
        }
        return checked;
    }

    /**
     * Converts a duration to nanoseconds while saturating values that exceed the primitive range.
     *
     * @param value validated duration
     * @return nanoseconds
     */
    private static long nanos(final Duration value) {
        try {
            return value.toNanos();
        } catch (final ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

}

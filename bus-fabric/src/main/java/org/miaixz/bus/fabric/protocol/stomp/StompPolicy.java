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
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable STOMP heartbeat negotiation policy advertised in the CONNECT frame.
 *
 * @param clientSendHeartbeat    minimum interval at which the client can send heartbeats
 * @param clientReceiveHeartbeat desired interval at which the client receives heartbeats
 * @author Kimi Liu
 * @since Java 21+
 */
public record StompPolicy(Duration clientSendHeartbeat, Duration clientReceiveHeartbeat) implements Policy {

    /**
     * Typed option for the complete STOMP heartbeat policy.
     */
    public static final Options.Key<StompPolicy> OPTION = Options.key("stomp.policy", StompPolicy.class);

    /**
     * Shared policy that disables both heartbeat directions.
     */
    private static final StompPolicy DISABLED = new StompPolicy(Duration.ZERO, Duration.ZERO);

    /**
     * Creates and validates a heartbeat policy.
     *
     * @param clientSendHeartbeat    minimum interval at which the client can send heartbeats
     * @param clientReceiveHeartbeat desired interval at which the client receives heartbeats
     */
    public StompPolicy {
        clientSendHeartbeat = heartbeat(clientSendHeartbeat, "Client send heartbeat");
        clientReceiveHeartbeat = heartbeat(clientReceiveHeartbeat, "Client receive heartbeat");
    }

    /**
     * Returns the shared disabled policy.
     *
     * @return disabled heartbeat policy
     */
    public static StompPolicy disabled() {
        return DISABLED;
    }

    /**
     * Resolves a heartbeat policy from options.
     *
     * @param options option source
     * @return configured policy or the shared disabled policy
     */
    public static StompPolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final StompPolicy configured = current.get(OPTION);
        return configured == null ? disabled() : configured;
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
     * Converts the send capability to milliseconds.
     *
     * @return non-negative send capability in milliseconds
     */
    long clientSendHeartbeatMillis() {
        return clientSendHeartbeat.toMillis();
    }

    /**
     * Converts the receive preference to milliseconds.
     *
     * @return non-negative receive preference in milliseconds
     */
    long clientReceiveHeartbeatMillis() {
        return clientReceiveHeartbeat.toMillis();
    }

    /**
     * Validates one heartbeat duration and its wire millisecond representation.
     *
     * @param value duration candidate
     * @param name  component name
     * @return validated duration
     */
    private static Duration heartbeat(final Duration value, final String name) {
        final Duration checked = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        if (checked.isNegative()) {
            throw new ValidateException(name + " must not be negative");
        }
        try {
            checked.toMillis();
        } catch (final ArithmeticException e) {
            throw new ValidateException(name + " is too large", e);
        }
        return checked;
    }

}

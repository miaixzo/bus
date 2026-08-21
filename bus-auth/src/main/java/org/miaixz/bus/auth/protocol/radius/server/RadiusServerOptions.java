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
package org.miaixz.bus.auth.protocol.radius.server;

import java.util.Set;

import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.radius.RadiusPacket;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Defines the bounded wire versions and security behavior of one server-role RADIUS Source registration.
 * <p>
 * Client addresses, ports, shared secrets, and external implementation identifiers are intentionally absent. The
 * trusted transport boundary owns sockets and ALPN, while the exact {@link RadiusRequestHandler} binding owns client
 * data and contains only credential references.
 * </p>
 *
 * @param versions                    accepted historic or RADIUS/1.1 header semantics
 * @param eapSupported                whether the Provider processes RFC 3579 EAP-Message Attributes
 * @param requireMessageAuthenticator whether all historic Access-Requests require Message-Authenticator
 * @param maximumPacketBytes          maximum accepted and emitted packet size without RFC 7930 extension
 * @author Kimi Liu
 */
public record RadiusServerOptions(Set<RadiusPacket.Version> versions, boolean eapSupported,
        boolean requireMessageAuthenticator, int maximumPacketBytes) implements Options<RadiusServerOptions> {

    /**
     * Smallest valid RADIUS packet containing only the fixed header.
     */
    public static final int MINIMUM_PACKET_BYTES = Normal._20;
    /**
     * RFC 2865 default maximum packet size enforced by the packet codecs.
     */
    public static final int MAXIMUM_PACKET_BYTES = Normal._4096;

    /**
     * Validates and freezes the RADIUS Provider options.
     *
     * @param versions                    non-empty accepted header versions
     * @param eapSupported                whether RFC 3579 pass-through is enabled
     * @param requireMessageAuthenticator whether historic Access packets require Type 80
     * @param maximumPacketBytes          packet limit from 20 through 4096
     * @throws IllegalArgumentException if a component violates the supported RADIUS Provider bounds
     */
    public RadiusServerOptions {
        Assert.notNull(versions, "RADIUS Provider versions must not be null");
        Assert.notEmpty(versions, "RADIUS Provider versions must not be empty");
        versions = Set.copyOf(versions);
        Assert.isTrue(
                maximumPacketBytes >= MINIMUM_PACKET_BYTES && maximumPacketBytes <= MAXIMUM_PACKET_BYTES,
                "RADIUS maximum packet bytes must be between 20 and 4096");
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<RadiusServerOptions> type() {
        return RadiusServerOptions.class;
    }

    @Override
    public RadiusServerOptions snapshot() {
        return this;
    }

}

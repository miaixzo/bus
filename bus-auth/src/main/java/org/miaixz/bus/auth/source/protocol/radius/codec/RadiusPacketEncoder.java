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
package org.miaixz.bus.auth.source.protocol.radius.codec;

import java.nio.ByteBuffer;

import org.miaixz.bus.auth.source.protocol.radius.Radius;
import org.miaixz.bus.auth.source.protocol.radius.RadiusPacket;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * Encodes one complete historic RADIUS or RFC 9765 RADIUS/1.1 packet.
 * <p>
 * Header selection is structural. A {@link RadiusPacket.LegacyHeader} writes Identifier and Authenticator, whereas a
 * {@link RadiusPacket.Radius11Header} writes zero Reserved fields and its four-octet opaque Token.
 * </p>
 *
 * @author Kimi Liu
 */
public class RadiusPacketEncoder implements Encoder<RadiusPacket, byte[]> {

    /**
     * Number of reserved octets after the RADIUS/1.1 Token.
     */
    private static final int RADIUS11_RESERVED_BYTES = Radius.AUTHENTICATOR_BYTES - Radius.TOKEN_BYTES;

    /**
     * Configured packet size ceiling.
     */
    private final int maximumPacketBytes;

    /**
     * Explicit deployment opt-in for the Experimental RFC 9765 profile.
     */
    private final boolean radius11Enabled;

    /**
     * Raw Attribute sequence codec.
     */
    private final RadiusAttributeCodec attributeCodec;

    /**
     * Creates a bounded complete-packet encoder.
     *
     * @param maximumPacketBytes packet limit from 20 through 4096
     * @param radius11Enabled    explicit opt-in for Experimental RADIUS/1.1 encoding
     * @param attributeCodec     raw Attribute codec
     * @throws IllegalArgumentException if an argument violates the configured packet bounds
     */
    public RadiusPacketEncoder(final int maximumPacketBytes, final boolean radius11Enabled,
            final RadiusAttributeCodec attributeCodec) {
        Assert.isTrue(
                maximumPacketBytes >= Radius.HEADER_BYTES && maximumPacketBytes <= Radius.MAXIMUM_PACKET_BYTES,
                "RADIUS encoder maximum packet bytes must be between 20 and 4096");
        this.maximumPacketBytes = maximumPacketBytes;
        this.radius11Enabled = radius11Enabled;
        this.attributeCodec = Assert.notNull(attributeCodec, "RADIUS Attribute codec must not be null");
    }

    /**
     * Rejects packet Codes outside the implemented Access and Accounting set.
     *
     * @param code unsigned packet Code
     * @throws ProtocolException if the Code has no supported typed packet representation
     */
    private static void ensureImplementedCode(final int code) {
        if (code != Radius.Codes.ACCESS_REQUEST && code != Radius.Codes.ACCESS_ACCEPT
                && code != Radius.Codes.ACCESS_REJECT && code != Radius.Codes.ACCOUNTING_REQUEST
                && code != Radius.Codes.ACCOUNTING_RESPONSE && code != Radius.Codes.ACCESS_CHALLENGE) {
            throw new ProtocolException("Unsupported RADIUS packet Code: " + code);
        }
    }

    /**
     * Encodes one supported complete packet with an exact Length field.
     *
     * @param data supported Access or Accounting packet
     * @return complete packet bytes
     * @throws IllegalArgumentException if the packet is {@code null}
     * @throws ProtocolException        if the packet Code or total size is outside the supported RADIUS wire profile
     */
    @Override
    public byte[] encode(final RadiusPacket data) {
        final RadiusPacket packet = Assert.notNull(data, "RADIUS packet must not be null");
        ensureImplementedCode(packet.code().value());
        final byte[] attributes = attributeCodec.encode(packet.attributes());
        if (packet.header() instanceof RadiusPacket.Radius11Header && !radius11Enabled) {
            throw new ProtocolException("Experimental RADIUS/1.1 encoding is not enabled");
        }
        final int length = Radius.HEADER_BYTES + attributes.length;
        final boolean historicAccounting = packet.header() instanceof RadiusPacket.LegacyHeader
                && (packet.code().value() == Radius.Codes.ACCOUNTING_REQUEST
                        || packet.code().value() == Radius.Codes.ACCOUNTING_RESPONSE);
        final int protocolMaximum = historicAccounting ? Radius.HISTORIC_ACCOUNTING_MAXIMUM_BYTES
                : Radius.MAXIMUM_PACKET_BYTES;
        if (length > Math.min(maximumPacketBytes, protocolMaximum)) {
            throw new ProtocolException("RADIUS packet exceeds the configured maximum size");
        }
        final ByteBuffer output = ByteBuffer.allocate(length);
        output.put((byte) packet.code().value());
        if (packet.header() instanceof RadiusPacket.LegacyHeader legacy) {
            output.put((byte) legacy.identifier());
            output.putShort((short) length);
            output.put(legacy.authenticator());
        } else if (packet.header() instanceof RadiusPacket.Radius11Header radius11) {
            output.put((byte) 0);
            output.putShort((short) length);
            output.put(radius11.token());
            output.put(new byte[RADIUS11_RESERVED_BYTES]);
        } else {
            throw new ProtocolException("Unsupported RADIUS packet header");
        }
        output.put(attributes);
        return output.array();
    }

}

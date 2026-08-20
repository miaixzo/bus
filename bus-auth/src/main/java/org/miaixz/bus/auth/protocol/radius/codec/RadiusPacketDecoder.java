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
package org.miaixz.bus.auth.protocol.radius.codec;

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.auth.protocol.radius.*;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * Decodes one already-framed complete RADIUS packet using an explicitly selected header version.
 * <p>
 * The decoder never guesses RADIUS/1.1 from packet bytes. The trusted transport adapter must select the version from
 * UDP/historic TLS or successful TLS 1.3+ ALPN negotiation before constructing this codec.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RadiusPacketDecoder implements Decoder<byte[], RadiusPacket> {

    /**
     * Explicit header semantics selected by the transport boundary.
     */
    private final RadiusPacket.Version version;

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
     * Creates a bounded decoder for exactly one RADIUS header version.
     *
     * @param version            trusted historic or RADIUS/1.1 selection
     * @param maximumPacketBytes packet limit from 20 through 4096
     * @param radius11Enabled    explicit opt-in for Experimental RADIUS/1.1 decoding
     * @param attributeCodec     raw Attribute codec
     * @throws IllegalArgumentException if an argument is null or outside the configured packet bounds
     */
    public RadiusPacketDecoder(final RadiusPacket.Version version, final int maximumPacketBytes,
            final boolean radius11Enabled, final RadiusAttributeCodec attributeCodec) {
        this.version = Assert.notNull(version, "RADIUS decoder version must not be null");
        Assert.isTrue(
                maximumPacketBytes >= Radius.HEADER_BYTES && maximumPacketBytes <= Radius.MAXIMUM_PACKET_BYTES,
                "RADIUS decoder maximum packet bytes must be between 20 and 4096");
        Assert.isTrue(
                version != RadiusPacket.Version.RADIUS_1_1 || radius11Enabled,
                "Experimental RADIUS/1.1 decoding requires explicit opt-in");
        this.maximumPacketBytes = maximumPacketBytes;
        this.radius11Enabled = radius11Enabled;
        this.attributeCodec = Assert.notNull(attributeCodec, "RADIUS Attribute codec must not be null");
    }

    /**
     * Maps one implemented Code to its exact immutable packet record.
     *
     * @param code       unsigned packet Code
     * @param header     decoded version-specific header
     * @param attributes decoded raw Attributes
     * @return exact supported packet model
     * @throws ProtocolException if the Code has no supported typed packet representation
     */
    private static RadiusPacket packet(
            final int code,
            final RadiusPacket.Header header,
            final List<RadiusAttribute> attributes) {
        return switch (code) {
            case Radius.Codes.ACCESS_REQUEST -> new AccessRequest(header, attributes);
            case Radius.Codes.ACCESS_ACCEPT -> new AccessAccept(header, attributes);
            case Radius.Codes.ACCESS_REJECT -> new AccessReject(header, attributes);
            case Radius.Codes.ACCOUNTING_REQUEST -> new AccountingRequest(header, attributes);
            case Radius.Codes.ACCOUNTING_RESPONSE -> new AccountingResponse(header, attributes);
            case Radius.Codes.ACCESS_CHALLENGE -> new AccessChallenge(header, attributes);
            default -> throw new ProtocolException("Unsupported RADIUS packet Code: " + code);
        };
    }

    /**
     * Decodes one complete packet and consumes every input octet.
     *
     * @param encoded one complete RADIUS packet followed by optional transport padding
     * @return one of the six typed packet records supported by this decoder
     * @throws IllegalArgumentException if the input is {@code null}
     * @throws ProtocolException        if Length, Code, header, or Attributes are invalid
     */
    @Override
    public RadiusPacket decode(final byte[] encoded) {
        Assert.notNull(encoded, "RADIUS packet bytes must not be null");
        if (encoded.length < Radius.HEADER_BYTES) {
            throw new ProtocolException("RADIUS packet is shorter than the fixed header");
        }
        final int length = (Byte.toUnsignedInt(encoded[2]) << 8) | Byte.toUnsignedInt(encoded[3]);
        final int code = Byte.toUnsignedInt(encoded[0]);
        if (version == RadiusPacket.Version.RADIUS_1_1 && !radius11Enabled) {
            throw new ProtocolException("Experimental RADIUS/1.1 decoding is not enabled");
        }
        final boolean historicAccounting = version == RadiusPacket.Version.RADIUS_1_0
                && (code == Radius.Codes.ACCOUNTING_REQUEST || code == Radius.Codes.ACCOUNTING_RESPONSE);
        final int protocolMaximum = historicAccounting ? Radius.HISTORIC_ACCOUNTING_MAXIMUM_BYTES
                : Radius.MAXIMUM_PACKET_BYTES;
        if (length < Radius.HEADER_BYTES || length > Math.min(maximumPacketBytes, protocolMaximum)
                || length > encoded.length) {
            throw new ProtocolException("RADIUS Length is outside the configured range or exceeds supplied bytes");
        }
        final RadiusPacket.Header header;
        if (version == RadiusPacket.Version.RADIUS_1_0) {
            header = new RadiusPacket.LegacyHeader(Byte.toUnsignedInt(encoded[1]),
                    Arrays.copyOfRange(encoded, 4, Radius.HEADER_BYTES));
        } else {
            header = new RadiusPacket.Radius11Header(Arrays.copyOfRange(encoded, 4, 8));
        }
        final List<RadiusAttribute> attributes = attributeCodec
                .decode(Arrays.copyOfRange(encoded, Radius.HEADER_BYTES, length));
        return packet(code, header, attributes);
    }

}

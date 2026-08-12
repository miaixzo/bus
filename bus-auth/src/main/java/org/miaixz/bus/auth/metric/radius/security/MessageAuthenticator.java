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
package org.miaixz.bus.auth.metric.radius.security;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.auth.metric.radius.packet.RadiusAttribute;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacket;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Computes and verifies the RFC 2869 Message-Authenticator HMAC-MD5 value.
 */
public final class MessageAuthenticator {

    /**
     * Message-Authenticator attribute type.
     */
    public static final int TYPE = 80;

    /**
     * Prevents construction.
     */
    private MessageAuthenticator() {
        // No initialization required.
    }

    /**
     * Computes one HMAC-MD5 value over packet bytes with its attribute value already zeroed.
     *
     * @param packet zeroed packet bytes
     * @param secret shared secret
     * @return HMAC value
     */
    public static byte[] create(final byte[] packet, final byte[] secret) {
        final byte[] source = Arrays.copyOf(
                Assert.notNull(packet, () -> new ValidateException("RADIUS packet bytes must not be null")),
                packet.length);
        final byte[] key = Arrays.copyOf(
                Assert.notNull(secret, () -> new ValidateException("RADIUS shared secret must not be null")),
                secret.length);
        try {
            return Builder.hmacMd5(key).digest(source);
        } finally {
            Arrays.fill(source, (byte) Normal._0);
            Arrays.fill(key, (byte) Normal._0);
        }
    }

    /**
     * Verifies one value in constant time.
     *
     * @param packet   zeroed packet
     * @param secret   secret
     * @param received received value
     * @return whether valid
     */
    public static boolean verify(final byte[] packet, final byte[] secret, final byte[] received) {
        return received != null && MessageDigest.isEqual(create(packet, secret), received);
    }

    /**
     * Verifies the single Message-Authenticator attribute of a decoded packet.
     *
     * @param packet decoded packet
     * @param secret shared secret
     * @return {@code true} when exactly one correctly sized attribute verifies
     */
    public static boolean verify(final RadiusPacket packet, final byte[] secret) {
        final List<RadiusAttribute> authenticators = packet.attributes().stream()
                .filter(attribute -> attribute.type() == TYPE).toList();
        if (authenticators.size() != Normal._1
                || authenticators.getFirst().value().length != RadiusPacket.AUTHENTICATOR_BYTES) {
            return false;
        }
        final List<RadiusAttribute> zeroed = packet.attributes().stream()
                .map(
                        attribute -> attribute.type() == TYPE
                                ? RadiusAttribute.standard(TYPE, new byte[RadiusPacket.AUTHENTICATOR_BYTES])
                                : attribute)
                .toList();
        final RadiusPacket source = new RadiusPacket(packet.code(), packet.identifier(), packet.authenticator(),
                zeroed);
        return verify(RadiusPacketCodec.encode(source), secret, authenticators.getFirst().value());
    }

}

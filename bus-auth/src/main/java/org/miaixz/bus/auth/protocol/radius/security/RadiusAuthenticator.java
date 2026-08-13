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
package org.miaixz.bus.auth.protocol.radius.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import org.miaixz.bus.auth.protocol.radius.packet.RadiusPacket;
import org.miaixz.bus.auth.protocol.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Generates and verifies RADIUS request and response authenticators with constant-time comparison.
 *
 * @author Kimi Liu
 */
public final class RadiusAuthenticator {

    /**
     * Prevents construction.
     */
    private RadiusAuthenticator() {
        // No initialization required.
    }

    /**
     * Creates one Access-Request authenticator.
     *
     * @param random secure random port
     * @return sixteen random bytes
     * @throws ValidateException if the secure random source is {@code null}
     */
    public static byte[] access(final SecureRandom random) {
        final byte[] result = new byte[RadiusPacket.AUTHENTICATOR_BYTES];
        Assert.notNull(random, () -> new ValidateException("RADIUS random source must not be null")).nextBytes(result);
        return result.clone();
    }

    /**
     * Computes an Accounting-Request authenticator over a packet containing a zero authenticator.
     *
     * @param packet packet
     * @param secret shared secret bytes
     * @return authenticator
     * @throws ValidateException if packet or shared secret is invalid
     */
    public static byte[] accounting(final RadiusPacket packet, final byte[] secret) {
        return digest(packet, new byte[RadiusPacket.AUTHENTICATOR_BYTES], secret);
    }

    /**
     * Computes a response authenticator.
     *
     * @param packet               response packet
     * @param requestAuthenticator original request authenticator
     * @param secret               shared secret bytes
     * @return authenticator
     * @throws ValidateException if packet, request authenticator, or shared secret is invalid
     */
    public static byte[] response(final RadiusPacket packet, final byte[] requestAuthenticator, final byte[] secret) {
        return digest(packet, requestAuthenticator, secret);
    }

    /**
     * Verifies an authenticator in constant time.
     *
     * @param expected expected bytes
     * @param actual   received bytes
     * @return whether equal
     */
    public static boolean verify(final byte[] expected, final byte[] actual) {
        return expected != null && actual != null && MessageDigest.isEqual(expected, actual);
    }

    /**
     * Computes MD5 over the packet header, supplied authenticator, attributes, and secret.
     *
     * @param packet        packet
     * @param authenticator substituted authenticator
     * @param secret        secret
     * @return digest
     * @throws ValidateException if packet, authenticator, or shared secret is invalid
     */
    static byte[] digest(final RadiusPacket packet, final byte[] authenticator, final byte[] secret) {
        final RadiusPacket source = Assert
                .notNull(packet, () -> new ValidateException("RADIUS packet must not be null"));
        final byte[] key = Arrays.copyOf(
                Assert.notNull(secret, () -> new ValidateException("RADIUS shared secret must not be null")),
                secret.length);
        try {
            Assert.isTrue(key.length > Normal._0, () -> new ValidateException("RADIUS shared secret is empty"));
            final RadiusPacket substituted = new RadiusPacket(source.code(), source.identifier(), authenticator,
                    source.attributes());
            final byte[] encoded = RadiusPacketCodec.encode(substituted);
            final byte[] material = Arrays.copyOf(encoded, encoded.length + key.length);
            System.arraycopy(key, Normal._0, material, encoded.length, key.length);
            try {
                return Builder.md5(material);
            } finally {
                Arrays.fill(material, (byte) Normal._0);
            }
        } finally {
            Arrays.fill(key, (byte) Normal._0);
        }
    }

}

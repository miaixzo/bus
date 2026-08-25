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
package org.miaixz.bus.auth.shared.jwt;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Implements the version-one deterministic String-key profile using RFC 5869 HKDF-SHA-256.
 * <p>
 * The profile fixes UTF-8 input, salt, context, and output length so a String such as {@code A} produces the same
 * 256-bit HS256 key on every JVM and cluster node. It deliberately preserves every input character and performs no
 * trimming, case conversion, Unicode normalization, randomness, or node-specific processing.
 * </p>
 * <p>
 * Derivation satisfies the HS256 key-size requirement but cannot increase the entropy of a weak caller secret.
 * Applications should still supply high-entropy values even though this compatibility-oriented profile accepts every
 * non-empty String.
 * </p>
 *
 * @author Kimi Liu
 */
public final class HkdfJwtKeyDeriver implements JwtKeyDeriver {

    /**
     * Stable public name of this derivation contract.
     */
    public static final String VERSION = "HKDF_SHA256_V1";
    /**
     * Shared stateless profile instance.
     */
    public static final HkdfJwtKeyDeriver INSTANCE = new HkdfJwtKeyDeriver();
    /**
     * Versioned public HKDF salt used for JWT String key separation.
     */
    private static final byte[] SALT = "bus-auth:jwt:key:v1".getBytes(Charset.UTF_8);
    /**
     * HS256-specific HKDF context.
     */
    private static final byte[] HS256_INFO = JwaAlgorithm.HS256.name().getBytes(Charset.UTF_8);
    /**
     * First HKDF expand-block counter.
     */
    private static final byte[] FIRST_BLOCK = { 1 };

    /**
     * Creates the stateless version-one derivation profile.
     */
    public HkdfJwtKeyDeriver() {
        // No mutable state.
    }

    /**
     * Derives one 256-bit HS256 key from exact UTF-8 String material.
     *
     * @param algorithm exact trusted JWT algorithm; this profile accepts HS256 only
     * @param secret    non-empty String key material
     * @return deterministic HmacSHA256 key
     * @throws IllegalArgumentException if an argument is {@code null} or the String is empty
     * @throws ValidateException        if an algorithm other than HS256 is requested
     */
    @Override
    public SecretKey derive(final JwaAlgorithm algorithm, final String secret) {
        final JwaAlgorithm selected = Assert.notNull(algorithm, "JWT key derivation algorithm must not be null");
        if (!JwaAlgorithm.HS256.equals(selected)) {
            throw new ValidateException("JWT String key derivation profile supports HS256 only");
        }
        final byte[] material = Assert.notEmpty(secret, "JWT String secret must not be empty").getBytes(Charset.UTF_8);
        final byte[] pseudorandomKey = hmac(SALT, material);
        byte[] derived = null;
        try {
            final byte[] context = Arrays.copyOf(HS256_INFO, HS256_INFO.length + FIRST_BLOCK.length);
            System.arraycopy(FIRST_BLOCK, 0, context, HS256_INFO.length, FIRST_BLOCK.length);
            derived = hmac(pseudorandomKey, context);
            return new SecretKeySpec(derived, Algorithm.HMACSHA256.getValue());
        } finally {
            Arrays.fill(material, (byte) 0);
            Arrays.fill(pseudorandomKey, (byte) 0);
            if (derived != null) {
                Arrays.fill(derived, (byte) 0);
            }
        }
    }

    /**
     * Computes one dependency-free HMAC-SHA-256 step through the mandatory JCA algorithm.
     *
     * @param key  HMAC key bytes
     * @param data input bytes
     * @return newly allocated MAC result
     */
    private static byte[] hmac(final byte[] key, final byte[] data) {
        try {
            final Mac mac = Mac.getInstance(Algorithm.HMACSHA256.getValue());
            mac.init(new SecretKeySpec(key, Algorithm.HMACSHA256.getValue()));
            return mac.doFinal(data);
        } catch (GeneralSecurityException cause) {
            throw new CryptoException("JWT HKDF-SHA-256 key derivation is unavailable", cause);
        }
    }

    /**
     * Returns the frozen derivation profile name.
     *
     * @return {@value #VERSION}
     */
    @Override
    public String version() {
        return VERSION;
    }

}

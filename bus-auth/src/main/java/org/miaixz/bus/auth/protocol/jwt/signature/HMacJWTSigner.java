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
package org.miaixz.bus.auth.protocol.jwt.signature;

import java.security.Key;

import javax.crypto.SecretKey;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.crypto.center.HMac;

/**
 * HS256 JWT signer backed by the Bus cryptographic HMAC implementation.
 * <p>
 * The algorithm and key are trusted construction inputs. Compact JWS processing uses UTF-8 and unpadded Base64url
 * exclusively.
 * </p>
 *
 * @author Kimi Liu
 */
public final class HMacJWTSigner implements JWTSigner {

    /**
     * Minimum HS256 key length in bytes, as required by the JWA profile.
     */
    private static final int MINIMUM_KEY_BYTES = 32;

    /**
     * Bus cryptographic primitive bound to HmacSHA256.
     */
    private final HMac hMac;

    /**
     * Creates an HS256 signer from raw secret material.
     *
     * @param algorithm exact HmacSHA256 JCA algorithm name
     * @param key       non-null secret key material
     */
    public HMacJWTSigner(final String algorithm, final byte[] key) {
        requireAlgorithm(algorithm);
        Assert.notNull(key, "Signer key must be not null!");
        if (key.length < MINIMUM_KEY_BYTES) {
            throw new IllegalArgumentException("HS256 requires at least 256 bits of key material");
        }
        this.hMac = new HMac(Algorithm.HMACSHA256, key.clone());
    }

    /**
     * Creates an HS256 signer from a symmetric JCA key.
     *
     * @param algorithm exact HmacSHA256 JCA algorithm name
     * @param key       non-null symmetric key
     */
    public HMacJWTSigner(final String algorithm, final Key key) {
        requireAlgorithm(algorithm);
        Assert.notNull(key, "Signer key must be not null!");
        if (!(key instanceof SecretKey) || key.getEncoded() == null || key.getEncoded().length < MINIMUM_KEY_BYTES) {
            throw new IllegalArgumentException("HS256 requires an encodable symmetric key of at least 256 bits");
        }
        this.hMac = new HMac(Algorithm.HMACSHA256, key);
    }

    /**
     * Enforces the only trusted HMAC algorithm allowed by this JWT profile.
     *
     * @param algorithm supplied JCA algorithm name
     */
    private static void requireAlgorithm(final String algorithm) {
        if (!Algorithm.HMACSHA256.getValue().equals(algorithm)) {
            throw new IllegalArgumentException("Only HS256 is supported");
        }
    }

    /**
     * Computes an HS256 MAC over the exact compact signing input.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @return newly allocated raw MAC bytes
     */
    @Override
    public byte[] sign(final byte[] signingInput) {
        return hMac.digest(JwsSupport.signingInput(signingInput));
    }

    /**
     * Verifies an HS256 MAC by comparing decoded MAC bytes in constant time.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @param signature    raw untrusted MAC bytes; never retained
     * @return {@code true} only when the MAC is valid
     */
    @Override
    public boolean verify(final byte[] signingInput, final byte[] signature) {
        try {
            final byte[] expected = sign(signingInput);
            return JwsSupport.constantTimeEquals(expected, signature);
        } catch (final RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Returns the fixed JOSE algorithm.
     *
     * @return {@link TrustedAlgorithm#HS256}
     */
    @Override
    public TrustedAlgorithm algorithm() {
        return TrustedAlgorithm.HS256;
    }

}

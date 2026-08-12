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
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.StringKit;
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
public class HMacJWTSigner implements JWTSigner {

    /**
     * Bus cryptographic primitive bound to HmacSHA256.
     */
    private final HMac hMac;

    /**
     * Compatibility charset fixed to UTF-8.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * Creates an HS256 signer from raw secret material.
     *
     * @param algorithm exact HmacSHA256 JCA algorithm name
     * @param key       non-null secret key material
     */
    public HMacJWTSigner(final String algorithm, final byte[] key) {
        requireAlgorithm(algorithm);
        Assert.notNull(key, "Signer key must be not null!");
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
     * Preserves the compatibility mutator while enforcing the JWS UTF-8 requirement.
     *
     * @param charset required UTF-8 charset
     * @return this signer
     */
    public HMacJWTSigner setCharset(final java.nio.charset.Charset charset) {
        if (!Charset.UTF_8.equals(charset)) {
            throw new IllegalArgumentException("JWT signing requires UTF-8");
        }
        this.charset = charset;
        return this;
    }

    /**
     * Computes an HS256 MAC over the exact compact signing input.
     *
     * @param headerBase64  unpadded Base64url header segment
     * @param payloadBase64 unpadded Base64url payload segment
     * @return unpadded Base64url MAC
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        Assert.notNull(headerBase64, "JWT header segment must be not null!");
        Assert.notNull(payloadBase64, "JWT payload segment must be not null!");
        final String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        return Base64.encodeUrlSafe(hMac.digest(data, charset));
    }

    /**
     * Verifies an HS256 MAC by comparing decoded MAC bytes in constant time.
     *
     * @param headerBase64  unpadded Base64url header segment
     * @param payloadBase64 unpadded Base64url payload segment
     * @param signBase64    unpadded Base64url MAC
     * @return {@code true} only when the MAC is valid
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        if (headerBase64 == null || payloadBase64 == null || signBase64 == null || signBase64.isEmpty()) {
            return false;
        }
        try {
            final String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
            final byte[] expected = hMac.digest(data, charset);
            final byte[] presented = Base64.decode(signBase64);
            return signBase64.equals(Base64.encodeUrlSafe(presented)) && hMac.verify(expected, presented);
        } catch (final RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Returns the fixed JCA algorithm name.
     *
     * @return HmacSHA256
     */
    @Override
    public String getAlgorithm() {
        return this.hMac.getAlgorithm();
    }

}

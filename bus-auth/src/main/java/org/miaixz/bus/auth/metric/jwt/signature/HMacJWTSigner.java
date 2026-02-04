/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.HMac;

/**
 * HMAC algorithm JWT signer.
 * <p>
 * Implements the {@link JWTSigner} interface, using HMAC algorithms (e.g., HS256, HS384, HS512) for JWT signing and
 * verification. Supports custom encoding, with UTF-8 as the default.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HMacJWTSigner implements JWTSigner {

    /**
     * The HMAC algorithm instance, used for performing signing and verification.
     */
    private final HMac hMac;
    /**
     * The character encoding, default is UTF-8.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * Constructor, initializes the HMAC signer.
     *
     * @param algorithm the HMAC algorithm (e.g., HS256, HS384, HS512)
     * @param key       the secret key (byte array)
     * @throws IllegalArgumentException if the algorithm or key is invalid
     */
    public HMacJWTSigner(final String algorithm, final byte[] key) {
        // Initialize the HMAC algorithm instance
        this.hMac = new HMac(algorithm, key);
    }

    /**
     * Constructor, initializes the HMAC signer.
     *
     * @param algorithm the HMAC algorithm (e.g., HS256, HS384, HS512)
     * @param key       the secret key (Java security Key object)
     * @throws IllegalArgumentException if the algorithm or key is invalid
     */
    public HMacJWTSigner(final String algorithm, final Key key) {
        // Initialize the HMAC algorithm instance
        this.hMac = new HMac(algorithm, key);
    }

    /**
     * Sets the character encoding.
     *
     * @param charset the character encoding (e.g., UTF-8)
     * @return this object, supporting method chaining
     * @throws IllegalArgumentException if the encoding is invalid
     */
    public HMacJWTSigner setCharset(final java.nio.charset.Charset charset) {
        // Update the character encoding
        this.charset = charset;
        return this;
    }

    /**
     * Performs HMAC signing on the JWT header and payload.
     * <p>
     * Concatenates the Base64 encoded header and payload in "header.payload" format, generates a Base64 signature using
     * the HMAC algorithm.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @return the Base64 encoded signature
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        // Concatenate header and payload in "header.payload" format
        String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        // Generate Base64 signature using HMAC algorithm
        return hMac.digestBase64(data, charset, true);
    }

    /**
     * Verifies the JWT signature.
     * <p>
     * Regenerates the signature for the header and payload using the HMAC algorithm and compares it with the provided
     * signature.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @param signBase64    Base64 encoded signature to be verified
     * @return true if the verification passes, false otherwise
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        // Generate the expected signature
        final String sign = sign(headerBase64, payloadBase64);
        // Compare the expected signature with the provided signature
        return hMac.verify(ByteKit.toBytes(sign, charset), ByteKit.toBytes(signBase64, charset));
    }

    /**
     * Retrieves the name of the signing algorithm.
     *
     * @return the algorithm name (e.g., HS256, HS384, HS512)
     */
    @Override
    public String getAlgorithm() {
        // Returns the HMAC algorithm name
        return this.hMac.getAlgorithm();
    }

}

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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.Sign;

/**
 * RS256 and PS256 JWT signer backed by the Bus cryptographic signature implementation.
 * <p>
 * A private RSA key enables signing and a public RSA key enables verification. The selected JCA algorithm is a trusted
 * construction input and is limited to the two RSA algorithms allowed by the JWT profile.
 * </p>
 *
 * @author Kimi Liu
 * @see JWTSigner
 */
public class RSAJWTSigner implements JWTSigner {

    /**
     * Bus cryptographic signature implementation.
     */
    private final Sign sign;

    /**
     * Compatibility charset fixed to UTF-8.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * Creates an RS256 or PS256 signer with one RSA key.
     *
     * @param algorithm exact JCA algorithm name from {@link Algorithm}
     * @param key       RSA public verification key or RSA private signing key
     */
    public RSAJWTSigner(final String algorithm, final Key key) {
        this(algorithm, key, false);
    }

    /**
     * Creates an asymmetric signer for an approved subclass algorithm.
     *
     * @param algorithm         exact JCA algorithm name
     * @param key               asymmetric public or private key
     * @param subclassAlgorithm whether the subclass owns algorithm validation
     */
    protected RSAJWTSigner(final String algorithm, final Key key, final boolean subclassAlgorithm) {
        requireAlgorithm(algorithm, subclassAlgorithm);
        Assert.notNull(key, "Signer key must be not null!");
        if (!subclassAlgorithm && !(key instanceof RSAKey)
                || !(key instanceof PublicKey || key instanceof PrivateKey)) {
            throw new IllegalArgumentException("An RSA public or private key is required");
        }
        final PublicKey publicKey = key instanceof PublicKey ? (PublicKey) key : null;
        final PrivateKey privateKey = key instanceof PrivateKey ? (PrivateKey) key : null;
        this.sign = new Sign(algorithm, new KeyPair(publicKey, privateKey));
    }

    /**
     * Creates an RS256 or PS256 signer with an RSA key pair.
     *
     * @param algorithm exact JCA algorithm name from {@link Algorithm}
     * @param keyPair   RSA key pair containing at least one usable key
     */
    public RSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        this(algorithm, keyPair, false);
    }

    /**
     * Creates an asymmetric signer for an approved subclass algorithm and key pair.
     *
     * @param algorithm         exact JCA algorithm name
     * @param keyPair           asymmetric key pair
     * @param subclassAlgorithm whether the subclass owns algorithm validation
     */
    protected RSAJWTSigner(final String algorithm, final KeyPair keyPair, final boolean subclassAlgorithm) {
        requireAlgorithm(algorithm, subclassAlgorithm);
        requireKeyPair(keyPair, subclassAlgorithm);
        this.sign = new Sign(algorithm, keyPair);
    }

    /**
     * Validates the fixed RSA algorithm profile unless a subclass owns validation.
     *
     * @param algorithm         supplied JCA algorithm name
     * @param subclassAlgorithm whether validation belongs to a subclass
     */
    private static void requireAlgorithm(final String algorithm, final boolean subclassAlgorithm) {
        if (!subclassAlgorithm && !Algorithm.SHA256WITHRSA.getValue().equals(algorithm)
                && !Algorithm.SHA256WITHRSA_PSS.getValue().equals(algorithm)) {
            throw new IllegalArgumentException("Only RS256 and PS256 are supported");
        }
    }

    /**
     * Validates an asymmetric key pair and the RSA key family when required.
     *
     * @param keyPair           supplied key pair
     * @param subclassAlgorithm whether key-family validation belongs to a subclass
     */
    private static void requireKeyPair(final KeyPair keyPair, final boolean subclassAlgorithm) {
        Assert.notNull(keyPair, "Signer key pair must be not null!");
        final PublicKey publicKey = keyPair.getPublic();
        final PrivateKey privateKey = keyPair.getPrivate();
        if (publicKey == null && privateKey == null) {
            throw new IllegalArgumentException("Signer key pair must contain a key");
        }
        if (!subclassAlgorithm && (publicKey != null && !(publicKey instanceof RSAKey)
                || privateKey != null && !(privateKey instanceof RSAKey))) {
            throw new IllegalArgumentException("An RSA key pair is required");
        }
    }

    /**
     * Preserves the compatibility mutator while enforcing the JWS UTF-8 requirement.
     *
     * @param charset required UTF-8 charset
     * @return this signer
     */
    public RSAJWTSigner setCharset(final java.nio.charset.Charset charset) {
        if (!Charset.UTF_8.equals(charset)) {
            throw new IllegalArgumentException("JWT signing requires UTF-8");
        }
        this.charset = charset;
        return this;
    }

    /**
     * Signs the exact compact signing input.
     *
     * @param headerBase64  unpadded Base64url header segment
     * @param payloadBase64 unpadded Base64url payload segment
     * @return unpadded Base64url signature
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        Assert.notNull(headerBase64, "JWT header segment must be not null!");
        Assert.notNull(payloadBase64, "JWT payload segment must be not null!");
        final String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        return Base64.encodeUrlSafe(sign(ByteKit.toBytes(data, charset)));
    }

    /**
     * Signs bytes with the configured private key.
     *
     * @param data the data to be signed
     * @return the signed byte array
     */
    protected byte[] sign(final byte[] data) {
        return sign.sign(data);
    }

    /**
     * Verifies a compact signature with the configured public key.
     *
     * @param headerBase64  unpadded Base64url header segment
     * @param payloadBase64 unpadded Base64url payload segment
     * @param signBase64    unpadded Base64url signature
     * @return {@code true} only when the signature is valid
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        if (headerBase64 == null || payloadBase64 == null || signBase64 == null || signBase64.isEmpty()) {
            return false;
        }
        try {
            final byte[] data = ByteKit.toBytes(StringKit.format("{}.{}", headerBase64, payloadBase64), charset);
            final byte[] signed = Base64.decode(signBase64);
            return signBase64.equals(Base64.encodeUrlSafe(signed)) && verify(data, signed);
        } catch (final RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Verifies signature bytes with the configured public key.
     *
     * @param data   the data to be verified
     * @param signed the signed byte array
     * @return true if the verification passes, false otherwise
     */
    protected boolean verify(final byte[] data, final byte[] signed) {
        return sign.verify(data, signed);
    }

    /**
     * Returns the immutable JCA signature algorithm.
     *
     * @return configured JCA algorithm name
     */
    @Override
    public String getAlgorithm() {
        return this.sign.getSignature().getAlgorithm();
    }

}

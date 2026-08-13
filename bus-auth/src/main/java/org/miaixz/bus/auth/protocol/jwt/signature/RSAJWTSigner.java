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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
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
     * Trusted JOSE algorithm bound at construction.
     */
    private final TrustedAlgorithm algorithm;

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
        this.algorithm = resolve(algorithm, subclassAlgorithm);
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
        this.algorithm = resolve(algorithm, subclassAlgorithm);
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
     * Resolves the trusted JOSE identity after constructor validation.
     *
     * @param algorithm         validated JCA algorithm
     * @param subclassAlgorithm whether the ES256 subclass owns validation
     * @return bound JOSE algorithm
     */
    private static TrustedAlgorithm resolve(final String algorithm, final boolean subclassAlgorithm) {
        if (subclassAlgorithm) {
            return TrustedAlgorithm.ES256;
        }
        return Algorithm.SHA256WITHRSA_PSS.getValue().equals(algorithm) ? TrustedAlgorithm.PS256
                : TrustedAlgorithm.RS256;
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
     * Signs the exact compact signing input.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @return newly allocated raw signature bytes
     */
    @Override
    public byte[] sign(final byte[] signingInput) {
        return signBytes(JwsSupport.signingInput(signingInput));
    }

    /**
     * Signs bytes with the configured private key.
     *
     * @param data the data to be signed
     * @return the signed byte array
     */
    protected byte[] signBytes(final byte[] data) {
        return sign.sign(data);
    }

    /**
     * Verifies a compact signature with the configured public key.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @param signature    raw untrusted signature bytes; never retained
     * @return {@code true} only when the signature is valid
     */
    @Override
    public boolean verify(final byte[] signingInput, final byte[] signature) {
        try {
            return signature != null && verifyBytes(JwsSupport.signingInput(signingInput), signature);
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
    protected boolean verifyBytes(final byte[] data, final byte[] signed) {
        return sign.verify(data, signed);
    }

    /**
     * Returns the immutable JOSE algorithm.
     *
     * @return configured trusted algorithm
     */
    @Override
    public TrustedAlgorithm algorithm() {
        return algorithm;
    }

}

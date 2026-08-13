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
import java.security.interfaces.EdECKey;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.JWTException;
import org.miaixz.bus.crypto.center.Sign;

/**
 * EdDSA JWT signer fixed to the JDK 21 Ed25519 parameter set and backed by the Bus signature implementation.
 * <p>
 * A private Ed25519 key enables signing and a public Ed25519 key enables verification. The algorithm is fixed by this
 * type and cannot be selected by token input.
 * </p>
 *
 * @author Kimi Liu
 */
public final class EdDSAJWTSigner implements JWTSigner {

    /**
     * Required byte length of an Ed25519 signature.
     */
    private static final int SIGNATURE_LENGTH = 64;

    /**
     * Bus cryptographic signature implementation.
     */
    private final Sign sign;

    /**
     * Creates an Ed25519 signer from one public or private key.
     *
     * @param key Ed25519 public verification key or private signing key
     */
    public EdDSAJWTSigner(final Key key) {
        final Key validated = requireKey(key);
        final PublicKey publicKey = validated instanceof PublicKey ? (PublicKey) validated : null;
        final PrivateKey privateKey = validated instanceof PrivateKey ? (PrivateKey) validated : null;
        this.sign = new Sign(Algorithm.ED25519, new KeyPair(publicKey, privateKey));
    }

    /**
     * Creates an Ed25519 signer from a key pair containing at least one usable key.
     *
     * @param keyPair Ed25519 key pair
     */
    public EdDSAJWTSigner(final KeyPair keyPair) {
        this.sign = new Sign(Algorithm.ED25519, requireKeyPair(keyPair));
    }

    /**
     * Validates one Ed25519 public or private key.
     *
     * @param key supplied key
     * @return validated key
     */
    private static Key requireKey(final Key key) {
        if (!(key instanceof EdECKey) || !(key instanceof PublicKey || key instanceof PrivateKey)
                || !Algorithm.ED25519.getValue().equals(((EdECKey) key).getParams().getName())) {
            throw new IllegalArgumentException("An Ed25519 public or private key is required");
        }
        return key;
    }

    /**
     * Validates an Ed25519 key pair containing at least one key.
     *
     * @param keyPair supplied key pair
     * @return validated key pair
     */
    private static KeyPair requireKeyPair(final KeyPair keyPair) {
        if (keyPair == null || keyPair.getPublic() == null && keyPair.getPrivate() == null) {
            throw new IllegalArgumentException("Signer key pair must contain a key");
        }
        if (keyPair.getPublic() != null) {
            requireKey(keyPair.getPublic());
        }
        if (keyPair.getPrivate() != null) {
            requireKey(keyPair.getPrivate());
        }
        return keyPair;
    }

    /**
     * Signs the exact compact signing input with an Ed25519 private key.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @return newly allocated raw Ed25519 signature bytes
     */
    @Override
    public byte[] sign(final byte[] signingInput) {
        final byte[] signature = sign.sign(JwsSupport.signingInput(signingInput));
        if (signature.length != SIGNATURE_LENGTH) {
            throw new JWTException(ErrorCode._100532);
        }
        return signature;
    }

    /**
     * Verifies an exact compact signing input with an Ed25519 public key.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @param signature    raw untrusted Ed25519 signature bytes; never retained
     * @return {@code true} only when the signature is valid
     */
    @Override
    public boolean verify(final byte[] signingInput, final byte[] signature) {
        if (signature == null || signature.length != SIGNATURE_LENGTH) {
            return false;
        }
        try {
            return sign.verify(JwsSupport.signingInput(signingInput), signature);
        } catch (final RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Returns the fixed JOSE algorithm.
     *
     * @return {@link TrustedAlgorithm#EDDSA}
     */
    @Override
    public TrustedAlgorithm algorithm() {
        return TrustedAlgorithm.EDDSA;
    }

}

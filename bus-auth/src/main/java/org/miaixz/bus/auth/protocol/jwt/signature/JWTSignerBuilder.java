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
import java.util.Map;

import javax.crypto.SecretKey;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * Creates signers only for the five explicitly permitted signed JOSE algorithms.
 *
 * @author Kimi Liu
 */
public final class JWTSignerBuilder {

    /**
     * Exact allowed JOSE-to-JCA mapping.
     */
    private static final Map<String, String> ALGORITHMS = Map.of(
            "HS256",
            Algorithm.HMACSHA256.getValue(),
            "RS256",
            Algorithm.SHA256WITHRSA.getValue(),
            "PS256",
            Algorithm.SHA256WITHRSA_PSS.getValue(),
            "ES256",
            Algorithm.SHA256WITHECDSA.getValue(),
            "EdDSA",
            Algorithm.ED25519.getValue());

    /**
     * Prevents construction of the stateless signer factory.
     */
    private JWTSignerBuilder() {
        // No initialization required.
    }

    /**
     * @param key symmetric secret bytes; @return HS256 signer
     */
    public static JWTSigner hs256(final byte[] key) {
        return createSigner("HS256", key);
    }

    /**
     * @param key RSA key; @return RS256 signer
     */
    public static JWTSigner rs256(final Key key) {
        return createSigner("RS256", key);
    }

    /**
     * @param key RSA key; @return PS256 signer
     */
    public static JWTSigner ps256(final Key key) {
        return createSigner("PS256", key);
    }

    /**
     * @param key P-256 EC key; @return ES256 signer
     */
    public static JWTSigner es256(final Key key) {
        return createSigner("ES256", key);
    }

    /**
     * @param key Ed25519 key; @return EdDSA signer
     */
    public static JWTSigner eddsa(final Key key) {
        return createSigner("EdDSA", key);
    }

    /**
     * Creates a signer for a caller-selected signed algorithm and raw symmetric key.
     */
    public static JWTSigner createSigner(final TrustedAlgorithm algorithm, final byte[] key) {
        Assert.notNull(algorithm, "Signer algorithm must be not null!");
        return createSigner(algorithm.identifier(), key);
    }

    /**
     * Creates a signer for a caller-selected signed algorithm and asymmetric key.
     */
    public static JWTSigner createSigner(final TrustedAlgorithm algorithm, final Key key) {
        Assert.notNull(algorithm, "Signer algorithm must be not null!");
        return createSigner(algorithm.identifier(), key);
    }

    /**
     * Creates the sole byte-key algorithm signer.
     */
    public static JWTSigner createSigner(final String algorithmId, final byte[] key) {
        Assert.notNull(key, "Signer key must be not null!");
        if (!"HS256".equals(algorithmId)) {
            return unsupported();
        }
        return new HMacJWTSigner(Algorithm.HMACSHA256.getValue(), key);
    }

    /**
     * Creates one allowed asymmetric signer from a key pair.
     */
    public static JWTSigner createSigner(final String algorithmId, final KeyPair keyPair) {
        Assert.notNull(keyPair, "Signer key pair must be not null!");
        return switch (algorithmId == null ? "" : algorithmId) {
            case "RS256" -> new RSAJWTSigner(Algorithm.SHA256WITHRSA.getValue(), keyPair);
            case "PS256" -> new RSAJWTSigner(Algorithm.SHA256WITHRSA_PSS.getValue(), keyPair);
            case "ES256" -> new ECDSAJWTSigner(Algorithm.SHA256WITHECDSA.getValue(), keyPair);
            case "EdDSA" -> new EdDSAJWTSigner(keyPair);
            default -> unsupported();
        };
    }

    /**
     * Creates one allowed signer from a single key.
     */
    public static JWTSigner createSigner(final String algorithmId, final Key key) {
        Assert.notNull(key, "Signer key must be not null!");
        return switch (algorithmId == null ? "" : algorithmId) {
            case "HS256" -> key instanceof SecretKey ? new HMacJWTSigner(Algorithm.HMACSHA256.getValue(), key)
                    : unsupported();
            case "RS256" -> new RSAJWTSigner(Algorithm.SHA256WITHRSA.getValue(), key);
            case "PS256" -> new RSAJWTSigner(Algorithm.SHA256WITHRSA_PSS.getValue(), key);
            case "ES256" -> new ECDSAJWTSigner(Algorithm.SHA256WITHECDSA.getValue(), key);
            case "EdDSA" -> new EdDSAJWTSigner(key);
            default -> unsupported();
        };
    }

    /**
     * @param identifier exact JOSE identifier; @return mapped JCA algorithm or {@code null}
     */
    public static String getAlgorithm(final String identifier) {
        return identifier == null ? null : ALGORITHMS.get(identifier);
    }

    /**
     * @param algorithm exact JCA algorithm; @return mapped JOSE identifier or {@code null}
     */
    public static String getId(final String algorithm) {
        if (algorithm == null) {
            return null;
        }
        return ALGORITHMS.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(algorithm))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /**
     * Throws the stable unsupported-algorithm failure.
     */
    private static <T> T unsupported() {
        throw new JWTException(ErrorCode._100533, "Unsupported JWT signing algorithm");
    }

}

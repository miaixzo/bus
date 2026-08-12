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
import java.util.Map;

import javax.crypto.SecretKey;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * Builds JWT signers from a fixed JOSE algorithm allowlist and trusted key material.
 * <p>
 * Only HS256, RS256, PS256, ES256, and EdDSA are enabled. All legacy factory signatures remain present, but factories
 * for disabled algorithms fail closed. Algorithm identifiers are exact protocol values and never come from token input
 * when selecting a key or signer.
 * </p>
 *
 * @author Kimi Liu
 * @see JWTSigner
 * @see HMacJWTSigner
 * @see RSAJWTSigner
 * @see ECDSAJWTSigner
 * @see EdDSAJWTSigner
 */
public class JWTSignerBuilder {

    /**
     * JOSE identifier for HS256.
     */
    private static final String HS256 = "HS256";

    /**
     * JOSE identifier for RS256.
     */
    private static final String RS256 = "RS256";

    /**
     * JOSE identifier for PS256.
     */
    private static final String PS256 = "PS256";

    /**
     * JOSE identifier for ES256.
     */
    private static final String ES256 = "ES256";

    /**
     * JOSE identifier for EdDSA with Ed25519.
     */
    private static final String EDDSA = "EdDSA";

    /**
     * Exact allowed JOSE-to-JCA mapping.
     */
    private static final Map<String, String> ALGORITHMS = Map.of(
            HS256,
            Algorithm.HMACSHA256.getValue(),
            RS256,
            Algorithm.SHA256WITHRSA.getValue(),
            PS256,
            Algorithm.SHA256WITHRSA_PSS.getValue(),
            ES256,
            Algorithm.SHA256WITHECDSA.getValue(),
            EDDSA,
            Algorithm.ED25519.getValue());

    /**
     * Constructs a builder compatibility instance.
     */
    public JWTSignerBuilder() {
        // No initialization required.
    }

    /**
     * Rejects construction of an unsigned JWT signer.
     *
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner none() {
        return unsupported();
    }

    /**
     * Creates an HS256 signer.
     *
     * @param key symmetric secret bytes
     * @return HS256 signer
     */
    public static JWTSigner hs256(final byte[] key) {
        return createSigner(HS256, key);
    }

    /**
     * Rejects the disabled HS384 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner hs384(final byte[] key) {
        return unsupported();
    }

    /**
     * Rejects the disabled HS512 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner hs512(final byte[] key) {
        return unsupported();
    }

    /**
     * Creates an RS256 signer.
     *
     * @param key RSA public or private key
     * @return RS256 signer
     */
    public static JWTSigner rs256(final Key key) {
        return createSigner(RS256, key);
    }

    /**
     * Creates a PS256 signer.
     *
     * @param key RSA public or private key
     * @return PS256 signer
     */
    public static JWTSigner ps256(final Key key) {
        return createSigner(PS256, key);
    }

    /**
     * Rejects the disabled RS384 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner rs384(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled RS512 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner rs512(final Key key) {
        return unsupported();
    }

    /**
     * Creates an ES256 signer.
     *
     * @param key P-256 public or private key
     * @return ES256 signer
     */
    public static JWTSigner es256(final Key key) {
        return createSigner(ES256, key);
    }

    /**
     * Creates an EdDSA signer fixed to Ed25519.
     *
     * @param key Ed25519 public or private key
     * @return EdDSA signer
     */
    public static JWTSigner eddsa(final Key key) {
        return createSigner(EDDSA, key);
    }

    /**
     * Rejects the disabled ES384 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner es384(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled ES512 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner es512(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled HMAC-MD5 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner hmd5(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled HMAC-SHA1 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner hsha1(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled SM4-CMAC algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner sm4cmac(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled RSA-MD2 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner rmd2(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled RSA-MD5 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner rmd5(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled RSA-SHA1 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner rsha1(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled raw DSA algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner dnone(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled DSA-SHA1 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner dsha1(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled raw ECDSA algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner enone(final Key key) {
        return unsupported();
    }

    /**
     * Rejects the disabled ECDSA-SHA1 algorithm.
     *
     * @param key ignored legacy key
     * @return no signer because the algorithm is disabled
     */
    public static JWTSigner esha1(final Key key) {
        return unsupported();
    }

    /**
     * Creates a signer for the only allowed byte-key algorithm.
     *
     * @param algorithmId exact trusted JOSE algorithm identifier
     * @param key         symmetric secret bytes
     * @return HS256 signer
     */
    public static JWTSigner createSigner(final String algorithmId, final byte[] key) {
        Assert.notNull(key, "Signer key must be not null!");
        if (!HS256.equals(algorithmId)) {
            return unsupported();
        }
        return new HMacJWTSigner(Algorithm.HMACSHA256.getValue(), key);
    }

    /**
     * Creates an allowed asymmetric signer from a trusted key pair.
     *
     * @param algorithmId exact trusted JOSE algorithm identifier
     * @param keyPair     asymmetric key pair
     * @return signer bound to the exact algorithm
     */
    public static JWTSigner createSigner(final String algorithmId, final KeyPair keyPair) {
        Assert.notNull(keyPair, "Signer key pair must be not null!");
        if (algorithmId == null) {
            return unsupported();
        }
        return switch (algorithmId) {
            case RS256 -> new RSAJWTSigner(Algorithm.SHA256WITHRSA.getValue(), keyPair);
            case PS256 -> new RSAJWTSigner(Algorithm.SHA256WITHRSA_PSS.getValue(), keyPair);
            case ES256 -> new ECDSAJWTSigner(Algorithm.SHA256WITHECDSA.getValue(), keyPair);
            case EDDSA -> new EdDSAJWTSigner(keyPair);
            default -> unsupported();
        };
    }

    /**
     * Creates an allowed signer from a trusted key and exact algorithm identifier.
     *
     * @param algorithmId exact trusted JOSE algorithm identifier
     * @param key         trusted key material
     * @return signer bound to the exact algorithm
     */
    public static JWTSigner createSigner(final String algorithmId, final Key key) {
        Assert.notNull(key, "Signer key must be not null!");
        if (algorithmId == null) {
            return unsupported();
        }
        return switch (algorithmId) {
            case HS256 -> key instanceof SecretKey ? new HMacJWTSigner(Algorithm.HMACSHA256.getValue(), key)
                    : unsupported();
            case RS256 -> new RSAJWTSigner(Algorithm.SHA256WITHRSA.getValue(), key);
            case PS256 -> new RSAJWTSigner(Algorithm.SHA256WITHRSA_PSS.getValue(), key);
            case ES256 -> new ECDSAJWTSigner(Algorithm.SHA256WITHECDSA.getValue(), key);
            case EDDSA -> new EdDSAJWTSigner(key);
            default -> unsupported();
        };
    }

    /**
     * Maps an exact allowed JOSE identifier to its JCA name while preserving the compatibility fallback.
     *
     * @param idOrAlgorithm JOSE identifier or JCA algorithm name
     * @return mapped JCA name, the original unknown value, or {@code null}
     */
    public static String getAlgorithm(final String idOrAlgorithm) {
        return idOrAlgorithm == null ? null : ALGORITHMS.getOrDefault(idOrAlgorithm, idOrAlgorithm);
    }

    /**
     * Maps an allowed JCA name to its exact JOSE identifier while preserving the compatibility fallback.
     *
     * @param idOrAlgorithm JCA algorithm name or JOSE identifier
     * @return mapped JOSE identifier, the original unknown value, or {@code null}
     */
    public static String getId(final String idOrAlgorithm) {
        if (idOrAlgorithm == null) {
            return null;
        }
        for (final Map.Entry<String, String> entry : ALGORITHMS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(idOrAlgorithm)) {
                return entry.getKey();
            }
        }
        return idOrAlgorithm;
    }

    /**
     * Throws the stable failure used by every disabled or unknown algorithm entry point.
     *
     * @param <T> requested result type
     * @return no value because the algorithm is rejected
     */
    private static <T> T unsupported() {
        throw new JWTException(ErrorCode._100533, "Unsupported JWT signing algorithm");
    }

}

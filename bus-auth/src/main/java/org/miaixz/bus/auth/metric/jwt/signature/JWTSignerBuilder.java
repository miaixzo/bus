/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.map.BiMap;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * JWT Signer factory.
 * <p>
 * Provides functionality to create {@link JWTSigner} instances, supporting various signing algorithms (e.g., HMAC, RSA,
 * ECDSA). It uses a bidirectional mapping between JWT signing algorithm identifiers (e.g., HS256, RS256, ES256) and
 * Java standard algorithm names (e.g., HmacSHA256, SHA256withRSA).
 * </p>
 *
 * @see JWTSigner
 * @see HMacJWTSigner
 * @see RSAJWTSigner
 * @see ECDSAJWTSigner
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTSignerBuilder {

    /**
     * Regular expression pattern to match ECDSA algorithm identifiers (e.g., ES256, ES384, ES512).
     */
    private static final Pattern ES_ALGORITHM_PATTERN = Pattern.compile("es\\d{3}", Pattern.CASE_INSENSITIVE);

    /**
     * Bidirectional map storing the correspondence between JWT signing algorithm identifiers (e.g., HS256) and Java
     * standard algorithm names (e.g., HmacSHA256).
     */
    private static final BiMap<String, String> map = new BiMap<>(new HashMap<>() {

        {
            // Initialize HMAC algorithm mapping
            put("HS256", Algorithm.HMACSHA256.getValue());
            put("HS384", Algorithm.HMACSHA384.getValue());
            put("HS512", Algorithm.HMACSHA512.getValue());
            put("HMD5", Algorithm.HMACMD5.getValue());
            put("HSHA1", Algorithm.HMACSHA1.getValue());
            put("SM4CMAC", Algorithm.SM4CMAC.getValue());
            // Initialize RSA algorithm mapping
            put("RS256", Algorithm.SHA256WITHRSA.getValue());
            put("RS384", Algorithm.SHA384WITHRSA.getValue());
            put("RS512", Algorithm.SHA512WITHRSA.getValue());
            // Initialize ECDSA algorithm mapping
            put("ES256", Algorithm.SHA256WITHECDSA.getValue());
            put("ES384", Algorithm.SHA384WITHECDSA.getValue());
            put("ES512", Algorithm.SHA512WITHECDSA.getValue());
            // Initialize RSA-PSS algorithm mapping
            put("PS256", Algorithm.SHA256WITHRSA_PSS.getValue());
            put("PS384", Algorithm.SHA384WITHRSA_PSS.getValue());
            put("PS512", Algorithm.SHA512WITHRSA_PSS.getValue());
            // Initialize other RSA algorithm mapping
            put("RMD2", Algorithm.MD2WITHRSA.getValue());
            put("RMD5", Algorithm.MD5WITHRSA.getValue());
            put("RSHA1", Algorithm.SHA1WITHRSA.getValue());
            // Initialize DSA algorithm mapping
            put("DNONE", Algorithm.NONEWITHDSA.getValue());
            put("DSHA1", Algorithm.SHA1WITHDSA.getValue());
            // Initialize other ECDSA algorithm mapping
            put("ENONE", Algorithm.NONEWITHECDSA.getValue());
            put("ESHA1", Algorithm.SHA1WITHECDSA.getValue());
        }
    });

    /**
     * Creates a signer for unsigned JWTs.
     *
     * @return a {@link JWTSigner} instance for no signature
     */
    public static JWTSigner none() {
        return NoneJWTSigner.NONE;
    }

    /**
     * Creates an HS256 (HmacSHA256) signer.
     *
     * @param key the secret key (byte array)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner hs256(final byte[] key) {
        return createSigner("HS256", key);
    }

    /**
     * Creates an HS384 (HmacSHA384) signer.
     *
     * @param key the secret key (byte array)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner hs384(final byte[] key) {
        return createSigner("HS384", key);
    }

    /**
     * Creates an HS512 (HmacSHA512) signer.
     *
     * @param key the secret key (byte array)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner hs512(final byte[] key) {
        return createSigner("HS512", key);
    }

    /**
     * Creates an RS256 (SHA256withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rs256(final Key key) {
        return createSigner("RS256", key);
    }

    /**
     * Creates an RS384 (SHA384withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rs384(final Key key) {
        return createSigner("RS384", key);
    }

    /**
     * Creates an RS512 (SHA512withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rs512(final Key key) {
        return createSigner("RS512", key);
    }

    /**
     * Creates an ES256 (SHA256withECDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link ECDSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner es256(final Key key) {
        return createSigner("ES256", key);
    }

    /**
     * Creates an ES384 (SHA384withECDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link ECDSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner es384(final Key key) {
        return createSigner("ES384", key);
    }

    /**
     * Creates an ES512 (SHA512withECDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link ECDSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner es512(final Key key) {
        return createSigner("ES512", key);
    }

    /**
     * Creates an HMD5 (HmacMD5) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner hmd5(final Key key) {
        return createSigner("HMD5", key);
    }

    /**
     * Creates an HSHA1 (HmacSHA1) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner hsha1(final Key key) {
        return createSigner("HSHA1", key);
    }

    /**
     * Creates an SM4CMAC signer.
     *
     * @param key the key (public or private key)
     * @return an {@link HMacJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner sm4cmac(final Key key) {
        return createSigner("SM4CMAC", key);
    }

    /**
     * Creates an RMD2 (MD2withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rmd2(final Key key) {
        return createSigner("RMD2", key);
    }

    /**
     * Creates an RMD5 (MD5withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rmd5(final Key key) {
        return createSigner("RMD5", key);
    }

    /**
     * Creates an RSHA1 (SHA1withRSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner rsha1(final Key key) {
        return createSigner("RSHA1", key);
    }

    /**
     * Creates a DNONE (NONEwithDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner dnone(final Key key) {
        return createSigner("DNONE", key);
    }

    /**
     * Creates a DSHA1 (SHA1withDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link RSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner dsha1(final Key key) {
        return createSigner("DSHA1", key);
    }

    /**
     * Creates an ENONE (NONEwithECDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link ECDSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner enone(final Key key) {
        return createSigner("ENONE", key);
    }

    /**
     * Creates an ESHA1 (SHA1withECDSA) signer.
     *
     * @param key the key (public or private key)
     * @return an {@link ECDSAJWTSigner} instance
     * @throws IllegalArgumentException if the key is null
     */
    public static JWTSigner esha1(final Key key) {
        return createSigner("ESHA1", key);
    }

    /**
     * Creates a signer using a byte array key.
     * <p>
     * Creates an appropriate signer instance based on the algorithm ID (only supports HMAC algorithms).
     * </p>
     *
     * @param algorithmId the algorithm ID (e.g., HS256, HS384, HS512)
     * @param key         the secret key (byte array)
     * @return a {@link JWTSigner} instance
     * @throws IllegalArgumentException if the key is null or the algorithm ID is invalid
     */
    public static JWTSigner createSigner(final String algorithmId, final byte[] key) {
        // Validate that the key is not null
        Assert.notNull(key, "Signer key must be not null!");
        // Check if it's a no-signature algorithm
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return none();
        }
        return new HMacJWTSigner(getAlgorithm(algorithmId), key);
    }

    /**
     * Creates a signer using a key pair.
     * <p>
     * Creates an appropriate signer instance based on the algorithm ID (supports RSA or ECDSA algorithms).
     * </p>
     *
     * @param algorithmId the algorithm ID (e.g., RS256, ES256)
     * @param keyPair     the key pair (containing public and private keys)
     * @return a {@link JWTSigner} instance
     * @throws IllegalArgumentException if the key pair is null or the algorithm ID is invalid
     */
    public static JWTSigner createSigner(final String algorithmId, final KeyPair keyPair) {
        // Validate that the key pair is not null
        Assert.notNull(keyPair, "Signer key pair must be not null!");
        // Check if it's a no-signature algorithm
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return none();
        }
        // Get the Java standard algorithm name
        final String algorithm = getAlgorithm(algorithmId);
        // Check if it's an ECDSA algorithm
        if (PatternKit.isMatch(ES_ALGORITHM_PATTERN, algorithmId)) {
            return new ECDSAJWTSigner(algorithm, keyPair);
        }
        return new RSAJWTSigner(algorithm, keyPair);
    }

    /**
     * Creates a signer using a public or private key.
     * <p>
     * Creates an appropriate signer instance based on the algorithm ID and key type (supports HMAC, RSA, or ECDSA
     * algorithms).
     * </p>
     *
     * @param algorithmId the algorithm ID (e.g., HS256, RS256, ES256)
     * @param key         the key (public key, private key, or symmetric key)
     * @return a {@link JWTSigner} instance
     * @throws IllegalArgumentException if the key is null or the algorithm ID is invalid
     */
    public static JWTSigner createSigner(final String algorithmId, final Key key) {
        // Validate that the key is not null
        Assert.notNull(key, "Signer key must be not null!");
        // Check if it's a no-signature algorithm
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return NoneJWTSigner.NONE;
        }
        // Get the Java standard algorithm name
        final String algorithm = getAlgorithm(algorithmId);
        // Check if the key type is PublicKey or PrivateKey
        if (key instanceof PrivateKey || key instanceof PublicKey) {
            // Check if it's an ECDSA algorithm
            if (PatternKit.isMatch(ES_ALGORITHM_PATTERN, algorithmId)) {
                return new ECDSAJWTSigner(algorithm, key);
            }
            return new RSAJWTSigner(algorithm, key);
        }
        return new HMacJWTSigner(algorithm, key);
    }

    /**
     * Retrieves the algorithm name.
     * <p>
     * If the input is a JWT algorithm identifier (e.g., HS256), it returns the corresponding Java standard algorithm
     * name (e.g., HmacSHA256); otherwise, it returns the input value itself.
     * </p>
     *
     * @param idOrAlgorithm the algorithm ID or algorithm name
     * @return the algorithm name
     */
    public static String getAlgorithm(final String idOrAlgorithm) {
        return ObjectKit.defaultIfNull(getAlgorithmById(idOrAlgorithm), idOrAlgorithm);
    }

    /**
     * Retrieves the JWT algorithm identifier.
     * <p>
     * If the input is a Java standard algorithm name (e.g., HmacSHA256), it returns the corresponding JWT algorithm
     * identifier (e.g., HS256); otherwise, it returns the input value itself.
     * </p>
     *
     * @param idOrAlgorithm the algorithm ID or algorithm name
     * @return the JWT algorithm identifier
     */
    public static String getId(final String idOrAlgorithm) {
        return ObjectKit.defaultIfNull(getIdByAlgorithm(idOrAlgorithm), idOrAlgorithm);
    }

    /**
     * Retrieves the Java standard algorithm name based on the JWT algorithm identifier.
     *
     * @param id the JWT algorithm identifier (e.g., HS256)
     * @return the Java standard algorithm name (e.g., HmacSHA256), or null if not found
     */
    private static String getAlgorithmById(final String id) {
        return map.get(id.toUpperCase());
    }

    /**
     * Retrieves the JWT algorithm identifier based on the Java standard algorithm name.
     *
     * @param algorithm the Java standard algorithm name (e.g., HmacSHA256)
     * @return the JWT algorithm identifier (e.g., HS256), or null if not found
     */
    private static String getIdByAlgorithm(final String algorithm) {
        return map.getKey(algorithm);
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.*;
import java.util.Objects;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec;
import org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * Key utility class for generating, reading, and managing cryptographic keys. This class provides a comprehensive set
 * of static methods for handling various key types, including symmetric keys, asymmetric key pairs, and certificates.
 * It supports operations such as:
 * <ul>
 * <li>Generating symmetric keys and asymmetric key pairs.</li>
 * <li>Reading keys and certificates from various formats (e.g., JKS, PKCS12, PEM).</li>
 * <li>Converting key formats and extracting key parameters.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Keeper {

    /**
     * Default key size in bits for RSA/DSA algorithms.
     * <p>
     * For RSA/DSA, the default key size is 1024 bits. Key sizes must be a multiple of 64, ranging from 512 to 1024
     * (inclusive) for these algorithms.
     */
    public static final int DEFAULT_KEY_SIZE = 1024;
    /**
     * KeyStore type for Java Key Store (JKS).
     */
    public static final String TYPE_JKS = "JKS";
    /**
     * KeyStore type for JCEKS.
     */
    public static final String TYPE_JCEKS = "jceks";
    /**
     * KeyStore type for PKCS#12.
     * <p>
     * PKCS#12 is a public-key cryptography standard that defines a file format for storing private keys, public keys,
     * and certificates. It is stored in a binary format and is also known as a PFX file.
     */
    public static final String TYPE_PKCS12 = "pkcs12";
    /**
     * Certificate type for X.509 certificates.
     */
    public static final String TYPE_X509 = "X.509";

    /**
     * Generates a {@link SecretKey} for symmetric encryption or digest algorithms. The key size will be determined by
     * the algorithm's default or a predefined default (e.g., 128 for AES).
     *
     * @param algorithm The algorithm name, e.g., "AES", "DES", "PBEWithMD5AndDES".
     * @return A new {@link SecretKey}.
     * @throws CryptoException if the algorithm is not found or key generation fails.
     */
    public static SecretKey generateKey(final String algorithm) {
        return generateKey(algorithm, -1);
    }

    /**
     * Generates a {@link SecretKey} for symmetric encryption or digest algorithms with a specified key size. If
     * {@code keySize} is less than or equal to 0, a default key size will be used (e.g., 128 for AES).
     *
     * @param algorithm The algorithm name, e.g., "AES", "DES", "PBEWithMD5AndDES".
     * @param keySize   The desired key size in bits. If &lt;= 0, the default size for the algorithm is used.
     * @return A new {@link SecretKey}.
     * @throws CryptoException if the algorithm is not found or key generation fails.
     */
    public static SecretKey generateKey(final String algorithm, final int keySize) {
        return generateKey(algorithm, keySize, null);
    }

    /**
     * Generates a {@link SecretKey} for symmetric encryption or digest algorithms with a specified key size and random
     * source. If {@code keySize} is less than or equal to 0, a default key size will be used (e.g., 128 for AES).
     *
     * @param algorithm The algorithm name, e.g., "AES", "DES", "PBEWithMD5AndDES".
     * @param keySize   The desired key size in bits. If &lt;= 0, the default size for the algorithm is used.
     * @param random    The {@link SecureRandom} instance to use for key generation. If {@code null}, a default one is
     *                  used.
     * @return A new {@link SecretKey}.
     * @throws CryptoException if the algorithm is not found or key generation fails.
     */
    public static SecretKey generateKey(String algorithm, int keySize, final SecureRandom random) {
        algorithm = getMainAlgorithm(algorithm);

        final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
        if (keySize <= 0 && Algorithm.AES.getValue().equals(algorithm)) {
            // For AES keys, unless specified, force 128 bits
            keySize = 128;
        }

        if (keySize > 0) {
            if (null == random) {
                keyGenerator.init(keySize);
            } else {
                keyGenerator.init(keySize, random);
            }
        }
        return keyGenerator.generateKey();
    }

    /**
     * Generates a {@link SecretKey} for symmetric encryption or digest algorithms using a provided key material. If the
     * key material is {@code null}, a random key will be generated.
     *
     * @param algorithm The algorithm name.
     * @param key       The key material as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link SecretKey}.
     * @throws CryptoException          if the algorithm is not found or key generation fails.
     * @throws IllegalArgumentException if the algorithm is blank.
     */
    public static SecretKey generateKey(final String algorithm, final byte[] key) {
        Assert.notBlank(algorithm, "Algorithm is blank!");
        final SecretKey secretKey;
        if (algorithm.startsWith("PBE")) {
            // PBE key
            secretKey = generatePBEKey(algorithm, (null == key) ? null : StringKit.toString(key).toCharArray());
        } else if (algorithm.startsWith("DES")) {
            // DES key
            secretKey = generateDESKey(algorithm, key);
        } else {
            // Other algorithm keys
            secretKey = (null == key) ? generateKey(algorithm) : new SecretKeySpec(key, algorithm);
        }
        return secretKey;
    }

    /**
     * Generates a {@link SecretKey} for DES algorithms (e.g., DES, DESede). If the key material is {@code null}, a
     * random key will be generated.
     *
     * @param algorithm The DES algorithm name.
     * @param key       The key material as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link SecretKey}.
     * @throws CryptoException if the algorithm is not a DES algorithm or key generation fails.
     */
    public static SecretKey generateDESKey(final String algorithm, final byte[] key) {
        if (StringKit.isBlank(algorithm) || !algorithm.startsWith("DES")) {
            throw new CryptoException("Algorithm [{}] is not a DES algorithm!", algorithm);
        }

        final SecretKey secretKey;
        if (null == key) {
            secretKey = generateKey(algorithm);
        } else {
            secretKey = generateKey(algorithm, Builder.createKeySpec(algorithm, key));
        }
        return secretKey;
    }

    /**
     * Generates a PBE (Password-Based Encryption) {@link SecretKey}. If the password is {@code null}, a random password
     * will be generated.
     *
     * @param algorithm The PBE algorithm name, e.g., "PBEWithMD5AndDES", "PBEWithSHA1AndDESede",
     *                  "PBEWithSHA1AndRC2_40".
     * @param password  The password as a character array. If {@code null}, a random password is used.
     * @return A new PBE {@link SecretKey}.
     * @throws CryptoException if the algorithm is not a PBE algorithm or key generation fails.
     */
    public static SecretKey generatePBEKey(final String algorithm, char[] password) {
        if (StringKit.isBlank(algorithm) || !algorithm.startsWith("PBE")) {
            throw new CryptoException("Algorithm [{}] is not a PBE algorithm!", algorithm);
        }

        if (null == password) {
            password = RandomKit.randomStringLower(32).toCharArray();
        }
        return generateKey(algorithm, Builder.createPBEKeySpec(password));
    }

    /**
     * Generates a {@link SecretKey} for symmetric encryption or digest algorithms using a {@link KeySpec}.
     *
     * @param algorithm The algorithm name.
     * @param keySpec   The {@link KeySpec} for the secret key.
     * @return A new {@link SecretKey}.
     * @throws CryptoException if key generation fails due to an invalid key specification.
     */
    public static SecretKey generateKey(final String algorithm, final KeySpec keySpec) {
        final SecretKeyFactory keyFactory = getSecretKeyFactory(algorithm);
        try {
            return keyFactory.generateSecret(keySpec);
        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Checks if a {@link KeyPair} is empty. A key pair is considered empty if:
     * <ul>
     * <li>The {@link KeyPair} itself is {@code null}.</li>
     * <li>Both {@link KeyPair#getPrivate()} and {@link KeyPair#getPublic()} are {@code null}.</li>
     * </ul>
     *
     * @param keyPair The {@link KeyPair} to check.
     * @return {@code true} if the key pair is empty or invalid, {@code false} otherwise.
     */
    public static boolean isEmpty(final KeyPair keyPair) {
        if (null == keyPair) {
            return false;
        }
        return null != keyPair.getPrivate() || null != keyPair.getPublic();
    }

    /**
     * Generates an RSA private key from a byte array. This method expects the key to be in PKCS#8 format, which defines
     * the syntax for private key information and encrypted private keys. For supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param key The private key material in DER-encoded PKCS#8 format.
     * @return The RSA {@link PrivateKey}.
     * @throws CryptoException if key generation fails.
     */
    public static PrivateKey generateRSAPrivateKey(final byte[] key) {
        return generatePrivateKey(Algorithm.RSA.getValue(), key);
    }

    /**
     * Generates a private key for asymmetric encryption from a byte array. This method expects the key to be in PKCS#8
     * format. For supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm, e.g., "RSA", "EC", "SM2".
     * @param key       The private key material in PKCS#8 format.
     * @return The {@link PrivateKey}, or {@code null} if the key material is {@code null}.
     * @throws CryptoException if key generation fails.
     */
    public static PrivateKey generatePrivateKey(final String algorithm, final byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePrivateKey(algorithm, new PKCS8EncodedKeySpec(key));
    }

    /**
     * Generates a private key for asymmetric encryption from a {@link KeySpec}. For supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm, e.g., "RSA", "EC", "SM2".
     * @param keySpec   The {@link KeySpec} for the private key.
     * @return The {@link PrivateKey}, or {@code null} if the key specification is {@code null}.
     * @throws CryptoException if key generation fails.
     */
    public static PrivateKey generatePrivateKey(String algorithm, final KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePrivate(keySpec);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Generates a private key from a {@link KeyStore} using an alias and password.
     *
     * @param keyStore The {@link KeyStore} containing the private key.
     * @param alias    The alias of the private key in the key store.
     * @param password The password for the private key.
     * @return The {@link PrivateKey}.
     * @throws CryptoException if key retrieval fails.
     */
    public static PrivateKey generatePrivateKey(final KeyStore keyStore, final String alias, final char[] password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Generates an RSA public key from a byte array. This method expects the key to be in X.509 certificate format. For
     * supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param key The public key material in DER-encoded X.509 format.
     * @return The RSA {@link PublicKey}.
     * @throws CryptoException if key generation fails.
     */
    public static PublicKey generateRSAPublicKey(final byte[] key) {
        return generatePublicKey(Algorithm.RSA.getValue(), key);
    }

    /**
     * Generates a public key for asymmetric encryption from a byte array. This method expects the key to be in X.509
     * certificate format. For supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param key       The public key material in DER-encoded X.509 format.
     * @return The {@link PublicKey}, or {@code null} if the key material is {@code null}.
     * @throws CryptoException if key generation fails.
     */
    public static PublicKey generatePublicKey(final String algorithm, final byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePublicKey(algorithm, new X509EncodedKeySpec(key));
    }

    /**
     * Generates a public key for asymmetric encryption from a {@link KeySpec}. For supported algorithms, refer to:
     * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">Standard
     * Names for KeyFactory Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param keySpec   The {@link KeySpec} for the public key.
     * @return The {@link PublicKey}, or {@code null} if the key specification is {@code null}.
     * @throws CryptoException if key generation fails.
     */
    public static PublicKey generatePublicKey(String algorithm, final KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePublic(keySpec);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Derives an RSA public key from an RSA private key.
     *
     * @param privateKey The RSA private key (must be an instance of {@link RSAPrivateCrtKey}).
     * @return The corresponding RSA public key, or {@code null} if the private key is not supported or is not an
     *         instance of {@link RSAPrivateCrtKey}.
     */
    public static PublicKey getRSAPublicKey(final PrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateCrtKey) {
            final RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
            return getRSAPublicKey(privk.getModulus(), privk.getPublicExponent());
        }
        return null;
    }

    /**
     * Retrieves an RSA public key object from its modulus and public exponent in hexadecimal string format.
     *
     * @param modulus        The RSA modulus as a hexadecimal string.
     * @param publicExponent The RSA public exponent as a hexadecimal string.
     * @return The RSA {@link PublicKey}.
     * @throws CryptoException if key generation fails due to an invalid key specification.
     */
    public static PublicKey getRSAPublicKey(final String modulus, final String publicExponent) {
        return getRSAPublicKey(new BigInteger(modulus, 16), new BigInteger(publicExponent, 16));
    }

    /**
     * Retrieves an RSA public key object from its modulus and public exponent as {@link BigInteger}s.
     *
     * @param modulus        The RSA modulus as a {@link BigInteger}.
     * @param publicExponent The RSA public exponent as a {@link BigInteger}.
     * @return The RSA {@link PublicKey}.
     * @throws CryptoException if key generation fails due to an invalid key specification.
     */
    public static PublicKey getRSAPublicKey(final BigInteger modulus, final BigInteger publicExponent) {
        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        try {
            return getKeyFactory(Algorithm.RSA.getValue()).generatePublic(publicKeySpec);
        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Generates an asymmetric key pair (public and private keys). The key size will be determined by the algorithm's
     * default or a predefined default (e.g., 256 for ECIES). For supported algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(final String algorithm) {
        int keySize = DEFAULT_KEY_SIZE;
        if ("ECIES".equalsIgnoreCase(algorithm)) {
            // ECIES algorithm has key length requirements, default to 256 here
            keySize = 256;
        }

        return generateKeyPair(algorithm, keySize);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with a specified key size. For supported algorithms,
     * refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param keySize   The length of the key modulus in bits.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(final String algorithm, final int keySize) {
        return generateKeyPair(algorithm, keySize, null);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with a specified key size and seed. For supported
     * algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param keySize   The length of the key modulus in bits.
     * @param seed      The seed for the {@link SecureRandom} instance. If {@code null}, a random seed is used.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(final String algorithm, final int keySize, final byte[] seed) {
        // SM2 algorithm requires special curve generation
        if ("SM2".equalsIgnoreCase(algorithm)) {
            final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(Builder.SM2_CURVE_NAME);
            return generateKeyPair(algorithm, keySize, seed, sm2p256v1);
        }

        return generateKeyPair(algorithm, keySize, seed, (AlgorithmParameterSpec[]) null);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with specified algorithm parameters. For supported
     * algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param params    The {@link AlgorithmParameterSpec} to initialize the key pair generator.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(final String algorithm, final AlgorithmParameterSpec params) {
        return generateKeyPair(algorithm, null, params);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with a specified algorithm, seed, and parameters. For
     * supported algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param seed      The seed for the {@link SecureRandom} instance. If {@code null}, a random seed is used.
     * @param param     The {@link AlgorithmParameterSpec} to initialize the key pair generator.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(
            final String algorithm,
            final byte[] seed,
            final AlgorithmParameterSpec param) {
        return generateKeyPair(algorithm, DEFAULT_KEY_SIZE, seed, param);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with a specified algorithm, key size, seed, and
     * parameters. For supported algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     * <p>
     * For asymmetric encryption algorithms, key lengths have strict restrictions, as follows:
     * <p>
     * <b>RSA:</b>
     * 
     * <pre>
     * RS256, PS256: 2048 bits
     * RS384, PS384: 3072 bits
     * RS512, RS512: 4096 bits
     * </pre>
     * <p>
     * <b>EC (Elliptic Curve):</b>
     * 
     * <pre>
     * EC256: 256 bits
     * EC384: 384 bits
     * EC512: 512 bits
     * </pre>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param keySize   The length of the key modulus in bits.
     * @param seed      The seed for the {@link SecureRandom} instance. If {@code null}, a random seed is used.
     * @param params    Optional {@link AlgorithmParameterSpec}s to initialize the key pair generator.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails.
     */
    public static KeyPair generateKeyPair(
            final String algorithm,
            final int keySize,
            final byte[] seed,
            final AlgorithmParameterSpec... params) {
        return generateKeyPair(algorithm, keySize, RandomKit.createSecureRandom(seed), params);
    }

    /**
     * Generates an asymmetric key pair (public and private keys) with a specified algorithm, key size, random source,
     * and parameters. For supported algorithms, refer to: <a href=
     * "https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Standard
     * Names for KeyPairGenerator Algorithms</a>
     * <p>
     * For asymmetric encryption algorithms, key lengths have strict restrictions, as follows:
     * <p>
     * <b>RSA:</b>
     * 
     * <pre>
     * RS256, PS256: 2048 bits
     * RS384, PS384: 3072 bits
     * RS512, RS512: 4096 bits
     * </pre>
     * <p>
     * <b>EC (Elliptic Curve):</b>
     * 
     * <pre>
     * EC256: 256 bits
     * EC384: 384 bits
     * EC512: 512 bits
     * </pre>
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @param keySize   The length of the key modulus in bits.
     * @param random    The {@link SecureRandom} object to use for key generation. Can be {@code null} for default.
     * @param params    Optional {@link AlgorithmParameterSpec}s to initialize the key pair generator.
     * @return A new {@link KeyPair}.
     * @throws CryptoException if key pair generation fails due to an invalid algorithm parameter.
     */
    public static KeyPair generateKeyPair(
            String algorithm,
            int keySize,
            final SecureRandom random,
            final AlgorithmParameterSpec... params) {
        algorithm = getAlgorithmAfterWith(algorithm);
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

        // Initialize key modulus length
        if (keySize > 0) {
            // Key length adaptation correction
            if ("EC".equalsIgnoreCase(algorithm) && keySize > 256) {
                // For EC (Elliptic Curve) algorithms, key length is limited, default to 256 here
                keySize = 256;
            }
            if (null != random) {
                keyPairGen.initialize(keySize, random);
            } else {
                keyPairGen.initialize(keySize);
            }
        }

        // Custom initialization parameters
        if (ArrayKit.isNotEmpty(params)) {
            for (final AlgorithmParameterSpec param : params) {
                if (null == param) {
                    continue;
                }
                try {
                    if (null != random) {
                        keyPairGen.initialize(param, random);
                    } else {
                        keyPairGen.initialize(param);
                    }
                } catch (final InvalidAlgorithmParameterException e) {
                    throw new CryptoException(e);
                }
            }
        }
        return keyPairGen.generateKeyPair();
    }

    /**
     * Retrieves a {@link KeyPair} from a {@link KeyStore} given its type, input stream, password, and alias.
     *
     * @param type     The type of the KeyStore (e.g., {@link #TYPE_JKS}, {@link #TYPE_PKCS12}).
     * @param in       The {@link InputStream} to read the KeyStore from. Use
     *                 {@link FileKit#getInputStream(java.io.File)} for file-based KeyStores.
     * @param password The password for the KeyStore.
     * @param alias    The alias of the key pair in the KeyStore.
     * @return The {@link KeyPair}.
     * @throws CryptoException if KeyStore reading or key pair retrieval fails.
     */
    public static KeyPair getKeyPair(
            final String type,
            final InputStream in,
            final char[] password,
            final String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        return getKeyPair(keyStore, password, alias);
    }

    /**
     * Retrieves a {@link KeyPair} from a {@link KeyStore} given its password and alias.
     *
     * @param keyStore The {@link KeyStore} containing the key pair.
     * @param password The password for the private key in the KeyStore.
     * @param alias    The alias of the key pair in the KeyStore.
     * @return The {@link KeyPair}.
     * @throws CryptoException if key pair retrieval fails.
     */
    public static KeyPair getKeyPair(final KeyStore keyStore, final char[] password, final String alias) {
        final PublicKey publicKey;
        final PrivateKey privateKey;
        try {
            publicKey = keyStore.getCertificate(alias).getPublicKey();
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Retrieves a {@link KeyPairGenerator} instance for the given asymmetric encryption algorithm.
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @return A {@link KeyPairGenerator} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static KeyPairGenerator getKeyPairGenerator(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final KeyPairGenerator keyPairGen;
        try {
            keyPairGen = (null == provider) //
                    ? KeyPairGenerator.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyPairGenerator.getInstance(getMainAlgorithm(algorithm), provider);//
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyPairGen;
    }

    /**
     * Retrieves a {@link KeyFactory} instance for the given asymmetric encryption algorithm.
     *
     * @param algorithm The asymmetric encryption algorithm.
     * @return A {@link KeyFactory} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static KeyFactory getKeyFactory(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();
        try {
            return (null == provider) ? KeyFactory.getInstance(getMainAlgorithm(algorithm))
                    : KeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Retrieves a {@link SecretKeyFactory} instance for the given symmetric encryption algorithm.
     *
     * @param algorithm The symmetric encryption algorithm.
     * @return A {@link SecretKeyFactory} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static SecretKeyFactory getSecretKeyFactory(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final SecretKeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? SecretKeyFactory.getInstance(getMainAlgorithm(algorithm)) //
                    : SecretKeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyFactory;
    }

    /**
     * Retrieves a {@link KeyGenerator} instance for the given symmetric encryption algorithm.
     *
     * @param algorithm The symmetric encryption algorithm.
     * @return A {@link KeyGenerator} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static KeyGenerator getKeyGenerator(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();
        final KeyGenerator generator;
        try {
            generator = (null == provider) //
                    ? KeyGenerator.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyGenerator.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return generator;
    }

    /**
     * Extracts the main algorithm name from a full algorithm string (e.g., "RSA/ECB/PKCS1Padding" -> "RSA").
     *
     * @param algorithm The full algorithm string, possibly including mode and padding.
     * @return The main algorithm name.
     * @throws IllegalArgumentException if the algorithm string is blank.
     */
    public static String getMainAlgorithm(final String algorithm) {
        Assert.notBlank(algorithm, "Algorithm must be not blank!");
        final int slashIndex = algorithm.indexOf(Symbol.C_SLASH);
        if (slashIndex > 0) {
            return algorithm.substring(0, slashIndex);
        }
        return algorithm;
    }

    /**
     * Extracts the algorithm name used for key generation from a full algorithm string. For algorithms like ECDSA or
     * SM2, it returns "EC".
     *
     * @param algorithm The full algorithm string, possibly including "with" clauses.
     * @return The algorithm name suitable for key generation.
     * @throws IllegalArgumentException if the algorithm string is null.
     */
    public static String getAlgorithmAfterWith(String algorithm) {
        Assert.notNull(algorithm, "algorithm must be not null !");

        if (StringKit.startWithIgnoreCase(algorithm, "ECIESWith")) {
            return "EC";
        }

        // For algorithms like RSA/ECB/OAEPWithSHA-1AndMGF1Padding, only get the main method
        algorithm = getMainAlgorithm(algorithm);
        final int indexOfWith = StringKit.lastIndexOfIgnoreCase(algorithm, "with");
        if (indexOfWith > 0) {
            algorithm = StringKit.subSuf(algorithm, indexOfWith + "with".length());
        }
        if ("ECDSA".equalsIgnoreCase(algorithm) || "SM2".equalsIgnoreCase(algorithm)
                || "ECIES".equalsIgnoreCase(algorithm)) {
            algorithm = "EC";
        }
        return algorithm;
    }

    /**
     * Reads a public key from an X.509 certificate input stream. For more information on certificate files, see:
     * <a href="https://www.cnblogs.com/yinliang/p/10115519.html">Certificate File Explanation</a>
     *
     * @param in The {@link InputStream} of the .cer file. Use {@link FileKit#getInputStream(File)} to read from a file.
     * @return The {@link PublicKey} extracted from the certificate, or {@code null} if the certificate is invalid or
     *         not found.
     * @throws CryptoException if certificate reading fails.
     */
    public static PublicKey readPublicKeyFromCert(final InputStream in) {
        final java.security.cert.Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
    }

    /**
     * Encodes an EC public key (based on BouncyCastle). This method is a placeholder and should be replaced with a
     * specific encoding method. See: <a href="https://www.cnblogs.com/xinzhao/p/8963724.html">EC Public Key
     * Encoding</a>
     *
     * @param publicKey The {@link PublicKey}, which must be an instance of
     *                  {@code org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey}.
     * @return The encoded X coordinate of the public key.
     * @deprecated This method has a duplicate name and should be replaced by
     *             {@link #encodeECPublicKey(PublicKey, boolean)}.
     */
    @Deprecated
    public static byte[] encodeECPublicKey(final PublicKey publicKey) {
        return encodeECPublicKey(publicKey, false);
    }

    /**
     * Encodes a key into Base64 format.
     *
     * @param key The {@link Key} to encode.
     * @return The Base64 encoded key string.
     */
    public static String toBase64(final Key key) {
        return Base64.encode(key.getEncoded());
    }

    /**
     * Reads a JKS (Java Key Store) KeyStore file. KeyStore files are used to store key pairs for digital certificates.
     * See: <a href="http://snowolf.iteye.com/blog/391931">KeyStore File Usage</a>
     *
     * @param keyFile  The KeyStore file.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readJKSKeyStore(final File keyFile, final char[] password) {
        return readKeyStore(TYPE_JKS, keyFile, password);
    }

    /**
     * Reads a JKS (Java Key Store) KeyStore from an input stream. KeyStore files are used to store key pairs for
     * digital certificates. See: <a href="http://snowolf.iteye.com/blog/391931">KeyStore File Usage</a>
     *
     * @param in       The {@link InputStream} to read the KeyStore from. Use {@link FileKit#getInputStream(File)} to
     *                 read from a file.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readJKSKeyStore(final InputStream in, final char[] password) {
        return readKeyStore(TYPE_JKS, in, password);
    }

    /**
     * Reads a PKCS#12 KeyStore file. KeyStore files are used to store key pairs for digital certificates.
     *
     * @param keyFile  The KeyStore file.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readPKCS12KeyStore(final File keyFile, final char[] password) {
        return readKeyStore(TYPE_PKCS12, keyFile, password);
    }

    /**
     * Reads a PKCS#12 KeyStore from an input stream. KeyStore files are used to store key pairs for digital
     * certificates.
     *
     * @param in       The {@link InputStream} to read the KeyStore from. Use {@link FileKit#getInputStream(File)} to
     *                 read from a file.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readPKCS12KeyStore(final InputStream in, final char[] password) {
        return readKeyStore(TYPE_PKCS12, in, password);
    }

    /**
     * Reads a KeyStore file, automatically determining the type based on the file extension. KeyStore files are used to
     * store key pairs for digital certificates. The type is determined as follows:
     * 
     * <pre>
     *     .jks, .keystore -> JKS
     *     .p12, .pfx, etc. -> PKCS12
     * </pre>
     *
     * @param keyFile  The KeyStore file.
     * @param password The password for the KeyStore. Can be {@code null} if no password is required.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readKeyStore(final File keyFile, final char[] password) {
        final String suffix = FileName.getSuffix(keyFile);
        final String type;
        if (StringKit.equalsIgnoreCase(suffix, "jks") || StringKit.equalsIgnoreCase(suffix, "keystore")) {
            type = TYPE_JKS;
        } else {
            type = TYPE_PKCS12;
        }
        return readKeyStore(type, keyFile, password);
    }

    /**
     * Reads a KeyStore file of a specified type. KeyStore files are used to store key pairs for digital certificates.
     * See: <a href="http://snowolf.iteye.com/blog/391931">KeyStore File Usage</a>
     *
     * @param type     The type of the KeyStore (e.g., {@link #TYPE_JKS}, {@link #TYPE_PKCS12}).
     * @param keyFile  The KeyStore file.
     * @param password The password for the KeyStore. Can be {@code null} if no password is required.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readKeyStore(final String type, final File keyFile, final char[] password) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(keyFile);
            return readKeyStore(type, in, password);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Reads a KeyStore from an input stream of a specified type. KeyStore files are used to store key pairs for digital
     * certificates. See: <a href="http://snowolf.iteye.com/blog/391931">KeyStore File Usage</a>
     *
     * @param type     The type of the KeyStore (e.g., {@link #TYPE_JKS}, {@link #TYPE_PKCS12}).
     * @param in       The {@link InputStream} to read the KeyStore from. Use {@link FileKit#getInputStream(File)} to
     *                 read from a file.
     * @param password The password for the KeyStore. Can be {@code null} if no password is required.
     * @return The loaded {@link KeyStore}.
     * @throws CryptoException if KeyStore reading fails.
     */
    public static KeyStore readKeyStore(final String type, final InputStream in, final char[] password) {
        final KeyStore keyStore = getKeyStore(type);
        try {
            keyStore.load(in, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
        return keyStore;
    }

    /**
     * Retrieves a {@link KeyStore} instance of a specified type.
     *
     * @param type The type of the KeyStore (e.g., {@link #TYPE_JKS}, {@link #TYPE_PKCS12}).
     * @return A {@link KeyStore} instance.
     * @throws CryptoException if the KeyStore type is not found.
     */
    public static KeyStore getKeyStore(final String type) {
        final java.security.Provider provider = Holder.getProvider();
        try {
            return null == provider ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Reads a private key from a PEM (Privacy-Enhanced Mail) formatted input stream.
     *
     * @param pemStream The {@link InputStream} containing the PEM-encoded private key.
     * @return The {@link PrivateKey}.
     * @throws CryptoException if reading or parsing the PEM private key fails.
     */
    public static PrivateKey readPemPrivateKey(final InputStream pemStream) {
        return (PrivateKey) readPemKey(pemStream);
    }

    /**
     * Reads a public key from a PEM (Privacy-Enhanced Mail) formatted input stream.
     *
     * @param pemStream The {@link InputStream} containing the PEM-encoded public key.
     * @return The {@link PublicKey}.
     * @throws CryptoException if reading or parsing the PEM public key fails.
     */
    public static PublicKey readPemPublicKey(final InputStream pemStream) {
        return (PublicKey) readPemKey(pemStream);
    }

    /**
     * Reads a key (either public or private) from a PEM (Privacy-Enhanced Mail) formatted input stream. The type of key
     * ({@link PublicKey} or {@link PrivateKey}) is determined by the content of the PEM object.
     *
     * @param keyStream The {@link InputStream} containing the PEM-encoded key.
     * @return The {@link Key} (either {@link PublicKey} or {@link PrivateKey}), or {@code null} if the key type is
     *         unrecognized.
     * @throws CryptoException if reading or parsing the PEM key fails.
     */
    public static Key readPemKey(final InputStream keyStream) {
        final PemObject object = readPemObject(keyStream);
        final String type = object.getType();
        if (StringKit.isNotBlank(type)) {
            // private
            if (type.endsWith("EC PRIVATE KEY")) {
                try {
                    // Try PKCS#8
                    return generatePrivateKey("EC", object.getContent());
                } catch (final Exception e) {
                    // Try PKCS#1
                    return generatePrivateKey("EC", getOpenSSHPrivateKeySpec(object.getContent()));
                }
            }
            if (type.endsWith("PRIVATE KEY")) {
                return generateRSAPrivateKey(object.getContent());
            }

            // public
            if (type.endsWith("EC PUBLIC KEY")) {
                try {
                    // Try DER
                    return generatePublicKey("EC", object.getContent());
                } catch (final Exception ignore) {
                    // Try PKCS#1
                    return generatePublicKey("EC", getOpenSSHPublicKeySpec(object.getContent()));
                }
            } else if (type.endsWith("PUBLIC KEY")) {
                return generateRSAPublicKey(object.getContent());
            } else if (type.endsWith("CERTIFICATE")) {
                return readPublicKeyFromCert(IoKit.toStream(object.getContent()));
            }
        }

        // Unrecognized key type
        return null;
    }

    /**
     * Reads the raw byte content of a PEM object from an input stream.
     *
     * @param keyStream The {@link InputStream} containing the PEM object.
     * @return The byte array content of the PEM object, or {@code null} if the PEM object is {@code null}.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public static byte[] readPem(final InputStream keyStream) {
        final PemObject pemObject = readPemObject(keyStream);
        if (null != pemObject) {
            return pemObject.getContent();
        }
        return null;
    }

    /**
     * Reads a {@link PemObject} from an input stream. A {@link PemObject} includes the type, header information, and
     * content of the PEM entry.
     *
     * @param keyStream The {@link InputStream} containing the PEM data.
     * @return The {@link PemObject}.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public static PemObject readPemObject(final InputStream keyStream) {
        return readPemObject(IoKit.toUtf8Reader(keyStream));
    }

    /**
     * Reads a {@link PemObject} from a {@link Reader}. A {@link PemObject} includes the type, header information, and
     * content of the PEM entry.
     *
     * @param reader The {@link Reader} containing the PEM data.
     * @return The {@link PemObject}.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public static PemObject readPemObject(final Reader reader) {
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(reader);
            return pemReader.readPemObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(pemReader);
        }
    }

    /**
     * Converts a key or certificate into a PEM (Privacy-Enhanced Mail) formatted string.
     *
     * @param type    The type of the key (e.g., "PRIVATE KEY", "PUBLIC KEY", "CERTIFICATE").
     * @param content The byte array content of the key or certificate.
     * @return The PEM formatted string.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public static String toPem(final String type, final byte[] content) {
        final StringWriter stringWriter = new StringWriter();
        writePemObject(type, content, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Writes a PEM (Privacy-Enhanced Mail) object to an output stream.
     *
     * @param type      The type of the key (e.g., "PRIVATE KEY", "PUBLIC KEY", "CERTIFICATE").
     * @param content   The byte array content of the key or certificate, expected in PKCS#1 format.
     * @param keyStream The {@link OutputStream} to write the PEM object to.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public static void writePemObject(final String type, final byte[] content, final OutputStream keyStream) {
        writePemObject(new PemObject(type, content), keyStream);
    }

    /**
     * Writes a PEM (Privacy-Enhanced Mail) object to a writer.
     *
     * @param type    The type of the key (e.g., "PRIVATE KEY", "PUBLIC KEY", "CERTIFICATE").
     * @param content The byte array content of the key or certificate, expected in PKCS#1 format.
     * @param writer  The {@link Writer} to write the PEM object to.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public static void writePemObject(final String type, final byte[] content, final Writer writer) {
        writePemObject(new PemObject(type, content), writer);
    }

    /**
     * Writes a {@link PemObjectGenerator} to an output stream.
     *
     * @param pemObject The {@link PemObjectGenerator} containing the key or certificate information.
     * @param keyStream The {@link OutputStream} to write the PEM object to.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public static void writePemObject(final PemObjectGenerator pemObject, final OutputStream keyStream) {
        writePemObject(pemObject, IoKit.toUtf8Writer(keyStream));
    }

    /**
     * Writes a {@link PemObjectGenerator} to a writer.
     *
     * @param pemObject The {@link PemObjectGenerator} containing the key or certificate information.
     * @param writer    The {@link Writer} to write the PEM object to.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public static void writePemObject(final PemObjectGenerator pemObject, final Writer writer) {
        final PemWriter pemWriter = new PemWriter(writer);
        try {
            pemWriter.writeObject(pemObject);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(pemWriter);
        }
    }

    /**
     * Retrieves an EC public key from an EC private key and its parameter specification.
     *
     * @param privateKey The EC private key (must be an instance of
     *                   {@code org.bouncycastle.jce.interfaces.ECPrivateKey}).
     * @param spec       The EC parameter specification.
     * @return The EC {@link PublicKey}.
     * @throws CryptoException if public key generation fails.
     */
    public static PublicKey getECPublicKey(
            final org.bouncycastle.jce.interfaces.ECPrivateKey privateKey,
            final org.bouncycastle.jce.spec.ECParameterSpec spec) {
        final org.bouncycastle.jce.spec.ECPublicKeySpec keySpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(
                getQFromD(privateKey.getD(), spec), spec);
        return generatePublicKey("EC", keySpec);
    }

    /**
     * Calculates the public key point (Q value) from a private key's D value and EC parameter specification.
     *
     * @param d    The private key's D value.
     * @param spec The EC parameter specification.
     * @return The public key's point coordinate (Q value).
     */
    public static org.bouncycastle.math.ec.ECPoint getQFromD(
            final BigInteger d,
            final org.bouncycastle.jce.spec.ECParameterSpec spec) {
        return spec.getG().multiply(d).normalize();
    }

    /**
     * Encodes an EC private key by extracting its D value as a 32-byte array.
     *
     * @param privateKey The {@link PrivateKey}, which must be an instance of
     *                   {@code org.bouncycastle.jce.interfaces.ECPrivateKey}.
     * @return The D value of the private key as a byte array.
     */
    public static byte[] encodeECPrivateKey(final PrivateKey privateKey) {
        return ((org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey).getD().toByteArray();
    }

    /**
     * Encodes and compresses an EC public key (Q value) based on BouncyCastle. See:
     * <a href="https://www.cnblogs.com/xinzhao/p/8963724.html">EC Public Key Encoding</a>
     *
     * @param publicKey    The {@link PublicKey}, which must be an instance of
     *                     {@code org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey}.
     * @param isCompressed {@code true} to return the compressed form of the public key, {@code false} for the
     *                     uncompressed form.
     * @return The encoded Q value of the public key.
     */
    public static byte[] encodeECPublicKey(final PublicKey publicKey, final boolean isCompressed) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(isCompressed);
    }

    /**
     * Decodes and recovers an EC compressed public key (based on BouncyCastle) from a Base64 or Hex encoded string.
     * See: <a href="https://www.cnblogs.com/xinzhao/p/8963724.html">EC Public Key Decoding</a>
     *
     * @param encode    The Base64 or Hex encoded compressed public key string.
     * @param curveName The name of the EC curve.
     * @return The recovered {@link PublicKey}.
     * @throws CryptoException if decoding or public key generation fails.
     */
    public static PublicKey decodeECPoint(final String encode, final String curveName) {
        return decodeECPoint(Builder.decode(encode), curveName);
    }

    /**
     * Decodes and recovers an EC compressed public key (based on BouncyCastle) from a byte array.
     *
     * @param encodeByte The byte array of the compressed public key.
     * @param curveName  The name of the EC curve, e.g., {@link Builder#SM2_DOMAIN_PARAMS}.
     * @return The recovered {@link PublicKey}.
     * @throws CryptoException if decoding or public key generation fails.
     */
    public static PublicKey decodeECPoint(final byte[] encodeByte, final String curveName) {
        final X9ECParameters x9ECParameters = ECUtil.getNamedCurveByName(curveName);
        final ECCurve curve = x9ECParameters.getCurve();
        final ECPoint point = EC5Util.convertPoint(curve.decodePoint(encodeByte));

        // Recover public key format based on the curve
        final ECNamedCurveSpec ecSpec = new ECNamedCurveSpec(curveName, curve, x9ECParameters.getG(),
                x9ECParameters.getN());
        return generatePublicKey("EC", new ECPublicKeySpec(point, ecSpec));
    }

    /**
     * Converts a {@link Key} (either {@link PrivateKey} or {@link PublicKey}) to an {@link AsymmetricKeyParameter}.
     *
     * @param key The {@link Key} to convert.
     * @return An {@link ECPrivateKeyParameters} or {@link ECPublicKeyParameters} instance, or {@code null} if the key
     *         type is not supported.
     * @throws CryptoException if key conversion fails.
     */
    public static AsymmetricKeyParameter toParams(final Key key) {
        if (key instanceof PrivateKey) {
            return toPrivateParams((PrivateKey) key);
        } else if (key instanceof PublicKey) {
            return toPublicParams((PublicKey) key);
        }

        return null;
    }

    /**
     * Derives public key parameters from private key parameters.
     *
     * @param privateKeyParameters The {@link ECPrivateKeyParameters}.
     * @return The corresponding {@link ECPublicKeyParameters}.
     */
    public static ECPublicKeyParameters getPublicParams(final ECPrivateKeyParameters privateKeyParameters) {
        final ECDomainParameters domainParameters = privateKeyParameters.getParameters();
        final org.bouncycastle.math.ec.ECPoint q = new FixedPointCombMultiplier()
                .multiply(domainParameters.getG(), privateKeyParameters.getD());
        return new ECPublicKeyParameters(q, domainParameters);
    }

    /**
     * Converts a byte array representing the SM2 public key Q value to {@link ECPublicKeyParameters}.
     *
     * @param q The byte array of the SM2 public key Q value.
     * @return The {@link ECPublicKeyParameters} for SM2.
     */
    public static ECPublicKeyParameters toSm2PublicParams(final byte[] q) {
        return toPublicParams(q, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts a hexadecimal string representing the SM2 public key Q value to {@link ECPublicKeyParameters}.
     *
     * @param q The hexadecimal string of the SM2 public key Q value.
     * @return The {@link ECPublicKeyParameters} for SM2.
     */
    public static ECPublicKeyParameters toSm2PublicParams(final String q) {
        return toPublicParams(q, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts SM2 public key X and Y coordinates (as hexadecimal strings) to {@link ECPublicKeyParameters}.
     *
     * @param x The hexadecimal string of the public key X coordinate.
     * @param y The hexadecimal string of the public key Y coordinate.
     * @return The {@link ECPublicKeyParameters} for SM2.
     */
    public static ECPublicKeyParameters toSm2PublicParams(final String x, final String y) {
        return toPublicParams(x, y, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts SM2 public key X and Y coordinates (as byte arrays) to {@link ECPublicKeyParameters}.
     *
     * @param xBytes The byte array of the public key X coordinate.
     * @param yBytes The byte array of the public key Y coordinate.
     * @return The {@link ECPublicKeyParameters} for SM2.
     */
    public static ECPublicKeyParameters toSm2PublicParams(final byte[] xBytes, final byte[] yBytes) {
        return toPublicParams(xBytes, yBytes, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts public key X and Y coordinates (as hexadecimal strings) and domain parameters to
     * {@link ECPublicKeyParameters}.
     *
     * @param x                The hexadecimal string of the public key X coordinate.
     * @param y                The hexadecimal string of the public key Y coordinate.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}, or {@code null} if x or y are {@code null}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final String x,
            final String y,
            final ECDomainParameters domainParameters) {
        return toPublicParams(Builder.decode(x), Builder.decode(y), domainParameters);
    }

    /**
     * Converts public key X and Y coordinates (as byte arrays) and domain parameters to {@link ECPublicKeyParameters}.
     *
     * @param xBytes           The byte array of the public key X coordinate.
     * @param yBytes           The byte array of the public key Y coordinate.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}.
     * @throws NullPointerException if xBytes or yBytes are {@code null}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final byte[] xBytes,
            final byte[] yBytes,
            final ECDomainParameters domainParameters) {
        if (null == xBytes || null == yBytes) {
            return null;
        }
        return toPublicParams(
                BigIntegers.fromUnsignedByteArray(xBytes),
                BigIntegers.fromUnsignedByteArray(yBytes),
                domainParameters);
    }

    /**
     * Converts public key X and Y coordinates (as {@link BigInteger}s) and domain parameters to
     * {@link ECPublicKeyParameters}.
     *
     * @param x                The {@link BigInteger} of the public key X coordinate.
     * @param y                The {@link BigInteger} of the public key Y coordinate.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}, or {@code null} if x or y are {@code null}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final BigInteger x,
            final BigInteger y,
            final ECDomainParameters domainParameters) {
        if (null == x || null == y) {
            return null;
        }
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.createPoint(x, y), domainParameters);
    }

    /**
     * Converts an encoded curve coordinate point (as a hexadecimal string) and domain parameters to
     * {@link ECPublicKeyParameters}.
     *
     * @param pointEncoded     The hexadecimal string of the encoded curve coordinate point.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final String pointEncoded,
            final ECDomainParameters domainParameters) {
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.decodePoint(Builder.decode(pointEncoded)), domainParameters);
    }

    /**
     * Converts an encoded curve coordinate point (as a byte array) and domain parameters to
     * {@link ECPublicKeyParameters}.
     *
     * @param pointEncoded     The byte array of the encoded curve coordinate point.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final byte[] pointEncoded,
            final ECDomainParameters domainParameters) {
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.decodePoint(pointEncoded), domainParameters);
    }

    /**
     * Converts an {@code org.bouncycastle.math.ec.ECPoint} and domain parameters to {@link ECPublicKeyParameters}.
     *
     * @param point            The {@code org.bouncycastle.math.ec.ECPoint} representing the public key coordinate.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPublicKeyParameters}.
     */
    public static ECPublicKeyParameters toPublicParams(
            final org.bouncycastle.math.ec.ECPoint point,
            final ECDomainParameters domainParameters) {
        return new ECPublicKeyParameters(point, domainParameters);
    }

    /**
     * Converts a {@link PublicKey} to {@link ECPublicKeyParameters}.
     *
     * @param publicKey The {@link PublicKey} to convert. If {@code null}, returns {@code null}.
     * @return The {@link ECPublicKeyParameters} or {@code null}.
     * @throws CryptoException if key conversion fails due to an invalid key.
     */
    public static ECPublicKeyParameters toPublicParams(final PublicKey publicKey) {
        if (null == publicKey) {
            return null;
        }
        try {
            return (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(publicKey);
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Converts a hexadecimal string representing the private key D value to {@link ECPrivateKeyParameters}.
     *
     * @param d The hexadecimal string of the private key D value.
     * @return The {@link ECPrivateKeyParameters}.
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final String d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts a byte array representing the private key D value to {@link ECPrivateKeyParameters}.
     *
     * @param d The byte array of the private key D value.
     * @return The {@link ECPrivateKeyParameters}.
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final byte[] d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts a {@link BigInteger} representing the private key D value to {@link ECPrivateKeyParameters}.
     *
     * @param d The {@link BigInteger} of the private key D value.
     * @return The {@link ECPrivateKeyParameters}.
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final BigInteger d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * Converts a hexadecimal string representing the private key D value and domain parameters to
     * {@link ECPrivateKeyParameters}.
     *
     * @param d                The hexadecimal string of the private key D value.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPrivateKeyParameters}, or {@code null} if d is {@code null}.
     * @throws NullPointerException if the decoded byte array of d is {@code null}.
     */
    public static ECPrivateKeyParameters toPrivateParams(final String d, final ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return toPrivateParams(
                BigIntegers.fromUnsignedByteArray(Objects.requireNonNull(Builder.decode(d))),
                domainParameters);
    }

    /**
     * Converts a byte array representing the private key D value and domain parameters to
     * {@link ECPrivateKeyParameters}.
     *
     * @param d                The byte array of the private key D value.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPrivateKeyParameters}, or {@code null} if d is {@code null}.
     */
    public static ECPrivateKeyParameters toPrivateParams(final byte[] d, final ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return toPrivateParams(BigIntegers.fromUnsignedByteArray(d), domainParameters);
    }

    /**
     * Converts a {@link BigInteger} representing the private key D value and domain parameters to
     * {@link ECPrivateKeyParameters}.
     *
     * @param d                The {@link BigInteger} of the private key D value.
     * @param domainParameters The {@link ECDomainParameters} for the elliptic curve.
     * @return The {@link ECPrivateKeyParameters}, or {@code null} if d is {@code null}.
     */
    public static ECPrivateKeyParameters toPrivateParams(
            final BigInteger d,
            final ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return new ECPrivateKeyParameters(d, domainParameters);
    }

    /**
     * Converts a {@link PrivateKey} to {@link ECPrivateKeyParameters}.
     *
     * @param privateKey The {@link PrivateKey} to convert. If {@code null}, returns {@code null}.
     * @return The {@link ECPrivateKeyParameters} or {@code null}.
     * @throws CryptoException if key conversion fails due to an invalid key.
     */
    public static ECPrivateKeyParameters toPrivateParams(final PrivateKey privateKey) {
        if (null == privateKey) {
            return null;
        }
        try {
            return (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Converts an SM2 {@link ECPrivateKey} to a standard {@link PrivateKey}.
     *
     * @param privateKey The SM2 {@link ECPrivateKey}.
     * @return The standard {@link PrivateKey}.
     * @throws InternalException if an I/O error occurs during encoding.
     * @throws CryptoException   if private key generation fails.
     */
    public static PrivateKey toSm2PrivateKey(final ECPrivateKey privateKey) {
        try {
            final PrivateKeyInfo info = new PrivateKeyInfo(
                    new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, Builder.ID_SM2_PUBLIC_KEY_PARAM),
                    privateKey);
            return generatePrivateKey("SM2", info.getEncoded());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Generates an SM2 private key from a byte array, supporting various formats:
     * <ul>
     * <li>D value</li>
     * <li>PKCS#8 format</li>
     * <li>PKCS#1 format</li>
     * <li>OpenSSH format</li>
     * </ul>
     *
     * @param privateKeyBytes The byte array of the private key.
     * @return The {@link PrivateKey} for SM2, or {@code null} if the input is {@code null}.
     * @throws CryptoException if private key generation fails for any of the formats.
     */
    public static PrivateKey generateSm2PrivateKey(final byte[] privateKeyBytes) {
        if (null == privateKeyBytes) {
            return null;
        }
        final String algorithm = "SM2";
        KeySpec keySpec;
        // Try D value
        try {
            keySpec = getPrivateKeySpec(privateKeyBytes, Builder.SM2_EC_SPEC);
            return generatePrivateKey(algorithm, keySpec);
        } catch (final Exception ignore) {
        }

        // Try PKCS#8
        try {
            keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return generatePrivateKey(algorithm, keySpec);
        } catch (final Exception ignore) {
        }

        // Try PKCS#1 or OpenSSH format
        keySpec = getOpenSSHPrivateKeySpec(privateKeyBytes);
        return generatePrivateKey(algorithm, keySpec);
    }

    /**
     * Generates an SM2 public key from a byte array, supporting various formats:
     * <ul>
     * <li>Q value</li>
     * <li>X.509 format</li>
     * <li>PKCS#1 format</li>
     * </ul>
     *
     * @param publicKeyBytes The byte array of the public key.
     * @return The {@link PublicKey} for SM2, or {@code null} if the input is {@code null}.
     * @throws CryptoException if public key generation fails for any of the formats.
     */
    public static PublicKey generateSm2PublicKey(final byte[] publicKeyBytes) {
        if (null == publicKeyBytes) {
            return null;
        }
        final String algorithm = "SM2";
        KeySpec keySpec;
        // Try Q value
        try {
            keySpec = getPublicKeySpec(publicKeyBytes, Builder.SM2_EC_SPEC);
            return generatePublicKey(algorithm, keySpec);
        } catch (final Exception ignore) {
            // ignore
        }

        // Try X.509
        try {
            keySpec = new X509EncodedKeySpec(publicKeyBytes);
            return generatePublicKey(algorithm, keySpec);
        } catch (final Exception ignore) {
        }

        // Try PKCS#1
        keySpec = getOpenSSHPublicKeySpec(publicKeyBytes);
        return generatePublicKey(algorithm, keySpec);
    }

    /**
     * Generates an SM2 public key from its X and Y coordinates.
     *
     * @param x The byte array of the X coordinate.
     * @param y The byte array of the Y coordinate.
     * @return The {@link PublicKey} for SM2, or {@code null} if x or y are {@code null}.
     * @throws CryptoException if public key generation fails.
     */
    public static PublicKey generateSm2PublicKey(final byte[] x, final byte[] y) {
        if (null == x || null == y) {
            return null;
        }
        return generatePublicKey("sm2", getPublicKeySpec(x, y, Builder.SM2_EC_SPEC));
    }

    /**
     * Reads an X.509 certificate from an input stream. For more information on certificate files, see:
     * <a href="http://snowolf.iteye.com/blog/391931">Certificate File Usage</a>
     *
     * @param in The {@link InputStream} of the .cer file. Use {@link FileKit#getInputStream(File)} to read from a file.
     * @return The loaded {@link java.security.cert.Certificate}.
     * @throws CryptoException if certificate reading fails.
     */
    public static java.security.cert.Certificate readX509Certificate(final InputStream in) {
        return readCertificate(TYPE_X509, in);
    }

    /**
     * Reads an X.509 certificate from an input stream, requiring a password and alias for the KeyStore. For more
     * information on certificate files, see: <a href="http://snowolf.iteye.com/blog/391931">Certificate File Usage</a>
     *
     * @param in       The {@link InputStream} of the .cer file. Use {@link FileKit#getInputStream(File)} to read from a
     *                 file.
     * @param password The password for the KeyStore containing the certificate.
     * @param alias    The alias of the certificate in the KeyStore.
     * @return The loaded {@link java.security.cert.Certificate}.
     * @throws CryptoException if certificate reading fails.
     */
    public static java.security.cert.Certificate readX509Certificate(
            final InputStream in,
            final char[] password,
            final String alias) {
        return readCertificate(TYPE_X509, in, password, alias);
    }

    /**
     * Reads a certificate of a specified type from an input stream, requiring a password and alias for the KeyStore.
     * For more information on certificate files, see: <a href="http://snowolf.iteye.com/blog/391931">Certificate File
     * Usage</a>
     *
     * @param type     The type of the certificate (e.g., {@link #TYPE_X509}).
     * @param in       The {@link InputStream} to read the certificate from. Use {@link FileKit#getInputStream(File)} to
     *                 read from a file.
     * @param password The password for the KeyStore containing the certificate.
     * @param alias    The alias of the certificate in the KeyStore.
     * @return The loaded {@link java.security.cert.Certificate}.
     * @throws CryptoException if certificate reading fails.
     */
    public static java.security.cert.Certificate readCertificate(
            final String type,
            final InputStream in,
            final char[] password,
            final String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Reads a certificate of a specified type from an input stream. For more information on certificate files, see:
     * <a href="http://snowolf.iteye.com/blog/391931">Certificate File Usage</a>
     *
     * @param type The type of the certificate (e.g., {@link #TYPE_X509}).
     * @param in   The {@link InputStream} to read the certificate from. Use {@link FileKit#getInputStream(File)} to
     *             read from a file.
     * @return The loaded {@link java.security.cert.Certificate}.
     * @throws CryptoException if certificate reading fails.
     */
    public static java.security.cert.Certificate readCertificate(final String type, final InputStream in) {
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (final CertificateException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Retrieves a {@link java.security.cert.Certificate} from a {@link KeyStore} using an alias.
     *
     * @param keyStore The {@link KeyStore} containing the certificate.
     * @param alias    The alias of the certificate in the KeyStore.
     * @return The {@link java.security.cert.Certificate}.
     * @throws CryptoException if certificate retrieval fails.
     */
    public static java.security.cert.Certificate getCertificate(final KeyStore keyStore, final String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Retrieves a {@link CertificateFactory} instance for the given certificate type.
     *
     * @param type The type of the certificate (e.g., {@link #TYPE_X509}).
     * @return A {@link CertificateFactory} instance.
     * @throws CryptoException if the certificate type is not found.
     */
    public static CertificateFactory getCertificateFactory(final String type) {
        final Provider provider = Holder.getProvider();

        final CertificateFactory factory;
        try {
            factory = (null == provider) ? CertificateFactory.getInstance(type)
                    : CertificateFactory.getInstance(type, provider);
        } catch (final CertificateException e) {
            throw new CryptoException(e);
        }
        return factory;
    }

    /**
     * Checks if a given X.509 certificate is self-signed. A certificate is self-signed if it is signed by itself.
     *
     * @param cert The {@link X509Certificate} to check.
     * @return {@code true} if the certificate is self-signed, {@code false} otherwise.
     */
    public static boolean isSelfSigned(final X509Certificate cert) {
        return isSignedBy(cert, cert);
    }

    /**
     * Verifies if one X.509 certificate is signed by another (CA) certificate. This method checks if the CA
     * certificate's subject matches the end certificate's issuer and then verifies the end certificate's signature
     * using the CA certificate's public key. Source: sun.security.tools.KeyStoreUtil
     *
     * @param end The end-entity {@link X509Certificate} to be verified.
     * @param ca  The CA {@link X509Certificate} used for verification.
     * @return {@code true} if the end certificate is signed by the CA certificate, {@code false} otherwise.
     */
    public static boolean isSignedBy(final X509Certificate end, final X509Certificate ca) {
        // Check if the CA certificate's subject and the end certificate's issuer are the same
        if (!ca.getSubjectX500Principal().equals(end.getIssuerX500Principal())) {
            return false;
        }
        try {
            // Verify the end certificate using the CA certificate's public key
            end.verify(ca.getPublicKey());
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Retrieves an EC private key specification from a private key D value (byte array) and EC parameter specification.
     *
     * @param d             The private key D value as a byte array.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPrivateKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPrivateKeySpec getPrivateKeySpec(
            final byte[] d,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return getPrivateKeySpec(BigIntegers.fromUnsignedByteArray(d), parameterSpec);
    }

    /**
     * Retrieves an EC private key specification from a private key D value (BigInteger) and EC parameter specification.
     *
     * @param d             The private key D value as a {@link BigInteger}.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPrivateKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPrivateKeySpec getPrivateKeySpec(
            final BigInteger d,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return new org.bouncycastle.jce.spec.ECPrivateKeySpec(d, parameterSpec);
    }

    /**
     * Retrieves an EC public key specification from a public key Q value (byte array) and EC parameter specification.
     *
     * @param q             The public key Q value as a byte array.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPublicKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPublicKeySpec getPublicKeySpec(
            final byte[] q,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return getPublicKeySpec(parameterSpec.getCurve().decodePoint(q), parameterSpec);
    }

    /**
     * Retrieves an EC public key specification from public key X and Y coordinates (byte arrays) and EC parameter
     * specification.
     *
     * @param x             The public key X coordinate as a byte array.
     * @param y             The public key Y coordinate as a byte array.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPublicKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPublicKeySpec getPublicKeySpec(
            final byte[] x,
            final byte[] y,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return getPublicKeySpec(
                BigIntegers.fromUnsignedByteArray(x),
                BigIntegers.fromUnsignedByteArray(y),
                parameterSpec);
    }

    /**
     * Retrieves an EC public key specification from public key X and Y coordinates (BigIntegers) and EC parameter
     * specification.
     *
     * @param x             The public key X coordinate as a {@link BigInteger}.
     * @param y             The public key Y coordinate as a {@link BigInteger}.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPublicKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPublicKeySpec getPublicKeySpec(
            final BigInteger x,
            final BigInteger y,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return getPublicKeySpec(parameterSpec.getCurve().createPoint(x, y), parameterSpec);
    }

    /**
     * Retrieves an EC public key specification from an EC point and EC parameter specification.
     *
     * @param ecPoint       The {@code org.bouncycastle.math.ec.ECPoint} representing the public key coordinate.
     * @param parameterSpec The {@code org.bouncycastle.jce.spec.ECParameterSpec}.
     * @return The {@code org.bouncycastle.jce.spec.ECPublicKeySpec}.
     */
    public static org.bouncycastle.jce.spec.ECPublicKeySpec getPublicKeySpec(
            final org.bouncycastle.math.ec.ECPoint ecPoint,
            final org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return new org.bouncycastle.jce.spec.ECPublicKeySpec(ecPoint, parameterSpec);
    }

    /**
     * Creates an {@link OpenSSHPrivateKeySpec} from a byte array. The key is expected to be in PKCS#1 or OpenSSH
     * format.
     *
     * @param key The private key as a byte array.
     * @return A new {@link OpenSSHPrivateKeySpec}.
     */
    public static OpenSSHPrivateKeySpec getOpenSSHPrivateKeySpec(final byte[] key) {
        return new OpenSSHPrivateKeySpec(key);
    }

    /**
     * Creates an {@link OpenSSHPublicKeySpec} from a byte array. The key is expected to be in PKCS#1 format.
     *
     * @param key The public key as a byte array.
     * @return A new {@link OpenSSHPublicKeySpec}.
     */
    public static OpenSSHPublicKeySpec getOpenSSHPublicKeySpec(final byte[] key) {
        return new OpenSSHPublicKeySpec(key);
    }

}

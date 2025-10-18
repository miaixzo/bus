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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DefaultBufferedBlockCipher;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.Arrays;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.codec.binary.Hex;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.crypto.builtin.digest.mac.BCHMac;
import org.miaixz.bus.crypto.builtin.digest.mac.Mac;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.*;
import org.w3c.dom.Element;

/**
 * Security-related utility class.
 * <p>
 * This class provides various methods for cryptographic operations, categorized into three main types:
 * <ul>
 * <li>Symmetric encryption (e.g., AES, DES)</li>
 * <li>Asymmetric encryption (e.g., RSA, DSA)</li>
 * <li>Digest encryption (e.g., MD5, SHA-1, SHA-256, HMAC)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * The default curve name for SM2 algorithm.
     */
    public static final String SM2_CURVE_NAME = "sm2p256v1";
    /**
     * The elliptic curve parameters for SM2 algorithm.
     */
    public static final ECParameterSpec SM2_EC_SPEC = ECNamedCurveTable.getParameterSpec(SM2_CURVE_NAME);
    /**
     * Recommended SM2 curve parameters (from https://github.com/ZZMarquis/gmhelper).
     */
    public static final ECDomainParameters SM2_DOMAIN_PARAMS = toDomainParams(SM2_EC_SPEC);
    /**
     * OID identifier for the SM2 national cryptographic algorithm public key parameters.
     */
    public static final ASN1ObjectIdentifier ID_SM2_PUBLIC_KEY_PARAM = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
    /**
     * The length of R and S components in a signature.
     */
    private static final int RS_LEN = 32;
    /**
     * System property to control whether to decode Hex strings. Defaults to true.
     */
    public static String CRYPTO_DECODE_HEX = "bus.crypto.decodeHex";

    /**
     * Generates an algorithm string in the format "XXXwithXXX".
     *
     * @param asymmetricAlgorithm The asymmetric algorithm (e.g., RSA, DSA).
     * @param digestAlgorithm     The digest algorithm (e.g., SHA1, MD5). Can be {@code null} for "NONE".
     * @return The generated algorithm string.
     */
    public static String generateAlgorithm(final Algorithm asymmetricAlgorithm, final Algorithm digestAlgorithm) {
        final String digestPart = (null == digestAlgorithm) ? "NONE" : digestAlgorithm.name();
        return StringKit.format("{}with{}", digestPart, asymmetricAlgorithm.getValue());
    }

    /**
     * Creates an AES encryptor/decryptor with a randomly generated key. Note: The same {@link AES} object or the same
     * key must be used for decryption.
     * <p>
     * Example:
     *
     * <pre>
     * AES encryption: aes().encrypt(data)
     * AES decryption: aes().decrypt(data)
     * </pre>
     * 
     *
     * @return A new {@link AES} instance.
     */
    public static AES aes() {
        return new AES();
    }

    /**
     * Creates an AES encryptor/decryptor with the given key.
     * <p>
     * Example:
     *
     * <pre>
     * AES encryption: aes(key).encrypt(data)
     * AES decryption: aes(key).decrypt(data)
     * </pre>
     * 
     *
     * @param key The AES key.
     * @return A new {@link AES} instance.
     */
    public static AES aes(final byte[] key) {
        return new AES(key);
    }

    /**
     * Creates a DES encryptor/decryptor with a randomly generated key. Note: The same {@link DES} object or the same
     * key must be used for decryption.
     * <p>
     * Example:
     *
     * <pre>
     * DES encryption: des().encrypt(data)
     * DES decryption: des().decrypt(data)
     * </pre>
     * 
     *
     * @return A new {@link DES} instance.
     */
    public static DES des() {
        return new DES();
    }

    /**
     * Creates a DES encryptor/decryptor with the given key.
     * <p>
     * Example:
     *
     * <pre>
     * DES encryption: des(key).encrypt(data)
     * DES decryption: des(key).decrypt(data)
     * </pre>
     * 
     *
     * @param key The DES key.
     * @return A new {@link DES} instance.
     */
    public static DES des(final byte[] key) {
        return new DES(key);
    }

    /**
     * Creates a TDEA (Triple DES) encryptor/decryptor with a randomly generated key. Note: The same {@link TDEA} object
     * or the same key must be used for decryption. The default implementation in Java is DESede/ECB/PKCS5Padding.
     * <p>
     * Example:
     *
     * <pre>
     * DESede encryption: tdea().encrypt(data)
     * DESede decryption: tdea().decrypt(data)
     * </pre>
     * 
     *
     * @return A new {@link TDEA} instance.
     */
    public static TDEA tdea() {
        return new TDEA();
    }

    /**
     * Creates a TDEA (Triple DES) encryptor/decryptor with the given key. Note: The same {@link TDEA} object or the
     * same key must be used for decryption. The default implementation in Java is DESede/ECB/PKCS5Padding.
     * <p>
     * Example:
     *
     * <pre>
     * DESede encryption: tdea(key).encrypt(data)
     * DESede decryption: tdea(key).decrypt(data)
     * </pre>
     * 
     *
     * @param key The TDEA key.
     * @return A new {@link TDEA} instance.
     */
    public static TDEA tdea(final byte[] key) {
        return new TDEA(key);
    }

    /**
     * Creates an MD5 digester.
     * <p>
     * Example:
     *
     * <pre>
     * MD5 digest: md5().digest(data)
     * MD5 digest to hexadecimal string: md5().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link MD5} instance.
     */
    public static MD5 md5() {
        return MD5.of();
    }

    /**
     * Performs MD5 encryption and returns the hexadecimal MD5 string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal MD5 string.
     */
    public static String md5(final String data) {
        return MD5.of().digestHex(data);
    }

    /**
     * Performs MD5 encryption on an input stream and returns the hexadecimal MD5 string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal MD5 string.
     */
    public static String md5(final InputStream data) {
        return MD5.of().digestHex(data);
    }

    /**
     * Performs MD5 encryption on a file and returns the hexadecimal MD5 string.
     *
     * @param dataFile The file to be digested.
     * @return The hexadecimal MD5 string.
     */
    public static String md5(final File dataFile) {
        return MD5.of().digestHex(dataFile);
    }

    /**
     * Creates a SHA1 digester.
     * <p>
     * Example:
     *
     * <pre>
     * SHA1 digest: sha1().digest(data)
     * SHA1 digest to hexadecimal string: sha1().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link Digester} instance for SHA1.
     */
    public static Digester sha1() {
        return new Digester(Algorithm.SHA1);
    }

    /**
     * Performs SHA1 encryption and returns the hexadecimal SHA1 string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal SHA1 string.
     */
    public static String sha1(final String data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * Performs SHA1 encryption on an input stream and returns the hexadecimal SHA1 string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal SHA1 string.
     */
    public static String sha1(final InputStream data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * Performs SHA1 encryption on a file and returns the hexadecimal SHA1 string.
     *
     * @param dataFile The file to be digested.
     * @return The hexadecimal SHA1 string.
     */
    public static String sha1(final File dataFile) {
        return new Digester(Algorithm.SHA1).digestHex(dataFile);
    }

    /**
     * Creates a SHA256 digester.
     * <p>
     * Example:
     *
     * <pre>
     * SHA256 digest: sha256().digest(data)
     * SHA256 digest to hexadecimal string: sha256().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link Digester} instance for SHA256.
     */
    public static Digester sha256() {
        return new Digester(Algorithm.SHA256);
    }

    /**
     * Performs SHA256 encryption and returns the hexadecimal SHA256 string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal SHA256 string.
     */
    public static String sha256(final String data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * Performs SHA256 encryption on an input stream and returns the hexadecimal SHA256 string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal SHA256 string.
     */
    public static String sha256(final InputStream data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * Performs SHA256 encryption on a file and returns the hexadecimal SHA256 string.
     *
     * @param dataFile The file to be digested.
     * @return The hexadecimal SHA256 string.
     */
    public static String sha256(final File dataFile) {
        return new Digester(Algorithm.SHA256).digestHex(dataFile);
    }

    /**
     * Creates an HMac object with the specified algorithm and key. Calling the {@code digest} method on the returned
     * object will yield the HMAC value.
     *
     * @param algorithm The {@link Algorithm} for HMAC.
     * @param key       The key as a string. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance.
     */
    public static HMac hmac(final Algorithm algorithm, final String key) {
        return new HMac(algorithm, ByteKit.toBytes(key));
    }

    /**
     * Creates an HMac object with the specified algorithm and key. Calling the {@code digest} method on the returned
     * object will yield the HMAC value.
     *
     * @param algorithm The {@link Algorithm} for HMAC.
     * @param key       The key as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance.
     */
    public static HMac hmac(final Algorithm algorithm, final byte[] key) {
        return new HMac(algorithm, key);
    }

    /**
     * Creates an HMac object with the specified algorithm and {@link SecretKey}. Calling the {@code digest} method on
     * the returned object will yield the HMAC value.
     *
     * @param algorithm The {@link Algorithm} for HMAC.
     * @param key       The {@link SecretKey}. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance.
     */
    public static HMac hmac(final Algorithm algorithm, final SecretKey key) {
        return new HMac(algorithm, key);
    }

    /**
     * Creates an HmacMD5 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacMD5 digest: hmacMd5(key).digest(data)
     * HmacMD5 digest to hexadecimal string: hmacMd5(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a string. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacMD5.
     */
    public static HMac hmacMd5(final String key) {
        return hmacMd5(ByteKit.toBytes(key));
    }

    /**
     * Creates an HmacMD5 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacMD5 digest: hmacMd5(key).digest(data)
     * HmacMD5 digest to hexadecimal string: hmacMd5(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacMD5.
     */
    public static HMac hmacMd5(final byte[] key) {
        return new HMac(Algorithm.HMACMD5, key);
    }

    /**
     * Creates an HmacMD5 encryptor with a randomly generated key.
     * <p>
     * Example:
     *
     * <pre>
     * HmacMD5 digest: hmacMd5().digest(data)
     * HmacMD5 digest to hexadecimal string: hmacMd5().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link HMac} instance for HmacMD5.
     */
    public static HMac hmacMd5() {
        return new HMac(Algorithm.HMACMD5);
    }

    /**
     * Creates an HmacSHA1 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA1 digest: hmacSha1(key).digest(data)
     * HmacSHA1 digest to hexadecimal string: hmacSha1(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a string. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacSHA1.
     */
    public static HMac hmacSha1(final String key) {
        return hmacSha1(ByteKit.toBytes(key));
    }

    /**
     * Creates an HmacSHA1 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA1 digest: hmacSha1(key).digest(data)
     * HmacSHA1 digest to hexadecimal string: hmacSha1(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacSHA1.
     */
    public static HMac hmacSha1(final byte[] key) {
        return new HMac(Algorithm.HMACSHA1, key);
    }

    /**
     * Creates an HmacSHA1 encryptor with a randomly generated key.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA1 digest: hmacSha1().digest(data)
     * HmacSHA1 digest to hexadecimal string: hmacSha1().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link HMac} instance for HmacSHA1.
     */
    public static HMac hmacSha1() {
        return new HMac(Algorithm.HMACSHA1);
    }

    /**
     * Creates an HmacSHA256 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA256 digest: hmacSha256(key).digest(data)
     * HmacSHA256 digest to hexadecimal string: hmacSha256(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a string. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacSHA256.
     */
    public static HMac hmacSha256(final String key) {
        return hmacSha256(ByteKit.toBytes(key));
    }

    /**
     * Creates an HmacSHA256 encryptor.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA256 digest: hmacSha256(key).digest(data)
     * HmacSHA256 digest to hexadecimal string: hmacSha256(key).digestHex(data)
     * </pre>
     * 
     *
     * @param key The encryption key as a byte array. If {@code null}, a random key is generated.
     * @return A new {@link HMac} instance for HmacSHA256.
     */
    public static HMac hmacSha256(final byte[] key) {
        return new HMac(Algorithm.HMACSHA256, key);
    }

    /**
     * Creates an HmacSHA256 encryptor with a randomly generated key.
     * <p>
     * Example:
     *
     * <pre>
     * HmacSHA256 digest: hmacSha256().digest(data)
     * HmacSHA256 digest to hexadecimal string: hmacSha256().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link HMac} instance for HmacSHA256.
     */
    public static HMac hmacSha256() {
        return new HMac(Algorithm.HMACSHA256);
    }

    /**
     * Creates an RSA algorithm object, generating a new public-private key pair.
     *
     * @return A new {@link RSA} instance.
     */
    public static RSA rsa() {
        return new RSA();
    }

    /**
     * Creates an RSA algorithm object with the given private and public keys (Base64 encoded). If both private and
     * public keys are {@code null}, a new key pair will be generated. If only one key is provided, the RSA object can
     * only be used for operations corresponding to that key (e.g., encryption with public key, decryption with private
     * key).
     *
     * @param privateKeyBase64 The Base64 encoded private key string.
     * @param publicKeyBase64  The Base64 encoded public key string.
     * @return A new {@link RSA} instance.
     */
    public static RSA rsa(final String privateKeyBase64, final String publicKeyBase64) {
        return new RSA(privateKeyBase64, publicKeyBase64);
    }

    /**
     * Creates an RSA algorithm object with the given private and public keys (byte arrays). If both private and public
     * keys are {@code null}, a new key pair will be generated. If only one key is provided, the RSA object can only be
     * used for operations corresponding to that key (e.g., encryption with public key, decryption with private key).
     *
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     * @return A new {@link RSA} instance.
     */
    public static RSA rsa(final byte[] privateKey, final byte[] publicKey) {
        return new RSA(privateKey, publicKey);
    }

    /**
     * Adds a cryptographic algorithm provider. Providers added this way will be prioritized.
     * <p>
     * Example:
     *
     * <pre>
     * addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
     * </pre>
     * 
     *
     * @param provider The {@link java.security.Provider} to add.
     */
    public static void addProvider(final java.security.Provider provider) {
        if (ArrayKit.contains(Security.getProviders(), provider)) {
            // If the provider is already registered, do not re-register
            return;
        }
        Security.insertProviderAt(provider, 0);
    }

    /**
     * Decodes a string key, supporting the following encodings:
     * <ul>
     * <li>Hexadecimal encoding</li>
     * <li>Base64 encoding</li>
     * </ul>
     * <p>
     * Note: For some special strings, it might be difficult to distinguish between Hex and Base64. The system property
     * {@link #CRYPTO_DECODE_HEX} can be used to force disable Hex parsing.
     * 
     *
     * @param key The key string to be decoded.
     * @return The decoded key as a byte array.
     * @throws IllegalArgumentException if the value is neither hex nor base64, or if the key is null.
     */
    public static byte[] decode(final String key) {
        if (Objects.isNull(key)) {
            return null;
        }

        // Some special strings cannot distinguish between Hex and Base64. Here, use system properties to force disable
        // Hex parsing.
        final boolean decodeHex = Keys.getBoolean(CRYPTO_DECODE_HEX, true);
        if (decodeHex && Validator.isHex(key)) {
            return Hex.decode(key);
        } else if (Base64.isTypeBase64(key)) {
            return Base64.decode(key);
        }
        throw new IllegalArgumentException("Value is not hex or base64!");
    }

    /**
     * Creates a {@link Cipher} instance for the given algorithm. If the provider is {@code null}, it attempts to find a
     * provider using {@link Holder}; if no provider is found, the JDK's default provider is used.
     *
     * @param algorithm The algorithm name.
     * @return A new {@link Cipher} instance.
     * @throws CryptoException if the algorithm is not found or cipher creation fails.
     */
    public static Cipher createCipher(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final Cipher cipher;
        try {
            cipher = (null == provider) ? Cipher.getInstance(algorithm) : Cipher.getInstance(algorithm, provider);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }

        return cipher;
    }

    /**
     * Creates a {@link MessageDigest} instance for the given algorithm. If the provided {@code provider} is
     * {@code null}, it attempts to find a provider using {@link Holder}; if no provider is found, the JDK's default
     * provider is used.
     *
     * @param algorithm The algorithm name.
     * @param provider  The {@link java.security.Provider} to use. If {@code null}, {@link Holder} is used to find one.
     * @return A new {@link MessageDigest} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static MessageDigest createMessageDigest(final String algorithm, java.security.Provider provider) {
        if (null == provider) {
            provider = Holder.getProvider();
        }

        final MessageDigest messageDigest;
        try {
            messageDigest = (null == provider) ? MessageDigest.getInstance(algorithm)
                    : MessageDigest.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return messageDigest;
    }

    /**
     * Creates a {@link MessageDigest} instance for the given algorithm using the JDK's default provider.
     *
     * @param algorithm The algorithm name.
     * @return A new {@link MessageDigest} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static MessageDigest createJdkMessageDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Creates a {@link javax.crypto.Mac} instance for the given algorithm. If the provider is {@code null}, it attempts
     * to find a provider using {@link Holder}; if no provider is found, the JDK's default provider is used.
     *
     * @param algorithm The algorithm name.
     * @return A new {@link javax.crypto.Mac} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static javax.crypto.Mac createMac(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final javax.crypto.Mac mac;
        try {
            mac = (null == provider) ? javax.crypto.Mac.getInstance(algorithm)
                    : javax.crypto.Mac.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return mac;
    }

    /**
     * Creates an RC4 encryptor/decryptor with the given key.
     *
     * @param key The RC4 key.
     * @return A new {@link Crypto} instance configured for RC4.
     */
    public static Crypto rc4(final byte[] key) {
        return new Crypto(Algorithm.RC4, key);
    }

    /**
     * Forcibly disables the use of custom {@link java.security.Provider}s, such as Bouncy Castle. This setting is
     * global and affects all subsequent cryptographic operations.
     */
    public static void disableCustomProvider() {
        Holder.setUseCustomProvider(false);
    }

    /**
     * Performs PBKDF2 password encryption.
     *
     * @param password The plaintext password as a character array.
     * @param salt     The salt as a byte array, typically 16 bytes long.
     * @return The encrypted password as a hexadecimal string.
     */
    public static String pbkdf2(final char[] password, final byte[] salt) {
        return new PBKDF2().encryptHex(password, salt);
    }

    /**
     * Creates an FPE (Format Preserving Encryption) instance, supporting FF1 and FF3-1 modes.
     *
     * @param mode   The FPE mode enumeration, either FF1 or FF3-1.
     * @param key    The encryption key. If {@code null}, a random key is generated. The key length must be 16, 24, or
     *               32 bits.
     * @param mapper The {@link AlphabetMapper} defining the character set for encryption. The characters to be
     *               encrypted must match this mapping (e.g., digits for phone numbers, bank card numbers).
     * @param tweak  The tweak value, used to prevent collisions in partial encryption. Typically, immutable parts of
     *               the data are used as the tweak.
     * @return A new {@link FPE} instance.
     */
    public static FPE fpe(final FPE.FPEMode mode, final byte[] key, final AlphabetMapper mapper, final byte[] tweak) {
        return new FPE(mode, key, mapper, tweak);
    }

    /**
     * Creates a ZUC-128 algorithm implementation (based on BouncyCastle).
     *
     * @param key The encryption key.
     * @param iv  The initialization vector (IV) or salt, 16 bytes long. If {@code null}, a random IV is generated.
     * @return A new {@link ZUC} instance configured for ZUC-128.
     */
    public static ZUC zuc128(final byte[] key, final byte[] iv) {
        return new ZUC(Algorithm.ZUC_128, key, iv);
    }

    /**
     * Creates a ZUC-256 algorithm implementation (based on BouncyCastle).
     *
     * @param key The encryption key.
     * @param iv  The initialization vector (IV) or salt, 25 bytes long. If {@code null}, a random IV is generated.
     * @return A new {@link ZUC} instance configured for ZUC-256.
     */
    public static ZUC zuc256(final byte[] key, final byte[] iv) {
        return new ZUC(Algorithm.ZUC_256, key, iv);
    }

    /**
     * Creates an SM2 algorithm object, generating a new public-private key pair.
     *
     * @return A new {@link SM2} instance.
     */
    public static SM2 sm2() {
        return new SM2();
    }

    /**
     * Creates an SM2 algorithm object with the given private and public keys (Hex or Base64 encoded). If both private
     * and public keys are {@code null}, a new key pair will be generated. If only one key is provided, the SM2 object
     * can only be used for operations corresponding to that key.
     *
     * @param privateKey The private key string (Hex or Base64 encoded).
     * @param publicKey  The public key string (Hex or Base64 encoded).
     * @return A new {@link SM2} instance.
     */
    public static SM2 sm2(final String privateKey, final String publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * Creates an SM2 algorithm object with the given private and public keys (byte arrays). If both private and public
     * keys are {@code null}, a new key pair will be generated. If only one key is provided, the SM2 object can only be
     * used for operations corresponding to that key.
     *
     * @param privateKey The private key as a byte array, expected in PKCS#8 format.
     * @param publicKey  The public key as a byte array, expected in X.509 format.
     * @return A new {@link SM2} instance.
     */
    public static SM2 sm2(final byte[] privateKey, final byte[] publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * Creates an SM2 algorithm object with the given private and public keys. If both private and public keys are
     * {@code null}, a new key pair will be generated. If only one key is provided, the SM2 object can only be used for
     * operations corresponding to that key.
     *
     * @param privateKey The {@link PrivateKey}.
     * @param publicKey  The {@link PublicKey}.
     * @return A new {@link SM2} instance.
     */
    public static SM2 sm2(final PrivateKey privateKey, final PublicKey publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * Creates an SM2 algorithm object with the given private and public key parameters. If both private and public key
     * parameters are {@code null}, a new key pair will be generated. If only one key parameter is provided, the SM2
     * object can only be used for operations corresponding to that key.
     *
     * @param privateKeyParams The {@link ECPrivateKeyParameters}.
     * @param publicKeyParams  The {@link ECPublicKeyParameters}.
     * @return A new {@link SM2} instance.
     */
    public static SM2 sm2(final ECPrivateKeyParameters privateKeyParams, final ECPublicKeyParameters publicKeyParams) {
        return new SM2(privateKeyParams, publicKeyParams);
    }

    /**
     * Creates an SM3 digester.
     * <p>
     * Example:
     *
     * <pre>
     * SM3 digest: sm3().digest(data)
     * SM3 digest to hexadecimal string: sm3().digestHex(data)
     * </pre>
     * 
     *
     * @return A new {@link SM3} instance.
     */
    public static SM3 sm3() {
        return new SM3();
    }

    /**
     * Creates an SM3 digester with a specified salt.
     *
     * @param salt The salt to be used for SM3 digestion.
     * @return A new {@link SM3} instance with the given salt.
     */
    public static SM3 sm3WithSalt(final byte[] salt) {
        return new SM3(salt);
    }

    /**
     * Performs SM3 encryption and returns the hexadecimal SM3 string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal SM3 string.
     */
    public static String sm3(final String data) {
        return sm3().digestHex(data);
    }

    /**
     * Performs SM3 encryption on an input stream and returns the hexadecimal SM3 string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal SM3 string.
     */
    public static String sm3(final InputStream data) {
        return sm3().digestHex(data);
    }

    /**
     * Performs SM3 encryption on a file and returns the hexadecimal SM3 string.
     *
     * @param dataFile The file to be digested.
     * @return The hexadecimal SM3 string.
     */
    public static String sm3(final File dataFile) {
        return sm3().digestHex(dataFile);
    }

    /**
     * Creates an SM4 encryptor/decryptor with a randomly generated key. Note: The same {@link Crypto} object or the
     * same key must be used for decryption.
     * <p>
     * Example:
     *
     * <pre>
     * SM4 encryption: sm4().encrypt(data)
     * SM4 decryption: sm4().decrypt(data)
     * </pre>
     * 
     *
     * @return A new {@link SM4} instance.
     */
    public static SM4 sm4() {
        return new SM4();
    }

    /**
     * Creates an SM4 encryptor/decryptor with the given key.
     * <p>
     * Example:
     *
     * <pre>
     * SM4 encryption: sm4(key).encrypt(data)
     * SM4 decryption: sm4(key).decrypt(data)
     * </pre>
     * 
     *
     * @param key The SM4 key.
     * @return A new {@link SM4} instance.
     */
    public static SM4 sm4(final byte[] key) {
        return new SM4(key);
    }

    /**
     * Converts the SM2 encrypted data from C1C2C3 order to C1C3C2 order. This method is called after encryption when
     * using BouncyCastle's old standard (C1C2C3).
     *
     * @param c1c2c3             The encrypted bytes in C1C2C3 order.
     * @param ecDomainParameters The {@link ECDomainParameters} of the elliptic curve.
     * @return The encrypted bytes in C1C3C2 order.
     */
    public static byte[] changeC1C2C3ToC1C3C2(final byte[] c1c2c3, final ECDomainParameters ecDomainParameters) {
        // For sm2p256v1, this is fixed at 65. See GMNamedCurves, ECCurve code.
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        final byte[] result = new byte[c1c2c3.length];
        System.arraycopy(c1c2c3, 0, result, 0, c1Len); // c1
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len); // c3
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len); // c2
        return result;
    }

    /**
     * Converts the SM2 encrypted data from C1C3C2 order to C1C2C3 order before decryption. This method is called before
     * decryption when using BouncyCastle's old standard (C1C3C2).
     *
     * @param c1c3c2             The encrypted bytes in C1C3C2 order.
     * @param ecDomainParameters The {@link ECDomainParameters} of the elliptic curve.
     * @return The encrypted bytes in C1C2C3 order.
     */
    public static byte[] changeC1C3C2ToC1C2C3(final byte[] c1c3c2, final ECDomainParameters ecDomainParameters) {
        // For sm2p256v1, this is fixed at 65. See GMNamedCurves, ECCurve code.
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        final byte[] result = new byte[c1c3c2.length];
        System.arraycopy(c1c3c2, 0, result, 0, c1Len); // c1: 0->65
        System.arraycopy(c1c3c2, c1Len + c3Len, result, c1Len, c1c3c2.length - c1Len - c3Len); // c2
        System.arraycopy(c1c3c2, c1Len, result, c1c3c2.length - c3Len, c3Len); // c3
        return result;
    }

    /**
     * Converts an SM3withSM2 signature result from ASN.1 format to a plain R||S byte array.
     *
     * @param rsDer The signature result in ASN.1 format.
     * @return The signature result as a plain byte array (R concatenated with S).
     * @throws InternalException if an I/O error occurs during decoding.
     */
    public static byte[] rsAsn1ToPlain(final byte[] rsDer) {
        final BigInteger[] decode;
        try {
            decode = StandardDSAEncoding.INSTANCE.decode(SM2_DOMAIN_PARAMS.getN(), rsDer);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        final byte[] r = toFixedLengthBytes(decode[0]);
        final byte[] s = toFixedLengthBytes(decode[1]);

        return ArrayKit.addAll(r, s);
    }

    /**
     * Converts a plain R||S byte array to an SM3withSM2 signature result in ASN.1 format. This method is used before
     * signature verification when the input is a plain R||S byte array.
     *
     * @param sign The signature result as a plain byte array (R concatenated with S).
     * @return The signature result in ASN.1 format.
     * @throws CryptoException   if the input signature length is not 2 * {@link #RS_LEN}.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static byte[] rsPlainToAsn1(final byte[] sign) {
        if (sign.length != RS_LEN * 2) {
            throw new CryptoException("err rs. ");
        }
        final BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        final BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        try {
            return StandardDSAEncoding.INSTANCE.encode(SM2_DOMAIN_PARAMS.getN(), r, s);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an HmacSM3 algorithm {@link Mac} instance.
     *
     * @param key The key for HmacSM3.
     * @return A new {@link Mac} instance for HmacSM3.
     */
    public static Mac createHmacSm3Engine(final byte[] key) {
        return new BCHMac(new SM3Digest(), key);
    }

    /**
     * Creates an HmacSM3 algorithm {@link HMac} object. Call {@code digestXXX} methods on the returned object to get
     * the HMAC-SM3 value.
     *
     * @param key The key for HmacSM3.
     * @return A new {@link HMac} instance for HmacSM3.
     */
    public static HMac hmacSm3(final byte[] key) {
        return new HMac(Algorithm.HMACSM3, key);
    }

    /**
     * Converts a {@link BigInteger} to a fixed-length byte array. For SM2p256v1, the 'n' value is
     * 00fffffffeffffffffffffffffffffffff7203df6b21c6052b53bbf40939d54123, and 'r' and 's' are results of modulo 'n', so
     * their length should be less than or equal to 32 bytes.
     *
     * @param rOrS The {@link BigInteger} representing 'r' or 's'.
     * @return A fixed-length byte array (32 bytes) representing the BigInteger.
     * @throws CryptoException if the length of the BigInteger is invalid.
     */
    private static byte[] toFixedLengthBytes(final BigInteger rOrS) {
        // for sm2p256v1, n is 00fffffffeffffffffffffffffffffffff7203df6b21c6052b53bbf40939d54123,
        // r and s are the result of mod n, so they should be less than n and have length<=32
        final byte[] rs = rOrS.toByteArray();
        if (rs.length == RS_LEN) {
            return rs;
        } else if (rs.length == RS_LEN + 1 && rs[0] == 0) {
            return Arrays.copyOfRange(rs, 1, RS_LEN + 1);
        } else if (rs.length < RS_LEN) {
            final byte[] result = new byte[RS_LEN];
            Arrays.fill(result, (byte) 0);
            System.arraycopy(rs, 0, result, RS_LEN - rs.length, rs.length);
            return result;
        } else {
            throw new CryptoException("Error rs: {}", org.bouncycastle.util.encoders.Hex.toHexString(rs));
        }
    }

    /**
     * Generates a {@link Signature} object for asymmetric encryption.
     *
     * @param asymmetricAlgorithm The asymmetric encryption {@link Algorithm}.
     * @param digestAlgorithm     The digest {@link Algorithm}.
     * @return A new {@link Signature} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static Signature createSignature(final Algorithm asymmetricAlgorithm, final Algorithm digestAlgorithm) {
        return createSignature(generateAlgorithm(asymmetricAlgorithm, digestAlgorithm));
    }

    /**
     * Creates a {@link Signature} object for the given algorithm.
     *
     * @param algorithm The algorithm name.
     * @return A new {@link Signature} instance.
     * @throws CryptoException if the algorithm is not found.
     */
    public static Signature createSignature(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final Signature signature;
        try {
            signature = (null == provider) ? Signature.getInstance(algorithm)
                    : Signature.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return signature;
    }

    /**
     * Creates a {@link Sign} algorithm object, generating a new public-private key pair.
     *
     * @param algorithm The signing algorithm.
     * @return A new {@link Sign} instance.
     */
    public static Sign sign(final Algorithm algorithm) {
        return new Sign(algorithm);
    }

    /**
     * Creates a {@link Sign} algorithm object with the given private and public keys (Base64 encoded). If both private
     * and public keys are {@code null}, a new key pair will be generated. If only one key is provided, the Sign object
     * can only be used for operations corresponding to that key.
     *
     * @param algorithm        The signing algorithm.
     * @param privateKeyBase64 The Base64 encoded private key string.
     * @param publicKeyBase64  The Base64 encoded public key string.
     * @return A new {@link Sign} instance.
     */
    public static Sign sign(final Algorithm algorithm, final String privateKeyBase64, final String publicKeyBase64) {
        return new Sign(algorithm, privateKeyBase64, publicKeyBase64);
    }

    /**
     * Creates a {@link Sign} algorithm object with the given private and public keys (byte arrays). If both private and
     * public keys are {@code null}, a new key pair will be generated. If only one key is provided, the Sign object can
     * only be used for operations corresponding to that key.
     *
     * @param algorithm  The algorithm enumeration.
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     * @return A new {@link Sign} instance.
     */
    public static Sign sign(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        return new Sign(algorithm, privateKey, publicKey);
    }

    /**
     * Signs parameters by sorting them by key, concatenating them into a string, and then encrypting the string using
     * the provided symmetric encryption algorithm. Key-value pairs are concatenated without separators, and null values
     * are ignored.
     *
     * @param crypto      The symmetric encryption algorithm to use for signing.
     * @param params      The map of parameters to sign.
     * @param otherParams Additional parameter strings (e.g., a secret key) to append to the concatenated string.
     * @return The generated signature string.
     */
    public static String signParams(final Crypto crypto, final Map<?, ?> params, final String... otherParams) {
        return signParams(crypto, params, Normal.EMPTY, Normal.EMPTY, true, otherParams);
    }

    /**
     * Signs parameters by sorting them by key, concatenating them into a string, and then encrypting the string using
     * the provided symmetric encryption algorithm.
     *
     * @param crypto            The symmetric encryption algorithm to use for signing.
     * @param params            The map of parameters to sign.
     * @param separator         The separator string to use between key-value entries.
     * @param keyValueSeparator The separator string to use between keys and values.
     * @param isIgnoreNull      Whether to ignore null keys and values during concatenation.
     * @param otherParams       Additional parameter strings (e.g., a secret key) to append to the concatenated string.
     * @return The generated signature string.
     */
    public static String signParams(
            final Crypto crypto,
            final Map<?, ?> params,
            final String separator,
            final String keyValueSeparator,
            final boolean isIgnoreNull,
            final String... otherParams) {
        return crypto.encryptHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }

    /**
     * Performs MD5 signing on parameters. Parameters are sorted by key, concatenated into a string (without separators
     * between key-value pairs), and then the MD5 hash is generated. Null values are ignored.
     *
     * @param params      The map of parameters to sign.
     * @param otherParams Additional parameter strings (e.g., a secret key) to append.
     * @return The generated MD5 signature string.
     */
    public static String signParamsMd5(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.MD5, params, otherParams);
    }

    /**
     * Performs SHA1 signing on parameters. Parameters are sorted by key, concatenated into a string (without separators
     * between key-value pairs), and then the SHA1 hash is generated. Null values are ignored.
     *
     * @param params      The map of parameters to sign.
     * @param otherParams Additional parameter strings (e.g., a secret key) to append.
     * @return The generated SHA1 signature string.
     */
    public static String signParamsSha1(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.SHA1, params, otherParams);
    }

    /**
     * Performs SHA256 signing on parameters. Parameters are sorted by key, concatenated into a string (without
     * separators between key-value pairs), and then the SHA256 hash is generated. Null values are ignored.
     *
     * @param params      The map of parameters to sign.
     * @param otherParams Additional parameter strings (e.g., a secret key) to append.
     * @return The generated SHA256 signature string.
     */
    public static String signParamsSha256(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.SHA256, params, otherParams);
    }

    /**
     * Signs parameters using the specified digest algorithm. Parameters are sorted by key, concatenated into a string
     * (without separators between key-value pairs), and then the digest hash is generated. Null values are ignored.
     *
     * @param digestAlgorithm The digest algorithm to use for signing.
     * @param params          The map of parameters to sign.
     * @param otherParams     Additional parameter strings (e.g., a secret key) to append.
     * @return The generated signature string.
     */
    public static String signParams(
            final Algorithm digestAlgorithm,
            final Map<?, ?> params,
            final String... otherParams) {
        return signParams(digestAlgorithm, params, Normal.EMPTY, Normal.EMPTY, true, otherParams);
    }

    /**
     * Signs parameters using the specified digest algorithm. Parameters are sorted by key, concatenated into a string,
     * and then the digest hash is generated.
     *
     * @param digestAlgorithm   The digest algorithm to use for signing.
     * @param params            The map of parameters to sign.
     * @param separator         The separator string to use between key-value entries.
     * @param keyValueSeparator The separator string to use between keys and values.
     * @param isIgnoreNull      Whether to ignore null keys and values during concatenation.
     * @param otherParams       Additional parameter strings (e.g., a secret key) to append.
     * @return The generated signature string.
     */
    public static String signParams(
            final Algorithm digestAlgorithm,
            final Map<?, ?> params,
            final String separator,
            final String keyValueSeparator,
            final boolean isIgnoreNull,
            final String... otherParams) {
        return new Digester(digestAlgorithm)
                .digestHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }

    /**
     * Computes the 32-bit MD5 digest of the given data.
     *
     * @param data The data to be digested.
     * @return The MD5 digest as a byte array.
     */
    public static byte[] md5(final byte[] data) {
        return MD5.of().digest(data);
    }

    /**
     * Computes the 32-bit MD5 digest of the given string using the specified charset.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The MD5 digest as a byte array.
     */
    public static byte[] md5(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digest(data, charset);
    }

    /**
     * Computes the 32-bit MD5 digest of the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal representation of the MD5 digest.
     */
    public static String md5Hex(final byte[] data) {
        return MD5.of().digestHex(data);
    }

    /**
     * Computes the 32-bit MD5 digest of the given string using the specified charset and returns it as a hexadecimal
     * string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal representation of the MD5 digest.
     */
    public static String md5Hex(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digestHex(data, charset);
    }

    /**
     * Computes the 32-bit MD5 digest of the given string using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal representation of the MD5 digest.
     */
    public static String md5Hex(final String data) {
        return md5Hex(data, Charset.UTF_8);
    }

    /**
     * Computes the 32-bit MD5 digest of the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal representation of the MD5 digest.
     */
    public static String md5Hex(final InputStream data) {
        return MD5.of().digestHex(data);
    }

    /**
     * Computes the 32-bit MD5 digest of the given file and returns it as a hexadecimal string.
     *
     * @param file The file to be digested.
     * @return The hexadecimal representation of the MD5 digest.
     */
    public static String md5Hex(final File file) {
        return MD5.of().digestHex(file);
    }

    /**
     * Computes the 16-bit MD5 digest of the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal representation of the 16-bit MD5 digest.
     */
    public static String md5Hex16(final byte[] data) {
        return MD5.of().digestHex16(data);
    }

    /**
     * Computes the 16-bit MD5 digest of the given string using the specified charset and returns it as a hexadecimal
     * string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal representation of the 16-bit MD5 digest.
     */
    public static String md5Hex16(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digestHex16(data, charset);
    }

    /**
     * Computes the 16-bit MD5 digest of the given string using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal representation of the 16-bit MD5 digest.
     */
    public static String md5Hex16(final String data) {
        return md5Hex16(data, Charset.UTF_8);
    }

    /**
     * Computes the 16-bit MD5 digest of the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal representation of the 16-bit MD5 digest.
     */
    public static String md5Hex16(final InputStream data) {
        return MD5.of().digestHex16(data);
    }

    /**
     * Computes the 16-bit MD5 digest of the given file and returns it as a hexadecimal string.
     *
     * @param file The file to be digested.
     * @return The hexadecimal representation of the 16-bit MD5 digest.
     */
    public static String md5Hex16(final File file) {
        return MD5.of().digestHex16(file);
    }

    /**
     * Converts a 32-bit MD5 hexadecimal string to a 16-bit MD5 hexadecimal string.
     *
     * @param md5Hex The 32-bit MD5 hexadecimal string.
     * @return The 16-bit MD5 hexadecimal string.
     */
    public static String md5HexTo16(final String md5Hex) {
        return md5Hex.substring(8, 24);
    }

    /**
     * Computes the SHA-1 digest of the given data.
     *
     * @param data The data to be digested.
     * @return The SHA-1 digest as a byte array.
     */
    public static byte[] sha1(final byte[] data) {
        return digester(Algorithm.SHA1).digest(data);
    }

    /**
     * Computes the SHA-1 digest of the given string using the specified charset.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The SHA-1 digest as a byte array.
     */
    public static byte[] sha1(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA1).digest(data, charset);
    }

    /**
     * Computes the SHA-1 digest of the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal representation of the SHA-1 digest.
     */
    public static String sha1Hex(final byte[] data) {
        return digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * Computes the SHA-1 digest of the given string using the specified charset and returns it as a hexadecimal string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal representation of the SHA-1 digest.
     */
    public static String sha1Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA1).digestHex(data, charset);
    }

    /**
     * Computes the SHA-1 digest of the given string using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal representation of the SHA-1 digest.
     */
    public static String sha1Hex(final String data) {
        return sha1Hex(data, Charset.UTF_8);
    }

    /**
     * Computes the SHA-1 digest of the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal representation of the SHA-1 digest.
     */
    public static String sha1Hex(final InputStream data) {
        return digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * Computes the SHA-1 digest of the given file and returns it as a hexadecimal string.
     *
     * @param file The file to be digested.
     * @return The hexadecimal representation of the SHA-1 digest.
     */
    public static String sha1Hex(final File file) {
        return digester(Algorithm.SHA1).digestHex(file);
    }

    /**
     * Computes the SHA-256 digest of the given data.
     *
     * @param data The data to be digested.
     * @return The SHA-256 digest as a byte array.
     */
    public static byte[] sha256(final byte[] data) {
        return digester(Algorithm.SHA256).digest(data);
    }

    /**
     * Computes the SHA-256 digest of the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal representation of the SHA-256 digest.
     */
    public static String sha256Hex(final byte[] data) {
        return digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * Computes the SHA-256 digest of the given string using the specified charset and returns it as a hexadecimal
     * string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal representation of the SHA-256 digest.
     */
    public static String sha256Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA256).digestHex(data, charset);
    }

    /**
     * Computes the SHA-256 digest of the given string using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal representation of the SHA-256 digest.
     */
    public static String sha256Hex(final String data) {
        return sha256Hex(data, Charset.UTF_8);
    }

    /**
     * Computes the SHA-256 digest of the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal representation of the SHA-256 digest.
     */
    public static String sha256Hex(final InputStream data) {
        return digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * Computes the SHA-256 digest of the given file and returns it as a hexadecimal string.
     *
     * @param file The file to be digested.
     * @return The hexadecimal representation of the SHA-256 digest.
     */
    public static String sha256Hex(final File file) {
        return digester(Algorithm.SHA256).digestHex(file);
    }

    /**
     * Computes the SHA-512 digest of the given data.
     *
     * @param data The data to be digested.
     * @return The SHA-512 digest as a byte array.
     */
    public static byte[] sha512(final byte[] data) {
        return digester(Algorithm.SHA512).digest(data);
    }

    /**
     * Computes the SHA-512 digest of the given string using the specified charset.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The SHA-512 digest as a byte array.
     */
    public static byte[] sha512(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA512).digest(data, charset);
    }

    /**
     * Computes the SHA-512 digest of the given string using UTF-8 encoding.
     *
     * @param data The string data to be digested.
     * @return The SHA-512 digest as a byte array.
     */
    public static byte[] sha512(final String data) {
        return sha512(data, Charset.UTF_8);
    }

    /**
     * Computes the SHA-512 digest of the data from the given input stream.
     *
     * @param data The input stream containing the data to be digested.
     * @return The SHA-512 digest as a byte array.
     */
    public static byte[] sha512(final InputStream data) {
        return digester(Algorithm.SHA512).digest(data);
    }

    /**
     * Computes the SHA-512 digest of the given file.
     *
     * @param file The file to be digested.
     * @return The SHA-512 digest as a byte array.
     */
    public static byte[] sha512(final File file) {
        return digester(Algorithm.SHA512).digest(file);
    }

    /**
     * Computes the SHA-512 digest of the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be digested.
     * @return The hexadecimal representation of the SHA-512 digest.
     */
    public static String sha512Hex(final byte[] data) {
        return digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * Computes the SHA-512 digest of the given string using the specified charset and returns it as a hexadecimal
     * string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal representation of the SHA-512 digest.
     */
    public static String sha512Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA512).digestHex(data, charset);
    }

    /**
     * Computes the SHA-512 digest of the given string using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal representation of the SHA-512 digest.
     */
    public static String sha512Hex(final String data) {
        return sha512Hex(data, Charset.UTF_8);
    }

    /**
     * Computes the SHA-512 digest of the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The hexadecimal representation of the SHA-512 digest.
     */
    public static String sha512Hex(final InputStream data) {
        return digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * Computes the SHA-512 digest of the given file and returns it as a hexadecimal string.
     *
     * @param file The file to be digested.
     * @return The hexadecimal representation of the SHA-512 digest.
     */
    public static String sha512Hex(final File file) {
        return digester(Algorithm.SHA512).digestHex(file);
    }

    /**
     * Creates a new {@link Digester} instance for the specified signing algorithm.
     *
     * @param algorithm The signing {@link Algorithm}.
     * @return A new {@link Digester} instance.
     */
    public static Digester digester(final Algorithm algorithm) {
        return digester(algorithm.getValue());
    }

    /**
     * Creates a new {@link Digester} instance for the specified signing algorithm name.
     *
     * @param algorithm The name of the signing algorithm.
     * @return A new {@link Digester} instance.
     */
    public static Digester digester(final String algorithm) {
        return new Digester(algorithm);
    }

    /**
     * Generates a Bcrypt hashed password from a plaintext password.
     *
     * @param password The plaintext password.
     * @return The Bcrypt hashed password.
     */
    public static String hashpw(final String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * Verifies if a plaintext password matches a Bcrypt hashed password.
     *
     * @param password The plaintext password.
     * @param hashed   The Bcrypt hashed password.
     * @return {@code true} if the password matches, {@code false} otherwise.
     */
    public static boolean checkpw(final String password, final String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    /**
     * Builds an {@link ECDomainParameters} object from an {@link ECParameterSpec}.
     *
     * @param parameterSpec The {@link ECParameterSpec}.
     * @return A new {@link ECDomainParameters} instance.
     */
    public static ECDomainParameters toDomainParams(final ECParameterSpec parameterSpec) {
        return new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN(),
                parameterSpec.getH());
    }

    /**
     * Builds an {@link ECDomainParameters} object from a curve name.
     *
     * @param curveName The name of the elliptic curve.
     * @return A new {@link ECDomainParameters} instance.
     */
    public static ECDomainParameters toDomainParams(final String curveName) {
        return toDomainParams(ECUtil.getNamedCurveByName(curveName));
    }

    /**
     * Builds an {@link ECDomainParameters} object from {@link X9ECParameters}.
     *
     * @param x9ECParameters The {@link X9ECParameters}.
     * @return A new {@link ECDomainParameters} instance.
     */
    public static ECDomainParameters toDomainParams(final X9ECParameters x9ECParameters) {
        return new ECDomainParameters(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN(),
                x9ECParameters.getH());
    }

    /**
     * Converts a Java PKCS#8 format private key to OpenSSL-supported PKCS#1 format.
     *
     * @param privateKey The private key in PKCS#8 format.
     * @return The private key in PKCS#1 format as a byte array.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static byte[] toPkcs1(final PrivateKey privateKey) {
        final PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        try {
            return pkInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a Java X.509 format public key to OpenSSL-supported PKCS#1 format.
     *
     * @param publicKey The public key in X.509 format.
     * @return The public key in PKCS#1 format as a byte array.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static byte[] toPkcs1(final PublicKey publicKey) {
        final SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        try {
            return spkInfo.parsePublicKey().getEncoded();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Wraps a {@link BlockCipher} with a specified mode and padding into a {@link BufferedBlockCipher}.
     *
     * @param cipher  The {@link BlockCipher} to wrap.
     * @param mode    The cipher mode (e.g., CBC, CFB, CTR, OFB, CTS).
     * @param padding The padding scheme (e.g., NoPadding, PKCS5Padding, ZeroPadding, ISO10126Padding).
     * @return A new {@link BufferedBlockCipher} instance, or {@code null} if the mode/padding combination is not
     *         supported.
     */
    public static BufferedBlockCipher wrap(BlockCipher cipher, final Algorithm.Mode mode, final Padding padding) {
        switch (mode) {
            case CBC:
                cipher = CBCBlockCipher.newInstance(cipher);
                break;

            case CFB:
                cipher = CFBBlockCipher.newInstance(cipher, cipher.getBlockSize() * 8);
                break;

            case CTR:
                cipher = SICBlockCipher.newInstance(cipher);
                break;

            case OFB:
                cipher = new OFBBlockCipher(cipher, cipher.getBlockSize() * 8);
            case CTS:
                return new CTSBlockCipher(cipher);
        }

        switch (padding) {
            case NoPadding:
                return new DefaultBufferedBlockCipher(cipher);

            case PKCS5Padding:
                return new PaddedBufferedBlockCipher(cipher);

            case ZeroPadding:
                return new PaddedBufferedBlockCipher(cipher, new ZeroBytePadding());

            case ISO10126Padding:
                return new PaddedBufferedBlockCipher(cipher, new ISO10126d2Padding());
        }

        return null;
    }

    /**
     * Creates a {@link KeySpec} based on the specified algorithm and key material.
     * <ul>
     * <li>For DESede: {@link DESedeKeySpec} is used. If key is null, a random 24-byte key is generated.</li>
     * <li>For DES: {@link DESKeySpec} is used. If key is null, a random 8-byte key is generated.</li>
     * <li>For others: {@link SecretKeySpec} is used.</li>
     * </ul>
     *
     * @param algorithm The algorithm name (e.g., "DESede", "DES").
     * @param key       The key material as a byte array. Can be {@code null} for random key generation.
     * @return A new {@link KeySpec} instance.
     * @throws CryptoException if an invalid key is provided for DES/DESede.
     */
    public static KeySpec createKeySpec(final String algorithm, byte[] key) {
        try {
            if (algorithm.startsWith("DESede")) {
                if (null == key) {
                    key = RandomKit.randomBytes(24);
                }
                // DESede compatibility
                return new DESedeKeySpec(key);
            } else if (algorithm.startsWith("DES")) {
                if (null == key) {
                    key = RandomKit.randomBytes(8);
                }
                return new DESKeySpec(key);
            }
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }

        return new SecretKeySpec(key, algorithm);
    }

    /**
     * Creates a {@link PBEKeySpec} from a password. PBE algorithms do not have a traditional key; instead, they derive
     * a key from a password. If the password is {@code null}, a random 32-character lowercase string is generated as
     * the password.
     *
     * @param password The password as a character array. Can be {@code null} for random password generation.
     * @return A new {@link PBEKeySpec} instance.
     */
    public static PBEKeySpec createPBEKeySpec(char[] password) {
        if (null == password) {
            password = RandomKit.randomStringLower(32).toCharArray();
        }
        return new PBEKeySpec(password);
    }

    /**
     * Creates a {@link PBEParameterSpec} with the given salt and iteration count.
     *
     * @param salt           The salt value as a byte array.
     * @param iterationCount The number of iterations for the digest algorithm.
     * @return A new {@link PBEParameterSpec} instance.
     */
    public static PBEParameterSpec createPBEParameterSpec(final byte[] salt, final int iterationCount) {
        return new PBEParameterSpec(salt, iterationCount);
    }

    /**
     * 将XML格式的密钥参数转化为{@link RSAPrivateCrtKeySpec}，XML为C#生成格式，类似于：
     * 
     * <pre>{@code
     * <RSAKeyValue>
     *     <Modulus>xx</Modulus>
     *     <Exponent>xx</Exponent>
     *     <P>xxxxxxxxx</P>
     *     <Q>xxxxxxxxx</Q>
     *     <DP>xxxxxxxx</DP>
     *     <DQ>xxxxxxxx</DQ>
     *     <InverseQ>xx</InverseQ>
     *     <D>xxxxxxxxx</D>
     * </RSAKeyValue>
     * }</pre>
     *
     * @param xml xml格式密钥字符串
     * @return {@link RSAPrivateCrtKeySpec}
     */
    public static RSAPrivateCrtKeySpec xmlToRSAPrivateCrtKeySpec(final String xml) {
        // 1. 解析XML
        final Element rootElement = XmlKit.getRootElement(XmlKit.parseXml(xml));

        // 2. 提取各个字段
        final String modulusB64 = XmlKit.elementText(rootElement, "Modulus");
        final String exponentB64 = XmlKit.elementText(rootElement, "Exponent");
        final String pB64 = XmlKit.elementText(rootElement, "P");
        final String qB64 = XmlKit.elementText(rootElement, "Q");
        final String dpB64 = XmlKit.elementText(rootElement, "DP");
        final String dqB64 = XmlKit.elementText(rootElement, "DQ");
        final String inverseQB64 = XmlKit.elementText(rootElement, "InverseQ");
        final String dB64 = XmlKit.elementText(rootElement, "D");

        // 3. Base64解码
        final byte[] modulus = Base64.decode(modulusB64);
        final byte[] publicExponent = Base64.decode(exponentB64);
        final byte[] privateExponent = Base64.decode(dB64);
        final byte[] primeP = Base64.decode(pB64);
        final byte[] primeQ = Base64.decode(qB64);
        final byte[] primeExponentP = Base64.decode(dpB64);
        final byte[] primeExponentQ = Base64.decode(dqB64);
        final byte[] crtCoefficient = Base64.decode(inverseQB64);

        // 4. 创建RSAPrivateCrtKeySpec
        return new RSAPrivateCrtKeySpec(new BigInteger(1, modulus), new BigInteger(1, publicExponent),
                new BigInteger(1, privateExponent), new BigInteger(1, primeP), new BigInteger(1, primeQ),
                new BigInteger(1, primeExponentP), new BigInteger(1, primeExponentQ),
                new BigInteger(1, crtCoefficient));
    }

    /**
     * Encrypts data using the specified algorithm and key. The key string should be comma-separated, e.g.,
     * "privateKey,publicKey,type".
     *
     * @param algorithm The encryption algorithm.
     * @param key       The key string (e.g., "privateKey,publicKey,type").
     * @param content   The data to be encrypted.
     * @return The encrypted data as a byte array.
     * @throws CryptoException if encryption fails.
     */
    public static byte[] encrypt(String algorithm, String key, byte[] content) {
        final org.miaixz.bus.crypto.Provider provider = Registry.require(algorithm);
        return provider.encrypt(key, content);
    }

    /**
     * Encrypts data using the specified algorithm and key, returning the result as a hexadecimal string. The key string
     * should be comma-separated, e.g., "privateKey,publicKey,type".
     *
     * @param algorithm The encryption algorithm.
     * @param key       The key string (e.g., "privateKey,publicKey,type").
     * @param content   The string content to be encrypted.
     * @param charset   The character set to use for encoding the content.
     * @return The encrypted data as a hexadecimal string.
     * @throws CryptoException if encryption fails.
     */
    public static String encrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return HexKit.encodeString(encrypt(algorithm, key, content.getBytes(charset)));
    }

    /**
     * Encrypts data from an input stream using the specified algorithm and key. The key string should be
     * comma-separated, e.g., "privateKey,publicKey,type".
     *
     * @param algorithm   The encryption algorithm.
     * @param key         The key string (e.g., "privateKey,publicKey,type").
     * @param inputStream The input stream containing the data to be encrypted.
     * @return An {@link InputStream} containing the encrypted data.
     * @throws CryptoException if encryption fails.
     */
    public static InputStream encrypt(String algorithm, String key, InputStream inputStream) {
        final org.miaixz.bus.crypto.Provider provider = Registry.require(algorithm);
        return new ByteArrayInputStream(provider.encrypt(key, IoKit.readBytes(inputStream)));
    }

    /**
     * Decrypts data using the specified algorithm and key. The key string should be comma-separated, e.g.,
     * "privateKey,publicKey,type".
     *
     * @param algorithm The decryption algorithm.
     * @param key       The key string (e.g., "privateKey,publicKey,type").
     * @param content   The data to be decrypted.
     * @return The decrypted data as a byte array.
     * @throws CryptoException if decryption fails.
     */
    public static byte[] decrypt(String algorithm, String key, byte[] content) {
        return Registry.require(algorithm).decrypt(key, content);
    }

    /**
     * Decrypts data using the specified algorithm and key, returning the result as a string. The key string should be
     * comma-separated, e.g., "privateKey,publicKey,type". The input content is expected to be a hexadecimal string.
     *
     * @param algorithm The decryption algorithm.
     * @param key       The key string (e.g., "privateKey,publicKey,type").
     * @param content   The hexadecimal string content to be decrypted.
     * @param charset   The character set to use for decoding the decrypted content.
     * @return The decrypted data as a string.
     * @throws CryptoException if decryption fails.
     */
    public static String decrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return new String(decrypt(algorithm, key, HexKit.decode(content)), charset);
    }

    /**
     * Decrypts data from an input stream using the specified algorithm and key. The key string should be
     * comma-separated, e.g., "privateKey,publicKey,type".
     *
     * @param algorithm   The decryption algorithm.
     * @param key         The key string (e.g., "privateKey,publicKey,type").
     * @param inputStream The input stream containing the data to be decrypted.
     * @return An {@link InputStream} containing the decrypted data.
     * @throws CryptoException if decryption fails.
     */
    public static InputStream decrypt(String algorithm, String key, InputStream inputStream) {
        return new ByteArrayInputStream(Registry.require(algorithm).decrypt(key, IoKit.readBytes(inputStream)));
    }

}

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
package org.miaixz.bus.core.lang;

import javax.crypto.Cipher;

/**
 * Enumeration for various cryptographic algorithm types, including asymmetric, digest, HMAC, symmetric, and national
 * algorithms. See:
 * <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Signature"> Standard
 * Names for Java Cryptography Architecture</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Algorithm {

    /****************************** Asymmetric - Algorithm Types ******************************/

    /**
     * RSA algorithm.
     */
    RSA("RSA"),
    /**
     * RSA2 algorithm.
     */
    RSA2("RSA2"),
    /**
     * RSA algorithm with default padding: RSA/ECB/PKCS1Padding.
     */
    RSA_ECB_PKCS1("RSA/ECB/PKCS1Padding"),
    /**
     * RSA algorithm with no padding: RSA/ECB/NoPadding.
     */
    RSA_ECB("RSA/ECB/NoPadding"),
    /**
     * RSA algorithm with no padding and no mode: RSA/None/NoPadding.
     */
    RSA_NONE("RSA/None/NoPadding"),
    /**
     * Elliptic Curve (EC) algorithm.
     */
    EC("EC"),
    /**
     * Elliptic Curve Digital Signature Algorithm (ECDSA).
     */
    ECDSA("ECDSA"),

    /***************************** Asymmetric - Signature Algorithms *****************************/

    /**
     * RSA signature algorithm with no digest.
     */
    NONEWITHRSA("NONEwithRSA"),
    /**
     * MD2 with RSA encryption signature algorithm.
     */
    MD2WITHRSA("MD2withRSA"),
    /**
     * MD5 with RSA signature algorithm.
     */
    MD5WITHRSA("MD5withRSA"),

    /**
     * SHA-1 with RSA signature algorithm.
     */
    SHA1WITHRSA("SHA1withRSA"),
    /**
     * SHA-256 with RSA signature algorithm.
     */
    SHA256WITHRSA("SHA256withRSA"),
    /**
     * SHA-384 with RSA signature algorithm.
     */
    SHA384WITHRSA("SHA384withRSA"),
    /**
     * SHA-512 with RSA signature algorithm.
     */
    SHA512WITHRSA("SHA512withRSA"),

    /**
     * Digital Signature Algorithm (DSA) with no digest.
     */
    NONEWITHDSA("NONEwithDSA"),
    /**
     * DSA with SHA-1 signature algorithm.
     */
    SHA1WITHDSA("SHA1withDSA"),
    /**
     * ECDSA signature algorithm with no digest.
     */
    NONEWITHECDSA("NONEwithECDSA"),
    /**
     * SHA-1 with ECDSA signature algorithm.
     */
    SHA1WITHECDSA("SHA1withECDSA"),
    /**
     * SHA-256 with ECDSA signature algorithm.
     */
    SHA256WITHECDSA("SHA256withECDSA"),
    /**
     * SHA-384 with ECDSA signature algorithm.
     */
    SHA384WITHECDSA("SHA384withECDSA"),
    /**
     * SHA-512 with ECDSA signature algorithm.
     */
    SHA512WITHECDSA("SHA512withECDSA"),

    /**
     * SHA256WithRSA/PSS signature algorithm.
     */
    SHA256WITHRSA_PSS("SHA256WithRSA/PSS"),
    /**
     * SHA384WithRSA/PSS signature algorithm.
     */
    SHA384WITHRSA_PSS("SHA384WithRSA/PSS"),
    /**
     * SHA512WithRSA/PSS signature algorithm.
     */
    SHA512WITHRSA_PSS("SHA512WithRSA/PSS"),

    /****************************** Digest - Algorithm Types *****************************/

    /**
     * MD2 message digest algorithm.
     */
    MD2("MD2"),
    /**
     * MD5 message digest algorithm.
     */
    MD5("MD5"),
    /**
     * SHA-1 message digest algorithm.
     */
    SHA1("SHA-1"),
    /**
     * SHA-256 message digest algorithm.
     */
    SHA256("SHA-256"),
    /**
     * SHA-384 message digest algorithm.
     */
    SHA384("SHA-384"),
    /**
     * SHA-512 message digest algorithm.
     */
    SHA512("SHA-512"),
    /**
     * SHA1PRNG pseudo-random number generator algorithm.
     */
    SHA1PRNG("SHA1PRNG"),

    /***************************** Digest - HMAC Algorithms *****************************/

    /**
     * HmacMD5 algorithm.
     */
    HMACMD5("HmacMD5"),
    /**
     * HmacSHA1 algorithm.
     */
    HMACSHA1("HmacSHA1"),
    /**
     * HmacSHA256 algorithm.
     */
    HMACSHA256("HmacSHA256"),
    /**
     * HmacSHA384 algorithm.
     */
    HMACSHA384("HmacSHA384"),
    /**
     * HmacSHA512 algorithm.
     */
    HMACSHA512("HmacSHA512"),
    /**
     * HmacSM3 algorithm implementation, requires BouncyCastle library support.
     */
    HMACSM3("HmacSM3"),
    /**
     * SM4 CMAC mode implementation, requires BouncyCastle library support.
     */
    SM4CMAC("SM4CMAC"),

    /***************************** Symmetric - Algorithm Types *****************************/

    /**
     * Default AES encryption mode: AES/ECB/PKCS5Padding.
     */
    AES("AES"),
    /**
     * ARCFOUR algorithm.
     */
    ARCFOUR("ARCFOUR"),
    /**
     * Blowfish algorithm.
     */
    BLOWFISH("Blowfish"),
    /**
     * Default DES encryption mode: DES/ECB/PKCS5Padding.
     */
    DES("DES"),
    /**
     * 3DES algorithm, default implementation: DESede/ECB/PKCS5Padding.
     */
    DESEDE("DESede"),
    /**
     * Block cipher algorithm RC2. RC2 encryption is twice as fast as DES.
     */
    RC2("RC2"),
    /**
     * Stream cipher algorithm RC4, with variable key length.
     */
    RC4("RC4"),

    /**
     * PBEWithMD5AndDES algorithm.
     */
    PBEWITHMD5ANDDES("PBEWithMD5AndDES"),
    /**
     * PBEWithSHA1AndDESede algorithm.
     */
    PBEWITHSHA1ANDDESEDE("PBEWithSHA1AndDESede"),
    /**
     * PBEWithSHA1AndRC2_40 algorithm.
     */
    PBEWITHSHA1ANDRC2_40("PBEWithSHA1AndRC2_40"),

    /******************************* National Algorithms *******************************/

    /**
     * SM1 symmetric algorithm.
     */
    SM1("SM1"),
    /**
     * SM2 public key cryptography algorithm.
     */
    SM2("SM2"),
    /**
     * SM3 hash algorithm, primarily used for digital signatures and verification, message authentication code
     * generation and verification, random number generation, etc. Its security and efficiency are comparable to
     * SHA-256.
     */
    SM3("SM3"),
    /**
     * SM4 iterative block cipher algorithm.
     */
    SM4("SM4"),

    /******************************* Other Algorithms *******************************/

    /**
     * ZUC algorithm - ZUC-128.
     */
    ZUC_128("ZUC-128"),
    /**
     * ZUC algorithm - ZUC-256.
     */
    ZUC_256("ZUC-256"),
    /**
     * ECIES (Elliptic Curve Integrated Encryption Scheme).
     */
    ECIES("ECIES"),
    /**
     * PBKDF2 applies a pseudo-random function to derive a key. PBKDF2 simply involves repeatedly calculating a salted
     * hash.
     */
    PBKDF2WITHHMACSHA1("PBKDF2WithHmacSHA1"),
    /**
     * ChaCha20 stream cipher algorithm.
     */
    CHACHA20("ChaCha20");

    /**
     * The string representation of the algorithm.
     */
    private final String value;

    /**
     * Constructs an {@code Algorithm} enum constant.
     *
     * @param value The string representation of the algorithm, case-sensitive.
     */
    Algorithm(final String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the algorithm, which is case-sensitive.
     *
     * @return The string representation of the algorithm.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Enumeration for cryptographic algorithm modes. These modes describe how block ciphers (not stream ciphers)
     * process plaintext in blocks during encryption.
     *
     * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher"> Cipher
     *      section in Standard Names for Java Cryptography Architecture</a>
     */
    public enum Mode {
        /**
         * No mode specified.
         */
        NONE,
        /**
         * Cipher Block Chaining (CBC) mode.
         */
        CBC,
        /**
         * Cipher Feedback (CFB) mode.
         */
        CFB,
        /**
         * Counter (CTR) mode (a simplification of OFB).
         */
        CTR,
        /**
         * Ciphertext Stealing (CTS) mode.
         */
        CTS,
        /**
         * Electronic Codebook (ECB) mode.
         */
        ECB,
        /**
         * Output Feedback (OFB) mode.
         */
        OFB,
        /**
         * Propagating Cipher Block Chaining (PCBC) mode.
         */
        PCBC,
        /**
         * Galois/Counter Mode (GCM). It adds GMAC features on top of CTR encryption, addressing the issue that CTR
         * cannot perform integrity checks on encrypted messages.
         */
        GCM
    }

    /**
     * Enumeration for Cipher operation types.
     */
    public enum Type {

        /**
         * Encryption mode.
         */
        ENCRYPT(javax.crypto.Cipher.ENCRYPT_MODE),
        /**
         * Decryption mode.
         */
        DECRYPT(javax.crypto.Cipher.DECRYPT_MODE),
        /**
         * Key wrapping mode.
         */
        WRAP(javax.crypto.Cipher.WRAP_MODE),
        /**
         * Key unwrapping mode.
         */
        UNWRAP(javax.crypto.Cipher.UNWRAP_MODE);

        /**
         * The integer value representing the cipher operation type.
         */
        private final int value;

        /**
         * Constructs a {@code Type} enum constant.
         *
         * @param value The integer value as defined in {@link Cipher}.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Returns the integer value corresponding to this cipher operation type.
         *
         * @return The integer value.
         */
        public int getValue() {
            return this.value;
        }
    }

}

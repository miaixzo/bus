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
package org.miaixz.bus.crypto.center;

import java.io.Serial;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.crypto.Holder;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.asymmetric.Crypto;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;

/**
 * RSA public key/private key/signature encryption and decryption.
 * <p>
 * Because asymmetric encryption is extremely slow, it is generally not used to encrypt files. Symmetric encryption is
 * used instead. Asymmetric encryption algorithms can be used to encrypt the keys of symmetric encryption, thus ensuring
 * the security of the keys and therefore the data.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RSA extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852290353723L;

    /**
     * Constructor, generates a new private-public key pair.
     */
    public RSA() {
        super(Algorithm.RSA_ECB_PKCS1);
    }

    /**
     * Constructor, generates a new private-public key pair.
     *
     * @param algorithm Custom RSA algorithm, e.g., RSA/ECB/PKCS1Padding.
     */
    public RSA(final String algorithm) {
        super(algorithm);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param algorithm Custom RSA algorithm, e.g., RSA/ECB/PKCS1Padding.
     * @param keyPair   The key pair, {@code null} to generate a random one.
     */
    public RSA(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param algorithm Custom RSA algorithm.
     * @param keyPair   The key pair, {@code null} to generate a random one.
     */
    public RSA(final Algorithm algorithm, final KeyPair keyPair) {
        super(algorithm.getValue(), keyPair);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key in Hex or Base64 representation.
     * @param publicKey  The public key in Hex or Base64 representation.
     */
    public RSA(final String privateKey, final String publicKey) {
        super(Algorithm.RSA_ECB_PKCS1, privateKey, publicKey);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param algorithm  Custom RSA algorithm, e.g., RSA/ECB/PKCS1Padding.
     * @param privateKey The private key in Hex or Base64 representation.
     * @param publicKey  The public key in Hex or Base64 representation.
     */
    public RSA(final String algorithm, final String privateKey, final String publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key.
     * @param publicKey  The public key.
     */
    public RSA(final byte[] privateKey, final byte[] publicKey) {
        super(Algorithm.RSA_ECB_PKCS1, privateKey, publicKey);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param modulus         The modulus (N).
     * @param privateExponent The private exponent (d).
     * @param publicExponent  The public exponent (e).
     */
    public RSA(final BigInteger modulus, final BigInteger privateExponent, final BigInteger publicExponent) {
        this(generatePrivateKey(modulus, privateExponent), generatePublicKey(modulus, publicExponent));
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key.
     * @param publicKey  The public key.
     */
    public RSA(final PrivateKey privateKey, final PublicKey publicKey) {
        super(Algorithm.RSA_ECB_PKCS1, privateKey, publicKey);
    }

    /**
     * Generates an RSA private key.
     *
     * @param modulus         The modulus (N).
     * @param privateExponent The private exponent (d).
     * @return {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(final BigInteger modulus, final BigInteger privateExponent) {
        return Keeper.generatePrivateKey(
                Algorithm.RSA_ECB_PKCS1.getValue(),
                new RSAPrivateKeySpec(modulus, privateExponent));
    }

    /**
     * Generates an RSA public key.
     *
     * @param modulus        The modulus (N).
     * @param publicExponent The public exponent (e).
     * @return {@link PublicKey}
     */
    public static PublicKey generatePublicKey(final BigInteger modulus, final BigInteger publicExponent) {
        return Keeper
                .generatePublicKey(Algorithm.RSA_ECB_PKCS1.getValue(), new RSAPublicKeySpec(modulus, publicExponent));
    }

    /**
     * {@inheritDoc}
     * <p>
     * When not using the BouncyCastle provider, the block size uses the default algorithm.
     *
     * @param data    {@inheritDoc}
     * @param keyType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public byte[] encrypt(final byte[] data, final KeyType keyType) {
        // When not using the BouncyCastle provider, the block size uses the default algorithm.
        if (this.encryptBlockSize < 0 && null == Holder.getProvider()) {
            // Encrypted data length <= modulus length - 11
            this.encryptBlockSize = ((RSAKey) getKeyByType(keyType)).getModulus().bitLength() / 8 - 11;
        }
        return super.encrypt(data, keyType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * When not using the BouncyCastle provider, the block size uses the default algorithm.
     *
     * @param bytes   {@inheritDoc}
     * @param keyType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public byte[] decrypt(final byte[] bytes, final KeyType keyType) {
        // When not using the BouncyCastle provider, the block size uses the default algorithm.
        if (this.decryptBlockSize < 0 && null == Holder.getProvider()) {
            // Encrypted data length <= modulus length
            this.decryptBlockSize = ((RSAKey) getKeyByType(keyType)).getModulus().bitLength() / 8;
        }
        return super.decrypt(bytes, keyType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to initialize the cipher and handles NoSuchAlgorithmException by trying to add BouncyCastle provider.
     */
    @Override
    protected void initCipher() {
        try {
            super.initCipher();
        } catch (final CryptoException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof NoSuchAlgorithmException) {
                // On Linux, if the BouncyCastle provider is not imported, the RSA/ECB/PKCS1Padding
                // algorithm may not be found. In this case, use the default algorithm.
                this.algorithm = Algorithm.RSA.getValue();
                super.initCipher();
            }
            throw e;
        }
    }

}

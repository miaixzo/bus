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
package org.miaixz.bus.crypto.builtin.asymmetric;

import java.io.IOException;
import java.io.Serial;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Cipher;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.cipher.JceCipher;

/**
 * Asymmetric cryptographic algorithm implementation.
 * <p>
 * Asymmetric encryption involves two main use cases:
 * </p>
 * <ol>
 * <li><b>Signing:</b> Uses the private key for encryption and the public key for decryption. This is used to allow all
 * public key holders to verify the identity of the private key owner and to prevent tampering with the content
 * published by the private key owner. It does not, however, guarantee that the content cannot be obtained by others.
 * </li>
 * <li><b>Encryption:</b> Uses the public key for encryption and the private key for decryption. This is used to send
 * information to the public key owner. This information might be tampered with by others, but it cannot be obtained by
 * others.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Crypto extends AbstractCrypto<Crypto> {

    @Serial
    private static final long serialVersionUID = 2852288768007L;

    /**
     * The {@link Cipher} responsible for performing encryption or decryption operations.
     */
    protected Cipher cipher;
    /**
     * The block size for encryption. A value of -1 indicates that the block size is not explicitly set and should be
     * determined dynamically.
     */
    protected int encryptBlockSize = -1;
    /**
     * The block size for decryption. A value of -1 indicates that the block size is not explicitly set and should be
     * determined dynamically.
     */
    protected int decryptBlockSize = -1;
    /**
     * Algorithm-specific parameters, such as initialization vectors (IVs) or other cryptographic settings.
     */
    private AlgorithmParameterSpec algorithmParameterSpec;
    /**
     * Custom {@link SecureRandom} instance for generating random numbers, allowing for custom seeds.
     */
    private SecureRandom random;

    /**
     * Constructs an asymmetric crypto instance, generating a new private-public key pair.
     *
     * @param algorithm The {@link Algorithm} to use for asymmetric cryptography.
     */
    public Crypto(final Algorithm algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm and an existing {@link KeyPair}. If the
     * {@code keyPair} is {@code null}, a new key pair will be randomly generated. If only one key (private or public)
     * is provided within the {@code keyPair}, the crypto object can only be used for operations corresponding to that
     * key.
     *
     * @param algorithm The {@link Algorithm} to use for asymmetric cryptography.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public Crypto(final Algorithm algorithm, final KeyPair keyPair) {
        super(algorithm.getValue(), keyPair);
    }

    /**
     * Constructs an asymmetric crypto instance, generating a new private-public key pair.
     *
     * @param algorithm The name of the asymmetric algorithm.
     */
    public Crypto(final String algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm name and an existing {@link KeyPair}. If
     * the {@code keyPair} is {@code null}, a new key pair will be randomly generated. If only one key (private or
     * public) is provided within the {@code keyPair}, the crypto object can only be used for operations corresponding
     * to that key.
     *
     * @param algorithm The name of the asymmetric algorithm.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public Crypto(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm and private/public keys provided as Hex or
     * Base64 encoded strings. If both private and public keys are {@code null}, a new key pair will be generated. If
     * only one key is provided, the crypto object can only be used for operations corresponding to that key.
     *
     * @param algorithm  The {@link Algorithm} to use for asymmetric cryptography.
     * @param privateKey The private key as a Hex or Base64 encoded string.
     * @param publicKey  The public key as a Hex or Base64 encoded string.
     */
    public Crypto(final Algorithm algorithm, final String privateKey, final String publicKey) {
        this(algorithm.getValue(), Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm and private/public keys provided as byte
     * arrays. If both private and public keys are {@code null}, a new key pair will be generated. If only one key is
     * provided, the crypto object can only be used for operations corresponding to that key.
     *
     * @param algorithm  The {@link Algorithm} to use for asymmetric cryptography.
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     */
    public Crypto(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm name and private/public keys provided as
     * Hex or Base64 encoded strings. If both private and public keys are {@code null}, a new key pair will be
     * generated. If only one key is provided, the crypto object can only be used for operations corresponding to that
     * key.
     *
     * @param algorithm  The name of the asymmetric algorithm.
     * @param privateKey The private key as a Base64 encoded string.
     * @param publicKey  The public key as a Base64 encoded string.
     */
    public Crypto(final String algorithm, final String privateKey, final String publicKey) {
        this(algorithm, Base64.decode(privateKey), Base64.decode(publicKey));
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm name and private/public keys provided as
     * byte arrays. If both private and public keys are {@code null}, a new key pair will be generated. If only one key
     * is provided, the crypto object can only be used for operations corresponding to that key.
     *
     * @param algorithm  The name of the asymmetric algorithm.
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     */
    public Crypto(final String algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm, new KeyPair(Keeper.generatePublicKey(algorithm, publicKey),
                Keeper.generatePrivateKey(algorithm, privateKey)));
    }

    /**
     * Constructs an asymmetric crypto instance with the specified algorithm and existing {@link PrivateKey} and
     * {@link PublicKey} objects. If both private and public keys are {@code null}, a new key pair will be generated. If
     * only one key is provided, the crypto object can only be used for operations corresponding to that key.
     *
     * @param algorithm  The {@link Algorithm} to use for asymmetric cryptography.
     * @param privateKey The {@link PrivateKey}.
     * @param publicKey  The {@link PublicKey}.
     */
    public Crypto(final Algorithm algorithm, final PrivateKey privateKey, final PublicKey publicKey) {
        this(algorithm.getValue(), new KeyPair(publicKey, privateKey));
    }

    /**
     * Retrieves the encryption block size.
     *
     * @return The encryption block size in bytes.
     */
    public int getEncryptBlockSize() {
        return encryptBlockSize;
    }

    /**
     * Sets the encryption block size.
     *
     * @param encryptBlockSize The encryption block size in bytes.
     */
    public void setEncryptBlockSize(final int encryptBlockSize) {
        this.encryptBlockSize = encryptBlockSize;
    }

    /**
     * Retrieves the decryption block size.
     *
     * @return The decryption block size in bytes.
     */
    public int getDecryptBlockSize() {
        return decryptBlockSize;
    }

    /**
     * Sets the decryption block size.
     *
     * @param decryptBlockSize The decryption block size in bytes.
     */
    public void setDecryptBlockSize(final int decryptBlockSize) {
        this.decryptBlockSize = decryptBlockSize;
    }

    /**
     * Retrieves the {@link AlgorithmParameterSpec} used by this crypto instance. In some algorithms, specific
     * parameters are required, such as {@code IESParameterSpec} in ECIES.
     *
     * @return The {@link AlgorithmParameterSpec}.
     */
    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.algorithmParameterSpec;
    }

    /**
     * Sets the {@link AlgorithmParameterSpec} for this crypto instance. In some algorithms, specific parameters are
     * required, such as {@code IESParameterSpec} in ECIES.
     *
     * @param algorithmParameterSpec The {@link AlgorithmParameterSpec} to set.
     * @return This {@code Crypto} instance.
     */
    public Crypto setAlgorithmParameterSpec(final AlgorithmParameterSpec algorithmParameterSpec) {
        this.algorithmParameterSpec = algorithmParameterSpec;
        return this;
    }

    /**
     * Sets the {@link SecureRandom} instance for generating random numbers, allowing for custom seeds.
     *
     * @param random The {@link SecureRandom} instance to use.
     * @return This {@code Crypto} instance.
     */
    public Crypto setRandom(final SecureRandom random) {
        this.random = random;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param algorithm {@inheritDoc}
     * @param keyPair   {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Crypto init(final String algorithm, final KeyPair keyPair) {
        super.init(algorithm, keyPair);
        initCipher();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param data    {@inheritDoc}
     * @param keyType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public byte[] encrypt(final byte[] data, final KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Algorithm.Type.ENCRYPT, key);

            if (this.encryptBlockSize < 0) {
                // If BC library is introduced, automatically get block size
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    this.encryptBlockSize = blockSize;
                }
            }

            return doFinal(data, this.encryptBlockSize < 0 ? data.length : this.encryptBlockSize);
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param data    {@inheritDoc}
     * @param keyType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public byte[] decrypt(final byte[] data, final KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Algorithm.Type.DECRYPT, key);

            if (this.decryptBlockSize < 0) {
                // If BC library is introduced, automatically get block size
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    this.decryptBlockSize = blockSize;
                }
            }

            return doFinal(data, this.decryptBlockSize < 0 ? data.length : this.decryptBlockSize);
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the underlying {@link Cipher} instance.
     *
     * @return The {@link Cipher} instance.
     */
    public Cipher getCipher() {
        return this.cipher;
    }

    /**
     * Initializes the {@link Cipher}, attempting to load the Bouncy Castle provider by default.
     */
    protected void initCipher() {
        this.cipher = new JceCipher(this.algorithm);
    }

    /**
     * Performs the final encryption or decryption operation on the given data. Data is processed in blocks if its
     * length exceeds {@code maxBlockSize}.
     *
     * @param data         The content data to be encrypted or decrypted.
     * @param maxBlockSize The maximum block size for processing data in segments.
     * @return The encrypted or decrypted data.
     * @throws IOException     This exception should theoretically not be triggered in this context.
     * @throws CryptoException if the cryptographic operation fails.
     */
    private byte[] doFinal(final byte[] data, final int maxBlockSize) throws IOException {
        // If data length is less than or equal to maxBlockSize, process in one go
        if (data.length <= maxBlockSize) {
            return this.cipher.processFinal(data, 0, data.length);
        }

        // Process in blocks
        return doFinalWithBlock(data, maxBlockSize);
    }

    /**
     * Performs encryption or decryption in blocks (segments).
     *
     * @param data         The data to be encrypted or decrypted.
     * @param maxBlockSize The maximum size of each segment. Must be greater than 0.
     * @return The encrypted or decrypted data.
     * @throws IOException     This exception should theoretically not be triggered in this context.
     * @throws CryptoException if the cryptographic operation fails.
     */
    private byte[] doFinalWithBlock(final byte[] data, final int maxBlockSize) throws IOException {
        final int dataLength = data.length;
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();

        int offSet = 0;
        // Remaining length
        int remainLength = dataLength;
        int blockSize;
        // Process data in segments
        while (remainLength > 0) {
            blockSize = Math.min(remainLength, maxBlockSize);
            out.write(this.cipher.processFinal(data, offSet, blockSize));

            offSet += blockSize;
            remainLength = dataLength - offSet;
        }

        return out.toByteArray();
    }

    /**
     * Initializes the {@link Cipher} with the specified mode (encryption or decryption) and key.
     *
     * @param mode The operation mode, either {@link Algorithm.Type#ENCRYPT} or {@link Algorithm.Type#DECRYPT}.
     * @param key  The cryptographic key.
     * @return The initialized {@link Cipher} instance.
     * @throws CryptoException if cipher initialization fails.
     */
    private Cipher initMode(final Algorithm.Type mode, final Key key) {
        final Cipher cipher = this.cipher;
        cipher.init(mode, new JceCipher.JceParameters(key, this.algorithmParameterSpec, this.random));
        return cipher;
    }

}

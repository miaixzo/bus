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
package org.miaixz.bus.crypto.builtin.symmetric;

import java.io.*;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.SaltMagic;
import org.miaixz.bus.crypto.builtin.SaltParser;
import org.miaixz.bus.crypto.cipher.JceCipher;

/**
 * Symmetric encryption algorithm.
 * <p>
 * In symmetric encryption, the sender encrypts the plaintext (original data) with an encryption key using a specific
 * algorithm, turning it into complex ciphertext. To decrypt the message, the recipient must use the same key and the
 * inverse of the algorithm. In symmetric encryption, only one key is used by both parties for encryption and
 * decryption, which requires the decrypting party to know the key in advance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Crypto implements Encryptor, Decryptor, Serializable {

    @Serial
    private static final long serialVersionUID = 2852289196593L;

    /**
     * Lock for thread-safe operations.
     */
    private final Lock lock = new ReentrantLock();
    /**
     * The cipher.
     */
    private JceCipher cipher;
    /**
     * Algorithm parameter specifications.
     */
    private AlgorithmParameterSpec algorithmParameterSpec;
    /**
     * Custom SecureRandom.
     */
    private SecureRandom random;
    /**
     * SecretKey responsible for storing the symmetric key.
     */
    private SecretKey secretKey;
    /**
     * Whether to use ZeroPadding.
     */
    private boolean isZeroPadding;

    /**
     * Constructor, uses a random key.
     *
     * @param algorithm {@link Algorithm}
     */
    public Crypto(final Algorithm algorithm) {
        this(algorithm, (byte[]) null);
    }

    /**
     * Constructor, uses a random key.
     *
     * @param algorithm The algorithm, can be "algorithm/mode/padding" or just "algorithm".
     */
    public Crypto(final String algorithm) {
        this(algorithm, (byte[]) null);
    }

    /**
     * Constructor.
     *
     * @param algorithm The algorithm {@link Algorithm}.
     * @param key       Custom key.
     */
    public Crypto(final Algorithm algorithm, final byte[] key) {
        this(algorithm.getValue(), key);
    }

    /**
     * Constructor.
     *
     * @param algorithm The algorithm {@link Algorithm}.
     * @param key       Custom key.
     */
    public Crypto(final Algorithm algorithm, final SecretKey key) {
        this(algorithm.getValue(), key);
    }

    /**
     * Constructor.
     *
     * @param algorithm The algorithm.
     * @param key       The secret key.
     */
    public Crypto(final String algorithm, final byte[] key) {
        this(algorithm, Keeper.generateKey(algorithm, key));
    }

    /**
     * Constructor.
     *
     * @param algorithm The algorithm.
     * @param key       The secret key.
     */
    public Crypto(final String algorithm, final SecretKey key) {
        this(algorithm, key, null);
    }

    /**
     * Constructor.
     *
     * @param algorithm  The algorithm.
     * @param key        The secret key.
     * @param paramsSpec Algorithm parameter specifications, e.g., for salt.
     */
    public Crypto(final String algorithm, final SecretKey key, final AlgorithmParameterSpec paramsSpec) {
        init(algorithm, key);
        initParams(algorithm, paramsSpec);
    }

    /**
     * Copies the decrypted stream, handling ZeroPadding.
     *
     * @param in        The {@link CipherInputStream}.
     * @param out       The output stream.
     * @param blockSize The block size.
     * @throws IOException if an I/O error occurs.
     */
    private static void copyForZeroPadding(final CipherInputStream in, final OutputStream out, final int blockSize)
            throws IOException {
        int n = 1;
        if (Normal._8192 > blockSize) {
            n = Math.max(n, Normal._8192 / blockSize);
        }
        // The buffer here uses a multiple of blockSize to conveniently read the padded zeros into one buffer.
        final int bufSize = blockSize * n;
        final byte[] preBuffer = new byte[bufSize];
        final byte[] buffer = new byte[bufSize];

        boolean isFirst = true;
        int preReadSize = 0;
        for (int readSize; (readSize = in.read(buffer)) != Normal.__1;) {
            if (isFirst) {
                isFirst = false;
            } else {
                // Write out the previous batch of data.
                out.write(preBuffer, 0, preReadSize);
            }
            ArrayKit.copy(buffer, preBuffer, readSize);
            preReadSize = readSize;
        }
        // Remove all trailing padding zeros.
        int i = preReadSize - 1;
        while (i >= 0 && 0 == preBuffer[i]) {
            i--;
        }
        out.write(preBuffer, 0, i + 1);
        out.flush();
    }

    /**
     * Initializes the crypto.
     *
     * @param algorithm The algorithm.
     * @param key       The secret key. If {@code null}, a key will be generated automatically.
     * @return this instance.
     */
    public Crypto init(String algorithm, final SecretKey key) {
        Assert.notBlank(algorithm, "'algorithm' must be not blank !");
        this.secretKey = key;

        // Check for ZeroPadding, if found, replace it with NoPadding and set a flag for special handling.
        if (algorithm.contains(Padding.ZeroPadding.name())) {
            algorithm = StringKit.replace(algorithm, Padding.ZeroPadding.name(), Padding.NoPadding.name());
            this.isZeroPadding = true;
        }

        this.cipher = new JceCipher(algorithm);
        return this;
    }

    /**
     * Gets the symmetric key.
     *
     * @return The symmetric key.
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * Gets the Cipher object.
     *
     * @return The Cipher object.
     */
    public Cipher getCipher() {
        return cipher.getRaw();
    }

    /**
     * Sets the {@link AlgorithmParameterSpec}, typically used for salt or an initialization vector (IV).
     *
     * @param algorithmParameterSpec The {@link AlgorithmParameterSpec}.
     * @return this instance.
     */
    public Crypto setAlgorithmParameterSpec(final AlgorithmParameterSpec algorithmParameterSpec) {
        this.algorithmParameterSpec = algorithmParameterSpec;
        return this;
    }

    /**
     * Sets the initialization vector (IV).
     *
     * @param iv The {@link IvParameterSpec}.
     * @return this instance.
     */
    public Crypto setIv(final IvParameterSpec iv) {
        return setAlgorithmParameterSpec(iv);
    }

    /**
     * Sets the initialization vector (IV).
     *
     * @param iv The IV bytes.
     * @return this instance.
     */
    public Crypto setIv(final byte[] iv) {
        return setIv(new IvParameterSpec(iv));
    }

    /**
     * Sets the {@link SecureRandom} generator, allowing for a custom seed.
     *
     * @param random The {@link SecureRandom} generator.
     * @return this instance.
     */
    public Crypto setRandom(final SecureRandom random) {
        this.random = random;
        return this;
    }

    /**
     * Initializes the cipher mode and clears previous data.
     *
     * @param mode The mode enum.
     * @return this instance.
     */
    public Crypto setMode(final Algorithm.Type mode) {
        return setMode(mode, null);
    }

    /**
     * Initializes the cipher mode and clears previous data.
     *
     * @param mode The mode enum.
     * @param salt The salt value.
     * @return this instance.
     */
    public Crypto setMode(final Algorithm.Type mode, final byte[] salt) {
        lock.lock();
        try {
            initMode(mode, salt);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Updates the data. Intermediate results of block encryption can be used as random numbers. Before the first
     * update, {@link #setMode(Algorithm.Type)} must be called to initialize encryption or decryption mode. Each
     * subsequent update is cumulative.
     *
     * @param data The bytes to be encrypted/decrypted.
     * @return The bytes after the update.
     */
    public byte[] update(final byte[] data) {
        final Cipher cipher = this.cipher.getRaw();
        lock.lock();
        try {
            return cipher.update(paddingDataWithZero(data, cipher.getBlockSize()));
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finishes a multiple-part encryption or decryption operation, depending on how this cipher was initialized.
     *
     * @return A new buffer with the result.
     */
    public byte[] doFinal() {
        final Cipher cipher = this.cipher.getRaw();
        lock.lock();
        try {
            return cipher.doFinal();
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finishes a multiple-part encryption or decryption operation and returns the result as a hex string.
     *
     * @return A new hex string with the result.
     */
    public String doFinalHex() {
        return HexKit.encodeString(doFinal());
    }

    /**
     * Updates data and returns the result as a hex string. Before the first update, {@link #setMode(Algorithm.Type)}
     * must be called to initialize encryption or decryption mode. Each subsequent update is cumulative.
     *
     * @param data The bytes to be encrypted/decrypted.
     * @return The hex string after the update.
     */
    public String updateHex(final byte[] data) {
        return HexKit.encodeString(update(data));
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        return encrypt(data, null);
    }

    /**
     * Encrypts data.
     *
     * @param data The bytes to be encrypted.
     * @param salt The salt value. If {@code null}, it's not set; otherwise, it generates ciphertext with a "Salted__"
     *             header.
     * @return The encrypted bytes.
     */
    public byte[] encrypt(final byte[] data, final byte[] salt) {
        byte[] result;
        lock.lock();
        try {
            final JceCipher cipher = initMode(Algorithm.Type.ENCRYPT, salt);
            result = cipher.processFinal(paddingDataWithZero(data, cipher.getBlockSize()));
        } finally {
            lock.unlock();
        }
        return SaltMagic.addMagic(result, salt);
    }

    @Override
    public void encrypt(final InputStream data, final OutputStream out, final boolean isClose)
            throws InternalException {
        CipherOutputStream cipherOutputStream = null;
        lock.lock();
        try {
            final JceCipher cipher = initMode(Algorithm.Type.ENCRYPT, null);
            cipherOutputStream = new CipherOutputStream(out, cipher.getRaw());
            final long length = IoKit.copy(data, cipherOutputStream);
            if (this.isZeroPadding) {
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    // The remaining data after splitting by block size.
                    final int remainLength = (int) (length % blockSize);
                    if (remainLength > 0) {
                        // Pad with zeros.
                        cipherOutputStream.write(new byte[blockSize - remainLength]);
                        cipherOutputStream.flush();
                    }
                }
            }
        } catch (final InternalException e) {
            throw e;
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
            // The CipherOutputStream must be closed to ensure all data is written.
            IoKit.closeQuietly(cipherOutputStream);
            if (isClose) {
                IoKit.closeQuietly(data);
            }
        }
    }

    @Override
    public byte[] decrypt(final byte[] bytes) {
        final int blockSize;
        final byte[] decryptData;
        lock.lock();
        try {
            final byte[] salt = SaltMagic.getSalt(bytes);
            final JceCipher cipher = initMode(Algorithm.Type.DECRYPT, salt);
            blockSize = cipher.getBlockSize();
            decryptData = cipher.processFinal(SaltMagic.getData(bytes));
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }

        return removePadding(decryptData, blockSize);
    }

    @Override
    public void decrypt(final InputStream data, final OutputStream out, final boolean isClose)
            throws InternalException {
        CipherInputStream cipherInputStream = null;
        lock.lock();
        try {
            final JceCipher cipher = initMode(Algorithm.Type.DECRYPT, null);
            cipherInputStream = new CipherInputStream(data, cipher.getRaw());
            if (this.isZeroPadding) {
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    copyForZeroPadding(cipherInputStream, out, blockSize);
                    return;
                }
            }
            IoKit.copy(cipherInputStream, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final InternalException e) {
            throw e;
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
            // The CipherInputStream must be closed to read all data.
            IoKit.closeQuietly(cipherInputStream);
            if (isClose) {
                IoKit.closeQuietly(data);
            }
        }
    }

    /**
     * Initializes encryption/decryption parameters, such as the IV.
     *
     * @param algorithm  The algorithm.
     * @param paramsSpec User-defined {@link AlgorithmParameterSpec}.
     * @return this instance.
     */
    private Crypto initParams(final String algorithm, AlgorithmParameterSpec paramsSpec) {
        if (null == paramsSpec) {
            byte[] iv = Optional.ofNullable(cipher).map(JceCipher::getRaw).map(Cipher::getIV).getOrNull();

            // Random IV
            if (StringKit.startWithIgnoreCase(algorithm, "PBE")) {
                // For PBE algorithms, use a random salt.
                if (null == iv) {
                    iv = RandomKit.randomBytes(8);
                }
                paramsSpec = new PBEParameterSpec(iv, 100);
            } else if (StringKit.startWithIgnoreCase(algorithm, "AES")) {
                if (null != iv) {
                    // For AES, use the Cipher's default random IV.
                    paramsSpec = new IvParameterSpec(iv);
                }
            }
        }

        return setAlgorithmParameterSpec(paramsSpec);
    }

    /**
     * Initializes the {@link JceCipher} for encryption or decryption mode.
     *
     * @param mode The mode, see {@link Algorithm.Type#ENCRYPT} or {@link Algorithm.Type#DECRYPT}.
     * @return The initialized {@link JceCipher}.
     */
    private JceCipher initMode(final Algorithm.Type mode, final byte[] salt) {
        SecretKey secretKey = this.secretKey;
        if (null != salt) {
            // Provide compatibility support for OpenSSL format.
            final String algorithm = getCipher().getAlgorithm();
            final byte[][] keyAndIV = SaltParser.ofMd5(32, algorithm).getKeyAndIV(secretKey.getEncoded(), salt);
            secretKey = Keeper.generateKey(algorithm, keyAndIV[0]);
            if (ArrayKit.isNotEmpty(keyAndIV[1])) {
                setAlgorithmParameterSpec(new IvParameterSpec(keyAndIV[1]));
            }
        }

        final JceCipher cipher = this.cipher;
        cipher.init(mode, new JceCipher.JceParameters(secretKey, this.algorithmParameterSpec, this.random));
        return cipher;
    }

    /**
     * Pads the data with zeros to a multiple of the block size.
     * <p>
     * This is only effective in {@link Padding#ZeroPadding} mode and when the data length is not a multiple of the
     * block size; otherwise, the original data is returned.
     *
     * <p>
     * See: https://blog.csdn.net/OrangeJack/article/details/82913804
     *
     * @param data      The data to pad.
     * @param blockSize The block size.
     * @return The padded data, or the original data if ZeroPadding is false or the length is already a multiple.
     */
    private byte[] paddingDataWithZero(final byte[] data, final int blockSize) {
        if (this.isZeroPadding) {
            final int length = data.length;
            // The remainder length after dividing the data by the block size.
            final int remainLength = length % blockSize;
            if (remainLength > 0) {
                // The new length is a multiple of blockSize, the remainder is padded with zeros.
                return ArrayKit.resize(data, length + blockSize - remainLength);
            }
        }
        return data;
    }

    /**
     * Removes padding from the data according to the block size, used for decryption. This is only effective in
     * {@link Padding#ZeroPadding} mode; otherwise, the original data is returned.
     *
     * @param data      The data.
     * @param blockSize The block size, must be greater than 0.
     * @return The data with padding removed, or the original data if ZeroPadding is false.
     */
    private byte[] removePadding(final byte[] data, final int blockSize) {
        if (this.isZeroPadding && blockSize > 0) {
            final int length = data.length;
            final int remainLength = length % blockSize;
            if (remainLength == 0) {
                // If the decoded data length is a multiple of the block size, it might have zero padding.
                // Remove all trailing zeros.
                int i = length - 1;
                while (i >= 0 && 0 == data[i]) {
                    i--;
                }
                return ArrayKit.resize(data, i + 1);
            }
        }
        return data;
    }

}

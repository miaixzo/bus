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
package org.miaixz.bus.crypto.cipher;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.ShortBufferException;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Cipher;

/**
 * Provides a wrapper for {@link javax.crypto.Cipher} methods, simplifying its usage. This class allows for consistent
 * handling of JCE (Java Cryptography Extension) ciphers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JceCipher extends SimpleWrapper<javax.crypto.Cipher> implements Cipher {

    /**
     * Constructs a {@code JceCipher} instance for the specified algorithm.
     *
     * @param algorithm The name of the algorithm (e.g., "AES/ECB/PKCS5Padding").
     */
    public JceCipher(final String algorithm) {
        this(Builder.createCipher(algorithm));
    }

    /**
     * Constructs a {@code JceCipher} instance using an existing {@link javax.crypto.Cipher} object.
     *
     * @param cipher The {@link javax.crypto.Cipher} instance, which can be created via
     *               {@link javax.crypto.Cipher#getInstance(String)}.
     */
    public JceCipher(final javax.crypto.Cipher cipher) {
        super(Assert.notNull(cipher));
    }

    /**
     * Returns the algorithm name. This method is designed to be overridden by subclasses. Implementations should return
     * the name of the algorithm used by this cipher.
     *
     * @return the algorithm name
     */
    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithm();
    }

    /**
     * Returns the block size in bytes. This method is designed to be overridden by subclasses. Implementations should
     * return the block size of the cipher.
     *
     * @return the block size in bytes, or 0 if the underlying algorithm is not a block cipher
     */
    @Override
    public int getBlockSize() {
        return this.raw.getBlockSize();
    }

    /**
     * Returns the output buffer length required for processing the given input length. This method is designed to be
     * overridden by subclasses. Implementations should calculate the output size based on the cipher's padding and
     * mode.
     *
     * @param len the input length
     * @return the output buffer length
     */
    @Override
    public int getOutputSize(final int len) {
        return this.raw.getOutputSize(len);
    }

    /**
     * Initializes the cipher for encryption or decryption. This method is designed to be overridden by subclasses.
     * Implementations should properly initialize the cipher with the given mode and parameters.
     *
     * @param mode       The operation mode (ENCRYPT_MODE or DECRYPT_MODE).
     * @param parameters The cipher parameters including key and algorithm parameters.
     */
    @Override
    public void init(final Algorithm.Type mode, final Parameters parameters) {
        Assert.isInstanceOf(JceParameters.class, parameters, "Only support JceParameters!");

        try {
            init(mode.getValue(), (JceParameters) parameters);
        } catch (final InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Continues a multi-part encryption or decryption operation (depending on how this cipher was initialized),
     * processing another data part. The first {@code inputLen} bytes in the {@code input} buffer, starting at
     * {@code inputOffset}, are processed, and the result is stored in the output buffer.
     *
     * @param in    The input buffer.
     * @param inOff The offset in the input buffer where the data begins.
     * @param len   The length of the data to process.
     * @return A new buffer containing the result. Returns {@code null} if the underlying cipher is a block cipher and
     *         the input data is too short to produce a new block.
     */
    public byte[] process(final byte[] in, final int inOff, final int len) {
        return this.raw.update(in, inOff, len);
    }

    /**
     * Continues a multi-part encryption or decryption operation (depending on how this cipher was initialized),
     * processing another data part. The first {@code inputLen} bytes in the {@code input} buffer, starting at
     * {@code inputOffset}, are processed, and the result is stored in the {@code output} buffer.
     *
     * @param in    The input buffer.
     * @param inOff The offset in the input buffer where the data begins.
     * @param len   The length of the data to process.
     * @param out   The output buffer where the result should be stored.
     * @return The number of bytes stored in the {@code out} buffer.
     * @throws CryptoException if a {@link ShortBufferException} occurs.
     */
    public int process(final byte[] in, final int inOff, final int len, final byte[] out) {
        try {
            return this.raw.update(in, inOff, len, out);
        } catch (final ShortBufferException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Returns the initialization vector (IV) in a new buffer. This is useful when a random IV is created, or in the
     * context of password-based encryption or decryption where the IV is derived from the user-provided password.
     *
     * @return The initialization vector in a new buffer, or {@code null} if the underlying algorithm does not use an
     *         IV, or if the IV has not been set.
     */
    public byte[] getIV() {
        return this.raw.getIV();
    }

    /**
     * Performs the initialization operation for the underlying {@link javax.crypto.Cipher}.
     *
     * @param mode          The operation mode (e.g., {@link javax.crypto.Cipher#ENCRYPT_MODE},
     *                      {@link javax.crypto.Cipher#DECRYPT_MODE}).
     * @param jceParameters The {@link JceParameters} containing the key, algorithm parameters, and random source.
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid.
     * @throws InvalidKeyException                if the key is invalid.
     */
    public void init(final int mode, final JceParameters jceParameters)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        final javax.crypto.Cipher cipher = this.raw;
        if (null != jceParameters.parameterSpec) {
            if (null != jceParameters.random) {
                cipher.init(mode, jceParameters.key, jceParameters.parameterSpec, jceParameters.random);
            } else {
                cipher.init(mode, jceParameters.key, jceParameters.parameterSpec);
            }
        } else {
            if (null != jceParameters.random) {
                cipher.init(mode, jceParameters.key, jceParameters.random);
            } else {
                cipher.init(mode, jceParameters.key);
            }
        }
    }

    /**
     * Continues a multi-part encryption or decryption operation. This method is designed to be overridden by
     * subclasses. Implementations should process the given input data and return the result.
     *
     * @param in     The input buffer.
     * @param inOff  The offset in the input buffer where the data begins.
     * @param len    The length of the data to process.
     * @param out    The output buffer where the result should be stored.
     * @param outOff The offset in the output buffer where the result should be stored.
     * @return The number of bytes stored in the {@code out} buffer.
     */
    @Override
    public int process(final byte[] in, final int inOff, final int len, final byte[] out, final int outOff) {
        try {
            return this.raw.update(in, inOff, len, out, outOff);
        } catch (final ShortBufferException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Finishes a multi-part encryption or decryption operation. This method is designed to be overridden by subclasses.
     * Implementations should finalize the cipher operation and write any remaining output data.
     *
     * @param out    The output buffer.
     * @param outOff The offset in the output buffer where the result should be stored.
     * @return The number of bytes stored in the {@code out} buffer.
     */
    @Override
    public int doFinal(final byte[] out, final int outOff) {
        try {
            return this.raw.doFinal(out, outOff);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Processes the final data block and finishes the encryption or decryption operation. This method is designed to be
     * overridden by subclasses. Implementations should process the final data block and return the complete result.
     *
     * @param data     The input data.
     * @param inOffset The offset in the input data where processing should begin.
     * @param inputLen The length of the input data to process.
     * @return The result of the encryption or decryption operation.
     */
    @Override
    public byte[] processFinal(final byte[] data, final int inOffset, final int inputLen) {
        try {
            return this.raw.doFinal(data, inOffset, inputLen);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * A wrapper class for JCE's {@link AlgorithmParameterSpec} and related parameters. This class holds the key,
     * algorithm parameter specification, and secure random generator required for initializing a JCE cipher.
     */
    public static class JceParameters implements Parameters {

        /**
         * The cryptographic key.
         */
        private final Key key;
        /**
         * The algorithm-specific parameter specification.
         */
        private final AlgorithmParameterSpec parameterSpec;
        /**
         * The secure random number generator, which can be customized with a seed.
         */
        private final SecureRandom random;

        /**
         * Constructs a {@code JceParameters} instance with the specified key, algorithm parameter specification, and
         * secure random generator.
         *
         * @param key           The cryptographic key.
         * @param parameterSpec The algorithm-specific parameter specification.
         * @param random        The secure random number generator.
         */
        public JceParameters(final Key key, final AlgorithmParameterSpec parameterSpec, final SecureRandom random) {
            this.key = key;
            this.parameterSpec = parameterSpec;
            this.random = random;
        }
    }

}

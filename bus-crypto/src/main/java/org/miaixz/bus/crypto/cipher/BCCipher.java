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
package org.miaixz.bus.crypto.cipher;

import java.util.Arrays;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.crypto.Cipher;

/**
 * An implementation of encryption and decryption based on the BouncyCastle library. This class wraps:
 * <ul>
 * <li>{@link BufferedBlockCipher}</li>
 * <li>{@link BlockCipher}</li>
 * <li>{@link StreamCipher}</li>
 * <li>{@link AEADBlockCipher}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCCipher implements Cipher, Wrapper<Object> {

    /**
     * {@link BufferedBlockCipher}, for block ciphers, including engine, mode, and padding.
     */
    private BufferedBlockCipher bufferedBlockCipher;
    /**
     * {@link BlockCipher}, for block ciphers, generally used for symmetric encryption like AES.
     */
    private BlockCipher blockCipher;
    /**
     * {@link AEADBlockCipher}, for Authenticated Encryption with Associated Data.
     */
    private AEADBlockCipher aeadBlockCipher;
    /**
     * {@link StreamCipher} for stream-based encryption.
     */
    private StreamCipher streamCipher;

    /**
     * Constructor.
     *
     * @param bufferedBlockCipher {@link BufferedBlockCipher}
     */
    public BCCipher(final BufferedBlockCipher bufferedBlockCipher) {
        this.bufferedBlockCipher = Assert.notNull(bufferedBlockCipher);
    }

    /**
     * Constructor.
     *
     * @param blockCipher {@link BlockCipher}
     */
    public BCCipher(final BlockCipher blockCipher) {
        this.blockCipher = Assert.notNull(blockCipher);
    }

    /**
     * Constructor.
     *
     * @param aeadBlockCipher {@link AEADBlockCipher}
     */
    public BCCipher(final AEADBlockCipher aeadBlockCipher) {
        this.aeadBlockCipher = Assert.notNull(aeadBlockCipher);
    }

    /**
     * Constructor.
     *
     * @param streamCipher {@link StreamCipher}
     */
    public BCCipher(final StreamCipher streamCipher) {
        this.streamCipher = Assert.notNull(streamCipher);
    }

    /**
     * Gets the raw underlying BouncyCastle cipher object.
     *
     * @return The raw cipher object.
     */
    @Override
    public Object getRaw() {
        if (null != this.bufferedBlockCipher) {
            return this.bufferedBlockCipher;
        }
        if (null != this.blockCipher) {
            return this.blockCipher;
        }
        if (null != this.aeadBlockCipher) {
            return this.aeadBlockCipher;
        }
        return this.streamCipher;
    }

    /**
     * Gets the name of the algorithm.
     *
     * @return The algorithm name.
     */
    @Override
    public String getAlgorithm() {
        if (null != this.bufferedBlockCipher) {
            return this.bufferedBlockCipher.getUnderlyingCipher().getAlgorithmName();
        }
        if (null != this.blockCipher) {
            return this.blockCipher.getAlgorithmName();
        }
        if (null != this.aeadBlockCipher) {
            return this.aeadBlockCipher.getUnderlyingCipher().getAlgorithmName();
        }
        return this.streamCipher.getAlgorithmName();
    }

    /**
     * Gets the block size of the cipher.
     *
     * @return The block size, or -1 for stream ciphers.
     */
    @Override
    public int getBlockSize() {
        if (null != this.bufferedBlockCipher) {
            return this.bufferedBlockCipher.getBlockSize();
        }
        if (null != this.blockCipher) {
            return this.blockCipher.getBlockSize();
        }
        if (null != this.aeadBlockCipher) {
            return this.aeadBlockCipher.getUnderlyingCipher().getBlockSize();
        }
        return -1;
    }

    /**
     * Initializes the cipher with a mode and parameters.
     *
     * @param mode       The operation mode (encrypt or decrypt).
     * @param parameters The parameters for the cipher.
     */
    @Override
    public void init(final Algorithm.Type mode, final Parameters parameters) {
        Assert.isInstanceOf(BCParameters.class, parameters, "Only support BCParameters!");

        final boolean forEncryption;
        if (mode == Algorithm.Type.ENCRYPT) {
            forEncryption = true;
        } else if (mode == Algorithm.Type.DECRYPT) {
            forEncryption = false;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode.name());
        }
        final CipherParameters cipherParameters = ((BCParameters) parameters).parameters;

        if (null != this.bufferedBlockCipher) {
            this.bufferedBlockCipher.init(forEncryption, cipherParameters);
            return;
        }
        if (null != this.blockCipher) {
            this.blockCipher.init(forEncryption, cipherParameters);
        }
        if (null != this.aeadBlockCipher) {
            this.aeadBlockCipher.init(forEncryption, cipherParameters);
            return;
        }
        this.streamCipher.init(forEncryption, cipherParameters);
    }

    /**
     * Gets the required size for the output buffer.
     *
     * @param len The length of the input data.
     * @return The required output buffer size.
     */
    @Override
    public int getOutputSize(final int len) {
        if (null != this.bufferedBlockCipher) {
            return this.bufferedBlockCipher.getOutputSize(len);
        }
        if (null != this.aeadBlockCipher) {
            return this.aeadBlockCipher.getOutputSize(len);
        }
        return -1;
    }

    /**
     * Processes a block of data.
     *
     * @param in     The input buffer.
     * @param inOff  The offset in the input buffer.
     * @param len    The length of the input data.
     * @param out    The output buffer.
     * @param outOff The offset in the output buffer.
     * @return The number of bytes processed.
     */
    @Override
    public int process(final byte[] in, final int inOff, final int len, final byte[] out, final int outOff) {
        if (null != this.bufferedBlockCipher) {
            return this.bufferedBlockCipher.processBytes(in, inOff, len, out, outOff);
        }
        if (null != this.blockCipher) {
            final byte[] subBytes;
            if (inOff + len < in.length) {
                subBytes = Arrays.copyOf(in, inOff + len);
            } else {
                subBytes = in;
            }
            return this.blockCipher.processBlock(subBytes, inOff, out, outOff);
        }
        if (null != this.aeadBlockCipher) {
            return this.aeadBlockCipher.processBytes(in, inOff, len, out, outOff);
        }
        return this.streamCipher.processBytes(in, inOff, len, out, outOff);
    }

    /**
     * Finishes the encryption/decryption operation.
     *
     * @param out    The output buffer.
     * @param outOff The offset in the output buffer.
     * @return The number of bytes written to the output buffer.
     */
    @Override
    public int doFinal(final byte[] out, final int outOff) {
        if (null != this.bufferedBlockCipher) {
            try {
                return this.bufferedBlockCipher.doFinal(out, outOff);
            } catch (final InvalidCipherTextException e) {
                throw new CryptoException(e);
            }
        }
        if (null != this.aeadBlockCipher) {
            try {
                return this.aeadBlockCipher.doFinal(out, outOff);
            } catch (final InvalidCipherTextException e) {
                throw new CryptoException(e);
            }
        }
        return 0;
    }

    /**
     * A wrapper for BouncyCastle's {@link CipherParameters}.
     */
    public static class BCParameters implements Parameters {

        /**
         * The algorithm parameters.
         */
        protected final CipherParameters parameters;

        /**
         * Constructor.
         *
         * @param parameters {@link CipherParameters}
         */
        public BCParameters(final CipherParameters parameters) {
            this.parameters = parameters;
        }
    }

}

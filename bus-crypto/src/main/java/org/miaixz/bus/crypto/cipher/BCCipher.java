/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.util.Arrays;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.crypto.Cipher;

/**
 * 基于BouncyCastle库封装的加密解密实现，包装包括：
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
     * {@link BufferedBlockCipher}，块加密，包含engine、mode、padding
     */
    private BufferedBlockCipher bufferedBlockCipher;
    /**
     * {@link BlockCipher} 块加密，一般用于AES等对称加密
     */
    private BlockCipher blockCipher;
    /**
     * {@link AEADBlockCipher}, 关联数据的认证加密(Authenticated Encryption with Associated Data)
     */
    private AEADBlockCipher aeadBlockCipher;
    /**
     * {@link StreamCipher}
     */
    private StreamCipher streamCipher;

    /**
     * 构造
     *
     * @param bufferedBlockCipher {@link BufferedBlockCipher}
     */
    public BCCipher(final BufferedBlockCipher bufferedBlockCipher) {
        this.bufferedBlockCipher = Assert.notNull(bufferedBlockCipher);
    }

    /**
     * 构造
     *
     * @param blockCipher {@link BlockCipher}
     */
    public BCCipher(final BlockCipher blockCipher) {
        this.blockCipher = Assert.notNull(blockCipher);
    }

    /**
     * 构造
     *
     * @param aeadBlockCipher {@link AEADBlockCipher}
     */
    public BCCipher(final AEADBlockCipher aeadBlockCipher) {
        this.aeadBlockCipher = Assert.notNull(aeadBlockCipher);
    }

    /**
     * 构造
     *
     * @param streamCipher {@link StreamCipher}
     */
    public BCCipher(final StreamCipher streamCipher) {
        this.streamCipher = Assert.notNull(streamCipher);
    }

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
     * BouncyCastle库的{@link CipherParameters}封装
     */
    public static class BCParameters implements Parameters {
        /**
         * 算法的参数
         */
        protected final CipherParameters parameters;

        /**
         * 构造
         *
         * @param parameters {@link CipherParameters}
         */
        public BCParameters(final CipherParameters parameters) {
            this.parameters = parameters;
        }
    }

}

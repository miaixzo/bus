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
 * 非对称加密算法
 *
 * <pre>
 * 1、签名：使用私钥加密，公钥解密。
 * 用于让所有公钥所有者验证私钥所有者的身份并且用来防止私钥所有者发布的内容被篡改，但是不用来保证内容不被他人获得。
 *
 * 2、加密：用公钥加密，私钥解密。
 * 用于向公钥所有者发布信息,这个信息可能被他人篡改,但是无法被他人获得。
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Crypto extends AbstractCrypto<Crypto> {

    @Serial
    private static final long serialVersionUID = 2852335331758L;

    /**
     * Cipher负责完成加密或解密工作
     */
    protected Cipher cipher;
    /**
     * 加密的块大小
     */
    protected int encryptBlockSize = -1;
    /**
     * 解密的块大小
     */
    protected int decryptBlockSize = -1;
    /**
     * 算法参数
     */
    private AlgorithmParameterSpec algorithmParameterSpec;
    /**
     * 自定义随机数
     */
    private SecureRandom random;

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm {@link Algorithm}
     */
    public Crypto(final Algorithm algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm 算法
     * @param keyPair   密钥对，包含私钥和公钥，如果为{@code null}，则生成随机键值对
     */
    public Crypto(final Algorithm algorithm, final KeyPair keyPair) {
        super(algorithm.getValue(), keyPair);
    }

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Crypto(final String algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm 算法
     * @param keyPair   密钥对，包含私钥和公钥，如果为{@code null}，则生成随机键值对
     */
    public Crypto(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public Crypto(final Algorithm algorithm, final String privateKey, final String publicKey) {
        this(algorithm.getValue(), Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  非对称加密算法
     * @param privateKey 私钥Base64
     * @param publicKey  公钥Base64
     */
    public Crypto(final String algorithm, final String privateKey, final String publicKey) {
        this(algorithm, Base64.decode(privateKey), Base64.decode(publicKey));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(final String algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm, new KeyPair(Keeper.generatePublicKey(algorithm, publicKey),
                Keeper.generatePrivateKey(algorithm, privateKey)));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(final Algorithm algorithm, final PrivateKey privateKey, final PublicKey publicKey) {
        this(algorithm.getValue(), new KeyPair(publicKey, privateKey));
    }

    /**
     * 获取加密块大小
     *
     * @return 加密块大小
     */
    public int getEncryptBlockSize() {
        return encryptBlockSize;
    }

    /**
     * 设置加密块大小
     *
     * @param encryptBlockSize 加密块大小
     */
    public void setEncryptBlockSize(final int encryptBlockSize) {
        this.encryptBlockSize = encryptBlockSize;
    }

    /**
     * 获取解密块大小
     *
     * @return 解密块大小
     */
    public int getDecryptBlockSize() {
        return decryptBlockSize;
    }

    /**
     * 设置解密块大小
     *
     * @param decryptBlockSize 解密块大小
     */
    public void setDecryptBlockSize(final int decryptBlockSize) {
        this.decryptBlockSize = decryptBlockSize;
    }

    /**
     * 获取{@link AlgorithmParameterSpec} 在某些算法中，需要特别的参数，例如在ECIES中，此处为IESParameterSpec
     *
     * @return {@link AlgorithmParameterSpec}
     */
    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.algorithmParameterSpec;
    }

    /**
     * 设置{@link AlgorithmParameterSpec} 在某些算法中，需要特别的参数，例如在ECIES中，此处为IESParameterSpec
     *
     * @param algorithmParameterSpec {@link AlgorithmParameterSpec}
     * @return this
     */
    public Crypto setAlgorithmParameterSpec(final AlgorithmParameterSpec algorithmParameterSpec) {
        this.algorithmParameterSpec = algorithmParameterSpec;
        return this;
    }

    /**
     * 设置随机数生成器，可自定义随机数种子
     *
     * @param random 随机数生成器，可自定义随机数种子
     * @return this
     */
    public Crypto setRandom(final SecureRandom random) {
        this.random = random;
        return this;
    }

    @Override
    public Crypto init(final String algorithm, final KeyPair keyPair) {
        super.init(algorithm, keyPair);
        initCipher();
        return this;
    }

    @Override
    public byte[] encrypt(final byte[] data, final KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Algorithm.Type.ENCRYPT, key);

            if (this.encryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
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

    @Override
    public byte[] decrypt(final byte[] data, final KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Algorithm.Type.DECRYPT, key);

            if (this.decryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
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
     * 获得加密或解密器
     *
     * @return 加密或解密
     */
    public Cipher getCipher() {
        return this.cipher;
    }

    /**
     * 初始化{@link Cipher}，默认尝试加载BC库
     */
    protected void initCipher() {
        this.cipher = new JceCipher(this.algorithm);
    }

    /**
     * 加密或解密
     *
     * @param data         被加密或解密的内容数据
     * @param maxBlockSize 最大块（分段）大小
     * @return 加密或解密后的数据
     * @throws IOException IO异常，不会被触发
     */
    private byte[] doFinal(final byte[] data, final int maxBlockSize) throws IOException {
        // 不足分段
        if (data.length <= maxBlockSize) {
            return this.cipher.processFinal(data, 0, data.length);
        }

        // 分段解密
        return doFinalWithBlock(data, maxBlockSize);
    }

    /**
     * 分段加密或解密
     *
     * @param data         数据
     * @param maxBlockSize 最大分段的段大小，不能为小于1
     * @return 加密或解密后的数据
     * @throws IOException IO异常，不会被触发
     */
    private byte[] doFinalWithBlock(final byte[] data, final int maxBlockSize) throws IOException {
        final int dataLength = data.length;
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();

        int offSet = 0;
        // 剩余长度
        int remainLength = dataLength;
        int blockSize;
        // 对数据分段处理
        while (remainLength > 0) {
            blockSize = Math.min(remainLength, maxBlockSize);
            out.write(this.cipher.processFinal(data, offSet, blockSize));

            offSet += blockSize;
            remainLength = dataLength - offSet;
        }

        return out.toByteArray();
    }

    /**
     * 初始化{@link Cipher}的模式，如加密模式或解密模式
     *
     * @param mode 模式，可选{@link Algorithm.Type#ENCRYPT}或者{@link Algorithm.Type#DECRYPT}
     * @param key  密钥
     * @return {@link Cipher}
     */
    private Cipher initMode(final Algorithm.Type mode, final Key key) {
        final Cipher cipher = this.cipher;
        cipher.init(mode, new JceCipher.JceParameters(key, this.algorithmParameterSpec, this.random));
        return cipher;
    }

}

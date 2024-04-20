/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.crypto.asymmetric;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.CryptoException;
import org.miaixz.bus.core.io.stream.FastByteOutputStream;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Ciphers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

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

    private static final long serialVersionUID = 1L;

    /**
     * Cipher负责完成加密或解密工作
     */
    protected Ciphers ciphers;

    /**
     * 加密的块大小
     */
    protected int encryptBlockSize = -1;
    /**
     * 解密的块大小
     */
    protected int decryptBlockSize = -1;

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm {@link Algorithm}
     */
    public Crypto(Algorithm algorithm) {
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Crypto(String algorithm) {
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm     {@link Algorithm}
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public Crypto(Algorithm algorithm, String privateKeyStr, String publicKeyStr) {
        this(algorithm.getValue(), Builder.decode(privateKeyStr), Builder.decode(publicKeyStr));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(Algorithm algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(Algorithm algorithm, PrivateKey privateKey, PublicKey publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm        非对称加密算法
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     */
    public Crypto(String algorithm, String privateKeyBase64, String publicKeyBase64) {
        this(algorithm, Base64.decode(privateKeyBase64), Base64.decode(publicKeyBase64));
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(String algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm, //
                Builder.generatePrivateKey(algorithm, privateKey),
                Builder.generatePublicKey(algorithm, publicKey)
        );
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Crypto(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
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
    public void setEncryptBlockSize(int encryptBlockSize) {
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
    public void setDecryptBlockSize(int decryptBlockSize) {
        this.decryptBlockSize = decryptBlockSize;
    }

    /**
     * 获取{@link AlgorithmParameterSpec}
     * 在某些算法中，需要特别的参数，例如在ECIES中，此处为IESParameterSpec
     *
     * @return {@link AlgorithmParameterSpec}
     */
    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.ciphers.getParams();
    }

    /**
     * 设置{@link AlgorithmParameterSpec}
     * 在某些算法中，需要特别的参数，例如在ECIES中，此处为IESParameterSpec
     *
     * @param algorithmParameterSpec {@link AlgorithmParameterSpec}
     */
    public void setAlgorithmParameterSpec(AlgorithmParameterSpec algorithmParameterSpec) {
        this.ciphers.setParams(algorithmParameterSpec);
    }

    /**
     * 设置随机数生成器，可自定义随机数种子
     *
     * @param random 随机数生成器，可自定义随机数种子
     * @return this
     */
    public Crypto setRandom(SecureRandom random) {
        this.ciphers.setRandom(random);
        return this;
    }

    @Override
    public Crypto init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super.init(algorithm, privateKey, publicKey);
        initCipher();
        return this;
    }

    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Cipher.ENCRYPT_MODE, key);

            if (this.encryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    this.encryptBlockSize = blockSize;
                }
            }

            return doFinal(data, this.encryptBlockSize < 0 ? data.length : this.encryptBlockSize);
        } catch (Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte[] decrypt(byte[] data, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        lock.lock();
        try {
            final Cipher cipher = initMode(Cipher.DECRYPT_MODE, key);

            if (this.decryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    this.decryptBlockSize = blockSize;
                }
            }

            return doFinal(data, this.decryptBlockSize < 0 ? data.length : this.decryptBlockSize);
        } catch (Exception e) {
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
        return this.ciphers.getRaw();
    }

    /**
     * 初始化{@link Cipher}，默认尝试加载BC库
     */
    protected void initCipher() {
        this.ciphers = new Ciphers(this.algorithm);
    }

    /**
     * 加密或解密
     *
     * @param data         被加密或解密的内容数据
     * @param maxBlockSize 最大块（分段）大小
     * @return 加密或解密后的数据
     * @throws IllegalBlockSizeException 分段异常
     * @throws BadPaddingException       padding错误异常
     * @throws IOException               IO异常，不会被触发
     */
    private byte[] doFinal(byte[] data, int maxBlockSize) throws IllegalBlockSizeException, BadPaddingException, IOException {
        // 模长
        final int dataLength = data.length;

        // 不足分段
        if (dataLength <= maxBlockSize) {
            return getCipher().doFinal(data, 0, dataLength);
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
     * @throws IllegalBlockSizeException 分段异常
     * @throws BadPaddingException       padding错误异常
     * @throws IOException               IO异常，不会被触发
     */
    private byte[] doFinalWithBlock(byte[] data, int maxBlockSize) throws IllegalBlockSizeException, BadPaddingException, IOException {
        final int dataLength = data.length;
        final FastByteOutputStream out = new FastByteOutputStream();

        int offSet = 0;
        // 剩余长度
        int remainLength = dataLength;
        int blockSize;
        // 对数据分段处理
        while (remainLength > 0) {
            blockSize = Math.min(remainLength, maxBlockSize);
            out.write(getCipher().doFinal(data, offSet, blockSize));

            offSet += blockSize;
            remainLength = dataLength - offSet;
        }

        return out.toByteArray();
    }

    /**
     * 初始化{@link Cipher}的模式，如加密模式或解密模式
     *
     * @param mode 模式，可选{@link Cipher#ENCRYPT_MODE}或者{@link Cipher#DECRYPT_MODE}
     * @param key  密钥
     * @throws InvalidAlgorithmParameterException 异常算法错误
     * @throws InvalidKeyException                异常KEY错误
     */
    private Cipher initMode(int mode, Key key) throws InvalidAlgorithmParameterException, InvalidKeyException {
        return this.ciphers.initMode(mode, key).getRaw();
    }

}

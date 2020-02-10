/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.io.FastByteArray;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.crypto.Builder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

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
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class Asymmetric extends Safety<Asymmetric> {

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
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Asymmetric(String algorithm) {
        this(algorithm, (byte[]) null, null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  非对称加密算法
     * @param privateKey 私钥Base64
     * @param publicKey  公钥Base64
     */
    public Asymmetric(String algorithm, String privateKey, String publicKey) {
        this(algorithm, Base64.decode(privateKey), Base64.decode(publicKey));
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Asymmetric(String algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm,
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
    public Asymmetric(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
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

    @Override
    public Asymmetric init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super.init(algorithm, privateKey, publicKey);
        initCipher();
        return this;
    }

    /**
     * 加密
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        final int maxBlockSize = this.encryptBlockSize < 0 ? data.length : this.encryptBlockSize;

        lock.lock();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return doFinal(data, maxBlockSize);
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param data    被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    @Override
    public byte[] decrypt(byte[] data, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        final int maxBlockSize = this.decryptBlockSize < 0 ? data.length : this.decryptBlockSize;

        lock.lock();
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return doFinal(data, maxBlockSize);
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获得加密或解密器
     *
     * @return 加密或解密
     */
    public Cipher getClipher() {
        return cipher;
    }

    /**
     * 初始化{@link Cipher}，默认尝试加载BC库
     */
    protected void initCipher() {
        this.cipher = Builder.createCipher(algorithm);
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
            return this.cipher.doFinal(data, 0, dataLength);
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

        final FastByteArray out = new FastByteArray();

        int offSet = 0;
        // 剩余长度
        int remainLength = dataLength;
        int blockSize;
        // 对数据分段处理
        while (remainLength > 0) {
            blockSize = Math.min(remainLength, maxBlockSize);
            out.write(cipher.doFinal(data, offSet, blockSize));

            offSet += blockSize;
            remainLength = dataLength - offSet;
        }

        return out.toByteArray();
    }

}

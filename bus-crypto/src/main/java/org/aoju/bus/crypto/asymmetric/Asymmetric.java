/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.KeyType;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
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
 * @version 3.2.8
 * @since JDK 1.8
 */
public class Asymmetric extends AbstractAsymmetric<Asymmetric> {

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
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm     {@link ModeType}
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public Asymmetric(String algorithm, String privateKeyStr, String publicKeyStr) {
        this(algorithm, CryptoUtils.decode(privateKeyStr), CryptoUtils.decode(publicKeyStr));
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
    public Asymmetric(String algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm,
                CryptoUtils.generatePrivateKey(algorithm, privateKey),
                CryptoUtils.generatePublicKey(algorithm, publicKey)
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
     * @since 3.1.1
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
        final int inputLen = data.length;
        final int maxBlockSize = this.encryptBlockSize < 0 ? inputLen : this.encryptBlockSize;

        lock.lock();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int offSet = 0;
            byte[] cache;
            // 剩余长度
            int remainLength = inputLen;
            // 对数据分段加密
            while (remainLength > 0) {
                cache = cipher.doFinal(data, offSet, Math.min(remainLength, maxBlockSize));
                out.write(cache, 0, cache.length);

                offSet += maxBlockSize;
                remainLength = inputLen - offSet;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CommonException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param bytes   被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    @Override
    public byte[] decrypt(byte[] bytes, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        // 模长
        final int inputLen = bytes.length;
        final int maxBlockSize = this.decryptBlockSize < 0 ? inputLen : this.decryptBlockSize;

        lock.lock();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            cipher.init(Cipher.DECRYPT_MODE, key);
            int offSet = 0;
            byte[] cache;
            // 剩余长度
            int remainLength = inputLen;
            // 对数据分段解密
            while (remainLength > 0) {
                cache = cipher.doFinal(bytes, offSet, Math.min(remainLength, maxBlockSize));
                out.write(cache, 0, cache.length);

                offSet += maxBlockSize;
                remainLength = inputLen - offSet;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CommonException(e);
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
     *
     * @since 4.5.2
     */
    protected void initCipher() {
        this.cipher = CryptoUtils.createCipher(algorithm);
    }

}

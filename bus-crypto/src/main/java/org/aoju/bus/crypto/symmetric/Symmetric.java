/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.crypto.symmetric;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;
import org.aoju.bus.crypto.Builder;
import org.aoju.bus.crypto.Padding;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEParameterSpec;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.spec.AlgorithmParameterSpec;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对称加密算法
 * 在对称加密算法中,数据发信方将明文（原始数据）和加密密钥一起经过特殊加密算法处理后,使其变成复杂的加密密文发送出去
 * 收信方收到密文后,若想解读原文,则需要使用加密用过的密钥及相同算法的逆算法对密文进行解密,才能使其恢复成可读明文
 * 在对称加密算法中,使用的密钥只有一个,发收信双方都使用这个密钥对数据进行加密和解密,这就要求解密方事先必须知道加密密钥
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class Symmetric {

    /**
     * SecretKey 负责保存对称密钥
     */
    private SecretKey secretKey;
    /**
     * Cipher负责完成加密或解密工作
     */
    private Cipher cipher;
    /**
     * 加密解密参数
     */
    private AlgorithmParameterSpec params;
    /**
     * 是否0填充
     */
    private boolean isZeroPadding;
    private Lock lock = new ReentrantLock();


    /**
     * 构造,使用随机密钥
     *
     * @param algorithm 算法,可以是"algorithm/mode/padding"或者"algorithm"
     */
    public Symmetric(String algorithm) {
        this(algorithm, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public Symmetric(String algorithm, byte[] key) {
        this(algorithm, Builder.generateKey(algorithm, key));
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     * @since 3.1.2
     */
    public Symmetric(String algorithm, SecretKey key) {
        this(algorithm, key, null);
    }

    /**
     * 构造
     *
     * @param algorithm  算法
     * @param key        密钥
     * @param paramsSpec 算法参数,例如加盐等
     * @since 3.3.0
     */
    public Symmetric(String algorithm, SecretKey key, AlgorithmParameterSpec paramsSpec) {
        init(algorithm, key);
        if (null != paramsSpec) {
            setParams(paramsSpec);
        }
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥,如果为<code>null</code>自动生成一个key
     * @return {@link Symmetric}
     */
    public Symmetric init(String algorithm, SecretKey key) {
        Assert.notBlank(algorithm, "'algorithm' must be not blank !");
        this.secretKey = key;

        // 对于PBE算法使用随机数加盐
        if (algorithm.startsWith("PBE")) {
            this.params = new PBEParameterSpec(RandomUtils.randomBytes(8), 100);
        }

        // 检查是否为ZeroPadding,是则替换为NoPadding,并标记以便单独处理
        if (algorithm.contains(Padding.ZeroPadding.name())) {
            algorithm = StringUtils.replace(algorithm, Padding.ZeroPadding.name(), Padding.NoPadding.name());
            this.isZeroPadding = true;
        }

        this.cipher = Builder.createCipher(algorithm);
        return this;
    }

    /**
     * 设置 {@link AlgorithmParameterSpec},通常用于加盐或偏移向量
     *
     * @param params {@link AlgorithmParameterSpec}
     * @return 自身
     */
    public Symmetric setParams(AlgorithmParameterSpec params) {
        this.params = params;
        return this;
    }

    /**
     * 加密
     *
     * @param data 被加密的bytes
     * @return 加密后的bytes
     */
    public byte[] encrypt(byte[] data) {
        lock.lock();
        try {
            if (null == this.params) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, params);
            }
            return cipher.doFinal(paddingDataWithZero(data, cipher.getBlockSize()));
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Hex
     */
    public String encryptHex(byte[] data) {
        return HexUtils.encodeHexStr(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Base64
     */
    public String encryptBase64(byte[] data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, String charset) {
        return encrypt(StringUtils.bytes(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, Charset charset) {
        return encrypt(StringUtils.bytes(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Hex
     */
    public String encryptHex(String data, String charset) {
        return HexUtils.encodeHexStr(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Hex
     */
    public String encryptHex(String data, Charset charset) {
        return HexUtils.encodeHexStr(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Base64
     */
    public String encryptBase64(String data, String charset) {
        return Base64.encode(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Base64
     */
    public String encryptBase64(String data, Charset charset) {
        return Base64.encode(encrypt(data, charset));
    }

    /**
     * 加密,使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data) {
        return encrypt(StringUtils.bytes(data, org.aoju.bus.core.consts.Charset.UTF_8));
    }

    /**
     * 加密,使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Hex
     */
    public String encryptHex(String data) {
        return HexUtils.encodeHexStr(encrypt(data));
    }

    /**
     * 加密,使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Base64
     */
    public String encryptBase64(String data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data 被加密的字符串
     * @return 加密后的bytes
     */
    public byte[] encrypt(InputStream data) {
        return encrypt(IoUtils.readBytes(data));
    }

    /**
     * 加密
     *
     * @param data 被加密的字符串
     * @return 加密后的Hex
     */
    public String encryptHex(InputStream data) {
        return HexUtils.encodeHexStr(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data 被加密的字符串
     * @return 加密后的Base64
     */
    public String encryptBase64(InputStream data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 解密
     *
     * @param bytes 被解密的bytes
     * @return 解密后的bytes
     */
    public byte[] decrypt(byte[] bytes) {
        final int blockSize;
        final byte[] decryptData;

        lock.lock();
        try {
            if (null == this.params) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, params);
            }
            blockSize = cipher.getBlockSize();
            decryptData = cipher.doFinal(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }

        return removePadding(decryptData, blockSize);
    }

    /**
     * 解密为字符串
     *
     * @param bytes   被解密的bytes
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    public String decryptStr(byte[] bytes, Charset charset) {
        return StringUtils.str(decrypt(bytes), charset);
    }

    /**
     * 解密为字符串,默认UTF-8编码
     *
     * @param bytes 被解密的bytes
     * @return 解密后的String
     */
    public String decryptStr(byte[] bytes) {
        return decryptStr(bytes, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串
     *
     * @param data 被解密的String,必须为16进制字符串或Base64表示形式
     * @return 解密后的bytes
     */
    public byte[] decrypt(String data) {
        return decrypt(Builder.decode(data));
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串
     *
     * @param data    被解密的String
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    public String decryptStr(String data, Charset charset) {
        return StringUtils.str(decrypt(data), charset);
    }

    /**
     * 解密Hex表示的字符串,默认UTF-8编码
     *
     * @param data 被解密的String
     * @return 解密后的String
     */
    public String decryptStr(String data) {
        return decryptStr(data, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 解密,不会关闭流
     *
     * @param data 被解密的bytes
     * @return 解密后的bytes
     */
    public byte[] decrypt(InputStream data) {
        return decrypt(IoUtils.readBytes(data));
    }

    /**
     * 解密,不会关闭流
     *
     * @param data    被解密的InputStream
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    public String decryptStr(InputStream data, Charset charset) {
        return StringUtils.str(decrypt(data), charset);
    }

    /**
     * 解密
     *
     * @param data 被解密的InputStream
     * @return 解密后的String
     */
    public String decryptStr(InputStream data) {
        return decryptStr(data, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 获得对称密钥
     *
     * @return 获得对称密钥
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * 获得加密或解密器
     *
     * @return 加密或解密
     */
    public Cipher getCipher() {
        return cipher;
    }

    /**
     * 数据按照blockSize的整数倍长度填充填充0
     *
     * <p>
     * 在{@link Padding#ZeroPadding} 模式下,且数据长度不是blockSize的整数倍才有效,否则返回原数据
     *
     * <p>
     * 见：https://blog.csdn.net/OrangeJack/article/details/82913804
     *
     * @param data      数据
     * @param blockSize 块大小
     * @return 填充后的数据, 如果isZeroPadding为false或长度刚好, 返回原数据
     */
    private byte[] paddingDataWithZero(byte[] data, int blockSize) {
        if (this.isZeroPadding) {
            final int length = data.length;
            // 按照块拆分后的数据中多余的数据
            final int remainLength = length % blockSize;
            if (remainLength > 0) {
                // 新长度为blockSize的整数倍,多余部分填充0
                return ArrayUtils.resize(data, length + blockSize - remainLength);
            }
        }
        return data;
    }

    /**
     * 数据按照blockSize去除填充部分,用于解密
     *
     * <p>
     * 在{@link Padding#ZeroPadding} 模式下,且数据长度不是blockSize的整数倍才有效,否则返回原数据
     *
     * @param data      数据
     * @param blockSize 块大小
     * @return 去除填充后的数据, 如果isZeroPadding为false或长度刚好, 返回原数据
     */
    private byte[] removePadding(byte[] data, int blockSize) {
        if (this.isZeroPadding) {
            final int length = data.length;
            final int remainLength = length % blockSize;
            if (remainLength == 0) {
                // 解码后的数据正好是块大小的整数倍,说明可能存在补0的情况,去掉末尾所有的0
                int i = length - 1;
                while (i >= 0 && 0 == data[i]) {
                    i--;
                }
                return ArrayUtils.resize(data, i + 1);
            }
        }
        return data;
    }

}

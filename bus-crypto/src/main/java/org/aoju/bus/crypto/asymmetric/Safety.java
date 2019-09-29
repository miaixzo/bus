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
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.codec.BCD;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.utils.HexUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.Builder;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public abstract class Safety<T extends Safety<T>> extends Keys<T> {

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
    public Safety(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 加密
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public abstract byte[] encrypt(byte[] data, KeyType keyType);

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(byte[] data, KeyType keyType) {
        return HexUtils.encodeHexStr(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    public String encryptBase64(byte[] data, KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, String charset, KeyType keyType) {
        return encrypt(StringUtils.bytes(data, charset), keyType);
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, Charset charset, KeyType keyType) {
        return encrypt(StringUtils.bytes(data, charset), keyType);
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, KeyType keyType) {
        return encrypt(StringUtils.bytes(data, org.aoju.bus.core.consts.Charset.UTF_8), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(String data, KeyType keyType) {
        return HexUtils.encodeHexStr(encrypt(data, keyType));
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的bytes
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(String data, Charset charset, KeyType keyType) {
        return HexUtils.encodeHexStr(encrypt(data, charset, keyType));
    }

    /**
     * 编码为Base64字符串，使用UTF-8编码
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    public String encryptBase64(String data, KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的字符串
     * @param charset 字符集
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    public String encryptBase64(String data, Charset charset, KeyType keyType) {
        return Base64.encode(encrypt(data, charset, keyType));
    }

    /**
     * 加密
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(InputStream data, KeyType keyType) {
        return encrypt(IoUtils.readBytes(data), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(InputStream data, KeyType keyType) {
        return HexUtils.encodeHexStr(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    public String encryptBase64(InputStream data, KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * 分组加密
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @return 加密后的密文
     */
    public String encryptBcd(String data, KeyType keyType) {
        return encryptBcd(data, keyType, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 分组加密
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 加密后的密文
     */
    public String encryptBcd(String data, KeyType keyType, Charset charset) {
        return BCD.bcdToStr(encrypt(data, charset, keyType));
    }

    /**
     * 解密
     *
     * @param bytes   被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    public abstract byte[] decrypt(byte[] bytes, KeyType keyType);

    /**
     * 解密
     *
     * @param data    被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    public byte[] decrypt(InputStream data, KeyType keyType) {
        return decrypt(IoUtils.readBytes(data), keyType);
    }

    /**
     * 从Hex或Base64字符串解密，编码为UTF-8格式
     *
     * @param data    Hex（16进制）或Base64字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    public byte[] decrypt(String data, KeyType keyType) {
        return decrypt(Builder.decode(data), keyType);
    }

    /**
     * 解密为字符串，密文需为Hex（16进制）或Base64字符串
     *
     * @param data    数据，Hex（16进制）或Base64字符串
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     */
    public String decryptStr(String data, KeyType keyType, Charset charset) {
        return StringUtils.str(decrypt(data, keyType), charset);
    }

    /**
     * 解密为字符串，密文需为Hex（16进制）或Base64字符串
     *
     * @param data    数据，Hex（16进制）或Base64字符串
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public String decryptStr(String data, KeyType keyType) {
        return decryptStr(data, keyType, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 解密BCD
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public byte[] decryptFromBcd(String data, KeyType keyType) {
        return decryptFromBcd(data, keyType, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 分组解密
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     */
    public byte[] decryptFromBcd(String data, KeyType keyType, Charset charset) {
        final byte[] dataBytes = BCD.ascToBcd(StringUtils.bytes(data, charset));
        return decrypt(dataBytes, keyType);
    }

    /**
     * 解密为字符串，密文需为BCD格式
     *
     * @param data    数据，BCD格式
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     */
    public String decryptStrFromBcd(String data, KeyType keyType, Charset charset) {
        return StringUtils.str(decryptFromBcd(data, keyType, charset), charset);
    }

    /**
     * 解密为字符串，密文需为BCD格式，编码为UTF-8格式
     *
     * @param data    数据，BCD格式
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public String decryptStrFromBcd(String data, KeyType keyType) {
        return decryptStrFromBcd(data, keyType, org.aoju.bus.core.consts.Charset.UTF_8);
    }

}

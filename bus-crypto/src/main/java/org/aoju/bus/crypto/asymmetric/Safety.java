/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.codec.BCD;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.HexKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.crypto.Builder;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 抽象的非对称加密对象，包装了加密和解密为Hex和Base64的封装
 *
 * @param <T> 返回自身类型
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
public abstract class Safety<T extends Safety<T>> extends Keys<T> {

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
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
        return HexKit.encodeHexStr(encrypt(data, keyType));
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
        return encrypt(StringKit.bytes(data, charset), keyType);
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, java.nio.charset.Charset charset, KeyType keyType) {
        return encrypt(StringKit.bytes(data, charset), keyType);
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    public byte[] encrypt(String data, KeyType keyType) {
        return encrypt(StringKit.bytes(data, Charset.UTF_8), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(String data, KeyType keyType) {
        return HexKit.encodeHexStr(encrypt(data, keyType));
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的bytes
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(String data, java.nio.charset.Charset charset, KeyType keyType) {
        return HexKit.encodeHexStr(encrypt(data, charset, keyType));
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
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    public String encryptBase64(String data, java.nio.charset.Charset charset, KeyType keyType) {
        return Base64.encode(encrypt(data, charset, keyType));
    }

    /**
     * 加密
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     * @throws InstrumentException IO异常
     */
    public byte[] encrypt(InputStream data, KeyType keyType) throws InstrumentException {
        return encrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    public String encryptHex(InputStream data, KeyType keyType) {
        return HexKit.encodeHexStr(encrypt(data, keyType));
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
     * @throws InstrumentException 加密异常
     */
    public String encryptBcd(String data, KeyType keyType) {
        return encryptBcd(data, keyType, Charset.UTF_8);
    }

    /**
     * 分组加密
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 加密后的密文
     * @throws InstrumentException 加密异常
     */
    public String encryptBcd(String data, KeyType keyType, java.nio.charset.Charset charset) {
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
     * @throws InstrumentException IO异常
     */
    public byte[] decrypt(InputStream data, KeyType keyType) throws InstrumentException {
        return decrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * 从Hex或Base64字符串解密，编码为UTF-8格式
     *
     * @param data    Hex(16进制)或Base64字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    public byte[] decrypt(String data, KeyType keyType) {
        return decrypt(Builder.decode(data), keyType);
    }

    /**
     * 解密为字符串，密文需为Hex(16进制)或Base64字符串
     *
     * @param data    数据，Hex(16进制)或Base64字符串
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     */
    public String decryptStr(String data, KeyType keyType, java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data, keyType), charset);
    }

    /**
     * 解密为字符串，密文需为Hex(16进制)或Base64字符串
     *
     * @param data    数据，Hex(16进制)或Base64字符串
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public String decryptStr(String data, KeyType keyType) {
        return decryptStr(data, keyType, Charset.UTF_8);
    }

    /**
     * 解密BCD
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public byte[] decryptFromBcd(String data, KeyType keyType) {
        return decryptFromBcd(data, keyType, Charset.UTF_8);
    }

    /**
     * 分组解密
     *
     * @param data    数据
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     */
    public byte[] decryptFromBcd(String data, KeyType keyType, java.nio.charset.Charset charset) {
        final byte[] dataBytes = BCD.ascToBcd(StringKit.bytes(data, charset));
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
    public String decryptStrFromBcd(String data, KeyType keyType, java.nio.charset.Charset charset) {
        return StringKit.toString(decryptFromBcd(data, keyType, charset), charset);
    }

    /**
     * 解密为字符串，密文需为BCD格式，编码为UTF-8格式
     *
     * @param data    数据，BCD格式
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    public String decryptStrFromBcd(String data, KeyType keyType) {
        return decryptStrFromBcd(data, keyType, Charset.UTF_8);
    }

}

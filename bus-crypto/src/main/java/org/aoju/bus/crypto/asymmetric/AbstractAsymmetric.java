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

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.utils.HexUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.KeyType;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public abstract class AbstractAsymmetric<T extends AbstractAsymmetric<T>> extends BaseAsymmetric<T> {

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
    public AbstractAsymmetric(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 字符串转BCD码
     *
     * @param asc ASCII字符串
     * @return BCD
     */
    public static byte[] strToBcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len >>= 1;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j;
        int k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * ASCII转BCD
     *
     * @param ascii ASCII byte数组
     * @return BCD
     */
    public static byte[] ascToBcd(byte[] ascii) {
        return ascToBcd(ascii, ascii.length);
    }

    /**
     * ASCII转BCD
     *
     * @param ascii     ASCII byte数组
     * @param ascLength 长度
     * @return BCD
     */
    public static byte[] ascToBcd(byte[] ascii, int ascLength) {
        byte[] bcd = new byte[ascLength / 2];
        int j = 0;
        for (int i = 0; i < (ascLength + 1) / 2; i++) {
            bcd[i] = ascToBcd(ascii[j++]);
            bcd[i] = (byte) (((j >= ascLength) ? 0x00 : ascToBcd(ascii[j++])) + (bcd[i] << 4));
        }
        return bcd;
    }

    /**
     * BCD转ASCII字符串
     *
     * @param bytes BCD byte数组
     * @return ASCII字符串
     */
    public static String bcdToStr(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }

    /**
     * 转换单个byte为BCD
     *
     * @param asc ACSII
     * @return BCD
     */
    private static byte ascToBcd(byte asc) {
        byte bcd;

        if ((asc >= '0') && (asc <= '9')) {
            bcd = (byte) (asc - '0');
        } else if ((asc >= 'A') && (asc <= 'F')) {
            bcd = (byte) (asc - 'A' + 10);
        } else if ((asc >= 'a') && (asc <= 'f')) {
            bcd = (byte) (asc - 'a' + 10);
        } else {
            bcd = (byte) (asc - 48);
        }
        return bcd;
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
     * @since 4.0.1
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
     * @since 4.0.1
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
     * @since 4.0.1
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
     * @since 4.0.1
     */
    public String encryptBase64(String data, KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的字符串
     * @param charset 字符编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     * @since 4.0.1
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
     * @since 4.0.1
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
     * @since 4.0.1
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
     * @since 4.1.0
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
     * @since 4.1.0
     */
    public String encryptBcd(String data, KeyType keyType, Charset charset) {
        return bcdToStr(encrypt(data, charset, keyType));
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
     * @since 4.5.2
     */
    public byte[] decrypt(String data, KeyType keyType) {
        return decrypt(CryptoUtils.decode(data), keyType);
    }

    /**
     * 解密为字符串，密文需为Hex（16进制）或Base64字符串
     *
     * @param data    数据，Hex（16进制）或Base64字符串
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     * @since 4.5.2
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
     * @since 4.5.2
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
     * @since 4.1.0
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
     * @since 4.1.0
     */
    public byte[] decryptFromBcd(String data, KeyType keyType, Charset charset) {
        final byte[] dataBytes = ascToBcd(StringUtils.bytes(data, charset));
        return decrypt(dataBytes, keyType);
    }

    /**
     * 解密为字符串，密文需为BCD格式
     *
     * @param data    数据，BCD格式
     * @param keyType 密钥类型
     * @param charset 加密前编码
     * @return 解密后的密文
     * @since 4.5.2
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
     * @since 4.5.2
     */
    public String decryptStrFromBcd(String data, KeyType keyType) {
        return decryptStrFromBcd(data, keyType, org.aoju.bus.core.consts.Charset.UTF_8);
    }
}

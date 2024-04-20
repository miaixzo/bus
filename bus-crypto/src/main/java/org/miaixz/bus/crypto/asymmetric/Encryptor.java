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
import org.miaixz.bus.core.toolkit.HexKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 非对称加密器接口，提供：
 * <ul>
 *     <li>加密为bytes</li>
 *     <li>加密为Hex(16进制)</li>
 *     <li>加密为Base64</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Encryptor {

    /**
     * 加密
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    byte[] encrypt(byte[] data, KeyType keyType);

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    default String encryptHex(byte[] data, KeyType keyType) {
        return HexKit.encodeHexString(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    default String encryptBase64(byte[] data, KeyType keyType) {
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
    default byte[] encrypt(String data, String charset, KeyType keyType) {
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
    default byte[] encrypt(String data, Charset charset, KeyType keyType) {
        return encrypt(StringKit.bytes(data, charset), keyType);
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    default byte[] encrypt(String data, KeyType keyType) {
        return encrypt(StringKit.bytes(data), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    default String encryptHex(String data, KeyType keyType) {
        return HexKit.encodeHexString(encrypt(data, keyType));
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的bytes
     * @param charset 编码
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    default String encryptHex(String data, Charset charset, KeyType keyType) {
        return HexKit.encodeHexString(encrypt(data, charset, keyType));
    }

    /**
     * 编码为Base64字符串，使用UTF-8编码
     *
     * @param data    被加密的字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    default String encryptBase64(String data, KeyType keyType) {
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
    default String encryptBase64(String data, Charset charset, KeyType keyType) {
        return Base64.encode(encrypt(data, charset, keyType));
    }

    /**
     * 加密
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     * @throws CryptoException IO异常
     */
    default byte[] encrypt(InputStream data, KeyType keyType) throws CryptoException {
        return encrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * 编码为Hex字符串
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Hex字符串
     */
    default String encryptHex(InputStream data, KeyType keyType) {
        return HexKit.encodeHexString(encrypt(data, keyType));
    }

    /**
     * 编码为Base64字符串
     *
     * @param data    被加密的数据流
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return Base64字符串
     */
    default String encryptBase64(InputStream data, KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

}

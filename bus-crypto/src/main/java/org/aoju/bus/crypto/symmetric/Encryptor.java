/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.crypto.symmetric;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.exception.CryptoException;
import org.aoju.bus.core.toolkit.HexKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 对称加密器接口，提供：
 * <ul>
 *     <li>加密为bytes</li>
 *     <li>加密为Hex(16进制)</li>
 *     <li>加密为Base64</li>
 * </ul>
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public interface Encryptor {

    /**
     * 加密
     *
     * @param data 被加密的bytes
     * @return 加密后的bytes
     */
    byte[] encrypt(byte[] data);

    /**
     * 加密，针对大数据量，可选结束后是否关闭流
     *
     * @param data    被加密的字符串
     * @param out     输出流，可以是文件或网络位置
     * @param isClose 是否关闭流
     */
    void encrypt(InputStream data, OutputStream out, boolean isClose);

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Hex
     */
    default String encryptHex(byte[] data) {
        return HexKit.encodeHexStr(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Base64
     */
    default String encryptBase64(byte[] data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的bytes
     */
    default byte[] encrypt(String data, String charset) {
        return encrypt(StringKit.bytes(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的bytes
     */
    default byte[] encrypt(String data, Charset charset) {
        return encrypt(StringKit.bytes(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Hex
     */
    default String encryptHex(String data, String charset) {
        return HexKit.encodeHexStr(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Hex
     */
    default String encryptHex(String data, Charset charset) {
        return HexKit.encodeHexStr(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Base64
     */
    default String encryptBase64(String data, String charset) {
        return Base64.encode(encrypt(data, charset));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Base64
     */
    default String encryptBase64(String data, Charset charset) {
        return Base64.encode(encrypt(data, charset));
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的bytes
     */
    default byte[] encrypt(String data) {
        return encrypt(StringKit.bytes(data, org.aoju.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Hex
     */
    default String encryptHex(String data) {
        return HexKit.encodeHexStr(encrypt(data));
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Base64
     */
    default String encryptBase64(String data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 加密，加密后关闭流
     *
     * @param data 被加密的字符串
     * @return 加密后的bytes
     * @throws CryptoException IO异常
     */
    default byte[] encrypt(InputStream data) throws CryptoException {
        return encrypt(IoKit.readBytes(data));
    }

    /**
     * 加密
     *
     * @param data 被加密的字符串
     * @return 加密后的Hex
     */
    default String encryptHex(InputStream data) {
        return HexKit.encodeHexStr(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data 被加密的字符串
     * @return 加密后的Base64
     */
    default String encryptBase64(InputStream data) {
        return Base64.encode(encrypt(data));
    }

}

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
package org.miaixz.bus.crypto.builtin.symmetric;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.crypto.Builder;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 对称解密器接口，提供：
 * <ul>
 *     <li>从bytes解密</li>
 *     <li>从Hex(16进制)解密</li>
 *     <li>从Base64解密</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SymmetricDecryptor {

    /**
     * 解密
     *
     * @param bytes 被解密的bytes
     * @return 解密后的bytes
     */
    byte[] decrypt(byte[] bytes);

    /**
     * 解密，针对大数据量，结束后不关闭流
     *
     * @param data    加密的字符串
     * @param out     输出流，可以是文件或网络位置
     * @param isClose 是否关闭流，包括输入和输出流
     * @throws InternalException IO异常
     */
    void decrypt(InputStream data, OutputStream out, boolean isClose);

    /**
     * 解密为字符串
     *
     * @param bytes   被解密的bytes
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    default String decryptString(final byte[] bytes, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(bytes), charset);
    }

    /**
     * 解密为字符串，默认UTF-8编码
     *
     * @param bytes 被解密的bytes
     * @return 解密后的String
     */
    default String decryptString(final byte[] bytes) {
        return decryptString(bytes, Charset.UTF_8);
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串
     *
     * @param data 被解密的String，必须为16进制字符串或Base64表示形式
     * @return 解密后的bytes
     */
    default byte[] decrypt(final String data) {
        return decrypt(Builder.decode(data));
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串
     *
     * @param data    被解密的String
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    default String decryptString(final String data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data), charset);
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串，默认UTF-8编码
     *
     * @param data 被解密的String
     * @return 解密后的String
     */
    default String decryptString(final String data) {
        return decryptString(data, Charset.UTF_8);
    }

    /**
     * 解密，会关闭流
     *
     * @param data 被解密的bytes
     * @return 解密后的bytes
     * @throws InternalException IO异常
     */
    default byte[] decrypt(final InputStream data) throws InternalException {
        return decrypt(IoKit.readBytes(data));
    }

    /**
     * 解密，不会关闭流
     *
     * @param data    被解密的InputStream
     * @param charset 解密后的charset
     * @return 解密后的String
     */
    default String decryptString(final InputStream data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data), charset);
    }

    /**
     * 解密
     *
     * @param data 被解密的InputStream
     * @return 解密后的String
     */
    default String decryptString(final InputStream data) {
        return decryptString(data, Charset.UTF_8);
    }

}

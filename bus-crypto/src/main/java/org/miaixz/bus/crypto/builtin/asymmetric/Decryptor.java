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
package org.miaixz.bus.crypto.builtin.asymmetric;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

import java.io.InputStream;

/**
 * 非对称解密器接口，提供：
 * <ul>
 *     <li>从bytes解密</li>
 *     <li>从Hex(16进制)解密</li>
 *     <li>从Base64解密</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Decryptor {

    /**
     * 解密
     *
     * @param bytes   被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    byte[] decrypt(byte[] bytes, KeyType keyType);

    /**
     * 解密
     *
     * @param data    被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     * @throws InternalException IO异常
     */
    default byte[] decrypt(final InputStream data, final KeyType keyType) throws InternalException {
        return decrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * 从Hex或Base64字符串解密，编码为UTF-8格式
     *
     * @param data    Hex（16进制）或Base64字符串
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    default byte[] decrypt(final String data, final KeyType keyType) {
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
    default String decryptString(final String data, final KeyType keyType, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data, keyType), charset);
    }

    /**
     * 解密为字符串，密文需为Hex（16进制）或Base64字符串
     *
     * @param data    数据，Hex（16进制）或Base64字符串
     * @param keyType 密钥类型
     * @return 解密后的密文
     */
    default String decryptString(final String data, final KeyType keyType) {
        return decryptString(data, keyType, Charset.UTF_8);
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto.center;

import java.io.*;
import java.security.MessageDigest;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * MAC摘要算法（此类兼容和JCE的 {@code javax.crypto.Mac}对象和BC库的{@code org.bouncycastle.crypto.Mac}对象） MAC，全称为“Message
 * Authentication Code”，中文名“消息鉴别码” 主要是利用指定算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出。 一般的，消息鉴别码用于验证传输于两个共同享有一个密钥的单位之间的消息。
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mac implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852337666506L;
    /**
     * Mac引擎
     */
    private final org.miaixz.bus.crypto.builtin.digest.mac.Mac engine;

    /**
     * 构造
     *
     * @param engine MAC算法实现引擎
     */
    public Mac(final org.miaixz.bus.crypto.builtin.digest.mac.Mac engine) {
        this.engine = engine;
    }

    /**
     * 获得MAC算法引擎
     *
     * @return MAC算法引擎
     */
    public org.miaixz.bus.crypto.builtin.digest.mac.Mac getEngine() {
        return this.engine;
    }

    /**
     * 生成文件摘要
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public byte[] digest(final String data, final java.nio.charset.Charset charset) {
        return digest(ByteKit.toBytes(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public byte[] digest(final String data) {
        return digest(data, Charset.UTF_8);
    }

    /**
     * 生成文件摘要，并转为Base64
     *
     * @param data      被摘要数据
     * @param isUrlSafe 是否使用URL安全字符
     * @return 摘要
     */
    public String digestBase64(final String data, final boolean isUrlSafe) {
        return digestBase64(data, Charset.UTF_8, isUrlSafe);
    }

    /**
     * 生成文件摘要，并转为Base64
     *
     * @param data      被摘要数据
     * @param charset   编码
     * @param isUrlSafe 是否使用URL安全字符
     * @return 摘要
     */
    public String digestBase64(final String data, final java.nio.charset.Charset charset, final boolean isUrlSafe) {
        final byte[] digest = digest(data, charset);
        return isUrlSafe ? Base64.encodeUrlSafe(digest) : Base64.encode(digest);
    }

    /**
     * 生成文件摘要，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public String digestHex(final String data, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(digest(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(final String data) {
        return digestHex(data, Charset.UTF_8);
    }

    /**
     * 生成文件摘要 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param file 被摘要文件
     * @return 摘要bytes
     * @throws CryptoException Cause by IOException
     */
    public byte[] digest(final File file) throws CryptoException {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return digest(in);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 生成文件摘要，并转为16进制字符串 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param file 被摘要文件
     * @return 摘要
     */
    public String digestHex(final File file) {
        return HexKit.encodeString(digest(file));
    }

    /**
     * 生成摘要
     *
     * @param data 数据bytes
     * @return 摘要bytes
     */
    public byte[] digest(final byte[] data) {
        return digest(new ByteArrayInputStream(data), -1);
    }

    /**
     * 生成摘要，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(final byte[] data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * 生成摘要，使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data {@link InputStream} 数据流
     * @return 摘要bytes
     */
    public byte[] digest(final InputStream data) {
        return digest(data, Normal._8192);
    }

    /**
     * 生成摘要，并转为16进制字符串 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(final InputStream data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#_8192} 做为默认值
     * @return 摘要bytes
     */
    public byte[] digest(final InputStream data, final int bufferLength) {
        return this.engine.digest(data, bufferLength);
    }

    /**
     * 生成摘要，并转为16进制字符串 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data         被摘要数据
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#_8192} 做为默认值
     * @return 摘要
     */
    public String digestHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(digest(data, bufferLength));
    }

    /**
     * 验证生成的摘要与给定的摘要比较是否一致 简单比较每个byte位是否相同
     *
     * @param digest          生成的摘要
     * @param digestToCompare 需要比较的摘要
     * @return 是否一致
     * @see MessageDigest#isEqual(byte[], byte[])
     */
    public boolean verify(final byte[] digest, final byte[] digestToCompare) {
        return MessageDigest.isEqual(digest, digestToCompare);
    }

    /**
     * 获取MAC算法块长度
     *
     * @return MAC算法块长度
     */
    public int getMacLength() {
        return this.engine.getMacLength();
    }

    /**
     * 获取算法
     *
     * @return 算法
     */
    public String getAlgorithm() {
        return this.engine.getAlgorithm();
    }

}

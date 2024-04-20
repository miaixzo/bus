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
package org.miaixz.bus.crypto.digest.mac;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.CryptoException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.HexKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;

/**
 * MAC摘要算法（此类兼容和JCE的 {@code javax.crypto.Mac}对象和BC库的{@code org.bouncycastle.crypto.Mac}对象）
 * MAC，全称为“Message Authentication Code”，中文名“消息鉴别码” 主要是利用指定算法，以一个密钥和一个消息为输入，生
 * 成一个消息摘要作为输出一般的，消息鉴别码用于验证传输于两个共同享有一个密钥的单位之间的消息
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mac implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MacEngine engine;

    /**
     * 构造
     *
     * @param engine MAC算法实现引擎
     */
    public Mac(MacEngine engine) {
        this.engine = engine;
    }

    /**
     * 获得MAC算法引擎
     *
     * @return MAC算法引擎
     */
    public MacEngine getEngine() {
        return this.engine;
    }

    /**
     * 生成文件摘要
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public byte[] digest(String data, java.nio.charset.Charset charset) {
        return digest(StringKit.bytes(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public byte[] digest(String data) {
        return digest(data, Charset.UTF_8);
    }

    /**
     * 生成文件摘要，并转为Base64
     *
     * @param data      被摘要数据
     * @param isUrlSafe 是否使用URL安全字符
     * @return 摘要
     */
    public String digestBase64(String data, boolean isUrlSafe) {
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
    public String digestBase64(String data, java.nio.charset.Charset charset, boolean isUrlSafe) {
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
    public String digestHex(String data, java.nio.charset.Charset charset) {
        return HexKit.encodeHexString(digest(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(String data) {
        return digestHex(data, Charset.UTF_8);
    }

    /**
     * 生成文件摘要
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param file 被摘要文件
     * @return 摘要bytes
     * @throws CryptoException Cause by IOException
     */
    public byte[] digest(File file) throws CryptoException {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return digest(in);
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * 生成文件摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param file 被摘要文件
     * @return 摘要
     */
    public String digestHex(File file) {
        return HexKit.encodeHexString(digest(file));
    }

    /**
     * 生成摘要
     *
     * @param data 数据bytes
     * @return 摘要bytes
     */
    public byte[] digest(byte[] data) {
        return digest(new ByteArrayInputStream(data), -1);
    }

    /**
     * 生成摘要，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(byte[] data) {
        return HexKit.encodeHexString(digest(data));
    }

    /**
     * 生成摘要，使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data {@link InputStream} 数据流
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data) {
        return digest(data, IoKit.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(InputStream data) {
        return HexKit.encodeHexString(digest(data));
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link IoKit#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data, int bufferLength) {
        return this.engine.digest(data, bufferLength);
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data         被摘要数据
     * @param bufferLength 缓存长度，不足1使用 {@link IoKit#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要
     */
    public String digestHex(InputStream data, int bufferLength) {
        return HexKit.encodeHexString(digest(data, bufferLength));
    }

    /**
     * 验证生成的摘要与给定的摘要比较是否一致
     * 简单比较每个byte位是否相同
     *
     * @param digest          生成的摘要
     * @param digestToCompare 需要比较的摘要
     * @return 是否一致
     * @see MessageDigest#isEqual(byte[], byte[])
     */
    public boolean verify(byte[] digest, byte[] digestToCompare) {
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

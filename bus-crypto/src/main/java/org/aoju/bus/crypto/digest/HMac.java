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
package org.aoju.bus.crypto.digest;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.exception.CryptoException;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.HexKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.crypto.digest.mac.MacEngine;
import org.aoju.bus.crypto.digest.mac.MacEngineFactory;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

/**
 * HMAC摘要算法
 * HMAC，全称为“Hash Message Authentication Code”，中文名“散列消息鉴别码”
 * 主要是利用哈希算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出
 * 一般的，消息鉴别码用于验证传输于两个共 同享有一个密钥的单位之间的消息
 * HMAC 可以与任何迭代散列函数捆绑使用MD5 和 SHA-1 就是这种散列函数HMAC 还可以使用一个用于计算和确认消息鉴别值的密钥
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public class HMac implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MacEngine engine;

    /**
     * 构造，自动生成密钥
     *
     * @param algorithm 算法 {@link Algorithm}
     */
    public HMac(Algorithm algorithm) {
        this(algorithm, (Key) null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法 {@link Algorithm}
     * @param key       密钥
     */
    public HMac(Algorithm algorithm, byte[] key) {
        this(algorithm.getValue(), key);
    }

    /**
     * 构造
     *
     * @param algorithm 算法 {@link Algorithm}
     * @param key       密钥
     */
    public HMac(Algorithm algorithm, Key key) {
        this(algorithm.getValue(), key);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public HMac(String algorithm, byte[] key) {
        this(algorithm, new SecretKeySpec(key, algorithm));
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public HMac(String algorithm, Key key) {
        this(algorithm, key, null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     * @param spec      {@link AlgorithmParameterSpec}
     */
    public HMac(String algorithm, Key key, AlgorithmParameterSpec spec) {
        this(MacEngineFactory.createEngine(algorithm, key, spec));
    }

    /**
     * 构造
     *
     * @param engine MAC算法实现引擎
     */
    public HMac(MacEngine engine) {
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
    public byte[] digest(String data, Charset charset) {
        return digest(StringKit.bytes(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public byte[] digest(String data) {
        return digest(data, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 生成文件摘要，并转为Base64
     *
     * @param data      被摘要数据
     * @param isUrlSafe 是否使用URL安全字符
     * @return 摘要
     */
    public String digestBase64(String data, boolean isUrlSafe) {
        return digestBase64(data, org.aoju.bus.core.lang.Charset.UTF_8, isUrlSafe);
    }

    /**
     * 生成文件摘要，并转为Base64
     *
     * @param data      被摘要数据
     * @param charset   编码
     * @param isUrlSafe 是否使用URL安全字符
     * @return 摘要
     */
    public String digestBase64(String data, Charset charset, boolean isUrlSafe) {
        return StringKit.toString(Base64.encode(digest(data, charset), false, isUrlSafe));
    }

    /**
     * 生成文件摘要，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public String digestHex(String data, Charset charset) {
        return HexKit.encodeHexStr(digest(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(String data) {
        return digestHex(data, org.aoju.bus.core.lang.Charset.UTF_8);
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
        return HexKit.encodeHexStr(digest(file));
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
        return HexKit.encodeHexStr(digest(data));
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
        return HexKit.encodeHexStr(digest(data));
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
        return HexKit.encodeHexStr(digest(data, bufferLength));
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

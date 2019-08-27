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
package org.aoju.bus.crypto.digest;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.HexUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * HMAC摘要算法
 * HMAC，全称为“Hash Message Authentication Code”，中文名“散列消息鉴别码”
 * 主要是利用哈希算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出。
 * 一般的，消息鉴别码用于验证传输于两个共 同享有一个密钥的单位之间的消息。
 * HMAC 可以与任何迭代散列函数捆绑使用。MD5 和 SHA-1 就是这种散列函数。HMAC 还可以使用一个用于计算和确认消息鉴别值的密钥。
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public class HMac {

    private Mac mac;
    private SecretKey secretKey;

    /**
     * 构造，自动生成密钥
     *
     * @param algorithm 算法 {@link ModeType}
     */
    public HMac(String algorithm) {
        this(algorithm, (SecretKey) null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法 {@link ModeType}
     * @param key       密钥
     */
    public HMac(String algorithm, byte[] key) {
        init(algorithm, key);
    }

    /**
     * 构造
     *
     * @param algorithm 算法 {@link ModeType}
     * @param key       密钥
     */
    public HMac(String algorithm, SecretKey key) {
        init(algorithm, key);
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥
     * @return {@link HMac}
     * @throws CommonException Cause by IOException
     */
    public HMac init(String algorithm, byte[] key) {
        return init(algorithm, (null == key) ? null : new SecretKeySpec(key, algorithm));
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥 {@link SecretKey}
     * @return {@link HMac}
     * @throws CommonException Cause by IOException
     */
    public HMac init(String algorithm, SecretKey key) {
        try {
            mac = Mac.getInstance(algorithm);
            if (null != key) {
                this.secretKey = key;
            } else {
                this.secretKey = CryptoUtils.generateKey(algorithm);
            }
            mac.init(this.secretKey);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return this;
    }

    /**
     * 生成文件摘要
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public byte[] digest(String data, String charset) {
        return digest(StringUtils.bytes(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public byte[] digest(String data) {
        return digest(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 生成文件摘要，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public String digestHex(String data, String charset) {
        return HexUtils.encodeHexStr(digest(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(String data) {
        return digestHex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 生成文件摘要
     * 使用默认缓存大小，见 {@link IoUtils#DEFAULT_BUFFER_SIZE}
     *
     * @param file 被摘要文件
     * @return 摘要bytes
     * @throws CommonException Cause by IOException
     */
    public byte[] digest(File file) throws CommonException {
        InputStream in = null;
        try {
            in = FileUtils.getInputStream(file);
            return digest(in);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 生成文件摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoUtils#DEFAULT_BUFFER_SIZE}
     *
     * @param file 被摘要文件
     * @return 摘要
     */
    public String digestHex(File file) {
        return HexUtils.encodeHexStr(digest(file));
    }

    /**
     * 生成摘要
     *
     * @param data 数据bytes
     * @return 摘要bytes
     */
    public byte[] digest(byte[] data) {
        byte[] result;
        try {
            result = mac.doFinal(data);
        } finally {
            mac.reset();
        }
        return result;
    }

    /**
     * 生成摘要，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(byte[] data) {
        return HexUtils.encodeHexStr(digest(data));
    }

    /**
     * 生成摘要，使用默认缓存大小，见 {@link IoUtils#DEFAULT_BUFFER_SIZE}
     *
     * @param data {@link InputStream} 数据流
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data) {
        return digest(data, IoUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoUtils#DEFAULT_BUFFER_SIZE}
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(InputStream data) {
        return HexUtils.encodeHexStr(digest(data));
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link IoUtils#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = IoUtils.DEFAULT_BUFFER_SIZE;
        }
        byte[] buffer = new byte[bufferLength];

        byte[] result = null;
        try {
            int read = data.read(buffer, 0, bufferLength);

            while (read > -1) {
                mac.update(buffer, 0, read);
                read = data.read(buffer, 0, bufferLength);
            }
            result = mac.doFinal();
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            mac.reset();
        }
        return result;
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoUtils#DEFAULT_BUFFER_SIZE}
     *
     * @param data         被摘要数据
     * @param bufferLength 缓存长度，不足1使用 {@link IoUtils#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要
     */
    public String digestHex(InputStream data, int bufferLength) {
        return HexUtils.encodeHexStr(digest(data, bufferLength));
    }

    /**
     * 获得 {@link Mac}
     *
     * @return {@link Mac}
     */
    public Mac getMac() {
        return mac;
    }

    /**
     * 获得密钥
     *
     * @return 密钥
     * @since 3.0.3
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

}

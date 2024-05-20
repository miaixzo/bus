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
package org.miaixz.bus.crypto.builtin.digest;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Holder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.Provider;

/**
 * 摘要算法
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Digester extends SimpleWrapper<MessageDigest> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 盐值
     */
    protected byte[] salt;
    /**
     * 加盐位置，即将盐值字符串放置在数据的index数，默认0
     */
    protected int saltPosition;
    /**
     * 散列次数
     */
    protected int digestCount;

    /**
     * 构造
     *
     * @param algorithm 算法枚举
     */
    public Digester(final Algorithm algorithm) {
        this(algorithm.getValue());
    }

    /**
     * 构造
     *
     * @param algorithm 算法枚举
     */
    public Digester(final String algorithm) {
        this(algorithm, null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param provider  算法提供者，{@code null}表示使用{@link Holder}找到的提供方。
     */
    public Digester(final Algorithm algorithm, final Provider provider) {
        this(algorithm.getValue(), provider);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param provider  算法提供者，{@code null}表示使用{@link Holder}找到的提供方。
     */
    public Digester(final String algorithm, final Provider provider) {
        this(Builder.createMessageDigest(algorithm, provider));
    }

    /**
     * 构造
     *
     * @param messageDigest {@link MessageDigest}
     */
    public Digester(final MessageDigest messageDigest) {
        super(messageDigest);
    }

    /**
     * 设置加盐内容
     *
     * @param salt 盐值
     * @return this
     */
    public Digester setSalt(final byte[] salt) {
        this.salt = salt;
        return this;
    }

    /**
     * 设置加盐的位置，只有盐值存在时有效
     * 加盐的位置指盐位于数据byte数组中的位置，例如：
     *
     * <pre>
     * data: 0123456
     * </pre>
     * <p>
     * 则当saltPosition = 2时，盐位于data的1和2中间，即第二个空隙，即：
     *
     * <pre>
     * data: 01[salt]23456
     * </pre>
     *
     * @param saltPosition 盐的位置
     * @return this
     */
    public Digester setSaltPosition(final int saltPosition) {
        this.saltPosition = saltPosition;
        return this;
    }

    /**
     * 设置重复计算摘要值次数
     *
     * @param digestCount 摘要值次数
     * @return this
     */
    public Digester setDigestCount(final int digestCount) {
        this.digestCount = digestCount;
        return this;
    }

    /**
     * 重置{@link MessageDigest}
     *
     * @return this
     */
    public Digester reset() {
        this.raw.reset();
        return this;
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
     * 生成文件摘要
     * 使用默认缓存大小，见 {@link Normal#DEFAULT_BUFFER_SIZE}
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
     * 生成文件摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link Normal#DEFAULT_BUFFER_SIZE}
     *
     * @param file 被摘要文件
     * @return 摘要
     */
    public String digestHex(final File file) {
        return HexKit.encodeString(digest(file));
    }

    /**
     * 生成摘要，考虑加盐和重复摘要次数
     *
     * @param data 数据bytes
     * @return 摘要bytes
     */
    public byte[] digest(final byte[] data) {
        final byte[] result;
        if (this.saltPosition <= 0) {
            // 加盐在开头，自动忽略空盐值
            result = doDigest(this.salt, data);
        } else if (this.saltPosition >= data.length) {
            // 加盐在末尾，自动忽略空盐值
            result = doDigest(data, this.salt);
        } else if (ArrayKit.isNotEmpty(this.salt)) {
            final MessageDigest digest = this.raw;
            // 加盐在中间
            digest.update(data, 0, this.saltPosition);
            digest.update(this.salt);
            digest.update(data, this.saltPosition, data.length - this.saltPosition);
            result = digest.digest();
        } else {
            // 无加盐
            result = doDigest(data);
        }

        return resetAndRepeatDigest(result);
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
     * 生成摘要，使用默认缓存大小，见 {@link Normal#DEFAULT_BUFFER_SIZE}
     *
     * @param data {@link InputStream} 数据流
     * @return 摘要bytes
     */
    public byte[] digest(final InputStream data) {
        return digest(data, Normal.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link Normal#DEFAULT_BUFFER_SIZE}
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
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     * @throws InternalException IO异常
     */
    public byte[] digest(final InputStream data, int bufferLength) throws InternalException {
        if (bufferLength < 1) {
            bufferLength = Normal.DEFAULT_BUFFER_SIZE;
        }

        final byte[] result;
        try {
            if (ArrayKit.isEmpty(this.salt)) {
                result = digestWithoutSalt(data, bufferLength);
            } else {
                result = digestWithSalt(data, bufferLength);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return resetAndRepeatDigest(result);
    }

    /**
     * 生成摘要，并转为16进制字符串
     * 使用默认缓存大小，见 {@link Normal#DEFAULT_BUFFER_SIZE}
     *
     * @param data         被摘要数据
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要
     */
    public String digestHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(digest(data, bufferLength));
    }

    /**
     * 获取散列长度，0表示不支持此方法
     *
     * @return 散列长度，0表示不支持此方法
     */
    public int getDigestLength() {
        return this.raw.getDigestLength();
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     * @throws IOException 从流中读取数据引发的IO异常
     */
    private byte[] digestWithoutSalt(final InputStream data, final int bufferLength) throws IOException {
        final MessageDigest digest = this.raw;
        final byte[] buffer = new byte[bufferLength];
        int read;
        while ((read = data.read(buffer, 0, bufferLength)) > -1) {
            digest.update(buffer, 0, read);
        }
        return digest.digest();
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     * @throws IOException 从流中读取数据引发的IO异常
     */
    private byte[] digestWithSalt(final InputStream data, final int bufferLength) throws IOException {
        final MessageDigest digest = this.raw;
        if (this.saltPosition <= 0) {
            // 加盐在开头
            digest.update(this.salt);
        }

        final byte[] buffer = new byte[bufferLength];
        int total = 0;
        int read;
        while ((read = data.read(buffer, 0, bufferLength)) > -1) {
            total += read;
            if (this.saltPosition > 0 && total >= this.saltPosition) {
                if (total != this.saltPosition) {
                    digest.update(buffer, 0, total - this.saltPosition);
                }
                // 加盐在中间
                digest.update(this.salt);
                digest.update(buffer, total - this.saltPosition, read);
            } else {
                digest.update(buffer, 0, read);
            }
        }

        if (total < this.saltPosition) {
            // 加盐在末尾
            digest.update(this.salt);
        }

        return digest.digest();
    }

    /**
     * 生成摘要
     *
     * @param datas 数据bytes
     * @return 摘要bytes
     */
    private byte[] doDigest(final byte[]... datas) {
        final MessageDigest digest = this.raw;
        for (final byte[] data : datas) {
            if (null != data) {
                digest.update(data);
            }
        }
        return digest.digest();
    }

    /**
     * 重复计算摘要，取决于{@link #digestCount} 值
     * 每次计算摘要前都会重置{@link #digest}
     *
     * @param digestData 第一次摘要过的数据
     * @return 摘要
     */
    private byte[] resetAndRepeatDigest(byte[] digestData) {
        final int digestCount = Math.max(1, this.digestCount);
        reset();
        for (int i = 0; i < digestCount - 1; i++) {
            digestData = doDigest(digestData);
            reset();
        }
        return digestData;
    }

}

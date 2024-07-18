/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto.builtin.digest.mac;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;

import java.io.IOException;
import java.io.InputStream;

/**
 * MAC（Message Authentication Code）算法引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Mac {

    /**
     * 加入需要被摘要的内容
     *
     * @param in 内容
     */
    default void update(final byte[] in) {
        update(in, 0, in.length);
    }

    /**
     * 加入需要被摘要的内容
     *
     * @param in    内容
     * @param inOff 内容起始位置
     * @param len   内容长度
     */
    void update(byte[] in, int inOff, int len);

    /**
     * 结束并生成摘要
     *
     * @return 摘要内容
     */
    byte[] doFinal();

    /**
     * 重置
     */
    void reset();

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#_8192} 做为默认值
     * @return 摘要bytes
     */
    default byte[] digest(final InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = Normal._8192;
        }

        final byte[] buffer = new byte[bufferLength];

        byte[] result;
        try {
            int read = data.read(buffer, 0, bufferLength);

            while (read > -1) {
                update(buffer, 0, read);
                read = data.read(buffer, 0, bufferLength);
            }
            result = doFinal();
        } catch (final IOException e) {
            throw new CryptoException(e);
        } finally {
            reset();
        }
        return result;
    }

    /**
     * 获取MAC算法块大小
     *
     * @return MAC算法块大小
     */
    int getMacLength();

    /**
     * 获取当前算法
     *
     * @return 算法
     */
    String getAlgorithm();

}

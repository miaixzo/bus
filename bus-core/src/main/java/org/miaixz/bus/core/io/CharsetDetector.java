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
package org.miaixz.bus.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * 编码探测器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharsetDetector {

    /**
     * 默认的参与测试的编码
     */
    private static final Charset[] DEFAULT_CHARSETS;

    static {
        final String[] names = { "UTF-8", "GBK", "GB2312", "GB18030", "UTF-16BE", "UTF-16LE", "UTF-16", "BIG5",
                "UNICODE", "US-ASCII" };
        DEFAULT_CHARSETS = Convert.convert(Charset[].class, names);
    }

    /**
     * 探测文件编码
     *
     * @param file     文件
     * @param charsets 需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     */
    public static Charset detect(final File file, final Charset... charsets) {
        return detect(FileKit.getInputStream(file), charsets);
    }

    /**
     * 探测编码 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用支持reset方法的流
     *
     * @param in       流，使用后关闭此流
     * @param charsets 需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     */
    public static Charset detect(final InputStream in, final Charset... charsets) {
        return detect(Normal._8192, in, charsets);
    }

    /**
     * 探测编码 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用支持reset方法的流
     *
     * @param bufferSize 自定义缓存大小，即每次检查的长度
     * @param in         流，使用后关闭此流
     * @param charsets   需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     */
    public static Charset detect(final int bufferSize, final InputStream in, Charset... charsets) {
        if (ArrayKit.isEmpty(charsets)) {
            charsets = DEFAULT_CHARSETS;
        }

        final byte[] buffer = new byte[bufferSize];
        try {
            while (in.read(buffer) > -1) {
                for (final Charset charset : charsets) {
                    final CharsetDecoder decoder = charset.newDecoder();
                    if (identify(buffer, decoder)) {
                        return charset;
                    }
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
        return null;
    }

    /**
     * 通过try的方式测试指定bytes是否可以被解码，从而判断是否为指定编码
     *
     * @param bytes   测试的bytes
     * @param decoder 解码器
     * @return 是否是指定编码
     */
    private static boolean identify(final byte[] bytes, final CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (final CharacterCodingException e) {
            return false;
        }
        return true;
    }

}

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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;

/**
 * {@link ByteBuffer} 、{@link CharBuffer}工具类
 * 此工具来自于 t-io 项目以及其它项目的相关部分收集
 * ByteBuffer的相关介绍见：https://www.cnblogs.com/ruber/p/6857159.html
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BufferKit {

    /**
     * {@link ByteBuffer} 转byte数组
     *
     * @param bytebuffer {@link ByteBuffer}
     * @return byte数组
     */
    public static byte[] toBytes(final ByteBuffer bytebuffer) {
        if (bytebuffer.hasArray()) {
            return Arrays.copyOfRange(bytebuffer.array(), bytebuffer.position(), bytebuffer.limit());
        } else {
            final int oldPosition = bytebuffer.position();
            bytebuffer.position(0);
            final int size = bytebuffer.limit();
            final byte[] buffers = new byte[size];
            bytebuffer.get(buffers);
            bytebuffer.position(oldPosition);
            return buffers;
        }
    }

    /**
     * 拷贝到一个新的ByteBuffer
     *
     * @param src   源ByteBuffer
     * @param start 起始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的ByteBuffer
     */
    public static ByteBuffer copy(final ByteBuffer src, final int start, final int end) {
        return copy(src, ByteBuffer.allocate(end - start));
    }

    /**
     * 拷贝ByteBuffer
     *
     * @param src  源ByteBuffer
     * @param dest 目标ByteBuffer
     * @return 目标ByteBuffer
     */
    public static ByteBuffer copy(final ByteBuffer src, final ByteBuffer dest) {
        return copy(src, dest, Math.min(src.limit(), dest.remaining()));
    }

    /**
     * 拷贝ByteBuffer
     *
     * @param src    源ByteBuffer
     * @param dest   目标ByteBuffer
     * @param length 长度
     * @return 目标ByteBuffer
     */
    public static ByteBuffer copy(final ByteBuffer src, final ByteBuffer dest, final int length) {
        return copy(src, src.position(), dest, dest.position(), length);
    }

    /**
     * 拷贝ByteBuffer
     *
     * @param src       源ByteBuffer
     * @param srcStart  源开始的位置
     * @param dest      目标ByteBuffer
     * @param destStart 目标开始的位置
     * @param length    长度
     * @return 目标ByteBuffer
     */
    public static ByteBuffer copy(final ByteBuffer src, final int srcStart, final ByteBuffer dest, final int destStart, final int length) {
        System.arraycopy(src.array(), srcStart, dest.array(), destStart, length);
        return dest;
    }

    /**
     * 读取剩余部分并转为UTF-8编码字符串
     *
     * @param buffer ByteBuffer
     * @return 字符串
     */
    public static String readString(final ByteBuffer buffer) {
        return readString(buffer, Charset.UTF_8);
    }

    /**
     * 读取剩余部分并转为字符串
     *
     * @param buffer  ByteBuffer
     * @param charset 编码
     * @return 字符串
     */
    public static String readString(final ByteBuffer buffer, final java.nio.charset.Charset charset) {
        return StringKit.toString(readBytes(buffer), charset);
    }

    /**
     * 读取剩余部分bytes
     *
     * @param buffer ByteBuffer
     * @return bytes
     */
    public static byte[] readBytes(final ByteBuffer buffer) {
        final int remaining = buffer.remaining();
        final byte[] ab = new byte[remaining];
        buffer.get(ab);
        return ab;
    }

    /**
     * 读取指定长度的bytes
     * 如果长度不足，则读取剩余部分，此时buffer必须为读模式
     *
     * @param buffer    ByteBuffer
     * @param maxLength 最大长度
     * @return bytes
     */
    public static byte[] readBytes(final ByteBuffer buffer, int maxLength) {
        final int remaining = buffer.remaining();
        if (maxLength > remaining) {
            maxLength = remaining;
        }
        final byte[] ab = new byte[maxLength];
        buffer.get(ab);
        return ab;
    }

    /**
     * 读取指定区间的数据
     *
     * @param buffer {@link ByteBuffer}
     * @param start  开始位置
     * @param end    结束位置
     * @return bytes
     */
    public static byte[] readBytes(final ByteBuffer buffer, final int start, final int end) {
        final byte[] bs = new byte[end - start];
        System.arraycopy(buffer.array(), start, bs, 0, bs.length);
        return bs;
    }

    /**
     * 一行的末尾位置，查找位置时位移ByteBuffer到结束位置
     *
     * @param buffer {@link ByteBuffer}
     * @return 末尾位置，未找到或达到最大长度返回-1
     */
    public static int lineEnd(final ByteBuffer buffer) {
        return lineEnd(buffer, buffer.remaining());
    }

    /**
     * 一行的末尾位置，查找位置时位移ByteBuffer到结束位置
     * 支持的换行符如下：
     *
     * <pre>
     * 1. \r\n
     * 2. \n
     * </pre>
     *
     * @param buffer    {@link ByteBuffer}
     * @param maxLength 读取最大长度
     * @return 末尾位置，未找到或达到最大长度返回-1
     */
    public static int lineEnd(final ByteBuffer buffer, final int maxLength) {
        final int primitivePosition = buffer.position();
        boolean canEnd = false;
        int charIndex = primitivePosition;
        byte b;
        while (buffer.hasRemaining()) {
            b = buffer.get();
            charIndex++;
            if (b == Symbol.C_CR) {
                canEnd = true;
            } else if (b == Symbol.C_LF) {
                return canEnd ? charIndex - 2 : charIndex - 1;
            } else {
                // 只有\r无法确认换行
                canEnd = false;
            }

            if (charIndex - primitivePosition > maxLength) {
                // 查找到尽头，未找到，还原位置
                buffer.position(primitivePosition);
                throw new IndexOutOfBoundsException(StringKit.format("Position is out of maxLength: {}", maxLength));
            }
        }

        // 查找到buffer尽头，未找到，还原位置
        buffer.position(primitivePosition);
        // 读到结束位置
        return -1;
    }

    /**
     * 读取一行，如果buffer中最后一部分并非完整一行，则返回null
     * 支持的换行符如下：
     *
     * <pre>
     * 1. \r\n
     * 2. \n
     * </pre>
     *
     * @param buffer  ByteBuffer
     * @param charset 编码
     * @return 一行
     */
    public static String readLine(final ByteBuffer buffer, final java.nio.charset.Charset charset) {
        final int startPosition = buffer.position();
        final int endPosition = lineEnd(buffer);

        if (endPosition > startPosition) {
            final byte[] bs = readBytes(buffer, startPosition, endPosition);
            return StringKit.toString(bs, charset);
        } else if (endPosition == startPosition) {
            return Normal.EMPTY;
        }

        return null;
    }

    /**
     * 创建新Buffer
     *
     * @param data 数据
     * @return {@link ByteBuffer}
     */
    public static ByteBuffer of(final byte[] data) {
        return ByteBuffer.wrap(data);
    }

    /**
     * 从字符串创建新Buffer
     *
     * @param data    数据
     * @param charset 编码
     * @return {@link ByteBuffer}
     */
    public static ByteBuffer of(final CharSequence data, final java.nio.charset.Charset charset) {
        return of(ByteKit.toBytes(data, charset));
    }

    /**
     * 创建{@link CharBuffer}
     *
     * @param capacity 容量
     * @return {@link CharBuffer}
     */
    public static CharBuffer ofCharBuffer(final int capacity) {
        return CharBuffer.allocate(capacity);
    }

}

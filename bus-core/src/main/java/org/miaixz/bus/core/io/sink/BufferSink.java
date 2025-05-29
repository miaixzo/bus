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
package org.miaixz.bus.core.io.sink;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;

/**
 * 缓冲接收器接口，提供高效的写操作，内部维护缓冲区以减少性能开销。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BufferSink extends Sink, WritableByteChannel {

    /**
     * 获取内部缓冲区。
     *
     * @return 内部缓冲区对象
     */
    Buffer buffer();

    /**
     * 写入字节字符串。
     *
     * @param byteString 字节字符串
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink write(ByteString byteString) throws IOException;

    /**
     * 写入完整的字节数组。
     *
     * @param source 字节数组
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink write(byte[] source) throws IOException;

    /**
     * 写入字节数组的指定部分。
     *
     * @param source    字节数组
     * @param offset    起始偏移量
     * @param byteCount 写入字节数
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink write(byte[] source, int offset, int byteCount) throws IOException;

    /**
     * 从源读取所有字节并写入接收器。
     *
     * @param source 数据源
     * @return 读取的字节数，源耗尽时返回 0
     * @throws IOException 如果读取或写入失败
     */
    long writeAll(Source source) throws IOException;

    /**
     * 从源读取指定字节数并写入接收器。
     *
     * @param source    数据源
     * @param byteCount 要读取的字节数
     * @return 当前接收器
     * @throws IOException 如果读取或写入失败
     */
    BufferSink write(Source source, long byteCount) throws IOException;

    /**
     * 使用 UTF-8 编码写入字符串。
     *
     * @param string 输入字符串
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeUtf8(String string) throws IOException;

    /**
     * 使用 UTF-8 编码写入字符串的指定部分。
     *
     * @param string      输入字符串
     * @param beginIndex  起始索引
     * @param endIndex    结束索引
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException;

    /**
     * 使用 UTF-8 编码写入 Unicode 码点。
     *
     * @param codePoint Unicode 码点
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeUtf8CodePoint(int codePoint) throws IOException;

    /**
     * 使用指定字符集编码写入字符串。
     *
     * @param string  输入字符串
     * @param charset 字符集
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeString(String string, Charset charset) throws IOException;

    /**
     * 使用指定字符集编码写入字符串的指定部分。
     *
     * @param string      输入字符串
     * @param beginIndex  起始索引
     * @param endIndex    结束索引
     * @param charset     字符集
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException;

    /**
     * 写入单个字节。
     *
     * @param b 字节值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeByte(int b) throws IOException;

    /**
     * 使用大端序写入短整型（2 字节）。
     *
     * @param s 短整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeShort(int s) throws IOException;

    /**
     * 使用小端序写入短整型（2 字节）。
     *
     * @param s 短整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeShortLe(int s) throws IOException;

    /**
     * 使用大端序写入整型（4 字节）。
     *
     * @param i 整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeInt(int i) throws IOException;

    /**
     * 使用小端序写入整型（4 字节）。
     *
     * @param i 整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeIntLe(int i) throws IOException;

    /**
     * 使用大端序写入长整型（8 字节）。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeLong(long v) throws IOException;

    /**
     * 使用小端序写入长整型（8 字节）。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeLongLe(long v) throws IOException;

    /**
     * 以十进制形式写入长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeDecimalLong(long v) throws IOException;

    /**
     * 以十六进制形式写入无符号长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink writeHexadecimalUnsignedLong(long v) throws IOException;

    /**
     * 将缓冲数据写入底层接收器，并递归刷新以推送数据到最终目标。
     *
     * @throws IOException 如果写入或刷新失败
     */
    @Override
    void flush() throws IOException;

    /**
     * 将缓冲数据写入底层接收器，较弱的刷新操作，确保数据向目标移动。
     *
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink emit() throws IOException;

    /**
     * 将完整的缓冲段写入底层接收器，限制缓冲区内存占用。
     *
     * @return 当前接收器
     * @throws IOException 如果写入失败
     */
    BufferSink emitCompleteSegments() throws IOException;

    /**
     * 获取写入此接收器的输出流。
     *
     * @return 输出流
     */
    OutputStream outputStream();

}
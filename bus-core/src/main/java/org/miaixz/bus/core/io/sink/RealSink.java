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

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * 实现缓冲接收器，管理字节流的写入操作，支持多种数据格式的写入并适配底层接收器。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealSink implements BufferSink {

    /**
     * 内部缓冲区
     */
    public final Buffer buffer = new Buffer();

    /**
     * 底层接收器
     */
    public final Sink sink;

    /**
     * 是否已关闭
     */
    boolean closed;

    /**
     * 构造方法，初始化接收器。
     *
     * @param sink 底层接收器
     * @throws NullPointerException 如果 sink 为 null
     */
    public RealSink(Sink sink) {
        if (null == sink) {
            throw new NullPointerException("sink == null");
        }
        this.sink = sink;
    }

    /**
     * 获取内部缓冲区。
     *
     * @return 内部缓冲区
     */
    @Override
    public Buffer buffer() {
        return buffer;
    }

    /**
     * 从源缓冲区写入指定字节数。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要写入的字节数
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source, byteCount);
        emitCompleteSegments();
    }

    /**
     * 写入字节字符串。
     *
     * @param byteString 字节字符串
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink write(ByteString byteString) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(byteString);
        return emitCompleteSegments();
    }

    /**
     * 使用 UTF-8 编码写入字符串。
     *
     * @param string 输入字符串
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeUtf8(String string) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8(string);
        return emitCompleteSegments();
    }

    /**
     * 使用 UTF-8 编码写入字符串的指定部分。
     *
     * @param string      输入字符串
     * @param beginIndex  起始索引
     * @param endIndex    结束索引
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8(string, beginIndex, endIndex);
        return emitCompleteSegments();
    }

    /**
     * 使用 UTF-8 编码写入 Unicode 码点。
     *
     * @param codePoint Unicode 码点
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8CodePoint(codePoint);
        return emitCompleteSegments();
    }

    /**
     * 使用指定字符集编码写入字符串。
     *
     * @param string  输入字符串
     * @param charset 字符集
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeString(String string, Charset charset) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeString(string, charset);
        return emitCompleteSegments();
    }

    /**
     * 使用指定字符集编码写入字符串的指定部分。
     *
     * @param string      输入字符串
     * @param beginIndex  起始索引
     * @param endIndex    结束索引
     * @param charset     字符集
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeString(string, beginIndex, endIndex, charset);
        return emitCompleteSegments();
    }

    /**
     * 写入字节数组。
     *
     * @param source 字节数组
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink write(byte[] source) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source);
        return emitCompleteSegments();
    }

    /**
     * 写入字节数组的指定部分。
     *
     * @param source    字节数组
     * @param offset    起始偏移量
     * @param byteCount 写入字节数
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source, offset, byteCount);
        return emitCompleteSegments();
    }

    /**
     * 从 ByteBuffer 写入数据。
     *
     * @param source 数据源 ByteBuffer
     * @return 写入的字节数
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public int write(ByteBuffer source) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        int result = buffer.write(source);
        emitCompleteSegments();
        return result;
    }

    /**
     * 从源读取所有数据并写入接收器。
     *
     * @param source 数据源
     * @return 读取的总字节数
     * @throws IOException              如果读取或写入失败
     * @throws IllegalArgumentException 如果 source 为 null
     */
    @Override
    public long writeAll(Source source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(buffer, SectionBuffer.SIZE)) != -1;) {
            totalBytesRead += readCount;
            emitCompleteSegments();
        }
        return totalBytesRead;
    }

    /**
     * 从源读取指定字节数并写入接收器。
     *
     * @param source    数据源
     * @param byteCount 要读取的字节数
     * @return 当前接收器
     * @throws IOException              如果读取或写入失败
     * @throws EOFException             如果源数据不足
     */
    @Override
    public BufferSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(buffer, byteCount);
            if (read == -1)
                throw new EOFException();
            byteCount -= read;
            emitCompleteSegments();
        }
        return this;
    }

    /**
     * 写入单个字节。
     *
     * @param b 字节值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeByte(int b) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeByte(b);
        return emitCompleteSegments();
    }

    /**
     * 使用大端序写入短整型。
     *
     * @param s 短整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeShort(int s) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeShort(s);
        return emitCompleteSegments();
    }

    /**
     * 使用小端序写入短整型。
     *
     * @param s 短整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeShortLe(int s) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeShortLe(s);
        return emitCompleteSegments();
    }

    /**
     * 使用大端序写入整型。
     *
     * @param i 整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeInt(int i) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeInt(i);
        return emitCompleteSegments();
    }

    /**
     * 使用小端序写入整型。
     *
     * @param i 整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeIntLe(int i) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeIntLe(i);
        return emitCompleteSegments();
    }

    /**
     * 使用大端序写入长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeLong(v);
        return emitCompleteSegments();
    }

    /**
     * 使用小端序写入长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeLongLe(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeLongLe(v);
        return emitCompleteSegments();
    }

    /**
     * 以十进制形式写入长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeDecimalLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeDecimalLong(v);
        return emitCompleteSegments();
    }

    /**
     * 以十六进制形式写入无符号长整型。
     *
     * @param v 长整型值
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeHexadecimalUnsignedLong(v);
        return emitCompleteSegments();
    }

    /**
     * 将完整的缓冲段写入底层接收器。
     *
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink emitCompleteSegments() throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        long byteCount = buffer.completeSegmentByteCount();
        if (byteCount > 0)
            sink.write(buffer, byteCount);
        return this;
    }

    /**
     * 将所有缓冲数据写入底层接收器。
     *
     * @return 当前接收器
     * @throws IOException              如果写入失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public BufferSink emit() throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        long byteCount = buffer.size();
        if (byteCount > 0)
            sink.write(buffer, byteCount);
        return this;
    }

    /**
     * 获取适配此接收器的输出流。
     *
     * @return 输出流
     */
    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (closed)
                    throw new IOException("closed");
                buffer.writeByte((byte) b);
                emitCompleteSegments();
            }

            @Override
            public void write(byte[] data, int offset, int byteCount) throws IOException {
                if (closed)
                    throw new IOException("closed");
                buffer.write(data, offset, byteCount);
                emitCompleteSegments();
            }

            @Override
            public void flush() throws IOException {
                if (!closed) {
                    RealSink.this.flush();
                }
            }

            @Override
            public void close() {
                RealSink.this.close();
            }

            @Override
            public String toString() {
                return RealSink.this + ".outputStream()";
            }
        };
    }

    /**
     * 刷新缓冲区，将所有数据写入底层接收器。
     *
     * @throws IOException              如果刷新失败
     * @throws IllegalStateException    如果接收器已关闭
     */
    @Override
    public void flush() throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        if (buffer.size > 0) {
            sink.write(buffer, buffer.size);
        }
        sink.flush();
    }

    /**
     * 检查接收器是否打开。
     *
     * @return 是否打开
     */
    @Override
    public boolean isOpen() {
        return !closed;
    }

    /**
     * 关闭接收器，释放资源。
     *
     * @throws IOException 如果关闭失败
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        Throwable thrown = null;
        try {
            if (buffer.size > 0) {
                sink.write(buffer, buffer.size);
            }
        } catch (Throwable e) {
            thrown = e;
        }
        try {
            sink.close();
        } catch (Throwable e) {
            if (null == thrown)
                thrown = e;
        }
        closed = true;
        if (null != thrown)
            IoKit.sneakyRethrow(thrown);
    }

    /**
     * 获取底层接收器的超时配置。
     *
     * @return 超时对象
     */
    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 字符串表示，包含底层接收器信息
     */
    @Override
    public String toString() {
        return "buffer(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}
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
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * GZIP 格式压缩接收器，使用 {@link Deflater} 进行压缩，仅在必要时调用 flush 以优化性能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GzipSink implements Sink {

    /**
     * 接收 GZIP 格式数据的缓冲接收器
     */
    private final BufferSink sink;

    /**
     * 用于压缩数据的压缩器
     */
    private final Deflater deflater;

    /**
     * 负责在解压源和压缩接收器之间移动数据的压缩接收器
     */
    private final DeflaterSink deflaterSink;

    /**
     * 计算压缩数据的 CRC32 校验和
     */
    private final CRC32 crc = new CRC32();

    /**
     * 是否已关闭
     */
    private boolean closed;

    /**
     * 构造方法，初始化 GZIP 接收器。
     *
     * @param sink 底层接收器
     * @throws IllegalArgumentException 如果 sink 为 null
     */
    public GzipSink(Sink sink) {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        this.deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.sink = IoKit.buffer(sink);
        this.deflaterSink = new DeflaterSink(this.sink, deflater);
        writeHeader();
    }

    /**
     * 从源缓冲区读取指定字节数并写入压缩数据。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要读取的字节数
     * @throws IOException              如果写入失败
     * @throws IllegalArgumentException 如果 byteCount 小于 0
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0)
            return;
        updateCrc(source, byteCount);
        deflaterSink.write(source, byteCount);
    }

    /**
     * 刷新缓冲区，推送压缩数据到目标。
     *
     * @throws IOException 如果刷新失败
     */
    @Override
    public void flush() throws IOException {
        deflaterSink.flush();
    }

    /**
     * 获取接收器的超时配置。
     *
     * @return 超时对象
     */
    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    /**
     * 关闭接收器，完成压缩并释放资源。
     *
     * @throws IOException 如果关闭失败
     */
    @Override
    public void close() throws IOException {
        if (closed)
            return;
        Throwable thrown = null;
        try {
            deflaterSink.finishDeflate();
            writeFooter();
        } catch (Throwable e) {
            thrown = e;
        }
        try {
            deflater.end();
        } catch (Throwable e) {
            if (thrown == null)
                thrown = e;
        }
        try {
            sink.close();
        } catch (Throwable e) {
            if (thrown == null)
                thrown = e;
        }
        closed = true;
        if (thrown != null) {
            IoKit.sneakyRethrow(thrown);
        }
    }

    /**
     * 获取压缩器以访问统计信息、字典、压缩级别等。
     *
     * @return 压缩器对象
     */
    public final Deflater deflater() {
        return deflater;
    }

    /**
     * 写入 GZIP 文件头。
     */
    private void writeHeader() {
        Buffer buffer = this.sink.buffer();
        buffer.writeShort(0x1f8b); // Two-byte Gzip ID.
        buffer.writeByte(0x08); // 8 == Deflate compression method.
        buffer.writeByte(0x00); // No flags.
        buffer.writeInt(0x00); // No modification time.
        buffer.writeByte(0x00); // No extra flags.
        buffer.writeByte(0x00); // No OS.
    }

    /**
     * 写入 GZIP 文件尾，包括 CRC32 校验和和原始数据长度。
     *
     * @throws IOException 如果写入失败
     */
    private void writeFooter() throws IOException {
        // 原始数据的 CRC32
        sink.writeIntLe((int) crc.getValue());
        // 原始数据长度
        sink.writeIntLe((int) deflater.getBytesRead());
    }

    /**
     * 更新 CRC32 校验和。
     *
     * @param buffer    数据缓冲区
     * @param byteCount 要更新的字节数
     */
    private void updateCrc(Buffer buffer, long byteCount) {
        for (SectionBuffer head = buffer.head; byteCount > 0; head = head.next) {
            int segmentLength = (int) Math.min(byteCount, head.limit - head.pos);
            crc.update(head.data, head.pos, segmentLength);
            byteCount -= segmentLength;
        }
    }

}
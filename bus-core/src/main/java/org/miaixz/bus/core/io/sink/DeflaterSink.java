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
import java.util.zip.Deflater;

import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * 使用 Deflater 进行压缩的接收器，立即压缩并写入缓冲数据，调用 flush 会导致压缩率降低。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeflaterSink implements Sink {

    /**
     * 底层缓冲接收器
     */
    private final BufferSink sink;

    /**
     * 压缩器
     */
    private final Deflater deflater;

    /**
     * 是否已关闭
     */
    private boolean closed;

    /**
     * 构造方法，使用指定的接收器和压缩器。
     *
     * @param sink     底层接收器
     * @param deflater 压缩器
     */
    public DeflaterSink(Sink sink, Deflater deflater) {
        this(IoKit.buffer(sink), deflater);
    }

    /**
     * 构造方法，使用指定的缓冲接收器和压缩器。
     *
     * @param sink     缓冲接收器
     * @param deflater 压缩器
     * @throws IllegalArgumentException 如果 sink 或 deflater 为 null
     */
    DeflaterSink(BufferSink sink, Deflater deflater) {
        if (sink == null)
            throw new IllegalArgumentException("source == null");
        if (deflater == null)
            throw new IllegalArgumentException("inflater == null");
        this.sink = sink;
        this.deflater = deflater;
    }

    /**
     * 从源缓冲区读取指定字节数并压缩写入接收器。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要读取的字节数
     * @throws IOException 如果写入失败
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            SectionBuffer head = source.head;
            int toDeflate = (int) Math.min(byteCount, head.limit - head.pos);
            deflater.setInput(head.data, head.pos, toDeflate);
            deflate(false);
            source.size -= toDeflate;
            head.pos += toDeflate;
            if (head.pos == head.limit) {
                source.head = head.pop();
                LifeCycle.recycle(head);
            }
            byteCount -= toDeflate;
        }
    }

    /**
     * 压缩数据到接收器。
     *
     * @param syncFlush 是否同步刷新
     * @throws IOException 如果压缩或写入失败
     */
    private void deflate(boolean syncFlush) throws IOException {
        Buffer buffer = sink.buffer();
        while (true) {
            SectionBuffer s = buffer.writableSegment(1);
            int deflated = syncFlush
                    ? deflater.deflate(s.data, s.limit, SectionBuffer.SIZE - s.limit, Deflater.SYNC_FLUSH)
                    : deflater.deflate(s.data, s.limit, SectionBuffer.SIZE - s.limit);
            if (deflated > 0) {
                s.limit += deflated;
                buffer.size += deflated;
                sink.emitCompleteSegments();
            } else if (deflater.needsInput()) {
                if (s.pos == s.limit) {
                    buffer.head = s.pop();
                    LifeCycle.recycle(s);
                }
                return;
            }
        }
    }

    /**
     * 刷新缓冲区，立即压缩并写入所有数据。
     *
     * @throws IOException 如果刷新失败
     */
    @Override
    public void flush() throws IOException {
        deflate(true);
        sink.flush();
    }

    /**
     * 完成压缩过程，写入剩余数据。
     *
     * @throws IOException 如果写入失败
     */
    void finishDeflate() throws IOException {
        deflater.finish();
        deflate(false);
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
            finishDeflate();
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
        if (thrown != null)
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
     * @return 字符串表示，包含类名和底层接收器信息
     */
    @Override
    public String toString() {
        return "DeflaterSink(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}
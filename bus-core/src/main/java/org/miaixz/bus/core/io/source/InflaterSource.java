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
package org.miaixz.bus.core.io.source;

import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * 使用<a href="http://tools.ietf.org/html/rfc1951">DEFLATE</a> 解压缩从另一个源读取的数据的源。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InflaterSource implements Source {

    private final BufferSource source;
    private final Inflater inflater;

    /**
     * 当调用 Inflater.setInput() 时，inflater 会保留字节数组，直到再次需要输入。
     */
    private int bufferBytesHeldByInflater;
    private boolean closed;

    public InflaterSource(Source source, Inflater inflater) {
        this(IoKit.buffer(source), inflater);
    }

    /**
     * 此包私有构造函数与其受信任的调用者共享一个缓冲区。 一般来说，我们不能共享 BufferedSource，因为 inflater 会保留输入字节，直到它们被溢出为止。
     *
     * @param source   缓冲源
     * @param inflater 缓冲区
     */
    InflaterSource(BufferSource source, Inflater inflater) {
        if (source == null)
            throw new IllegalArgumentException("source == null");
        if (inflater == null)
            throw new IllegalArgumentException("inflater == null");
        this.source = source;
        this.inflater = inflater;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed)
            throw new IllegalStateException("closed");
        if (byteCount == 0)
            return 0;

        while (true) {
            boolean sourceExhausted = refill();

            // 将缓冲区的压缩数据解压到接收器中
            try {
                SectionBuffer tail = sink.writableSegment(1);
                int toRead = (int) Math.min(byteCount, SectionBuffer.SIZE - tail.limit);
                int bytesInflated = inflater.inflate(tail.data, tail.limit, toRead);
                if (bytesInflated > 0) {
                    tail.limit += bytesInflated;
                    sink.size += bytesInflated;
                    return bytesInflated;
                }
                if (inflater.finished() || inflater.needsDictionary()) {
                    releaseInflatedBytes();
                    if (tail.pos == tail.limit) {
                        // 分配了一个尾段，但最终并不需要它。回收！
                        sink.head = tail.pop();
                        LifeCycle.recycle(tail);
                    }
                    return -1;
                }
                if (sourceExhausted)
                    throw new EOFException("source exhausted prematurely");
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * 如果需要输入，则用压缩数据重新填充缓冲区。（并且仅在需要输入时才有效） 如果缓冲区需要输入但源已耗尽，则返回 true。
     */
    public boolean refill() throws IOException {
        if (!inflater.needsInput())
            return false;

        releaseInflatedBytes();
        if (inflater.getRemaining() != 0)
            throw new IllegalStateException("?");

        // 如果源中有压缩字节，则将它们分配给缓冲区
        if (source.exhausted()) {
            return true;
        }

        // 将缓冲区字节分配给缓冲区
        SectionBuffer head = source.getBuffer().head;
        bufferBytesHeldByInflater = head.limit - head.pos;
        inflater.setInput(head.data, head.pos, bufferBytesHeldByInflater);
        return false;
    }

    /**
     * 当缓冲区处理完压缩数据后，将其从缓冲区中移除。
     */
    private void releaseInflatedBytes() throws IOException {
        if (bufferBytesHeldByInflater == 0)
            return;
        int toRelease = bufferBytesHeldByInflater - inflater.getRemaining();
        bufferBytesHeldByInflater -= toRelease;
        source.skip(toRelease);
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;
        inflater.end();
        closed = true;
        source.close();
    }

}

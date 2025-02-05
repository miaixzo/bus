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
 * 这种流体的强冲刷可能导致压缩降低 每一个 调用{@link #flush}立即压缩所有当前缓存的数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeflaterSink implements Sink {

    private final BufferSink sink;
    private final Deflater deflater;
    private boolean closed;

    public DeflaterSink(Sink sink, Deflater deflater) {
        this(IoKit.buffer(sink), deflater);
    }

    /**
     * This package-private constructor shares a buffer with its trusted caller. In general we can't share a
     * BufferedSource because the deflater holds input bytes until they are inflated.
     */
    DeflaterSink(BufferSink sink, Deflater deflater) {
        if (sink == null)
            throw new IllegalArgumentException("source == null");
        if (deflater == null)
            throw new IllegalArgumentException("inflater == null");
        this.sink = sink;
        this.deflater = deflater;
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            // Share bytes from the head segment of 'source' with the deflater.
            SectionBuffer head = source.head;
            int toDeflate = (int) Math.min(byteCount, head.limit - head.pos);
            deflater.setInput(head.data, head.pos, toDeflate);

            // Deflate those bytes into sink.
            deflate(false);

            // Mark those bytes as read.
            source.size -= toDeflate;
            head.pos += toDeflate;
            if (head.pos == head.limit) {
                source.head = head.pop();
                LifeCycle.recycle(head);
            }

            byteCount -= toDeflate;
        }
    }

    private void deflate(boolean syncFlush) throws IOException {
        Buffer buffer = sink.buffer();
        while (true) {
            SectionBuffer s = buffer.writableSegment(1);

            // The 4-parameter overload of deflate() doesn't exist in the RI until
            // Java 1.7, and is public (although with @hide) on Android since 2.3.
            // The @hide tag means that this code won't compile against the Android
            // 2.3 SDK, but it will run fine there.
            int deflated = syncFlush
                    ? deflater.deflate(s.data, s.limit, SectionBuffer.SIZE - s.limit, Deflater.SYNC_FLUSH)
                    : deflater.deflate(s.data, s.limit, SectionBuffer.SIZE - s.limit);

            if (deflated > 0) {
                s.limit += deflated;
                buffer.size += deflated;
                sink.emitCompleteSegments();
            } else if (deflater.needsInput()) {
                if (s.pos == s.limit) {
                    // We allocated a tail segment, but didn't end up needing it. Recycle!
                    buffer.head = s.pop();
                    LifeCycle.recycle(s);
                }
                return;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        deflate(true);
        sink.flush();
    }

    void finishDeflate() throws IOException {
        deflater.finish();
        deflate(false);
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;

        // Emit deflated data to the underlying sink. If this fails, we still need
        // to close the deflater and the sink; otherwise we risk leaking resources.
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

    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    @Override
    public String toString() {
        return "DeflaterSink(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}

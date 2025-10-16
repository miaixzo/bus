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
 * A {@code DeflaterSink} compresses data using a {@link Deflater} and writes it to an underlying {@link Sink}. Data is
 * compressed immediately as it is written. Calling {@link #flush()} may reduce compression efficiency.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeflaterSink implements Sink {

    /**
     * The underlying buffered sink to which compressed data is written.
     */
    private final BufferSink sink;

    /**
     * The deflater instance used for compression.
     */
    private final Deflater deflater;

    /**
     * A flag indicating whether this sink has been closed.
     */
    private boolean closed;

    /**
     * Constructs a {@code DeflaterSink} with the specified underlying sink and deflater.
     *
     * @param sink     The underlying sink to write compressed data to.
     * @param deflater The deflater to use for compression.
     */
    public DeflaterSink(Sink sink, Deflater deflater) {
        this(IoKit.buffer(sink), deflater);
    }

    /**
     * Constructs a {@code DeflaterSink} with the specified underlying buffered sink and deflater.
     *
     * @param sink     The underlying buffered sink to write compressed data to.
     * @param deflater The deflater to use for compression.
     * @throws IllegalArgumentException If {@code sink} or {@code deflater} is {@code null}.
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
     * Writes {@code byteCount} bytes from {@code source} to this sink, compressing them before writing to the
     * underlying sink.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to read from {@code source} and write.
     * @throws IOException If an I/O error occurs during the write or compression operation.
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
     * Compresses data from the deflater's input buffer and writes it to the underlying sink.
     *
     * @param syncFlush If {@code true}, a {@link Deflater#SYNC_FLUSH} is performed, which may reduce compression
     *                  efficiency but ensures all pending data is written.
     * @throws IOException If an I/O error occurs during the compression or write operation.
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
     * Flushes any buffered data to the underlying sink, performing a synchronous flush on the deflater to ensure all
     * pending data is compressed and written.
     *
     * @throws IOException If an I/O error occurs during the flush operation.
     */
    @Override
    public void flush() throws IOException {
        deflate(true);
        sink.flush();
    }

    /**
     * Finishes the compression process, writing any remaining compressed data to the sink. This method should be called
     * before closing the sink to ensure all data is written.
     *
     * @throws IOException If an I/O error occurs during the compression or write operation.
     */
    void finishDeflate() throws IOException {
        deflater.finish();
        deflate(false);
    }

    /**
     * Closes this sink, finishes the compression, and releases any system resources associated with the deflater and
     * the underlying sink.
     *
     * @throws IOException If an I/O error occurs during the close operation.
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
     * Returns the timeout for the underlying sink.
     *
     * @return The timeout object associated with the underlying sink.
     */
    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    /**
     * Returns a string representation of this {@code DeflaterSink}. The string representation includes the class name
     * and the string representation of the underlying sink.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return "DeflaterSink(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}

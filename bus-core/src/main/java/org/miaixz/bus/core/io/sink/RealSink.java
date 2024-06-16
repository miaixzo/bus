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
package org.miaixz.bus.core.io.sink;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 原始流信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealSink implements BufferSink {

    public final Buffer buffer = new Buffer();
    public final Sink sink;
    boolean closed;

    public RealSink(Sink sink) {
        if (null == sink) {
            throw new NullPointerException("sink == null");
        }
        this.sink = sink;
    }

    @Override
    public Buffer buffer() {
        return buffer;
    }

    @Override
    public void write(Buffer source, long byteCount)
            throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source, byteCount);
        emitCompleteSegments();
    }

    @Override
    public BufferSink write(ByteString byteString) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(byteString);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeUtf8(String string) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8(string);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeUtf8(String string, int beginIndex, int endIndex)
            throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8(string, beginIndex, endIndex);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8CodePoint(codePoint);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeString(String string, Charset charset) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeString(string, charset);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeString(String string, int beginIndex, int endIndex,
                                  Charset charset) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeString(string, beginIndex, endIndex, charset);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink write(byte[] source) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source, offset, byteCount);
        return emitCompleteSegments();
    }

    @Override
    public int write(ByteBuffer source) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        int result = buffer.write(source);
        emitCompleteSegments();
        return result;
    }

    @Override
    public long writeAll(Source source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(buffer, SectionBuffer.SIZE)) != -1; ) {
            totalBytesRead += readCount;
            emitCompleteSegments();
        }
        return totalBytesRead;
    }

    @Override
    public BufferSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(buffer, byteCount);
            if (read == -1) throw new EOFException();
            byteCount -= read;
            emitCompleteSegments();
        }
        return this;
    }

    @Override
    public BufferSink writeByte(int b) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeByte(b);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeShort(int s) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeShort(s);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeShortLe(int s) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeShortLe(s);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeInt(int i) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeInt(i);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeIntLe(int i) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeIntLe(i);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeLong(v);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeLongLe(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeLongLe(v);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeDecimalLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeDecimalLong(v);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeHexadecimalUnsignedLong(v);
        return emitCompleteSegments();
    }

    @Override
    public BufferSink emitCompleteSegments() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        long byteCount = buffer.completeSegmentByteCount();
        if (byteCount > 0) sink.write(buffer, byteCount);
        return this;
    }

    @Override
    public BufferSink emit() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        long byteCount = buffer.size();
        if (byteCount > 0) sink.write(buffer, byteCount);
        return this;
    }

    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (closed) throw new IOException("closed");
                buffer.writeByte((byte) b);
                emitCompleteSegments();
            }

            @Override
            public void write(byte[] data, int offset, int byteCount) throws IOException {
                if (closed) throw new IOException("closed");
                buffer.write(data, offset, byteCount);
                emitCompleteSegments();
            }

            @Override
            public void flush() throws IOException {
                // For backwards compatibility, a flush() on a closed stream is a no-op.
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

    @Override
    public void flush() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        if (buffer.size > 0) {
            sink.write(buffer, buffer.size);
        }
        sink.flush();
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

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
            if (null == thrown) thrown = e;
        }
        closed = true;

        if (null != thrown) IoKit.sneakyRethrow(thrown);
    }

    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    @Override
    public String toString() {
        return "buffer(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}

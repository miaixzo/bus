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
 * An implementation of {@link BufferSink} that manages byte stream write operations. It supports writing various data
 * formats and adapts to an underlying {@link Sink}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealSink implements BufferSink {

    /**
     * The internal buffer used for accumulating data before writing to the underlying sink.
     */
    public final Buffer buffer = new Buffer();

    /**
     * The underlying sink to which data is eventually written.
     */
    public final Sink sink;

    /**
     * A flag indicating whether this sink has been closed.
     */
    boolean closed;

    /**
     * Constructs a {@code RealSink} with the specified underlying sink.
     *
     * @param sink The underlying sink to which data will be written.
     * @throws NullPointerException If {@code sink} is {@code null}.
     */
    public RealSink(Sink sink) {
        if (null == sink) {
            throw new NullPointerException("sink == null");
        }
        this.sink = sink;
    }

    /**
     * Returns the internal buffer of this sink.
     *
     * @return The internal {@link Buffer} object.
     */
    @Override
    public Buffer buffer() {
        return buffer;
    }

    /**
     * Writes {@code byteCount} bytes from {@code source} to this sink's internal buffer, and then emits complete
     * segments to the underlying sink.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to write.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source, byteCount);
        emitCompleteSegments();
    }

    /**
     * Writes a {@link ByteString} to this sink's internal buffer, and then emits complete segments to the underlying
     * sink.
     *
     * @param byteString The {@link ByteString} to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink write(ByteString byteString) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(byteString);
        return emitCompleteSegments();
    }

    /**
     * Writes a string to this sink's internal buffer using UTF-8 encoding, and then emits complete segments to the
     * underlying sink.
     *
     * @param string The string to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeUtf8(String string) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8(string);
        return emitCompleteSegments();
    }

    /**
     * Writes a portion of a string to this sink's internal buffer using UTF-8 encoding, and then emits complete
     * segments to the underlying sink.
     *
     * @param string     The string to write from.
     * @param beginIndex The starting index of the substring to write.
     * @param endIndex   The ending index (exclusive) of the substring to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8(string, beginIndex, endIndex);
        return emitCompleteSegments();
    }

    /**
     * Writes a Unicode code point to this sink's internal buffer using UTF-8 encoding, and then emits complete segments
     * to the underlying sink.
     *
     * @param codePoint The Unicode code point to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeUtf8CodePoint(codePoint);
        return emitCompleteSegments();
    }

    /**
     * Writes a string to this sink's internal buffer using the specified character set, and then emits complete
     * segments to the underlying sink.
     *
     * @param string  The string to write.
     * @param charset The character set to use for encoding the string.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeString(String string, Charset charset) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeString(string, charset);
        return emitCompleteSegments();
    }

    /**
     * Writes a portion of a string to this sink's internal buffer using the specified character set, and then emits
     * complete segments to the underlying sink.
     *
     * @param string     The string to write from.
     * @param beginIndex The starting index of the substring to write.
     * @param endIndex   The ending index (exclusive) of the substring to write.
     * @param charset    The character set to use for encoding the string.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeString(string, beginIndex, endIndex, charset);
        return emitCompleteSegments();
    }

    /**
     * Writes an entire byte array to this sink's internal buffer, and then emits complete segments to the underlying
     * sink.
     *
     * @param source The byte array to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink write(byte[] source) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source);
        return emitCompleteSegments();
    }

    /**
     * Writes a portion of a byte array to this sink's internal buffer, and then emits complete segments to the
     * underlying sink.
     *
     * @param source    The byte array to write from.
     * @param offset    The starting offset in the byte array.
     * @param byteCount The number of bytes to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.write(source, offset, byteCount);
        return emitCompleteSegments();
    }

    /**
     * Writes data from a {@link ByteBuffer} to this sink's internal buffer, and then emits complete segments to the
     * underlying sink.
     *
     * @param source The source {@link ByteBuffer}.
     * @return The number of bytes written.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
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
     * Reads all data from the given {@link Source} and writes it to this sink.
     *
     * @param source The {@link Source} to read from.
     * @return The total number of bytes read and written.
     * @throws IOException              If an I/O error occurs during the read or write operation.
     * @throws IllegalArgumentException If {@code source} is {@code null}.
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
     * Reads a specified number of bytes from the given {@link Source} and writes them to this sink.
     *
     * @param source    The {@link Source} to read from.
     * @param byteCount The number of bytes to read and write.
     * @return This {@code BufferSink} instance.
     * @throws IOException  If an I/O error occurs during the read or write operation.
     * @throws EOFException If the source does not contain enough data to satisfy the {@code byteCount}.
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
     * Writes a single byte to this sink's internal buffer, and then emits complete segments to the underlying sink.
     *
     * @param b The byte value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeByte(int b) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeByte(b);
        return emitCompleteSegments();
    }

    /**
     * Writes a 2-byte short integer to this sink's internal buffer using big-endian byte order, and then emits complete
     * segments to the underlying sink.
     *
     * @param s The short integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeShort(int s) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeShort(s);
        return emitCompleteSegments();
    }

    /**
     * Writes a 2-byte short integer to this sink's internal buffer using little-endian byte order, and then emits
     * complete segments to the underlying sink.
     *
     * @param s The short integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeShortLe(int s) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeShortLe(s);
        return emitCompleteSegments();
    }

    /**
     * Writes a 4-byte integer to this sink's internal buffer using big-endian byte order, and then emits complete
     * segments to the underlying sink.
     *
     * @param i The integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeInt(int i) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeInt(i);
        return emitCompleteSegments();
    }

    /**
     * Writes a 4-byte integer to this sink's internal buffer using little-endian byte order, and then emits complete
     * segments to the underlying sink.
     *
     * @param i The integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeIntLe(int i) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeIntLe(i);
        return emitCompleteSegments();
    }

    /**
     * Writes an 8-byte long integer to this sink's internal buffer using big-endian byte order, and then emits complete
     * segments to the underlying sink.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeLong(v);
        return emitCompleteSegments();
    }

    /**
     * Writes an 8-byte long integer to this sink's internal buffer using little-endian byte order, and then emits
     * complete segments to the underlying sink.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeLongLe(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeLongLe(v);
        return emitCompleteSegments();
    }

    /**
     * Writes a long integer to this sink's internal buffer in decimal form, and then emits complete segments to the
     * underlying sink.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeDecimalLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeDecimalLong(v);
        return emitCompleteSegments();
    }

    /**
     * Writes an unsigned long integer to this sink's internal buffer in hexadecimal form, and then emits complete
     * segments to the underlying sink.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
     */
    @Override
    public BufferSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        buffer.writeHexadecimalUnsignedLong(v);
        return emitCompleteSegments();
    }

    /**
     * Emits complete buffered segments to the underlying sink. This method writes as much data as possible from the
     * internal buffer to the underlying sink without blocking, ensuring that only full segments are written.
     *
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
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
     * Emits all buffered data to the underlying sink. This method writes all data currently in the internal buffer to
     * the underlying sink.
     *
     * @return This {@code BufferSink} instance.
     * @throws IOException           If an I/O error occurs during the write operation.
     * @throws IllegalStateException If this sink is closed.
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
     * Returns an {@link OutputStream} that writes to this sink.
     *
     * @return An {@link OutputStream} for this sink.
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
     * Flushes any buffered data to the underlying sink. This method forces any buffered output bytes to be written out
     * to the underlying stream.
     *
     * @throws IOException           If an I/O error occurs during the flush operation.
     * @throws IllegalStateException If this sink is closed.
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
     * Checks if this sink is open.
     *
     * @return {@code true} if the sink is open, {@code false} otherwise.
     */
    @Override
    public boolean isOpen() {
        return !closed;
    }

    /**
     * Closes this sink, pushes any remaining buffered data to the underlying sink, and then releases any system
     * resources associated with it. Closing a previously closed sink has no effect.
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
     * Returns the timeout for the underlying sink.
     *
     * @return The {@link Timeout} object associated with the underlying sink.
     */
    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    /**
     * Returns a string representation of this {@code RealSink}. The string representation includes information about
     * the underlying sink.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return "buffer(" + sink + Symbol.PARENTHESE_RIGHT;
    }

}

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
package org.miaixz.bus.core.io.source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link BufferSource} that reads from an underlying {@link Source} and buffers data. This class provides efficient
 * methods for reading various data types and performing operations like searching and skipping bytes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealSource implements BufferSource {

    /**
     * The internal buffer that stores data read from the source.
     */
    public final Buffer buffer = new Buffer();
    /**
     * The underlying source from which data is read.
     */
    public final Source source;
    /**
     * A flag indicating whether this source has been closed.
     */
    boolean closed;

    /**
     * Constructs a new {@code RealSource} with the given underlying {@link Source}.
     *
     * @param source The source to read data from.
     * @throws NullPointerException If the provided source is null.
     */
    public RealSource(Source source) {
        if (null == source) {
            throw new NullPointerException("source == null");
        }
        this.source = source;
    }

    /**
     * Returns the internal buffer of this source.
     *
     * @return The {@link Buffer} instance used by this source.
     */
    @Override
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * Reads at least 1 byte and at most {@code byteCount} bytes from this source and appends them to {@code sink}.
     * Returns the number of bytes read, or -1 if this source has been exhausted.
     *
     * @param sink      The buffer to which bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code sink} is null or {@code byteCount} is negative.
     * @throws IllegalStateException    If this source is closed.
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (closed) {
            throw new IllegalStateException("closed");
        }

        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        long toRead = Math.min(byteCount, buffer.size);
        return buffer.read(sink, toRead);
    }

    /**
     * Returns true if this source is exhausted. This is the case if there are no more bytes in the buffer and the
     * underlying source has also been exhausted.
     *
     * @return True if the source is exhausted, false otherwise.
     * @throws IOException           If an I/O error occurs.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public boolean exhausted() throws IOException {
        if (closed) {
            throw new IllegalStateException("closed");
        }
        return buffer.exhausted() && source.read(buffer, SectionBuffer.SIZE) == -1;
    }

    /**
     * Ensures that at least {@code byteCount} bytes are in the buffer. If not enough bytes are available, this method
     * attempts to read from the underlying source until the required number of bytes are present or the underlying
     * source is exhausted.
     *
     * @param byteCount The minimum number of bytes required in the buffer.
     * @throws IOException If an I/O error occurs or the underlying source is exhausted before {@code byteCount} bytes
     *                     are available.
     */
    @Override
    public void require(long byteCount) throws IOException {
        if (!request(byteCount)) {
            throw new EOFException();
        }
    }

    /**
     * Attempts to fill the buffer with at least {@code byteCount} bytes. This method reads from the underlying source
     * until the buffer contains at least {@code byteCount} bytes or the underlying source is exhausted.
     *
     * @param byteCount The minimum number of bytes to request in the buffer.
     * @return True if at least {@code byteCount} bytes are available, false if the source is exhausted.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code byteCount} is negative.
     * @throws IllegalStateException    If this source is closed.
     */
    @Override
    public boolean request(long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed)
            throw new IllegalStateException("closed");
        while (buffer.size < byteCount) {
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return false;
        }
        return true;
    }

    /**
     * Reads a single byte from this source.
     *
     * @return The byte read.
     * @throws IOException If an I/O error occurs or the source is exhausted.
     */
    @Override
    public byte readByte() throws IOException {
        require(1);
        return buffer.readByte();
    }

    /**
     * Reads all bytes from this source and returns them as a {@link ByteString}.
     *
     * @return A {@link ByteString} containing all bytes from the source.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ByteString readByteString() throws IOException {
        buffer.writeAll(source);
        return buffer.readByteString();
    }

    /**
     * Reads {@code byteCount} bytes from this source and returns them as a {@link ByteString}.
     *
     * @param byteCount The number of bytes to read.
     * @return A {@link ByteString} containing {@code byteCount} bytes from the source.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    @Override
    public ByteString readByteString(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readByteString(byteCount);
    }

    /**
     * Reads a byte sequence from this source that matches one of the provided {@link SegmentBuffer} options. This
     * method will consume the matched byte sequence from the source.
     *
     * @param segmentBuffer The {@link SegmentBuffer} containing the options to match against.
     * @return The index of the matched {@link ByteString} in the {@link SegmentBuffer}, or -1 if no match is found and
     *         the source is exhausted.
     * @throws IOException           If an I/O error occurs.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public int select(SegmentBuffer segmentBuffer) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            int index = buffer.selectPrefix(segmentBuffer, true);
            if (index == -1)
                return -1;
            if (index == -2) {
                if (source.read(buffer, SectionBuffer.SIZE) == -1L)
                    return -1;
            } else {
                int selectedSize = segmentBuffer.byteStrings[index].size();
                buffer.skip(selectedSize);
                return index;
            }
        }
    }

    /**
     * Reads all bytes from this source and returns them as a byte array.
     *
     * @return A byte array containing all bytes from the source.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public byte[] readByteArray() throws IOException {
        buffer.writeAll(source);
        return buffer.readByteArray();
    }

    /**
     * Reads {@code byteCount} bytes from this source and returns them as a byte array.
     *
     * @param byteCount The number of bytes to read.
     * @return A byte array containing {@code byteCount} bytes from the source.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    @Override
    public byte[] readByteArray(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readByteArray(byteCount);
    }

    /**
     * Reads bytes from this source into the given byte array.
     *
     * @param sink The byte array to write bytes into.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(byte[] sink) throws IOException {
        return read(sink, 0, sink.length);
    }

    /**
     * Reads exactly {@code sink.length} bytes from this source into the given byte array.
     *
     * @param sink The byte array to write bytes into.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code sink.length} bytes are
     *                     available.
     */
    @Override
    public void readFully(byte[] sink) throws IOException {
        try {
            require(sink.length);
        } catch (EOFException e) {
            int offset = 0;
            while (buffer.size > 0) {
                int read = buffer.read(sink, offset, (int) buffer.size);
                if (read == -1)
                    throw new AssertionError();
                offset += read;
            }
            throw e;
        }
        buffer.readFully(sink);
    }

    /**
     * Reads bytes from this source into the given byte array, starting at {@code offset} and reading up to
     * {@code byteCount} bytes.
     *
     * @param sink      The byte array to write bytes into.
     * @param offset    The starting offset in the byte array.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        IoKit.checkOffsetAndCount(sink.length, offset, byteCount);

        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        int toRead = (int) Math.min(byteCount, buffer.size);
        return buffer.read(sink, offset, toRead);
    }

    /**
     * Reads bytes from this source into the given {@link ByteBuffer}.
     *
     * @param sink The {@link ByteBuffer} to write bytes into.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(ByteBuffer sink) throws IOException {
        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        return buffer.read(sink);
    }

    /**
     * Reads exactly {@code byteCount} bytes from this source into the given {@link Buffer}.
     *
     * @param sink      The buffer to write bytes into.
     * @param byteCount The number of bytes to read.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    @Override
    public void readFully(Buffer sink, long byteCount) throws IOException {
        try {
            require(byteCount);
        } catch (EOFException e) {
            // The underlying source is exhausted. Copy the bytes we got before rethrowing.
            sink.writeAll(buffer);
            throw e;
        }
        buffer.readFully(sink, byteCount);
    }

    /**
     * Reads all bytes from this source and writes them to the given {@link Sink}.
     *
     * @param sink The {@link Sink} to write all bytes to.
     * @return The total number of bytes written to the sink.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code sink} is null.
     */
    @Override
    public long readAll(Sink sink) throws IOException {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }

        long totalBytesWritten = 0;
        while (source.read(buffer, SectionBuffer.SIZE) != -1) {
            long emitByteCount = buffer.completeSegmentByteCount();
            if (emitByteCount > 0) {
                totalBytesWritten += emitByteCount;
                sink.write(buffer, emitByteCount);
            }
        }
        if (buffer.size() > 0) {
            totalBytesWritten += buffer.size();
            sink.write(buffer, buffer.size());
        }
        return totalBytesWritten;
    }

    /**
     * Reads all bytes from this source and decodes them as a UTF-8 string.
     *
     * @return The decoded UTF-8 string.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public String readUtf8() throws IOException {
        buffer.writeAll(source);
        return buffer.readUtf8();
    }

    /**
     * Reads {@code byteCount} bytes from this source and decodes them as a UTF-8 string.
     *
     * @param byteCount The number of bytes to read.
     * @return The decoded UTF-8 string.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    @Override
    public String readUtf8(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readUtf8(byteCount);
    }

    /**
     * Reads all bytes from this source and decodes them using the specified charset.
     *
     * @param charset The charset to use for decoding.
     * @return The decoded string.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code charset} is null.
     */
    @Override
    public String readString(Charset charset) throws IOException {
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }

        buffer.writeAll(source);
        return buffer.readString(charset);
    }

    /**
     * Reads {@code byteCount} bytes from this source and decodes them using the specified charset.
     *
     * @param byteCount The number of bytes to read.
     * @param charset   The charset to use for decoding.
     * @return The decoded string.
     * @throws IOException              If an I/O error occurs or the source is exhausted before {@code byteCount} bytes
     *                                  are available.
     * @throws IllegalArgumentException If {@code charset} is null.
     */
    @Override
    public String readString(long byteCount, Charset charset) throws IOException {
        require(byteCount);
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        return buffer.readString(byteCount, charset);
    }

    /**
     * Reads a complete UTF-8 line from this source, ending with a newline character (LF). The newline character is
     * consumed but not included in the returned string.
     *
     * @return The decoded UTF-8 line, or null if the source is exhausted before a newline is found.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public String readUtf8Line() throws IOException {
        long newline = indexOf((byte) Symbol.C_LF);

        if (newline == -1) {
            return buffer.size != 0 ? readUtf8(buffer.size) : null;
        }

        return buffer.readUtf8Line(newline);
    }

    /**
     * Reads a complete UTF-8 line from this source, ending with a newline character (LF). This method is strict and
     * will throw an {@link EOFException} if a newline is not found.
     *
     * @return The decoded UTF-8 line.
     * @throws IOException If an I/O error occurs or a newline is not found before the source is exhausted.
     */
    @Override
    public String readUtf8LineStrict() throws IOException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    /**
     * Reads a complete UTF-8 line from this source, ending with a newline character (LF), with a maximum length limit.
     * This method is strict and will throw an {@link EOFException} if a newline is not found within the specified
     * limit.
     *
     * @param limit The maximum number of bytes to scan for a newline character.
     * @return The decoded UTF-8 line.
     * @throws IOException              If an I/O error occurs or a newline is not found within the limit.
     * @throws IllegalArgumentException If {@code limit} is negative.
     */
    @Override
    public String readUtf8LineStrict(long limit) throws IOException {
        if (limit < 0)
            throw new IllegalArgumentException("limit < 0: " + limit);
        long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
        long newline = indexOf((byte) Symbol.C_LF, 0, scanLength);
        if (newline != -1)
            return buffer.readUtf8Line(newline);
        if (scanLength < Long.MAX_VALUE && request(scanLength) && buffer.getByte(scanLength - 1) == Symbol.C_CR
                && request(scanLength + 1) && buffer.getByte(scanLength) == Symbol.C_LF) {
            return buffer.readUtf8Line(scanLength); // The line was 'limit' UTF-8 bytes followed by \r\n.
        }
        Buffer data = new Buffer();
        buffer.copyTo(data, 0, Math.min(Normal._32, buffer.size()));
        throw new EOFException("\\n not found: limit=" + Math.min(buffer.size(), limit) + " content="
                + data.readByteString().hex() + '…');
    }

    /**
     * Reads a UTF-8 code point from this source.
     *
     * @return The decoded UTF-8 code point.
     * @throws IOException If an I/O error occurs or the source is exhausted before a complete code point can be read.
     */
    @Override
    public int readUtf8CodePoint() throws IOException {
        require(1);

        byte b0 = buffer.getByte(0);
        if ((b0 & 0xe0) == 0xc0) {
            require(2);
        } else if ((b0 & 0xf0) == 0xe0) {
            require(3);
        } else if ((b0 & 0xf8) == 0xf0) {
            require(4);
        }

        return buffer.readUtf8CodePoint();
    }

    /**
     * Reads a 2-byte signed short from this source.
     *
     * @return The short value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 2 bytes are available.
     */
    @Override
    public short readShort() throws IOException {
        require(2);
        return buffer.readShort();
    }

    /**
     * Reads a 2-byte signed short from this source in little-endian byte order.
     *
     * @return The short value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 2 bytes are available.
     */
    @Override
    public short readShortLe() throws IOException {
        require(2);
        return buffer.readShortLe();
    }

    /**
     * Reads a 4-byte signed integer from this source.
     *
     * @return The integer value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 4 bytes are available.
     */
    @Override
    public int readInt() throws IOException {
        require(4);
        return buffer.readInt();
    }

    /**
     * Reads a 4-byte signed integer from this source in little-endian byte order.
     *
     * @return The integer value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 4 bytes are available.
     */
    @Override
    public int readIntLe() throws IOException {
        require(4);
        return buffer.readIntLe();
    }

    /**
     * Reads an 8-byte signed long from this source.
     *
     * @return The long value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 8 bytes are available.
     */
    @Override
    public long readLong() throws IOException {
        require(8);
        return buffer.readLong();
    }

    /**
     * Reads an 8-byte signed long from this source in little-endian byte order.
     *
     * @return The long value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 8 bytes are available.
     */
    @Override
    public long readLongLe() throws IOException {
        require(8);
        return buffer.readLongLe();
    }

    /**
     * Reads a decimal long from this source. This method reads digits until a non-digit character is encountered.
     *
     * @return The decimal long value read.
     * @throws IOException           If an I/O error occurs or the source is exhausted.
     * @throws NumberFormatException If the first character is not a digit or a minus sign, or if the number is
     *                               malformed.
     */
    @Override
    public long readDecimalLong() throws IOException {
        require(1);

        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < Symbol.C_ZERO || b > Symbol.C_NINE) && (pos != 0 || b != Symbol.C_MINUS)) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(
                            String.format("Expected leading [0-9] or '-' character but was %#x", b));
                }
                break;
            }
        }

        return buffer.readDecimalLong();
    }

    /**
     * Reads a hexadecimal unsigned long from this source. This method reads hexadecimal digits until a non-hexadecimal
     * character is encountered.
     *
     * @return The hexadecimal unsigned long value read.
     * @throws IOException           If an I/O error occurs or the source is exhausted.
     * @throws NumberFormatException If the first character is not a hexadecimal digit, or if the number is malformed.
     */
    @Override
    public long readHexadecimalUnsignedLong() throws IOException {
        require(1);

        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < Symbol.C_ZERO || b > Symbol.C_NINE) && (b < 'a' || b > 'f') && (b < 'A' || b > 'F')) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(
                            String.format("Expected leading [0-9a-fA-F] character but was %#x", b));
                }
                break;
            }
        }

        return buffer.readHexadecimalUnsignedLong();
    }

    /**
     * Skips {@code byteCount} bytes from this source.
     *
     * @param byteCount The number of bytes to skip.
     * @throws IOException           If an I/O error occurs or the source is exhausted before {@code byteCount} bytes
     *                               can be skipped.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public void skip(long byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        while (byteCount > 0) {
            if (buffer.size == 0 && source.read(buffer, SectionBuffer.SIZE) == -1) {
                throw new EOFException();
            }
            long toSkip = Math.min(byteCount, buffer.size());
            buffer.skip(toSkip);
            byteCount -= toSkip;
        }
    }

    /**
     * Finds the first occurrence of the given byte in this source.
     *
     * @param b The byte to search for.
     * @return The index of the first occurrence of the byte, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long indexOf(byte b) throws IOException {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    /**
     * Finds the first occurrence of the given byte in this source, starting from {@code fromIndex}.
     *
     * @param b         The byte to search for.
     * @param fromIndex The index to start the search from.
     * @return The index of the first occurrence of the byte, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long indexOf(byte b, long fromIndex) throws IOException {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    /**
     * Finds the first occurrence of the given byte in this source within the specified range.
     *
     * @param b         The byte to search for.
     * @param fromIndex The index to start the search from (inclusive).
     * @param toIndex   The index to end the search at (exclusive).
     * @return The index of the first occurrence of the byte, or -1 if not found within the range.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalStateException    If this source is closed.
     * @throws IllegalArgumentException If {@code fromIndex} is negative or {@code toIndex} is less than
     *                                  {@code fromIndex}.
     */
    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(String.format("fromIndex=%s toIndex=%s", fromIndex, toIndex));
        }

        while (fromIndex < toIndex) {
            long result = buffer.indexOf(b, fromIndex, toIndex);
            if (result != -1L)
                return result;

            // The byte wasn't in the buffer. Give up if we've already reached our target size or if the
            // underlying stream is exhausted.
            long lastBufferSize = buffer.size;
            if (lastBufferSize >= toIndex || source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Continue the search from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize);
        }
        return -1L;
    }

    /**
     * Finds the first occurrence of the given {@link ByteString} in this source.
     *
     * @param bytes The {@link ByteString} to search for.
     * @return The index of the first occurrence of the byte string, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    /**
     * Finds the first occurrence of the given {@link ByteString} in this source, starting from {@code fromIndex}.
     *
     * @param bytes     The {@link ByteString} to search for.
     * @param fromIndex The index to start the search from.
     * @return The index of the first occurrence of the byte string, or -1 if not found.
     * @throws IOException           If an I/O error occurs.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            long result = buffer.indexOf(bytes, fromIndex);
            if (result != -1)
                return result;

            long lastBufferSize = buffer.size;
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Keep searching, picking up from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize - bytes.size() + 1);
        }
    }

    /**
     * Finds the first occurrence of any byte from {@code targetBytes} in this source.
     *
     * @param targetBytes The {@link ByteString} containing the bytes to search for.
     * @return The index of the first occurrence of any byte from {@code targetBytes}, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long indexOfElement(ByteString targetBytes) throws IOException {
        return indexOfElement(targetBytes, 0);
    }

    /**
     * Finds the first occurrence of any byte from {@code targetBytes} in this source, starting from {@code fromIndex}.
     *
     * @param targetBytes The {@link ByteString} containing the bytes to search for.
     * @param fromIndex   The index to start the search from.
     * @return The index of the first occurrence of any byte from {@code targetBytes}, or -1 if not found.
     * @throws IOException           If an I/O error occurs.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            long result = buffer.indexOfElement(targetBytes, fromIndex);
            if (result != -1)
                return result;

            long lastBufferSize = buffer.size;
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Keep searching, picking up from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize);
        }
    }

    /**
     * Checks if the bytes in this source, starting at {@code offset}, are equal to the given {@link ByteString}.
     *
     * @param offset The offset in this source to start the comparison.
     * @param bytes  The {@link ByteString} to compare against.
     * @return True if the bytes are equal, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    /**
     * Checks if a range of bytes in this source, starting at {@code offset}, is equal to a sub-range of the given
     * {@link ByteString}.
     *
     * @param offset      The offset in this source to start the comparison.
     * @param bytes       The {@link ByteString} to compare against.
     * @param bytesOffset The offset in {@code bytes} to start the comparison.
     * @param byteCount   The number of bytes to compare.
     * @return True if the bytes are equal, false otherwise.
     * @throws IOException           If an I/O error occurs.
     * @throws IllegalStateException If this source is closed.
     */
    @Override
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        if (offset < 0 || bytesOffset < 0 || byteCount < 0 || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; i++) {
            long bufferOffset = offset + i;
            if (!request(bufferOffset + 1))
                return false;
            if (buffer.getByte(bufferOffset) != bytes.getByte(bytesOffset + i))
                return false;
        }
        return true;
    }

    /**
     * Returns a new {@link BufferSource} that can peek into this source without consuming its data.
     *
     * @return A new {@link BufferSource} for peeking.
     */
    @Override
    public BufferSource peek() {
        return IoKit.buffer(new PeekSource(this));
    }

    /**
     * Returns an {@link InputStream} that reads from this source.
     *
     * @return An {@link InputStream} instance.
     */
    @Override
    public InputStream inputStream() {
        return new InputStream() {

            /**
             * Reads the next byte of data from the input stream.
             *
             * @return The next byte of data, or -1 if the end of the stream is reached.
             * @throws IOException If an I/O error occurs.
             */
            @Override
            public int read() throws IOException {
                if (closed)
                    throw new IOException("closed");
                if (buffer.size == 0) {
                    long count = source.read(buffer, SectionBuffer.SIZE);
                    if (count == -1)
                        return -1;
                }
                return buffer.readByte() & 0xff;
            }

            /**
             * Reads up to {@code byteCount} bytes of data from the input stream into an array of bytes.
             *
             * @param data      The buffer into which the data is read.
             * @param offset    The start offset in the destination array {@code data} at which the data is written.
             * @param byteCount The maximum number of bytes to read.
             * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of
             *         the stream has been reached.
             * @throws IOException If an I/O error occurs.
             */
            @Override
            public int read(byte[] data, int offset, int byteCount) throws IOException {
                if (closed)
                    throw new IOException("closed");
                IoKit.checkOffsetAndCount(data.length, offset, byteCount);

                if (buffer.size == 0) {
                    long count = source.read(buffer, SectionBuffer.SIZE);
                    if (count == -1)
                        return -1;
                }

                return buffer.read(data, offset, byteCount);
            }

            /**
             * Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by
             * the next caller of a method for this input stream.
             *
             * @return The number of bytes that can be read from this input stream without blocking.
             * @throws IOException If an I/O error occurs.
             */
            @Override
            public int available() throws IOException {
                if (closed)
                    throw new IOException("closed");
                return (int) Math.min(buffer.size, Integer.MAX_VALUE);
            }

            /**
             * Closes this input stream and releases any system resources associated with the stream.
             *
             * @throws IOException If an I/O error occurs.
             */
            @Override
            public void close() throws IOException {
                RealSource.this.close();
            }

            /**
             * Returns a string representation of this {@code InputStream}.
             *
             * @return A string representation.
             */
            @Override
            public String toString() {
                return RealSource.this + ".inputStream()";
            }
        };
    }

    /**
     * Checks if this source is open.
     *
     * @return True if the source is open, false otherwise.
     */
    @Override
    public boolean isOpen() {
        return !closed;
    }

    /**
     * Closes this source and the underlying source, releasing any resources held by them. This method can be called
     * multiple times safely.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        if (closed)
            return;
        closed = true;
        source.close();
        buffer.clear();
    }

    /**
     * Returns the timeout for this source, delegated to the underlying source.
     *
     * @return The timeout instance.
     */
    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    /**
     * Returns a string representation of this {@code RealSource}.
     *
     * @return A string representation.
     */
    @Override
    public String toString() {
        return "buffer(" + source + Symbol.PARENTHESE_RIGHT;
    }

}

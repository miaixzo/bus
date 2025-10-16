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
 * A {@code BufferSink} provides efficient write operations by maintaining an internal buffer to reduce the overhead of
 * frequent I/O calls. It extends both {@link Sink} and {@link WritableByteChannel} to offer a comprehensive set of
 * methods for writing various data types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BufferSink extends Sink, WritableByteChannel {

    /**
     * Returns the internal buffer of this sink.
     *
     * @return The internal {@link Buffer} object.
     */
    Buffer buffer();

    /**
     * Writes a {@link ByteString} to this sink.
     *
     * @param byteString The {@link ByteString} to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink write(ByteString byteString) throws IOException;

    /**
     * Writes an entire byte array to this sink.
     *
     * @param source The byte array to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink write(byte[] source) throws IOException;

    /**
     * Writes a portion of a byte array to this sink.
     *
     * @param source    The byte array to write from.
     * @param offset    The starting offset in the byte array.
     * @param byteCount The number of bytes to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink write(byte[] source, int offset, int byteCount) throws IOException;

    /**
     * Reads all bytes from the given {@link Source} and writes them to this sink.
     *
     * @param source The {@link Source} to read from.
     * @return The number of bytes read and written. Returns 0 if the source is exhausted.
     * @throws IOException If an I/O error occurs during the read or write operation.
     */
    long writeAll(Source source) throws IOException;

    /**
     * Reads a specified number of bytes from the given {@link Source} and writes them to this sink.
     *
     * @param source    The {@link Source} to read from.
     * @param byteCount The number of bytes to read and write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the read or write operation.
     */
    BufferSink write(Source source, long byteCount) throws IOException;

    /**
     * Writes a string to this sink using UTF-8 encoding.
     *
     * @param string The string to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeUtf8(String string) throws IOException;

    /**
     * Writes a portion of a string to this sink using UTF-8 encoding.
     *
     * @param string     The string to write from.
     * @param beginIndex The starting index of the substring to write.
     * @param endIndex   The ending index (exclusive) of the substring to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException;

    /**
     * Writes a Unicode code point to this sink using UTF-8 encoding.
     *
     * @param codePoint The Unicode code point to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeUtf8CodePoint(int codePoint) throws IOException;

    /**
     * Writes a string to this sink using the specified character set.
     *
     * @param string  The string to write.
     * @param charset The character set to use for encoding the string.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeString(String string, Charset charset) throws IOException;

    /**
     * Writes a portion of a string to this sink using the specified character set.
     *
     * @param string     The string to write from.
     * @param beginIndex The starting index of the substring to write.
     * @param endIndex   The ending index (exclusive) of the substring to write.
     * @param charset    The character set to use for encoding the string.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException;

    /**
     * Writes a single byte to this sink.
     *
     * @param b The byte value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeByte(int b) throws IOException;

    /**
     * Writes a 2-byte short integer to this sink using big-endian byte order.
     *
     * @param s The short integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeShort(int s) throws IOException;

    /**
     * Writes a 2-byte short integer to this sink using little-endian byte order.
     *
     * @param s The short integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeShortLe(int s) throws IOException;

    /**
     * Writes a 4-byte integer to this sink using big-endian byte order.
     *
     * @param i The integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeInt(int i) throws IOException;

    /**
     * Writes a 4-byte integer to this sink using little-endian byte order.
     *
     * @param i The integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeIntLe(int i) throws IOException;

    /**
     * Writes an 8-byte long integer to this sink using big-endian byte order.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeLong(long v) throws IOException;

    /**
     * Writes an 8-byte long integer to this sink using little-endian byte order.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeLongLe(long v) throws IOException;

    /**
     * Writes a long integer to this sink in decimal form.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeDecimalLong(long v) throws IOException;

    /**
     * Writes an unsigned long integer to this sink in hexadecimal form.
     *
     * @param v The long integer value to write.
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink writeHexadecimalUnsignedLong(long v) throws IOException;

    /**
     * Flushes any buffered data to the underlying sink, recursively flushing to push data towards the ultimate
     * destination.
     *
     * @throws IOException If an I/O error occurs during the write or flush operation.
     */
    @Override
    void flush() throws IOException;

    /**
     * Emits buffered data to the underlying sink. This is a weaker flush operation that ensures data is moved towards
     * the destination but does not guarantee that it is fully written to the underlying output.
     *
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink emit() throws IOException;

    /**
     * Emits complete buffered segments to the underlying sink, limiting buffer memory usage.
     *
     * @return This {@code BufferSink} instance.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    BufferSink emitCompleteSegments() throws IOException;

    /**
     * Returns an {@link OutputStream} that writes to this sink.
     *
     * @return An {@link OutputStream} for this sink.
     */
    OutputStream outputStream();

}

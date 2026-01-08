/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.xyz;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Utility class for {@link ByteBuffer} and {@link CharBuffer}. This tool collects relevant parts from the t-io project
 * and other projects. For more information on ByteBuffer, see: https://www.cnblogs.com/ruber/p/6857159.html
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BufferKit {

    /**
     * Constructs a new BufferKit. Utility class constructor for static access.
     */
    private BufferKit() {
    }

    /**
     * Converts a {@link ByteBuffer} to a byte array.
     *
     * @param bytebuffer The {@link ByteBuffer} to convert.
     * @return The byte array.
     */
    public static byte[] toBytes(final ByteBuffer bytebuffer) {
        if (bytebuffer.hasArray()) {
            return Arrays.copyOfRange(bytebuffer.array(), bytebuffer.position(), bytebuffer.limit());
        } else {
            final int oldPosition = bytebuffer.position();
            bytebuffer.position(0);
            final int size = bytebuffer.limit();
            final byte[] buffers = new byte[size];
            bytebuffer.get(buffers);
            bytebuffer.position(oldPosition);
            return buffers;
        }
    }

    /**
     * Copies a portion of a source ByteBuffer to a new ByteBuffer.
     *
     * @param src   The source ByteBuffer.
     * @param start The starting position (inclusive).
     * @param end   The ending position (exclusive).
     * @return A new ByteBuffer containing the copied portion.
     */
    public static ByteBuffer copy(final ByteBuffer src, final int start, final int end) {
        return copy(src, ByteBuffer.allocate(end - start));
    }

    /**
     * Copies data from a source ByteBuffer to a destination ByteBuffer. The amount copied is the minimum of the
     * source's limit and the destination's remaining capacity.
     *
     * @param src  The source ByteBuffer.
     * @param dest The destination ByteBuffer.
     * @return The destination ByteBuffer.
     */
    public static ByteBuffer copy(final ByteBuffer src, final ByteBuffer dest) {
        return copy(src, dest, Math.min(src.limit(), dest.remaining()));
    }

    /**
     * Copies a specified length of data from a source ByteBuffer to a destination ByteBuffer.
     *
     * @param src    The source ByteBuffer.
     * @param dest   The destination ByteBuffer.
     * @param length The number of bytes to copy.
     * @return The destination ByteBuffer.
     */
    public static ByteBuffer copy(final ByteBuffer src, final ByteBuffer dest, final int length) {
        return copy(src, src.position(), dest, dest.position(), length);
    }

    /**
     * Copies a specified length of data from a source ByteBuffer to a destination ByteBuffer, starting from specified
     * positions in both buffers.
     *
     * @param src       The source ByteBuffer.
     * @param srcStart  The starting position in the source ByteBuffer.
     * @param dest      The destination ByteBuffer.
     * @param destStart The starting position in the destination ByteBuffer.
     * @param length    The number of bytes to copy.
     * @return The destination ByteBuffer.
     */
    public static ByteBuffer copy(
            final ByteBuffer src,
            final int srcStart,
            final ByteBuffer dest,
            final int destStart,
            final int length) {
        System.arraycopy(src.array(), srcStart, dest.array(), destStart, length);
        return dest;
    }

    /**
     * Reads the remaining bytes from the ByteBuffer and converts them to a UTF-8 encoded string.
     *
     * @param buffer The ByteBuffer to read from.
     * @return The string representation of the remaining bytes.
     */
    public static String readString(final ByteBuffer buffer) {
        return readString(buffer, Charset.UTF_8);
    }

    /**
     * Reads the remaining bytes from the ByteBuffer and converts them to a string using the specified charset.
     *
     * @param buffer  The ByteBuffer to read from.
     * @param charset The charset to use for decoding the bytes.
     * @return The string representation of the remaining bytes.
     */
    public static String readString(final ByteBuffer buffer, final java.nio.charset.Charset charset) {
        return StringKit.toString(readBytes(buffer), charset);
    }

    /**
     * Reads the remaining bytes from the ByteBuffer.
     *
     * @param buffer The ByteBuffer to read from.
     * @return A byte array containing the remaining bytes.
     */
    public static byte[] readBytes(final ByteBuffer buffer) {
        final int remaining = buffer.remaining();
        final byte[] ab = new byte[remaining];
        buffer.get(ab);
        return ab;
    }

    /**
     * Reads a specified maximum length of bytes from the ByteBuffer. If the remaining bytes are less than
     * {@code maxLength}, all remaining bytes are read. The buffer must be in read mode.
     *
     * @param buffer    The ByteBuffer to read from.
     * @param maxLength The maximum number of bytes to read.
     * @return A byte array containing the read bytes.
     */
    public static byte[] readBytes(final ByteBuffer buffer, int maxLength) {
        final int remaining = buffer.remaining();
        if (maxLength > remaining) {
            maxLength = remaining;
        }
        final byte[] ab = new byte[maxLength];
        buffer.get(ab);
        return ab;
    }

    /**
     * Reads data from the specified range within the ByteBuffer.
     *
     * @param buffer The {@link ByteBuffer} to read from.
     * @param start  The starting position (inclusive).
     * @param end    The ending position (exclusive).
     * @return A byte array containing the data from the specified range.
     */
    public static byte[] readBytes(final ByteBuffer buffer, final int start, final int end) {
        final byte[] bs = new byte[end - start];
        System.arraycopy(buffer.array(), start, bs, 0, bs.length);
        return bs;
    }

    /**
     * Finds the end position of a line in the ByteBuffer. The ByteBuffer's position is advanced to the end of the line.
     *
     * @param buffer The {@link ByteBuffer} to search.
     * @return The end position of the line, or -1 if not found or maximum length reached.
     */
    public static int lineEnd(final ByteBuffer buffer) {
        return lineEnd(buffer, buffer.remaining());
    }

    /**
     * Finds the end position of a line in the ByteBuffer, with a maximum search length. The ByteBuffer's position is
     * advanced to the end of the line. Supported line endings are:
     * 
     * <pre>
     * 1. \r\n
     * 2. \n
     * </pre>
     *
     * @param buffer    The {@link ByteBuffer} to search.
     * @param maxLength The maximum length to search for a line ending.
     * @return The end position of the line, or -1 if not found or maximum length reached.
     * @throws IndexOutOfBoundsException if the search exceeds maxLength without finding a line end.
     */
    public static int lineEnd(final ByteBuffer buffer, final int maxLength) {
        final int primitivePosition = buffer.position();
        boolean canEnd = false;
        int charIndex = primitivePosition;
        byte b;
        while (buffer.hasRemaining()) {
            b = buffer.get();
            charIndex++;
            if (b == Symbol.C_CR) {
                canEnd = true;
            } else if (b == Symbol.C_LF) {
                return canEnd ? charIndex - 2 : charIndex - 1;
            } else {
                // Only \r cannot confirm a line break
                canEnd = false;
            }

            if (charIndex - primitivePosition > maxLength) {
                // Reached the end of the search, not found, restore position
                buffer.position(primitivePosition);
                throw new IndexOutOfBoundsException(StringKit.format("Position is out of maxLength: {}", maxLength));
            }
        }

        // Reached the end of the buffer, not found, restore position
        buffer.position(primitivePosition);
        // Reached the end of the read position
        return -1;
    }

    /**
     * Reads a single line from the ByteBuffer. If the last part of the buffer is not a complete line, {@code null} is
     * returned. Supported line endings are:
     * 
     * <pre>
     * 1. \r\n
     * 2. \n
     * </pre>
     *
     * @param buffer  The ByteBuffer to read from.
     * @param charset The charset to use for decoding the line.
     * @return The read line, or {@code null} if no complete line is found.
     */
    public static String readLine(final ByteBuffer buffer, final java.nio.charset.Charset charset) {
        final int startPosition = buffer.position();
        final int endPosition = lineEnd(buffer);

        if (endPosition > startPosition) {
            final byte[] bs = readBytes(buffer, startPosition, endPosition);
            return StringKit.toString(bs, charset);
        } else if (endPosition == startPosition) {
            return Normal.EMPTY;
        }

        return null;
    }

    /**
     * Creates a new {@link ByteBuffer} from a byte array.
     *
     * @param data The byte array to wrap.
     * @return A new {@link ByteBuffer}.
     */
    public static ByteBuffer of(final byte[] data) {
        return ByteBuffer.wrap(data);
    }

    /**
     * Creates a new {@link ByteBuffer} from a character sequence using the specified charset.
     *
     * @param data    The character sequence.
     * @param charset The charset to use for encoding the characters.
     * @return A new {@link ByteBuffer}.
     */
    public static ByteBuffer of(final CharSequence data, final java.nio.charset.Charset charset) {
        return of(ByteKit.toBytes(data, charset));
    }

    /**
     * Creates a new {@link CharBuffer} with the specified capacity.
     *
     * @param capacity The capacity of the CharBuffer.
     * @return A new {@link CharBuffer}.
     */
    public static CharBuffer ofCharBuffer(final int capacity) {
        return CharBuffer.allocate(capacity);
    }

}

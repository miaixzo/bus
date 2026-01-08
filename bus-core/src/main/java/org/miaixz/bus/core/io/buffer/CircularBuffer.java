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
package org.miaixz.bus.core.io.buffer;

import java.util.Objects;

/**
 * A circular buffer implementation for bytes. This buffer allows efficient storage and retrieval of bytes in a
 * fixed-size array, overwriting the oldest data when the buffer is full.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CircularBuffer {

    /**
     * The underlying byte array that stores the buffer's data.
     */
    private final byte[] buffer;
    /**
     * The starting offset (read position) within the buffer.
     */
    private int startOffset;
    /**
     * The ending offset (write position) within the buffer.
     */
    private int endOffset;
    /**
     * The current number of bytes stored in the buffer.
     */
    private int currentNumberOfBytes;

    /**
     * Constructs a {@code CircularBuffer} with a default buffer size of 8192 bytes (2 &lt;&lt; 12).
     */
    public CircularBuffer() {
        this(2 << 12);
    }

    /**
     * Constructs a {@code CircularBuffer} with the specified buffer size.
     *
     * @param pSize The desired size of the circular buffer in bytes.
     */
    public CircularBuffer(final int pSize) {
        buffer = new byte[pSize];
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

    /**
     * Reads and removes the next byte from the buffer.
     *
     * @return The byte read from the buffer.
     * @throws IllegalStateException If the buffer is empty. Use {@link #hasBytes()} or
     *                               {@link #getCurrentNumberOfBytes()} to check for available bytes.
     */
    public byte read() {
        if (currentNumberOfBytes <= 0) {
            throw new IllegalStateException("No bytes available.");
        }
        final byte b = buffer[startOffset];
        --currentNumberOfBytes;
        if (++startOffset == buffer.length) {
            startOffset = 0;
        }
        return b;
    }

    /**
     * Reads a specified number of bytes from the buffer and stores them into the given byte array starting at the
     * specified target offset. The bytes are removed from the buffer.
     *
     * @param targetBuffer The byte array to write the read bytes into.
     * @param targetOffset The starting offset in the {@code targetBuffer} where bytes will be written.
     * @param length       The number of bytes to read from the circular buffer.
     * @throws NullPointerException     If the provided {@code targetBuffer} is {@code null}.
     * @throws IllegalArgumentException If {@code targetOffset} or {@code length} is negative, or if
     *                                  {@code targetBuffer} is too small to hold the specified length starting from
     *                                  {@code targetOffset}.
     * @throws IllegalStateException    If the buffer contains fewer bytes than the requested {@code length}. Use
     *                                  {@link #getCurrentNumberOfBytes()} to check available bytes.
     */
    public void read(final byte[] targetBuffer, final int targetOffset, final int length) {
        Objects.requireNonNull(targetBuffer);
        if (targetOffset < 0 || targetOffset >= targetBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + targetOffset);
        }
        if (length < 0 || length > buffer.length) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
        if (targetOffset + length > targetBuffer.length) {
            throw new IllegalArgumentException("The supplied byte array contains only " + targetBuffer.length
                    + " bytes, but offset, and length would require " + (targetOffset + length - 1));
        }
        if (currentNumberOfBytes < length) {
            throw new IllegalStateException(
                    "Currently, there are only " + currentNumberOfBytes + "in the buffer, not " + length);
        }
        int offset = targetOffset;
        for (int i = 0; i < length; i++) {
            targetBuffer[offset++] = buffer[startOffset];
            --currentNumberOfBytes;
            if (++startOffset == buffer.length) {
                startOffset = 0;
            }
        }
    }

    /**
     * Adds a single byte to the buffer.
     *
     * @param value The byte to add.
     * @throws IllegalStateException If the buffer is full. Use {@link #hasSpace()} or {@link #getSpace()} to check for
     *                               available space.
     */
    public void add(final byte value) {
        if (currentNumberOfBytes >= buffer.length) {
            throw new IllegalStateException("No space available");
        }
        buffer[endOffset] = value;
        ++currentNumberOfBytes;
        if (++endOffset == buffer.length) {
            endOffset = 0;
        }
    }

    /**
     * Checks if the next bytes in the buffer exactly match the bytes provided in {@code sourceBuffer} from
     * {@code offset} for {@code length}. No bytes are removed from the buffer during this operation. If the result is
     * true, subsequent invocations of {@link #read()} are guaranteed to return exactly those bytes.
     *
     * @param sourceBuffer The byte array to compare against.
     * @param offset       The starting offset in {@code sourceBuffer}.
     * @param length       The number of bytes to compare.
     * @return {@code true} if the next bytes in the buffer match the specified sequence, {@code false} otherwise.
     * @throws IllegalArgumentException If {@code offset} or {@code length} is negative, or if {@code length} exceeds
     *                                  the buffer's capacity.
     * @throws NullPointerException     If the byte array {@code sourceBuffer} is {@code null}.
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (offset < 0 || offset >= sourceBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (length < 0 || length > buffer.length) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
        if (length > currentNumberOfBytes) { // Changed from '<' to '>' to correctly check if enough bytes are available
            return false;
        }
        int localOffset = startOffset;
        for (int i = 0; i < length; i++) {
            if (buffer[localOffset] != sourceBuffer[i + offset]) {
                return false;
            }
            if (++localOffset == buffer.length) {
                localOffset = 0;
            }
        }
        return true;
    }

    /**
     * Adds the given bytes from {@code targetBuffer} to this circular buffer. This is equivalent to invoking
     * {@link #add(byte)} for each byte from {@code targetBuffer} starting at {@code offset} for {@code length}.
     *
     * @param targetBuffer The byte array containing the bytes to add.
     * @param offset       The starting offset in {@code targetBuffer}.
     * @param length       The number of bytes to add.
     * @throws IllegalStateException    If the buffer does not have sufficient space. Use {@link #getSpace()} to prevent
     *                                  this exception.
     * @throws IllegalArgumentException If {@code offset} or {@code length} is negative, or if {@code offset} is out of
     *                                  bounds for {@code targetBuffer}.
     * @throws NullPointerException     If the byte array {@code targetBuffer} is {@code null}.
     */
    public void add(final byte[] targetBuffer, final int offset, final int length) {
        Objects.requireNonNull(targetBuffer, "Buffer");
        if (offset < 0 || offset >= targetBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (length < 0) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
        if (currentNumberOfBytes + length > buffer.length) {
            throw new IllegalStateException("No space available");
        }
        for (int i = 0; i < length; i++) {
            buffer[endOffset] = targetBuffer[offset + i];
            if (++endOffset == buffer.length) {
                endOffset = 0;
            }
        }
        currentNumberOfBytes += length;
    }

    /**
     * Returns whether there is currently room for at least one byte in the buffer. This is equivalent to calling
     * {@link #hasSpace(int) hasSpace(1)}.
     *
     * @return {@code true} if there is space for a byte, {@code false} otherwise.
     * @see #hasSpace(int)
     * @see #getSpace()
     */
    public boolean hasSpace() {
        return currentNumberOfBytes < buffer.length;
    }

    /**
     * Returns whether there is currently room for the given number of bytes in the buffer.
     *
     * @param count The number of bytes to check for space.
     * @return {@code true} if there is space for the given number of bytes, {@code false} otherwise.
     * @see #hasSpace()
     * @see #getSpace()
     */
    public boolean hasSpace(final int count) {
        return currentNumberOfBytes + count <= buffer.length;
    }

    /**
     * Returns whether the buffer is currently holding at least one byte.
     *
     * @return {@code true} if the buffer is not empty, {@code false} otherwise.
     */
    public boolean hasBytes() {
        return currentNumberOfBytes > 0;
    }

    /**
     * Returns the number of bytes that can currently be added to the buffer.
     *
     * @return The number of bytes of free space available in the buffer.
     */
    public int getSpace() {
        return buffer.length - currentNumberOfBytes;
    }

    /**
     * Returns the number of bytes that are currently present in the buffer.
     *
     * @return The number of bytes currently stored in the buffer.
     */
    public int getCurrentNumberOfBytes() {
        return currentNumberOfBytes;
    }

    /**
     * Removes all bytes from the buffer, effectively resetting it to an empty state.
     */
    public void clear() {
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

}

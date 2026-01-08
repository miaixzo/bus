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
package org.miaixz.bus.core.io;

import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;

/**
 * A circular byte buffer implementation. This is a fixed-size buffer that overwrites its oldest data when it becomes
 * full. It is not thread-safe.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CircularByteBuffer {

    /**
     * The underlying byte array used as the buffer.
     */
    private final byte[] buffer;
    /**
     * The index of the first byte in the buffer (the read position).
     */
    private int startOffset;
    /**
     * The index where the next byte will be written (the write position).
     */
    private int endOffset;
    /**
     * The current number of bytes stored in the buffer.
     */
    private int currentNumberOfBytes;

    /**
     * Constructs a new {@code CircularByteBuffer} with a default buffer size ({@link Normal#_8192}).
     */
    public CircularByteBuffer() {
        this(Normal._8192);
    }

    /**
     * Constructs a new {@code CircularByteBuffer} with the specified buffer size.
     *
     * @param pSize The size of the buffer.
     */
    public CircularByteBuffer(final int pSize) {
        buffer = new byte[pSize];
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

    /**
     * Reads and removes the next byte from the buffer.
     *
     * @return The byte read.
     * @throws IllegalStateException if the buffer is empty. Use {@link #hasBytes()} or
     *                               {@link #getCurrentNumberOfBytes()} to check before reading.
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
     * Reads a specified number of bytes from the buffer and stores them in the given byte array at the given offset.
     *
     * @param targetBuffer The target byte array to write to.
     * @param targetOffset The starting offset in the target array.
     * @param length       The number of bytes to read.
     * @throws NullPointerException     if the provided array is {@code null}.
     * @throws IllegalArgumentException if {@code targetOffset} or {@code length} is negative, or if
     *                                  {@code targetBuffer} is too small.
     * @throws IllegalStateException    if there are not enough bytes in the buffer. Use
     *                                  {@link #getCurrentNumberOfBytes()} to check.
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
                    + " bytes, but offset, and length would require " + (targetOffset + length));
        }
        if (currentNumberOfBytes < length) {
            throw new IllegalStateException(
                    "Currently, there are only " + currentNumberOfBytes + " bytes in the buffer, not " + length);
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
     * @throws IllegalStateException if the buffer is full. Use {@link #hasSpace()} or {@link #getSpace()} to check
     *                               before adding.
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
     * Returns whether the next bytes in the buffer are exactly those given by {@code sourceBuffer}. No bytes are
     * removed from the buffer.
     *
     * @param sourceBuffer The buffer to compare against.
     * @param offset       The starting offset in {@code sourceBuffer}.
     * @param length       The number of bytes to compare.
     * @return True if the next bytes in the buffer match the given sequence, false otherwise.
     * @throws IllegalArgumentException if {@code offset} or {@code length} is negative.
     * @throws NullPointerException     if {@code sourceBuffer} is null.
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (offset < 0 || offset >= sourceBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (length < 0 || length > buffer.length) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
        if (length > currentNumberOfBytes) {
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
     * Adds the given bytes to the buffer. This is equivalent to invoking {@link #add(byte)} for each byte.
     *
     * @param sourceBuffer The buffer to copy from.
     * @param offset       The starting offset in {@code sourceBuffer}.
     * @param length       The number of bytes to copy.
     * @throws IllegalStateException    if the buffer does not have sufficient space.
     * @throws IllegalArgumentException if {@code offset} or {@code length} is negative.
     * @throws NullPointerException     if {@code sourceBuffer} is null.
     */
    public void add(final byte[] sourceBuffer, final int offset, final int length) {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (offset < 0 || offset >= sourceBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (length < 0) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
        if (currentNumberOfBytes + length > buffer.length) {
            throw new IllegalStateException("No space available");
        }
        for (int i = 0; i < length; i++) {
            buffer[endOffset] = sourceBuffer[offset + i];
            if (++endOffset == buffer.length) {
                endOffset = 0;
            }
        }
        currentNumberOfBytes += length;
    }

    /**
     * Returns whether there is currently room for at least one byte in the buffer.
     *
     * @return True if there is space for a byte, false otherwise.
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
     * @return True if there is space for the given number of bytes, false otherwise.
     * @see #hasSpace()
     * @see #getSpace()
     */
    public boolean hasSpace(final int count) {
        return currentNumberOfBytes + count <= buffer.length;
    }

    /**
     * Returns whether the buffer is currently holding at least one byte.
     *
     * @return True if the buffer is not empty, false otherwise.
     */
    public boolean hasBytes() {
        return currentNumberOfBytes > 0;
    }

    /**
     * Returns the number of bytes that can currently be added to the buffer.
     *
     * @return The number of available bytes.
     */
    public int getSpace() {
        return buffer.length - currentNumberOfBytes;
    }

    /**
     * Returns the number of bytes that are currently present in the buffer.
     *
     * @return The number of bytes currently in the buffer.
     */
    public int getCurrentNumberOfBytes() {
        return currentNumberOfBytes;
    }

    /**
     * Removes all bytes from the buffer, resetting its state.
     */
    public void clear() {
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

}

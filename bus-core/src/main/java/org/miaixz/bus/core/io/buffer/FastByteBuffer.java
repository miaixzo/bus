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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * A fast byte buffer implementation that stores data in a collection of byte arrays (chunks) instead of a single array.
 * This approach reduces memory copying and improves performance for growing buffers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastByteBuffer extends FastBuffer {

    /**
     * The collection of byte array chunks that store the buffer's data.
     */
    private byte[][] buffers = new byte[16][];
    /**
     * The currently active byte array chunk for writing.
     */
    private byte[] currentBuffer;

    /**
     * Constructs a {@code FastByteBuffer} with a default minimum chunk size of 8192 bytes.
     */
    public FastByteBuffer() {
        this(Normal._8192);
    }

    /**
     * Constructs a {@code FastByteBuffer} with the specified minimum chunk size.
     *
     * @param size The minimum byte length for a single chunk.
     */
    public FastByteBuffer(final int size) {
        super(size);
    }

    /**
     * Appends a portion of a byte array to this fast buffer.
     *
     * @param array The source byte array.
     * @param off   The starting offset in the source array.
     * @param len   The number of bytes to append.
     * @return This {@code FastByteBuffer} instance.
     * @throws IndexOutOfBoundsException If {@code off} or {@code len} are negative, or if {@code off + len} is greater
     *                                   than {@code array.length}.
     */
    public FastByteBuffer append(final byte[] array, final int off, final int len) {
        final int end = off + len;
        if ((off < 0) || (len < 0) || (end > array.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return this;
        }
        final int newSize = size + len;
        int remaining = len;

        if (currentBuffer != null) {
            // first try to fill current buffer
            final int part = Math.min(remaining, currentBuffer.length - offset);
            System.arraycopy(array, end - remaining, currentBuffer, offset, part);
            remaining -= part;
            offset += part;
            size += part;
        }

        if (remaining > 0) {
            // still some data left
            // ask for new buffer
            ensureCapacity(newSize);

            // then copy remaining
            // but this time we are sure that it will fit
            final int part = Math.min(remaining, currentBuffer.length - offset);
            System.arraycopy(array, end - remaining, currentBuffer, offset, part);
            offset += part;
            size += part;
        }

        return this;
    }

    /**
     * Appends an entire byte array to this fast buffer.
     *
     * @param array The byte array to append.
     * @return This {@code FastByteBuffer} instance.
     */
    public FastByteBuffer append(final byte[] array) {
        return append(array, 0, array.length);
    }

    /**
     * Appends a single byte to this fast buffer.
     *
     * @param element The byte to append.
     * @return This {@code FastByteBuffer} instance.
     */
    public FastByteBuffer append(final byte element) {
        if ((currentBuffer == null) || (offset == currentBuffer.length)) {
            ensureCapacity(size + 1);
        }

        currentBuffer[offset] = element;
        offset++;
        this.size++;

        return this;
    }

    /**
     * Appends the entire contents of another {@code FastByteBuffer} to this buffer.
     *
     * @param buff The {@code FastByteBuffer} to append.
     * @return This {@code FastByteBuffer} instance.
     */
    public FastByteBuffer append(final FastByteBuffer buff) {
        if (buff.size == 0) {
            return this;
        }
        for (int i = 0; i < buff.currentBufferIndex; i++) {
            append(buff.buffers[i]);
        }
        append(buff.currentBuffer, 0, buff.offset);
        return this;
    }

    /**
     * Returns the byte array chunk at the specified index.
     *
     * @param index The index of the buffer chunk to retrieve.
     * @return The byte array chunk.
     */
    public byte[] array(final int index) {
        return buffers[index];
    }

    /**
     * Resets the buffer to its initial empty state, clearing all data and resetting internal pointers.
     */
    @Override
    public void reset() {
        super.reset();
        currentBuffer = null;
    }

    /**
     * Returns the total length of the data currently stored in the buffer. This method is an alias for {@link #size()}.
     *
     * @return The total length of the buffer in bytes.
     */
    @Override
    public int length() {
        return this.size;
    }

    /**
     * Returns the data in the fast buffer as a byte array. If the buffer contains data in a single, contiguous array
     * and its length matches the buffer's size, a zero-copy operation is performed by returning the internal array
     * directly. Otherwise, a new array is created and populated.
     * <p>
     * Note: If the internal array is returned, modifications to it will affect this buffer.
     *
     * @return A byte array containing the buffer's data.
     */
    public byte[] toArrayZeroCopyIfPossible() {
        if (1 == currentBufferIndex) {
            final int len = buffers[0].length;
            if (len == size) {
                return buffers[0];
            }
        }

        return toArray();
    }

    /**
     * Returns the data in the fast buffer as a new byte array.
     *
     * @return A new byte array containing the buffer's data.
     */
    public byte[] toArray() {
        return toArray(0, this.size);
    }

    /**
     * Returns a portion of the data in the fast buffer as a new byte array.
     *
     * @param start The logical starting position within the buffer (inclusive).
     * @param len   The logical length of bytes to retrieve.
     * @return A new byte array containing the specified portion of the buffer's data.
     * @throws IllegalArgumentException If {@code start} or {@code len} is negative.
     */
    public byte[] toArray(int start, int len) {
        Assert.isTrue(start >= 0, "Start must be greater than zero!");
        Assert.isTrue(len >= 0, "Length must be greater than zero!");

        if (start >= this.size || len == 0) {
            return new byte[0];
        }
        if (len > (this.size - start)) {
            len = this.size - start;
        }
        int remaining = len;
        int pos = 0;
        final byte[] result = new byte[len];

        int i = 0;
        while (start >= buffers[i].length) {
            start -= buffers[i].length;
            i++;
        }

        while (i < buffersCount) {
            final byte[] buf = buffers[i];
            final int bufLen = Math.min(buf.length - start, remaining);
            System.arraycopy(buf, start, result, pos, bufLen);
            pos += bufLen;
            remaining -= bufLen;
            if (remaining == 0) {
                break;
            }
            start = 0;
            i++;
        }
        return result;
    }

    /**
     * Returns the byte at the specified logical index within the buffer.
     *
     * @param index The logical index of the byte to retrieve.
     * @return The byte at the specified index.
     * @throws IndexOutOfBoundsException If {@code index} is out of bounds (negative or greater than or equal to the
     *                                   buffer's size).
     */
    public byte get(int index) {
        if ((index >= this.size) || (index < 0)) {
            throw new IndexOutOfBoundsException();
        }
        int ndx = 0;
        while (true) {
            final byte[] b = buffers[ndx];
            if (index < b.length) {
                return b[index];
            }
            ndx++;
            index -= b.length;
        }
    }

    /**
     * Ensures that the buffer has enough capacity to accommodate the specified amount of data. If the existing buffer
     * is insufficient, a new byte array chunk will be allocated. The allocated chunk will not be smaller than
     * {@link #minChunkLen}. If the {@code buffers} array itself needs to grow, it will be doubled in size.
     *
     * @param capacity The desired total capacity in bytes.
     */
    @Override
    protected void ensureCapacity(final int capacity) {
        final int delta = capacity - this.size;
        final int newBufferSize = Math.max(minChunkLen, delta);

        currentBufferIndex++;
        currentBuffer = new byte[newBufferSize];
        offset = 0;

        // add buffer
        if (currentBufferIndex >= buffers.length) {
            final int newLen = buffers.length << 1;
            final byte[][] newBuffers = new byte[newLen][];
            System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
            buffers = newBuffers;
        }
        buffers[currentBufferIndex] = currentBuffer;
        buffersCount++;
    }

}

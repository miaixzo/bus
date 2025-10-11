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
package org.miaixz.bus.core.io.buffer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * A fast character buffer implementation that stores data in a collection of character arrays (chunks) instead of a
 * single array. This approach reduces memory copying and improves performance for growing buffers, especially during
 * frequent append operations.
 * <p>
 * Note: While this buffer offers better performance than {@link StringBuilder} for many repeated append operations, its
 * {@link #toArray()} method might have poorer performance due to the need to consolidate multiple character arrays into
 * one.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastCharBuffer extends FastBuffer implements CharSequence, Appendable {

    /**
     * The collection of character array chunks that store the buffer's data.
     */
    private char[][] buffers = new char[16][];
    /**
     * The currently active character array chunk for writing.
     */
    private char[] currentBuffer;

    /**
     * Constructs a {@code FastCharBuffer} with a default minimum chunk size of 8192 characters.
     */
    public FastCharBuffer() {
        this(Normal._8192);
    }

    /**
     * Constructs a {@code FastCharBuffer} with the specified minimum chunk size.
     *
     * @param size The minimum character length for a single chunk.
     */
    public FastCharBuffer(final int size) {
        super(size);
    }

    /**
     * Appends a portion of a character array to this fast buffer.
     *
     * @param array The source character array.
     * @param off   The starting offset in the source array.
     * @param len   The number of characters to append.
     * @return This {@code FastCharBuffer} instance.
     * @throws IndexOutOfBoundsException If {@code off} or {@code len} are negative, or if {@code off + len} is greater
     *                                   than {@code array.length}.
     */
    public FastCharBuffer append(final char[] array, final int off, final int len) {
        final int end = off + len;
        if ((off < 0) || (len < 0) || (end > array.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return this;
        }
        final int newSize = this.size + len;
        int remaining = len;

        if (currentBuffer != null) {
            // first try to fill current buffer
            final int part = Math.min(remaining, currentBuffer.length - offset);
            System.arraycopy(array, end - remaining, currentBuffer, offset, part);
            remaining -= part;
            offset += part;
            this.size += part;
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
            this.size += part;
        }

        return this;
    }

    /**
     * Appends an entire character array to this fast buffer.
     *
     * @param array The character array to append.
     * @return This {@code FastCharBuffer} instance.
     */
    public FastCharBuffer append(final char[] array) {
        return append(array, 0, array.length);
    }

    /**
     * Appends a single character to this fast buffer.
     *
     * @param element The character to append.
     * @return This {@code FastCharBuffer} instance.
     */
    public FastCharBuffer append(final char element) {
        if ((currentBuffer == null) || (offset == currentBuffer.length)) {
            ensureCapacity(this.size + 1);
        }

        currentBuffer[offset] = element;
        offset++;
        this.size++;

        return this;
    }

    /**
     * Appends the entire contents of another {@code FastCharBuffer} to this buffer.
     *
     * @param buff The {@code FastCharBuffer} to append.
     * @return This {@code FastCharBuffer} instance.
     */
    public FastCharBuffer append(final FastCharBuffer buff) {
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
     * Returns the character array chunk at the specified index.
     *
     * @param index The index of the buffer chunk to retrieve.
     * @return The character array chunk.
     */
    public char[] array(final int index) {
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
     * Returns the data in the fast buffer as a character array. If the buffer contains data in a single, contiguous
     * array and its length matches the buffer's size, a zero-copy operation is performed by returning the internal
     * array directly. Otherwise, a new array is created and populated.
     * <p>
     * Note: If the internal array is returned, modifications to it will affect this buffer.
     *
     * @return A character array containing the buffer's data.
     */
    public char[] toArrayZeroCopyIfPossible() {
        if (1 == currentBufferIndex) {
            final int len = buffers[0].length;
            if (len == this.size) {
                return buffers[0];
            }
        }

        return toArray();
    }

    /**
     * Returns the data in the fast buffer as a new character array.
     *
     * @return A new character array containing the buffer's data.
     */
    public char[] toArray() {
        return toArray(0, this.size);
    }

    /**
     * Returns a portion of the data in the fast buffer as a new character array.
     *
     * @param start The logical starting position within the buffer (inclusive).
     * @param len   The logical length of characters to retrieve.
     * @return A new character array containing the specified portion of the buffer's data.
     * @throws IllegalArgumentException If {@code start} or {@code len} is negative.
     */
    public char[] toArray(int start, int len) {
        Assert.isTrue(start >= 0, "Start must be greater than zero!");
        Assert.isTrue(len >= 0, "Length must be greater than zero!");

        if (start >= this.size || len == 0) {
            return new char[0];
        }
        if (len > (this.size - start)) {
            len = this.size - start;
        }
        int remaining = len;
        int pos = 0;
        final char[] result = new char[len];

        int i = 0;
        while (start >= buffers[i].length) {
            start -= buffers[i].length;
            i++;
        }

        while (i < buffersCount) {
            final char[] buf = buffers[i];
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
     * Returns the character at the specified logical index within the buffer.
     *
     * @param index The logical index of the character to retrieve.
     * @return The character at the specified index.
     * @throws IndexOutOfBoundsException If {@code index} is out of bounds (negative or greater than or equal to the
     *                                   buffer's size).
     */
    public char get(int index) {
        if ((index >= this.size) || (index < 0)) {
            throw new IndexOutOfBoundsException();
        }
        int ndx = 0;
        while (true) {
            final char[] b = buffers[ndx];
            if (index < b.length) {
                return b[index];
            }
            ndx++;
            index -= b.length;
        }
    }

    /**
     * Returns a string representation of the data in this buffer.
     *
     * @return A string containing all characters from this buffer.
     */
    @Override
    public String toString() {
        return new String(toArray());
    }

    /**
     * Returns the character at the specified index.
     *
     * @param index The index of the character to retrieve.
     * @return The character at the specified index.
     * @throws IndexOutOfBoundsException If {@code index} is out of bounds.
     */
    @Override
    public char charAt(final int index) {
        return get(index);
    }

    /**
     * Returns a new {@code CharSequence} that is a subsequence of this character buffer. The subsequence starts with
     * the character at the specified {@code start} and extends to the character at index {@code end - 1}.
     *
     * @param start The start index, inclusive.
     * @param end   The end index, exclusive.
     * @return The specified subsequence.
     * @throws IndexOutOfBoundsException If {@code start} or {@code end} are negative, or if {@code start} is greater
     *                                   than {@code end}, or if {@code end} is greater than {@link #length()}.
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        final int len = end - start;
        return new StringBuilder(len).append(toArray(start, len));
    }

    /**
     * Appends the specified character sequence to this buffer.
     *
     * @param csq The character sequence to append.
     * @return This {@code FastCharBuffer} instance.
     */
    @Override
    public FastCharBuffer append(final CharSequence csq) {
        if (csq instanceof String) {
            return append((String) csq);
        }
        return append(csq, 0, csq.length());
    }

    /**
     * Appends a subsequence of the specified character sequence to this buffer.
     *
     * @param csq   The character sequence to append.
     * @param start The start index of the subsequence, inclusive.
     * @param end   The end index of the subsequence, exclusive.
     * @return This {@code FastCharBuffer} instance.
     */
    @Override
    public FastCharBuffer append(final CharSequence csq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    /**
     * Appends the specified string to this buffer.
     *
     * @param string The string to append.
     * @return This {@code FastCharBuffer} instance.
     */
    public FastCharBuffer append(final String string) {
        final int len = string.length();
        if (len == 0) {
            return this;
        }

        final int newSize = this.size + len;
        int remaining = len;
        int start = 0;

        if (currentBuffer != null) {
            // first try to fill current buffer
            final int part = Math.min(remaining, currentBuffer.length - offset);
            string.getChars(0, part, currentBuffer, offset);
            remaining -= part;
            offset += part;
            this.size += part;
            start += part;
        }

        if (remaining > 0) {
            // still some data left
            // ask for new buffer
            ensureCapacity(newSize);

            // then copy remaining
            // but this time we are sure that it will fit
            final int part = Math.min(remaining, currentBuffer.length - offset);
            string.getChars(start, start + part, currentBuffer, offset);
            offset += part;
            this.size += part;
        }

        return this;
    }

    /**
     * Ensures that the buffer has enough capacity to accommodate the specified amount of data. If the existing buffer
     * is insufficient, a new character array chunk will be allocated. The allocated chunk will not be smaller than
     * {@link #minChunkLen}. If the {@code buffers} array itself needs to grow, it will be doubled in size.
     *
     * @param capacity The desired total capacity in characters.
     */
    @Override
    protected void ensureCapacity(final int capacity) {
        final int delta = capacity - this.size;
        final int newBufferSize = Math.max(minChunkLen, delta);

        currentBufferIndex++;
        currentBuffer = new char[newBufferSize];
        offset = 0;

        // add buffer
        if (currentBufferIndex >= buffers.length) {
            final int newLen = buffers.length << 1;
            final char[][] newBuffers = new char[newLen][];
            System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
            buffers = newBuffers;
        }
        buffers[currentBufferIndex] = currentBuffer;
        buffersCount++;
    }

}

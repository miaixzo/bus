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

import org.miaixz.bus.core.lang.Normal;

/**
 * Abstract base class for fast buffers, designed for efficient reading and writing of data, minimizing memory copying.
 * Compared to standard buffers, this implementation uses a two-dimensional array to extend length, reducing memory
 * copying and improving performance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FastBuffer {

    /**
     * The minimum byte length for a single chunk (segment) within the buffer.
     */
    protected final int minChunkLen;

    /**
     * The total number of chunks (buffers) currently allocated.
     */
    protected int buffersCount;
    /**
     * The index of the currently active buffer chunk.
     */
    protected int currentBufferIndex = -1;
    /**
     * The current offset within the active buffer chunk.
     */
    protected int offset;
    /**
     * The total size of the data currently stored in the buffer.
     */
    protected int size;

    /**
     * Constructs a {@code FastBuffer} with a specified minimum chunk size. If the provided size is non-positive, it
     * defaults to {@link Normal#_8192}.
     *
     * @param size The minimum byte length for a single chunk.
     */
    public FastBuffer(int size) {
        if (size <= 0) {
            size = Normal._8192;
        }
        this.minChunkLen = Math.abs(size);
    }

    /**
     * Returns the index of the current buffer chunk.
     *
     * @return The index of the current buffer chunk.
     */
    public int index() {
        return this.currentBufferIndex;
    }

    /**
     * Returns the current offset within the active buffer chunk.
     *
     * @return The current offset.
     */
    public int offset() {
        return this.offset;
    }

    /**
     * Resets the buffer to its initial empty state, clearing all data and resetting internal pointers.
     */
    public void reset() {
        this.size = 0;
        this.offset = 0;
        this.currentBufferIndex = -1;
        this.buffersCount = 0;
    }

    /**
     * Returns the total size of the data currently stored in the buffer.
     *
     * @return The total size of the buffer in bytes.
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns the total length of the data currently stored in the buffer. This method is an alias for {@link #size()}.
     *
     * @return The total length of the buffer in bytes.
     */
    public int length() {
        return this.size;
    }

    /**
     * Checks if the buffer is empty.
     *
     * @return {@code true} if the buffer contains no data, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Ensures that the buffer has enough capacity to accommodate the specified amount of data. If the existing buffer
     * is insufficient, a new buffer chunk will be allocated. The allocated chunk will not be smaller than the minimum
     * chunk length.
     *
     * @param capacity The desired capacity in bytes.
     */
    abstract protected void ensureCapacity(final int capacity);

}

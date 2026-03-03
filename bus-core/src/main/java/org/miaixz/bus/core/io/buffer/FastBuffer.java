/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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

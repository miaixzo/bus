/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * Represents a virtual {@link ByteBuffer} that is a slice of a larger, underlying {@link BufferPage}.
 * <p>
 * This class allows for the management of smaller buffer segments without the overhead of allocating and deallocating
 * individual {@code ByteBuffer} objects. It tracks its position within the parent buffer page and provides a mechanism
 * for releasing the buffer back to the page for reuse.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class VirtualBuffer {

    /**
     * The {@link BufferPage} to which this virtual buffer belongs.
     */
    private final BufferPage bufferPage;
    /**
     * A semaphore to ensure that the buffer is cleaned (released) only once.
     */
    private final Semaphore clean = new Semaphore(1);
    /**
     * The virtual {@link ByteBuffer}, which is a slice of the parent buffer.
     *
     * @see ByteBuffer#slice()
     */
    private ByteBuffer buffer;
    /**
     * The starting position of this virtual buffer within the parent buffer.
     */
    private int parentPosition;

    /**
     * The ending limit of this virtual buffer within the parent buffer.
     */
    private int parentLimit;

    /**
     * The capacity of this virtual buffer.
     */
    private int capacity;

    /**
     * Constructs a new VirtualBuffer.
     *
     * @param bufferPage     the parent buffer page
     * @param buffer         the sliced ByteBuffer
     * @param parentPosition the starting position in the parent buffer
     * @param parentLimit    the ending limit in the parent buffer
     */
    VirtualBuffer(BufferPage bufferPage, ByteBuffer buffer, int parentPosition, int parentLimit) {
        this.bufferPage = bufferPage;
        this.buffer = buffer;
        this.parentPosition = parentPosition;
        this.parentLimit = parentLimit;
        updateCapacity();
    }

    /**
     * Wraps an existing {@link ByteBuffer} in a VirtualBuffer.
     * <p>
     * The resulting VirtualBuffer is not associated with a {@link BufferPage} and cannot be recycled.
     * </p>
     *
     * @param buffer the ByteBuffer to wrap
     * @return a new VirtualBuffer instance
     */
    public static VirtualBuffer wrap(ByteBuffer buffer) {
        return new VirtualBuffer(null, buffer, 0, 0);
    }

    /**
     * Gets the starting position of this virtual buffer within its parent buffer.
     *
     * @return the parent position
     */
    int getParentPosition() {
        return parentPosition;
    }

    /**
     * Sets the starting position of this virtual buffer within its parent buffer.
     *
     * @param parentPosition the new parent position
     */
    void setParentPosition(int parentPosition) {
        this.parentPosition = parentPosition;
        updateCapacity();
    }

    /**
     * Gets the ending limit of this virtual buffer within its parent buffer.
     *
     * @return the parent limit
     */
    int getParentLimit() {
        return parentLimit;
    }

    /**
     * Sets the ending limit of this virtual buffer within its parent buffer.
     *
     * @param parentLimit the new parent limit
     */
    void setParentLimit(int parentLimit) {
        this.parentLimit = parentLimit;
        updateCapacity();
    }

    /**
     * Updates the capacity of this virtual buffer based on its parent position and limit.
     */
    private void updateCapacity() {
        this.capacity = this.parentLimit - this.parentPosition;
    }

    /**
     * Gets the capacity of this virtual buffer.
     *
     * @return the capacity in bytes
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the underlying {@link ByteBuffer}.
     *
     * @return the actual ByteBuffer
     */
    public ByteBuffer buffer() {
        return buffer;
    }

    /**
     * Sets the underlying {@link ByteBuffer} and releases the clean semaphore.
     *
     * @param buffer the new ByteBuffer
     */
    void buffer(ByteBuffer buffer) {
        this.buffer = buffer;
        clean.release();
    }

    /**
     * Releases this virtual buffer, allowing its memory to be reused.
     * <p>
     * If this buffer belongs to a {@link BufferPage}, it will be returned to the page for recycling.
     * </p>
     *
     * @throws UnsupportedOperationException if the buffer has already been cleaned
     */
    public void clean() {
        if (clean.tryAcquire()) {
            if (bufferPage != null) {
                bufferPage.clean(this);
            }
        } else {
            throw new UnsupportedOperationException("buffer has already been cleaned");
        }
    }

    @Override
    public String toString() {
        return "VirtualBuffer{" + "parentPosition=" + parentPosition + ", parentLimit=" + parentLimit + '}';
    }

}

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a memory page for {@link ByteBuffer} instances.
 * <p>
 * This class manages a single, large, underlying {@code ByteBuffer} (either direct or heap-allocated). It allows for
 * the allocation of smaller, logical {@link VirtualBuffer} instances from this page. It handles the lifecycle of these
 * virtual buffers, including allocation and recycling, to reduce the overhead of frequent memory allocation and garbage
 * collection.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class BufferPage {

    /**
     * A lock to ensure thread-safe allocation and cleaning of buffers.
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * The underlying physical buffer for this page.
     */
    private final ByteBuffer buffer;
    /**
     * A queue of virtual buffers that are pending cleanup and recycling.
     */
    private final ConcurrentLinkedQueue<VirtualBuffer> cleanBuffers = new ConcurrentLinkedQueue<>();
    /**
     * A list of currently available (free) virtual buffers within this page.
     */
    private final List<VirtualBuffer> availableBuffers;
    /**
     * A flag indicating whether the buffer page is currently idle.
     */
    private boolean idle = true;

    /**
     * Constructs a new BufferPage.
     *
     * @param size   the size of the buffer page
     * @param direct whether to use a direct (off-heap) buffer
     */
    BufferPage(int size, boolean direct) {
        availableBuffers = new LinkedList<>();
        this.buffer = allocate0(size, direct);
        availableBuffers.add(new VirtualBuffer(this, null, buffer.position(), buffer.limit()));
    }

    /**
     * Allocates the physical buffer for this page.
     *
     * @param size   the size of the buffer to allocate
     * @param direct {@code true} for a direct buffer, {@code false} for a heap buffer
     * @return the allocated {@link ByteBuffer}
     */
    private ByteBuffer allocate0(int size, boolean direct) {
        return direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
    }

    /**
     * Allocates a virtual buffer of the specified size from this page.
     *
     * @param size the size of the virtual buffer to allocate
     * @return a {@link VirtualBuffer} of the requested size
     * @throws UnsupportedOperationException if the requested size is zero
     */
    public VirtualBuffer allocate(final int size) {
        if (size == 0) {
            throw new UnsupportedOperationException("cannot allocate zero bytes");
        }
        VirtualBuffer virtualBuffer = allocate0(size);
        return virtualBuffer == null ? new VirtualBuffer(null, allocate0(size, false), 0, 0) : virtualBuffer;
    }

    /**
     * Internal method to allocate a virtual buffer.
     *
     * @param size the size of the virtual buffer to allocate
     * @return an allocated {@link VirtualBuffer}, or {@code null} if allocation fails
     */
    private VirtualBuffer allocate0(final int size) {
        idle = false;
        VirtualBuffer cleanBuffer = cleanBuffers.poll();
        if (cleanBuffer != null && cleanBuffer.getCapacity() >= size) {
            cleanBuffer.buffer().clear();
            cleanBuffer.buffer(cleanBuffer.buffer());
            return cleanBuffer;
        }
        lock.lock();
        try {
            if (cleanBuffer != null) {
                clean0(cleanBuffer);
                while ((cleanBuffer = cleanBuffers.poll()) != null) {
                    if (cleanBuffer.getCapacity() >= size) {
                        cleanBuffer.buffer().clear();
                        cleanBuffer.buffer(cleanBuffer.buffer());
                        return cleanBuffer;
                    } else {
                        clean0(cleanBuffer);
                    }
                }
            }

            int count = availableBuffers.size();
            VirtualBuffer bufferChunk = null;
            // Use a fast allocation algorithm if only one free chunk remains
            if (count == 1) {
                bufferChunk = fastAllocate(size);
            } else if (count > 1) {
                bufferChunk = slowAllocate(size);
            }
            return bufferChunk;
        } finally {
            lock.unlock();
        }
    }

    /**
     * A fast allocation algorithm for when there is only one available free chunk.
     *
     * @param size the requested allocation size
     * @return an allocated {@link VirtualBuffer}, or {@code null} if space is insufficient
     */
    private VirtualBuffer fastAllocate(int size) {
        VirtualBuffer freeChunk = availableBuffers.get(0);
        VirtualBuffer bufferChunk = allocate(size, freeChunk);
        if (freeChunk == bufferChunk) {
            availableBuffers.clear();
        }
        return bufferChunk;
    }

    /**
     * An iterative allocation algorithm for when there are multiple available free chunks.
     *
     * @param size the requested allocation size
     * @return an allocated {@link VirtualBuffer}, or {@code null} if space is insufficient
     */
    private VirtualBuffer slowAllocate(int size) {
        Iterator<VirtualBuffer> iterator = availableBuffers.listIterator(0);
        VirtualBuffer bufferChunk;
        while (iterator.hasNext()) {
            VirtualBuffer freeChunk = iterator.next();
            bufferChunk = allocate(size, freeChunk);
            if (freeChunk == bufferChunk) {
                iterator.remove();
            }
            if (bufferChunk != null) {
                return bufferChunk;
            }
        }
        return null;
    }

    /**
     * Allocates a smaller buffer chunk from a larger free chunk.
     *
     * @param size      the requested size of the new chunk
     * @param freeChunk the larger free chunk from which to allocate
     * @return an allocated {@link VirtualBuffer}, or {@code null} if the free chunk is too small
     */
    private VirtualBuffer allocate(int size, VirtualBuffer freeChunk) {
        final int capacity = freeChunk.getCapacity();
        if (capacity < size) {
            return null;
        }
        VirtualBuffer bufferChunk;
        if (capacity == size) {
            buffer.limit(freeChunk.getParentLimit());
            buffer.position(freeChunk.getParentPosition());
            freeChunk.buffer(buffer.slice());
            bufferChunk = freeChunk;
        } else {
            buffer.limit(freeChunk.getParentPosition() + size);
            buffer.position(freeChunk.getParentPosition());
            bufferChunk = new VirtualBuffer(this, buffer.slice(), buffer.position(), buffer.limit());
            freeChunk.setParentPosition(buffer.limit());
        }
        if (bufferChunk.buffer().remaining() != size) {
            throw new RuntimeException("allocate " + size + ", buffer:" + bufferChunk);
        }
        return bufferChunk;
    }

    /**
     * Marks a virtual buffer for cleaning by adding it to the clean queue.
     *
     * @param cleanBuffer the virtual buffer to be cleaned
     */
    void clean(VirtualBuffer cleanBuffer) {
        cleanBuffers.offer(cleanBuffer);
    }

    /**
     * Attempts to clean and merge any pending buffers in the clean queue. This method is intended to be called
     * periodically and will only perform cleanup if the page is idle to avoid contention.
     */
    void tryClean() {
        // If the page is still idle in the next cycle, trigger the cleanup task
        if (!idle) {
            idle = true;
        } else if (!cleanBuffers.isEmpty() && lock.tryLock()) {
            try {
                VirtualBuffer cleanBuffer;
                while ((cleanBuffer = cleanBuffers.poll()) != null) {
                    clean0(cleanBuffer);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Recycles a virtual buffer by merging it back into the list of available buffers.
     *
     * @param cleanBuffer the virtual buffer to recycle
     */
    private void clean0(VirtualBuffer cleanBuffer) {
        ListIterator<VirtualBuffer> iterator = availableBuffers.listIterator(0);
        while (iterator.hasNext()) {
            VirtualBuffer freeBuffer = iterator.next();
            // Case 1: The cleanBuffer is immediately before the freeBuffer and can be merged
            if (freeBuffer.getParentPosition() == cleanBuffer.getParentLimit()) {
                freeBuffer.setParentPosition(cleanBuffer.getParentPosition());
                return;
            }
            // Case 2: The cleanBuffer is immediately after the freeBuffer and can be merged
            if (freeBuffer.getParentLimit() == cleanBuffer.getParentPosition()) {
                freeBuffer.setParentLimit(cleanBuffer.getParentLimit());
                // Check if the next buffer is also contiguous and merge it as well
                if (iterator.hasNext()) {
                    VirtualBuffer next = iterator.next();
                    if (next.getParentPosition() == freeBuffer.getParentLimit()) {
                        freeBuffer.setParentLimit(next.getParentLimit());
                        iterator.remove();
                    } else if (next.getParentPosition() < freeBuffer.getParentLimit()) {
                        throw new IllegalStateException("Buffer order inconsistency detected");
                    }
                }
                return;
            }
            // Case 3: The cleanBuffer should be inserted before the current freeBuffer
            if (freeBuffer.getParentPosition() > cleanBuffer.getParentLimit()) {
                iterator.previous();
                iterator.add(cleanBuffer);
                return;
            }
        }
        // Case 4: The cleanBuffer should be added to the end of the list
        iterator.add(cleanBuffer);
    }

    /**
     * Releases the resources held by this buffer page. For direct buffers, this does not explicitly deallocate memory
     * but clears the buffer.
     */
    void release() {
        if (buffer.isDirect()) {
            buffer.clear();
        }
    }

    @Override
    public String toString() {
        return "BufferPage{" + "availableBuffers=" + availableBuffers + ", " + "cleanBuffers=" + cleanBuffers + '}';
    }

}

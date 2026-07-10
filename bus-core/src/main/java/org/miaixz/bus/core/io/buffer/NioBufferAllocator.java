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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allocates heap or direct NIO byte buffers with a bounded idle cache.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class NioBufferAllocator implements AutoCloseable {

    /**
     * Default reusable NIO buffer size.
     */
    public static final int DEFAULT_BUFFER_SIZE = Segment.SIZE;

    /**
     * Default maximum number of idle buffers retained by an allocator.
     */
    public static final int DEFAULT_MAX_IDLE = 256;

    /**
     * Idle reusable buffers.
     */
    private final ConcurrentLinkedQueue<java.nio.ByteBuffer> idleBuffers = new ConcurrentLinkedQueue<>();

    /**
     * Number of idle reusable buffers.
     */
    private final AtomicInteger idleCount = new AtomicInteger();

    /**
     * Capacity of reusable buffers retained by this allocator.
     */
    private final int bufferSize;

    /**
     * Maximum number of idle buffers to retain.
     */
    private final int maxIdle;

    /**
     * Whether this allocator creates direct buffers.
     */
    private final boolean direct;

    /**
     * Whether this allocator has been closed.
     */
    private volatile boolean closed;

    /**
     * Creates a heap buffer allocator with the default settings.
     *
     * @return a heap NIO buffer allocator
     */
    public static NioBufferAllocator heap() {
        return heap(DEFAULT_BUFFER_SIZE, DEFAULT_MAX_IDLE);
    }

    /**
     * Creates a heap buffer allocator.
     *
     * @param bufferSize the reusable buffer size
     * @param maxIdle    the maximum idle buffer count
     * @return a heap NIO buffer allocator
     */
    public static NioBufferAllocator heap(int bufferSize, int maxIdle) {
        return new NioBufferAllocator(bufferSize, maxIdle, false);
    }

    /**
     * Creates a direct buffer allocator with the default settings.
     *
     * @return a direct NIO buffer allocator
     */
    public static NioBufferAllocator direct() {
        return direct(DEFAULT_BUFFER_SIZE, DEFAULT_MAX_IDLE);
    }

    /**
     * Creates a direct buffer allocator.
     *
     * @param bufferSize the reusable buffer size
     * @param maxIdle    the maximum idle buffer count
     * @return a direct NIO buffer allocator
     */
    public static NioBufferAllocator direct(int bufferSize, int maxIdle) {
        return new NioBufferAllocator(bufferSize, maxIdle, true);
    }

    /**
     * Creates a NIO buffer allocator.
     *
     * @param bufferSize the reusable buffer size
     * @param maxIdle    the maximum idle buffer count
     * @param direct     whether buffers should be direct
     */
    public NioBufferAllocator(int bufferSize, int maxIdle, boolean direct) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize);
        }
        if (maxIdle < 0) {
            throw new IllegalArgumentException("maxIdle < 0: " + maxIdle);
        }
        this.bufferSize = bufferSize;
        this.maxIdle = maxIdle;
        this.direct = direct;
    }

    /**
     * Allocates a reusable buffer lease.
     *
     * @return a leased NIO buffer
     */
    public NioBuffer allocate() {
        return allocate(bufferSize);
    }

    /**
     * Allocates a buffer lease with at least {@code minCapacity} bytes.
     *
     * <p>
     * Requests no larger than the configured reusable size are served from the allocator cache. Larger requests
     * allocate a one-off buffer with the requested capacity, avoiding pollution of the fixed-size cache.
     *
     * @param minCapacity the minimum required capacity
     * @return a leased NIO buffer
     */
    public NioBuffer allocate(int minCapacity) {
        if (minCapacity <= 0) {
            throw new IllegalArgumentException("minCapacity <= 0: " + minCapacity);
        }
        assertOpen();
        if (minCapacity > bufferSize) {
            final java.nio.ByteBuffer buffer = newByteBuffer(minCapacity);
            assertOpen();
            return new NioBuffer(null, buffer, false);
        }

        java.nio.ByteBuffer buffer;
        while ((buffer = idleBuffers.poll()) != null) {
            idleCount.decrementAndGet();
            buffer.clear();
            assertOpen();
            return new NioBuffer(this, buffer, true);
        }
        buffer = newByteBuffer(bufferSize);
        assertOpen();
        return new NioBuffer(this, buffer, true);
    }

    /**
     * Returns the reusable buffer size.
     *
     * @return the reusable buffer size
     */
    public int bufferSize() {
        return bufferSize;
    }

    /**
     * Returns whether this allocator creates direct buffers.
     *
     * @return {@code true} if this allocator creates direct buffers
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * Returns whether this allocator has been closed.
     *
     * @return {@code true} if this allocator is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the number of idle buffers currently retained.
     *
     * @return the idle buffer count
     */
    public int idleCount() {
        return idleCount.get();
    }

    /**
     * Returns the maximum number of idle buffers retained.
     *
     * @return the maximum idle buffer count
     */
    public int maxIdle() {
        return maxIdle;
    }

    /**
     * Clears all idle buffers and closes this allocator.
     */
    @Override
    public void close() {
        closed = true;
        idleBuffers.clear();
        idleCount.set(0);
    }

    /**
     * Returns a reusable buffer to this allocator.
     *
     * @param buffer the buffer to release
     */
    void release(java.nio.ByteBuffer buffer) {
        if (buffer == null || closed || buffer.capacity() != bufferSize || buffer.isDirect() != direct
                || buffer.isReadOnly()) {
            return;
        }
        buffer.clear();
        int count = idleCount.incrementAndGet();
        if (count <= maxIdle) {
            idleBuffers.offer(buffer);
        } else {
            idleCount.decrementAndGet();
        }
    }

    /**
     * Allocates a new NIO buffer.
     *
     * @param capacity the capacity to allocate
     * @return the allocated NIO buffer
     */
    private java.nio.ByteBuffer newByteBuffer(int capacity) {
        return direct ? java.nio.ByteBuffer.allocateDirect(capacity) : java.nio.ByteBuffer.allocate(capacity);
    }

    /**
     * Verifies that this allocator is still open.
     */
    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("NioBufferAllocator has been closed");
        }
    }

}

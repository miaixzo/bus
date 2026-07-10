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

/**
 * Represents a leased NIO byte buffer that can be returned to its owning allocator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class NioBuffer implements AutoCloseable {

    /**
     * Allocator that owns this lease, or {@code null} when the buffer is one-off.
     */
    private final NioBufferAllocator allocator;

    /**
     * NIO buffer exposed by this lease.
     */
    private final java.nio.ByteBuffer buffer;

    /**
     * Whether this lease should be returned to the allocator.
     */
    private final boolean reusable;

    /**
     * Whether this lease has already been closed.
     */
    private boolean closed;

    /**
     * Creates a leased NIO buffer.
     *
     * @param allocator the owner allocator, or {@code null} for one-off buffers
     * @param buffer    the leased NIO buffer
     * @param reusable  whether the buffer should be returned to the allocator
     */
    NioBuffer(NioBufferAllocator allocator, java.nio.ByteBuffer buffer, boolean reusable) {
        this.allocator = allocator;
        this.buffer = buffer;
        this.reusable = reusable;
    }

    /**
     * Returns the leased NIO buffer.
     *
     * @return the leased NIO buffer
     * @throws IllegalStateException if this lease is already closed
     */
    public java.nio.ByteBuffer buffer() {
        assertOpen();
        return buffer;
    }

    /**
     * Returns the buffer capacity.
     *
     * @return the buffer capacity in bytes
     * @throws IllegalStateException if this lease is already closed
     */
    public int capacity() {
        assertOpen();
        return buffer.capacity();
    }

    /**
     * Returns whether the buffer is direct.
     *
     * @return {@code true} if the underlying buffer is direct
     * @throws IllegalStateException if this lease is already closed
     */
    public boolean isDirect() {
        assertOpen();
        return buffer.isDirect();
    }

    /**
     * Clears the underlying buffer.
     *
     * @return this lease
     * @throws IllegalStateException if this lease is already closed
     */
    public NioBuffer clear() {
        assertOpen();
        buffer.clear();
        return this;
    }

    /**
     * Flips the underlying buffer.
     *
     * @return this lease
     * @throws IllegalStateException if this lease is already closed
     */
    public NioBuffer flip() {
        assertOpen();
        buffer.flip();
        return this;
    }

    /**
     * Returns this buffer to the owner allocator when it is reusable.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (reusable && allocator != null) {
            allocator.release(buffer);
        }
    }

    /**
     * Verifies that this lease is still open.
     */
    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("NioBuffer has been closed");
        }
    }

}

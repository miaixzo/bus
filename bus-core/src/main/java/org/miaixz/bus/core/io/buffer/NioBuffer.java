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
     * Returns the current position of the underlying buffer.
     *
     * @return the current position
     * @throws IllegalStateException if this lease is already closed
     */
    public int position() {
        assertOpen();
        return buffer.position();
    }

    /**
     * Returns the current limit of the underlying buffer.
     *
     * @return the current limit
     * @throws IllegalStateException if this lease is already closed
     */
    public int limit() {
        assertOpen();
        return buffer.limit();
    }

    /**
     * Returns the number of bytes between the current position and limit.
     *
     * @return the number of remaining bytes
     * @throws IllegalStateException if this lease is already closed
     */
    public int remaining() {
        assertOpen();
        return buffer.remaining();
    }

    /**
     * Returns whether bytes remain between the current position and limit.
     *
     * @return {@code true} if bytes remain
     * @throws IllegalStateException if this lease is already closed
     */
    public boolean hasRemaining() {
        assertOpen();
        return buffer.hasRemaining();
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
     * Reads bytes from {@code source} into this NIO buffer.
     *
     * @param source the core buffer to drain
     * @return the number of bytes read, or {@code -1} if {@code source} is empty
     * @throws IllegalArgumentException if {@code source} is null
     * @throws IllegalStateException    if this lease is already closed
     */
    public int readFrom(final Buffer source) {
        return readFrom(source, buffer.remaining());
    }

    /**
     * Reads up to {@code maxBytes} from {@code source} into this NIO buffer.
     *
     * @param source   the core buffer to drain
     * @param maxBytes the maximum number of bytes to read
     * @return the number of bytes read, or {@code -1} if {@code source} is empty
     * @throws IllegalArgumentException if {@code source} is null or {@code maxBytes} is negative
     * @throws IllegalStateException    if this lease is already closed
     */
    public int readFrom(final Buffer source, final int maxBytes) {
        assertOpen();
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        return source.readTo(buffer, Math.min(maxBytes, buffer.remaining()));
    }

    /**
     * Writes all remaining bytes from this NIO buffer to {@code target}.
     *
     * @param target the destination core buffer
     * @return the number of bytes written
     * @throws IllegalArgumentException if {@code target} is null
     * @throws IllegalStateException    if this lease is already closed
     */
    public int writeTo(final Buffer target) {
        return writeTo(target, buffer.remaining());
    }

    /**
     * Writes up to {@code maxBytes} from this NIO buffer to {@code target}.
     *
     * @param target   the destination core buffer
     * @param maxBytes the maximum number of bytes to write
     * @return the number of bytes written
     * @throws IllegalArgumentException if {@code target} is null or {@code maxBytes} is negative
     * @throws IllegalStateException    if this lease is already closed
     */
    public int writeTo(final Buffer target, final int maxBytes) {
        assertOpen();
        if (target == null) {
            throw new IllegalArgumentException("target == null");
        }
        if (maxBytes < 0) {
            throw new IllegalArgumentException("maxBytes < 0: " + maxBytes);
        }
        final int byteCount = Math.min(maxBytes, buffer.remaining());
        if (byteCount == 0) {
            return 0;
        }
        final int originalLimit = buffer.limit();
        buffer.limit(buffer.position() + byteCount);
        try {
            target.write(buffer);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        } finally {
            buffer.limit(originalLimit);
        }
        return byteCount;
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

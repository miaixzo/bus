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

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * A leased {@link ByteBuffer} slice allocated from a {@link SlabBuffer}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SliceBuffer {

    /**
     * Parent slab, or {@code null} for wrapped external buffers.
     */
    private final SlabBuffer slabBuffer;

    /**
     * Guard that prevents duplicate release.
     */
    private final Semaphore releaseGuard = new Semaphore(1);

    /**
     * The leased byte buffer.
     */
    private ByteBuffer buffer;

    /**
     * Start offset in the parent slab.
     */
    private int parentPosition;

    /**
     * End offset in the parent slab.
     */
    private int parentLimit;

    /**
     * Capacity represented by the parent offsets.
     */
    private int capacity;

    /**
     * Creates a slice buffer.
     *
     * @param slabBuffer     the parent slab
     * @param buffer         the leased byte buffer
     * @param parentPosition the start offset in the parent slab
     * @param parentLimit    the end offset in the parent slab
     */
    SliceBuffer(SlabBuffer slabBuffer, ByteBuffer buffer, int parentPosition, int parentLimit) {
        this.slabBuffer = slabBuffer;
        this.buffer = buffer;
        this.parentPosition = parentPosition;
        this.parentLimit = parentLimit;
        updateCapacity();
    }

    /**
     * Wraps an external {@link ByteBuffer}.
     *
     * @param buffer the external buffer
     * @return a slice buffer wrapper
     */
    public static SliceBuffer wrap(ByteBuffer buffer) {
        return new SliceBuffer(null, buffer, 0, 0);
    }

    /**
     * Returns the parent slab start offset.
     *
     * @return the parent slab start offset
     */
    int getParentPosition() {
        return parentPosition;
    }

    /**
     * Updates the parent slab start offset.
     *
     * @param parentPosition the new start offset
     */
    void setParentPosition(int parentPosition) {
        this.parentPosition = parentPosition;
        updateCapacity();
    }

    /**
     * Returns the parent slab end offset.
     *
     * @return the parent slab end offset
     */
    int getParentLimit() {
        return parentLimit;
    }

    /**
     * Updates the parent slab end offset.
     *
     * @param parentLimit the new end offset
     */
    void setParentLimit(int parentLimit) {
        this.parentLimit = parentLimit;
        updateCapacity();
    }

    /**
     * Recomputes the capacity from parent offsets.
     */
    private void updateCapacity() {
        capacity = parentLimit - parentPosition;
    }

    /**
     * Returns the represented capacity.
     *
     * @return the represented capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the leased {@link ByteBuffer}.
     *
     * @return the leased byte buffer
     */
    public ByteBuffer buffer() {
        return buffer;
    }

    /**
     * Replaces the leased buffer and marks this lease as reusable.
     *
     * @param buffer the new leased buffer
     */
    void buffer(ByteBuffer buffer) {
        this.buffer = buffer;
        releaseGuard.release();
    }

    /**
     * Returns this slice to its parent slab.
     */
    public void release() {
        if (releaseGuard.tryAcquire()) {
            if (slabBuffer != null) {
                slabBuffer.release(this);
            }
            return;
        }
        throw new UnsupportedOperationException("buffer has already been released");
    }

    /**
     * Returns a readable description of this slice buffer.
     *
     * @return the buffer description
     */
    @Override
    public String toString() {
        return "SliceBuffer{" + "parentPosition=" + parentPosition + ", parentLimit=" + parentLimit + '}';
    }

}

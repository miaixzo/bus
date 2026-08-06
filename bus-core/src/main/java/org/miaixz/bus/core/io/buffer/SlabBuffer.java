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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fixed-size slab that leases reusable {@link SliceBuffer} views from one backing {@link ByteBuffer}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SlabBuffer {

    /**
     * Lock that protects free-list mutations.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Physical slab buffer.
     */
    private final ByteBuffer buffer;

    /**
     * Slices waiting to be merged back into the free list.
     */
    private final ConcurrentLinkedQueue<SliceBuffer> releasedSlices = new ConcurrentLinkedQueue<>();

    /**
     * Free slice ranges in this slab.
     */
    private final List<SliceBuffer> availableSlices;

    /**
     * Whether this slab was idle in the last compaction cycle.
     */
    private boolean idle = true;

    /**
     * Creates a slab buffer.
     *
     * @param size   the slab size
     * @param direct whether to allocate direct memory
     */
    SlabBuffer(int size, boolean direct) {
        availableSlices = new LinkedList<>();
        buffer = allocate(size, direct);
        availableSlices.add(new SliceBuffer(this, null, buffer.position(), buffer.limit()));
    }

    /**
     * Allocates a physical buffer.
     *
     * @param size   the buffer size
     * @param direct whether to allocate a direct buffer
     * @return the allocated buffer
     */
    private ByteBuffer allocate(int size, boolean direct) {
        return direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
    }

    /**
     * Allocates a slice from this slab.
     *
     * @param size the requested size
     * @return a slice buffer
     */
    public SliceBuffer allocate(final int size) {
        if (size == 0) {
            throw new UnsupportedOperationException("cannot allocate zero bytes");
        }
        SliceBuffer sliceBuffer = allocateFromSlab(size);
        return sliceBuffer == null ? new SliceBuffer(null, allocate(size, false), 0, 0) : sliceBuffer;
    }

    /**
     * Allocates a slice from the slab free list.
     *
     * @param size the requested size
     * @return a slice buffer, or {@code null} if this slab cannot satisfy the request
     */
    private SliceBuffer allocateFromSlab(final int size) {
        idle = false;
        SliceBuffer releasedSlice = releasedSlices.poll();
        if (releasedSlice != null && releasedSlice.getCapacity() >= size) {
            releasedSlice.buffer().clear();
            releasedSlice.buffer(releasedSlice.buffer());
            return releasedSlice;
        }
        lock.lock();
        try {
            if (releasedSlice != null) {
                release(releasedSlice, false);
                while ((releasedSlice = releasedSlices.poll()) != null) {
                    if (releasedSlice.getCapacity() >= size) {
                        releasedSlice.buffer().clear();
                        releasedSlice.buffer(releasedSlice.buffer());
                        return releasedSlice;
                    }
                    release(releasedSlice, false);
                }
            }

            int count = availableSlices.size();
            if (count == 1) {
                return fastAllocate(size);
            }
            if (count > 1) {
                return slowAllocate(size);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Allocates from the only available free range.
     *
     * @param size the requested size
     * @return a slice buffer, or {@code null}
     */
    private SliceBuffer fastAllocate(int size) {
        SliceBuffer freeSlice = availableSlices.get(0);
        SliceBuffer allocatedSlice = allocate(size, freeSlice);
        if (freeSlice == allocatedSlice) {
            availableSlices.clear();
        }
        return allocatedSlice;
    }

    /**
     * Allocates by scanning multiple free ranges.
     *
     * @param size the requested size
     * @return a slice buffer, or {@code null}
     */
    private SliceBuffer slowAllocate(int size) {
        Iterator<SliceBuffer> iterator = availableSlices.listIterator(0);
        while (iterator.hasNext()) {
            SliceBuffer freeSlice = iterator.next();
            SliceBuffer allocatedSlice = allocate(size, freeSlice);
            if (freeSlice == allocatedSlice) {
                iterator.remove();
            }
            if (allocatedSlice != null) {
                return allocatedSlice;
            }
        }
        return null;
    }

    /**
     * Allocates a slice range from a free range.
     *
     * @param size      the requested size
     * @param freeSlice the free range
     * @return a slice buffer, or {@code null}
     */
    private SliceBuffer allocate(int size, SliceBuffer freeSlice) {
        int capacity = freeSlice.getCapacity();
        if (capacity < size) {
            return null;
        }
        SliceBuffer allocatedSlice;
        if (capacity == size) {
            buffer.limit(freeSlice.getParentLimit());
            buffer.position(freeSlice.getParentPosition());
            freeSlice.buffer(buffer.slice());
            allocatedSlice = freeSlice;
        } else {
            buffer.limit(freeSlice.getParentPosition() + size);
            buffer.position(freeSlice.getParentPosition());
            allocatedSlice = new SliceBuffer(this, buffer.slice(), buffer.position(), buffer.limit());
            freeSlice.setParentPosition(buffer.limit());
        }
        if (allocatedSlice.buffer().remaining() != size) {
            throw new IllegalStateException("allocate " + size + ", buffer:" + allocatedSlice);
        }
        return allocatedSlice;
    }

    /**
     * Queues a slice for recycling.
     *
     * @param releasedSlice the slice to release
     */
    void release(SliceBuffer releasedSlice) {
        releasedSlices.offer(releasedSlice);
    }

    /**
     * Compacts queued releases when this slab remains idle across compaction cycles.
     */
    void compact() {
        if (!idle) {
            idle = true;
            return;
        }
        if (!releasedSlices.isEmpty() && lock.tryLock()) {
            try {
                SliceBuffer releasedSlice;
                while ((releasedSlice = releasedSlices.poll()) != null) {
                    release(releasedSlice, false);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Merges a released slice back into the free list.
     *
     * @param releasedSlice the slice to merge
     * @param queue         whether to enqueue instead of merging immediately
     */
    private void release(SliceBuffer releasedSlice, boolean queue) {
        if (queue) {
            release(releasedSlice);
            return;
        }
        ListIterator<SliceBuffer> iterator = availableSlices.listIterator(0);
        while (iterator.hasNext()) {
            SliceBuffer freeSlice = iterator.next();
            if (freeSlice.getParentPosition() == releasedSlice.getParentLimit()) {
                freeSlice.setParentPosition(releasedSlice.getParentPosition());
                return;
            }
            if (freeSlice.getParentLimit() == releasedSlice.getParentPosition()) {
                freeSlice.setParentLimit(releasedSlice.getParentLimit());
                if (iterator.hasNext()) {
                    SliceBuffer next = iterator.next();
                    if (next.getParentPosition() == freeSlice.getParentLimit()) {
                        freeSlice.setParentLimit(next.getParentLimit());
                        iterator.remove();
                    } else if (next.getParentPosition() < freeSlice.getParentLimit()) {
                        throw new IllegalStateException("Buffer order inconsistency detected");
                    }
                }
                return;
            }
            if (freeSlice.getParentPosition() > releasedSlice.getParentLimit()) {
                iterator.previous();
                iterator.add(releasedSlice);
                return;
            }
        }
        iterator.add(releasedSlice);
    }

    /**
     * Releases this slab.
     */
    void release() {
        buffer.clear();
    }

    /**
     * Returns a readable description of this slab.
     *
     * @return the slab description
     */
    @Override
    public String toString() {
        return "SlabBuffer{" + "availableSlices=" + availableSlices + ", releasedSlices=" + releasedSlices + '}';
    }

}

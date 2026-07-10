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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allocates {@link SlabBuffer} instances with round-robin slab selection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SlabBufferAllocator {

    /**
     * Background compactor for slab allocators.
     */
    private static final ScheduledThreadPoolExecutor SLAB_BUFFER_ALLOCATOR_COMPACTOR = new ScheduledThreadPoolExecutor(
            1, runnable -> {
                Thread thread = new Thread(runnable, "SlabBufferAllocatorCompact");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * Shared fallback allocator that creates standalone heap slices.
     */
    public static final SlabBufferAllocator DEFAULT_ALLOCATOR = new SlabBufferAllocator(0, 1, false);

    /**
     * Round-robin cursor.
     */
    private final AtomicInteger cursor = new AtomicInteger();

    /**
     * Slabs owned by this allocator.
     */
    private SlabBuffer[] slabBuffers;

    /**
     * Whether this allocator accepts allocations.
     */
    private volatile boolean enabled = true;

    /**
     * Background compaction future.
     */
    private final ScheduledFuture<?> future;

    /**
     * Creates a direct slab allocator.
     *
     * @param slabSize  the size of each slab
     * @param slabCount the number of slabs
     */
    public SlabBufferAllocator(final int slabSize, final int slabCount) {
        this(slabSize, slabCount, true);
    }

    /**
     * Creates a slab allocator.
     *
     * @param slabSize  the size of each slab
     * @param slabCount the number of slabs
     * @param isDirect  whether slabs should use direct memory
     */
    public SlabBufferAllocator(final int slabSize, final int slabCount, final boolean isDirect) {
        slabBuffers = new SlabBuffer[slabCount];
        for (int i = 0; i < slabCount; i++) {
            slabBuffers[i] = new SlabBuffer(slabSize, isDirect);
        }
        future = SLAB_BUFFER_ALLOCATOR_COMPACTOR
                .scheduleWithFixedDelay(this::compact, 500, 1000, TimeUnit.MILLISECONDS);
        if (slabCount == 0 || slabSize == 0) {
            future.cancel(false);
        }
    }

    /**
     * Allocates a slab from this allocator.
     *
     * @return the selected slab
     */
    public SlabBuffer allocate() {
        if (!enabled) {
            throw new IllegalStateException("Buffer allocator is disabled");
        }
        return slabBuffers[(cursor.getAndIncrement() & Integer.MAX_VALUE) % slabBuffers.length];
    }

    /**
     * Disables this allocator and releases slabs on the next compaction tick.
     */
    public void release() {
        enabled = false;
    }

    /**
     * Performs background compaction.
     */
    private void compact() {
        if (enabled) {
            for (SlabBuffer slabBuffer : slabBuffers) {
                slabBuffer.compact();
            }
            return;
        }
        if (slabBuffers != null) {
            for (SlabBuffer slabBuffer : slabBuffers) {
                slabBuffer.release();
            }
            slabBuffers = null;
        }
        future.cancel(false);
    }

}

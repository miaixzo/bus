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
package org.miaixz.bus.fabric.network;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pooled backing storage for network hot paths.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Backing {

    /**
     * Maximum supported buffer capacity.
     */
    private static final int MAX_CAPACITY = 16_777_216;

    /**
     * Maximum retained pooled buffers.
     */
    private static final int MAX_POOLED = 64;

    /**
     * Buffer capacity.
     */
    private final int capacity;

    /**
     * Release flag.
     */
    private final AtomicBoolean released;

    /**
     * Internal mutable buffer.
     */
    private ByteBuffer buffer;

    /**
     * Creates a backing storage holder.
     *
     * @param buffer buffer
     */
    private Backing(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new ValidateException("Backing buffer must not be null");
        }
        this.capacity = buffer.capacity();
        this.buffer = buffer;
        this.released = new AtomicBoolean();
    }

    /**
     * Allocates backing storage.
     *
     * @param capacity capacity
     * @return backing storage
     */
    public static Backing allocate(final int capacity) {
        validateCapacity(capacity);
        final ByteBuffer pooled = acquire(capacity);
        if (pooled != null) {
            return new Backing(pooled);
        }
        try {
            return new Backing(ByteBuffer.allocate(capacity));
        } catch (final OutOfMemoryError e) {
            throw new InternalException("Unable to allocate backing buffer", e);
        }
    }

    /**
     * Returns a read-only buffer view.
     *
     * @return read-only buffer
     */
    public ByteBuffer buffer() {
        return current().asReadOnlyBuffer();
    }

    /**
     * Returns the buffer capacity.
     *
     * @return capacity
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Clears this buffer.
     *
     * @return this buffer
     */
    public Backing clear() {
        current().clear();
        return this;
    }

    /**
     * Flips this buffer to read mode.
     *
     * @return this buffer
     */
    public Backing flip() {
        current().flip();
        return this;
    }

    /**
     * Releases this buffer back to the pool.
     */
    public void release() {
        if (released.compareAndSet(false, true)) {
            final ByteBuffer current = buffer;
            buffer = null;
            if (current != null) {
                recycle(current);
            }
        }
    }

    /**
     * Acquires a pooled buffer.
     *
     * @param capacity capacity
     * @return buffer or null
     */
    private static ByteBuffer acquire(final int capacity) {
        final Queue<ByteBuffer> queue = pools().get(capacity);
        if (queue == null) {
            return null;
        }
        final ByteBuffer pooled = queue.poll();
        if (pooled != null) {
            pooledCount().decrementAndGet();
            pooled.clear();
        }
        return pooled;
    }

    /**
     * Recycles a buffer.
     *
     * @param buffer buffer
     */
    private static void recycle(final ByteBuffer buffer) {
        try {
            buffer.clear();
            final int pooled = pooledCount().incrementAndGet();
            if (pooled <= MAX_POOLED) {
                pools().computeIfAbsent(buffer.capacity(), ignored -> new ConcurrentLinkedQueue<>()).offer(buffer);
            } else {
                pooledCount().decrementAndGet();
            }
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to recycle backing buffer", e);
        }
    }

    /**
     * Returns pooled buffers grouped by capacity.
     *
     * @return buffer pools
     */
    private static ConcurrentHashMap<Integer, Queue<ByteBuffer>> pools() {
        return Instances.get(Backing.class.getName() + ".pools", ConcurrentHashMap::new);
    }

    /**
     * Returns the global pooled buffer count.
     *
     * @return pooled count
     */
    private static AtomicInteger pooledCount() {
        return Instances.get(Backing.class.getName() + ".pooled", AtomicInteger::new);
    }

    /**
     * Returns the current mutable buffer.
     *
     * @return buffer
     */
    private ByteBuffer current() {
        final ByteBuffer current = buffer;
        if (released.get() || current == null) {
            throw new StatefulException("Backing buffer has been released");
        }
        return current;
    }

    /**
     * Validates capacity.
     *
     * @param capacity capacity
     */
    private static void validateCapacity(final int capacity) {
        if (capacity <= 0 || capacity > MAX_CAPACITY) {
            throw new ValidateException("Backing buffer capacity out of range");
        }
    }

}

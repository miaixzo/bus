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
package org.miaixz.bus.fabric.registry.connection;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.miaixz.bus.fabric.network.Destination;

/**
 * FIFO ownership boundary for connection-pool waiters.
 * <p>
 * The owning {@link ConnectionPool} supplies the existing synchronization boundary; this class deliberately adds no
 * lock and therefore does not change contention characteristics.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class PoolWaiters implements Iterable<PoolWaiters.Waiter> {

    /**
     * Ordered waiter queue.
     */
    private final ArrayDeque<Waiter> queue = new ArrayDeque<>();

    /**
     * Adds one waiter at the tail.
     *
     * @param waiter waiter to add
     */
    void addLast(final Waiter waiter) {
        queue.addLast(waiter);
    }

    /**
     * Removes one waiter.
     *
     * @param waiter waiter to remove
     * @return true when present
     */
    boolean remove(final Waiter waiter) {
        return queue.remove(waiter);
    }

    /**
     * Returns the oldest waiter.
     *
     * @return queue head or {@code null}
     */
    Waiter peekFirst() {
        return queue.peekFirst();
    }

    /**
     * Returns whether the queue is empty.
     *
     * @return true when empty
     */
    boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns queue size.
     *
     * @return waiter count
     */
    int size() {
        return queue.size();
    }

    /**
     * Clears all waiter references.
     */
    void clear() {
        queue.clear();
    }

    /**
     * Returns the FIFO iterator. The caller must hold the pool coordination lock.
     *
     * @return waiter iterator
     */
    @Override
    public Iterator<Waiter> iterator() {
        return queue.iterator();
    }

    /**
     * One blocked acquisition request.
     */
    static final class Waiter {

        /**
         * Requested destination.
         */
        final Destination destination;

        /**
         * Exact thread to unpark.
         */
        final Thread thread;

        /**
         * Queue membership guard.
         */
        boolean queued;

        /**
         * Monotonic queue-entry time, or zero before admission.
         */
        long queuedAtNanos;

        /**
         * Lease transferred directly by a releasing HTTP/1.1 owner, or {@code null}.
         */
        ConnectionLease handoff;

        /**
         * Whether the owning acquisition scope was cancelled.
         */
        boolean cancelled;

        /**
         * Captures the current thread and requested destination.
         *
         * @param destination requested destination
         */
        Waiter(final Destination destination) {
            this.destination = destination;
            this.thread = Thread.currentThread();
        }

    }

}

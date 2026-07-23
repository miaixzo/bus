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

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;

/**
 * Independently locked connection pool partition for one destination.
 *
 * <p>
 * HTTP/1 idle entries and active multiplex connections are physically separated. The newest HTTP/1 connection is
 * borrowed first, while multiplex selection chooses the healthy connection with the greatest stream capacity.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class ConnectionBucket {

    /**
     * Immutable destination key shared by every connection in this partition.
     */
    private final Destination destination;

    /**
     * Sole lock protecting this destination partition's mutable collections and counters.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * HTTP/1 idle entries ordered from oldest at the head to newest at the tail.
     */
    private final ArrayDeque<PooledConnection> idle = new ArrayDeque<>();

    /**
     * Registered multiplex-capable physical connections eligible for capacity selection.
     */
    private final ArrayList<Connection> multiplex = new ArrayList<>();

    /**
     * Threads waiting for destination capacity in first-in, first-out order.
     */
    private final ArrayDeque<Thread> waiters = new ArrayDeque<>();

    /**
     * Non-negative count of physical connections owned by this bucket.
     */
    private int physicalCount;

    /**
     * Earliest known HTTP/1 idle expiration, or null when the idle deque is empty.
     */
    private Instant earliestExpiry;

    /**
     * Creates an empty partition for one destination.
     *
     * @param destination non-null immutable destination key
     */
    ConnectionBucket(final Destination destination) {
        if (destination == null) {
            throw new ValidateException("Connection bucket destination must not be null");
        }
        this.destination = destination;
    }

    /**
     * Returns this partition's destination key.
     *
     * @return immutable destination
     */
    Destination destination() {
        return destination;
    }

    /**
     * Adds a newly idle non-multiplex connection as the newest candidate and wakes the oldest waiter.
     *
     * @param entry     non-null pooled entry whose connection is not multiplex-capable
     * @param expiresAt non-null computed idle-expiration instant
     */
    void offerIdle(final PooledConnection entry, final Instant expiresAt) {
        if (entry == null || expiresAt == null || entry.connection().multiplex()) {
            throw new ValidateException("Invalid HTTP/1 idle entry");
        }
        lock.lock();
        try {
            idle.addLast(entry);
            if (earliestExpiry == null || expiresAt.isBefore(earliestExpiry)) {
                earliestExpiry = expiresAt;
            }
            signalHead();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes and returns the newest healthy HTTP/1 idle entry, closing unhealthy entries encountered first.
     *
     * @return newest healthy entry, or null when no idle candidate remains
     */
    PooledConnection pollIdle() {
        lock.lock();
        try {
            PooledConnection entry = idle.pollLast();
            while (entry != null && !entry.connection().healthy()) {
                entry.connection().close();
                physicalCount = Math.max(0, physicalCount - 1);
                entry = idle.pollLast();
            }
            if (idle.isEmpty()) {
                earliestExpiry = null;
            }
            return entry;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Registers a multiplex-capable physical connection if an equal candidate is not already present.
     *
     * @param connection non-null multiplex-capable connection
     */
    void addMultiplex(final Connection connection) {
        if (connection == null || !connection.multiplex()) {
            throw new ValidateException("Connection bucket requires a multiplex connection");
        }
        lock.lock();
        try {
            if (!multiplex.contains(connection)) {
                multiplex.add(connection);
                signalHead();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the first multiplex candidate equal to the supplied connection.
     *
     * @param connection connection to remove, or null when no candidate should match
     */
    void removeMultiplex(final Connection connection) {
        lock.lock();
        try {
            multiplex.remove(connection);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Selects the healthy non-draining multiplex candidate with the greatest positive capacity.
     *
     * @return highest-capacity candidate, or null when none reports available capacity
     */
    Connection bestMultiplex() {
        lock.lock();
        try {
            Connection best = null;
            int capacity = 0;
            for (int index = multiplex.size() - 1; index >= 0; index--) {
                final Connection candidate = multiplex.get(index);
                if (!candidate.healthy() || candidate.draining()) {
                    multiplex.remove(index);
                    continue;
                }
                final int available = Math.max(0, candidate.capacity());
                if (available > capacity) {
                    capacity = available;
                    best = candidate;
                }
            }
            return best;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Increments physical connection ownership under the partition lock.
     *
     * @return incremented physical connection count
     */
    int incrementPhysical() {
        lock.lock();
        try {
            return ++physicalCount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrements physical ownership without going below zero and wakes the oldest waiter.
     *
     * @return resulting non-negative physical connection count
     */
    int decrementPhysical() {
        lock.lock();
        try {
            physicalCount = Math.max(0, physicalCount - 1);
            signalHead();
            return physicalCount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current physical connection count under the partition lock.
     *
     * @return non-negative owned connection count
     */
    int physicalCount() {
        lock.lock();
        try {
            return physicalCount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the earliest known idle expiration.
     *
     * @return earliest recorded expiration, or null when no idle entry remains
     */
    Instant earliestExpiry() {
        lock.lock();
        try {
            return earliestExpiry;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Appends a thread to the FIFO waiter queue unless an equal reference is already queued.
     *
     * @param thread waiter thread to register
     */
    void addWaiter(final Thread thread) {
        lock.lock();
        try {
            if (!waiters.contains(thread)) {
                waiters.addLast(thread);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the first queued waiter equal to the supplied thread.
     *
     * @param thread waiter thread to remove
     */
    void removeWaiter(final Thread thread) {
        lock.lock();
        try {
            waiters.remove(thread);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unparks only the oldest destination waiter while the partition lock is held.
     */
    private void signalHead() {
        final Thread thread = waiters.peekFirst();
        if (thread != null) {
            java.util.concurrent.locks.LockSupport.unpark(thread);
        }
    }

}

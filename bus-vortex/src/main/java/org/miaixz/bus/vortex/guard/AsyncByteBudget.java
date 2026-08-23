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
package org.miaixz.bus.vortex.guard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Provides a process-wide, asynchronous weighted semaphore measured in logical body bytes.
 * <p>
 * Requests that do not fit are queued in first-in-first-out order and completed when an earlier lease releases enough
 * capacity. Waiting, cancellation and delivery races do not block Reactor event-loop threads and cannot leak capacity.
 * A caller must retain the returned {@link Lease} for as long as an equivalent in-memory representation remains live.
 *
 * @author Kimi Liu
 */
public class AsyncByteBudget implements AutoCloseable {

    /**
     * Maximum logical bytes that active leases may own together.
     */
    private final long limitBytes;
    /**
     * Protects usage and waiter-queue transitions.
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * FIFO queue of acquisitions that could not be satisfied immediately.
     */
    private final ArrayDeque<Waiter> waiters = new ArrayDeque<>();
    /**
     * Logical bytes currently owned by live leases.
     */
    private long usedBytes;
    /**
     * Whether new acquisitions have been permanently disabled.
     */
    private boolean closed;

    /**
     * Creates an empty byte budget.
     *
     * @param limitBytes maximum logical bytes that may be owned concurrently
     */
    public AsyncByteBudget(long limitBytes) {
        if (limitBytes <= 0) {
            throw new IllegalArgumentException("limitBytes must be positive");
        }
        this.limitBytes = limitBytes;
    }

    /**
     * Acquires an exact number of logical bytes asynchronously.
     * <p>
     * The returned publisher is cold. Acquisition starts on subscription; a request that currently does not fit waits
     * in the FIFO queue until capacity is returned or its subscription is cancelled.
     *
     * @param bytes exact logical bytes to own
     * @return a cold publisher that emits one ownership lease
     */
    public Mono<Lease> acquire(long bytes) {
        if (bytes < 0 || bytes > this.limitBytes) {
            return Mono.error(new IllegalArgumentException("requested bytes exceed the budget limit"));
        }
        if (bytes == 0) {
            return Mono.just(new Lease(this, 0));
        }
        return Mono.defer(() -> {
            Waiter waiter = new Waiter(bytes);
            enqueue(waiter);
            return waiter.result.asMono().doOnCancel(() -> cancel(waiter));
        });
    }

    /**
     * Adds a waiter or grants its lease immediately while holding the state lock.
     *
     * @param waiter pending acquisition to enqueue or grant
     */
    private void enqueue(Waiter waiter) {
        long bytes = waiter.bytes;
        Lease immediate = null;
        this.lock.lock();
        try {
            if (this.closed) {
                waiter.result.tryEmitError(new IllegalStateException("byte budget is closed"));
                return;
            }
            if (this.waiters.isEmpty() && bytes <= this.limitBytes - this.usedBytes) {
                this.usedBytes += bytes;
                immediate = new Lease(this, bytes);
                waiter.lease = immediate;
            } else {
                this.waiters.addLast(waiter);
            }
        } finally {
            this.lock.unlock();
        }
        if (immediate != null) {
            deliver(waiter, immediate);
        }
    }

    /**
     * Removes a cancelled waiter or returns a concurrently granted lease.
     *
     * @param waiter acquisition cancelled by its subscriber
     */
    private void cancel(Waiter waiter) {
        waiter.cancelled.set(true);
        Lease lease = waiter.lease;
        if (lease != null) {
            lease.close();
            return;
        }
        this.lock.lock();
        try {
            this.waiters.remove(waiter);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Returns capacity and grants as many FIFO waiters as the new capacity permits.
     *
     * @param bytes logical bytes returned by a closed lease
     */
    private void release(long bytes) {
        List<Waiter> ready = new ArrayList<>();
        this.lock.lock();
        try {
            this.usedBytes -= bytes;
            while (!this.waiters.isEmpty()) {
                Waiter waiter = this.waiters.peekFirst();
                if (waiter.cancelled.get()) {
                    this.waiters.removeFirst();
                    continue;
                }
                if (waiter.bytes > this.limitBytes - this.usedBytes) {
                    break;
                }
                this.waiters.removeFirst();
                this.usedBytes += waiter.bytes;
                waiter.lease = new Lease(this, waiter.bytes);
                ready.add(waiter);
            }
        } finally {
            this.lock.unlock();
        }
        ready.forEach(waiter -> deliver(waiter, waiter.lease));
    }

    /**
     * Delivers a granted lease, returning it when cancellation won the delivery race.
     *
     * @param waiter pending acquisition receiving the result
     * @param lease  capacity granted to the waiter
     */
    private static void deliver(Waiter waiter, Lease lease) {
        if (waiter.cancelled.get()) {
            lease.close();
        } else {
            Sinks.EmitResult emitted = waiter.result.tryEmitValue(lease);
            if (emitted.isFailure()) {
                lease.close();
            }
        }
    }

    /**
     * Returns the immutable budget limit.
     *
     * @return immutable maximum concurrent logical bytes
     */
    public long limitBytes() {
        return this.limitBytes;
    }

    /**
     * Returns a point-in-time count of owned logical bytes.
     *
     * @return a point-in-time count of logical bytes owned by active leases
     */
    public long usedBytes() {
        this.lock.lock();
        try {
            return this.usedBytes;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Returns a point-in-time count of queued acquisitions.
     *
     * @return a point-in-time count of acquisitions waiting for capacity
     */
    public int waitingRequests() {
        this.lock.lock();
        try {
            return this.waiters.size();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Rejects future acquisitions and fails all queued waiters. Existing leases remain valid and may close normally.
     */
    @Override
    public void close() {
        List<Waiter> pending;
        this.lock.lock();
        try {
            this.closed = true;
            pending = new ArrayList<>(this.waiters);
            this.waiters.clear();
        } finally {
            this.lock.unlock();
        }
        pending.forEach(waiter -> waiter.result.tryEmitError(new IllegalStateException("byte budget is closed")));
    }

    /**
     * Mutable state for one pending acquisition.
     */
    private static final class Waiter {

        /**
         * Exact logical capacity requested by this waiter.
         */
        private final long bytes;

        /**
         * One-shot result used to deliver either a lease or shutdown failure.
         */
        private final Sinks.One<Lease> result = Sinks.one();

        /**
         * Records cancellation across queue and delivery races.
         */
        private final AtomicBoolean cancelled = new AtomicBoolean();

        /**
         * Lease granted under the state lock and delivered immediately afterward.
         */
        private volatile Lease lease;

        /**
         * Creates one pending acquisition request.
         *
         * @param bytes exact logical capacity requested
         */
        private Waiter(long bytes) {
            this.bytes = bytes;
        }
    }

    /**
     * Idempotent ownership token for an exact number of logical bytes.
     */
    public static class Lease implements AutoCloseable {

        /**
         * Budget that owns and reclaims this lease's capacity.
         */
        private final AsyncByteBudget budget;

        /**
         * Exact logical bytes charged to the budget.
         */
        private long bytes;

        /**
         * Ensures capacity is returned only once.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates an ownership token for capacity already charged to the budget.
         *
         * @param budget budget that granted the capacity
         * @param bytes  exact logical bytes charged
         */
        public Lease(AsyncByteBudget budget, long bytes) {
            this.budget = budget;
            this.bytes = bytes;
        }

        /**
         * Returns the capacity owned by this lease.
         *
         * @return exact logical bytes owned by this lease
         */
        public synchronized long bytes() {
            return this.bytes;
        }

        /**
         * Returns capacity that is no longer needed while retaining ownership of the remaining bytes.
         * <p>
         * Unknown-length bodies reserve their maximum permitted size before subscribing to the source. Once the actual
         * size is known, this operation releases the unused portion without opening a window in which buffered bytes
         * are unaccounted for. A closed lease cannot be resized.
         *
         * @param retainedBytes exact capacity that must remain owned by this lease
         */
        public void shrinkTo(long retainedBytes) {
            long releasedBytes;
            synchronized (this) {
                if (this.closed.get()) {
                    throw new IllegalStateException("closed byte-budget lease cannot be resized");
                }
                if (retainedBytes < 0 || retainedBytes > this.bytes) {
                    throw new IllegalArgumentException("retainedBytes must be in 0.." + this.bytes);
                }
                releasedBytes = this.bytes - retainedBytes;
                this.bytes = retainedBytes;
            }
            if (releasedBytes > 0) {
                this.budget.release(releasedBytes);
            }
        }

        /**
         * Returns the owned capacity to the budget exactly once.
         */
        @Override
        public void close() {
            long releasedBytes = 0;
            synchronized (this) {
                if (this.closed.compareAndSet(false, true)) {
                    releasedBytes = this.bytes;
                    this.bytes = 0;
                }
            }
            if (releasedBytes > 0) {
                this.budget.release(releasedBytes);
            }
        }
    }

}

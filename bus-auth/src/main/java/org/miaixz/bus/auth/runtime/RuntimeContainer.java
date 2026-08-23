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
package org.miaixz.bus.auth.runtime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Owns one complete executable runtime container and retires its compiled Source workers after active calls finish.
 *
 * @author Kimi Liu
 */
final class RuntimeContainer {

    /**
     * Fixed Roster snapshot published with this container.
     */
    private final Roster roster;

    /**
     * Compiled Source workers keyed by exact Roster reference.
     */
    private final Map<Roster.Reference, SourceWorker> workers;

    /**
     * Monitor protecting retirement and lease accounting.
     */
    private final Object lifecycle = new Object();

    /**
     * Whether this container rejects new leases.
     */
    private boolean retired;

    /**
     * Whether all workers have already received their close attempt.
     */
    private boolean closed;

    /**
     * Number of active invocations retaining this container.
     */
    private int leases;

    /**
     * Creates one detached container from a fixed Roster snapshot and worker index.
     *
     * @param roster  fixed Roster snapshot
     * @param workers compiled Source worker index
     */
    RuntimeContainer(final Roster roster, final Map<Roster.Reference, SourceWorker> workers) {
        this.roster = Assert.notNull(roster, "Runtime container Roster must not be null");
        Assert.notNull(workers, "Runtime container Source worker index must not be null");
        final Map<Roster.Reference, SourceWorker> copy = new LinkedHashMap<>(workers.size());
        workers.forEach(
                (reference, worker) -> copy.put(
                        Assert.notNull(reference, "Runtime container Source worker reference must not be null"),
                        Assert.notNull(worker, "Runtime container Source worker must not be null")));
        this.workers = Map.copyOf(copy);
    }

    /**
     * Returns the fixed Roster snapshot published with this container.
     *
     * @return container Roster
     */
    Roster roster() {
        return roster;
    }

    /**
     * Resolves one compiled Source worker without crossing into Roster responsibilities.
     *
     * @param reference exact Source Roster reference
     * @return optional compiled worker
     */
    Optional<SourceWorker> worker(final Roster.Reference reference) {
        return Optional.ofNullable(
                workers.get(Assert.notNull(reference, "Runtime container Source worker reference must not be null")));
    }

    /**
     * Retains this container for one invocation unless retirement has begun.
     *
     * @return closeable lease, or {@code null} after retirement
     */
    Lease acquire() {
        synchronized (lifecycle) {
            if (retired) {
                return null;
            }
            leases++;
            return new Lease(this);
        }
    }

    /**
     * Rejects new leases and closes workers after the final active lease finishes.
     */
    void retire() {
        final boolean close;
        synchronized (lifecycle) {
            retired = true;
            close = leases == 0 && !closed;
            if (close) {
                closed = true;
            }
        }
        if (close) {
            closeWorkers();
        }
    }

    /**
     * Releases one active lease and completes deferred retirement when necessary.
     */
    private void release() {
        final boolean close;
        synchronized (lifecycle) {
            if (leases > 0) {
                leases--;
            }
            close = retired && leases == 0 && !closed;
            if (close) {
                closed = true;
            }
        }
        if (close) {
            closeWorkers();
        }
    }

    /**
     * Gives every compiled worker one best-effort close attempt.
     */
    private void closeWorkers() {
        for (SourceWorker worker : workers.values()) {
            try {
                worker.close();
            } catch (RuntimeException ignored) {
                // Retirement remains best-effort for every worker in this container.
            }
        }
    }

    /**
     * Atomically publishes the current complete runtime container.
     */
    static final class Cell {

        /**
         * Current published container.
         */
        private final AtomicReference<RuntimeContainer> current;

        /**
         * Creates the atomic cell with a complete initial container.
         *
         * @param initial initial container
         */
        Cell(final RuntimeContainer initial) {
            this.current = new AtomicReference<>(Assert.notNull(initial, "Initial runtime container must not be null"));
        }

        /**
         * Returns the current container for read-only observation or compare-and-set expectation.
         *
         * @return current container
         */
        RuntimeContainer current() {
            return current.get();
        }

        /**
         * Acquires a lease from the current non-retired container.
         *
         * @return current container lease, or {@code null} when the retained container is retired
         */
        Lease acquire() {
            while (true) {
                final RuntimeContainer observed = current.get();
                final Lease lease = observed.acquire();
                if (lease != null) {
                    return lease;
                }
                if (observed == current.get()) {
                    return null;
                }
            }
        }

        /**
         * Atomically replaces the expected complete container.
         *
         * @param expected    container observed before candidate loading
         * @param replacement fully compiled replacement container
         * @return whether publication succeeded
         */
        boolean replace(final RuntimeContainer expected, final RuntimeContainer replacement) {
            Assert.notNull(expected, "Expected runtime container must not be null");
            Assert.notNull(replacement, "Replacement runtime container must not be null");
            return current.compareAndSet(expected, replacement);
        }

    }

    /**
     * Retains one container until an invocation finishes.
     */
    static final class Lease implements AutoCloseable {

        /**
         * Retained container.
         */
        private final RuntimeContainer container;

        /**
         * Idempotent lease-close state.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a lease retaining one exact runtime container.
         *
         * @param container retained container
         */
        private Lease(final RuntimeContainer container) {
            this.container = container;
        }

        /**
         * Returns the retained container.
         *
         * @return retained container
         */
        RuntimeContainer container() {
            return container;
        }

        /**
         * Releases this lease exactly once.
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                container.release();
            }
        }

    }

}

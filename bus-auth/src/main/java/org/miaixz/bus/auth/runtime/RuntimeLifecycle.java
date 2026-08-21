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

import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;

/**
 * Provides the single lifecycle gate shared by every entry of one authentication runtime.
 * <p>
 * Closing rejects new operations immediately. Operations that already acquired a lease may finish, but reload commit is
 * accepted only while the same gate is still running. The last in-flight lease completes the transition to closed.
 * </p>
 *
 * @author Kimi Liu
 */
final class RuntimeLifecycle {

    /** Monitor guarding lifecycle state and operation count. */
    private final Object gate = new Object();
    /** Current lifecycle state guarded by {@link #gate}. */
    private Lifecycle.State state = Lifecycle.State.RUNNING;
    /** Number of admitted operations still holding leases. */
    private int operations;

    /** Creates a lifecycle gate in the running state. */
    RuntimeLifecycle() {
    }

    /**
     * Returns the current runtime state.
     *
     * @return shared lifecycle state
     */
    public Lifecycle.State state() {
        synchronized (gate) {
            return state;
        }
    }

    /**
     * Returns whether new work may start.
     *
     * @return {@code true} only while running
     */
    public boolean running() {
        return state() == Lifecycle.State.RUNNING;
    }

    /**
     * Acquires one in-flight operation lease.
     *
     * @return lease, or {@code null} after close has begun
     */
    public Lease enter() {
        synchronized (gate) {
            if (state != Lifecycle.State.RUNNING) {
                return null;
            }
            operations++;
            return new Lease(this);
        }
    }

    /**
     * Executes an atomic publication only when close has not begun.
     *
     * @param publication publication action
     * @return {@code true} when the action ran
     */
    public boolean commit(final Runnable publication) {
        synchronized (gate) {
            if (state != Lifecycle.State.RUNNING) {
                return false;
            }
            publication.run();
            return true;
        }
    }

    /**
     * Rejects new operations and closes after existing operation leases are released.
     */
    public void close() {
        synchronized (gate) {
            if (state != Lifecycle.State.RUNNING) {
                return;
            }
            state = operations == 0 ? Lifecycle.State.CLOSED : Lifecycle.State.CLOSING;
        }
    }

    /** Releases one admitted operation and completes deferred close when it was the last lease. */
    private void leave() {
        synchronized (gate) {
            if (operations > 0) {
                operations--;
            }
            if (state == Lifecycle.State.CLOSING && operations == 0) {
                state = Lifecycle.State.CLOSED;
            }
        }
    }

    /**
     * Represents one operation admitted by the shared runtime lifecycle.
     */
    public static final class Lease implements AutoCloseable {

        /** Lifecycle gate that admitted this operation. */
        private final RuntimeLifecycle lifecycle;
        /** Idempotent release marker. */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates one lease owned by an exact lifecycle gate.
         *
         * @param lifecycle admitting lifecycle
         */
        private Lease(final RuntimeLifecycle lifecycle) {
            this.lifecycle = lifecycle;
        }

        /** Releases this operation lease at most once. */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                lifecycle.leave();
            }
        }

    }

}

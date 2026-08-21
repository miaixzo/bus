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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.worker.RegistryListener;
import org.miaixz.bus.core.lang.Assert;

/**
 * Delivers Registry observations in acceptance order without executing project callbacks inside the commit gate.
 * <p>
 * This class owns notification ordering and failure isolation only. It does not load, validate, compile, publish, or
 * retain Registry data, and it never owns or closes the project executor.
 * </p>
 *
 * @author Kimi Liu
 */
final class RegistryNotifier {

    private static final int MAXIMUM_PENDING = 256;

    private final List<RegistryListener> listeners;
    private final Executor executor;
    private final Queue<Notification> pending = new ArrayDeque<>();
    private boolean dispatching;
    private boolean closed;
    private long dropped;
    private Registry.Revision latestCommitted = new Registry.Revision(0L);

    RegistryNotifier(final List<RegistryListener> listeners, final Executor executor) {
        Assert.notNull(listeners, "Registry listener list must not be null");
        final List<RegistryListener> copy = new ArrayList<>(listeners.size());
        for (RegistryListener listener : listeners) {
            copy.add(Assert.notNull(listener, "Registry listener must not be null"));
        }
        this.listeners = List.copyOf(copy);
        this.executor = Assert.notNull(executor, "Registry listener executor must not be null");
    }

    synchronized void enqueueCommitted(final Registry.Revision revision) {
        latestCommitted = Assert.notNull(revision, "Committed Registry revision must not be null");
        enqueue(listener -> listener.committed(revision));
    }

    void rejected(final Registry.Report report) {
        synchronized (this) {
            enqueue(listener -> listener.rejected(report));
        }
        dispatch();
    }

    private void enqueue(final Notification notification) {
        if (!closed && !listeners.isEmpty()) {
            if (pending.size() == MAXIMUM_PENDING) {
                pending.poll();
                dropped++;
            }
            pending.add(Assert.notNull(notification, "Registry notification must not be null"));
        }
    }

    /**
     * Schedules queued callbacks outside the Registry commit gate.
     * <p>
     * A direct project executor may execute the drain on this caller thread, but this method is invoked only after the
     * framework commit lock has been released.
     * </p>
     */
    void dispatch() {
        synchronized (this) {
            if (closed || dispatching || pending.isEmpty()) {
                return;
            }
            dispatching = true;
        }
        Thread.startVirtualThread(() -> {
            try {
                executor.execute(this::drain);
            } catch (RuntimeException ignored) {
                synchronized (this) {
                    dropped += pending.size();
                    pending.clear();
                    dispatching = false;
                }
            }
        });
    }

    private void drain() {
        while (true) {
            deliverOverflow();
            final Notification notification;
            synchronized (this) {
                notification = pending.poll();
                if (notification == null) {
                    dispatching = false;
                    return;
                }
            }
            deliver(notification);
        }
    }

    /** Delivers and resets the accumulated observation gap before later queued events. */
    private void deliverOverflow() {
        final long count;
        final Registry.Revision revision;
        synchronized (this) {
            count = dropped;
            revision = latestCommitted;
            dropped = 0L;
        }
        if (count == 0L) {
            return;
        }
        for (RegistryListener listener : listeners) {
            try {
                listener.overflow(count, revision);
            } catch (RuntimeException ignored) {
                // Observation failure cannot alter Registry publication.
            }
        }
    }

    private void deliver(final Notification notification) {
        for (RegistryListener listener : listeners) {
            try {
                notification.deliver(listener);
            } catch (RuntimeException ignored) {
                // Project observations cannot alter the framework decision that has already been made.
            }
        }
    }

    synchronized void close() {
        closed = true;
        pending.clear();
    }

    @FunctionalInterface
    private interface Notification {

        void deliver(RegistryListener listener);
    }

}

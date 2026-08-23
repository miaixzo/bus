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

import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.auth.worker.RosterListener;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

/**
 * Delivers Roster observations in acceptance order without executing project callbacks inside the commit gate.
 * <p>
 * This class owns notification ordering and failure isolation only. It does not load, validate, compile, publish, or
 * retain Roster data, and it never owns or closes the project executor.
 * </p>
 *
 * @author Kimi Liu
 */
final class RosterNotifier {

    /**
     * Immutable project listeners in deterministic delivery order.
     */
    private final List<RosterListener> listeners;
    /**
     * Caller-owned executor used to deliver listener callbacks.
     */
    private final Executor executor;
    /**
     * Bounded pending notification queue guarded by this instance monitor.
     */
    private final Queue<Notification> pending = new ArrayDeque<>();
    /**
     * Whether one queue-draining task is currently scheduled or running.
     */
    private boolean dispatching;
    /**
     * Whether shutdown has permanently disabled new notifications.
     */
    private boolean closed;
    /**
     * Number of dropped observations awaiting one overflow callback.
     */
    private long dropped;
    /**
     * Latest committed revision reported with an observation gap.
     */
    private Roster.Revision latestCommitted = new Roster.Revision(0L);

    /**
     * Creates one bounded failure-isolated Roster observation dispatcher.
     *
     * @param listeners project listeners in delivery order
     * @param executor  caller-owned callback executor
     */
    RosterNotifier(final List<RosterListener> listeners, final Executor executor) {
        Assert.notNull(listeners, "Roster listener list must not be null");
        final List<RosterListener> copy = new ArrayList<>(listeners.size());
        for (RosterListener listener : listeners) {
            copy.add(Assert.notNull(listener, "Roster listener must not be null"));
        }
        this.listeners = List.copyOf(copy);
        this.executor = Assert.notNull(executor, "Roster listener executor must not be null");
    }

    /**
     * Queues one committed revision while the Roster commit gate is held.
     *
     * @param revision committed revision
     */
    synchronized void enqueueCommitted(final Roster.Revision revision) {
        latestCommitted = Assert.notNull(revision, "Committed Roster revision must not be null");
        enqueue(listener -> listener.committed(revision));
    }

    /**
     * Queues and schedules one rejected Blueprint report.
     *
     * @param report rejected validation report
     */
    void rejected(final Roster.Report report) {
        synchronized (this) {
            enqueue(listener -> listener.rejected(report));
        }
        dispatch();
    }

    /**
     * Adds one callback to the bounded queue.
     *
     * @param notification callback to enqueue
     */
    private void enqueue(final Notification notification) {
        if (!closed && !listeners.isEmpty()) {
            if (pending.size() == Normal._256) {
                pending.poll();
                dropped++;
                Logger.warn(
                        false,
                        "Auth",
                        "Roster notification queue overflow: dropped={}, capacity={}",
                        dropped,
                        Normal._256);
            }
            pending.add(Assert.notNull(notification, "Roster notification must not be null"));
        }
    }

    /**
     * Schedules queued callbacks outside the Roster commit gate.
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
            } catch (RuntimeException cause) {
                final long droppedCount;
                synchronized (this) {
                    dropped += pending.size();
                    droppedCount = dropped;
                    pending.clear();
                    dispatching = false;
                }
                Logger.error(
                        false,
                        "Auth",
                        cause,
                        "Roster notification scheduling failed: dropped={}, exception={}",
                        droppedCount,
                        cause.getClass().getSimpleName());
            }
        });
    }

    /**
     * Drains pending callbacks in order until the queue becomes empty.
     */
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

    /**
     * Delivers and resets the accumulated observation gap before later queued events.
     */
    private void deliverOverflow() {
        final long count;
        final Roster.Revision revision;
        synchronized (this) {
            count = dropped;
            revision = latestCommitted;
            dropped = 0L;
        }
        if (count == 0L) {
            return;
        }
        Logger.warn(
                false,
                "Auth",
                "Roster notification observation gap: dropped={}, latestRevision={}",
                count,
                revision.value());
        for (RosterListener listener : listeners) {
            try {
                listener.overflow(count, revision);
            } catch (RuntimeException cause) {
                Logger.warn(
                        false,
                        "Auth",
                        cause,
                        "Roster overflow listener failed: listener={}, exception={}",
                        listener.getClass().getName(),
                        cause.getClass().getSimpleName());
            }
        }
    }

    /**
     * Delivers one callback to every listener without propagating listener failures.
     *
     * @param notification callback
     */
    private void deliver(final Notification notification) {
        for (RosterListener listener : listeners) {
            try {
                notification.deliver(listener);
            } catch (RuntimeException cause) {
                Logger.warn(
                        false,
                        "Auth",
                        cause,
                        "Roster listener failed: listener={}, exception={}",
                        listener.getClass().getName(),
                        cause.getClass().getSimpleName());
            }
        }
    }

    /**
     * Permanently disables notification delivery and clears queued callbacks.
     */
    synchronized void close() {
        closed = true;
        pending.clear();
        Logger.debug(false, "Auth", "Roster notifier closed: listeners={}", listeners.size());
    }

    /**
     * Represents one isolated listener callback.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface Notification {

        /**
         * Delivers this observation to one listener.
         *
         * @param listener destination listener
         */
        void deliver(RosterListener listener);

    }

}

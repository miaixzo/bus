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
package org.miaixz.bus.fabric.runtime.dispatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Synchronized ready and running queues for dispatch handles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchQueue implements AutoCloseable {

    /**
     * Dispatch limit.
     */
    private final DispatchLimit limit;

    /**
     * Ready handles.
     */
    private final ArrayDeque<Entry> queued;

    /**
     * Running handles.
     */
    private final LinkedHashMap<DispatchHandle, Entry> running;

    /**
     * Running counts per key.
     */
    private final Map<String, Integer> runningByKey;

    /**
     * Whether this queue permanently rejects new short tasks.
     */
    private boolean closed;

    /**
     * Creates a queue.
     *
     * @param limit dispatch limit
     */
    DispatchQueue(final DispatchLimit limit) {
        this.limit = require(limit, "Dispatch limit");
        this.queued = new ArrayDeque<>();
        this.running = new LinkedHashMap<>();
        this.runningByKey = new HashMap<>();
    }

    /**
     * Adds a handle to the ready queue.
     *
     * @param handle dispatch handle
     * @return true when enqueued
     */
    public synchronized boolean enqueue(final DispatchHandle handle) {
        final DispatchHandle current = require(handle, "Dispatch handle");
        return enqueue(current.tag(), current, current.activity(), current.activity().cancellation(), current);
    }

    /**
     * Adds one fully owned short-task snapshot to this queue.
     *
     * @param tag          cancellation tag
     * @param owner        task owner
     * @param activity     short activity
     * @param cancellation activity cancellation scope
     * @param handle       unique dispatch handle
     * @return true when enqueued
     */
    synchronized boolean enqueue(
            final Object tag,
            final Object owner,
            final Activity activity,
            final Cancellation cancellation,
            final DispatchHandle handle) {
        final Entry entry = new Entry(tag, owner, activity, cancellation, handle);
        if (closed || entry.handle().state() != Status.QUEUED || entry.handle().future().isDone()
                || contains(entry.handle())) {
            return false;
        }
        queued.addLast(entry);
        return true;
    }

    /**
     * Promotes ready handles that fit the dispatch limits.
     *
     * @return promoted handles
     */
    public synchronized List<DispatchHandle> promote() {
        return promoteEntries().stream().map(Entry::handle).toList();
    }

    /**
     * Reserves ready short tasks that fit the dispatch limits for a worker.
     * <p>
     * The consuming worker must successfully call {@link DispatchHandle#markRunning()} before invoking each returned
     * activity. A failed promotion must be finished without executing the activity.
     *
     * @return promoted task entries
     */
    synchronized List<Entry> promoteEntries() {
        final List<Entry> promoted = new ArrayList<>();
        final Iterator<Entry> iterator = queued.iterator();
        while (iterator.hasNext() && running.size() < limit.max()) {
            final Entry entry = iterator.next();
            final DispatchHandle handle = entry.handle();
            if (handle.state() != Status.QUEUED || handle.future().isDone()) {
                iterator.remove();
                continue;
            }
            final int countForKey = runningByKey.getOrDefault(handle.key(), Normal._0);
            if (!limit.canPromote(running.size(), countForKey)) {
                continue;
            }
            iterator.remove();
            running.put(handle, entry);
            runningByKey.put(handle.key(), countForKey + Normal._1);
            promoted.add(entry);
        }
        return List.copyOf(promoted);
    }

    /**
     * Removes a completed running handle.
     *
     * @param handle dispatch handle
     */
    public synchronized void finish(final DispatchHandle handle) {
        require(handle, "Dispatch handle");
        if (running.remove(handle) != null) {
            final String key = handle.key();
            final int count = runningByKey.getOrDefault(key, Normal._0);
            if (count <= Normal._1) {
                runningByKey.remove(key);
            } else {
                runningByKey.put(key, count - Normal._1);
            }
        }
    }

    /**
     * Cancels a ready or running handle.
     *
     * @param handle dispatch handle
     * @return true when cancelled
     */
    public boolean cancel(final DispatchHandle handle) {
        require(handle, "Dispatch handle");
        final boolean present;
        synchronized (this) {
            final boolean queuedRemoved = removeQueued(handle);
            final boolean runningRemoved = running.remove(handle) != null;
            if (runningRemoved) {
                decrement(handle.key());
            }
            present = queuedRemoved || runningRemoved;
        }
        return present && handle.cancel();
    }

    /**
     * Returns a ready snapshot.
     *
     * @return ready snapshot
     */
    public synchronized List<DispatchHandle> queued() {
        return queued.stream().map(Entry::handle).toList();
    }

    /**
     * Returns a running snapshot.
     *
     * @return running snapshot
     */
    public synchronized List<DispatchHandle> running() {
        return List.copyOf(running.keySet());
    }

    /**
     * Returns whether this queue is idle.
     *
     * @return true when no ready or running handles remain
     */
    public synchronized boolean idle() {
        return queued.isEmpty() && running.isEmpty();
    }

    /**
     * Permanently closes this queue, cancels every retained task, and clears all counts.
     */
    @Override
    public void close() {
        final List<DispatchHandle> handles;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            handles = new ArrayList<>(queued.size() + running.size());
            queued.stream().map(Entry::handle).forEach(handles::add);
            handles.addAll(running.keySet());
            queued.clear();
            running.clear();
            runningByKey.clear();
        }
        for (final DispatchHandle handle : handles) {
            handle.cancel();
        }
    }

    /**
     * Returns whether either queue partition already contains a handle.
     *
     * @param handle handle
     * @return true when retained
     */
    private boolean contains(final DispatchHandle handle) {
        if (running.containsKey(handle)) {
            return true;
        }
        return queued.stream().anyMatch(entry -> entry.handle() == handle);
    }

    /**
     * Removes one queued entry by handle identity.
     *
     * @param handle handle
     * @return true when removed
     */
    private boolean removeQueued(final DispatchHandle handle) {
        final Iterator<Entry> iterator = queued.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().handle() == handle) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Decrements running count for a key.
     *
     * @param key dispatch key
     */
    private void decrement(final String key) {
        final int count = runningByKey.getOrDefault(key, Normal._0);
        if (count <= Normal._1) {
            runningByKey.remove(key);
        } else {
            runningByKey.put(key, count - Normal._1);
        }
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Immutable ownership snapshot for one short dispatch task.
     */
    static final class Entry {

        /**
         * Cancellation tag.
         */
        private final Object tag;

        /**
         * Task owner.
         */
        private final Object owner;

        /**
         * Short activity.
         */
        private final Activity activity;

        /**
         * Activity cancellation scope.
         */
        private final Cancellation cancellation;

        /**
         * Unique authoritative handle.
         */
        private final DispatchHandle handle;

        /**
         * Creates a validated short-task snapshot.
         *
         * @param tag          cancellation tag
         * @param owner        task owner
         * @param activity     activity
         * @param cancellation cancellation scope
         * @param handle       unique handle
         */
        private Entry(final Object tag, final Object owner, final Activity activity, final Cancellation cancellation,
                final DispatchHandle handle) {
            this.tag = tag;
            this.owner = require(owner, "Dispatch owner");
            this.activity = require(activity, "Dispatch activity");
            this.cancellation = require(cancellation, "Dispatch cancellation");
            this.handle = require(handle, "Dispatch handle");
            Assert.isTrue(
                    this.activity == this.handle.activity(),
                    () -> new ValidateException("Dispatch entry activity must belong to its handle"));
            Assert.isTrue(
                    this.cancellation == this.activity.cancellation(),
                    () -> new ValidateException("Dispatch entry cancellation must belong to its activity"));
            Assert.isTrue(
                    Objects.equals(this.tag, this.handle.tag()),
                    () -> new ValidateException("Dispatch entry tag must match its handle"));
        }

        /**
         * Returns the cancellation tag.
         *
         * @return tag
         */
        Object tag() {
            return tag;
        }

        /**
         * Returns the task owner.
         *
         * @return owner
         */
        Object owner() {
            return owner;
        }

        /**
         * Returns the short activity.
         *
         * @return activity
         */
        Activity activity() {
            return activity;
        }

        /**
         * Returns the activity cancellation scope.
         *
         * @return cancellation scope
         */
        Cancellation cancellation() {
            return cancellation;
        }

        /**
         * Returns the unique dispatch handle.
         *
         * @return handle
         */
        DispatchHandle handle() {
            return handle;
        }

    }

}

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
import java.util.IdentityHashMap;
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
     * Maximum number of short tasks retained in the ready partition.
     */
    private static final int MAX_QUEUED = 65_536;

    /**
     * Global and per-key concurrency limits applied during reservation.
     */
    private final DispatchLimit limit;

    /**
     * Ready-task buckets indexed by dispatch key.
     */
    private final Map<String, ReadyBucket> buckets;

    /**
     * Round-robin keys that currently have queued work.
     */
    private final ArrayDeque<ReadyBucket> ready;

    /**
     * O(1) queued membership by handle identity.
     */
    private final Map<DispatchHandle, Entry> queuedEntries;

    /**
     * Queue-local enqueue sequence used to order diagnostic snapshots.
     */
    private long sequence;

    /**
     * Reserved or running tasks indexed by handle identity in promotion order.
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
     * Creates an empty queue governed by the supplied dispatch limits.
     *
     * @param limit global and per-key dispatch limits
     */
    DispatchQueue(final DispatchLimit limit) {
        this.limit = require(limit, "Dispatch limit");
        this.buckets = new HashMap<>();
        this.ready = new ArrayDeque<>();
        this.queuedEntries = new IdentityHashMap<>();
        this.running = new LinkedHashMap<>();
        this.runningByKey = new HashMap<>();
    }

    /**
     * Adds an eligible handle to the ready queue using ownership data from the handle and its activity.
     *
     * @param handle queued handle to retain
     * @return {@code true} if the handle was accepted into the ready queue
     */
    public synchronized boolean enqueue(final DispatchHandle handle) {
        final DispatchHandle current = require(handle, "Dispatch handle");
        return enqueue(current.tag(), current, current.activity(), current.activity().cancellation(), current);
    }

    /**
     * Adds one fully owned short-task snapshot to this queue.
     *
     * @param tag          cancellation tag, which may be {@code null}
     * @param owner        object that owns the task
     * @param activity     short activity to execute
     * @param cancellation cancellation scope belonging to the activity
     * @param handle       unique queued handle belonging to the activity
     * @return {@code true} if the snapshot was accepted into the ready queue
     */
    synchronized boolean enqueue(
            final Object tag,
            final Object owner,
            final Activity activity,
            final Cancellation cancellation,
            final DispatchHandle handle) {
        final DispatchHandle current = require(handle, "Dispatch handle");
        if (closed || current.state() != Status.QUEUED || current.future().isDone() || contains(current)
                || queuedEntries.size() >= MAX_QUEUED) {
            return false;
        }
        final Entry entry = new Entry(tag, owner, activity, cancellation, current, ++sequence);
        final ReadyBucket bucket = buckets.computeIfAbsent(current.key(), ReadyBucket::new);
        bucket.entries.addLast(entry);
        queuedEntries.put(current, entry);
        publish(bucket);
        return true;
    }

    /**
     * Reserves ready handles that fit the dispatch limits and moves them to the running partition.
     *
     * @return immutable list of handles reserved during this pass, in reservation order
     */
    public synchronized List<DispatchHandle> promote() {
        final List<Entry> entries = reserveEntries();
        final ArrayList<DispatchHandle> handles = new ArrayList<>(entries.size());
        for (final Entry entry : entries) {
            handles.add(entry.handle());
        }
        return List.copyOf(handles);
    }

    /**
     * Reserves ready short tasks that fit the dispatch limits for a worker.
     * <p>
     * The consuming worker must successfully call {@link DispatchHandle#markRunning()} before invoking each returned
     * activity. A failed promotion must be finished without executing the activity.
     *
     * @return immutable list of task entries reserved during this pass, in reservation order
     */
    synchronized List<Entry> promoteEntries() {
        return reserveEntries();
    }

    /**
     * Performs one bounded ready-bucket reservation pass while the queue monitor is held.
     *
     * @return task entries reserved for immediate worker execution
     */
    private List<Entry> reserveEntries() {
        final List<Entry> promoted = new ArrayList<>();
        int budget = ready.size() + Math.max(0, limit.max() - running.size());
        while (budget-- > 0 && !ready.isEmpty() && running.size() < limit.max()) {
            final ReadyBucket bucket = ready.removeFirst();
            bucket.ready = false;
            final Entry entry = bucket.entries.pollFirst();
            if (entry == null) {
                buckets.remove(bucket.key, bucket);
                continue;
            }
            final DispatchHandle handle = entry.handle();
            if (handle.state() != Status.QUEUED || handle.future().isDone()) {
                queuedEntries.remove(handle);
                publishOrRemove(bucket);
                continue;
            }
            final int countForKey = runningByKey.getOrDefault(handle.key(), Normal._0);
            if (!limit.canPromote(running.size(), countForKey)) {
                bucket.entries.addFirst(entry);
                publish(bucket);
                continue;
            }
            queuedEntries.remove(handle);
            running.put(handle, entry);
            runningByKey.put(handle.key(), countForKey + Normal._1);
            promoted.add(entry);
            publishOrRemove(bucket);
        }
        return promoted;
    }

    /**
     * Removes a completed handle from the running partition and releases its per-key capacity.
     *
     * @param handle completed handle whose reservation is released
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
     * Removes a retained handle and asks it to transition to the cancelled state.
     *
     * @param handle retained handle to cancel
     * @return {@code true} if the handle was retained and its cancellation transition succeeded
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
     * Returns a ready-handle snapshot in original enqueue order.
     *
     * @return immutable snapshot of handles currently awaiting reservation
     */
    public synchronized List<DispatchHandle> queued() {
        final ArrayList<Entry> entries = new ArrayList<>(queuedEntries.values());
        entries.sort((left, right) -> Long.compare(left.sequence, right.sequence));
        final ArrayList<DispatchHandle> handles = new ArrayList<>(entries.size());
        for (final Entry entry : entries) {
            handles.add(entry.handle());
        }
        return List.copyOf(handles);
    }

    /**
     * Returns a snapshot of handles in reservation order from the running partition.
     *
     * @return immutable snapshot of reserved or running handles
     */
    public synchronized List<DispatchHandle> running() {
        return List.copyOf(running.keySet());
    }

    /**
     * Returns whether this queue is idle.
     *
     * @return {@code true} when neither queue partition retains a handle
     */
    public synchronized boolean idle() {
        return queuedEntries.isEmpty() && running.isEmpty();
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
            handles = new ArrayList<>(queuedEntries.size() + running.size());
            handles.addAll(queuedEntries.keySet());
            handles.addAll(running.keySet());
            queuedEntries.clear();
            buckets.clear();
            ready.clear();
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
     * @param handle handle identity to locate
     * @return {@code true} if either partition retains the handle
     */
    private boolean contains(final DispatchHandle handle) {
        return running.containsKey(handle) || queuedEntries.containsKey(handle);
    }

    /**
     * Removes one queued entry by handle identity.
     *
     * @param handle handle identity to remove
     * @return {@code true} if a ready entry was removed
     */
    private boolean removeQueued(final DispatchHandle handle) {
        final Entry entry = queuedEntries.remove(handle);
        if (entry == null) {
            return false;
        }
        final ReadyBucket bucket = buckets.get(handle.key());
        if (bucket != null) {
            bucket.entries.remove(entry);
            if (bucket.entries.isEmpty()) {
                buckets.remove(bucket.key, bucket);
                if (bucket.ready) {
                    ready.remove(bucket);
                    bucket.ready = false;
                }
            }
        }
        return true;
    }

    /**
     * Publishes a non-empty bucket once.
     *
     * @param bucket per-key bucket to append to the ready queue
     */
    private void publish(final ReadyBucket bucket) {
        if (!bucket.ready && !bucket.entries.isEmpty()) {
            bucket.ready = true;
            ready.addLast(bucket);
        }
    }

    /**
     * Republishes non-empty work or removes an empty bucket.
     *
     * @param bucket per-key bucket whose remaining work is reconciled
     */
    private void publishOrRemove(final ReadyBucket bucket) {
        if (bucket.entries.isEmpty()) {
            buckets.remove(bucket.key, bucket);
        } else {
            publish(bucket);
        }
    }

    /**
     * Decrements running count for a key.
     *
     * @param key dispatch key whose running count is released
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
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
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
         * Queue-local enqueue sequence used for stable diagnostic ordering.
         */
        private final long sequence;

        /**
         * Creates a validated short-task snapshot.
         *
         * @param tag          cancellation tag, which may be {@code null}
         * @param owner        object that owns the task
         * @param activity     short activity to execute
         * @param cancellation cancellation scope belonging to the activity
         * @param handle       unique handle belonging to the activity
         * @param sequence     queue-local enqueue sequence used for stable ordering
         */
        private Entry(final Object tag, final Object owner, final Activity activity, final Cancellation cancellation,
                final DispatchHandle handle, final long sequence) {
            this.tag = tag;
            this.owner = require(owner, "Dispatch owner");
            this.activity = require(activity, "Dispatch activity");
            this.cancellation = require(cancellation, "Dispatch cancellation");
            this.handle = require(handle, "Dispatch handle");
            this.sequence = sequence;
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
         * @return cancellation tag, or {@code null} when no tag was assigned
         */
        Object tag() {
            return tag;
        }

        /**
         * Returns the task owner.
         *
         * @return object that owns this task
         */
        Object owner() {
            return owner;
        }

        /**
         * Returns the short activity.
         *
         * @return short activity to execute
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
         * @return authoritative handle for this task
         */
        DispatchHandle handle() {
            return handle;
        }

    }

    /**
     * FIFO work bucket for one dispatch key.
     * <p>
     * Queue methods access instances only while holding the enclosing queue monitor. The {@code ready} flag records
     * whether the key is already published in the round-robin ready-key deque.
     * </p>
     */
    private static final class ReadyBucket {

        /**
         * Dispatch key shared by every entry in this bucket.
         */
        private final String key;

        /**
         * Entries awaiting reservation in FIFO order.
         */
        private final ArrayDeque<Entry> entries = new ArrayDeque<>();

        /**
         * Whether this bucket currently has a token in the ready-key deque.
         */
        private boolean ready;

        /**
         * Creates an empty bucket for a dispatch key.
         *
         * @param key dispatch key shared by entries added to the bucket
         */
        private ReadyBucket(final String key) {
            this.key = key;
        }
    }

}

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Synchronized ready and running queues for dispatch handles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchQueue {

    /**
     * Dispatch limit.
     */
    private final DispatchLimit limit;

    /**
     * Ready handles.
     */
    private final ArrayDeque<DispatchHandle> queued;

    /**
     * Running handles.
     */
    private final LinkedHashSet<DispatchHandle> running;

    /**
     * Running counts per key.
     */
    private final Map<String, Integer> runningByKey;

    /**
     * Creates a queue.
     *
     * @param limit dispatch limit
     */
    DispatchQueue(final DispatchLimit limit) {
        this.limit = require(limit, "Dispatch limit");
        this.queued = new ArrayDeque<>();
        this.running = new LinkedHashSet<>();
        this.runningByKey = new HashMap<>();
    }

    /**
     * Adds a handle to the ready queue.
     *
     * @param handle dispatch handle
     * @return true when enqueued
     */
    public synchronized boolean enqueue(final DispatchHandle handle) {
        require(handle, "Dispatch handle");
        if (handle.cancelled() || handle.future().isDone() || queued.contains(handle) || running.contains(handle)) {
            return false;
        }
        queued.add(handle);
        return true;
    }

    /**
     * Promotes ready handles that fit the dispatch limits.
     *
     * @return promoted handles
     */
    public synchronized List<DispatchHandle> promote() {
        final List<DispatchHandle> promoted = new ArrayList<>();
        final Iterator<DispatchHandle> iterator = queued.iterator();
        while (iterator.hasNext() && running.size() < limit.max()) {
            final DispatchHandle handle = iterator.next();
            if (handle.cancelled() || handle.future().isDone()) {
                iterator.remove();
                continue;
            }
            final int countForKey = runningByKey.getOrDefault(handle.key(), 0);
            if (!limit.canPromote(running.size(), countForKey)) {
                continue;
            }
            iterator.remove();
            running.add(handle);
            runningByKey.put(handle.key(), countForKey + 1);
            promoted.add(handle);
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
        if (running.remove(handle)) {
            final String key = handle.key();
            final int count = runningByKey.getOrDefault(key, 0);
            if (count <= 1) {
                runningByKey.remove(key);
            } else {
                runningByKey.put(key, count - 1);
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
            final boolean queuedRemoved = queued.remove(handle);
            final boolean runningRemoved = running.remove(handle);
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
        return List.copyOf(queued);
    }

    /**
     * Returns a running snapshot.
     *
     * @return running snapshot
     */
    public synchronized List<DispatchHandle> running() {
        return List.copyOf(running);
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
     * Decrements running count for a key.
     *
     * @param key dispatch key
     */
    private void decrement(final String key) {
        final int count = runningByKey.getOrDefault(key, 0);
        if (count <= 1) {
            runningByKey.remove(key);
        } else {
            runningByKey.put(key, count - 1);
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}

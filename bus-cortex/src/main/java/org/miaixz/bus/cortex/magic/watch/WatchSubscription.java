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
package org.miaixz.bus.cortex.magic.watch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Watch;

import lombok.Getter;
import lombok.Setter;

/**
 * Single watch subscription entry.
 *
 * @param <T> watched value type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class WatchSubscription<T> {

    /**
     * Vector that defines what this watch subscription is tracking.
     */
    private Vector vector;
    /**
     * Canonical namespace identifier used for quota accounting and event envelopes.
     */
    private String namespace_id;
    /**
     * Listener to invoke when the watched values change.
     */
    private Listener<Watch<T>> listener;
    /**
     * Unix epoch milliseconds when the watch subscription was created.
     */
    private long createdAt;
    /**
     * Unix epoch milliseconds of the last access, used for expiry.
     */
    private long lastAccess;
    /**
     * Unix epoch milliseconds of the last dispatched event.
     */
    private long lastEventAt;
    /**
     * Unix epoch milliseconds when the watch should expire.
     */
    private long expiresAt;
    /**
     * Number of dispatch attempts executed for the subscription.
     */
    private long dispatchCount;
    /**
     * Number of events currently accepted but not yet fully delivered to the listener.
     */
    private long pendingCount;
    /**
     * Highest observed pending count for this subscription.
     */
    private long peakPendingCount;
    /**
     * Last watch event sequence emitted for this subscription. This is intentionally separate from dispatch count so
     * consumers can validate event ordering across filtered and retried notifications.
     */
    private long lastSequence;
    /**
     * Number of events dropped before they entered the serial dispatch chain.
     */
    private long droppedCount;
    /**
     * Unix epoch milliseconds of the last dropped event.
     */
    private long lastDroppedAt;
    /**
     * Number of failed dispatch attempts.
     */
    private long failureCount;
    /**
     * Last error observed while dispatching to the listener.
     */
    private String lastError;
    /**
     * Tail of the per-subscription dispatch chain. Events are appended to this chain so one subscriber observes
     * notifications in the same order they were emitted.
     */
    private CompletableFuture<Void> dispatchTail = CompletableFuture.completedFuture(null);
    /**
     * Unix epoch milliseconds of the last successfully delivered event.
     */
    private long lastDeliveredAt;

    /**
     * Creates one watch subscription with common lifecycle timestamps initialized.
     *
     * @param vector       watch vector
     * @param namespace_id canonical namespace identifier for quota accounting
     * @param listener     listener to invoke
     * @param now          current timestamp
     */
    public WatchSubscription(Vector vector, String namespace_id, Listener<Watch<T>> listener, long now) {
        this.vector = vector;
        this.namespace_id = namespace_id;
        this.listener = listener;
        this.createdAt = now;
        this.lastAccess = now;
        this.expiresAt = now;
    }

    /**
     * Updates the watch access timestamps.
     *
     * @param now      current timestamp
     * @param expireMs expiry window in milliseconds
     */
    public void touch(long now, long expireMs) {
        this.lastAccess = now;
        this.expiresAt = now + Math.max(expireMs, 0L);
    }

    /**
     * Appends one listener task to this subscription's serial dispatch chain.
     *
     * @param task     task to execute
     * @param executor executor that runs listener callbacks
     */
    public synchronized void enqueueDispatch(Runnable task, Executor executor) {
        dispatchTail = dispatchTail.handle((ignored, failure) -> null).thenRunAsync(task, executor);
    }

    /**
     * Reserves one slot in the subscription backlog before an event enters the serial dispatch chain.
     *
     * @param maxPending maximum allowed pending event count, or {@code <= 0} for unlimited
     * @return {@code true} when the event may enter the backlog
     */
    public synchronized boolean reservePending(int maxPending) {
        if (maxPending > 0 && pendingCount >= maxPending) {
            return false;
        }
        pendingCount++;
        if (pendingCount > peakPendingCount) {
            peakPendingCount = pendingCount;
        }
        return true;
    }

    /**
     * Marks one event as dropped before listener delivery.
     *
     * @param now         current timestamp
     * @param errorReason optional diagnostic reason
     */
    public synchronized void recordDrop(long now, String errorReason) {
        droppedCount++;
        lastDroppedAt = now;
        if (errorReason != null && !errorReason.isBlank()) {
            lastError = errorReason;
        }
    }

    /**
     * Completes one pending delivery attempt and updates delivery diagnostics.
     *
     * @param now current timestamp
     */
    public synchronized void completeDelivery(long now) {
        releasePending();
        lastDeliveredAt = now;
    }

    /**
     * Releases one pending slot without marking the event as successfully delivered.
     */
    public synchronized void releasePending() {
        if (pendingCount > 0) {
            pendingCount--;
        }
    }

}

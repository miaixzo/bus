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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides non-blocking, hierarchical admission for complete requests and long-lived response streams.
 * <p>
 * Every accepted exchange owns one request permit until its reactive lifecycle terminates. Downloads and realtime
 * streams additionally own one shared streaming permit and one mode-specific permit. Acquisition never waits or blocks
 * an event-loop thread; saturation is reported by returning {@code null}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AdmissionGate {

    /**
     * Permits for complete inbound request lifecycles.
     */
    private final Semaphore requests;

    /**
     * Permits shared by downloads and realtime streams.
     */
    private final Semaphore streams;

    /**
     * Mode-specific permits for file downloads.
     */
    private final Semaphore downloads;

    /**
     * Mode-specific permits for latency-sensitive realtime streams.
     */
    private final Semaphore realtimeStreams;

    /**
     * Configured request capacity, retained for snapshots.
     */
    private final int maxRequests;

    /**
     * Configured shared streaming capacity, retained for snapshots.
     */
    private final int maxStreams;

    /**
     * Configured download capacity, retained for snapshots.
     */
    private final int maxDownloads;

    /**
     * Configured realtime-stream capacity, retained for snapshots.
     */
    private final int maxRealtimeStreams;

    /**
     * Creates a hierarchical admission gate.
     *
     * @param maxRequests        maximum complete request lifecycles
     * @param maxStreams         maximum downloads and realtime streams combined
     * @param maxDownloads       maximum file downloads
     * @param maxRealtimeStreams maximum realtime streams
     */
    public AdmissionGate(int maxRequests, int maxStreams, int maxDownloads, int maxRealtimeStreams) {
        if (maxRequests <= 0 || maxStreams <= 0 || maxStreams > maxRequests || maxDownloads <= 0
                || maxDownloads > maxStreams || maxRealtimeStreams <= 0 || maxRealtimeStreams > maxStreams) {
            throw new IllegalArgumentException("invalid Vortex admission limits");
        }
        this.maxRequests = maxRequests;
        this.maxStreams = maxStreams;
        this.maxDownloads = maxDownloads;
        this.maxRealtimeStreams = maxRealtimeStreams;
        this.requests = new Semaphore(maxRequests, false);
        this.streams = new Semaphore(maxStreams, false);
        this.downloads = new Semaphore(maxDownloads, false);
        this.realtimeStreams = new Semaphore(maxRealtimeStreams, false);
    }

    /**
     * Attempts to admit one complete request lifecycle.
     *
     * @return an ownership lease, or {@code null} when request capacity is exhausted
     */
    public Lease tryAcquireRequest() {
        return this.requests.tryAcquire() ? new Lease(this.requests) : null;
    }

    /**
     * Attempts to acquire both shared streaming and download-specific capacity.
     *
     * @return an ownership lease, or {@code null} when either capacity is exhausted
     */
    public Lease tryAcquireDownload() {
        if (!this.streams.tryAcquire()) {
            return null;
        }
        if (!this.downloads.tryAcquire()) {
            this.streams.release();
            return null;
        }
        return new Lease(this.downloads, this.streams);
    }

    /**
     * Attempts to acquire both shared streaming and realtime-specific capacity.
     *
     * @return an ownership lease, or {@code null} when either capacity is exhausted
     */
    public Lease tryAcquireRealtimeStream() {
        if (!this.streams.tryAcquire()) {
            return null;
        }
        if (!this.realtimeStreams.tryAcquire()) {
            this.streams.release();
            return null;
        }
        return new Lease(this.realtimeStreams, this.streams);
    }

    /**
     * Returns a point-in-time view of currently owned permits.
     *
     * @return current admission usage
     */
    public Snapshot snapshot() {
        return new Snapshot(this.maxRequests - this.requests.availablePermits(),
                this.maxStreams - this.streams.availablePermits(),
                this.maxDownloads - this.downloads.availablePermits(),
                this.maxRealtimeStreams - this.realtimeStreams.availablePermits());
    }

    /**
     * Records the number of active request, shared-stream, download and realtime-stream permits.
     *
     * @param requests        active complete-request permits
     * @param streams         active shared-stream permits
     * @param downloads       active download permits
     * @param realtimeStreams active realtime-stream permits
     */
    public record Snapshot(int requests, int streams, int downloads, int realtimeStreams) {
    }

    /**
     * Idempotent ownership token for one or more admission permits.
     */
    public static final class Lease implements AutoCloseable {

        /**
         * Semaphores whose permits are jointly owned by this lease.
         */
        private final Semaphore[] permits;

        /**
         * Ensures every owned permit is returned only once.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a lease over permits that were already acquired atomically by the gate.
         *
         * @param permits semaphores whose permits this lease must return
         */
        private Lease(Semaphore... permits) {
            this.permits = permits;
        }

        /**
         * Returns every owned permit exactly once.
         */
        @Override
        public void close() {
            if (this.closed.compareAndSet(false, true)) {
                for (Semaphore permit : this.permits) {
                    permit.release();
                }
            }
        }
    }

}

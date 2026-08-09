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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Samples heap and Netty direct-memory usage on a dedicated daemon thread.
 * <p>
 * Direct-memory pressure rejects downloads first and realtime streams next; heap pressure stops new buffered work.
 * Emergency pressure rejects every new request. Recovery uses lower thresholds to avoid admission oscillation.
 *
 * @author Kimi Liu
 */
public final class MemoryPressure implements AutoCloseable {

    /**
     * Supplies direct bytes currently allocated by the unified Netty allocator.
     */
    private final LongSupplier directUsed;
    /**
     * Maximum direct memory available to the process.
     */
    private final long directMaximum;
    /**
     * Direct-memory ratio that first rejects new downloads.
     */
    private final double directDownloadThreshold;
    /**
     * Direct-memory ratio that rejects every new stream.
     */
    private final double directStreamingThreshold;
    /**
     * Direct-memory ratio that rejects every new request.
     */
    private final double directEmergencyThreshold;
    /**
     * Direct-memory ratio below which hysteresis permits recovery to normal.
     */
    private final double directRecoveryThreshold;
    /**
     * Single daemon executor that samples memory away from request threads.
     */
    private final ScheduledExecutorService sampler;
    /**
     * Latest sampled state, published lock-free to request threads.
     */
    private volatile Level level = Level.NORMAL;

    /**
     * Starts periodic memory sampling.
     *
     * @param directUsed          supplier for currently allocated direct bytes
     * @param directMaximum       maximum direct memory in bytes
     * @param directHighWatermark ratio that stops new realtime streaming work
     * @param directLowWatermark  recovery ratio below which normal operation resumes
     * @param intervalMillis      sampling interval in milliseconds
     */
    public MemoryPressure(LongSupplier directUsed, long directMaximum, double directHighWatermark,
            double directLowWatermark, int intervalMillis) {
        if (directUsed == null || directMaximum <= 0 || directHighWatermark <= 0 || directHighWatermark >= 0.90d
                || directLowWatermark < 0 || directLowWatermark >= directHighWatermark || intervalMillis <= 0) {
            throw new IllegalArgumentException("invalid memory pressure configuration");
        }
        this.directUsed = directUsed;
        this.directMaximum = directMaximum;
        this.directDownloadThreshold = Math.max(directLowWatermark, directHighWatermark - 0.10d);
        this.directStreamingThreshold = directHighWatermark;
        this.directEmergencyThreshold = directHighWatermark + 0.10d;
        this.directRecoveryThreshold = directLowWatermark;
        this.sampler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "vortex-memory-pressure");
            thread.setDaemon(true);
            return thread;
        });
        this.sampler.scheduleAtFixedRate(this::sample, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Samples both memory domains and advances or recovers the pressure state.
     */
    private void sample() {
        Runtime runtime = Runtime.getRuntime();
        double heapRatio = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();
        double directRatio = (double) Math.max(0, this.directUsed.getAsLong()) / this.directMaximum;
        if (heapRatio >= 0.85d || directRatio >= this.directEmergencyThreshold) {
            this.level = Level.EMERGENCY;
        } else if (heapRatio >= 0.75d) {
            this.level = Level.BUFFERING_THROTTLED;
        } else if (directRatio >= this.directStreamingThreshold) {
            this.level = Level.STREAMING_THROTTLED;
        } else if (directRatio >= this.directDownloadThreshold) {
            this.level = Level.DOWNLOAD_THROTTLED;
        } else if (heapRatio < 0.65d && directRatio < this.directRecoveryThreshold) {
            this.level = Level.NORMAL;
        }
    }

    /**
     * Returns the latest sampled pressure state.
     *
     * @return latest sampled pressure state
     */
    public Level level() {
        return this.level;
    }

    /**
     * Reports whether emergency pressure blocks every new request.
     *
     * @return whether every new request must be rejected
     */
    public boolean rejectAll() {
        return this.level == Level.EMERGENCY;
    }

    /**
     * Reports whether heap pressure blocks new body materialization.
     *
     * @return whether new work that materializes a complete body must wait
     */
    public boolean rejectBuffering() {
        return this.level == Level.BUFFERING_THROTTLED || rejectAll();
    }

    /**
     * Reports whether direct-memory pressure blocks new realtime streams.
     *
     * @return whether new realtime streams must be rejected
     */
    public boolean rejectStreaming() {
        return this.level == Level.STREAMING_THROTTLED || rejectAll();
    }

    /**
     * Reports whether direct-memory pressure blocks new downloads.
     *
     * @return whether new file downloads must be rejected
     */
    public boolean rejectDownloads() {
        return this.level == Level.DOWNLOAD_THROTTLED || this.level == Level.STREAMING_THROTTLED || rejectAll();
    }

    /**
     * Stops the daemon sampler immediately.
     */
    @Override
    public void close() {
        this.sampler.shutdownNow();
    }

    /**
     * Ordered pressure states consumed by admission decisions.
     */
    public enum Level {
        /**
         * All request categories may be admitted.
         */
        NORMAL,
        /**
         * New downloads are rejected while realtime streams and buffered work remain available.
         */
        DOWNLOAD_THROTTLED,
        /**
         * New downloads and realtime streams are rejected.
         */
        STREAMING_THROTTLED,
        /**
         * New body materialization waits for heap recovery.
         */
        BUFFERING_THROTTLED,
        /**
         * Every new request is rejected until both memory domains recover.
         */
        EMERGENCY
    }

}

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
package org.miaixz.bus.metrics.bridge;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.metric.Timer;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.metric.indigenous.NativeProvider;

/**
 * Periodically pushes local metric snapshots to bus-cortex via CacheX.
 * <p>
 * Key pattern: {@code metrics:{namespace}:{serviceId}:{metricName}} cortex can then aggregate across instances for
 * cluster-level /metricz.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CortexExporter {

    /**
     * CacheX store used to write metric snapshots.
     */
    private final CacheX<String, String> store;
    /**
     * Cortex namespace used as part of the cache key.
     */
    private final String namespace;
    /**
     * Service identifier included in the cache key.
     */
    private final String serviceId;
    /**
     * Push interval in seconds; also used as the TTL multiplier base.
     */
    private final int intervalSeconds;
    /**
     * Background daemon scheduler for periodic metric pushes.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Creates a CortexExporter that pushes metric snapshots to the given CacheX store.
     *
     * @param store           CacheX store used to write metric snapshots
     * @param namespace       cortex namespace for key scoping
     * @param serviceId       service identifier included in the cache key
     * @param intervalSeconds push interval in seconds; also used as TTL multiplier base
     */
    public CortexExporter(CacheX<String, String> store, String namespace, String serviceId, int intervalSeconds) {
        this.store = store;
        this.namespace = namespace;
        this.serviceId = serviceId;
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, Builder.THREAD_NAME_CORTEX);
            t.setDaemon(true);
            return t;
        });
    }

    /** Starts the background push scheduler. */
    public void start() {
        scheduler.scheduleAtFixedRate(this::push, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        Logger.info(
                "[bus-metrics] CortexExporter started, interval={}s, ns={}, svc={}",
                intervalSeconds,
                namespace,
                serviceId);
    }

    /** Stops the background push scheduler. */
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * Performs a single push of all timer snapshots to the CacheX store. Only runs when the active provider is a
     * {@link NativeProvider}; no-op otherwise.
     */
    public void push() {
        try {
            Provider provider = Metrics.getProvider();
            if (!(provider instanceof NativeProvider)) {
                return;
            }
            for (Timer timer : provider.timers()) {
                TimerSnapshot snap = timer.snapshot();
                String key = Builder.CORTEX_KEY_PREFIX + namespace + ":" + serviceId + ":" + snap.name();
                // Serialize as compact JSON-like string (no external lib)
                String value = serializeSnapshot(snap);
                store.write(key, value, intervalSeconds * Builder.CORTEX_TTL_MULTIPLIER * 1000L);
            }
        } catch (Exception e) {
            Logger.warn("[bus-metrics] CortexExporter push failed: {}", e.getMessage());
        }
    }

    /**
     * Serializes a {@link TimerSnapshot} to a compact JSON string for CacheX storage.
     *
     * @param snap the timer snapshot to serialize
     * @return JSON string representation of the snapshot
     */
    private String serializeSnapshot(TimerSnapshot snap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"count\":").append(snap.count()).append(",\"totalNanos\":").append(snap.totalNanos())
                .append(",\"maxNanos\":").append(snap.maxNanos()).append("}");
        return sb.toString();
    }

}

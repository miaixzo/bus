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
package org.miaixz.bus.cortex.registry;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.health.HttpProber;
import org.miaixz.bus.cortex.health.TcpProber;
import org.miaixz.bus.cortex.Status;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.Instance;

/**
 * Server-side active health probe scheduler.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HealthProbeScheduler {

    /**
     * API registry providing runtime instances to probe.
     */
    private final ApiRegistry apiRegistry;
    /**
     * Watch manager notified when instance health state changes.
     */
    private final WatchManager watchManager;
    /**
     * Interval in milliseconds between probe cycles.
     */
    private final long intervalMs;
    /**
     * Probe timeout in milliseconds configured for health checks.
     */
    private final long timeoutMs;
    /**
     * Single-threaded scheduler running periodic health probes.
     */
    private final ScheduledExecutorService executor;

    /**
     * Creates a HealthProbeScheduler with the given registry and timing parameters.
     *
     * @param apiRegistry  registry used to look up instances to probe
     * @param watchManager watch manager to notify on health state changes
     * @param intervalMs   milliseconds between probe cycles
     * @param timeoutMs    milliseconds before a probe attempt times out
     */
    public HealthProbeScheduler(ApiRegistry apiRegistry, WatchManager watchManager, long intervalMs, long timeoutMs) {
        this.apiRegistry = apiRegistry;
        this.watchManager = watchManager;
        this.intervalMs = intervalMs;
        this.timeoutMs = timeoutMs;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cortex-health-prober");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the periodic health probe scheduler.
     */
    public void start() {
        executor.scheduleAtFixedRate(this::probe, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the health probe scheduler.
     */
    public void stop() {
        executor.shutdown();
    }

    /**
     * Probes all registered instances and publishes their latest health state.
     */
    private void probe() {
        try {
            Vector vector = new Vector();
            vector.setNamespace(Builder.DEFAULT_NAMESPACE);
            vector.setLimit(Integer.MAX_VALUE);
            List<Instance> instances = apiRegistry.queryInstances(Builder.DEFAULT_NAMESPACE, null, null);
            HttpProber httpProber = new HttpProber();
            TcpProber tcpProber = new TcpProber();
            for (Instance instance : instances) {
                try {
                    Prober prober = "tcp".equalsIgnoreCase(instance.getScheme()) ? tcpProber : httpProber;
                    Status result = prober.check(instance);
                    String newState = result.isHealthy() ? "UP" : "DOWN";
                    instance.setState(newState);
                    instance.setHealthy(result.isHealthy() ? 1 : 0);
                    watchManager.notify("instance:" + instance.getFingerprint(), instance);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

}

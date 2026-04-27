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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;
import org.miaixz.bus.cortex.health.HttpProber;
import org.miaixz.bus.cortex.health.TcpProber;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.logger.Logger;

/**
 * Server-side active health probe scheduler.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HealthProbeScheduler implements AutoCloseable, CortexLifecycle, CortexDiagnostics {

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
     * Worker pool executing instance probes concurrently.
     */
    private final ExecutorService workers;
    /**
     * HTTP prober using the configured timeout.
     */
    private final Prober httpProber;
    /**
     * TCP prober using the configured timeout.
     */
    private final Prober tcpProber;
    /**
     * Whether the periodic task has been started.
     */
    private final AtomicBoolean started = new AtomicBoolean();
    /**
     * Whether the scheduler has been permanently stopped.
     */
    private final AtomicBoolean stopped = new AtomicBoolean();
    /**
     * Number of probe cycles submitted by this scheduler.
     */
    private final AtomicLong probeCount = new AtomicLong();
    /**
     * Number of failed instance probe attempts.
     */
    private final AtomicLong failureCount = new AtomicLong();
    /**
     * Last time a probe cycle ran.
     */
    private volatile long lastProbeAt;
    /**
     * Most recent failure message captured by the scheduler.
     */
    private volatile String lastError;

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
        this.httpProber = new HttpProber(timeoutMs);
        this.tcpProber = new TcpProber(timeoutMs);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cortex-health-prober");
            t.setDaemon(true);
            return t;
        });
        this.workers = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Starts the periodic health probe scheduler.
     */
    @Override
    public void start() {
        if (stopped.get()) {
            throw new IllegalStateException("HealthProbeScheduler is stopped");
        }
        if (!started.compareAndSet(false, true)) {
            return;
        }
        executor.scheduleAtFixedRate(this::probe, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the health probe scheduler.
     */
    @Override
    public void stop() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        started.set(false);
        executor.shutdownNow();
        workers.shutdownNow();
        awaitTermination(executor, "health scheduler");
        awaitTermination(workers, "health workers");
    }

    /**
     * Returns whether the periodic health scheduler is active.
     *
     * @return {@code true} when probes are scheduled and the scheduler is not stopped
     */
    @Override
    public boolean isRunning() {
        return started.get() && !stopped.get();
    }

    /**
     * Returns current health-probe diagnostics.
     *
     * @return diagnostics snapshot
     */
    @Override
    public DiagnosticsSnapshot diagnostics() {
        DiagnosticsSnapshot snapshot = new DiagnosticsSnapshot();
        snapshot.setComponent("health-probe");
        snapshot.setStatus(isRunning() ? "running" : "stopped");
        snapshot.setMetrics(
                java.util.Map.of(
                        "running",
                        isRunning(),
                        "probeCount",
                        probeCount.get(),
                        "failureCount",
                        failureCount.get(),
                        "lastProbeAt",
                        lastProbeAt,
                        "intervalMs",
                        intervalMs,
                        "timeoutMs",
                        timeoutMs));
        snapshot.setLastError(lastError);
        snapshot.setUpdatedAt(System.currentTimeMillis());
        return snapshot;
    }

    /**
     * Probes all registered instances and publishes their latest health state.
     */
    private void probe() {
        if (!started.get()) {
            return;
        }
        lastProbeAt = System.currentTimeMillis();
        probeCount.incrementAndGet();
        List<Instance> instances = apiRegistry.queryInstances(null, null, null, null);
        if (instances == null || instances.isEmpty()) {
            return;
        }
        for (Instance instance : instances) {
            workers.execute(() -> probeInstance(instance, httpProber, tcpProber));
        }
    }

    /**
     * Probes one runtime instance, updates its health state, and republishes the refreshed snapshot when needed.
     *
     * @param instance   runtime instance
     * @param httpProber HTTP prober
     * @param tcpProber  TCP prober
     */
    private void probeInstance(Instance instance, Prober httpProber, Prober tcpProber) {
        try {
            Prober prober = "tcp".equalsIgnoreCase(instance.getScheme()) ? tcpProber : httpProber;
            Status result = prober.check(instance);
            String newState = result.isHealthy() ? "UP" : "DOWN";
            Integer newHealthy = result.isHealthy() ? 1 : 0;
            instance.setLastProbeAt(System.currentTimeMillis());
            instance.setLastStatus(result);
            instance.setHealthSource(result.getSource());
            if (Objects.equals(instance.getState(), newState) && Objects.equals(instance.getHealthy(), newHealthy)) {
                return;
            }
            instance.setState(newState);
            instance.setHealthy(newHealthy);
            instance.setStateChangedAt(System.currentTimeMillis());
            ApiAssets service = apiRegistry.refreshByMethodVersion(
                    instance.getNamespace_id(),
                    instance.getApp_id(),
                    instance.getMethod(),
                    instance.getVersion());
            if (service != null) {
                apiRegistry.register(service, instance);
            } else if (watchManager != null) {
                watchManager.notify("instance:" + instance.getFingerprint(), instance);
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            lastError = e.getMessage();
            Logger.warn(
                    "Health probe failed for {}/{}@{}:{}: {}",
                    instance.getMethod(),
                    instance.getVersion(),
                    instance.getHost(),
                    instance.getPort(),
                    e.getMessage());
        }
    }

    /**
     * Awaits termination of one executor during scheduler shutdown.
     *
     * @param executorService executor to await
     * @param name            logical executor name used for diagnostics
     */
    private void awaitTermination(ExecutorService executorService, String name) {
        try {
            if (!executorService.awaitTermination(Math.max(timeoutMs, 1000L), TimeUnit.MILLISECONDS)) {
                Logger.warn("Timed out while stopping {}", name);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes the scheduler by stopping probe submission and shutting down its executors.
     */
    @Override
    public void close() {
        stop();
    }

}

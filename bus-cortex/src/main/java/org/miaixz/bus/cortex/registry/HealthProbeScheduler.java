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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.center.date.Chrono;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.health.HealthInfo;
import org.miaixz.bus.cortex.health.HttpProber;
import org.miaixz.bus.cortex.health.TcpProber;
import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.logger.Logger;

/**
 * Server-side active health probe scheduler.
 *
 * @author Kimi Liu
 */
public class HealthProbeScheduler implements AutoCloseable, CortexLifecycle, CortexDiagnostics {

    /**
     * API registry providing runtime instances to probe.
     */
    private final ApiRegistry apiRegistry;

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
     * Number of failed probe cycles or instance probe attempts.
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
     * @param apiRegistry registry used to look up instances to probe
     * @param intervalMs  milliseconds between probe cycles
     * @param timeoutMs   milliseconds before a probe attempt times out
     */
    public HealthProbeScheduler(ApiRegistry apiRegistry, long intervalMs, long timeoutMs) {
        this.apiRegistry = apiRegistry;
        this.intervalMs = intervalMs;
        this.timeoutMs = timeoutMs;
        this.httpProber = new HttpProber(timeoutMs);
        this.tcpProber = new TcpProber(timeoutMs);
        this.executor = Executors
                .newSingleThreadScheduledExecutor(r -> ThreadKit.newThread(r, "cortex-health-prober", true));
        this.workers = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Starts the periodic health probe scheduler.
     *
     * @throws IllegalStateException if the scheduler has already been stopped
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
                Map.of(
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
        snapshot.setUpdatedAt(DateKit.current());
        return snapshot;
    }

    /**
     * Probes every page of registered services and isolates cycle failures from future scheduled executions.
     */
    private void probe() {
        if (!isRunning()) {
            return;
        }
        lastProbeAt = DateKit.current();
        probeCount.incrementAndGet();
        try {
            Map<List<String>, Map<ApiAssets, Instance>> processes = new LinkedHashMap<>();
            Vector vector = Vector.builder().type(Type.API.key()).build();
            while (isRunning()) {
                List<ApiAssets> services = apiRegistry.query(vector);
                if (services.isEmpty()) {
                    break;
                }
                for (ApiAssets service : services) {
                    if (!isRunning()) {
                        return;
                    }
                    for (Instance instance : apiRegistry.queryInstances(service)) {
                        if (!instance.isActive() || Status.Health.MAINTENANCE.matches(instance.getStatus())
                                || !Integer.valueOf(1).equals(service.getStatus()))
                            continue;
                        try {
                            Instance expected = BeanKit.toBean(instance, Instance.class);
                            Instance target = probeTarget(expected);
                            HealthInfo health = target.healthView();
                            List<String> identity = List.of(
                                    Objects.toString(expected.getTenant_id(), ""),
                                    expected.getApp_id(),
                                    Objects.toString(expected.getId(), ""),
                                    expected.getChecksum(),
                                    Objects.toString(health.getSession(), ""),
                                    target.getHost(),
                                    target.getPort().toString(),
                                    health.getScheme(),
                                    Protocol.TCP.name.equals(health.getScheme()) ? "" : health.getPath());
                            processes.computeIfAbsent(identity, ignored -> new LinkedHashMap<>())
                                    .put(RegistryAssets.api(service), expected);
                        } catch (IllegalArgumentException invalid) {
                            failureCount.incrementAndGet();
                            lastError = "Invalid runtime probe configuration";
                            Logger.warn(
                                    false,
                                    "Cortex",
                                    "Health probe configuration rejected for instance: {}",
                                    instance.getId());
                        }
                    }
                }
                if (services.size() < vector.getLimit()) {
                    break;
                }
                vector.setOffset(Math.addExact(vector.getOffset(), services.size()));
            }
            for (Map<ApiAssets, Instance> registrations : processes.values()) {
                if (!isRunning()) {
                    return;
                }
                workers.execute(() -> probeInstance(registrations, httpProber, tcpProber));
            }
        } catch (Exception e) {
            if (!isRunning()) {
                return;
            }
            failureCount.incrementAndGet();
            lastError = e.getMessage();
            Logger.warn(false, "Cortex", e, "Health probe cycle failed: {}", e.getMessage());
        }
    }

    /**
     * Probes one runtime instance, updates its health state, and republishes the refreshed snapshot when needed.
     *
     * @param registrations existing service definitions and runtime snapshots for one shared process
     * @param httpProber    HTTP prober
     * @param tcpProber     TCP prober
     */
    private void probeInstance(Map<ApiAssets, Instance> registrations, Prober httpProber, Prober tcpProber) {
        Instance sample = registrations.values().stream()
                .filter(instance -> instance.isActive() && !Status.Health.MAINTENANCE.matches(instance.getStatus()))
                .findFirst().orElse(null);
        if (sample == null)
            return;
        try {
            Instance target = probeTarget(sample);
            Prober prober = Protocol.TCP.name.equals(target.healthView().getScheme()) ? tcpProber : httpProber;
            long started = DateKit.current();
            Status result = prober.check(target);
            long finished = DateKit.current();
            for (Map.Entry<ApiAssets, Instance> registration : registrations.entrySet()) {
                if (!isRunning())
                    return;
                try {
                    apiRegistry.updateHealth(registration.getKey(), registration.getValue(), result, started, finished);
                } catch (Exception failure) {
                    failureCount.incrementAndGet();
                    lastError = failure.getMessage();
                    Logger.warn(
                            false,
                            "Cortex",
                            "Health observation rejected for service {}: {}",
                            registration.getKey().getId(),
                            failure.getMessage());
                }
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            lastError = e.getMessage();
            Logger.warn(
                    false,
                    "Cortex",
                    "Health probe failed for {}@{}:{}: {}",
                    sample.getApp_id(),
                    sample.getHost(),
                    sample.getPort(),
                    e.getMessage());
        }
    }

    /**
     * Builds a detached effective probe target without rewriting the saved configuration or session. HTTP defaults
     * match the existing prober, including port 80 when no explicit port is present; TCP ignores the HTTP path.
     *
     * @param instance captured runtime snapshot
     * @return normalized target used both for grouping and actual I/O
     * @throws IllegalArgumentException if the endpoint or protocol cannot be probed
     */
    private Instance probeTarget(Instance instance) {
        Instance target = BeanKit.toBean(instance, Instance.class);
        HealthInfo health = target.healthView();
        String scheme = health.getScheme() == null ? Protocol.HTTP.name
                : StringKit.toLowerCase(health.getScheme().strip());
        if (!List.of(Protocol.HTTP.name, Protocol.HTTPS.name, Protocol.TCP.name).contains(scheme)
                || target.getHost() == null || target.getHost().isBlank())
            throw new IllegalArgumentException("Invalid probe endpoint");
        if (target.getPort() == null && !Protocol.TCP.name.equals(scheme))
            target.setPort(Port._80.getPort());
        if (target.getPort() == null || target.getPort() < 1 || target.getPort() > 65535)
            throw new IllegalArgumentException("Invalid probe port");
        health.setScheme(scheme);
        health.setPath(
                Protocol.TCP.name.equals(scheme) ? null : health.getPath() == null ? "/health" : health.getPath());
        target.health(health);
        return target;
    }

    /**
     * Awaits termination of one executor during scheduler shutdown.
     *
     * @param executorService executor to await
     * @param name            logical executor name used for diagnostics
     */
    private void awaitTermination(ExecutorService executorService, String name) {
        try {
            if (!executorService
                    .awaitTermination(Math.max(timeoutMs, Chrono.SECOND.getMillis()), TimeUnit.MILLISECONDS)) {
                Logger.warn(false, "Cortex", "Health timed out while stopping {}", name);
            }
        } catch (InterruptedException e) {
            Logger.warn(
                    false,
                    "Cortex",
                    e,
                    "Cortex operation failed: provider={}, recoverable={}, exception={}",
                    "HealthProbeScheduler",
                    false,
                    e.getClass().getSimpleName());
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

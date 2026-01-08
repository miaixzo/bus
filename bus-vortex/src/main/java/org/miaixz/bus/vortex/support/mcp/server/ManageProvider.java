/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support.mcp.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.health.Platform;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OperatingSystem;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.magic.Metrics;
import org.miaixz.bus.vortex.provider.MetricsProvider;
import org.miaixz.bus.vortex.provider.ProcessProvider;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A default provider that manages services as local OS processes and monitors their performance.
 * <p>
 * This provider uses Java's {@link ProcessBuilder} to manage the lifecycle of processes. It also implements the
 * {@link MetricsProvider} interface to provide CPU and memory metrics.
 * <p>
 * <strong>Note:</strong> Performance monitoring requires the optional {@code com.github.oshi:oshi-core} dependency to
 * be present on the classpath. If OSHI is not available, performance metrics will be returned as zero.
 * <p>
 * This implementation is suitable for single-node deployments. For distributed or containerized environments, a custom
 * provider (e.g., for Docker or Kubernetes) would be required.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ManageProvider implements ProcessProvider, MetricsProvider {

    private final ConcurrentMap<String, Process> processMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code ManageProvider}.
     * <p>
     * It checks for the availability of the OSHI library on the classpath at construction time. If OSHI is not found, a
     * warning is logged, and performance monitoring will be disabled.
     */
    public ManageProvider() {

    }

    /**
     * Starts a local process based on the provided {@link Assets} configuration.
     * <p>
     * This method is idempotent. If a process for the given asset is already running, it returns a handle to the
     * existing process. Otherwise, it performs the following steps:
     * <ol>
     * <li>Uses {@link ProcessBuilder} to create and start a new OS process.</li>
     * <li>Stores the {@link Process} handle in an internal map.</li>
     * <li>Spawns a background thread to consume the process's stdout and log it, preventing the process from
     * blocking.</li>
     * </ol>
     * All blocking operations are performed on a bounded elastic scheduler.
     *
     * @param assets The configuration of the service to start.
     * @return A {@code Mono} emitting the {@link Process} handle upon successful start.
     */
    @Override
    public Mono<Process> start(Assets assets) {
        return Mono.fromCallable(() -> {
            Assert.notNull(assets, "Assets cannot be null");
            String serviceId = assets.getId();

            Process existingProcess = processMap.get(serviceId);
            if (existingProcess != null && existingProcess.isAlive()) {
                Logger.info("Service '{}' is already running.", serviceId);
                return existingProcess;
            }

            String commandString = assets.getCommand();
            if (StringKit.isBlank(commandString)) {
                throw new IllegalArgumentException("Command for asset '" + serviceId + "' cannot be empty.");
            }
            List<String> commandList = Arrays.asList(commandString.split("\\s+"));
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);

            String envJson = assets.getEnv();
            if (StringKit.isNotBlank(envJson)) {
                try {
                    Map<String, Object> envMap = JsonKit.toMap(envJson);
                    if (envMap != null) {
                        envMap.forEach((key, value) -> processBuilder.environment().put(key, String.valueOf(value)));
                    }
                } catch (Exception e) {
                    Logger.error(
                            "Failed to parse environment variables JSON for service '{}': {}",
                            serviceId,
                            envJson,
                            e);
                    throw new IllegalArgumentException(
                            "Invalid environment variables JSON for asset '" + serviceId + "'", e);
                }
            }

            processBuilder.redirectErrorStream(true);

            try {
                Logger.info("Starting service '{}' with command: {}", serviceId, commandString);
                Process process = processBuilder.start();
                processMap.put(serviceId, process);

                Schedulers.boundedElastic().schedule(() -> {
                    try (var reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Logger.trace("[Process-{}]: {}", serviceId, line);
                        }
                    } catch (IOException e) {
                        Logger.warn("Error reading output for service '{}': {}", serviceId, e.getMessage());
                    }
                });
                return process;
            } catch (IOException e) {
                Logger.error("Failed to start service '{}'", serviceId, e);
                throw new RuntimeException("Failed to start service: " + serviceId, e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Stops a local process associated with the given {@link Assets}.
     * <p>
     * This method implements a graceful shutdown sequence:
     * <ol>
     * <li>It first sends a {@code SIGTERM} signal to the process.</li>
     * <li>It waits for a short period (5 seconds).</li>
     * <li>If the process has not terminated, it sends a {@code SIGKILL} signal to force its termination.</li>
     * </ol>
     *
     * @param assets The configuration of the service to stop.
     * @return A {@code Mono<Void>} that completes when the process has been stopped.
     */
    @Override
    public Mono<Void> stop(Assets assets) {
        return Mono.fromRunnable(() -> {
            Assert.notNull(assets, "Assets cannot be null");
            String serviceId = assets.getId();
            Process process = processMap.get(serviceId);

            if (process != null && process.isAlive()) {
                Logger.info("Stopping service '{}'", serviceId);
                process.destroy();
                try {
                    if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        Logger.warn("Service '{}' did not terminate gracefully. Forcing shutdown.", serviceId);
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.error("Interrupted while waiting for service '{}' to stop.", serviceId, e);
                    process.destroyForcibly();
                }
                processMap.remove(serviceId);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Restarts a local process by sequentially stopping and then starting it.
     *
     * @param assets The configuration of the service to restart.
     * @return A {@code Mono} emitting the new {@link Process} handle upon successful restart.
     */
    @Override
    public Mono<Process> restart(Assets assets) {
        return stop(assets).then(start(assets));
    }

    /**
     * Retrieves the current status of the service process.
     *
     * @param assets The configuration of the service to check.
     * @return A {@code Mono} emitting the current {@link EnumValue.Lifecycle} status (e.g., RUNNING, STOPPED).
     */
    @Override
    public Mono<EnumValue.Lifecycle> getStatus(Assets assets) {
        return Mono.fromSupplier(() -> {
            Assert.notNull(assets, "Assets cannot be null");
            Process process = processMap.get(assets.getId());
            if (process == null) {
                return EnumValue.Lifecycle.STOPPED;
            }
            return process.isAlive() ? EnumValue.Lifecycle.RUNNING : EnumValue.Lifecycle.STOPPED;
        });
    }

    /**
     * Retrieves performance metrics for a given service process using the OSHI library.
     *
     * @param serviceId The unique ID of the service.
     * @return A {@code Mono} emitting the {@link Metrics}. If OSHI is unavailable or the process is not found, it
     *         returns metrics with zero values.
     */
    @Override
    public Mono<Metrics> getMetrics(String serviceId) {
        return Mono.fromCallable(() -> {
            Process process = processMap.get(serviceId);
            if (process == null || !process.isAlive()) {
                return Metrics.builder().cpu(0).memory(0).build();
            }

            long pid = process.pid();
            OperatingSystem os = Platform.INSTANCE.getOperatingSystem();
            OSProcess osProcess = os.getProcess((int) pid);

            if (osProcess == null) {
                return Metrics.builder().cpu(0).memory(0).build();
            }

            // Calculate average CPU load over the process uptime
            double cpuLoad = 100d * (osProcess.getKernelTime() + osProcess.getUserTime()) / osProcess.getUpTime();
            long memoryUsage = osProcess.getResidentSetSize();

            return Metrics.builder().cpu(cpuLoad).memory(memoryUsage).build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

}

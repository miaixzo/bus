/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.support.mcp;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.springframework.context.SmartLifecycle;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Manages the lifecycle of all MCP (Model Context Protocol) clients. This service is responsible for initializing,
 * caching, and destroying all assets that operate over the MCP protocol. It also includes dynamic
 * registration/deregistration, health checking, and self-healing capabilities. Implements SmartLifecycle to integrate
 * with Spring's application lifecycle.
 */
public class McpService implements SmartLifecycle {

    /**
     * The interval in seconds for performing health checks.
     */
    private static final long HEALTH_CHECK_INTERVAL_SECONDS = 30;
    /**
     * The separator used to create unique tool names by prefixing the service name.
     */
    private static final String TOOL_NAME_SEPARATOR = "::";
    private final AssetsRegistry registry;
    /**
     * A thread-safe cache holding all active and initialized MCP client instances. The key is the service name (asset
     * name), and the value is the corresponding McpClient instance.
     */
    private final Map<String, McpClient> clientCache = new ConcurrentHashMap<>();
    /**
     * A map to store the original Assets configuration for each client. This is needed for re-initialization during
     * self-healing.
     */
    private final Map<String, Assets> assetsCache = new ConcurrentHashMap<>();
    /**
     * Scheduler for periodic health checks. Uses a single thread to avoid concurrent health check executions.
     */
    private final ScheduledExecutorService healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mcp-health-check-scheduler");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructs the service with a dependency on the AssetsRegistry.
     * 
     * @param registry The registry providing access to all asset configurations.
     */
    public McpService(AssetsRegistry registry) {
        this.registry = registry;
    }

    /**
     * Starts the McpService. This method is called by Spring when the application context is started. It initializes
     * all MCP clients and starts the periodic health check task.
     */
    @Override
    public void start() {
        Logger.info("MCP Service is starting...");
        List<Assets> mcpAssets = this.registry.getAll().stream().filter(a -> a.getMode() >= 3) // Modes 3,4,5,6 are MCP
                                                                                               // related
                .toList();

        Logger.info("Found {} MCP assets to initialize.", mcpAssets.size());

        for (Assets asset : mcpAssets) {
            try {
                // Use registerClient for initial setup as well
                register(asset).block(); // Block during init to ensure readiness
            } catch (Exception e) {
                Logger.error("Failed to initialize MCP client for asset: {}", asset.getName(), e);
            }
        }
        startHealthCheckTask();
        running.set(true);
        Logger.info("MCP Service started.");
    }

    /**
     * Stops the McpService. This method is called by Spring when the application context is stopped. It shuts down all
     * MCP clients and stops the periodic health check task.
     */
    @Override
    public void stop() {
        Logger.info("MCP Service is stopping...");
        healthCheckScheduler.shutdownNow(); // Stop the health check task immediately
        clientCache.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                Logger.error("Error closing a MCP client.", e);
            }
        });
        clientCache.clear();
        assetsCache.clear();
        running.set(false);
        Logger.info("MCP Service stopped.");
    }

    /**
     * Checks if the McpService is currently running.
     * 
     * @return true if the service is running, false otherwise.
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Returns the phase of this SmartLifecycle component. Lower values indicate earlier startup and later shutdown. We
     * set a high value to ensure it starts after most other beans and stops before them.
     * 
     * @return The phase value.
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * Dynamically registers and initializes a new MCP client. If a client with the same name already exists, it will be
     * replaced.
     * 
     * @param asset The Assets configuration for the client to register.
     * @return A Mono that completes when the client is successfully registered and initialized.
     */
    public Mono<Void> register(Assets asset) {
        // Close existing client if any
        McpClient existingClient = clientCache.remove(asset.getName());
        if (existingClient != null) {
            Logger.info("Closing existing client for '{}' before re-registration.", asset.getName());
            existingClient.close();
        }

        return Mono.<Void>defer(() -> {
            try {
                McpClient client;
                switch (asset.getMode()) { // Use asset.getMode() directly
                    case 3: // SSE
                        client = new SseClient(asset);
                        break;

                    case 4: // STDIO
                        client = new StdioClient(asset);
                        break;

                    case 5: // OPENAPI
                        client = new OpenApiClient(asset);
                        break;

                    case 6: // STREAMABLE-HTTP
                        client = new StreamableHttpClient(asset);
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown or unsupported MCP mode: " + asset.getMode());
                }

                return client.initialize().doOnSuccess(v -> {
                    clientCache.put(asset.getName(), client);
                    assetsCache.put(asset.getName(), asset); // Store original asset for self-healing
                    Logger.info(
                            "Client for '{}' (mode {}) registered and initialized successfully.",
                            asset.getName(),
                            asset.getMode());
                }).doOnError(e -> {
                    Logger.error("Error registering client for asset '{}': {}", asset.getName(), e.getMessage());
                    // Ensure client is closed if initialization fails
                    client.close();
                }).then(); // This ensures Mono<Void>
            } catch (Exception e) {
                Logger.error("Error creating client for asset '{}': {}", asset.getName(), e.getMessage());
                return Mono.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()); // Execute client creation/init on a dedicated scheduler
    }

    /**
     * Dynamically unregisters and closes an existing MCP client.
     * 
     * @param serviceName The name of the service (asset name) to unregister.
     * @return A Mono that completes when the client is successfully unregistered and closed.
     */
    public Mono<Void> destroy(String serviceName) {
        return Mono.<Void>fromRunnable(() -> {
            McpClient client = clientCache.remove(serviceName);
            assetsCache.remove(serviceName);
            if (client != null) {
                Logger.info("Closing client for '{}' due to unregistration.", serviceName);
                client.close();
            } else {
                Logger.warn("Client '{}' not found in cache for unregistration.", serviceName);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Dynamically updates an existing MCP client. This is achieved by unregistering the old client and registering a
     * new one with the updated configuration.
     * 
     * @param asset The updated Assets configuration for the client.
     * @return A Mono that completes when the client is successfully updated.
     */
    public Mono<Void> refresh(Assets asset) {
        Logger.info("Attempting to update client '{}'.", asset.getName());
        return destroy(asset.getName()).then(register(asset))
                .doOnSuccess(v -> Logger.info("Client '{}' updated successfully.", asset.getName()))
                .doOnError(e -> Logger.error("Failed to update client '{}': {}", asset.getName(), e.getMessage()));
    }

    /**
     * Starts the periodic health check task for all managed MCP clients.
     */
    private void startHealthCheckTask() {
        healthCheckScheduler.scheduleAtFixedRate(() -> {
            Logger.debug("Starting periodic health check for {} MCP clients.", clientCache.size());
            Flux.fromIterable(clientCache.entrySet()).flatMap(entry -> {
                String serviceName = entry.getKey();
                McpClient client = entry.getValue();
                return client.isHealthy().doOnNext(isHealthy -> {
                    if (!isHealthy) {
                        Logger.warn("Client '{}' is unhealthy. Attempting self-healing.", serviceName);
                        // Trigger self-healing: unregister and re-register
                        Assets assetToHeal = assetsCache.get(serviceName);
                        if (assetToHeal != null) {
                            destroy(serviceName).then(register(assetToHeal)).subscribe(
                                    null,
                                    e -> Logger.error("Self-healing failed for '{}': {}", serviceName, e.getMessage()));
                        } else {
                            Logger.error("Cannot self-heal client '{}': original asset config not found.", serviceName);
                        }
                    }
                }).onErrorResume(e -> {
                    Logger.error("Health check for client '{}' failed with error: {}", serviceName, e.getMessage());
                    // Treat error during health check as unhealthy and attempt self-healing
                    Assets assetToHeal = assetsCache.get(serviceName);
                    if (assetToHeal != null) {
                        destroy(serviceName).then(register(assetToHeal)).subscribe(
                                null,
                                err -> Logger.error("Self-healing failed for '{}': {}", serviceName, err.getMessage()));
                    } else {
                        Logger.error("Cannot self-heal client '{}': original asset config not found.", serviceName);
                    }
                    return Mono.just(false); // Mark as unhealthy
                });
            }).subscribeOn(Schedulers.boundedElastic()) // Run health checks concurrently on a different scheduler
                    .subscribe(); // Subscribe to trigger the reactive flow
        }, HEALTH_CHECK_INTERVAL_SECONDS, HEALTH_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Retrieves an initialized MCP client instance by its service name.
     * 
     * @param serviceName The name of the service (asset name).
     * @return McpClient instance, or null if not found or not ready.
     */
    public McpClient getMcp(String serviceName) {
        return clientCache.get(serviceName);
    }

    /**
     * Retrieves all active and initialized MCP client instances.
     * 
     * @return A collection of all McpClient instances.
     */
    public Collection<McpClient> getAll() {
        return clientCache.values();
    }

    /**
     * Aggregates and returns a list of all tools from all active MCP clients. Each tool's name is prefixed with its
     * service name to ensure uniqueness.
     * 
     * @return A Mono emitting a List of all available Tools.
     */
    public Mono<List<Tool>> getTools() {
        return Mono.just(clientCache.entrySet().stream().flatMap(entry -> {
            String serviceName = entry.getKey();
            McpClient client = entry.getValue();
            return client.getTools().stream().map(
                    tool -> new Tool(serviceName + TOOL_NAME_SEPARATOR + tool.getName(), tool.getDescription(),
                            tool.getInputSchema()));
        }).collect(Collectors.toList()));
    }

    /**
     * Retrieves the health status of a specific MCP client.
     * 
     * @param serviceName The name of the service.
     * @return A Mono emitting true if the client is healthy, false if unhealthy or not found.
     */
    public Mono<Boolean> getHealth(String serviceName) {
        McpClient client = clientCache.get(serviceName);
        if (client == null) {
            return Mono.just(false); // Client not found is considered unhealthy
        }
        return client.isHealthy().onErrorReturn(false); // Treat any error during health check as unhealthy
    }

    /**
     * Retrieves the overall health status of all managed MCP clients.
     * 
     * @return A Mono emitting true if all clients are healthy, false otherwise.
     */
    public Mono<Boolean> getOverall() {
        if (clientCache.isEmpty()) {
            return Mono.just(true); // No clients managed, so considered healthy
        }
        return Flux.fromIterable(clientCache.values()).flatMap(McpClient::isHealthy).all(isHealthy -> isHealthy);
    }

}

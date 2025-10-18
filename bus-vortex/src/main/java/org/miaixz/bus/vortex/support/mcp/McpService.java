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

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.provider.ProcessProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.support.mcp.client.McpClient;
import org.miaixz.bus.vortex.support.mcp.client.StdioClient;
import org.springframework.context.SmartLifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A service that manages the lifecycle and provides access to all MCP (Model Context Protocol) clients.
 * <p>
 * This service acts as a high-level coordinator and a client registry. It implements {@link SmartLifecycle} to hook
 * into the Spring application lifecycle. Its primary responsibilities are:
 * <ul>
 * <li>On startup, it finds all MCP-related assets and uses a {@link ProcessProvider} to start their underlying
 * processes.</li>
 * <li>For each successfully started process, it creates and initializes a corresponding {@link McpClient} (e.g.,
 * {@link StdioClient}) to handle communication.</li>
 * <li>It maintains a cache of all active clients, making them available to other services like
 * {@link org.miaixz.bus.vortex.support.McpRouter}.</li>
 * <li>On shutdown, it gracefully closes all clients and stops all managed processes.</li>
 * </ul>
 * This design separates the concern of process lifecycle management (delegated to {@code ProcessProvider}) from the
 * concern of protocol communication (encapsulated in {@code McpClient}).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class McpService implements SmartLifecycle {

    private static final String TOOL_NAME_SEPARATOR = "::";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AssetsRegistry assetsRegistry;
    private final ProcessProvider processProvider;
    private final Map<String, McpClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Constructs the service with its required dependencies.
     *
     * @param assetsRegistry  The registry providing access to all API asset configurations.
     * @param processProvider The provider responsible for managing the lifecycle of external processes.
     */
    public McpService(AssetsRegistry assetsRegistry, ProcessProvider processProvider) {
        this.assetsRegistry = assetsRegistry;
        this.processProvider = processProvider;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            Logger.info("MCP Service is starting...");
            List<Assets> mcpAssets = this.assetsRegistry.getAll().stream().filter(a -> a.getMode() >= 3) // Modes 3, 4,
                                                                                                         // 5, 6 are MCP
                                                                                                         // related
                    .toList();

            Logger.info("Found {} MCP assets to initialize.", mcpAssets.size());

            Flux.fromIterable(mcpAssets).flatMap(this::startAndRegisterClient)
                    .doOnError(e -> Logger.error("Error during MCP service startup.", e)).subscribe();

            Logger.info("MCP Service startup process initiated for all clients.");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            Logger.info("MCP Service is stopping...");

            // Stop all underlying processes via the provider
            Flux.fromIterable(assetsRegistry.getAll().stream().filter(a -> a.getMode() >= 3).toList())
                    .flatMap(processProvider::stop)
                    .doOnError(e -> Logger.error("Error during MCP service shutdown.", e)).blockLast(); // Block to
                                                                                                        // ensure
                                                                                                        // processes are
                                                                                                        // stopped
                                                                                                        // before the
                                                                                                        // app exits

            // Close all client connections
            clientCache.values().forEach(McpClient::close);
            clientCache.clear();

            Logger.info("MCP Service stopped.");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private Mono<Void> startAndRegisterClient(Assets asset) {
        return processProvider.start(asset).flatMap(process -> {
            McpClient client = createClientForAsset(asset, process);
            return client.initialize().doOnSuccess(v -> {
                clientCache.put(asset.getId(), client);
                Logger.info("Client for '{}' registered and initialized successfully.", asset.getName());
            });
        }).doOnError(e -> Logger.error("Failed to start or register client for asset '{}'", asset.getName(), e)).then();
    }

    private McpClient createClientForAsset(Assets asset, Process process) {
        return switch (asset.getMode()) {
            // For now, we only have a StdioClient implementation.
            // This can be extended to support other client types (SSE, HTTP) in the future.
            case 4 -> new StdioClient(asset, process);
            default -> throw new IllegalArgumentException(
                    "Unsupported MCP mode for client creation: " + asset.getMode());
        };
    }

    /**
     * Retrieves an initialized MCP client instance by its service ID.
     *
     * @param serviceId The unique ID.
     * @return The {@link McpClient} instance, or {@code null} if not found or not ready.
     */
    public McpClient getMcp(String serviceId) {
        return clientCache.get(serviceId);
    }

    /**
     * Retrieves all active and initialized MCP client instances.
     *
     * @return A collection of all active {@link McpClient} instances.
     */
    public Collection<McpClient> getAll() {
        return clientCache.values();
    }

    /**
     * Aggregates and returns a list of all tools from all active MCP clients.
     * <p>
     * Each tool's name is prefixed with its service name and a separator (e.g., "serviceName::toolName") to ensure
     * uniqueness across all services.
     *
     * @return A {@code Mono} emitting a List of all available {@link Tool}s.
     */
    public Mono<List<Tool>> getTools() {
        return Mono.fromCallable(() -> clientCache.entrySet().stream().flatMap(entry -> {
            String serviceName = entry.getKey();
            McpClient client = entry.getValue();
            return client.getTools().stream().map(
                    tool -> new Tool(serviceName + TOOL_NAME_SEPARATOR + tool.getName(), tool.getDescription(),
                            tool.getInputSchema()));
        }).collect(Collectors.toList()));
    }

}

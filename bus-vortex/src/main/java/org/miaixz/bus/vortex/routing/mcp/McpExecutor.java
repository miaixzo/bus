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
package org.miaixz.bus.vortex.routing.mcp;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.RegistryAssets;
import org.miaixz.bus.cortex.registry.RegistryIdentity;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.JsonProvider;
import org.miaixz.bus.vortex.provider.ProcessProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.routing.Coordinator;
import org.miaixz.bus.vortex.routing.McpRouter;
import org.miaixz.bus.vortex.routing.mcp.client.McpClient;
import org.miaixz.bus.vortex.routing.mcp.client.SseClient;
import org.miaixz.bus.vortex.routing.mcp.client.StreamableHttpClient;
import org.miaixz.bus.vortex.routing.mcp.client.StdioClient;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * An executor that manages the lifecycle and provides access to all MCP (Model Context Protocol) clients.
 * <p>
 * This executor acts as a high-level coordinator and a client registry. It implements {@link SmartLifecycle} to hook
 * into the Spring application lifecycle. Its primary responsibilities are:
 * <ul>
 * <li>On startup, it finds all MCP-related assets and uses a {@link ProcessProvider} to start their underlying
 * processes.</li>
 * <li>For each successfully started process, it creates and initializes a corresponding {@link McpClient} (e.g.,
 * {@link StdioClient}) to handle communication.</li>
 * <li>It maintains a cache of all active clients, making them available to other executors like {@link McpRouter}.</li>
 * <li>On shutdown, it gracefully closes all clients and stops all managed processes.</li>
 * </ul>
 * This design separates the concern of process lifecycle management (delegated to {@code ProcessProvider}) from the
 * concern of protocol communication (encapsulated in {@code McpClient}).
 * <p>
 * Generic type parameters: {@code Executor<Void, Void>}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class McpExecutor extends Coordinator<Void, Void> implements SmartLifecycle {

    /**
     * The separator used to prefix tool names with their service ID for uniqueness.
     */
    private static final String TOOL_NAME_SEPARATOR = "::";

    /**
     * An atomic flag to track the running state of the service, ensuring idempotent start/stop.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * The in-memory registry providing access to runtime route assets.
     */
    private final AssetsRegistry assetsRegistry;
    /**
     * The provider responsible for starting, stopping, and managing external system processes.
     */
    private final ProcessProvider processProvider;
    /**
     * A thread-safe cache holding all active and initialized MCP clients, keyed by their service ID (asset ID).
     */
    private final Map<String, McpClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Constructs the executor with its required dependencies.
     *
     * @param assetsRegistry  The in-memory registry providing access to runtime route assets.
     * @param processProvider The provider responsible for managing the lifecycle of external processes.
     */
    public McpExecutor(AssetsRegistry assetsRegistry, ProcessProvider processProvider) {
        this.assetsRegistry = assetsRegistry;
        this.processProvider = processProvider;
    }

    /**
     * Executes an MCP request using the provided context.
     * <p>
     * This method is required by the {@link org.miaixz.bus.vortex.Executor} interface. For MCP executors, lifecycle is
     * managed through {@link SmartLifecycle} methods {@link #start()} and {@link #stop()}.
     *
     * @param context The request context
     * @param input   Optional input parameter (may be null for MCP)
     * @return A Mono completing successfully (MCP uses SmartLifecycle for management)
     */
    @Override
    public Mono<Void> execute(Context context, Void input) {
        this.start();
        return Mono.fromRunnable(() -> Logger.debug("MCP execute called for context: {}, object: {}", context, input))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()).then(Mono.empty());
    }

    /**
     * Starts the MCP service and initializes all MCP clients.
     * <p>
     * This method is called by Spring during application startup. It:
     * <ol>
     * <li>Finds all MCP assets already registered in runtime memory</li>
     * <li>Starts the external process for each asset using ProcessProvider</li>
     * <li>Creates and initializes the corresponding MCP client for each process</li>
     * <li>Registers each successfully initialized client in the client cache</li>
     * </ol>
     * The startup process is asynchronous and non-blocking.
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            Logger.info("MCP Service is starting...");

            Mono.fromCallable(() -> this.assetsRegistry.getAll().stream().filter(McpExecutor::isMcpAsset).toList())
                    .subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable)
                    .flatMap(this::startAndRegisterClient)
                    .doOnError(e -> Logger.error("Error during MCP service startup.", e)).subscribe();

            Logger.info("MCP Service startup process initiated for all clients.");
        }
    }

    /**
     * Stops the MCP service and gracefully shuts down all MCP clients and processes.
     * <p>
     * This method is called by Spring during application shutdown. It:
     * <ol>
     * <li>Stops all external processes managed by ProcessProvider</li>
     * <li>Closes all MCP client connections</li>
     * <li>Clears the client cache</li>
     * </ol>
     * The shutdown process has a 5-second timeout to prevent indefinite blocking.
     */
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            Logger.info("MCP Service is stopping...");

            Mono<List<Assets>> mcpAssets = Mono
                    .fromCallable(() -> this.assetsRegistry.getAll().stream().filter(McpExecutor::isMcpAsset).toList())
                    .subscribeOn(Schedulers.boundedElastic());

            Mono<Void> stopProcesses = mcpAssets.flatMapMany(Flux::fromIterable)
                    .filter(asset -> "stdio".equals(resolveMcpTransport(asset))).flatMap(processProvider::stop)
                    .doOnError(e -> Logger.error("Error stopping MCP process.", e)).then();

            Mono<Void> closeClients = Flux.fromIterable(clientCache.values())
                    .flatMap(
                            client -> Mono.fromRunnable(client::close).subscribeOn(Schedulers.boundedElastic())
                                    .doOnError(e -> Logger.error("Error closing MCP client.", e)))
                    .then();

            Mono.when(stopProcesses, closeClients).doOnError(e -> Logger.error("Error during MCP service shutdown.", e))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .doOnError(e -> Logger.warn("MCP service shutdown timed out after 5 seconds")).block();

            clientCache.clear();
            Logger.info("MCP Service stopped.");
        }
    }

    /**
     * Checks whether the MCP service is currently running.
     * <p>
     * This method is called by Spring to check the lifecycle status.
     *
     * @return {@code true} if the service is running, {@code false} otherwise
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Assumes processProvider.start() and client.initialize() are already reactive (return Mono). If not, they must
     * also be wrapped in Mono.fromCallable().subscribeOn().
     */
    private Mono<Void> startAndRegisterClient(Assets asset) {
        String transport = resolveMcpTransport(asset);
        Mono<McpClient> clientMono;
        if ("stdio".equals(transport)) {
            clientMono = processProvider.start(asset).map(process -> createClientForAsset(asset, process));
        } else {
            clientMono = Mono.fromSupplier(() -> createClientForAsset(asset, null));
        }
        return clientMono.flatMap(client -> client.initialize().doOnSuccess(v -> {
            clientCache.put(asset.getId(), client);
            Logger.info("Client for '{}' registered and initialized successfully.", asset.getName());
        })).doOnError(e -> Logger.error("Failed to start or register client for asset '{}'", asset.getName(), e))
                .then();
    }

    /**
     * Creates the correct MCP client implementation for a runtime asset.
     *
     * @param asset   runtime MCP asset
     * @param process process handle for stdio transports, otherwise {@code null}
     * @return initialized client instance before protocol initialization
     */
    private McpClient createClientForAsset(Assets asset, Process process) {
        return switch (resolveMcpTransport(asset)) {
            case "stdio" -> new StdioClient(asset, process);
            case "streamable-http", "http" -> new StreamableHttpClient(asset);
            case "sse" -> new SseClient(asset);
            default -> throw new IllegalArgumentException(
                    "Unsupported MCP transport for client creation: " + resolveMcpTransport(asset));
        };
    }

    /**
     * Determines whether a runtime asset belongs to the MCP registry view.
     *
     * @param asset runtime asset candidate
     * @return {@code true} when the asset is non-null and its type is {@link Type#MCP}
     */
    private static boolean isMcpAsset(Assets asset) {
        return asset != null && Type.MCP.is(RegistryIdentity.type(asset.getType()));
    }

    /**
     * Resolves the effective MCP transport for a runtime asset.
     * <p>
     * The transport is read from encoded Cortex metadata first. When no explicit transport is available, the runtime
     * falls back to {@code sse}.
     *
     * @param asset runtime MCP asset
     * @return normalized transport name used for client selection
     */
    private static String resolveMcpTransport(Assets asset) {
        String transport = RegistryAssets.mcp(asset).transport();
        if (StringKit.isNotEmpty(transport)) {
            return transport.toLowerCase(Locale.ROOT);
        }
        return "sse";
    }

    /**
     * Retrieves an initialized MCP client instance by its service ID. This is a non-blocking in-memory cache lookup.
     *
     * @param serviceId The unique ID.
     * @return The {@link McpClient} instance, or {@code null} if not found or not ready.
     */
    public McpClient getMcp(String serviceId) {
        return clientCache.get(serviceId);
    }

    /**
     * Retrieves all active and initialized MCP client instances. This is a non-blocking in-memory operation.
     *
     * @return A collection of all active {@link McpClient} instances.
     */
    public Collection<McpClient> getAll() {
        return clientCache.values();
    }

    /**
     * Asynchronously aggregates and returns a list of all tools from all active MCP clients.
     * <p>
     * Each tool's name is prefixed with its service name and a separator (e.g., "serviceName::toolName") to ensure
     * uniqueness across all services.
     * <p>
     * This method fetches tools from all clients in parallel, assuming {@code client.getTools()} is a blocking I/O
     * call.
     *
     * @return A {@code Mono} emitting a List of all available {@link Tool}s.
     */
    public Mono<List<Tool>> getTools() {
        return Flux.fromIterable(clientCache.entrySet()).flatMap(entry -> Mono.fromCallable(() -> {
            String serviceName = entry.getKey();
            McpClient client = entry.getValue();
            return client.getTools().stream()
                    .map(
                            tool -> new Tool(serviceName + TOOL_NAME_SEPARATOR + tool.getName(), tool.getDescription(),
                                    tool.getInputSchema()))
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic())).flatMap(Flux::fromIterable).collectList();
    }

    /**
     * Calls a tool on the specified MCP service and formats the response based on stream mode.
     * <p>
     * This method retrieves the MCP client for the specified service, calls the tool, and formats the response
     * according to the {@link Assets} configuration (streaming vs buffering).
     *
     * @param serviceName The name of the MCP service
     * @param toolName    The name of the tool to call
     * @param arguments   The arguments for the tool call
     * @param streamMode  The stream mode (1 = buffering, 2 = streaming)
     * @return A {@code Mono<ServerResponse>} with the tool execution result
     */
    public Mono<ServerResponse> callToolAndFormat(
            String serviceName,
            String toolName,
            Map<String, Object> arguments,
            Integer streamMode) {

        return Mono.fromCallable(() -> getMcp(serviceName)).subscribeOn(Schedulers.boundedElastic()).flatMap(client -> {
            if (client == null) {
                return ServerResponse.status(503)
                        .bodyValue("Service '" + serviceName + "' is not available or not found.");
            }

            Mono<Object> toolResultMono = client.callTool(toolName, arguments);

            boolean isStreaming = streamMode != null && streamMode == 2;

            if (isStreaming) {
                return executeStreamingToolCall(toolResultMono);
            } else {
                return executeBufferingToolCall(toolResultMono);
            }
        });
    }

    /**
     * Executes the tool call in streaming mode.
     * <p>
     * Converts the tool result into a flux of data buffers for streaming transfer.
     *
     * @param toolResultMono The mono containing the tool execution result
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreamingToolCall(Mono<Object> toolResultMono) {
        return toolResultMono.flatMap(result -> {
            String resultJson = result.toString();
            Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
                byte[] bytes = resultJson.getBytes(Charset.UTF_8);
                return new DefaultDataBufferFactory().wrap(bytes);
            });

            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(dataFlux, DataBuffer.class);
        }).onErrorResume(e -> buildErrorResponse("Error calling tool", e));
    }

    /**
     * Executes the tool call in atomic/buffering mode.
     * <p>
     * Buffers the complete tool result before sending the response.
     *
     * @param toolResultMono The mono containing the tool execution result
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBufferingToolCall(Mono<Object> toolResultMono) {
        return toolResultMono
                .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result))
                .onErrorResume(e -> buildErrorResponse("Error calling tool", e));
    }

    /**
     * Builds a standardized error response using the configured error code.
     *
     * @param message The error message
     * @param error   The exception that occurred
     * @return A ServerResponse with the error payload
     */
    private Mono<ServerResponse> buildErrorResponse(String message, Throwable error) {
        ErrorsHandler.Message errorResponse = ErrorsHandler.Message.builder().errcode(ErrorCode._116008.getKey())
                .errmsg(message + ": " + error.getMessage()).build();
        return new JsonProvider().serialize(errorResponse).map(json -> json.getBytes(Charset.UTF_8))
                .flatMap(bytes -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(bytes));
    }

}

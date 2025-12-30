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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.JsonProvider;
import org.miaixz.bus.vortex.provider.ProcessProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.support.Coordinator;
import org.miaixz.bus.vortex.support.McpRouter;
import org.miaixz.bus.vortex.support.mcp.client.McpClient;
import org.miaixz.bus.vortex.support.mcp.client.StdioClient;
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
 * @since Java 17+
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
     * The registry providing access to all API asset configurations (e.g., from database or files).
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
     * @param assetsRegistry  The registry providing access to all API asset configurations.
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

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            Logger.info("MCP Service is starting...");

            // 1. Asynchronously get assets, offloading the potentially blocking registry call
            Mono.fromCallable(
                    () -> this.assetsRegistry.getAll().stream().filter(a -> a.getMode() >= 3) // Modes 3, 4, 5, 6 are
                                                                                              // MCP related
                            .toList())
                    .subscribeOn(Schedulers.boundedElastic()) // Offload the registry I/O
                    .flatMapMany(Flux::fromIterable) // Convert the List<Assets> to a Flux<Assets>
                    .flatMap(this::startAndRegisterClient) // 2. Start each client in parallel
                    .doOnError(e -> Logger.error("Error during MCP service startup.", e)).subscribe(); // 3.
                                                                                                       // Fire-and-forget
                                                                                                       // (startup is
                                                                                                       // async)

            Logger.info("MCP Service startup process initiated for all clients.");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            Logger.info("MCP Service is stopping...");

            // 1. Asynchronously get assets, offloading the potentially blocking registry call
            Mono<List<Assets>> mcpAssets = Mono
                    .fromCallable(() -> this.assetsRegistry.getAll().stream().filter(a -> a.getMode() >= 3).toList())
                    .subscribeOn(Schedulers.boundedElastic());

            // 2. Create a Mono to stop all processes (assumes provider.stop() is reactive)
            Mono<Void> stopProcesses = mcpAssets.flatMapMany(Flux::fromIterable).flatMap(processProvider::stop)
                    .doOnError(e -> Logger.error("Error stopping MCP process.", e)).then();

            // 3. Create a Mono to close all clients in parallel
            Mono<Void> closeClients = Flux.fromIterable(clientCache.values())
                    .flatMap(
                            client -> Mono.fromRunnable(client::close) // Wrap blocking I/O
                                    .subscribeOn(Schedulers.boundedElastic()) // Offload each close
                                    .doOnError(e -> Logger.error("Error closing MCP client.", e)))
                    .then();

            // 4. Run both stop/close operations in parallel and block until all are complete
            // (Blocking is acceptable in SmartLifecycle.stop())
            Mono.when(stopProcesses, closeClients).doOnError(e -> Logger.error("Error during MCP service shutdown.", e))
                    .block();

            // 5. Clear cache after all resources are released
            clientCache.clear();
            Logger.info("MCP Service stopped.");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Assumes processProvider.start() and client.initialize() are already reactive (return Mono). If not, they must
     * also be wrapped in Mono.fromCallable().subscribeOn().
     */
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
        // 1. Get all client entries from the cache (non-blocking)
        return Flux.fromIterable(clientCache.entrySet())
                // 2. For each client, fetch its tools in parallel.
                .flatMap(entry ->
                // 3. Wrap the blocking client.getTools() call
                Mono.fromCallable(() -> {
                    String serviceName = entry.getKey();
                    McpClient client = entry.getValue();
                    // This is the blocking call. It's now wrapped and will be offloaded.
                    return client.getTools().stream()
                            .map(
                                    tool -> new Tool(serviceName + TOOL_NAME_SEPARATOR + tool.getName(),
                                            tool.getDescription(), tool.getInputSchema()))
                            .collect(Collectors.toList());
                }).subscribeOn(Schedulers.boundedElastic()) // 4. Offload the blocking call
                )
                // 5. We now have a Flux<List<Tool>>. Flatten it to a Flux<Tool>.
                .flatMap(Flux::fromIterable)
                // 6. Collect all tools from all clients into a single list.
                .collectList();
    }

    /**
     * Calls a tool on the specified MCP service and formats the response based on stream mode.
     * <p>
     * This method retrieves the MCP client for the specified service, calls the tool, and formats the response
     * according to the {@link Assets#getStream()} configuration (streaming vs buffering).
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

            // Call the tool
            Mono<Object> toolResultMono = client.callTool(toolName, arguments);

            // Format response based on stream mode
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
            // Convert result to streaming data buffers
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

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
package org.miaixz.bus.vortex.support;

import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.mcp.McpService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * MCP protocol request router, acting as a pure request coordinator. It delegates all MCP asset lifecycle management to
 * the McpLifecycleService and is only responsible for handling real-time listTools and callTool requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class McpRouter implements Router {

    /**
     * The separator used to create unique tool names by prefixing the service name.
     */
    private static final String TOOL_NAME_SEPARATOR = "::";
    /**
     * The service responsible for managing the lifecycle of all MCP clients. This dependency will be injected by the
     * Spring container.
     */
    private final McpService service;

    /**
     * Constructs a new McpRouter with an injected McpLifecycleService.
     *
     * @param service The McpLifecycleService instance to use for managing MCP clients.
     */
    public McpRouter(McpService service) {
        this.service = service;
    }

    /**
     * Routes an incoming request by determining the desired action (listTools or callTool) and delegating to the
     * appropriate handler.
     *
     * @param request The incoming {@link ServerRequest}.
     * @return A {@link Mono<ServerResponse>} with the result of the operation.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        // This logic is synchronous, fast, and non-blocking.
        String action = request.queryParam("action").orElse("listTools");
        if ("listTools".equalsIgnoreCase(action)) {
            return listTools();
        } else if ("callTool".equalsIgnoreCase(action)) {
            return callTool(request);
        } else {
            return ServerResponse.badRequest().bodyValue("Unknown action: " + action);
        }
    }

    /**
     * Handles a request to list all available tools from all active MCP services. It aggregates tools from all clients
     * managed by the McpLifecycleService.
     *
     * @return A {@link Mono<ServerResponse>} containing a list of all available tools.
     */
    private Mono<ServerResponse> listTools() {
        // This assumes service.getTools() is already asynchronous and returns a Mono,
        // which is implied by the original code's use of flatMap.
        return this.service.getTools().flatMap(tools -> ServerResponse.ok().bodyValue(tools));
    }

    /**
     * Handles a request to call a specific tool. It parses the tool name to identify the target service, retrieves the
     * corresponding client, and delegates the call.
     * <p>
     * Supports both streaming and atomic response modes based on the {@link Assets#getStream()} configuration.
     *
     * @param request The incoming {@link ServerRequest} containing the tool name and arguments.
     * @return A {@link Mono<ServerResponse>} with the result from the tool execution.
     */
    private Mono<ServerResponse> callTool(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            // --- All this setup is fast, in-memory, and non-blocking ---
            String prefixedToolName = request.queryParam("toolName").orElse(null);
            if (StringKit.isEmpty(prefixedToolName)) {
                return ServerResponse.badRequest().bodyValue("Missing required parameter: toolName");
            }

            String[] parts = prefixedToolName.split(TOOL_NAME_SEPARATOR, 2);
            if (parts.length != 2) {
                return ServerResponse.badRequest()
                        .bodyValue("Invalid toolName format. Expected 'serviceName::toolName'.");
            }

            String serviceName = parts[0];
            String actualToolName = parts[1];

            // Extract all query parameters as arguments, excluding action and toolName.
            Map<String, Object> arguments = request.queryParams().toSingleValueMap().entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("action") && !entry.getKey().equals("toolName"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // --- End of non-blocking setup ---

            // Determine if streaming mode is enabled
            boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

            // 1. Wrap the potentially blocking call `service.getMcp()` in fromCallable
            // and offload it from the event loop.
            return Mono.fromCallable(() -> this.service.getMcp(serviceName)).subscribeOn(Schedulers.boundedElastic())
                    .flatMap(client -> {
                        // 2. This logic now runs after the client has been fetched asynchronously.
                        if (client == null) {
                            return ServerResponse.status(503)
                                    .bodyValue("Service '" + serviceName + "' is not available or not found.");
                        }

                        // 3. Call the tool and choose execution strategy based on stream mode
                        Mono<Object> toolResultMono = client.callTool(actualToolName, arguments);

                        if (isStreaming) {
                            // STREAMING MODE: Use streaming execution
                            return executeStreaming(toolResultMono);
                        } else {
                            // ATOMIC MODE: Use buffering execution
                            return executeBuffering(toolResultMono);
                        }
                    });
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
    private Mono<ServerResponse> executeStreaming(Mono<Object> toolResultMono) {
        return toolResultMono.flatMap(result -> {
            String resultJson = result.toString();
            DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

            // Convert result to streaming data buffers
            Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
                byte[] bytes = resultJson.getBytes(StandardCharsets.UTF_8);
                return bufferFactory.wrap(bytes);
            });

            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(dataFlux, DataBuffer.class);
        }).onErrorResume(
                e -> ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"error\": \"Error calling tool: " + e.getMessage() + "\"}"));
    }

    /**
     * Executes the tool call in atomic/buffering mode.
     * <p>
     * Buffers the complete tool result before sending the response.
     *
     * @param toolResultMono The mono containing the tool execution result
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(Mono<Object> toolResultMono) {
        return toolResultMono
                .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result))
                .onErrorResume(
                        e -> ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\": \"Error calling tool: " + e.getMessage() + "\"}"));
    }

}

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
import org.miaixz.bus.vortex.support.mcp.McpExecutor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * MCP protocol request router, acting as a pure request coordinator. It delegates all MCP asset lifecycle management to
 * the McpLifecycleExecutor and is only responsible for handling real-time listTools and callTool requests.
 * <p>
 * This router supports two response modes controlled by {@link Assets#getStream()}:
 * <ul>
 * <li>Buffering mode (stream = 1 or null): Buffers the complete tool result before returning</li>
 * <li>Streaming mode (stream = 2): Streams the tool result in chunks</li>
 * </ul>
 * </p>
 * Generic type parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class McpRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * The separator used to prefix tool names with their service ID for uniqueness.
     */
    private static final String TOOL_NAME_SEPARATOR = "::";

    /**
     * The executor responsible for managing the lifecycle of all MCP clients. This dependency will be injected by the
     * Spring container.
     */
    private final McpExecutor executor;

    /**
     * Constructs a new McpRouter with an injected McpLifecycleExecutor.
     *
     * @param executor The McpLifecycleExecutor instance to use for managing MCP clients.
     */
    public McpRouter(McpExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes an incoming request by determining the desired action (listTools or callTool) and delegating to the
     * appropriate handler.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@link Mono<ServerResponse>} with the result of the operation
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        ServerRequest request = input;
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
     * managed by the McpLifecycleExecutor.
     *
     * @return A {@link Mono<ServerResponse>} containing a list of all available tools
     */
    private Mono<ServerResponse> listTools() {
        return this.executor.getTools().flatMap(tools -> ServerResponse.ok().bodyValue(tools));
    }

    /**
     * Handles a request to call a specific tool. It parses the tool name to identify the target service and delegates
     * the call to the {@link McpExecutor}.
     *
     * @param request The incoming {@link ServerRequest} containing the tool name and arguments.
     * @return A {@link Mono<ServerResponse>} with the result from the tool execution.
     */
    private Mono<ServerResponse> callTool(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            // --- Parse tool name and arguments (fast, in-memory, non-blocking) ---
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
            // --- End of parsing ---

            // Delegate to executor
            return this.executor.callToolAndFormat(serviceName, actualToolName, arguments, assets.getStream());
        });
    }

}

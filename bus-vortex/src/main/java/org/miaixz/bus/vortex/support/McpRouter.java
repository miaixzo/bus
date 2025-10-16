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
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.mcp.McpClient;
import org.miaixz.bus.vortex.support.mcp.McpService;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

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
        // Now uses the new getAllTools() method from McpLifecycleService
        return this.service.getTools().flatMap(tools -> ServerResponse.ok().bodyValue(tools));
    }

    /**
     * Handles a request to call a specific tool. It parses the tool name to identify the target service, retrieves the
     * corresponding client, and delegates the call.
     *
     * @param request The incoming {@link ServerRequest} containing the tool name and arguments.
     * @return A {@link Mono<ServerResponse>} with the result from the tool execution.
     */
    private Mono<ServerResponse> callTool(ServerRequest request) {
        String prefixedToolName = request.queryParam("toolName").orElse(null);
        if (StringKit.isEmpty(prefixedToolName)) {
            return ServerResponse.badRequest().bodyValue("Missing required parameter: toolName");
        }

        String[] parts = prefixedToolName.split(TOOL_NAME_SEPARATOR, 2);
        if (parts.length != 2) {
            return ServerResponse.badRequest().bodyValue("Invalid toolName format. Expected 'serviceName::toolName'.");
        }

        String serviceName = parts[0];
        String actualToolName = parts[1];

        McpClient client = this.service.getMcp(serviceName);
        if (client == null) {
            return ServerResponse.status(503).bodyValue("Service '" + serviceName + "' is not available or not found.");
        }

        // Extract all query parameters as arguments, excluding action and toolName.
        Map<String, Object> arguments = request.queryParams().toSingleValueMap().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("action") && !entry.getKey().equals("toolName"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Mono<String> resultMono = client.callTool(actualToolName, arguments);

        return resultMono.flatMap(result -> ServerResponse.ok().bodyValue(result)).onErrorResume(
                e -> ServerResponse.status(500)
                        .bodyValue("{\"error\": \"Error calling tool: " + e.getMessage() + "\"}"));
    }

}

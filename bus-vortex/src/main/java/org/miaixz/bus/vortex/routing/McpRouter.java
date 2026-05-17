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
package org.miaixz.bus.vortex.routing;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.mcp.McpExecutor;

import reactor.core.publisher.Mono;

/**
 * MCP protocol request router, acting as a pure request coordinator for standard Streamable HTTP requests. Generic type
 * parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class McpRouter implements Router<ServerRequest, ServerResponse> {

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
     * Routes an incoming request by delegating the full Streamable HTTP protocol handling to {@link McpExecutor}.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@link Mono<ServerResponse>} with the result of the operation
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        ServerRequest request = input;
        Context context = request.exchange().getAttribute(Context.$);
        String ip = context == null || context.getX_request_ip() == null ? "N/A" : context.getX_request_ip();
        Logger.debug(true, "Vortex", "MCP request header snapshot: clientIp={}, path={}", ip, request.path());
        Logger.debug(
                true,
                "Vortex",
                "MCP request headers: clientIp={}, headers={}",
                ip,
                request.headers().asHttpHeaders().toSingleValueMap());
        Logger.debug(
                true,
                "Vortex",
                "MCP request parameters: clientIp={}, parameters={}",
                ip,
                request.queryParams().toSingleValueMap());
        return this.executor.execute(context, request);
    }

}

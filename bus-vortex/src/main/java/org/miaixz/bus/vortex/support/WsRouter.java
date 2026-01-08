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
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.ws.WsExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding WebSocket connections to upstream WebSocket services.
 * <p>
 * This class acts as a simple coordinator. It validates the WebSocket upgrade request and delegates the execution to
 * the {@link WsExecutor}.
 * <p>
 * The executor handles all protocol-specific logic including:
 * <ul>
 * <li>Building the upstream WebSocket URL</li>
 * <li>Generating WebSocket endpoint metadata</li>
 * <li>Returning appropriate {@link ServerResponse}</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> For actual WebSocket proxying, you need to configure a separate WebSocketHandlerAdapter in
 * your Spring configuration that maps WebSocket endpoints to handlers.
 * </p>
 * Generic type parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WsRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * The executor responsible for managing WebSocket connections and message forwarding.
     */
    private final WsExecutor executor;

    /**
     * Constructs a new {@code WsRouter}.
     *
     * @param executor The executor that will handle WebSocket connection management.
     */
    public WsRouter(WsExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes a WebSocket request by validating the upgrade and delegating to the executor.
     * <p>
     * This method validates the WebSocket upgrade request, retrieves the {@link Context}, and delegates the execution
     * to the {@link WsExecutor}. The executor is responsible for building the upstream URL and returning the endpoint
     * metadata.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@code Mono<ServerResponse>} with WebSocket endpoint information
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            String ip = context.getX_request_ipv4();
            String method = input.methodName();
            String path = input.path();

            Logger.info(
                    true,
                    "WebSocket",
                    "[{}] [{}] [{}] [WS_ROUTER_START] - Connecting to WebSocket target: {}",
                    ip,
                    method,
                    path,
                    context.getAssets().getMethod());

            // Check if this is a WebSocket upgrade request
            boolean isWebSocketUpgrade = input.headers().header("Upgrade").stream()
                    .anyMatch(h -> h.equalsIgnoreCase("websocket"));

            if (!isWebSocketUpgrade) {
                Logger.warn(
                        true,
                        "WebSocket",
                        "[{}] [{}] [{}] [WS_NOT_UPGRADE] - Request is not a WebSocket upgrade request",
                        ip,
                        method,
                        path);

                return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).bodyValue(
                        "{\"error\": \"WebSocket upgrade required. "
                                + "Please use WebSocket client to connect to this endpoint.\"}");
            }

            Logger.info(
                    true,
                    "WebSocket",
                    "[{}] [{}] [{}] [WS_UPGRADE_DETECTED] - WebSocket upgrade request detected",
                    ip,
                    method,
                    path);

            // Delegate to executor
            return executor.execute(context, null);
        });
    }

}

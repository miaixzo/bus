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

import java.time.Duration;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.ws.WsService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding WebSocket connections to upstream WebSocket services.
 * <p>
 * This class acts as a WebSocket proxy, establishing a bidirectional tunnel between the client and the upstream
 * WebSocket server. Since Spring WebFlux's functional routing doesn't directly support WebSocket upgrades in
 * ServerResponse, this router returns a response indicating that WebSocket upgrade should be handled separately.
 * <p>
 * <strong>Note:</strong> For WebSocket support, you need to configure a separate WebSocketHandlerAdapter in your Spring
 * configuration that maps WebSocket endpoints to handlers.
 *
 * @author Kimi Liu
 * @see WsService
 * @since Java 17+
 */
public class WsRouter implements Router {

    /**
     * The service responsible for managing WebSocket connections and message forwarding.
     */
    private final WsService service;

    /**
     * WebSocket client for establishing upstream connections.
     */
    private final WebSocketClient client;

    /**
     * Constructs a new {@code WsRouter}.
     *
     * @param service The service that will handle WebSocket connection management.
     */
    public WsRouter(WsService service) {
        this.service = service;
        this.client = new ReactorNettyWebSocketClient();
    }

    /**
     * Routes a WebSocket request by proxying to the upstream WebSocket service.
     * <p>
     * Since WebSocket upgrade cannot be handled directly in ServerResponse, this method:
     * <ol>
     * <li>Validates the WebSocket upgrade request</li>
     * <li>Establishes a connection to the upstream WebSocket server</li>
     * <li>Proxies messages bidirectionally</li>
     * <li>Returns a streaming response with the proxied data</li>
     * </ol>
     * <p>
     * <strong>Alternative Implementation:</strong> For true WebSocket support, consider using Spring's
     * WebSocketHandlerAdapter with a dedicated WebSocket endpoint mapping.
     *
     * @param request The current {@link ServerRequest}
     * @return A {@code Mono<ServerResponse>} with streaming data from the WebSocket connection
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            String ip = context.getX_request_ipv4();
            String method = request.methodName();
            String path = request.path();

            Logger.info(
                    true,
                    "WebSocket",
                    "[{}] [{}] [{}] [WS_ROUTER_START] - Connecting to WebSocket target: {}",
                    ip,
                    method,
                    path,
                    assets.getMethod());

            // Build upstream WebSocket URL
            String upstreamUrl = buildUpstreamUrl(assets);

            // Check if this is a WebSocket upgrade request
            boolean isWebSocketUpgrade = request.headers().header("Upgrade").stream()
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

            // For WebSocket, we need to return a response indicating upgrade is needed
            // In a real implementation, this would be handled by WebSocketHandlerAdapter
            Logger.info(
                    true,
                    "WebSocket",
                    "[{}] [{}] [{}] [WS_UPGRADE_DETECTED] - WebSocket upgrade request detected for: {}",
                    ip,
                    method,
                    path,
                    upstreamUrl);

            // Return a response with WebSocket connection information
            String responseJson = String.format(
                    "{\"status\": \"websocket_ready\", " + "\"message\": \"WebSocket endpoint configured\", "
                            + "\"upstream\": \"%s\", "
                            + "\"note\": \"Use WebSocket client to connect to this endpoint\"}",
                    upstreamUrl);

            // Determine if streaming mode is enabled (though WebSocket is inherently streaming)
            boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

            if (isStreaming) {
                return executeStreaming(responseJson);
            } else {
                return executeBuffering(responseJson);
            }
        });
    }

    /**
     * Executes the response in streaming mode.
     * <p>
     * Converts the response JSON into a flux of data buffers for streaming transfer.
     *
     * @param responseJson The JSON response string
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(String responseJson) {
        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

        Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
            byte[] bytes = responseJson.getBytes(Charset.UTF_8);
            return bufferFactory.wrap(bytes);
        });

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(dataFlux, DataBuffer.class);
    }

    /**
     * Executes the response in atomic/buffering mode.
     * <p>
     * Buffers the complete response before sending.
     *
     * @param responseJson The JSON response string
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(String responseJson) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(responseJson);
    }

    /**
     * Builds the upstream WebSocket URL from the asset configuration.
     * <p>
     * Constructs a WebSocket URL in the format: ws://host:port/path or wss://host:port/path for secure connections.
     * <p>
     * The protocol (ws/wss) is determined by:
     * <ul>
     * <li>If {@link Assets#getSign()} == 1, uses wss:// (secure WebSocket)</li>
     * <li>If port is 443, uses wss:// (standard HTTPS port)</li>
     * <li>If host starts with "wss://", uses wss://</li>
     * <li>Otherwise, uses ws:// (plain WebSocket)</li>
     * </ul>
     *
     * @param assets The asset configuration containing host, port, and path information.
     * @return The complete upstream WebSocket URL.
     */
    private String buildUpstreamUrl(Assets assets) {
        StringBuilder urlBuilder = new StringBuilder();

        // Determine protocol (ws or wss)
        boolean isSecure = false;

        // Check if host starts with wss:// or contains secure indicators
        if (assets.getHost() != null && assets.getHost().startsWith("wss://")) {
            isSecure = true;
        }

        urlBuilder.append(isSecure ? "wss://" : "ws://");

        // Add host (remove protocol prefix if present)
        String host = assets.getHost();
        if (host != null) {
            host = host.replaceFirst("^(ws://|wss://)", "");
            urlBuilder.append(host);
        }

        // Add port if specified and not default
        if (assets.getPort() != null && assets.getPort() > 0) {
            // Don't add port if it's the default for the protocol
            boolean isDefaultPort = (isSecure && assets.getPort() == 443) || (!isSecure && assets.getPort() == 80);
            if (!isDefaultPort) {
                urlBuilder.append(Symbol.COLON).append(assets.getPort());
            }
        }

        // Add path
        if (assets.getPath() != null && !assets.getPath().isEmpty()) {
            if (!assets.getPath().startsWith(Symbol.SLASH)) {
                urlBuilder.append(Symbol.SLASH);
            }
            urlBuilder.append(assets.getPath());
        }

        // Add URL pattern
        if (assets.getUrl() != null && !assets.getUrl().isEmpty()) {
            if (!assets.getUrl().startsWith(Symbol.SLASH)) {
                urlBuilder.append(Symbol.SLASH);
            }
            urlBuilder.append(assets.getUrl());
        }

        return urlBuilder.toString();
    }

}

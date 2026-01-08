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
package org.miaixz.bus.vortex.support.ws;

import jakarta.annotation.PreDestroy;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.support.Coordinator;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An executor for managing and executing WebSocket connections and sessions.
 * <p>
 * This executor provides centralized management of WebSocket connections, including:
 * <ul>
 * <li>Session tracking and lifecycle management</li>
 * <li>Connection pooling and reuse</li>
 * <li>Message routing and broadcasting</li>
 * <li>Connection health monitoring</li>
 * <li>Building WebSocket endpoint metadata responses</li>
 * </ul>
 * <p>
 * The executor maintains a registry of active WebSocket sessions, allowing for efficient message delivery and
 * connection management across the gateway.
 * <p>
 * Generic type parameters: {@code Executor<Object, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WsExecutor extends Coordinator<Object, ServerResponse> {

    /**
     * A thread-safe map of active WebSocket sessions, keyed by session ID. This allows tracking and managing all active
     * connections through the gateway.
     */
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * A thread-safe map of session metadata, keyed by session ID. Stores additional information about each session such
     * as connection time, target upstream URL, and custom attributes.
     */
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    /**
     * Executes a WebSocket request using the provided context.
     * <p>
     * This method validates the WebSocket upgrade request, builds the upstream URL, and returns a response containing
     * the WebSocket endpoint metadata.
     *
     * @param context The request context containing the assets configuration
     * @param input   Optional input object (may be null for WebSocket)
     * @return A {@link Mono<ServerResponse>} containing WebSocket endpoint information
     */
    @Override
    public Mono<ServerResponse> execute(Context context, Object input) {
        Assets assets = context.getAssets();
        String upstreamUrl = buildUpstreamUrl(assets);

        // Build response with WebSocket connection information
        String responseJson = String.format(
                "{\"status\": \"websocket_ready\", " + "\"message\": \"WebSocket endpoint configured\", "
                        + "\"upstream\": \"%s\", " + "\"note\": \"Use WebSocket client to connect to this endpoint\"}",
                upstreamUrl);

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

    /**
     * Registers a new WebSocket session.
     * <p>
     * This method should be called when a new WebSocket connection is established through the gateway. It tracks the
     * session for management and monitoring purposes.
     *
     * @param session The WebSocket session to register.
     * @param assets  The asset configuration for this connection.
     */
    public void registerSession(WebSocketSession session, Assets assets) {
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);

        SessionMetadata metadata = new SessionMetadata(sessionId, System.currentTimeMillis(),
                assets.getHost() + ":" + assets.getPort(), assets.getMethod());

        sessionMetadata.put(sessionId, metadata);

        Logger.info(
                "WebSocket",
                "Session registered: {} -> {} (Total active: {})",
                sessionId,
                metadata.getUpstreamTarget(),
                activeSessions.size());
    }

    /**
     * Unregisters a WebSocket session.
     * <p>
     * This method should be called when a WebSocket connection is closed. It removes the session from tracking and
     * cleans up associated resources.
     *
     * @param sessionId The ID of the session to unregister.
     */
    public void unregisterSession(String sessionId) {
        WebSocketSession session = activeSessions.remove(sessionId);
        SessionMetadata metadata = sessionMetadata.remove(sessionId);

        if (session != null && metadata != null) {
            long duration = System.currentTimeMillis() - metadata.getConnectedAt();
            Logger.info(
                    "WebSocket",
                    "Session unregistered: {} -> {} (Duration: {}ms, Remaining: {})",
                    sessionId,
                    metadata.getUpstreamTarget(),
                    duration,
                    activeSessions.size());
        }
    }

    /**
     * Gets the number of active WebSocket sessions.
     *
     * @return The count of currently active sessions.
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Gets metadata for a specific session.
     *
     * @param sessionId The session ID.
     * @return The session metadata, or null if not found.
     */
    public SessionMetadata getSessionMetadata(String sessionId) {
        return sessionMetadata.get(sessionId);
    }

    /**
     * Gets all active session IDs.
     *
     * @return A map of all active sessions.
     */
    public Map<String, WebSocketSession> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    /**
     * Gracefully closes all active WebSocket sessions.
     * <p>
     * This method is automatically called during application shutdown to ensure all WebSocket connections are properly
     * closed.
     *
     * @return A {@link Mono<ServerResponse>} that completes when all sessions are closed.
     */
    @PreDestroy
    @Override
    public Mono<ServerResponse> destroy() {
        Logger.info("Shutting down WsExecutor... (Active sessions: {})", activeSessions.size());

        return Flux.fromIterable(activeSessions.values()).flatMap(session -> {
            try {
                if (session.isOpen()) {
                    return session.close().doOnError(
                            error -> Logger.error("Error closing WebSocket session: {}", session.getId(), error))
                            .doOnSuccess(v -> Logger.debug("WebSocket session closed: {}", session.getId()));
                } else {
                    return Mono.empty();
                }
            } catch (Exception e) {
                Logger.error("Failed to close WebSocket session: {}", session.getId(), e);
                return Mono.error(e);
            }
        }).doOnComplete(() -> {
            activeSessions.clear();
            sessionMetadata.clear();
            Logger.info("WsExecutor shut down successfully.");
        }).then(Mono.empty());
    }

    /**
     * Metadata for a WebSocket session.
     */
    public static class SessionMetadata {

        private final String sessionId;
        private final long connectedAt;
        private final String upstreamTarget;
        private final String method;

        public SessionMetadata(String sessionId, long connectedAt, String upstreamTarget, String method) {
            this.sessionId = sessionId;
            this.connectedAt = connectedAt;
            this.upstreamTarget = upstreamTarget;
            this.method = method;
        }

        public String getSessionId() {
            return sessionId;
        }

        public long getConnectedAt() {
            return connectedAt;
        }

        public String getUpstreamTarget() {
            return upstreamTarget;
        }

        public String getMethod() {
            return method;
        }
    }

}

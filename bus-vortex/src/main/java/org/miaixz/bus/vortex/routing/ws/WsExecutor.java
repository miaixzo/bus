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
package org.miaixz.bus.vortex.routing.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.WebSocketSession;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.ServiceRoutes;
import org.miaixz.bus.errors.ComponentFailure;
import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.routing.Coordinator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
 */
public class WsExecutor extends Coordinator<Object, ServerResponse> {

    /**
     * Reports failures whose ws-cleanup response or lifecycle is owned by this adapter.
     */
    private static final ComponentFailure FAILURES = new ComponentFailure("vortex", "ws-cleanup");

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
     * Creates a WebSocket executor.
     */
    public WsExecutor() {
        // No initialization required.
    }

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

        String responseJson = JsonKit.toJsonString(
                Map.of(
                        "status",
                        "websocket_ready",
                        "message",
                        "WebSocket endpoint configured",
                        "upstream",
                        upstreamUrl,
                        "note",
                        "Use WebSocket client to connect to this endpoint"));

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(responseJson);
    }

    /**
     * Preserves the selected listener's transport security while resolving its WebSocket target. HTTP becomes WS, HTTPS
     * becomes WSS, and explicit WS/WSS protocol targets remain unchanged.
     *
     * @param assets request-local asset bound to its selected Instance
     * @return complete upstream WebSocket URL
     * @throws IllegalArgumentException when the resolved target uses another transport
     */
    private String buildUpstreamUrl(Assets assets) {
        String target = ServiceRoutes.endpoint(assets).toASCIIString();
        if (target.startsWith(Protocol.HTTPS_PREFIX))
            return Protocol.WSS_PREFIX + target.substring(Protocol.HTTPS_PREFIX.length());
        if (target.startsWith(Protocol.HTTP_PREFIX))
            return Protocol.WS_PREFIX + target.substring(Protocol.HTTP_PREFIX.length());
        if (!target.startsWith(Protocol.WS_PREFIX) && !target.startsWith(Protocol.WSS_PREFIX))
            throw new IllegalArgumentException("A WebSocket transport endpoint is required");
        return target;
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
                ServiceRoutes.endpoint(assets).toASCIIString(), assets.getMethod());

        sessionMetadata.put(sessionId, metadata);

        Logger.info(
                true,
                "Vortex",
                "WS session registered: protocol=ws, {} -> {} (Total active: {})",
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
                    true,
                    "Vortex",
                    "WS session unregistered: protocol=ws, {} -> {} (Duration: {}ms, Remaining: {})",
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
        Logger.info(
                false,
                "Vortex",
                "WS webSocket resources shutting down: protocol=ws, activeSessions={}",
                activeSessions.size());

        return Flux.fromIterable(activeSessions.values()).flatMap(session -> {
            try {
                if (session.isOpen()) {
                    return session.close().doOnError(error -> FAILURES.failed(ErrorType.IO_FAILURE, error)).doOnSuccess(
                            v -> Logger.debug(
                                    false,
                                    "Vortex",
                                    "WS webSocket session closed: protocol=ws, {}",
                                    session.getId()));
                } else {
                    return Mono.empty();
                }
            } catch (Exception e) {
                FAILURES.failed(ErrorType.IO_FAILURE, e);
                return Mono.error(e);
            }
        }).doOnComplete(() -> {
            activeSessions.clear();
            sessionMetadata.clear();
            Logger.info(false, "Vortex", "WS webSocket resources stopped: protocol=ws");
        }).then(Mono.empty());
    }

    /**
     * Metadata for a WebSocket session.
     *
     * @author Kimi Liu
     */
    public static class SessionMetadata {

        /**
         * Gateway WebSocket session identifier.
         */
        private final String sessionId;

        /**
         * Connection timestamp in milliseconds since epoch.
         */
        private final long connectedAt;

        /**
         * Upstream WebSocket target URI.
         */
        private final String upstreamTarget;

        /**
         * Logical method associated with the upstream route.
         */
        private final String method;

        /**
         * Creates session metadata.
         *
         * @param sessionId      The session identifier.
         * @param connectedAt    The connection timestamp in milliseconds since epoch.
         * @param upstreamTarget The upstream target URI.
         * @param method         The upstream method.
         */
        public SessionMetadata(String sessionId, long connectedAt, String upstreamTarget, String method) {
            this.sessionId = sessionId;
            this.connectedAt = connectedAt;
            this.upstreamTarget = upstreamTarget;
            this.method = method;
        }

        /**
         * Returns the session identifier.
         *
         * @return The session identifier.
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * Returns the connection timestamp.
         *
         * @return The connection timestamp in milliseconds since epoch.
         */
        public long getConnectedAt() {
            return connectedAt;
        }

        /**
         * Returns the upstream target URI.
         *
         * @return The upstream target URI.
         */
        public String getUpstreamTarget() {
            return upstreamTarget;
        }

        /**
         * Returns the upstream method.
         *
         * @return The upstream method.
         */
        public String getMethod() {
            return method;
        }

    }

}

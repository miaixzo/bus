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
package org.miaixz.bus.vortex.support.ws;

import jakarta.annotation.PreDestroy;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service for managing WebSocket connections and sessions.
 * <p>
 * This service provides centralized management of WebSocket connections, including:
 * <ul>
 * <li>Session tracking and lifecycle management</li>
 * <li>Connection pooling and reuse</li>
 * <li>Message routing and broadcasting</li>
 * <li>Connection health monitoring</li>
 * </ul>
 * <p>
 * The service maintains a registry of active WebSocket sessions, allowing for efficient
 * message delivery and connection management across the gateway.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WsService {

    /**
     * A thread-safe map of active WebSocket sessions, keyed by session ID.
     * This allows tracking and managing all active connections through the gateway.
     */
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * A thread-safe map of session metadata, keyed by session ID.
     * Stores additional information about each session such as connection time,
     * target upstream URL, and custom attributes.
     */
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    /**
     * Registers a new WebSocket session.
     * <p>
     * This method should be called when a new WebSocket connection is established
     * through the gateway. It tracks the session for management and monitoring purposes.
     *
     * @param session The WebSocket session to register.
     * @param assets  The asset configuration for this connection.
     */
    public void registerSession(WebSocketSession session, Assets assets) {
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);

        SessionMetadata metadata = new SessionMetadata(
                sessionId,
                System.currentTimeMillis(),
                assets.getHost() + ":" + assets.getPort(),
                assets.getMethod());

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
     * This method should be called when a WebSocket connection is closed.
     * It removes the session from tracking and cleans up associated resources.
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
     * This method is automatically called during application shutdown to ensure
     * all WebSocket connections are properly closed.
     */
    @PreDestroy
    public void destroy() {
        Logger.info("Shutting down WsService... (Active sessions: {})", activeSessions.size());

        activeSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close().subscribe(
                            null,
                            error -> Logger.error("Error closing WebSocket session: {}", session.getId(), error),
                            () -> Logger.debug("WebSocket session closed: {}", session.getId()));
                }
            } catch (Exception e) {
                Logger.error("Failed to close WebSocket session: {}", session.getId(), e);
            }
        });

        activeSessions.clear();
        sessionMetadata.clear();
        Logger.info("WsService shut down successfully.");
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

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
package org.miaixz.bus.starter.fabric;

import jakarta.annotation.Resource;

import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketServer;
import org.miaixz.bus.logger.Logger;

/**
 * Service class that manages a current-fabric WebSocket server lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WebSocketQuickService {

    /**
     * Fabric configuration properties.
     */
    private final FabricProperties properties;

    /**
     * Current fabric message handler.
     */
    @Resource
    private Handler handler;

    /**
     * Running WebSocket server.
     */
    private WebSocketServer server;

    /**
     * Constructs a new WebSocketQuickService with the given properties.
     *
     * @param properties fabric configuration properties
     */
    public WebSocketQuickService(final FabricProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the current fabric message handler.
     *
     * @param handler handler
     */
    public void setHandler(final Handler handler) {
        this.handler = handler;
    }

    /**
     * Starts the WebSocket server.
     */
    public synchronized void start() {
        if (server != null && server.running()) {
            return;
        }
        final FabricProperties.WebSocket websocket = properties.getWebsocket();
        final Handler currentHandler = handler == null ? WebSocketQuickService::noop : handler;
        final WebSocketServer currentServer = Fabric.websocketServer().bind(websocket.getHost(), websocket.getPort())
                .path(websocket.getPath()).onMessage(currentHandler).build();
        try {
            currentServer.start();
            server = currentServer;
            Logger.info(true, "Starter", "WebSocket server started on port: {}", websocket.getPort());
        } catch (final RuntimeException e) {
            currentServer.close();
            Logger.error(false, "Starter", "Failed to start WebSocket server", e);
        }
    }

    /**
     * Stops the WebSocket server.
     */
    public synchronized void stop() {
        final WebSocketServer currentServer = server;
        server = null;
        if (currentServer != null) {
            currentServer.close();
            Logger.info(false, "Starter", "WebSocket server stopped.");
        }
    }

    /**
     * Returns whether the current server is running.
     *
     * @return true when running
     */
    public synchronized boolean running() {
        return server != null && server.running();
    }

    /**
     * Default handler used when no Spring handler bean is provided.
     *
     * @param session session
     * @param message message
     */
    private static void noop(final Session session, final Message message) {
        // No-op keeps lifecycle-only starter configurations valid.
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.socket;

import jakarta.annotation.Resource;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.accord.AioServer;

import java.io.IOException;

/**
 * A service class that manages the lifecycle of a {@link AioServer}.
 * <p>
 * This service acts as a bridge between the Spring application context and the underlying socket server, allowing the
 * server to be started and stopped along with the application.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SocketQuickService {

    /**
     * The configuration properties for the socket server.
     */
    private final SocketProperties properties;

    /**
     * The message handler for processing incoming socket data.
     */
    @Resource
    private Handler handler;

    /**
     * The message protocol definition.
     */
    @Resource
    private Message message;

    /**
     * The underlying AIO (Asynchronous I/O) server instance.
     */
    private AioServer aioQuickServer;

    /**
     * Constructs a new SocketQuickService with the given properties.
     *
     * @param properties The socket configuration properties.
     */
    public SocketQuickService(SocketProperties properties) {
        this.properties = properties;
    }

    /**
     * Starts the AIO socket server.
     * <p>
     * This method initializes the {@link AioServer} with the configured port, message protocol, and handler, and then
     * starts it. This method is intended to be called as a bean's {@code init-method}.
     * </p>
     */
    public void start() {
        this.aioQuickServer = new AioServer(this.properties.getPort(), message, handler);
        try {
            aioQuickServer.start();
            Logger.info("AIO socket server started on port: {}", this.properties.getPort());
        } catch (IOException e) {
            Logger.error("Failed to start AIO socket server", e);
        }
    }

    /**
     * Stops the AIO socket server.
     * <p>
     * This method gracefully shuts down the running server instance. It is intended to be called as a bean's
     * {@code destroy-method}.
     * </p>
     */
    public void stop() {
        if (aioQuickServer != null) {
            aioQuickServer.shutdown();
            Logger.info("AIO socket server stopped.");
        }
    }

}

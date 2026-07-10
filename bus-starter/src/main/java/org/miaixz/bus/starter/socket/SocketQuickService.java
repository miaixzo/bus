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
package org.miaixz.bus.starter.socket;

import java.util.function.Supplier;

import jakarta.annotation.Resource;

import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.logger.Logger;

/**
 * Service class that manages a current-fabric TCP socket server lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SocketQuickService {

    /**
     * The configuration properties for the socket server.
     */
    private final SocketProperties properties;

    /**
     * Current fabric message handler.
     */
    @Resource
    private Handler handler;

    /**
     * Current fabric frame codec factory.
     */
    @Resource
    private Supplier<FrameCodec> frameCodec;

    /**
     * Running TCP server.
     */
    private TcpServer server;

    /**
     * Accepted-connection adapter.
     */
    private SocketHandlerAdapter adapter;

    /**
     * Constructs a new SocketQuickService with the given properties.
     *
     * @param properties socket configuration properties
     */
    public SocketQuickService(SocketProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the current fabric message handler.
     *
     * @param handler handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Sets the current fabric frame codec factory.
     *
     * @param frameCodec frame codec factory
     */
    public void setFrameCodec(Supplier<FrameCodec> frameCodec) {
        this.frameCodec = frameCodec;
    }

    /**
     * Starts the socket server.
     */
    public synchronized void start() {
        if (server != null && server.running()) {
            return;
        }
        final Handler currentHandler = handler == null ? SocketQuickService::noop : handler;
        final SocketFrameDecoder currentDecoder = frameCodec == null ? SocketFrameDecoder.line()
                : SocketFrameDecoder.of(frameCodec);
        final TcpServer currentServer = new TcpServer(Address.parse("tcp://0.0.0.0:" + properties.getPort()));
        final SocketHandlerAdapter currentAdapter = new SocketHandlerAdapter(currentHandler, currentDecoder);
        currentServer.accept(currentAdapter);
        try {
            currentServer.start();
            server = currentServer;
            adapter = currentAdapter;
            Logger.info(true, "Starter", "Socket server started on port: {}", properties.getPort());
        } catch (RuntimeException e) {
            currentAdapter.close();
            currentServer.close();
            Logger.error(false, "Starter", "Failed to start socket server", e);
        }
    }

    /**
     * Stops the socket server.
     */
    public synchronized void stop() {
        final TcpServer currentServer = server;
        final SocketHandlerAdapter currentAdapter = adapter;
        server = null;
        adapter = null;
        if (currentAdapter != null) {
            currentAdapter.close();
        }
        if (currentServer != null) {
            currentServer.close();
            Logger.info(false, "Starter", "Socket server stopped.");
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
    private static void noop(Session session, Message message) {
        // No-op keeps lifecycle-only starter configurations valid.
    }

}

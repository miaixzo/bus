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

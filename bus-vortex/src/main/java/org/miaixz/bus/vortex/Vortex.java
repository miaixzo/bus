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
package org.miaixz.bus.vortex;

import org.miaixz.bus.logger.Logger;
import org.springframework.context.SmartLifecycle;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of the core Reactor Netty HTTP server for the Vortex application.
 * <p>
 * This class implements {@link SmartLifecycle} to integrate seamlessly with the Spring application context. It is
 * responsible for starting the {@link HttpServer} when the application starts and gracefully shutting it down when the
 * application stops.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Vortex implements SmartLifecycle {

    /**
     * The underlying, configured Reactor Netty HTTP server instance.
     */
    private final HttpServer httpServer;

    /**
     * Holds the disposable server resource once the server is bound to a port.
     */
    private DisposableServer disposableServer;

    /**
     * An atomic flag to track the running state of the server.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructs a new {@code Vortex} server manager.
     *
     * @param httpServer The pre-configured {@link HttpServer} instance to be managed.
     */
    public Vortex(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * Starts the HTTP server. This method is called by the Spring container upon application startup. It binds the
     * server to its configured port and logs the outcome.
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                disposableServer = httpServer.bindNow();
                Logger.info("Vortex server started successfully on port: {}", disposableServer.port());
            } catch (Exception e) {
                running.set(false);
                Logger.error("Failed to start Vortex server", e);
                throw new RuntimeException("Failed to bind Vortex server", e);
            }
        }
    }

    /**
     * Stops the HTTP server. This method is called by the Spring container during a graceful shutdown. It disposes of
     * the server resources and logs the outcome.
     */
    @Override
    public void stop() {
        if (running.compareAndSet(true, false) && disposableServer != null) {
            try {
                disposableServer.disposeNow();
                Logger.info("Vortex server stopped successfully on port: {}", disposableServer.port());
            } catch (Exception e) {
                Logger.error("Error while stopping Vortex server", e);
            }
        }
    }

    /**
     * Checks if the server is currently running.
     *
     * @return {@code true} if the server is running, {@code false} otherwise.
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

}

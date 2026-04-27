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
package org.miaixz.bus.vortex;

import org.miaixz.bus.logger.Logger;
import org.springframework.context.SmartLifecycle;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of the core Reactor Netty HTTP server for the Vortex application.
 * <p>
 * This class implements {@link SmartLifecycle} to integrate seamlessly with the Spring application context. It is
 * responsible for starting the {@link HttpServer} when the application starts and gracefully shutting it down when the
 * application stops.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * the server resources, closes the connection pool, and logs the outcome.
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

            try {
                ConnectionProvider connectionProvider = Holder.getConnectionProviderIfPresent();
                if (connectionProvider != null) {
                    connectionProvider.dispose();
                    Logger.info("ConnectionProvider closed successfully");
                }
            } catch (Exception e) {
                Logger.error("Error while closing ConnectionProvider", e);
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

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

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.SmartLifecycle;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.logger.Logger;

import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Manages the public and optional private Reactor Netty HTTP listeners for Vortex.
 * <p>
 * The {@link SmartLifecycle} integration binds the configured listeners on Spring startup and disposes them on
 * shutdown.
 *
 * @author Kimi Liu
 */
public class Vortex implements SmartLifecycle {

    /**
     * Identifies server lifecycle diagnostics before and after the HTTP listener is available.
     */
    private static final ErrorContext LIFECYCLE = new ErrorContext("vortex", "lifecycle", ErrorContext.Phase.LOCAL);

    /**
     * The underlying, configured Reactor Netty HTTP server instance.
     */
    private final HttpServer httpServer;

    /**
     * An atomic flag to track the running state of the server.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Holds the disposable server resource once the server is bound to a port.
     */
    private DisposableServer disposableServer;

    /**
     * Constructs a new {@code Vortex} server manager.
     *
     * @param httpServer The pre-configured {@link HttpServer} instance to be managed.
     */
    public Vortex(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * Binds the gateway listener during Spring startup.
     *
     * @throws RuntimeException if the listener cannot be bound
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                disposableServer = httpServer.bindNow();
                Logger.info(true, "Vortex", "Gateway listener started: port={}", disposableServer.port());
            } catch (Exception e) {
                running.set(false);
                if (disposableServer != null) {
                    disposableServer.disposeNow();
                }
                ErrorBoundary.diagnose(LIFECYCLE, "gateway-start", e, null);
                throw new RuntimeException("Failed to bind Vortex server", e);
            }
        }
    }

    /**
     * Disposes the gateway listener during Spring shutdown.
     * <p>
     * Shutdown then closes the outbound HTTP resources, memory sampler, and byte budgets, which also ends any queued
     * capacity waits.
     */
    @Override
    public void stop() {
        if (running.compareAndSet(true, false) && disposableServer != null) {
            try {
                disposableServer.disposeNow();
                Logger.info(false, "Vortex", "Gateway listener stopped: port={}", disposableServer.port());
            } catch (Exception e) {
                ErrorBoundary.diagnose(LIFECYCLE, "gateway-stop", e, null);
            }

            Egress.Http.close(LIFECYCLE);

            try {
                Holder.closeCapacityResources();
                Logger.info(false, "Vortex", "Runtime capacity resources closed");
            } catch (Exception e) {
                ErrorBoundary.diagnose(LIFECYCLE, "capacity-resources-close", e, null);
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

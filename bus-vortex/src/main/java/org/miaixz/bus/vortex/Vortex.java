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

import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Server class responsible for starting and managing an HTTP server based on Reactor Netty.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Vortex {

    /**
     * The Reactor Netty HTTP server instance, used for handling HTTP requests.
     */
    private final HttpServer httpServer;

    /**
     * The disposable server instance, representing the bound server resources.
     */
    private DisposableServer disposableServer;

    /**
     * Constructs a {@code Vortex} instance with the given HTTP server.
     *
     * @param httpServer The Reactor Netty HTTP server instance.
     */
    public Vortex(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * Initializes and starts the HTTP server. Binds the {@code httpServer} to the specified port and logs a success
     * message upon startup.
     */
    private void init() {
        disposableServer = httpServer.bindNow();
        Logger.info("reactor server start on port:{} success", disposableServer.port());
    }

    /**
     * Stops and disposes of the HTTP server. Releases server resources and logs a success message upon shutdown.
     */
    private void destroy() {
        disposableServer.disposeNow();
        Logger.info("reactor server stop on port:{} success", disposableServer.port());
    }

}

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
package org.miaixz.bus.starter.bridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import jakarta.annotation.Resource;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * The server-side component of the configuration center, implemented as a Vert.x {@link AbstractVerticle}.
 * <p>
 * This verticle starts an HTTP server that listens for requests from clients seeking configuration information. It
 * provides an endpoint to resolve and return configurations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BridgeVerticleService extends AbstractVerticle {

    private final BridgeProperties properties;

    @Resource
    private Vertx vertx;
    @Resource
    private Resolvable resolvable;

    /**
     * Constructs a new service instance with the given bridge properties.
     *
     * @param properties The configuration properties for the bridge.
     */
    public BridgeVerticleService(BridgeProperties properties) {
        this.properties = properties;
    }

    /**
     * Starts the Vert.x verticle, initializing and starting the HTTP server.
     * <p>
     * If a valid port is configured, this method creates an HTTP server and a router. It sets up a route at
     * {@code /profile/get} to handle configuration requests.
     * </p>
     */
    @Override
    public void start() {
        if (this.properties.getPort() <= 0 || this.properties.getPort() > 0xFFFF) {
            return; // Do not start the server if the port is invalid.
        }
        Router router = Router.router(vertx);
        router.post("/profile/get").handler(context -> {
            String result;
            try {
                BridgeProperties requestProps = JsonKit.toPojo(context.body().asString(), BridgeProperties.class);
                Object data = this.resolvable.find(requestProps);
                Message message = Message.builder().data(data).build();
                Logger.info("Request: {}, Response: {}", requestProps, message);
                result = JsonKit.toJsonString(message);
            } catch (Exception e) {
                Logger.error("Error getting profile", e);
                result = JsonKit.toJsonString(Message.builder().errcode("-1").errmsg(e.getMessage()).build());
            }
            context.response().putHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON).end(result);
        });

        vertx.createHttpServer().requestHandler(router).listen(this.properties.getPort());
        Logger.info("Vert.x configuration server is listening on port {}", this.properties.getPort());
    }

    /**
     * Stops the Vert.x verticle, closing the Vert.x instance and shutting down the server.
     */
    @Override
    public void stop() {
        if (ObjectKit.isNotEmpty(this.vertx)) {
            this.vertx.close();
            Logger.info("Vert.x configuration server stopped.");
        }
    }

}

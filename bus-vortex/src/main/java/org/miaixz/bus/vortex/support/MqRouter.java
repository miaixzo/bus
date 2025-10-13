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
package org.miaixz.bus.vortex.support;

import java.time.Duration;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.mq.MqService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * MQ strategy router, responsible for forwarding requests to a message queue. This class acts as a coordinator,
 * delegating the actual message sending to the MqProducerService.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MqRouter implements Router {

    /**
     * The service responsible for sending messages to the message queue. In a full Spring application, this would
     * typically be injected.
     */
    private final MqService service;

    /**
     * Constructs a new MqRequestRouter. It initializes the MqProducerService with the provided properties. Note: In a
     * Spring context, it's better to inject the service directly.
     */
    public MqRouter(MqService service) {
        // This is a fallback for non-Spring environments.
        // A better approach is to have Spring manage the lifecycle of MqProducerService.
        // For now, we create it here, but it won't be able to use @Resource for properties.
        // The properties would need to be loaded manually.
        this.service = service;
    }

    /**
     * Routes a client request to the message queue. This method reads the request body and delegates the sending
     * operation to the MqProducerService.
     *
     * @param request The client's {@link ServerRequest} object, containing request information.
     * @param context The request context, containing request parameters and configuration information.
     * @param assets  The configuration assets, containing configuration information for the target service.
     * @return A {@link Mono<ServerResponse>} indicating the status of the message forwarding.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        long startTime = System.currentTimeMillis();
        Logger.info("MQ Router: Routing request for topic: {}", assets.getMethod());

        return request.bodyToMono(String.class).flatMap(
                payload -> this.service.send(assets.getMethod(), payload, Duration.ofMillis(assets.getTimeout())))
                .then(Mono.defer(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Logger.info(
                            "MQ Router: Successfully forwarded request for topic: {} in {}ms",
                            assets.getMethod(),
                            duration);
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("{\"status\": \"Request forwarded to MQ\"}");
                })).onErrorResume(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Logger.error(
                            "MQ Router: Failed to forward request for topic: {} in {}ms",
                            assets.getMethod(),
                            duration,
                            e);
                    return ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("{\"error\": \"Failed to forward request to MQ: " + e.getMessage() + "\"}");
                });
    }

}

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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.mq.MqService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * A {@link Router} implementation for forwarding requests to a message queue (MQ).
 * <p>
 * This class acts as a coordinator for asynchronous messaging. It extracts the request body and uses an
 * {@link MqService} to send the payload to a message broker. It immediately returns a success response to the client,
 * acknowledging that the message has been accepted for processing, without waiting for the downstream consumer to
 * finish.
 *
 * @author Kimi Liu
 * @see MqService
 * @since Java 17+
 */
public class MqRouter implements Router {

    /**
     * The service responsible for sending messages to the message queue.
     */
    private final MqService service;

    /**
     * Constructs a new {@code MqRouter}.
     *
     * @param service The service that will perform the message sending.
     */
    public MqRouter(MqService service) {
        this.service = service;
    }

    /**
     * Routes a client request by sending its body as a message to a message queue.
     * <p>
     * This method retrieves the {@link Context} and {@link Assets} to determine the target MQ topic and timeout. It
     * then reads the request body and delegates the sending operation to the {@link MqService}. It provides an
     * immediate acknowledgment to the client.
     *
     * @param request The current {@link ServerRequest}.
     * @return A {@code Mono<ServerResponse>} indicating that the message has been successfully forwarded to the MQ.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            long startTime = System.currentTimeMillis();
            Logger.info("MQ Router: Routing request for topic: {}", assets.getMethod());

            // Determine if streaming mode is enabled
            boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

            return request.bodyToMono(String.class)
                    // .switchIfEmpty() handles cases where the body might be empty
                    .switchIfEmpty(Mono.just(Normal.EMPTY)).flatMap(payload -> this.service.send(assets, payload))
                    .then(Mono.defer(() -> {
                        long duration = System.currentTimeMillis() - startTime;
                        Logger.info(
                                "MQ Router: Successfully forwarded request for topic: {} in {}ms",
                                assets.getMethod(),
                                duration);

                        String responseJson = "{\"status\": \"Request forwarded to MQ\"}";

                        if (isStreaming) {
                            // STREAMING MODE: Use streaming execution
                            return executeStreaming(responseJson);
                        } else {
                            // ATOMIC MODE: Use buffering execution
                            return executeBuffering(responseJson);
                        }
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
        });
    }

    /**
     * Executes the MQ acknowledgment response in streaming mode.
     * <p>
     * Converts the response JSON into a flux of data buffers for streaming transfer.
     *
     * @param responseJson The JSON response string
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(String responseJson) {
        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

        // Convert response to streaming data buffers
        Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            return bufferFactory.wrap(bytes);
        });

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(dataFlux, DataBuffer.class);
    }

    /**
     * Executes the MQ acknowledgment response in atomic/buffering mode.
     * <p>
     * Buffers the complete response before sending.
     *
     * @param responseJson The JSON response string
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(String responseJson) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(responseJson);
    }

}

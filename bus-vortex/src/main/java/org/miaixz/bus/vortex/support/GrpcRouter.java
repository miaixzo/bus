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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.grpc.GrpcService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding requests to gRPC services.
 * <p>
 * This class acts as a coordinator for gRPC communication. It extracts the request body and uses a {@link GrpcService}
 * to invoke the gRPC method on the target service.
 *
 * @author Kimi Liu
 * @see GrpcService
 * @since Java 17+
 */
public class GrpcRouter implements Router {

    /**
     * The service responsible for executing gRPC calls.
     */
    private final GrpcService service;

    /**
     * Constructs a new {@code GrpcRouter}.
     *
     * @param service The service that will perform the gRPC invocation.
     */
    public GrpcRouter(GrpcService service) {
        this.service = service;
    }

    /**
     * Routes a client request by invoking a gRPC method on the target service.
     * <p>
     * This method retrieves the {@link Context} and {@link Assets} to determine the target gRPC service and method. It
     * then reads the request body and delegates the invocation to the {@link GrpcService}.
     *
     * @param request The current {@link ServerRequest}.
     * @return A {@code Mono<ServerResponse>} containing the response from the gRPC service.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            long startTime = System.currentTimeMillis();
            String ip = context.getX_request_ipv4();
            String method = request.methodName();
            String path = request.path();

            Logger.info(
                    true,
                    "gRPC",
                    "[{}] [{}] [{}] [GRPC_ROUTER_START] - Routing request to gRPC service: {}",
                    ip,
                    method,
                    path,
                    assets.getMethod());

            // Determine if streaming mode is enabled
            boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

            return request.bodyToMono(String.class).switchIfEmpty(Mono.just("{}")).flatMap(payload -> {
                // Invoke gRPC service
                Mono<String> responseMono = this.service.invoke(assets, payload);

                if (isStreaming) {
                    // STREAMING MODE: Use streaming execution
                    return executeStreaming(responseMono, ip, method, path, startTime, assets);
                } else {
                    // ATOMIC MODE: Use buffering execution
                    return executeBuffering(responseMono, ip, method, path, startTime, assets);
                }
            }).onErrorResume(e -> {
                long duration = System.currentTimeMillis() - startTime;
                Logger.error(
                        true,
                        "gRPC",
                        "[{}] [{}] [{}] [GRPC_ROUTER_ERROR] - Failed to invoke gRPC service: {} in {}ms",
                        ip,
                        method,
                        path,
                        assets.getMethod(),
                        duration,
                        e);
                return ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"error\": \"Failed to invoke gRPC service: " + e.getMessage() + "\"}");
            });
        });
    }

    /**
     * Executes the gRPC call in streaming mode.
     * <p>
     * Converts the gRPC response into a flux of data buffers for streaming transfer.
     *
     * @param responseMono The mono containing the gRPC response
     * @param ip           The client IP for logging
     * @param method       The HTTP method for logging
     * @param path         The request path for logging
     * @param startTime    The request start time for logging
     * @param assets       The asset configuration
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(
            Mono<String> responseMono,
            String ip,
            String method,
            String path,
            long startTime,
            Assets assets) {
        return responseMono.flatMap(response -> {
            long duration = System.currentTimeMillis() - startTime;
            Logger.info(
                    false,
                    "gRPC",
                    "[{}] [{}] [{}] [GRPC_ROUTER_SUCCESS_STREAM] - Successfully invoked gRPC service: {} in {}ms (streaming)",
                    ip,
                    method,
                    path,
                    assets.getMethod(),
                    duration);

            DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

            // Convert response to streaming data buffers
            Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
                byte[] bytes = response.getBytes(Charset.UTF_8);
                return bufferFactory.wrap(bytes);
            });

            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(dataFlux, DataBuffer.class);
        });
    }

    /**
     * Executes the gRPC call in atomic/buffering mode.
     * <p>
     * Buffers the complete gRPC response before sending.
     *
     * @param responseMono The mono containing the gRPC response
     * @param ip           The client IP for logging
     * @param method       The HTTP method for logging
     * @param path         The request path for logging
     * @param startTime    The request start time for logging
     * @param assets       The asset configuration
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(
            Mono<String> responseMono,
            String ip,
            String method,
            String path,
            long startTime,
            Assets assets) {
        return responseMono.flatMap(response -> {
            long duration = System.currentTimeMillis() - startTime;
            Logger.info(
                    false,
                    "gRPC",
                    "[{}] [{}] [{}] [GRPC_ROUTER_SUCCESS_ATOMIC] - Successfully invoked gRPC service: {} in {}ms (atomic)",
                    ip,
                    method,
                    path,
                    assets.getMethod(),
                    duration);

            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response);
        });
    }

}

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
package org.miaixz.bus.vortex.routing.grpc;

import java.time.Duration;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.routing.Coordinator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An executor for executing gRPC methods on remote services via HTTP gateway.
 * <p>
 * This executor uses HTTP as transport protocol instead of direct gRPC, avoiding third-party gRPC library dependencies.
 * gRPC-Web or gRPC-HTTP proxy is required on the server side to translate HTTP requests to gRPC calls.
 * <ul>
 * <li>Buffering mode (stream = 1 or null): Buffers the complete response before returning</li>
 * <li>Streaming mode (stream = 2): Streams the response in chunks</li>
 * </ul>
 * Generic type parameters: {@code Executor<String, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GrpcExecutor extends Coordinator<String, ServerResponse> {

    /**
     * Creates a gRPC executor.
     */
    public GrpcExecutor() {
    }

    /**
     * Executes a gRPC request using the provided context and String payload.
     * <p>
     * This method is required by the {@link org.miaixz.bus.vortex.Executor} interface. It invokes the gRPC method and
     * selects the appropriate execution strategy (streaming or buffering) based.
     *
     * @param context The request context containing the assets configuration
     * @param input   The String payload to send to the gRPC service
     * @return A Mono emitting the ServerResponse
     */
    @Override
    public Mono<ServerResponse> execute(Context context, String input) {
        Assets assets = context.getAssets();
        String payload = input;

        Mono<String> responseMono = Mono.fromCallable(() -> invoke(assets, payload))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());

        boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

        if (isStreaming) {
            return executeStreaming(responseMono, assets);
        } else {
            return executeBuffering(responseMono, assets);
        }
    }

    /**
     * Executes the gRPC call in streaming mode.
     * <p>
     * Converts the gRPC response into a flux of data buffers for streaming transfer.
     *
     * @param responseMono The mono containing the gRPC response
     * @param assets       The asset configuration
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(Mono<String> responseMono, Assets assets) {
        return responseMono.flatMap(response -> {
            Logger.info(
                    false,
                    "gRPC",
                    "[GRPC_SUCCESS_STREAM] - Successfully invoked gRPC service: {} (streaming)",
                    assets.getMethod());

            DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

            Flux<DataBuffer> dataFlux = Flux.interval(Duration.ofMillis(10)).take(1).map(i -> {
                byte[] bytes = response.getBytes(Charset.UTF_8);
                return bufferFactory.wrap(bytes);
            });

            return ServerResponse.ok().header(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .body(dataFlux, DataBuffer.class);
        });
    }

    /**
     * Executes the gRPC call in atomic/buffering mode.
     * <p>
     * Buffers the complete gRPC response before sending.
     *
     * @param responseMono The mono containing the gRPC response
     * @param assets       The asset configuration
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(Mono<String> responseMono, Assets assets) {
        return responseMono.flatMap(response -> {
            Logger.info(
                    false,
                    "gRPC",
                    "[GRPC_SUCCESS_ATOMIC] - Successfully invoked gRPC service: {} (atomic)",
                    assets.getMethod());

            return ServerResponse.ok().header(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON).bodyValue(response);
        });
    }

    /**
     * Invokes a gRPC method via HTTP gateway.
     * <p>
     * This method sends HTTP POST requests to a gRPC-Web/gRPC-HTTP gateway, which translates the request to actual gRPC
     * calls. The request and response payloads are in JSON format for compatibility.
     *
     * @param assets  The configuration containing the gRPC service details (host, port, method).
     * @param payload The JSON string content of the request message.
     * @return The JSON response from the gRPC service.
     */
    public String invoke(Assets assets, String payload) {
        try {
            String fullMethodName = assets.getMethod();

            Logger.info(
                    true,
                    "gRPC",
                    "Invoking gRPC method via HTTP: {} on {}:{}",
                    fullMethodName,
                    assets.getHost(),
                    assets.getPort());

            String url = buildGrpcUrl(assets, fullMethodName);

            Logger.info(true, "gRPC", "gRPC method {} invoked successfully", fullMethodName);
            return Httpx.post(url, payload, MediaType.APPLICATION_JSON);

        } catch (Exception e) {
            Logger.error("Failed to invoke gRPC method '{}'", assets.getMethod(), e);
            throw new RuntimeException("Failed to invoke gRPC method: " + assets.getMethod(), e);
        }
    }

    /**
     * Builds the URL for gRPC gateway call.
     * <p>
     * Standard gRPC-Web URLs follow the pattern: http://host:port/package.Service/Method
     *
     * @param assets         The configuration for the target gRPC service.
     * @param fullMethodName The full gRPC method name (package.Service/Method).
     * @return The HTTP URL for the gRPC gateway.
     */
    private String buildGrpcUrl(Assets assets, String fullMethodName) {
        StringBuilder url = new StringBuilder();
        url.append("http://").append(assets.getHost()).append(Symbol.COLON).append(assets.getPort());

        if (StringKit.isNotEmpty(fullMethodName)) {
            url.append(Symbol.SLASH).append(fullMethodName);
        }

        return url.toString();
    }

}

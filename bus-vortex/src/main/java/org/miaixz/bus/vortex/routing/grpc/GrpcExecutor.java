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

import java.net.URI;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Delivery;
import org.miaixz.bus.vortex.Egress;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.routing.Coordinator;
import org.miaixz.bus.vortex.routing.StreamingRelay;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An executor for executing gRPC methods on remote services via HTTP gateway.
 * <p>
 * This executor uses HTTP as transport protocol instead of direct gRPC, avoiding third-party gRPC library dependencies.
 * gRPC-Web or gRPC-HTTP proxy is required on the server side to translate HTTP requests to gRPC calls.
 * <ul>
 * <li>{@code stream=1}: validates Content-Length and atomically relays a bounded response</li>
 * <li>{@code stream=2}: relays a latency-sensitive response under realtime-stream admission</li>
 * <li>{@code stream=3}: relays a file response under download admission and progress protection</li>
 * </ul>
 * Generic type parameters: {@code Executor<String, ServerResponse>}
 *
 * @author Kimi Liu
 */
public class GrpcExecutor extends Coordinator<String, ServerResponse> {

    /**
     * Creates a gRPC executor.
     */
    public GrpcExecutor() {
        // No initialization required.
    }

    /**
     * Executes a gRPC request using the provided context and String payload.
     * <p>
     * This method is required by the {@link org.miaixz.bus.vortex.Executor} interface. It invokes the gRPC method and
     * selects the strict response delivery from the route asset.
     *
     * @param context The request context containing the assets configuration
     * @param input   The String payload to send to the gRPC service
     * @return A Mono emitting the ServerResponse
     */
    @Override
    public Mono<ServerResponse> execute(Context context, String input) {
        Assets assets = context.getAssets();
        String payload = input;

        Delivery delivery = Delivery.of(assets.getStream());
        boolean isStreaming = delivery != Delivery.BUFFERED;

        if (isStreaming) {
            return executeStreaming(assets, payload, delivery);
        } else {
            return executeBuffering(context, assets, payload);
        }
    }

    /**
     * Executes the gRPC call in streaming mode.
     * <p>
     * Relays the gRPC response without aggregation and retains a mode-specific streaming lease until termination.
     *
     * @param assets   The asset configuration
     * @param payload  The JSON payload to send to the gRPC gateway
     * @param delivery realtime-stream or download delivery mode
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(Assets assets, String payload, Delivery delivery) {
        return request(assets, payload).retrieve().onStatus(status -> true, clientResponse -> Mono.empty())
                .toEntityFlux(DataBuffer.class).flatMap(responseEntity -> {
                    Logger.info(
                            false,
                            "Vortex",
                            "GRPC service invocation completed: protocol=grpc, event=GRPC_SUCCESS_STREAM, service={}, mode=streaming",
                            assets.getMethod());

                    ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
                    copyDownstreamHeaders(responseBuilder, responseEntity.getHeaders());
                    Flux<DataBuffer> dataFlux = responseEntity.getBody() == null ? Flux.empty()
                            : responseEntity.getBody().doOnDiscard(PooledDataBuffer.class, Octets::release);
                    dataFlux = StreamingRelay.relay(dataFlux, delivery);

                    return responseBuilder.body(BodyInserters.fromDataBuffers(dataFlux));
                });
    }

    /**
     * Executes the gRPC call in atomic/buffering mode.
     * <p>
     * Reserves either the declared Content-Length or the configured unknown-length maximum and retains the response
     * lease until the network write terminates.
     *
     * @param context request context retained for response processing
     * @param assets  The asset configuration
     * @param payload The JSON payload to send to the gRPC gateway
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(Context context, Assets assets, String payload) {
        return request(assets, payload).retrieve().onStatus(status -> true, clientResponse -> Mono.empty())
                .toEntityFlux(DataBuffer.class).flatMap(responseEntity -> {
                    Logger.info(
                            false,
                            "Vortex",
                            "GRPC service invocation completed: protocol=grpc, event=GRPC_SUCCESS_ATOMIC, service={}, mode=atomic",
                            assets.getMethod());

                    ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
                    copyDownstreamHeaders(responseBuilder, responseEntity.getHeaders());
                    Flux<DataBuffer> bodyFlux = responseEntity.getBody() == null ? Flux.empty()
                            : responseEntity.getBody();
                    boolean bodyForbidden = responseEntity.getStatusCode().is1xxInformational()
                            || responseEntity.getStatusCode().value() == 204
                            || responseEntity.getStatusCode().value() == 304;
                    if (bodyForbidden) {
                        return Octets.discard(bodyFlux).then(responseBuilder.build());
                    }
                    long declaredLength = responseEntity.getHeaders().getContentLength();
                    if (declaredLength > Math.toIntExact(Holder.get().getMaxBufferedResponseSize())) {
                        return Octets.discard(bodyFlux).then(
                                Mono.error(
                                        new DataBufferLimitException(
                                                "Atomic gRPC response exceeds buffered limit of "
                                                        + Math.toIntExact(Holder.get().getMaxBufferedResponseSize())
                                                        + " bytes")));
                    }
                    return Octets.readForRelay(
                            bodyFlux,
                            Math.toIntExact(Holder.get().getMaxBufferedResponseSize()),
                            Holder.responseBufferBudget(),
                            declaredLength).flatMap(bufferedBody -> {
                                int bodyLength = bufferedBody.length();
                                if (declaredLength >= 0 && bodyLength != declaredLength) {
                                    bufferedBody.close();
                                    return Mono.error(
                                            new DataBufferLimitException(
                                                    "Atomic gRPC response length mismatch: declared=" + declaredLength
                                                            + ", actual=" + bodyLength));
                                }
                                return Octets.own(
                                        responseBuilder.contentLength(bodyLength)
                                                .body(BodyInserters.fromDataBuffers(Octets.chunks(bufferedBody))),
                                        bufferedBody);
                            });
                });
    }

    /**
     * Invokes a gRPC method via HTTP gateway.
     * <p>
     * This method sends HTTP POST requests to a gRPC-Web/gRPC-HTTP gateway, which translates the request to actual gRPC
     * calls. The bounded JSON response may use either Content-Length or chunked transfer encoding and retains its
     * response-byte lease until conversion to the returned string completes.
     *
     * @param assets  The configuration containing the gRPC service details (host, port, method).
     * @param payload The JSON string content of the request message.
     * @return The JSON response from the gRPC service.
     */
    public Mono<String> invoke(Assets assets, String payload) {
        try {
            String fullMethodName = assets.getMethod();

            Logger.info(
                    true,
                    "Vortex",
                    "Invoking gRPC method via HTTP: protocol=grpc, {} on {}:{}",
                    fullMethodName,
                    assets.getHost(),
                    assets.getPort());

            URI uri = UrlKit.toURI(buildGrpcUrl(assets, fullMethodName));

            Logger.info(true, "Vortex", "GRPC method {} invoked successfully: protocol=grpc", fullMethodName);
            return Egress.request(HttpMethod.POST, uri).header(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .bodyValue(payload).retrieve().onStatus(status -> true, clientResponse -> Mono.empty())
                    .toEntityFlux(DataBuffer.class).flatMap(
                            responseEntity -> Octets
                                    .readForParsing(
                                            responseEntity.getBody() == null ? Flux.empty() : responseEntity.getBody(),
                                            Math.toIntExact(Holder.get().getMaxBufferedResponseSize()),
                                            Holder.responseBufferBudget(),
                                            responseEntity.getHeaders().getContentLength())
                                    .flatMap(
                                            bufferedBody -> Mono
                                                    .fromCallable(() -> new String(bufferedBody.bytes(), Charset.UTF_8))
                                                    .doFinally(signalType -> bufferedBody.close())));

        } catch (Exception e) {
            Logger.error(false, "Vortex", "Failed to invoke gRPC method '{}': protocol=grpc", assets.getMethod(), e);
            return Mono.error(new RuntimeException("Failed to invoke gRPC method: " + assets.getMethod(), e));
        }
    }

    /**
     * Creates one outbound gRPC gateway request through the shared HTTP client.
     *
     * @param assets  The configuration for the target gRPC service.
     * @param payload The JSON payload to send.
     * @return A WebClient request ready for response handling.
     */
    private WebClient.RequestHeadersSpec<?> request(Assets assets, String payload) {
        URI uri = UrlKit.toURI(buildGrpcUrl(assets, assets.getMethod()));
        return Egress.request(HttpMethod.POST, uri).header(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .bodyValue(payload);
    }

    /**
     * Copies safe downstream headers into the client response builder.
     *
     * @param responseBuilder   target server response builder
     * @param downstreamHeaders downstream response headers
     */
    private void copyDownstreamHeaders(ServerResponse.BodyBuilder responseBuilder, HttpHeaders downstreamHeaders) {
        responseBuilder.headers(headers -> {
            headers.addAll(downstreamHeaders);
            headers.remove(HttpHeaders.HOST);
            headers.remove(HttpHeaders.TRANSFER_ENCODING);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        });
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
        url.append(Protocol.HTTP_PREFIX).append(assets.getHost()).append(Symbol.COLON).append(assets.getPort());

        if (StringKit.isNotEmpty(fullMethodName)) {
            url.append(Symbol.SLASH).append(fullMethodName);
        }

        return UrlKit.normalize(url.toString(), false);
    }

}

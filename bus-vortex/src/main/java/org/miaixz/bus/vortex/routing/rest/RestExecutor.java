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
package org.miaixz.bus.vortex.routing.rest;

import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.routing.Coordinator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

/**
 * The core executor for executing RESTful HTTP requests to downstream services.
 * <p>
 * This executor encapsulates all the logic for using Spring's {@link WebClient} to act as a reverse proxy. It builds
 * the downstream request based on the provided {@link Assets} and {@link Context}, sends it, and then transforms the
 * downstream response back into a {@link ServerResponse} for the original client.
 * <p>
 * Generic type parameters: {@code Executor<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RestExecutor extends Coordinator<ServerRequest, ServerResponse> {

    /**
     * A cached, pre-configured {@link ExchangeStrategies} instance for the {@link WebClient}.
     * <p>
     * This is initialized statically to avoid redundant object creation. It sets a generous memory limit for codecs to
     * prevent errors when handling moderately large response bodies.
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128))).build();

    /**
     * A cached {@link HttpClient} instance that uses the shared connection pool.
     * <p>
     * This is initialized once and reused for all requests to avoid redundant HttpClient creation.
     */
    private static volatile HttpClient CACHED_HTTP_CLIENT;

    /**
     * Default constructor.
     * <p>
     * The HTTP connection pool is obtained from {@link Holder#connectionProvider()} which is configured globally via
     * {@code vortex.performance.maxConnections} property.
     */
    public RestExecutor() {

    }

    /**
     * Gets the cached HTTP client, initializing it lazily if needed.
     * <p>
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return The cached HttpClient instance
     */
    private HttpClient getHttpClient() {
        if (CACHED_HTTP_CLIENT == null) {
            synchronized (RestExecutor.class) {
                if (CACHED_HTTP_CLIENT == null) {
                    CACHED_HTTP_CLIENT = HttpClient.create(Holder.connectionProvider());
                    Logger.info(true, "Vortex", "Shared HTTP client initialized: protocol=http");
                }
            }
        }
        return CACHED_HTTP_CLIENT;
    }

    /**
     * Executes the HTTP request using the provided context and ServerRequest.
     * <p>
     * This method is required by the {@link org.miaixz.bus.vortex.Executor} interface. For REST executors, the
     * {@code input} parameter is typed as {@link ServerRequest} for compile-time type safety.
     *
     * @param context The request context
     * @param request The ServerRequest object (strongly typed)
     * @return A Mono emitting the ServerResponse, or error if validation fails
     */
    @NonNull
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest request) {
        final String method = request.methodName();
        final String path = request.path();
        final String ip = context.getX_request_ip();

        String baseUrl = buildBaseUrl(context);
        Logger.info(
                true,
                "Vortex",
                "Base URL: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_BASEURL, {}",
                ip,
                method,
                path,
                baseUrl);

        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build();

        Assets assets = context.getAssets();
        String targetUri = buildTargetUri(assets, context);
        Logger.info(
                true,
                "Vortex",
                "Target URI: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_URI, {}",
                ip,
                method,
                path,
                targetUri);

        WebClient.RequestBodySpec bodySpec = webClient.method(HttpMethod.valueOf(context.getHttpMethod().value()))
                .uri(targetUri);
        Logger.info(
                true,
                "Vortex",
                "HTTP method: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_METHOD, {}",
                ip,
                method,
                path,
                context.getHttpMethod());

        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });
        Logger.debug(
                true,
                "Vortex",
                "Request header snapshot: protocol=http, clientIp={}, method={}, path={}, targetUri={}",
                ip,
                method,
                path,
                targetUri);
        Logger.debug(
                true,
                "Vortex",
                "Request headers: protocol=http, clientIp={}, method={}, path={}, headers={}",
                ip,
                method,
                path,
                request.headers().asHttpHeaders().toSingleValueMap());
        Logger.debug(
                true,
                "Vortex",
                "Request parameters: protocol=http, clientIp={}, method={}, path={}, parameters={}",
                ip,
                method,
                path,
                context.getParameters());
        Logger.info(
                true,
                "Vortex",
                "Headers configured: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_HEADERS",
                ip,
                method,
                path);

        if (context.getHttpMethod() != HTTP.Method.GET) {
            MediaType mediaType = request.headers().contentType().orElse(null);
            if (mediaType != null) {
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    handleMultipartBody(bodySpec, context, ip, method, path);
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    handleJsonRequestBody(bodySpec, context, ip, method, path);
                } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                    handleFormRequestBody(bodySpec, context, ip, method, path);
                } else {
                    Logger.info(
                            true,
                            "Vortex",
                            "Unsupported media type: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_UNSUPPORTED, {}",
                            ip,
                            method,
                            path,
                            mediaType);
                    handleFormRequestBody(bodySpec, context, ip, method, path);
                }
            } else {
                Logger.info(
                        true,
                        "Vortex",
                        "No Content-Type header, defaulting to form data: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_DEFAULT",
                        ip,
                        method,
                        path);
                handleFormRequestBody(bodySpec, context, ip, method, path);
            }
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "GET request, no content processing: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_GET",
                    ip,
                    method,
                    path);
        }

        Logger.info(
                true,
                "Vortex",
                "Sending request: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_SEND",
                ip,
                method,
                path);

        boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

        if (isStreaming) {
            Logger.info(
                    true,
                    "Vortex",
                    "Using STREAMING mode (stream=2, exchangeToMono): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
            return executeStreaming(bodySpec, ip, method, path);
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "Using ATOMIC mode (stream=1, retrieve.toEntity): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
            return executeBuffering(bodySpec, ip, method, path);
        }
    }

    /**
     * Handles a JSON request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleJsonRequestBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String ip,
            String method,
            String path) {
        Map<String, Object> params = context.getParameters();
        if (!params.isEmpty()) {
            Mono<String> jsonBodyMono = Mono.fromCallable(() -> {
                String json = JsonKit.toJsonString(params);
                String fixed = fixJsonEncoding(json);
                int backslashCount = fixed.length() - fixed.replace("\\", "").length();
                Logger.debug(
                        true,
                        "Vortex",
                        "JSON prepared: protocol=http, clientIp={}, method={}, path={}, event=HTTP_BEFORE_SEND, backslashes={}, chars={}",
                        ip,
                        method,
                        path,
                        backslashCount,
                        fixed.length());
                return fixed;
            }).subscribeOn(Schedulers.boundedElastic()).doOnNext(jsonString -> {
                Logger.debug(
                        true,
                        "Vortex",
                        "JSON normalized: protocol=http, clientIp={}, method={}, path={}, event=HTTP_JSON_AFTER_FIX, chars={}",
                        ip,
                        method,
                        path,
                        jsonString.length());
            });

            bodySpec.contentType(MediaType.APPLICATION_JSON).body(jsonBodyMono, String.class);

            Logger.info(
                    true,
                    "Vortex",
                    "JSON content configured: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_JSON, parameterCount={}",
                    ip,
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "No JSON parameters to configure: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_JSON",
                    ip,
                    method,
                    path);
        }
    }

    /**
     * Handles a form-urlencoded request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleFormRequestBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String ip,
            String method,
            String path) {
        Map<String, Object> params = context.getParameters();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach((k, v) -> multiValueMap.add(k, String.valueOf(v)));
            bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(multiValueMap);
            Logger.info(
                    true,
                    "Vortex",
                    "Form content configured: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_FORM, parameterCount={}",
                    ip,
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "No form parameters to configure: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_FORM",
                    ip,
                    method,
                    path);
        }
    }

    /**
     * Handles a multipart/form-data request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleMultipartBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String ip,
            String method,
            String path) {
        Map<String, Part> fileParts = context.getFileParts();
        Map<String, Object> params = context.getParameters();
        if (!fileParts.isEmpty() || !params.isEmpty()) {
            MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
            fileParts.forEach(multipartData::add);
            params.forEach((k, v) -> multipartData.add(k, String.valueOf(v)));
            bodySpec.body(BodyInserters.fromMultipartData(multipartData));
            Logger.info(
                    true,
                    "Vortex",
                    "Multipart content configured: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_MULTIPART, fileCount={}, parameterCount={}",
                    ip,
                    method,
                    path,
                    fileParts.size(),
                    params.size());
        }
    }

    /**
     * Builds the target URI for the downstream request.
     *
     * @param assets  The configured assets for the target executor.
     * @param context The request context.
     * @return The constructed target URI string, including query parameters.
     */
    private String buildTargetUri(Assets assets, Context context) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());

        Map<String, String> query = context.getQuery();
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();

        if (!query.isEmpty()) {
            query.forEach((key, value) -> {
                if (!Args.isForwardingControlParameter(key)) {
                    multiValueMap.add(key, value);
                }
            });
        }

        if (context.getHttpMethod() == HTTP.Method.GET) {
            Map<String, Object> parameters = context.getParameters();
            if (!parameters.isEmpty()) {
                parameters.forEach((k, v) -> {
                    multiValueMap.remove(k);
                    multiValueMap.add(k, String.valueOf(v));
                });
            }
        }

        if (!multiValueMap.isEmpty()) {
            builder.queryParams(multiValueMap);
        }

        return builder.build().toUriString();
    }

    /**
     * **New private method.** Handles the execution for a STREAMING request (e.g., SSE). This uses the low-memory
     * `exchangeToMono` method.
     */
    private Mono<ServerResponse> executeStreaming(
            WebClient.RequestBodySpec bodySpec,
            String ip,
            String method,
            String path) {
        return bodySpec.exchangeToMono(clientResponse -> {
            ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(clientResponse.statusCode());
            Logger.debug(
                    false,
                    "Vortex",
                    "Downstream response headers: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_HEADERS, {}",
                    ip,
                    method,
                    path,
                    clientResponse.headers().asHttpHeaders());

            responseBuilder.headers(headers -> {
                headers.addAll(clientResponse.headers().asHttpHeaders());
                headers.remove(HttpHeaders.HOST);
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            });

            Flux<DataBuffer> bodyFlux = clientResponse.bodyToFlux(DataBuffer.class).doOnNext(dataBuffer -> {
                Logger.debug(
                        false,
                        "Vortex",
                        "Received data chunk, size: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_STREAM_CHUNK, {} bytes",
                        ip,
                        method,
                        path,
                        dataBuffer.readableByteCount());
            });

            return responseBuilder.body(bodyFlux, DataBuffer.class);
        }).doOnSubscribe(
                subscription -> Logger.info(
                        true,
                        "Vortex",
                        "Request subscribed (Streaming).: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_SUBSCRIBE",
                        ip,
                        method,
                        path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Vortex",
                                "Successfully built ServerResponse with status: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_SUCCESS, {} (Streaming)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Vortex",
                                "Request FAILED (Streaming): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_ERROR, {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Vortex",
                                "Request was cancelled by client (Streaming).: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_CANCEL",
                                ip,
                                method,
                                path));
    }

    /**
     * **New private method.** Handles the execution for a UNARY request (e.g., Servlet API). This uses the compatible
     * `retrieve().toEntity()` buffering method.
     */
    private Mono<ServerResponse> executeBuffering(
            WebClient.RequestBodySpec bodySpec,
            String ip,
            String method,
            String path) {
        return bodySpec.retrieve().toEntity(DataBuffer.class).flatMap(responseEntity -> {
            Logger.info(
                    false,
                    "Vortex",
                    "Received buffered status: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_BUFFERED, {}",
                    ip,
                    method,
                    path,
                    responseEntity.getStatusCode());
            Logger.info(
                    false,
                    "Vortex",
                    "Downstream response headers: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_HEADERS, {}",
                    ip,
                    method,
                    path,
                    responseEntity.getHeaders());

            DataBuffer body = responseEntity.getBody();
            if (body != null && body.readableByteCount() > 0) {
                Logger.info(
                        false,
                        "Vortex",
                        "Received buffered content: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_CONTENT_BUFFERED, bytes={}",
                        ip,
                        method,
                        path,
                        body.readableByteCount());
            } else {
                Logger.warn(
                        false,
                        "Vortex",
                        "Received buffered content is empty: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_CONTENT_BUFFERED",
                        ip,
                        method,
                        path);
            }

            ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
            responseBuilder.headers(headers -> {
                headers.addAll(responseEntity.getHeaders());
                headers.remove(HttpHeaders.HOST);
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            });

            if (body != null && body.readableByteCount() > 0) {
                return responseBuilder.body(Mono.just(body), DataBuffer.class);
            } else {
                return responseBuilder.build();
            }
        }).doOnSubscribe(
                subscription -> Logger.info(
                        true,
                        "Vortex",
                        "Request subscribed (Buffering).: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_SUBSCRIBE",
                        ip,
                        method,
                        path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Vortex",
                                "Successfully built ServerResponse with status: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_SUCCESS, {} (Buffering)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Vortex",
                                "Request FAILED (Buffering): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROOVER_ERROR, {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Vortex",
                                "Request was cancelled by client (Buffering).: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_CANCEL",
                                ip,
                                method,
                                path));
    }

}

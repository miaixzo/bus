/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.support.rest;

import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.support.Coordinator;
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
 * @since Java 17+
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
                    Logger.info(true, "RestExecutor", "HttpClient initialized with shared connection pool");
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
     * @param input   The ServerRequest object (strongly typed)
     * @return A Mono emitting the ServerResponse, or error if validation fails
     */
    @NonNull
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest input) {
        ServerRequest request = input;
        Assets assets = context.getAssets();

        final String method = request.methodName();
        final String path = request.path();
        final String ip = context.getX_request_ipv4();

        // 1. Build the base URL for the target service (synchronous, no I/O involved)
        String baseUrl = buildBaseUrl(context);
        Logger.info(true, "Http", "[{}] [{}] [{}] [HTTP_ROUTER_BASEURL] - Base URL: {}", ip, method, path, baseUrl);

        // 2. Build a WebClient with cached HTTP client (timeout is handled at VortexHandler level)
        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build();

        // 3. Build and execute the request.
        String targetUri = buildTargetUri(assets, context);
        Logger.info(true, "Http", "[{}] [{}] [{}] [HTTP_ROUTER_URI] - Target URI: {}", ip, method, path, targetUri);

        WebClient.RequestBodySpec bodySpec = webClient.method(context.getHttpMethod()).uri(targetUri);
        Logger.info(
                true,
                "Http",
                "[{}] [{}] [{}] [HTTP_ROUTER_METHOD] - HTTP method: {}",
                ip,
                method,
                path,
                context.getHttpMethod());

        // 4. Configure request headers, copying from the original request and cleaning up as needed.
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });
        Logger.info(true, "Http", "[{}] [{}] [{}] [HTTP_ROUTER_HEADERS] - Headers configured", ip, method, path);

        // 5. Handle the request body, if applicable (i.e., for non-GET requests).
        if (!HttpMethod.GET.equals(context.getHttpMethod())) {
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
                            "Http",
                            "[{}] [{}] [{}] [HTTP_ROUTER_UNSUPPORTED] - Unsupported media type: {}",
                            ip,
                            method,
                            path,
                            mediaType);
                    handleFormRequestBody(bodySpec, context, ip, method, path);
                }
            } else {
                Logger.info(
                        true,
                        "Http",
                        "[{}] [{}] [{}] [HTTP_ROUTER_DEFAULT] - No Content-Type header, defaulting to form data",
                        ip,
                        method,
                        path);
                handleFormRequestBody(bodySpec, context, ip, method, path);
            }
        } else {
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_GET] - GET request, no body processing",
                    ip,
                    method,
                    path);
        }

        // 6. Send the request and process the response (timeout and retry are handled at VortexHandler level).
        Logger.info(true, "Http", "[{}] [{}] [{}] [HTTP_ROUTER_SEND] - Sending request", ip, method, path);

        // Choose the execution strategy based on the asset's stream configuration.
        boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

        if (isStreaming) {
            // ** STRATEGY 1: STREAMING (stream = 2) **
            // Use the low-memory exchangeToMono for streaming responses (SSE, chunked transfer).
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_STRATEGY] - Using STREAMING mode (stream=2, exchangeToMono)",
                    ip,
                    method,
                    path);
            return executeStreaming(bodySpec, ip, method, path);
        } else {
            // ** STRATEGY 2: ATOMIC/BUFFERING (stream = 1 or null) **
            // Use the buffering retrieve().toEntity() for standard responses.
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_STRATEGY] - Using ATOMIC mode (stream=1, retrieve.toEntity)",
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
            // **OPTIMIZATION:** Wrap synchronous, CPU-bound JSON serialization
            // in fromCallable and offload it from the event loop.
            // **GRAALVM FIX**: Apply Native Image encoding fix for FastJSON unsafe operations
            Mono<String> jsonBodyMono = Mono.fromCallable(() -> {
                String json = JsonKit.toJsonString(params);
                String fixed = fixJsonEncoding(json);
                // Log what will actually be sent
                int backslashCount = fixed.length() - fixed.replace("\\", "").length();
                Logger.debug(
                        true,
                        "Http",
                        "[{}] [{}] [{}] [HTTP_BEFORE_SEND] - Backslashes: {}, First 300 chars: {}",
                        ip,
                        method,
                        path,
                        backslashCount,
                        fixed.length() > 300 ? fixed.substring(0, 300) + "..." : fixed);
                return fixed;
            }).subscribeOn(Schedulers.boundedElastic()).doOnNext(jsonString -> {
                // Log the exact JSON string after fix
                Logger.debug(
                        true,
                        "Http",
                        "[{}] [{}] [{}] [HTTP_JSON_AFTER_FIX] - Length: {}, Full JSON: {}",
                        ip,
                        method,
                        path,
                        jsonString.length(),
                        jsonString);
            });

            // Use body() for reactive types (Publisher)
            bodySpec.contentType(MediaType.APPLICATION_JSON).body(jsonBodyMono, String.class);

            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_JSON] - JSON body configured with {} parameters (async generation)",
                    ip,
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_JSON] - No JSON parameters to configure",
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
            // bodyValue is fine for MultiValueMap, WebClient handles it efficiently.
            bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(multiValueMap);
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_FORM] - Form body configured with {} parameters",
                    ip,
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_FORM] - No form parameters to configure",
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
            // fromMultipartData is non-blocking and supports streaming.
            bodySpec.body(BodyInserters.fromMultipartData(multipartData));
            Logger.info(
                    true,
                    "Http",
                    "[{}] [{}] [{}] [HTTP_ROUTER_MULTIPART] - Multipart body configured with {} files and {} params",
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
     * @return The constructed target URI string, including query parameters for GET requests.
     */
    private String buildTargetUri(Assets assets, Context context) {
        // This is fast, non-blocking URI building.
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());
        if (HttpMethod.GET.equals(context.getHttpMethod())) {
            Map<String, Object> params = context.getParameters();
            if (!params.isEmpty()) {
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
                params.forEach((k, v) -> multiValueMap.add(k, String.valueOf(v)));
                builder.queryParams(multiValueMap);
            }
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
        // This is your provided code block (streaming).
        return bodySpec.exchangeToMono(clientResponse -> {
            ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(clientResponse.statusCode());

            responseBuilder.headers(headers -> {
                headers.addAll(clientResponse.headers().asHttpHeaders());
                headers.remove(HttpHeaders.HOST);
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            });

            // Stream the response body directly, with logging
            Flux<DataBuffer> bodyFlux = clientResponse.bodyToFlux(DataBuffer.class).doOnNext(dataBuffer -> {
                Logger.debug(
                        false,
                        "Http",
                        "[{}] [{}] [{}] [HTTP_ROUTER_RECV_STREAM_CHUNK] - Received data chunk, size: {} bytes",
                        ip,
                        method,
                        path,
                        dataBuffer.readableByteCount());
            });

            return responseBuilder.body(bodyFlux, DataBuffer.class);
        }).doOnSubscribe(
                subscription -> Logger.info(
                        true,
                        "Http",
                        "[{}] [{}] [{}] [HTTP_ROUTER_SUBSCRIBE] - Request subscribed (Streaming).",
                        ip,
                        method,
                        path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_SUCCESS] - Successfully built ServerResponse with status: {} (Streaming)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_ERROR] - Request FAILED (Streaming): {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_CANCEL] - Request was cancelled by client (Streaming).",
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
        // This is the "buffering" fix that works with traditional Servlet backends.
        return bodySpec.retrieve().toEntity(DataBuffer.class) // <-- Buffers the *entire* response
                .flatMap(responseEntity -> {
                    // responseEntity is a ResponseEntity<DataBuffer>
                    Logger.info(
                            false,
                            "Http",
                            "[{}] [{}] [{}] [HTTP_ROUTER_RECV_BUFFERED] - Received buffered status: {}",
                            ip,
                            method,
                            path,
                            responseEntity.getStatusCode());

                    DataBuffer body = responseEntity.getBody();
                    if (body != null && body.readableByteCount() > 0) {
                        Logger.info(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_RECV_BODY_BUFFERED] - Received buffered body, size: {} bytes",
                                ip,
                                method,
                                path,
                                body.readableByteCount());
                    } else {
                        Logger.warn(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_RECV_BODY_BUFFERED] - Received buffered body, but it is NULL or EMPTY",
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
                        // Send the buffered body
                        return responseBuilder.body(Mono.just(body), DataBuffer.class);
                    } else {
                        // Send an empty response
                        return responseBuilder.build();
                    }
                })
                .doOnSubscribe(
                        subscription -> Logger.info(
                                true,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_SUBSCRIBE] - Request subscribed (Buffering).",
                                ip,
                                method,
                                path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_SUCCESS] - Successfully built ServerResponse with status: {} (Buffering)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROOVER_ERROR] - Request FAILED (Buffering): {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Http",
                                "[{}] [{}] [{}] [HTTP_ROUTER_CANCEL] - Request was cancelled by client (Buffering).",
                                ip,
                                method,
                                path));
    }

}

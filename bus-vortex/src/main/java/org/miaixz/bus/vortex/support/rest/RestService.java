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
package org.miaixz.bus.vortex.support.rest;

import java.time.Duration;
import java.util.Map;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
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

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * The core executor for forwarding requests to downstream RESTful HTTP services.
 * <p>
 * This service encapsulates all the logic for using Spring's {@link WebClient} to act as a reverse proxy. It builds the
 * downstream request based on the provided {@link Assets} and {@link Context}, sends it, and then transforms the
 * downstream response back into a {@link ServerResponse} for the original client.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RestService {

    /**
     * A cached, pre-configured {@link ExchangeStrategies} instance for the {@link WebClient}.
     * <p>
     * This is initialized statically to avoid redundant object creation. It sets a generous memory limit for codecs to
     * prevent errors when handling moderately large response bodies.
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128))).build();

    /**
     * A shared, reusable Connection Provider (Connection Pool) for all requests.
     */
    private static final ConnectionProvider SHARED_CONNECTION_PROVIDER = ConnectionProvider.create("vortex-http-pool");

    /**
     * Executes the full reverse-proxy logic for an HTTP request.
     * <p>
     * This method orchestrates the entire process of forwarding an incoming {@link ServerRequest} to an external HTTP
     * service. It involves building the target URL, dynamically configuring an
     * {@link reactor.netty.http.client.HttpClient} with the correct timeout strategy, creating a {@link WebClient}
     * instance, and then sending the request.
     *
     * @param request The original incoming {@link ServerRequest}.
     * @param context The request context, containing parameters and resolved configuration.
     * @param assets  The API asset configuration, containing details about the downstream service.
     * @return A {@link Mono<ServerResponse>} containing the response from the downstream service.
     */
    @NonNull
    public Mono<ServerResponse> execute(ServerRequest request, Context context, Assets assets) {
        String method = request.methodName();
        String path = request.path();

        // 1. Build the base URL for the target service.
        String baseUrl = buildBaseUrl(assets);
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_BASEURL] - Base URL: {}", method, path, baseUrl);

        // 2. Dynamically configure an HttpClient for THIS request.
        HttpClient configuredClient;
        if (assets.getMode() == 3) { // 3 represents SSE (Streaming)
            Logger.info(
                    "==>       HTTP: Applying IDLE timeout of {} seconds for streaming request.",
                    assets.getTimeout());
            configuredClient = HttpClient.create(SHARED_CONNECTION_PROVIDER)
                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(assets.getTimeout())));
        } else { // Unary request
            Logger.info(
                    "==>       HTTP: Applying RESPONSE timeout of {} seconds for unary request.",
                    assets.getTimeout());
            configuredClient = HttpClient.create(SHARED_CONNECTION_PROVIDER)
                    .responseTimeout(Duration.ofSeconds(assets.getTimeout()));
        }

        // 3. Build a WebClient ON TOP of the dynamically configured HttpClient.
        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(configuredClient))
                .exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build();

        // 4. Build and execute the request.
        String targetUri = buildTargetUri(assets, context);
        Logger.info("==>       HTTP: [{}] [{}] [HTTP_ROUTER_URI] - Target URI: {}", method, path, targetUri);

        WebClient.RequestBodySpec bodySpec = webClient.method(context.getHttpMethod()).uri(targetUri);
        Logger.info(
                "==>       HTTP: [{}] [{}] [HTTP_ROUTER_METHOD] - HTTP method: {}",
                method,
                path,
                context.getHttpMethod());

        // 5. Configure request headers, copying from the original request and cleaning up as needed.
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_HEADERS] - Headers configured", method, path);

        // 6. Handle the request body, if applicable (i.e., for non-GET requests).
        if (!HttpMethod.GET.equals(context.getHttpMethod())) {
            MediaType mediaType = request.headers().contentType().orElse(null);
            if (mediaType != null) {
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    handleMultipartBody(bodySpec, context, method, path);
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    handleJsonRequestBody(bodySpec, context, method, path);
                } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                    handleFormRequestBody(bodySpec, context, method, path);
                } else {
                    Logger.info(
                            "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_UNSUPPORTED] - Unsupported media type: {}",
                            method,
                            path,
                            mediaType);
                    handleFormRequestBody(bodySpec, context, method, path);
                }
            } else {
                Logger.info(
                        "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_DEFAULT] - No Content-Type header, defaulting to form data",
                        method,
                        path);
                handleFormRequestBody(bodySpec, context, method, path);
            }
        } else {
            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_GET] - GET request, no body processing",
                    method,
                    path);
        }

        // 7. Send the request and process the response.
        Logger.info(
                "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_SEND] - Sending request with timeout: {}s",
                method,
                path,
                assets.getTimeout());

        // **OPTIMIZATION:** Use exchangeToMono to STREAM the response, not retrieve().toEntity()
        // This avoids buffering the entire downstream response in memory.
        return bodySpec.exchangeToMono(clientResponse -> {
            ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(clientResponse.statusCode());

            // Copy all headers from the downstream response to the upstream response
            responseBuilder.headers(headers -> {
                headers.addAll(clientResponse.headers().asHttpHeaders());
                headers.remove(HttpHeaders.HOST);
                // Let the container manage transfer-encoding and content-length
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            });

            // Stream the response body directly
            return responseBuilder.body(clientResponse.bodyToFlux(DataBuffer.class), DataBuffer.class);
        });
    }

    /**
     * Handles a JSON request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleJsonRequestBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String method,
            String path) {
        Map<String, Object> params = context.getParameters();
        if (!params.isEmpty()) {
            // **OPTIMIZATION:** Wrap synchronous, CPU-bound JSON serialization
            // in fromCallable and offload it from the event loop.
            Mono<String> jsonBodyMono = Mono.fromCallable(() -> JsonKit.toJsonString(params))
                    .subscribeOn(Schedulers.boundedElastic());

            bodySpec.contentType(MediaType.APPLICATION_JSON).body(jsonBodyMono, String.class);

            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_JSON] - JSON body configured with {} parameters (async generation)",
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_JSON] - No JSON parameters to configure",
                    method,
                    path);
        }
    }

    /**
     * Handles a form-urlencoded request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleFormRequestBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String method,
            String path) {
        Map<String, Object> params = context.getParameters();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach((k, v) -> multiValueMap.add(k, String.valueOf(v)));
            // bodyValue is fine for MultiValueMap, WebClient handles it efficiently.
            bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(multiValueMap);
            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_FORM] - Form body configured with {} parameters",
                    method,
                    path,
                    params.size());
        } else {
            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_FORM] - No form parameters to configure",
                    method,
                    path);
        }
    }

    /**
     * Handles a multipart/form-data request body.
     *
     * @param bodySpec The request body specification.
     * @param context  The request context.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleMultipartBody(WebClient.RequestBodySpec bodySpec, Context context, String method, String path) {
        Map<String, Part> fileParts = context.getFileParts();
        Map<String, Object> params = context.getParameters();
        if (!fileParts.isEmpty() || !params.isEmpty()) {
            MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
            fileParts.forEach(multipartData::add);
            params.forEach((k, v) -> multipartData.add(k, String.valueOf(v)));
            // fromMultipartData is non-blocking and supports streaming.
            bodySpec.body(BodyInserters.fromMultipartData(multipartData));
            Logger.info(
                    "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_MULTIPART] - Multipart body configured with {} files and {} params",
                    method,
                    path,
                    fileParts.size(),
                    params.size());
        }
    }

    /**
     * Builds the base URL for the downstream service from the asset configuration.
     *
     * @param assets The configured assets for the target service.
     * @return The constructed base URL string (e.g., "http://api.example.com:8080/v1").
     */
    private String buildBaseUrl(Assets assets) {
        // This is fast, non-blocking string building.
        StringBuilder baseUrlBuilder = new StringBuilder(assets.getHost());
        if (assets.getPort() != null && assets.getPort() > 0) {
            baseUrlBuilder.append(Symbol.COLON).append(assets.getPort());
        }
        if (assets.getPath() != null && !assets.getPath().isEmpty()) {
            if (!assets.getPath().startsWith(Symbol.SLASH)) {
                baseUrlBuilder.append(Symbol.SLASH);
            }
            baseUrlBuilder.append(assets.getPath());
        }
        return baseUrlBuilder.toString();
    }

    /**
     * Builds the target URI for the downstream request.
     *
     * @param assets  The configured assets for the target service.
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

}

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.annotation.NonNull;

/**
 * HTTP strategy router, responsible for routing requests to HTTP services.
 * <p>
 * This class implements the {@link Router} interface to handle HTTP requests. It uses {@link WebClient} to forward
 * incoming requests to target HTTP services, managing request headers, body, and response processing.
 *
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpRequestRouter implements Router {

    /**
     * Pre-defined {@link ExchangeStrategies} instance for WebClient configuration.
     * <p>
     * This instance is initialized and cached when the class is loaded to avoid redundant creation and improve
     * performance. It is configured with a maximum in-memory size limit to prevent out-of-memory errors for large
     * requests.
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128))).build();

    /**
     * A thread-safe cache of {@link WebClient} instances, stored by their base URL.
     * <p>
     * This map ensures that a {@link WebClient} instance is reused for a given base URL, optimizing resource usage and
     * performance.
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * Processes client requests, constructs and forwards them to the target service, and returns the response.
     * <p>
     * This method orchestrates the forwarding of an incoming {@link ServerRequest} to an external HTTP service. It
     * involves building the target URL, configuring the {@link WebClient} with appropriate headers and body, sending
     * the request, and processing the received response.
     *
     * @param request The client's {@link ServerRequest} object.
     * @param context The request context, containing request parameters and configuration information.
     * @param assets  The configuration assets, containing configuration information for the target service.
     * @return {@link Mono<ServerResponse>} containing the response from the target service.
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // Get request method and path for logging
        String method = request.methodName();
        String path = request.path();

        // 1. Build the base URL for the target service
        String baseUrl = buildBaseUrl(assets);
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_BASEURL] - Base URL: {}", method, path, baseUrl);

        // 2. Get or create a WebClient instance for the base URL
        WebClient webClient = clients.computeIfAbsent(
                baseUrl,
                client -> WebClient.builder().exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build());
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_CLIENT] - WebClient created/retrieved", method, path);

        // 3. Build the target URI for the request
        String targetUri = buildTargetUri(assets, context);
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_URI] - Target URI: {}", method, path, targetUri);

        // 4. Configure the request with the appropriate HTTP method
        WebClient.RequestBodySpec bodySpec = webClient.method(context.getHttpMethod()).uri(targetUri);
        Logger.info(
                "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_METHOD] - HTTP method: {}",
                method,
                path,
                context.getHttpMethod());

        // 5. Configure request headers, copying from the incoming request and removing/clearing specific ones
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });
        Logger.info("==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_HEADERS] - Headers configured", method, path);

        // 6. Handle the request body (only for non-GET requests)
        if (!HttpMethod.GET.equals(context.getHttpMethod())) {
            MediaType mediaType = request.headers().contentType().orElse(null);
            if (mediaType != null) {
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    // Handle multipart request body
                    Map<String, Part> fileParts = context.getFilePartMap();
                    Map<String, String> params = context.getRequestMap();
                    if (!fileParts.isEmpty() || !params.isEmpty()) {
                        MultiValueMap<String, Part> partMap = new LinkedMultiValueMap<>(fileParts.size());
                        partMap.setAll(fileParts);
                        BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(partMap);
                        if (!params.isEmpty()) {
                            params.forEach(multipartInserter::with);
                        }
                        bodySpec.body(multipartInserter);
                        Logger.info(
                                "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_MULTIPART] - Multipart body configured with {} files and {} params",
                                method,
                                path,
                                fileParts.size(),
                                params.size());
                    }
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    // Handle JSON request body
                    return request.bodyToMono(String.class).defaultIfEmpty(Normal.EMPTY).flatMap(jsonBody -> {
                        Logger.info(
                                "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_JSON] - JSON request body size: {}",
                                method,
                                path,
                                jsonBody.length());
                        return bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(jsonBody)
                                .httpRequest(clientHttpRequest -> {
                                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                    reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
                                }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
                    });
                } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                    // Handle form-urlencoded request body
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
                // No Content-Type header, default to form data processing
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

        // 7. Send the request and process the response
        Logger.info(
                "==>       HTTP: [N/A] [{}] [{}] [HTTP_ROUTER_SEND] - Sending request with timeout: {}ms",
                method,
                path,
                assets.getTimeout());
        return bodySpec.httpRequest(clientHttpRequest -> {
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
    }

    /**
     * Handles the form request body.
     * <p>
     * This method converts the parameters from the request context into a {@link MultiValueMap} and sets it as the
     * request body. If the parameters map is empty, no request body is set.
     *
     * @param bodySpec The request body specification, used to configure the request body.
     * @param context  The request context, containing request parameters.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleFormRequestBody(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String method,
            String path) {
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach(multiValueMap::add);
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
     * Builds the base URL for the target service.
     * <p>
     * This method constructs the base URL using the host, port, and path information from the configured assets. The
     * port and path are optional and are included only if present.
     *
     * @param assets The configured assets, containing the host, port, and path information for the target service.
     * @return The constructed base URL string.
     */
    private String buildBaseUrl(Assets assets) {
        StringBuilder baseUrlBuilder = new StringBuilder(assets.getHost());
        if (assets.getPort() > 0) {
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
     * Builds the target URI for the request.
     * <p>
     * This method constructs the target URI using the URL from the configured assets and parameters from the request
     * context. For GET requests, parameters are appended to the URI as query strings.
     *
     * @param assets  The configured assets, containing the URL information for the target service.
     * @param context The request context, containing request parameters.
     * @return The constructed target URI string.
     */
    private String buildTargetUri(Assets assets, Context context) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());
        if (HttpMethod.GET.equals(context.getHttpMethod())) {
            Map<String, String> params = context.getRequestMap();
            if (!params.isEmpty()) {
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
                params.forEach(multiValueMap::add);
                builder.queryParams(multiValueMap);
            }
        }
        return builder.build().toUriString();
    }

    /**
     * Processes the response data.
     * <p>
     * This method converts the {@link ResponseEntity} received from the target service into a {@link ServerResponse}
     * object. It copies the response headers but removes the {@code CONTENT_LENGTH} header to avoid conflicts. If the
     * response body is empty, an empty response body is returned.
     *
     * @param responseEntity The {@link ResponseEntity} containing the response headers and body.
     * @return {@link Mono<ServerResponse>} representing the processed response.
     */
    private Mono<ServerResponse> processResponse(ResponseEntity<DataBuffer> responseEntity) {
        return ServerResponse.ok().headers(headers -> {
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(
                responseEntity.getBody() == null ? BodyInserters.empty()
                        : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

}

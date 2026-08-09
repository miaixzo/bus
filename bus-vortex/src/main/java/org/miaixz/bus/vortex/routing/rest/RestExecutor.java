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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
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
 * The core executor for executing RESTful HTTP requests to downstream services.
 * <p>
 * This executor encapsulates all the logic for using Spring's {@link WebClient} to act as a reverse proxy. It builds
 * the downstream request based on the provided {@link Assets} and {@link Context}, sends it, and then transforms the
 * downstream response back into a {@link ServerResponse} for the original client.
 * <p>
 * The route asset explicitly selects bounded atomic relay, realtime streaming or protected file download. Atomic
 * responses require Content-Length and retain an exact logical-byte lease through the network write; streaming modes
 * preserve downstream backpressure and never aggregate the body.
 * <p>
 * Generic type parameters: {@code Executor<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 */
public class RestExecutor extends Coordinator<ServerRequest, ServerResponse> {

    /**
     * Default constructor.
     */
    public RestExecutor() {
        // No initialization required.
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
        final String method = request.method().name();
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

        Assets assets = context.getAssets();
        URI targetUri = buildTargetUri(assets, context, baseUrl);
        Logger.info(
                true,
                "Vortex",
                "Target URI: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_URI, {}",
                ip,
                method,
                path,
                targetUri);

        WebClient.RequestBodySpec bodySpec = Egress
                .request(HttpMethod.valueOf(context.getHttpMethod().value()), targetUri);
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
            context.getHeaders().forEach((name, value) -> {
                if (StringKit.isNotBlank(name) && value != null) {
                    headers.remove(name);
                    headers.add(name, value);
                }
            });
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
                context.getHeaders());
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

        if (context.getHttpMethod() != Http.Method.GET) {
            MediaType mediaType = request.headers().contentType().orElse(null);
            if (mediaType != null) {
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    handleMultipartBody(bodySpec, context, ip, method, path);
                } else if (org.miaixz.bus.core.net.MediaType.isJson(mediaType.toString())) {
                    handleJsonRequestBody(bodySpec, request, context, ip, method, path);
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

        Delivery delivery = Delivery.of(assets.getStream());
        boolean isStreaming = delivery != Delivery.BUFFERED;

        if (isStreaming) {
            Logger.info(
                    true,
                    "Vortex",
                    "Using STREAMING mode (stream=2, exchangeToMono): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
            return executeStreaming(bodySpec, assets, targetUri, delivery, ip, method, path);
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "Using ATOMIC mode (stream=1, buffered passthrough): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
            return executeBuffering(bodySpec, assets, targetUri, ip, method, path);
        }
    }

    /**
     * Handles a JSON request body.
     *
     * @param bodySpec The request body specification.
     * @param request  The current server request.
     * @param context  The request context.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     */
    private void handleJsonRequestBody(
            WebClient.RequestBodySpec bodySpec,
            ServerRequest request,
            Context context,
            String ip,
            String method,
            String path) {
        if (Boolean.TRUE.equals(request.exchange().getAttribute(Context.RAW_JSON_PASSTHROUGH_ATTRIBUTE))) {
            MediaType mediaType = request.headers().contentType().orElse(MediaType.APPLICATION_JSON);
            bodySpec.contentType(mediaType);
            request.headers().contentLength().ifPresent(bodySpec::contentLength);
            Flux<DataBuffer> rawBody = request.exchange().getRequest().getBody()
                    .doOnDiscard(PooledDataBuffer.class, Octets::release);
            bodySpec.body(BodyInserters.fromDataBuffers(rawBody));
            Logger.info(
                    true,
                    "Vortex",
                    "CST JSON body configured for single-subscription passthrough: protocol=http, clientIp={}, method={}, path={}",
                    ip,
                    method,
                    path);
            return;
        }
        if (Boolean.TRUE.equals(request.exchange().getAttribute(Context.JSON_ARRAY_BODY_ATTRIBUTE))) {
            MediaType mediaType = request.headers().contentType().orElse(MediaType.APPLICATION_JSON);
            bodySpec.contentType(mediaType);
            request.headers().contentLength().ifPresent(length -> bodySpec.contentLength(length));
            bodySpec.body(BodyInserters.fromDataBuffers(request.exchange().getRequest().getBody()));
            Logger.info(
                    true,
                    "Vortex",
                    "JSON array content configured: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_JSON_ARRAY",
                    ip,
                    method,
                    path);
            return;
        }

        Map<String, Object> params = context.getParameters();
        if (!params.isEmpty()) {
            String json = JsonKit.toJsonString(params);
            String fixed = fixJsonEncoding(json);
            byte[] body = fixed.getBytes(Charset.UTF_8);
            int backslashCount = fixed.length() - fixed.replace(Symbol.BACKSLASH, Normal.EMPTY).length();
            Logger.debug(
                    true,
                    "Vortex",
                    "JSON prepared: protocol=http, clientIp={}, method={}, path={}, event=HTTP_BEFORE_SEND, backslashes={}, chars={}, bytes={}",
                    ip,
                    method,
                    path,
                    backslashCount,
                    fixed.length(),
                    body.length);

            bodySpec.contentType(MediaType.APPLICATION_JSON).contentLength(body.length).bodyValue(body);

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
    private URI buildTargetUri(Assets assets, Context context, String baseUrl) {
        URI routeUri = UrlKit.toURI(assets.getUrl());
        String routeUrl = stripQueryAndFragment(assets.getUrl());
        String routeBaseUrl = routeUri.isAbsolute() ? routeUrl : joinPath(baseUrl, routeUrl);
        String rawQuery = buildForwardQuery(context, routeUri.getRawQuery());
        return UrlKit
                .toURI(StringKit.isBlank(rawQuery) ? routeBaseUrl : routeBaseUrl + Symbol.QUESTION_MARK + rawQuery);
    }

    /**
     * Builds the downstream REST query string after removing gateway control parameters.
     *
     * @param context       request context
     * @param assetRawQuery query configured on the route URL, if any
     * @return raw query string
     */
    private String buildForwardQuery(Context context, String assetRawQuery) {
        List<String> parts = new ArrayList<>();
        if (StringKit.isNotBlank(assetRawQuery)) {
            parts.add(assetRawQuery);
        }
        Map<String, String> query = context.getQuery();
        if (!query.isEmpty()) {
            query.forEach((key, value) -> addQueryPair(parts, key, value));
        }

        if (context.getHttpMethod() == Http.Method.GET) {
            Map<String, Object> parameters = context.getParameters();
            if (!parameters.isEmpty()) {
                parameters.forEach((key, value) -> {
                    parts.removeIf(part -> part.startsWith(key + Symbol.EQUAL));
                    addQueryPair(parts, key, String.valueOf(value));
                });
            }
        }

        return String.join(Symbol.AND, parts);
    }

    /**
     * Adds one query pair using UrlKit encoding rules.
     *
     * @param parts query parts
     * @param key   parameter key
     * @param value parameter value
     */
    private void addQueryPair(List<String> parts, String key, String value) {
        if (Args.isForwardingControlParameter(key)) {
            return;
        }
        parts.add(UrlKit.toQuery(Map.of(key, StringKit.toStringOrEmpty(value)), Charset.UTF_8));
    }

    /**
     * Appends a path to a base URL with one separator.
     *
     * @param baseUrl base URL
     * @param path    path suffix
     * @return joined URL
     */
    private String joinPath(String baseUrl, String path) {
        if (StringKit.isBlank(path)) {
            return baseUrl;
        }
        boolean baseEnds = baseUrl.endsWith(Symbol.SLASH);
        boolean pathStarts = path.startsWith(Symbol.SLASH);
        if (baseEnds && pathStarts) {
            return baseUrl + path.substring(1);
        }
        if (!baseEnds && !pathStarts) {
            return baseUrl + Symbol.SLASH + path;
        }
        return baseUrl + path;
    }

    /**
     * Removes query and fragment from one URL.
     *
     * @param url source URL
     * @return URL without query and fragment
     */
    private String stripQueryAndFragment(String url) {
        int queryIndex = url.indexOf(Symbol.C_QUESTION_MARK);
        int fragmentIndex = url.indexOf(Symbol.C_HASH);
        int end = url.length();
        if (queryIndex >= 0) {
            end = Math.min(end, queryIndex);
        }
        if (fragmentIndex >= 0) {
            end = Math.min(end, fragmentIndex);
        }
        return url.substring(0, end);
    }

    /**
     * Handles a realtime-streaming or download response without materializing its body.
     * <p>
     * The response entity exposes a downstream {@link DataBuffer} stream that is written with a raw data-buffer body
     * inserter so long-lived streams and large files are not materialized in memory. The mode-specific lease remains
     * owned until response termination.
     * </p>
     *
     * @param bodySpec  downstream request specification
     * @param assets    resolved route asset used by retry policy
     * @param targetUri downstream target URI used by retry diagnostics
     * @param delivery  realtime-stream or download delivery mode
     * @param ip        client IP for logging
     * @param method    incoming HTTP method
     * @param path      incoming request path
     * @return proxied streaming response
     */
    private Mono<ServerResponse> executeStreaming(
            WebClient.RequestBodySpec bodySpec,
            Assets assets,
            URI targetUri,
            Delivery delivery,
            String ip,
            String method,
            String path) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        Mono<ResponseEntity<Flux<DataBuffer>>> response = bodySpec.retrieve()
                .onStatus(status -> true, clientResponse -> Mono.empty()).toEntityFlux(DataBuffer.class);
        return Egress.guard(response, 0, Egress.retryAttempts(assets, httpMethod), "rest-streaming", method, targetUri)
                .flatMap(responseEntity -> buildStreamingResponse(responseEntity, delivery, ip, method, path))
                .doOnSubscribe(
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
     * Handles the execution for an ATOMIC request using buffered downstream response passthrough.
     * <p>
     * The downstream response requires Content-Length, acquires that exact logical capacity, and is materialized into
     * bounded segments before the {@link ServerResponse} is returned. Ownership remains active through the gateway
     * network write, including cancellation.
     * </p>
     *
     * @param bodySpec  the downstream request specification
     * @param assets    resolved route asset used by retry policy
     * @param targetUri downstream target URI used by retry diagnostics
     * @param ip        the client IP for logging
     * @param method    the incoming request method for logging
     * @param path      the incoming request path for logging
     * @return a buffered {@link ServerResponse}
     */
    private Mono<ServerResponse> executeBuffering(
            WebClient.RequestBodySpec bodySpec,
            Assets assets,
            URI targetUri,
            String ip,
            String method,
            String path) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        Mono<ResponseEntity<Flux<DataBuffer>>> response = bodySpec.retrieve()
                .onStatus(status -> true, clientResponse -> Mono.empty()).toEntityFlux(DataBuffer.class);
        return Egress.guard(response, 0, Egress.retryAttempts(assets, httpMethod), "rest-atomic", method, targetUri)
                .flatMap(responseEntity -> {
                    Logger.info(
                            false,
                            "Vortex",
                            "Received downstream status: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_BUFFERED, {}",
                            ip,
                            method,
                            path,
                            responseEntity.getStatusCode());

                    return buildBufferedResponse(responseEntity, ip, method, path);
                })
                .doOnSubscribe(
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
                                "Request FAILED (Buffering): protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_ERROR, {}",
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

    /**
     * Builds a buffered response from a response body that has already been judged safe to materialize.
     *
     * @param responseEntity downstream response entity
     * @param ip             client IP for logging
     * @param method         incoming HTTP method
     * @param path           incoming request path
     * @return buffered response
     */
    private Mono<ServerResponse> buildBufferedResponse(
            ResponseEntity<Flux<DataBuffer>> responseEntity,
            String ip,
            String method,
            String path) {
        Logger.info(
                false,
                "Vortex",
                "Downstream response headers: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_HEADERS, {}",
                ip,
                method,
                path,
                responseEntity.getHeaders());

        ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
        copyResponseHeaders(responseBuilder, responseEntity.getHeaders());
        if (responseEntity.getHeaders().getContentLength() >= 0) {
            responseBuilder.contentLength(responseEntity.getHeaders().getContentLength());
        }

        Flux<DataBuffer> bodyFlux = responseEntity.getBody();
        boolean bodyForbidden = HttpMethod.HEAD.matches(method) || responseEntity.getStatusCode().is1xxInformational()
                || responseEntity.getStatusCode().value() == 204 || responseEntity.getStatusCode().value() == 304;
        if (bodyFlux == null) {
            return buildEmptyBufferedResponse(responseBuilder, ip, method, path);
        }
        if (bodyForbidden) {
            return discardResponseBody(bodyFlux).then(buildEmptyBufferedResponse(responseBuilder, ip, method, path));
        }

        long declaredLength = responseEntity.getHeaders().getContentLength();
        if (declaredLength < 0 || declaredLength > Math.toIntExact(Holder.get().getMaxBufferedResponseSize())) {
            return discardResponseBody(bodyFlux).then(
                    Mono.error(
                            new DataBufferLimitException("Atomic response requires Content-Length in range 0.."
                                    + Math.toIntExact(Holder.get().getMaxBufferedResponseSize()))));
        }

        return collectBufferedBody(bodyFlux, declaredLength).flatMap(body -> Mono.deferContextual(contextView -> {
            Context context = contextView.getOrDefault(Context.class, null);
            if (context == null) {
                body.close();
                return Mono.error(new IllegalStateException("Vortex request context is missing"));
            }
            int bodyLength = body.length();
            if (bodyLength != declaredLength) {
                return Mono.error(
                        new DataBufferLimitException("Atomic response length mismatch: declared=" + declaredLength
                                + ", actual=" + bodyLength));
            }
            if (bodyLength > 0) {
                Logger.info(
                        false,
                        "Vortex",
                        "Received buffered content: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_CONTENT_BUFFERED, bytes={}",
                        ip,
                        method,
                        path,
                        bodyLength);
                return responseBuilder.contentLength(bodyLength)
                        .body(BodyInserters.fromDataBuffers(Octets.chunksAndClose(body)));
            }
            body.close();
            return buildEmptyBufferedResponse(responseBuilder, ip, method, path);
        })).switchIfEmpty(Mono.defer(() -> buildEmptyBufferedResponse(responseBuilder, ip, method, path)));
    }

    /**
     * Collects a downstream response body while enforcing the buffered-response size limit.
     *
     * @param bodyFlux       downstream response buffers
     * @param declaredLength validated downstream Content-Length
     * @return buffered body with an attached response-byte lease
     */
    private Mono<Octets.BufferedBody> collectBufferedBody(Flux<DataBuffer> bodyFlux, long declaredLength) {
        return Octets.readForRelay(
                bodyFlux,
                Math.toIntExact(Holder.get().getMaxBufferedResponseSize()),
                Holder.responseBufferBudget(),
                declaredLength);
    }

    /**
     * Drains a downstream body that cannot be forwarded and releases every received pooled buffer.
     *
     * @param bodyFlux downstream response buffers
     * @return completion after the connection is reusable
     */
    private Mono<Void> discardResponseBody(Flux<DataBuffer> bodyFlux) {
        return Octets.discard(bodyFlux);
    }

    /**
     * Builds an empty buffered response.
     *
     * @param responseBuilder response builder
     * @param ip              client IP for logging
     * @param method          incoming HTTP method
     * @param path            incoming request path
     * @return empty response
     */
    private Mono<ServerResponse> buildEmptyBufferedResponse(
            ServerResponse.BodyBuilder responseBuilder,
            String ip,
            String method,
            String path) {
        Logger.warn(
                false,
                "Vortex",
                "Received buffered content is empty: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_CONTENT_BUFFERED",
                ip,
                method,
                path);
        return responseBuilder.build();
    }

    /**
     * Builds a streaming response without materializing the downstream body.
     *
     * @param responseEntity downstream response entity
     * @param ip             client IP for logging
     * @param method         incoming HTTP method
     * @param path           incoming request path
     * @param delivery       realtime-stream or download delivery mode
     * @return streaming response
     */
    private Mono<ServerResponse> buildStreamingResponse(
            ResponseEntity<Flux<DataBuffer>> responseEntity,
            Delivery delivery,
            String ip,
            String method,
            String path) {
        ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
        Logger.debug(
                false,
                "Vortex",
                "Downstream response headers: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_HEADERS, mode={}, {}",
                ip,
                method,
                path,
                delivery,
                responseEntity.getHeaders());

        copyResponseHeaders(responseBuilder, responseEntity.getHeaders());

        if (responseEntity.getHeaders().getContentLength() >= 0) {
            responseBuilder.contentLength(responseEntity.getHeaders().getContentLength());
        }

        Flux<DataBuffer> bodyFlux = responseEntity.getBody() == null ? Flux.empty()
                : responseEntity.getBody().doOnNext(
                        dataBuffer -> Logger.debug(
                                false,
                                "Vortex",
                                "Received data chunk, size: protocol=http, clientIp={}, method={}, path={}, event=HTTP_ROUTER_RECV_STREAM_CHUNK, mode={}, {} bytes",
                                ip,
                                method,
                                path,
                                delivery,
                                dataBuffer.readableByteCount()))
                        .doOnDiscard(PooledDataBuffer.class, Octets::release);

        bodyFlux = StreamingRelay.relay(bodyFlux, delivery);

        return responseBuilder.body(BodyInserters.fromDataBuffers(bodyFlux));
    }

    /**
     * Copies downstream response headers to the gateway response.
     *
     * @param responseBuilder response builder
     * @param source          downstream response headers
     */
    private void copyResponseHeaders(ServerResponse.BodyBuilder responseBuilder, HttpHeaders source) {
        responseBuilder.headers(headers -> {
            headers.addAll(source);
            headers.remove(HttpHeaders.HOST);
            headers.remove(HttpHeaders.TRANSFER_ENCODING);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        });
    }

}

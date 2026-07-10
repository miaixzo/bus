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
package org.miaixz.bus.vortex.routing.mcp;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.RegistryAssets;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Egress;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.routing.Coordinator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The core executor for proxying MCP Streamable HTTP requests to registered downstream MCP services.
 * <p>
 * This executor follows the same WebClient execution shape as {@link org.miaixz.bus.vortex.routing.rest.RestExecutor}:
 * it validates the resolved runtime route asset, builds a downstream request, configures headers and body content, then
 * chooses between streaming and buffering response handling.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class McpExecutor extends Coordinator<ServerRequest, ServerResponse> {

    /**
     * Creates an MCP Streamable HTTP reverse proxy executor.
     * <p>
     * Runtime route resolution is performed before this executor runs and is passed through {@link Context}; this keeps
     * the executor focused on request forwarding.
     */
    public McpExecutor() {
        // No initialization required.
    }

    /**
     * Executes a standard MCP Streamable HTTP request.
     * <p>
     * The method validates the qualified route context and resolved MCP route asset, builds the downstream URI, copies
     * MCP-specific request headers, attaches the POST JSON body when present, and then delegates to either streaming or
     * buffering response handling based on the request method and route asset configuration.
     *
     * @param context The request context populated by the strategy chain.
     * @param request The incoming MCP {@link ServerRequest}.
     * @return A {@link Mono} emitting the proxied {@link ServerResponse}, or an error response when validation fails.
     */
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest request) {
        final String method = request.method().name();
        final String path = request.path();
        final String ip = context == null ? null : context.getX_request_ip();

        if (context == null || context.getAssets() == null) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode._100800.getKey(), "MCP route asset is missing");
        }
        Assets target = context.getAssets();
        if (target == null || !Type.MCP.is(RegistryAssets.typeOf(target))
                || !Integer.valueOf(Args.PROTOCOL_MCP).equals(target.getProtocol())
                || StringKit.isBlank(target.getMethod()) || StringKit.isBlank(target.getUrl())
                || (target.getStatus() != null && !Integer.valueOf(1).equals(target.getStatus()))) {
            return buildErrorResponse(
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode._100800.getKey(),
                    "MCP route asset is not registered");
        }

        URI targetUri = buildTargetUri(target, context, context.getRemainingPath(), request);
        if (targetUri == null) {
            return buildErrorResponse(HttpStatus.BAD_GATEWAY, ErrorCode._100800.getKey(), "MCP target URL is invalid");
        }
        Logger.info(
                true,
                "Vortex",
                "MCP proxy started: clientIp={}, method={}, path={}, routePrefix={}, targetUrl={}",
                ip,
                method,
                path,
                target.getMethod(),
                targetUri);

        WebClient.RequestBodySpec bodySpec = Egress
                .request(HttpMethod.valueOf(context.getHttpMethod().value()), targetUri);
        Logger.info(
                true,
                "Vortex",
                "HTTP method: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_METHOD, {}",
                ip,
                method,
                path,
                context.getHttpMethod());

        bodySpec.headers(headers -> {
            copyHeader(request, headers, Args.MCP_PROTOCOL_VERSION);
            copyHeader(request, headers, Args.MCP_SESSION_ID);
            copyHeader(request, headers, Args.LAST_EVENT_ID);
            copyHeader(request, headers, HTTP.ACCEPT);
            copyHeader(request, headers, HTTP.CONTENT_TYPE);
            String forwardedFor = request.headers().firstHeader("X-Forwarded-For");
            String clientIp = context.getX_request_ip();
            if (StringKit.isNotBlank(clientIp)) {
                headers.set(
                        "X-Forwarded-For",
                        StringKit.isBlank(forwardedFor) ? clientIp : forwardedFor + ", " + clientIp);
            }
            headers.set("X-Forwarded-Proto", request.uri().getScheme() == null ? "http" : request.uri().getScheme());
            headers.set("X-Request-Id", context.getX_request_id());
        });
        Logger.debug(
                true,
                "Vortex",
                "Request header snapshot: protocol=mcp, clientIp={}, method={}, path={}, targetUri={}",
                ip,
                method,
                path,
                targetUri);
        Logger.debug(
                true,
                "Vortex",
                "Request headers: protocol=mcp, clientIp={}, method={}, path={}, headers={}",
                ip,
                method,
                path,
                request.headers().asHttpHeaders().toSingleValueMap());
        Logger.debug(
                true,
                "Vortex",
                "Request parameters: protocol=mcp, clientIp={}, method={}, path={}, parameters={}",
                ip,
                method,
                path,
                request.queryParams().toSingleValueMap());
        Logger.info(
                true,
                "Vortex",
                "Headers configured: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_HEADERS",
                ip,
                method,
                path);

        if (context.getHttpMethod() == HTTP.Method.POST) {
            handleJsonRequestBody(bodySpec, request, context, ip, method, path);
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "MCP request without body processing: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_NO_BODY",
                    ip,
                    method,
                    path);
        }

        Logger.info(
                true,
                "Vortex",
                "Sending request: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_SEND",
                ip,
                method,
                path);

        boolean isStreaming = context.getHttpMethod() == HTTP.Method.GET
                || (target.getStream() != null && target.getStream() == 2);
        if (isStreaming) {
            Logger.info(
                    true,
                    "Vortex",
                    "Using STREAMING mode: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
        } else {
            Logger.info(
                    true,
                    "Vortex",
                    "Using BUFFERING mode: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_STRATEGY",
                    ip,
                    method,
                    path);
        }

        return isStreaming ? executeStreaming(bodySpec, context, ip, method, path)
                : executeBuffering(bodySpec, ip, method, path);
    }

    /**
     * Handles a JSON MCP request body.
     * <p>
     * MCP POST requests have already been validated as JSON-compatible before reaching this executor. This method
     * forwards the original request body as a data-buffer publisher so the executor does not aggregate the body in
     * memory.
     *
     * @param bodySpec The request body specification.
     * @param request  The ServerRequest object.
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
        MediaType mediaType = request.headers().contentType().orElse(MediaType.APPLICATION_JSON);
        bodySpec.contentType(mediaType).body(BodyInserters.fromDataBuffers(request.exchange().getRequest().getBody()));
        Logger.info(
                true,
                "Vortex",
                "JSON content configured: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_JSON",
                ip,
                method,
                path);
    }

    /**
     * Builds the target URI for the downstream MCP request.
     * <p>
     * MCP route URLs are required to be bare base URLs without an existing query string. Relative route URLs are
     * resolved against the configured asset host, port, and path before appending the remaining request path. The
     * incoming query is then forwarded after gateway control parameters are removed.
     *
     * @param assets        The configured MCP route asset.
     * @param context       The request context.
     * @param remainingPath remaining request path produced by route resolution
     * @param request       The ServerRequest object.
     * @return The constructed target URI, including query parameters, or {@code null} when invalid.
     */
    private URI buildTargetUri(Assets assets, Context context, String remainingPath, ServerRequest request) {
        if (assets == null || StringKit.isBlank(assets.getUrl()) || remainingPath == null) {
            return null;
        }
        try {
            URI routeUri = UrlKit.toURI(assets.getUrl());
            if (routeUri.getRawQuery() != null) {
                return null;
            }
            String routeBaseUrl = routeUri.isAbsolute() ? assets.getUrl() : buildRelativeRouteBaseUrl(assets, context);
            if (StringKit.isBlank(routeBaseUrl)) {
                return null;
            }
            String rawRemainingPath = rawRemainingPath(request.uri().getRawPath(), assets.getMethod(), remainingPath);
            String targetUrl = joinPath(routeBaseUrl, rawRemainingPath);
            String rawQuery = buildForwardQuery(request);
            return UrlKit.toURI(StringKit.isBlank(rawQuery) ? targetUrl : targetUrl + Symbol.QUESTION_MARK + rawQuery);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * Resolves a relative MCP route URL against the target service base URL.
     *
     * @param assets  The configured MCP route asset.
     * @param context The request context.
     * @return The route base URL, or {@code null} when host configuration is missing.
     */
    private String buildRelativeRouteBaseUrl(Assets assets, Context context) {
        if (context == null || StringKit.isBlank(assets.getHost())) {
            return null;
        }
        return joinPath(buildBaseUrl(context), assets.getUrl());
    }

    /**
     * Builds the downstream query string after removing gateway control parameters.
     *
     * @param request incoming MCP request
     * @return raw query string for the downstream request
     */
    private String buildForwardQuery(ServerRequest request) {
        boolean hasControlParameter = request.queryParams().keySet().stream()
                .anyMatch(Args::isForwardingControlParameter);
        if (!hasControlParameter) {
            return request.uri().getRawQuery();
        }
        return request.queryParams().entrySet().stream()
                .filter(entry -> !Args.isForwardingControlParameter(entry.getKey()))
                .flatMap(
                        entry -> entry.getValue().stream().map(
                                value -> UrlKit.toQuery(
                                        Map.of(entry.getKey(), StringKit.toStringOrEmpty(value)),
                                        Charset.UTF_8)))
                .collect(Collectors.joining(Symbol.AND));
    }

    /**
     * Calculates the raw remaining path from the inbound request path and matched gateway route prefix.
     *
     * @param rawPath         incoming raw request path
     * @param routePrefix     matched gateway route prefix
     * @param decodedFallback decoded remaining path from route matching
     * @return raw remaining path suitable for downstream proxying
     */
    private String rawRemainingPath(String rawPath, String routePrefix, String decodedFallback) {
        if (StringKit.isNotBlank(rawPath) && StringKit.isNotBlank(routePrefix)) {
            if (rawPath.equals(routePrefix)) {
                return Normal.EMPTY;
            }
            if (rawPath.startsWith(routePrefix + Symbol.SLASH)) {
                return rawPath.substring(routePrefix.length());
            }
        }
        return encodeDecodedPath(decodedFallback);
    }

    /**
     * Encodes a decoded path fallback using UrlKit when raw path extraction is unavailable.
     *
     * @param decodedPath decoded path
     * @return raw encoded path
     */
    private String encodeDecodedPath(String decodedPath) {
        if (StringKit.isBlank(decodedPath)) {
            return Normal.EMPTY;
        }
        String path = decodedPath.startsWith(Symbol.SLASH) ? decodedPath : Symbol.SLASH + decodedPath;
        String marker = "http" + Symbol.COLON + Symbol.FORWARDSLASH + "vortex.local";
        try {
            return UrlKit.toURI(UrlKit.normalize(marker + path, true)).getRawPath();
        } catch (RuntimeException ex) {
            return decodedPath;
        }
    }

    /**
     * Appends the remaining request path to the downstream MCP base URL.
     *
     * @param baseUrl       downstream MCP base URL
     * @param remainingPath remaining request path
     * @return URL with a single separator between the base URL and remaining path
     */
    private String joinPath(String baseUrl, String remainingPath) {
        if (StringKit.isBlank(remainingPath)) {
            return baseUrl;
        }
        boolean baseEnds = baseUrl.endsWith(Symbol.SLASH);
        boolean remainingPathStarts = remainingPath.startsWith(Symbol.SLASH);
        if (baseEnds && remainingPathStarts) {
            return baseUrl + remainingPath.substring(1);
        }
        if (!baseEnds && !remainingPathStarts) {
            return baseUrl + Symbol.SLASH + remainingPath;
        }
        return baseUrl + remainingPath;
    }

    /**
     * Handles the execution for a STREAMING MCP request.
     * <p>
     * This path uses {@code exchangeToMono} and forwards the downstream response body as a {@link Flux} of
     * {@link DataBuffer}s. It is used for SSE-style MCP responses and long-lived GET streams, and mirrors the streaming
     * response branch used by REST routing.
     *
     * @param bodySpec The request body specification.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     * @return A {@link Mono} emitting the proxied streaming response.
     */
    private Mono<ServerResponse> executeStreaming(
            WebClient.RequestBodySpec bodySpec,
            Context context,
            String ip,
            String method,
            String path) {
        return bodySpec.retrieve().onStatus(status -> true, clientResponse -> Mono.empty())
                .toEntityFlux(DataBuffer.class).flatMap(responseEntity -> {
                    ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
                    Logger.debug(
                            false,
                            "Vortex",
                            "Downstream response headers: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_HEADERS, {}",
                            ip,
                            method,
                            path,
                            responseEntity.getHeaders());

                    responseBuilder.headers(headers -> {
                        headers.addAll(responseEntity.getHeaders());
                        headers.remove(HttpHeaders.HOST);
                        headers.remove(HttpHeaders.TRANSFER_ENCODING);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    });

                    Flux<DataBuffer> bodyFlux = responseEntity.getBody() == null ? Flux.empty()
                            : responseEntity.getBody();
                    bodyFlux = bodyFlux.doOnNext(dataBuffer -> {
                        Logger.debug(
                                false,
                                "Vortex",
                                "Received data chunk, size: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_STREAM_CHUNK, {} bytes",
                                ip,
                                method,
                                path,
                                dataBuffer.readableByteCount());
                    });
                    bodyFlux = rewriteSseEndpointIfNeeded(
                            bodyFlux,
                            context,
                            responseEntity.getHeaders(),
                            ip,
                            method,
                            path);

                    return responseBuilder.body(BodyInserters.fromDataBuffers(bodyFlux));
                })
                .doOnSubscribe(
                        subscription -> Logger.info(
                                true,
                                "Vortex",
                                "Request subscribed (Streaming).: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_SUBSCRIBE",
                                ip,
                                method,
                                path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Vortex",
                                "Successfully built ServerResponse with status: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_SUCCESS, {} (Streaming)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Vortex",
                                "Request FAILED (Streaming): protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_ERROR, {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Vortex",
                                "Request was cancelled by client (Streaming).: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_CANCEL",
                                ip,
                                method,
                                path));
    }

    /**
     * Rewrites legacy MCP SSE endpoint events so clients continue posting messages through the gateway route.
     * <p>
     * Downstream MCP servers commonly emit {@code data: /mcp/messages/...}; when the public entrypoint is
     * {@code /router/mcp/sse/...}, a standard SSE MCP client resolves that relative endpoint against the gateway
     * origin. This method rewrites only SSE data chunks that contain the downstream messages prefix.
     *
     * @param bodyFlux response body from downstream
     * @param context  current request context
     * @param headers  downstream response headers
     * @param ip       client IP for logging
     * @param method   HTTP method for logging
     * @param path     inbound request path for logging
     * @return response body with gateway endpoint data when applicable
     */
    private Flux<DataBuffer> rewriteSseEndpointIfNeeded(
            Flux<DataBuffer> bodyFlux,
            Context context,
            HttpHeaders headers,
            String ip,
            String method,
            String path) {
        if (context == null || context.getAssets() == null || context.getHttpMethod() != HTTP.Method.GET
                || headers.getContentType() == null
                || !MediaType.TEXT_EVENT_STREAM.isCompatibleWith(headers.getContentType())) {
            return bodyFlux;
        }
        String downstreamPrefix = downstreamMessagesPrefix(context.getAssets());
        String gatewayPrefix = gatewayMessagesPrefix(context.getAssets());
        if (StringKit.isBlank(downstreamPrefix) || StringKit.isBlank(gatewayPrefix)
                || downstreamPrefix.equals(gatewayPrefix)) {
            return bodyFlux;
        }
        return bodyFlux
                .map(dataBuffer -> rewriteEndpointChunk(dataBuffer, downstreamPrefix, gatewayPrefix, ip, method, path));
    }

    /**
     * Rewrites one SSE data chunk when it contains a legacy MCP messages endpoint.
     *
     * @param dataBuffer       downstream response chunk
     * @param downstreamPrefix endpoint prefix emitted by downstream
     * @param gatewayPrefix    endpoint prefix exposed by the gateway
     * @param ip               client IP for logging
     * @param method           HTTP method for logging
     * @param path             inbound request path for logging
     * @return original or rewritten data buffer
     */
    private DataBuffer rewriteEndpointChunk(
            DataBuffer dataBuffer,
            String downstreamPrefix,
            String gatewayPrefix,
            String ip,
            String method,
            String path) {
        String text = dataBuffer.toString(Charset.UTF_8);
        String rewritten = text.replace("data: " + downstreamPrefix, "data: " + gatewayPrefix)
                .replace("data:" + downstreamPrefix, "data:" + gatewayPrefix);
        if (text.equals(rewritten)) {
            return dataBuffer;
        }
        Logger.debug(
                false,
                "Vortex",
                "Rewrote MCP SSE endpoint: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_REWRITE_ENDPOINT, from={}, to={}",
                ip,
                method,
                path,
                downstreamPrefix,
                gatewayPrefix);
        var bufferFactory = dataBuffer.factory();
        if (dataBuffer instanceof PooledDataBuffer pooledDataBuffer && pooledDataBuffer.isAllocated()) {
            pooledDataBuffer.release();
        }
        return bufferFactory.wrap(rewritten.getBytes(Charset.UTF_8));
    }

    /**
     * Builds the downstream messages endpoint prefix that may be emitted in legacy MCP SSE endpoint events.
     *
     * @param assets current MCP route asset
     * @return downstream messages prefix, such as {@code /mcp/messages}
     */
    private String downstreamMessagesPrefix(Assets assets) {
        String servicePath = trimSlashes(assets.getPath());
        return StringKit.isBlank(servicePath) ? Symbol.SLASH + "messages"
                : Symbol.SLASH + servicePath + Symbol.SLASH + "messages";
    }

    /**
     * Derives the gateway messages endpoint prefix from the current SSE route prefix.
     *
     * @param assets current MCP route asset
     * @return gateway messages prefix, such as {@code /router/mcp/messages}
     */
    private String gatewayMessagesPrefix(Assets assets) {
        String routePrefix = assets.getMethod();
        if (StringKit.isBlank(routePrefix)) {
            return null;
        }
        String sseSuffix = Symbol.SLASH + "sse";
        if (routePrefix.endsWith(sseSuffix)) {
            return routePrefix.substring(0, routePrefix.length() - sseSuffix.length()) + Symbol.SLASH + "messages";
        }
        return routePrefix;
    }

    /**
     * Removes leading and trailing slashes from a path fragment.
     *
     * @param value path fragment
     * @return normalized path fragment
     */
    private String trimSlashes(String value) {
        if (StringKit.isBlank(value)) {
            return Normal.EMPTY;
        }
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == Symbol.C_SLASH) {
            start++;
        }
        while (end > start && value.charAt(end - 1) == Symbol.C_SLASH) {
            end--;
        }
        return value.substring(start, end);
    }

    /**
     * Handles the execution for a BUFFERING MCP request.
     * <p>
     * This path mirrors REST buffering behavior by consuming the downstream response body inside the WebClient exchange
     * before the {@link ServerResponse} is returned. It is used for non-streaming MCP responses such as JSON
     * acknowledgements.
     *
     * @param bodySpec The request body specification.
     * @param ip       The client IP for logging.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     * @return A {@link Mono} emitting the proxied buffering response.
     */
    private Mono<ServerResponse> executeBuffering(
            WebClient.RequestBodySpec bodySpec,
            String ip,
            String method,
            String path) {
        return bodySpec.exchangeToMono(clientResponse -> {
            Logger.info(
                    false,
                    "Vortex",
                    "Received downstream status: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_BUFFERED, {}",
                    ip,
                    method,
                    path,
                    clientResponse.statusCode());

            return clientResponse.toEntity(byte[].class).flatMap(responseEntity -> {
                Logger.info(
                        false,
                        "Vortex",
                        "Downstream response headers: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_HEADERS, {}",
                        ip,
                        method,
                        path,
                        responseEntity.getHeaders());

                byte[] body = responseEntity.getBody();
                if (body != null && body.length > 0) {
                    Logger.info(
                            false,
                            "Vortex",
                            "Received buffered content: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_CONTENT_BUFFERED, bytes={}",
                            ip,
                            method,
                            path,
                            body.length);
                } else {
                    Logger.warn(
                            false,
                            "Vortex",
                            "Received buffered content is empty: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_CONTENT_BUFFERED",
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

                if (body != null && body.length > 0) {
                    return responseBuilder.bodyValue(body);
                }
                return responseBuilder.build();
            });
        }).doOnSubscribe(
                subscription -> Logger.info(
                        true,
                        "Vortex",
                        "Request subscribed (Buffering).: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_SUBSCRIBE",
                        ip,
                        method,
                        path))
                .doOnSuccess(
                        serverResponse -> Logger.info(
                                false,
                                "Vortex",
                                "Successfully built ServerResponse with status: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_SUCCESS, {} (Buffering)",
                                ip,
                                method,
                                path,
                                serverResponse.statusCode()))
                .doOnError(
                        error -> Logger.error(
                                false,
                                "Vortex",
                                "Request FAILED (Buffering): protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_ERROR, {}",
                                ip,
                                method,
                                path,
                                error.getMessage(),
                                error))
                .doOnCancel(
                        () -> Logger.warn(
                                false,
                                "Vortex",
                                "Request was cancelled by client (Buffering).: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_CANCEL",
                                ip,
                                method,
                                path));
    }

    /**
     * Copies one inbound header to the downstream request when present.
     *
     * @param request The incoming server request.
     * @param headers The downstream request headers being configured.
     * @param name    The header name to copy.
     */
    private void copyHeader(ServerRequest request, HttpHeaders headers, String name) {
        List<String> values = request.headers().asHttpHeaders().get(name);
        if (values != null && !values.isEmpty()) {
            headers.put(name, values);
        }
    }

    /**
     * Builds a JSON error response using the gateway error envelope.
     *
     * @param status  The HTTP status to apply to the response.
     * @param errcode The gateway error code.
     * @param errmsg  The gateway error message.
     * @return A {@link Mono} emitting the error {@link ServerResponse}.
     */
    private Mono<ServerResponse> buildErrorResponse(HttpStatus status, String errcode, String errmsg) {
        Map<String, String> body = Map.of("errcode", errcode, "errmsg", errmsg);
        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JsonKit.toJsonString(body));
    }

}

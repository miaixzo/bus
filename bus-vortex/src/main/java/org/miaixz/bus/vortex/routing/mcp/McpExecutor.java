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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
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
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.routing.Coordinator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

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
     * Creates an MCP Streamable HTTP reverse proxy executor.
     * <p>
     * Runtime route resolution is performed before this executor runs and is passed through {@link Context}; this keeps
     * the executor focused on request forwarding.
     */
    public McpExecutor() {

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
        final String method = request.methodName();
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

        URI targetUri = buildTargetUri(target, context.getRemainingPath(), request);
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
                target.getUrl());

        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).build();
        WebClient.RequestBodySpec bodySpec = webClient.method(HttpMethod.valueOf(context.getHttpMethod().value()))
                .uri(targetUri);
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

        return isStreaming ? executeStreaming(bodySpec, ip, method, path)
                : executeBuffering(bodySpec, ip, method, path);
    }

    /**
     * Gets the shared Reactor Netty HTTP client, initializing it lazily if needed.
     * <p>
     * Request-specific {@link WebClient} instances reuse this low-level client so the connection pool remains shared
     * while per-request URI and headers stay isolated.
     *
     * @return The cached HttpClient instance
     */
    private HttpClient getHttpClient() {
        if (CACHED_HTTP_CLIENT == null) {
            synchronized (McpExecutor.class) {
                if (CACHED_HTTP_CLIENT == null) {
                    CACHED_HTTP_CLIENT = HttpClient.create(Holder.connectionProvider());
                    Logger.info(true, "Vortex", "Shared HTTP client initialized: protocol=http");
                }
            }
        }
        return CACHED_HTTP_CLIENT;
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
     * MCP route URLs are required to be bare base URLs without an existing query string. The remaining request path
     * resolved by {@link org.miaixz.bus.vortex.registry.AssetsRegistry} is appended to that base URL, then the incoming
     * query is forwarded after gateway control parameters are removed.
     *
     * @param assets        The configured MCP route asset.
     * @param remainingPath remaining request path produced by route resolution
     * @param request       The ServerRequest object.
     * @return The constructed target URI, including query parameters, or {@code null} when invalid.
     */
    private URI buildTargetUri(Assets assets, String remainingPath, ServerRequest request) {
        if (assets == null || StringKit.isBlank(assets.getUrl()) || remainingPath == null) {
            return null;
        }
        try {
            URI baseUri = URI.create(assets.getUrl());
            if (baseUri.getRawQuery() != null) {
                return null;
            }
            String targetUrl = joinPath(assets.getUrl(), remainingPath);
            String rawQuery = buildForwardQuery(request);
            return URI.create(StringKit.isBlank(rawQuery) ? targetUrl : targetUrl + "?" + rawQuery);
        } catch (IllegalArgumentException ex) {
            return null;
        }
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
                .collect(Collectors.joining("&"));
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
        boolean baseEnds = baseUrl.endsWith("/");
        boolean remainingPathStarts = remainingPath.startsWith("/");
        if (baseEnds && remainingPathStarts) {
            return baseUrl + remainingPath.substring(1);
        }
        if (!baseEnds && !remainingPathStarts) {
            return baseUrl + "/" + remainingPath;
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
            String ip,
            String method,
            String path) {
        return bodySpec.exchangeToMono(clientResponse -> {
            ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(clientResponse.statusCode());
            Logger.debug(
                    false,
                    "Vortex",
                    "Downstream response headers: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_HEADERS, {}",
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
                        "Received data chunk, size: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_STREAM_CHUNK, {} bytes",
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
     * Handles the execution for a BUFFERING MCP request.
     * <p>
     * This path mirrors REST buffering behavior by retrieving the downstream response as an entity and building a
     * bounded {@link ServerResponse}. It is used for non-streaming MCP responses such as JSON acknowledgements.
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
        return bodySpec.retrieve().toEntity(DataBuffer.class).flatMap(responseEntity -> {
            Logger.info(
                    false,
                    "Vortex",
                    "Received buffered status: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_BUFFERED, {}",
                    ip,
                    method,
                    path,
                    responseEntity.getStatusCode());
            Logger.info(
                    false,
                    "Vortex",
                    "Downstream response headers: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_HEADERS, {}",
                    ip,
                    method,
                    path,
                    responseEntity.getHeaders());

            DataBuffer body = responseEntity.getBody();
            if (body != null && body.readableByteCount() > 0) {
                Logger.info(
                        false,
                        "Vortex",
                        "Received buffered content: protocol=mcp, clientIp={}, method={}, path={}, event=MCP_ROUTER_RECV_CONTENT_BUFFERED, bytes={}",
                        ip,
                        method,
                        path,
                        body.readableByteCount());
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

            if (body != null && body.readableByteCount() > 0) {
                return responseBuilder.body(Mono.just(body), DataBuffer.class);
            }
            return responseBuilder.build();
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

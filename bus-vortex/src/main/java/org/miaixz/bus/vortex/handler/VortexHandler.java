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
package org.miaixz.bus.vortex.handler;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;
import reactor.util.retry.Retry;

/**
 * Request handling entry class, responsible for routing requests and asynchronously invoking multiple interceptor
 * logics.
 * <p>
 * This class implements the complete control flow for request processing, including context validation, routing
 * strategy selection, interceptor chain execution, and response handling. The concrete protocol handling logic is fully
 * delegated to the respective {@link Router} implementations (e.g., HTTP, MQ, MCP).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VortexHandler {

    /**
     * Thread-safe map of routing strategies, keyed by runtime protocol code, holding the corresponding {@link Router}
     * implementations.
     * <p>
     * Enables dynamic selection of routing strategies based on the protocol code stored in route assets. MCP is routed
     * as Streamable HTTP only. A thread-safe collection is used to support concurrent access.
     * <p>
     * Uses wildcard generics {@code Router<ServerRequest, ?>} to support different return types (ServerResponse,
     * String) across different protocol implementations.
     * </p>
     */
    private final Map<Integer, Router<ServerRequest, ?>> routers;

    /**
     * Ordered list of interceptors, sorted in ascending order by {@link Handler#getOrder()}, used to execute processing
     * logic at various request stages in sequence.
     * <p>
     * Interceptors are invoked at different phases of the request lifecycle (e.g., pre-processing, post-processing,
     * completion callback).
     * </p>
     */
    private final List<Handler> handlers;

    /**
     * Constructs a {@link VortexHandler} instance, initializing the routing strategy map and interceptor list.
     *
     * @param handlers list of interceptor instances, may be empty
     * @param routers  map of routing strategies, keyed by runtime protocol code, with values being the corresponding
     *                 {@link Router} implementations with wildcard return types
     * @throws NullPointerException if routers is null
     */
    public VortexHandler(List<Handler> handlers, Map<Integer, Router<ServerRequest, ?>> routers) {
        this.routers = Objects.requireNonNull(routers, "Routers map cannot be null");
        this.handlers = handlers.isEmpty() ? List.of(new AccessHandler())
                : handlers.stream().sorted(Comparator.comparingInt(Handler::getOrder)).collect(Collectors.toList());
    }

    /**
     * Handles client requests; this is the unified entry point for the Vortex gateway.
     * <p>
     * This method orchestrates the entire request processing flow:
     * <ol>
     * <li>Retrieve and validate the request context</li>
     * <li>Validate the Assets configuration</li>
     * <li>Execute pre-handle logic for all interceptors</li>
     * <li>Delegate actual forwarding or processing to the selected {@link Router}</li>
     * <li>Execute post-handle and after-completion logic for all interceptors</li>
     * <li>Log request duration, success/failure information</li>
     * </ol>
     *
     * @param request the client's {@link ServerRequest} containing full request information
     * @return {@link Mono<ServerResponse>} the response from the target service, returned reactively
     */
    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            String method = request.methodName();
            String path = request.path();

            final Context context = contextView.get(Context.class);
            if (context == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "Request context is null: clientIp=N/A, method={}, path={}, event=CONTEXT_ERROR",
                        method,
                        path);
                throw new ValidateException(ErrorCode._116000);
            }
            final String ip = context.getX_request_ip();
            ServerWebExchange exchange = request.exchange();
            Logger.info(
                    true,
                    "Vortex",
                    "Request received: clientIp={}, method={}, path={}, event=REQUEST_START",
                    ip,
                    method,
                    path);
            Logger.debug(true, "Vortex", "Request header snapshot: clientIp={}, method={}, path={}", ip, method, path);
            Logger.debug(
                    true,
                    "Vortex",
                    "Request headers: clientIp={}, headers={}",
                    ip,
                    request.headers().asHttpHeaders().toSingleValueMap());
            Logger.debug(true, "Vortex", "Request parameters: clientIp={}, parameters={}", ip, context.getParameters());

            Assets assets = context.getAssets();
            if (assets == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "Assets is null in request context: clientIp={}, method={}, path={}, event=ASSETS_ERROR",
                        ip,
                        method,
                        path);
                throw new ValidateException(ErrorCode._100800);
            }

            Integer protocol = assets.getProtocol();
            if (protocol == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "Invalid protocol: clientIp={}, method={}, path={}, event=INVALID_PROTOCOL, {}",
                        ip,
                        method,
                        path,
                        protocol);
                throw new ValidateException(ErrorCode._116005);
            }

            Router<ServerRequest, ?> router = routers.get(protocol);
            if (router == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "No router found for protocol: clientIp={}, method={}, path={}, event=ROUTER_NOT_FOUND, protocol={}, registeredProtocols={}",
                        ip,
                        method,
                        path,
                        protocol,
                        routers.keySet());
                throw new ValidateException(ErrorCode._100800, "No router for protocol: " + protocol);
            }

            Logger.info(
                    true,
                    "Vortex",
                    "Using router: clientIp={}, method={}, path={}, event=ROUTER_SELECT, {}",
                    ip,
                    method,
                    path,
                    router.getClass().getSimpleName());

            return executePreHandle(exchange, router).flatMap(preHandleResult -> {
                if (!preHandleResult) {
                    Logger.info(
                            true,
                            "Vortex",
                            "Pre-handle validation failed: clientIp={}, method={}, path={}, event=PREHANDLE_ERROR",
                            ip,
                            method,
                            path);
                    throw new ValidateException(ErrorCode._100800);
                }

                if (Integer.valueOf(Consts.ONE).equals(assets.getMock())) {
                    Logger.info(
                            true,
                            "Vortex",
                            "Returning mock data for method: clientIp={}, method={}, path={}, event=MOCK_MODE, {}",
                            ip,
                            method,
                            path,
                            assets.getMethod());

                    return handleMockResponse(context, assets).flatMap(
                                    response -> executePostHandlers(exchange, router, response, null).thenReturn(response))
                            .doOnSuccess(serverResponse -> {
                                long duration = System.currentTimeMillis() - context.getTimestamp();
                                Logger.info(
                                        false,
                                        "Vortex",
                                        "Method: clientIp={}, method={}, path={}, event=MOCK_SUCCESS, {}, Duration: {}ms",
                                        ip,
                                        method,
                                        path,
                                        assets.getMethod(),
                                        duration);
                                if (serverResponse != null) {
                                    Logger.info(
                                            false,
                                            "Vortex",
                                            "Status: clientIp={}, method={}, path={}, event=MOCK_COMPLETE, {}",
                                            ip,
                                            method,
                                            path,
                                            serverResponse.statusCode().value());
                                }
                            }).onErrorResume(Throwable.class, error -> {
                                Logger.error(
                                        false,
                                        "Vortex",
                                        "Error: clientIp={}, method={}, path={}, event=MOCK_ERROR, {}",
                                        ip,
                                        method,
                                        path,
                                        error.getMessage());
                                return executePostHandlersThenError(exchange, router, error);
                            });
                }

                return router.route(request).timeout(Duration.ofSeconds(routeTimeoutSeconds(assets, request)))
                        .retryWhen(buildRetrySpec(assets, ip, method, path)).cast(ServerResponse.class)
                        .flatMap(response -> executePostHandlers(exchange, router, response, null).thenReturn(response))
                        .doOnSuccess(serverResponse -> {
                            long duration = System.currentTimeMillis() - context.getTimestamp();
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "Method: clientIp={}, method={}, path={}, event=REQUEST_SUCCESS, {}, Duration: {}ms",
                                    ip,
                                    method,
                                    path,
                                    assets.getMethod(),
                                    duration);
                            if (serverResponse != null) {
                                Logger.info(
                                        false,
                                        "Vortex",
                                        "Status: clientIp={}, method={}, path={}, event=REQUEST_COMPLETE, {}",
                                        ip,
                                        method,
                                        path,
                                        serverResponse.statusCode().value());
                            }
                        }).onErrorResume(Throwable.class, error -> {
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "Error: clientIp={}, method={}, path={}, event=REQUEST_ERROR, {}",
                                    ip,
                                    method,
                                    path,
                                    error.getMessage());

                            return executePostHandlersThenError(exchange, router, error);
                        });
            });
        });
    }

    /**
     * Sequentially executes the pre-handle logic of all interceptors.
     * <p>
     * Interceptors are invoked in registration order. If any interceptor returns {@code false}, the chain is
     * immediately terminated, preventing subsequent routing.
     * </p>
     *
     * @param exchange the current request/response context
     * @param router   the selected routing strategy instance (with wildcard return type)
     * @return {@link Mono<Boolean>} indicating whether all pre-handle steps passed (true = all passed)
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Router<ServerRequest, ?> router) {
        return Flux.fromIterable(handlers).concatMap(handler -> handler.preHandle(exchange, router, null))
                .all(Boolean::booleanValue);
    }

    /**
     * Sequentially executes post-handle and after-completion logic of all interceptors.
     * <p>
     * First invokes {@link Handler#postHandle} for all handlers in sequence (only if no error), then invokes
     * {@link Handler#afterCompletion}. Completion callbacks should be executed regardless of success or failure.
     * </p>
     *
     * @param exchange the current request/response context
     * @param router   the selected routing strategy instance (with wildcard return type)
     * @param response the response returned from the target service (may be null if error occurred earlier)
     * @param error    the error that occurred (may be null if request succeeded)
     * @return {@link Mono<Void>} that completes when all post handlers have finished
     */
    private Mono<Void> executePostHandlers(
            ServerWebExchange exchange,
            Router<ServerRequest, ?> router,
            Object response,
            Throwable error) {
        ServerResponse serverResponse = response instanceof ServerResponse ? (ServerResponse) response : null;

        Mono<Void> postHandle = Mono.empty();
        if (error == null) {
            postHandle = Flux.fromIterable(handlers)
                    .concatMap(handler -> handler.postHandle(exchange, router, null, serverResponse)).then();
        }

        Mono<Void> afterCompletion = Flux.fromIterable(handlers)
                .concatMap(handler -> handler.afterCompletion(exchange, router, null, serverResponse, error)).then();

        return postHandle.then(afterCompletion);
    }

    /**
     * Executes completion handlers for a failed request and rethrows the original routing error.
     * <p>
     * Handler failures are attached as suppressed exceptions so a null response or cleanup failure cannot hide the
     * original failure, including critical errors such as {@link OutOfMemoryError}.
     * </p>
     *
     * @param exchange the current request/response context
     * @param router   the selected routing strategy instance
     * @param error    the original routing error
     * @return {@link Mono} that always terminates with the original error
     */
    private Mono<ServerResponse> executePostHandlersThenError(
            ServerWebExchange exchange, Router<ServerRequest, ?> router, Throwable error) {
        return executePostHandlers(exchange, router, null, error).onErrorResume(handlerError -> {
            try {
                if (handlerError != error) {
                    error.addSuppressed(handlerError);
                }
                Logger.error(
                        false,
                        "Vortex",
                        "Post handlers failed after routing error: event=POST_HANDLER_ERROR, original={}, suppressed={}",
                        error.getClass().getName(),
                        handlerError.getClass().getName());
            } catch (Throwable ignored) {
                // Preserve the original routing error even when suppression or logging fails.
            }
            return Mono.empty();
        }).then(Mono.error(error));
    }

    /**
     * Builds a retry specification based on the Assets configuration.
     * <p>
     * This method creates a retry strategy that:
     * <ul>
     * <li>Retries up to {@link Assets#getRetries()} times</li>
     * <li>Uses exponential backoff starting at 100ms</li>
     * <li>Only retries on transient errors (network timeouts, 5xx errors)</li>
     * <li>Logs each retry attempt for observability</li>
     * </ul>
     * </p>
     *
     * @param assets The asset configuration containing retry policy
     * @param ip     The client IP for logging
     * @param method The HTTP method for logging
     * @param path   The request path for logging
     * @return A configured {@link Retry} specification
     */
    private Retry buildRetrySpec(Assets assets, String ip, String method, String path) {
        int maxRetries = assets.getRetries() != null && assets.getRetries() > 0 ? assets.getRetries() : 0;

        if (maxRetries == 0) {
            return Retry.max(0);
        }

        return Retry.backoff(maxRetries, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(5)).filter(throwable -> {
            if (throwable instanceof java.util.concurrent.TimeoutException) {
                return true;
            }
            if (throwable instanceof java.net.ConnectException) {
                return true;
            }
            if (throwable instanceof java.net.SocketTimeoutException) {
                return true;
            }
            if (throwable instanceof java.io.IOException) {
                return true;
            }
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException ex = (WebClientResponseException) throwable;
                return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
            }
            return false;
        }).doBeforeRetry(retrySignal -> {
            long attempt = retrySignal.totalRetries() + 1;
            Throwable failure = retrySignal.failure();
            Logger.warn(
                    true,
                    "Vortex",
                    "Retry attempt {}/{} after error: clientIp={}, method={}, path={}, event=RETRY_ATTEMPT, {}",
                    ip,
                    method,
                    path,
                    attempt,
                    maxRetries,
                    failure.getClass().getSimpleName());
        }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
            Logger.error(
                    true,
                    "Vortex",
                    "All {} retry attempts exhausted: clientIp={}, method={}, path={}, event=RETRY_EXHAUSTED",
                    maxRetries,
                    ip,
                    method,
                    path);
            return retrySignal.failure();
        });
    }

    /**
     * Resolves the downstream routing timeout in seconds for one request.
     * <p>
     * Standard routes use {@link Assets#getTimeout()} when configured and fall back to 60 seconds. MCP GET requests are
     * commonly used for SSE or other long-lived stream reads, so their cHRRonfigured timeout is expanded to keep the
     * stream open longer while still retaining an upper bound.
     *
     * @param assets  route asset containing timeout and protocol configuration
     * @param request current server request
     * @return timeout in seconds
     */
    private long routeTimeoutSeconds(Assets assets, ServerRequest request) {
        long timeout = assets.getTimeout() != null && assets.getTimeout() > 0 ? assets.getTimeout() : 60;
        if (assets.getProtocol() != null && assets.getProtocol() == Args.PROTOCOL_MCP
                && HTTP.GET.equalsIgnoreCase(request.methodName())) {
            return timeout * 10;
        }
        return timeout;
    }

    /**
     * Handles mock mode responses by returning the mock data from Assets.result field.
     * <p>
     * This method is invoked when policy=-1 (mock mode). It bypasses the actual downstream service call and returns the
     * pre-configured mock data directly. The mock data is formatted according to the requested format (JSON/XML/BINARY)
     * specified in the context.
     * </p>
     *
     * @param context The request context
     * @param assets  The asset configuration containing the mock data in result field
     * @return A Mono of ServerResponse containing the mock data
     */
    private Mono<ServerResponse> handleMockResponse(Context context, Assets assets) {
        return Mono.fromCallable(() -> {
            String mockData = assets.getResult();

            if (mockData == null || mockData.isEmpty()) {
                Logger.warn(
                        true,
                        "Vortex",
                        "Mock mode has no result data: clientIp={}, method={}",
                        context.getX_request_ip(),
                        assets.getMethod());
                mockData = "{}";
            }

            Logger.info(
                    true,
                    "Vortex",
                    "Mock data prepared: clientIp={}, chars={}",
                    context.getX_request_ip(),
                    mockData.length());

            return mockData;
        }).flatMap(mockData -> {
            Formats format = context.getFormat();
            if (format == null) {
                format = Formats.JSON;
            }

            return ServerResponse.ok().contentType(format.getMediaType()).bodyValue(mockData);
        }).doOnSuccess(response -> {
            long duration = System.currentTimeMillis() - context.getTimestamp();
            Logger.info(
                    false,
                    "Vortex",
                    "Mock response returned: clientIp={}, durationMs={}",
                    context.getX_request_ip(),
                    duration);
        });
    }

}

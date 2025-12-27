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
package org.miaixz.bus.vortex.handler;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Handler;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

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
 * @since Java 17+
 */
public class VortexHandler {

    /**
     * Thread-safe map of routing strategies, keyed by protocol name, holding the corresponding {@link Router}
     * implementations.
     * <p>
     * Enables dynamic selection of routing strategies based on interaction mode, currently supporting HTTP, MQ, and MCP
     * (including both remote Streamable HTTP and local STDIO transports). A thread-safe collection is used to support
     * concurrent access.
     * </p>
     */
    private final Map<String, Router> routers;

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
     * @param routers  map of routing strategies, keyed by protocol name, with values being the corresponding
     *                 {@link Router} implementations
     * @throws NullPointerException if routers is null
     */
    public VortexHandler(List<Handler> handlers, Map<String, Router> routers) {
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
            // Get request method and path for logging
            String method = request.methodName();
            String path = request.path();

            // 1. Retrieve and validate request context
            final Context context = contextView.get(Context.class);
            if (context == null) {
                Logger.info(true, "Vortex", "[N/A] [{}] [{}] [CONTEXT_ERROR] - Request context is null", method, path);
                throw new ValidateException(ErrorCode._116000);
            }
            final String ip = context.getX_request_ipv4();
            ServerWebExchange exchange = request.exchange();
            Logger.info(true, "Vortex", "[{}] [{}] [{}] [REQUEST_START] - Request started", ip, method, path);

            // 2. Validate Assets configuration
            Assets assets = context.getAssets();
            if (assets == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "[{}] [{}] [{}] [ASSETS_ERROR] - Assets is null in request context",
                        ip,
                        method,
                        path);
                throw new ValidateException(ErrorCode._100800);
            }

            // 3. Map mode to router key
            String modeKey = switch (assets.getMode()) {
                case 1 -> Protocol.HTTP.getName(); // HTTP/HTTPS reverse proxy
                case 2 -> Protocol.MQ.getName(); // Message queue
                case 3 -> Protocol.MCP.getName(); // Model Context Protocol (handles both remote and local transports)
                case 4 -> Protocol.GRPC.getName(); // gRPC
                case 5 -> Protocol.WS.getName(); // WebSocket
                default -> throw new ValidateException(ErrorCode._116005);
            };

            // 4. Retrieve the corresponding Router instance
            Router router = routers.get(modeKey);
            if (router == null) {
                Logger.info(
                        true,
                        "Vortex",
                        "[{}] [{}] [{}] [ROUTER_NOT_FOUND] - No router found for mode key: {}",
                        ip,
                        method,
                        path,
                        modeKey);
                throw new ValidateException(ErrorCode._100800, "No router for mode: " + modeKey);
            }

            Logger.info(
                    true,
                    "Vortex",
                    "[{}] [{}] [{}] [ROUTER_SELECT] - Using router: {}",
                    ip,
                    method,
                    path,
                    router.getClass().getSimpleName());

            // 5. Execute pre-interceptors and delegate routing
            return executePreHandle(exchange, router).flatMap(preHandleResult -> {
                if (!preHandleResult) {
                    Logger.info(
                            true,
                            "Vortex",
                            "[{}] [{}] [{}] [PREHANDLE_ERROR] - Pre-handle validation failed",
                            ip,
                            method,
                            path);
                    throw new ValidateException(ErrorCode._100800);
                }

                // Actual routing with timeout + retry mechanism + post-interceptors
                return router.route(request)
                        .timeout(Duration.ofSeconds(assets.getTimeout() != null ? assets.getTimeout() : 60))
                        .retryWhen(buildRetrySpec(assets, ip, method, path))
                        .flatMap(response -> executePostHandlers(exchange, router, response)).doOnSuccess(response -> {
                            long duration = System.currentTimeMillis() - context.getTimestamp();
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "[{}] [{}] [{}] [REQUEST_SUCCESS] - Method: {}, Duration: {}ms",
                                    ip,
                                    method,
                                    path,
                                    assets.getMethod(),
                                    duration);
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "[{}] [{}] [{}] [REQUEST_COMPLETE] - Status: {}",
                                    ip,
                                    method,
                                    path,
                                    response.statusCode().value());
                        }).onErrorResume(error -> {
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "[{}] [{}] [{}] [REQUEST_ERROR] - Error: {}",
                                    ip,
                                    method,
                                    path,
                                    error.getMessage());

                            // Ensure afterCompletion is executed for all handlers even on error
                            return Flux.fromIterable(handlers)
                                    .concatMap(handler -> handler.afterCompletion(exchange, router, null, null, error))
                                    .then(Mono.error(error));
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
     * @param router   the selected routing strategy instance
     * @return {@link Mono<Boolean>} indicating whether all pre-handle steps passed (true = all passed)
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Router router) {
        return Flux.fromIterable(handlers).concatMap(handler -> handler.preHandle(exchange, router, null))
                .all(Boolean::booleanValue);
    }

    /**
     * Sequentially executes post-handle and after-completion logic of all interceptors.
     * <p>
     * First invokes {@link Handler#postHandle} for all handlers in sequence, then invokes
     * {@link Handler#afterCompletion}. Completion callbacks should be executed regardless of success or failure.
     * </p>
     *
     * @param exchange the current request/response context
     * @param router   the selected routing strategy instance
     * @param response the response returned from the target service (may be null if error occurred earlier)
     * @return {@link Mono<ServerResponse>} the original response after interceptor processing
     */
    private Mono<ServerResponse> executePostHandlers(
            ServerWebExchange exchange,
            Router router,
            ServerResponse response) {
        // postHandle chain
        Mono<Void> postHandle = Flux.fromIterable(handlers)
                .concatMap(handler -> handler.postHandle(exchange, router, null, response)).then();

        // afterCompletion chain (normal case)
        Mono<Void> afterCompletion = Flux.fromIterable(handlers)
                .concatMap(handler -> handler.afterCompletion(exchange, router, null, response, null)).then();

        return postHandle.then(afterCompletion).thenReturn(response);
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
            // No retries configured
            return Retry.max(0);
        }

        return Retry.backoff(maxRetries, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(5)).filter(throwable -> {
            // Only retry on transient errors
            if (throwable instanceof java.util.concurrent.TimeoutException) {
                return true; // Reactor timeout
            }
            if (throwable instanceof java.net.ConnectException) {
                return true; // Connection refused
            }
            if (throwable instanceof java.net.SocketTimeoutException) {
                return true; // Read timeout
            }
            if (throwable instanceof java.io.IOException) {
                return true; // Network I/O errors
            }
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException ex = (WebClientResponseException) throwable;
                // Retry on 5xx server errors and 429 Too Many Requests
                return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
            }
            return false; // Don't retry on other errors (4xx client errors, etc.)
        }).doBeforeRetry(retrySignal -> {
            long attempt = retrySignal.totalRetries() + 1;
            Throwable failure = retrySignal.failure();
            Logger.warn(
                    true,
                    "Vortex",
                    "[{}] [{}] [{}] [RETRY_ATTEMPT] - Retry attempt {}/{} after error: {}",
                    ip,
                    method,
                    path,
                    attempt,
                    maxRetries,
                    failure.getMessage());
        }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
            Logger.error(
                    true,
                    "Vortex",
                    "[{}] [{}] [{}] [RETRY_EXHAUSTED] - All {} retry attempts exhausted",
                    ip,
                    method,
                    path,
                    maxRetries);
            return retrySignal.failure();
        });
    }

}

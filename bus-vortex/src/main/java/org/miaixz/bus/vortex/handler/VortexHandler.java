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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.support.HttpRequestRouter;
import org.miaixz.bus.vortex.support.MqRequestRouter;
import org.miaixz.bus.vortex.support.McpRequestRouter;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * Request handling entry class, responsible for routing requests and asynchronously invoking multiple interceptor
 * logics.
 * <p>
 * This class implements the control flow for request processing, including request validation, routing strategy
 * selection, interceptor execution, and response handling. The specific protocol handling logic is entirely delegated
 * to their respective strategy implementers (HTTP, MQ, MCP).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VortexHandler {

    /**
     * A thread-safe map of strategies, storing strategy implementations indexed by protocol name.
     * <p>
     * This map allows dynamic selection of routing strategies based on the protocol, supporting HTTP, MQ, and MCP
     * protocols. {@code ConcurrentHashMap} is used to ensure thread safety.
     * </p>
     */
    private final Map<String, Router> strategies = new ConcurrentHashMap<>();

    /**
     * The default router strategy, used when a specific strategy is not provided or found.
     * <p>
     * The HTTP strategy is used as a fallback behavior by default.
     * </p>
     */
    private final Router defaultRouter;

    /**
     * A list of ordered interceptors, used to process requests in a specific sequence.
     * <p>
     * Interceptors are invoked at different stages of request processing (e.g., pre-processing, post-processing).
     * Interceptors are sorted by their order property to ensure execution sequence.
     * </p>
     */
    private final List<Handler> handlers;

    /**
     * Constructs a {@code VortexHandler}, initializing the strategy map and the interceptor list.
     *
     * @param handlers A list of asynchronous interceptor instances, used to handle various stages of a request.
     * @throws NullPointerException If handlers or the default strategy is null.
     */
    public VortexHandler(List<Handler> handlers) {
        strategies.put(Protocol.HTTP.name, new HttpRequestRouter());
        strategies.put(Protocol.MQ.name, new MqRequestRouter());
        strategies.put(Protocol.MCP.name, new McpRequestRouter());
        defaultRouter = strategies.get(Protocol.HTTP.name);
        Objects.requireNonNull(defaultRouter, "Default strategy cannot be null");
        // If handlers is empty, use the default AccessHandler
        this.handlers = handlers.isEmpty() ? List.of(new AccessHandler())
                : handlers.stream().sorted(Comparator.comparingInt(Handler::getOrder)).collect(Collectors.toList());
    }

    /**
     * Handles client requests, executes the control flow, and returns a response.
     * <p>
     * This method is the entry point for request processing, coordinating the entire request handling flow. The
     * processing flow includes:
     * <ol>
     * <li>Initialization and validation of the request context.</li>
     * <li>Validation of configured assets.</li>
     * <li>Selection of the routing strategy.</li>
     * <li>Execution of pre-processing handlers.</li>
     * <li>Delegation to the strategy implementer to handle the request.</li>
     * <li>Execution of post-processing handlers.</li>
     * </ol>
     *
     * @param request The client's {@link ServerRequest} object, containing all request information.
     * @return {@link Mono<ServerResponse>} containing the response from the target service, returned reactively.
     */
    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        return Mono.defer(() -> {
            // Get request method and path for logging
            String method = request.methodName();
            String path = request.path();

            // 1. Initialize and validate the request context
            Context context = Context.get(request);
            if (context == null) {
                Logger.info("==>    Handler: [N/A] [{}] [{}] [CONTEXT_ERROR] - Request context is null", method, path);
                throw new ValidateException(ErrorCode._116000);
            }
            ServerWebExchange exchange = request.exchange();
            Logger.info("==>    Handler: [N/A] [{}] [{}] [REQUEST_START] - Request started", method, path);

            // 2. Validate configured assets
            Assets assets = context.getAssets();
            if (assets == null) {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ASSETS_ERROR] - Assets is null in request context",
                        method,
                        path);
                throw new ValidateException(ErrorCode._100800);
            }

            // 3. Select the routing strategy
            String mode = switch (assets.getMode()) {
                case 1 -> Protocol.HTTP.name();
                case 2 -> Protocol.MQ.name();
                case 3 -> Protocol.MCP.name();
                default -> Protocol.HTTP.name();
            };

            Router router = strategies.getOrDefault(mode, defaultRouter);
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [ROUTER_SELECT] - Using route strategy: {}",
                    method,
                    path,
                    router.getClass().getSimpleName());

            // 4. Execute pre-processing
            return executePreHandle(exchange, router).flatMap(preHandleResult -> {
                if (!preHandleResult) {
                    Logger.info(
                            "==>    Handler: [N/A] [{}] [{}] [PREHANDLE_ERROR] - Pre-handle validation failed",
                            method,
                            path);
                    throw new ValidateException(ErrorCode._100800);
                }

                // 5. Delegate to the strategy implementer to handle the request
                return router.route(request, context, assets)
                        .flatMap(response -> executePostHandlers(exchange, router, response)).doOnSuccess(response -> {
                            long duration = System.currentTimeMillis() - context.getTimestamp();
                            Logger.info(
                                    "==>    Handler: [N/A] [{}] [{}] [REQUEST_SUCCESS] - Method: {}, Duration: {}ms",
                                    method,
                                    path,
                                    assets.getMethod(),
                                    duration);
                        }).onErrorResume(error -> {
                            Logger.info(
                                    "==>    Handler: [N/A] [{}] [{}] [REQUEST_ERROR] - Error processing request: {}",
                                    method,
                                    path,
                                    error.getMessage());
                            return Mono.whenDelayError(
                                    handlers.stream().map(
                                            handler -> handler.afterCompletion(exchange, router, null, null, error))
                                            .collect(Collectors.toList()))
                                    .then(Mono.error(error));
                        });
            });
        }).doOnSuccess(response -> {
            String method = request.methodName();
            String path = request.path();
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [REQUEST_COMPLETE] - Request completed with status: {}",
                    method,
                    path,
                    response.statusCode().value());
        });
    }

    /**
     * Executes the pre-processing logic of all interceptors.
     * <p>
     * This method calls the {@code preHandle} method of all interceptors in parallel and collects their results. It
     * returns {@code true} only if all interceptors return {@code true}, indicating that all pre-processing steps have
     * passed.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response context information.
     * @param router   The routing strategy, used to determine how to route the request.
     * @return {@code Mono<Boolean>} indicating whether all pre-processing steps passed ({@code true}) or if any
     *         interceptor blocked the request ({@code false}).
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Router router) {
        return Mono.zip(
                handlers.stream().map(handler -> handler.preHandle(exchange, router, null))
                        .collect(Collectors.toList()),
                results -> results.length > 0 && Arrays.stream(results).allMatch(Boolean.class::cast));
    }

    /**
     * Executes the post-processing logic of all interceptors.
     * <p>
     * This method calls the {@code postHandle} and {@code afterCompletion} methods of all interceptors in parallel. The
     * {@code postHandle} method is executed before the response is sent to the client, and the {@code afterCompletion}
     * method is executed after the request has been fully processed.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response context information.
     * @param router   The routing strategy, used to determine how to route the request.
     * @param response The {@link ServerResponse} object, containing the response status, headers, and body.
     * @return {@code Mono<ServerResponse>} The processed response, which may have been modified by the interceptors.
     */
    private Mono<ServerResponse> executePostHandlers(
            ServerWebExchange exchange,
            Router router,
            ServerResponse response) {
        return Mono
                .whenDelayError(
                        handlers.stream().map(handler -> handler.postHandle(exchange, router, null, response))
                                .collect(Collectors.toList()))
                .thenReturn(response).flatMap(
                        res -> Mono.whenDelayError(
                                handlers.stream()
                                        .map(handler -> handler.afterCompletion(exchange, router, null, res, null))
                                        .collect(Collectors.toList()))
                                .thenReturn(res));
    }

}

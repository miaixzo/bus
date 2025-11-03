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
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
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
    private final Map<String, Router> routers;

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
     * @param routers  A list of asynchronous interceptor instances, used to handle various stages of a request.
     * @throws NullPointerException If handlers or the default strategy is null.
     */
    public VortexHandler(List<Handler> handlers, Map<String, Router> routers) {
        this.routers = routers;
        Objects.requireNonNull(this.routers, "Default router cannot be null");
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
        return Mono.deferContextual(contextView -> {
            // Get request method and path for logging
            String method = request.methodName();
            String path = request.path();

            // 1. Initialize and validate the request context
            final Context context = contextView.get(Context.class);
            if (context == null) {
                Logger.info(true, "Vortex", "[N/A] [{}] [{}] [CONTEXT_ERROR] - Request context is null", method, path);
                throw new ValidateException(ErrorCode._116000);
            }
            final String ip = context.getX_request_ipv4();
            ServerWebExchange exchange = request.exchange();
            Logger.info(true, "Vortex", "[{}] [{}] [{}] [REQUEST_START] - Request started", ip, method, path);

            // 2. Validate configured assets
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

            // 3. Select the routing strategy
            String mode = switch (assets.getMode()) {
                case 1 -> Protocol.HTTP.getName();
                case 2 -> Protocol.MQ.getName();
                case 3 -> Protocol.MCP.getName();
                case 4 -> Protocol.WS.getName();
                default -> Protocol.HTTP.getName();
            };

            Router router = routers.get(mode);
            Logger.info(
                    true,
                    "Vortex",
                    "[{}] [{}] [{}] [ROUTER_SELECT] - Using route strategy: {}",
                    ip,
                    method,
                    path,
                    router.getClass().getSimpleName());

            // 4. Execute pre-processing
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

                // 5. Delegate to the strategy implementer to handle the request
                return router.route(request).flatMap(response -> executePostHandlers(exchange, router, response))
                        .doOnSuccess(response -> {
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
                                    "[{}] [{}] [{}] [REQUEST_COMPLETE] - Request completed with status: {}",
                                    ip,
                                    method,
                                    path,
                                    response.statusCode().value());
                        }).onErrorResume(error -> {
                            Logger.info(
                                    false,
                                    "Vortex",
                                    "[{}] [{}] [{}] [REQUEST_ERROR] - Error processing request: {}",
                                    ip,
                                    method,
                                    path,
                                    error.getMessage());
                            // Sequentially run afterCompletion for all handlers before re-throwing the error
                            return Flux.fromIterable(handlers)
                                    .concatMap(handler -> handler.afterCompletion(exchange, router, null, null, error))
                                    .then(Mono.error(error));
                        });
            });
        });
    }

    /**
     * Executes the pre-processing logic of all interceptors sequentially.
     * <p>
     * This method calls the {@code preHandle} method of all interceptors in a sequential chain. If any interceptor
     * returns {@code false}, the chain is immediately terminated, and the method returns {@code Mono<Boolean>} with a
     * value of {@code false}, effectively "short-circuiting" the execution.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response context information.
     * @param router   The routing strategy, used to determine how to route the request.
     * @return {@code Mono<Boolean>} indicating whether all pre-processing steps passed ({@code true}) or if any
     *         interceptor blocked the request ({@code false}).
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Router router) {
        return Flux.fromIterable(handlers).concatMap(handler -> handler.preHandle(exchange, router, null))
                .all(result -> result);
    }

    /**
     * Executes the post-processing logic of all interceptors sequentially.
     * <p>
     * This method first calls the {@code postHandle} method of all interceptors in sequence. After that completes, it
     * calls the {@code afterCompletion} method of all interceptors, also in sequence.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response context information.
     * @param router   The routing strategy, used to determine how to route the request.
     * @param response The {@link ServerResponse} object, containing the response status, headers, and body.
     * @return {@code Mono<ServerResponse>} The original response, after all interceptors have been processed.
     */
    private Mono<ServerResponse> executePostHandlers(
            ServerWebExchange exchange,
            Router router,
            ServerResponse response) {
        Mono<Void> postHandleChain = Flux.fromIterable(handlers)
                .concatMap(handler -> handler.postHandle(exchange, router, null, response)).then();

        Mono<Void> afterCompletionChain = Flux.fromIterable(handlers)
                .concatMap(handler -> handler.afterCompletion(exchange, router, null, response, null)).then();

        return postHandleChain.then(afterCompletionChain).thenReturn(response);
    }

}

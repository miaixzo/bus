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

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Handles asynchronous pre-processing logic for API requests.
 * <p>
 * This handler is typically used to perform initial checks or setup before the main request processing. It is ordered
 * with {@code Ordered.HIGHEST_PRECEDENCE}, ensuring it runs early in the handler chain.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessHandler extends AbstractHandler {

    /**
     * Asynchronous pre-processing method, typically used for permission validation or initial setup.
     * <p>
     * This method logs basic request information (HTTP method and path) and then proceeds. Currently, it assumes
     * validation passes and returns {@code true}.
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object, containing the HTTP request and response.
     * @param service  The service instance (typically a strategy object), may be {@code null}.
     * @param args     Method arguments, may be {@code null}.
     * @return {@code Mono<Boolean>} indicating whether the pre-handle validation passed ({@code true}) or failed
     *         ({@code false}).
     */
    @Override
    public Mono<Boolean> preHandle(ServerWebExchange exchange, Object service, Object args) {
        return Mono.fromCallable(() -> {
            // Get context and IP
            Context context = exchange.getAttribute(Context.$);
            String ip = (context != null) ? context.getX_request_ip() : "N/A";

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            Logger.info(
                    true,
                    "Access",
                    "[{}] [{}] [{}] [ACCESS_PREHANDLE] - Performing async preHandle validation for request",
                    ip,
                    method,
                    path);

            return true; // Assume validation passes
        });
    }

    /**
     * Asynchronous post-processing method, typically used to process response data.
     * <p>
     * This method logs basic request information after the main request handling has completed.
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object, containing the HTTP request and response.
     * @param service  The service instance, may be {@code null}.
     * @param args     Method arguments, may be {@code null}.
     * @param result   The result returned by the interface method, may be {@code null}.
     * @return {@code Mono<Void>} indicating the asynchronous completion of post-processing.
     */
    @Override
    public Mono<Void> postHandle(ServerWebExchange exchange, Object service, Object args, Object result) {
        return Mono.fromRunnable(() -> {
            // Get context and IP
            Context context = exchange.getAttribute(Context.$);
            String ip = (context != null) ? context.getX_request_ip() : "N/A";

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            Logger.info(
                    false,
                    "Access",
                    "[{}] [{}] [{}] [ACCESS_POSTHANDLE] - Post-processing response for request",
                    ip,
                    method,
                    path);
        });
    }

    /**
     * Asynchronous completion method, executed after the request is fully completed (whether successfully or with an
     * exception).
     * <p>
     * This method is typically used for cleanup, resource release, or final logging. It logs basic request information
     * and any exception that occurred during processing.
     * </p>
     *
     * @param exchange  The current {@link ServerWebExchange} object, containing the HTTP request and response.
     * @param service   The service instance, may be {@code null}.
     * @param args      Method arguments, may be {@code null}.
     * @param result    The final response result, may be {@code null}.
     * @param exception The exception object (if any) that occurred during request processing, may be {@code null}.
     * @return {@code Mono<Void>} indicating the asynchronous completion of the after-completion processing.
     */
    @Override
    public Mono<Void> afterCompletion(
            ServerWebExchange exchange,
            Object service,
            Object args,
            Object result,
            Throwable exception) {
        return Mono.fromRunnable(() -> {
            // Get context and IP
            Context context = exchange.getAttribute(Context.$);
            String ip = (context != null) ? context.getX_request_ip() : "N/A";

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
            String exceptionMsg = exception != null ? exception.getMessage() : "none";

            Logger.info(
                    false,
                    "Access",
                    "[{}] [{}] [{}] [ACCESS_COMPLETION] - Request completed, exception: {}",
                    ip,
                    method,
                    path,
                    exceptionMsg);
        });
    }

}

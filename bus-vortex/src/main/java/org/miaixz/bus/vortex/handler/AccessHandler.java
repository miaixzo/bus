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

import org.miaixz.bus.logger.Logger;
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
            // We need to log basic information, not try to get exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            // Use Logger directly, not VortexLogger
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [ACCESS_PREHANDLE] - Performing async preHandle validation for request",
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
            // We need to log basic information, not try to get exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            // Use Logger directly, not VortexLogger
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [ACCESS_POSTHANDLE] - Post-processing response for request",
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
            // We need to log basic information, not try to get exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
            String exceptionMsg = exception != null ? exception.getMessage() : "none";

            // Use Logger directly
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [ACCESS_COMPLETION] - Request completed, exception: {}",
                    method,
                    path,
                    exceptionMsg);
        });
    }

}

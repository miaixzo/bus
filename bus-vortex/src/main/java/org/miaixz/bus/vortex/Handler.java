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
package org.miaixz.bus.vortex;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Asynchronous interceptor interface, defining three stages of request processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler {

    /**
     * Retrieves the order of this handler. Handlers with smaller order values are executed earlier.
     *
     * @return The order value; smaller values indicate higher precedence.
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Asynchronous pre-processing method, executed before request handling.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param service  The service instance (typically a strategy object).
     * @param args     Method arguments, may be {@code null}.
     * @return {@code Mono<Boolean>} indicating whether validation passed ({@code true}) or failed ({@code false}).
     */
    default Mono<Boolean> preHandle(ServerWebExchange exchange, Object service, Object args) {
        return Mono.just(true);
    }

    /**
     * Asynchronous post-processing method, executed after request handling.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param service  The service instance.
     * @param args     Method arguments, may be {@code null}.
     * @param result   The result returned by the interface method.
     * @return {@code Mono<Void>} indicating the completion of asynchronous processing.
     */
    default Mono<Void> postHandle(ServerWebExchange exchange, Object service, Object args, Object result) {
        return Mono.empty();
    }

    /**
     * Asynchronous completion method, executed after the request is completed (regardless of success or failure).
     *
     * @param exchange  The current {@link ServerWebExchange} object.
     * @param service   The service instance.
     * @param args      Method arguments, may be {@code null}.
     * @param result    The final response result, may be {@code null}.
     * @param exception The exception object (if any), may be {@code null}.
     * @return {@code Mono<Void>} indicating the completion of asynchronous processing.
     */
    default Mono<Void> afterCompletion(
            ServerWebExchange exchange,
            Object service,
            Object args,
            Object result,
            Throwable exception) {
        return Mono.empty();
    }

}

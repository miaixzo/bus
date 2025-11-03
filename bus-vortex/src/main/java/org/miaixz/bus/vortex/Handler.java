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

import org.miaixz.bus.vortex.handler.VortexHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Defines an interceptor-style contract for cross-cutting concerns at the final stage of request handling.
 * <p>
 * Implementations of this interface can be registered with the {@link VortexHandler} to perform actions before and
 * after the request is routed to its final destination by a {@link Router}. This provides an AOP-like mechanism for
 * tasks such as logging, metrics, or final response modification.
 *
 * @author Kimi Liu
 * @see VortexHandler
 * @since Java 17+
 */
public interface Handler {

    /**
     * Retrieves the order of this handler within the handler chain. Handlers with smaller order values are executed
     * earlier.
     *
     * @return The order value; smaller values indicate higher precedence.
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Intercepts the request before it is routed by the {@link Router}.
     * <p>
     * This method is called by {@link VortexHandler} after the strategy chain has completed but before the final
     * routing decision is executed. Returning a {@code Mono} that emits {@code false} will short-circuit the request
     * processing and prevent the router from being called.
     *
     * @param exchange The current {@link ServerWebExchange}.
     * @param service  The selected {@link Router} instance that is about to be executed.
     * @param args     Method arguments, reserved for future use (currently {@code null}).
     * @return A {@code Mono<Boolean>} emitting {@code true} to proceed with the request, or {@code false} to block it.
     */
    default Mono<Boolean> preHandle(ServerWebExchange exchange, Object service, Object args) {
        return Mono.just(true);
    }

    /**
     * Intercepts the request after the {@link Router} has successfully executed and produced a response, but before the
     * response is sent to the client.
     *
     * @param exchange The current {@link ServerWebExchange}.
     * @param service  The {@link Router} instance that was executed.
     * @param args     Method arguments, reserved for future use (currently {@code null}).
     * @param result   The {@link org.springframework.web.reactive.function.server.ServerResponse} returned by the
     *                 router.
     * @return A {@code Mono<Void>} that signals the completion of the post-processing logic.
     */
    default Mono<Void> postHandle(ServerWebExchange exchange, Object service, Object args, Object result) {
        return Mono.empty();
    }

    /**
     * Called after the request processing is fully complete and the response has been sent, regardless of whether an
     * error occurred.
     * <p>
     * This method is the ideal place for cleanup, resource release, or final logging operations that must run even if
     * preceding steps failed.
     *
     * @param exchange  The current {@link ServerWebExchange}.
     * @param service   The {@link Router} instance that was intended to be or was executed.
     * @param args      Method arguments, reserved for future use (currently {@code null}).
     * @param result    The final {@link org.springframework.web.reactive.function.server.ServerResponse}, or
     *                  {@code null} if an error occurred before a response was generated.
     * @param exception The exception that was thrown during processing, or {@code null} if the request completed
     *                  successfully.
     * @return A {@code Mono<Void>} that signals the completion of the final cleanup logic.
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

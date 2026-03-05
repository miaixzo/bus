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

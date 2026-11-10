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
package org.miaixz.bus.starter.vortex;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.spring.web.routing.RouteErrorPolicy;

import reactor.core.publisher.Mono;

/**
 * Compatibility adapter for code that applies only the Vortex HTTP route policy.
 *
 * @author Kimi Liu
 */
final class VortexRouteErrorHandler implements WebExceptionHandler {

    /**
     * Route-only reactive adapter that passes unmatched errors to the next handler.
     */
    private final ReactiveErrorHandler delegate;

    /**
     * Creates a route-only adapter using the shared reactive implementation.
     *
     * @param policy compiled listener policy
     */
    VortexRouteErrorHandler(RouteErrorPolicy policy) {
        this.delegate = new ReactiveErrorHandler(policy.compiled(), null);
    }

    /**
     * Applies the gateway route policy and delegates unmatched failures.
     *
     * @param exchange current gateway exchange
     * @param failure  original gateway failure
     * @return response completion or the original failure for the next handler
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable failure) {
        return this.delegate.handle(exchange, failure);
    }

}

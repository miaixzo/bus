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
package org.miaixz.bus.vortex.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.guard.AdmissionGate;

import reactor.core.publisher.Mono;

/**
 * Enforces the process-wide limit for complete reactive request lifecycles.
 * <p>
 * A request permit is acquired before the remaining filter chain is subscribed and is retained until completion,
 * failure or cancellation of the response write. Emergency memory pressure and capacity exhaustion are rejected with
 * HTTP 503 and a short retry hint.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Ordered.HIGHEST_PRECEDENCE)
public final class AdmissionFilter extends AbstractFilter {

    /**
     * Creates the stateless process-wide admission filter.
     */
    public AdmissionFilter() {
        // No initialization required.
    }

    /**
     * Admits the exchange and binds its permit to the complete downstream lifecycle.
     *
     * @param exchange current server exchange
     * @param chain    remaining WebFilter chain
     * @return completion of either the admitted chain or the rejection response
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.defer(() -> {
            if (Holder.memoryPressure().rejectAll()) {
                return reject(exchange);
            }
            AdmissionGate.Lease lease = Holder.admissionGate().tryAcquireRequest();
            if (lease == null) {
                return reject(exchange);
            }
            try {
                return chain.filter(exchange).doFinally(signal -> lease.close());
            } catch (Throwable error) {
                lease.close();
                return Mono.error(error);
            }
        });
    }

    /**
     * Writes a body-less overload response that clients may retry after one second.
     *
     * @param exchange rejected exchange
     * @return response-write completion
     */
    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
        return exchange.getResponse().setComplete();
    }

}

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
package org.miaixz.bus.vortex.strategy.request;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.strategy.RequestStrategy;

import reactor.core.publisher.Mono;

/**
 * Prepares SDC queries and leaves POST bodies available for the downstream executor.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class SdcRequestStrategy extends RequestStrategy {

    /**
     * Identifies this stage as part of the SDC strategy chain.
     *
     * @return the SDC route identifier
     */
    @Override
    public Integer protocol() {
        return Args.ROUTE_SDC;
    }

    /**
     * Captures the original credential headers and query selectors, then prepares the shared request context and
     * preserves the POST body for forwarding.
     *
     * @param exchange incoming SDC exchange
     * @param chain    remaining strategies in the SDC chain
     * @return completion of the remaining strategy chain
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(view -> {
            Context context = view.get(Context.class);
            var request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            var input = new Input(headers.getOrEmpty(Http.Auth.API_KEY_HEADER),
                    headers.getFirst(HttpHeaders.CONTENT_TYPE), request.getQueryParams().keySet());
            ServerWebExchange prepared = prepare(exchange, context, false);
            return Mono.defer(
                    () -> context.getHttpMethod() == Http.Method.GET ? parse(prepared, chain, context)
                            : chain.apply(prepared))
                    .contextWrite(current -> current.put(Input.class, input));
        });
    }

    /**
     * Uses the connected peer for per-IP rate limiting.
     *
     * @param request current request
     * @return connected peer address
     */
    @Override
    protected String getClientIp(ServerHttpRequest request) {
        var peer = request.getRemoteAddress();
        return peer == null ? Normal.UNKNOWN : peer.getHostString();
    }

    /**
     * Original input needed by the SDC stages before single-value conversion and query sanitization.
     *
     * @param apiKeys     values supplied in the application credential header
     * @param contentType original media type
     * @param queryNames  supplied query names, including valueless parameters
     */
    public record Input(List<String> apiKeys, String contentType, Set<String> queryNames) {
    }

}

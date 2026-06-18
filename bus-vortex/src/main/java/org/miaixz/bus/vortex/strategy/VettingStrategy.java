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
package org.miaixz.bus.vortex.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Mono;

/**
 * Basic vetting strategy for routes without protocol-specific validation rules.
 * <p>
 * This strategy only keeps validations that are useful across protocols, such as rejecting {@code undefined} values.
 * Route resolution, authorization, signature checks, and enrichment belong to qualifier strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class VettingStrategy extends AbstractStrategy {

    /**
     * Hop-by-hop request headers that must not be forwarded to downstream services.
     */
    private static final List<String> HOP_BY_HOP_HEADERS = List.of(
            HttpHeaders.HOST,
            HttpHeaders.CONNECTION,
            "Keep-Alive",
            "Proxy-Authenticate",
            "Proxy-Authorization",
            "TE",
            "Trailer",
            HttpHeaders.TRANSFER_ENCODING,
            "Upgrade",
            HttpHeaders.CONTENT_LENGTH);

    /**
     * Creates a basic vetting strategy.
     */
    public VettingStrategy() {
        // No initialization required.
    }

    /**
     * Applies common validation.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return validation completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return validateAndEnrich(exchange, context)
                    .then(Mono.defer(() -> chain.apply(sanitizeForwardHeaders(exchange, context))));
        });
    }

    /**
     * Runs common request validation.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return validation completion signal
     */
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return validateParameters(context);
    }

    /**
     * Performs basic request-parameter validation shared by all generic routes.
     *
     * @param context request context
     * @return validation completion signal
     */
    protected Mono<Void> validateParameters(Context context) {
        return Mono.fromRunnable(() -> checkForUndefinedValues(context.getParameters()));
    }

    /**
     * Removes connection-level request headers before the routing stage sees the exchange.
     * <p>
     * The original request domain is captured before {@code Host} is removed so later enrichment keeps the same
     * semantics while downstream forwarding receives a sanitized request.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return exchange with sanitized request headers
     */
    protected ServerWebExchange sanitizeForwardHeaders(ServerWebExchange exchange, Context context) {
        ServerHttpRequest request = exchange.getRequest();
        if (StringKit.isBlank(context.getX_request_domain())) {
            context.setX_request_domain(determineRequestDomain(request));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.addAll(request.getHeaders());
        sanitizeForwardHeaders(headers);
        context.setHeaders(headers.toSingleValueMap());

        ServerHttpRequest requestDecorator = new ServerHttpRequestDecorator(request) {

            /**
             * Returns request headers after connection-level forwarding cleanup.
             *
             * @return sanitized request headers
             */
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
        return exchange.mutate().request(requestDecorator).build();
    }

    /**
     * Removes connection-level headers from one mutable header collection.
     *
     * @param headers request headers to sanitize
     */
    private void sanitizeForwardHeaders(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        List<String> connectionValues = new ArrayList<>(headers.getOrEmpty(HttpHeaders.CONNECTION));
        for (String connectionValue : connectionValues) {
            if (StringKit.isBlank(connectionValue)) {
                continue;
            }
            for (String extension : connectionValue.split(Symbol.COMMA)) {
                String name = extension == null ? null : extension.trim();
                if (StringKit.isNotBlank(name)) {
                    headers.remove(name);
                }
            }
        }
        HOP_BY_HOP_HEADERS.forEach(headers::remove);
    }

    /**
     * Rejects request parameters that contain the literal {@code undefined} token.
     *
     * @param params request parameters
     */
    protected void checkForUndefinedValues(Map<String, Object> params) {
        boolean hasUndefinedValue = params.entrySet().stream().anyMatch(
                entry -> Normal.UNDEFINED.equalsIgnoreCase(entry.getKey())
                        || Normal.UNDEFINED.equalsIgnoreCase(String.valueOf(entry.getValue())));

        if (hasUndefinedValue) {
            throw new ValidateException(ErrorCode._100101);
        }
    }

}

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

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Mono;

/**
 * Basic vetting strategy for routes without protocol-specific validation rules.
 * <p>
 * This strategy only keeps validations that are useful across protocols: rejecting {@code undefined} values, merging
 * authorization attributes, enriching request metadata, and removing gateway control parameters before forwarding.
 * Protocol checks, timestamp checks, and signature checks belong to protocol-specific vetting strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class VettingStrategy extends AbstractStrategy {

    /**
     * Creates a basic vetting strategy.
     */
    public VettingStrategy() {
        // No initialization required.
    }

    /**
     * Applies common validation and enrichment.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return validation completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return validateAndEnrich(exchange, context).then(chain.apply(exchange));
        });
    }

    /**
     * Runs common validation, authorization attribute merge, request metadata enrichment, and forwarding cleanup.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return validation completion signal
     */
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return validateParameters(context)
                .then(Mono.fromRunnable(() -> mergeAuthorizationAttributes(exchange, context)))
                .then(Mono.fromRunnable(() -> enrich(exchange, context)))
                .then(Mono.fromRunnable(() -> removeForwardingControlParameters(context)));
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

    /**
     * Merges authorization attributes after protocol validation.
     *
     * @param exchange current exchange carrying deferred authorization attributes
     * @param context  request context receiving merged attributes
     */
    protected void mergeAuthorizationAttributes(ServerWebExchange exchange, Context context) {
        Object attributes = exchange.getAttributes().remove(QualifierStrategy.AUTHORIZATION_ATTRIBUTES);
        if (!(attributes instanceof Map<?, ?> authMap) || authMap.isEmpty()) {
            return;
        }
        authMap.forEach((key, value) -> {
            if (key != null && value != null) {
                context.getParameters().put(key.toString(), value);
            }
        });
    }

    /**
     * Adds derived request metadata to the context and parameter map.
     *
     * @param exchange current exchange
     * @param context  request context
     */
    protected void enrich(ServerWebExchange exchange, Context context) {
        String x_request_id = context.getX_request_id();
        if (StringKit.isEmpty(x_request_id)) {
            x_request_id = context.getX_request_id();
            context.setX_request_id(x_request_id);
        }
        context.getParameters().put("x_request_id", x_request_id);

        String x_request_ipv4 = context.getX_request_ip();
        if (StringKit.isEmpty(x_request_ipv4)) {
            x_request_ipv4 = this.getClientIp(exchange.getRequest());
            context.setX_request_ipv4(x_request_ipv4);
        }
        context.getParameters().put("x_request_ipv4", x_request_ipv4);

        String x_request_domain = context.getX_request_domain();
        if (StringKit.isEmpty(x_request_domain)) {
            x_request_domain = this.determineRequestDomain(exchange.getRequest());
            context.setX_request_domain(x_request_domain);
        }
        context.getParameters().put("x_request_domain", x_request_domain);
    }

    /**
     * Removes gateway control parameters from the downstream-visible parameter map after validation has finished.
     * <p>
     * Route resolution, required-parameter checks, and signature verification must run before this method. Matching is
     * case-insensitive so variants such as {@code Method}, {@code VERSION}, or {@code Sign} are removed as well.
     *
     * @param context request context
     */
    protected void removeForwardingControlParameters(Context context) {
        context.getParameters().keySet().removeIf(Args::isForwardingControlParameter);
    }

    /**
     * Determines the request domain from proxy-aware authority headers.
     *
     * @param request current request
     * @return request authority or a stable unknown fallback
     */
    private String determineRequestDomain(ServerHttpRequest request) {
        return getAuthority(request).orElseGet(() -> {
            Logger.warn(
                    true,
                    "Vortex",
                    "Unable to determine request domain (host:port). Using default value: strategy=vetting, {}",
                    (Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO));
            return Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        });
    }

}

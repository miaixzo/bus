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

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Mono;

/**
 * Basic vetting strategy for routes without protocol-specific validation rules.
 * <p>
 * This strategy only keeps validations that are useful across protocols, such as rejecting {@code undefined} values.
 * Route resolution, authorization, signature checks, enrichment, and forwarding cleanup belong to qualifier strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class VettingStrategy extends AbstractStrategy {

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
            return validateAndEnrich(exchange, context).then(chain.apply(exchange));
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

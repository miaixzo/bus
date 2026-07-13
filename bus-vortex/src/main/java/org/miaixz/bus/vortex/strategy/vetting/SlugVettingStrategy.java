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
package org.miaixz.bus.vortex.strategy.vetting;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import reactor.core.publisher.Mono;

/**
 * Performs public slug request vetting.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class SlugVettingStrategy extends VettingStrategy {

    /**
     * Returns this strategy's dynamic protocol.
     *
     * @return slug protocol number
     */
    @Override
    public Integer protocol() {
        return Args.PROTOCOL_SLUG;
    }

    /**
     * Validates public slug request state, then delegates shared validation and header cleanup to the base strategy.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return completion signal
     */
    @Override
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return Mono.fromRunnable(() -> {
            if (context == null || context.getHttpMethod() == null) {
                throw new ValidateException(ErrorCode._100802);
            }
        }).then(super.validateAndEnrich(exchange, context));
    }

}

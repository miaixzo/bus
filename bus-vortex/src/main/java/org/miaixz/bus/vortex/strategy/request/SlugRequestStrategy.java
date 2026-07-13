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

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;
import org.miaixz.bus.vortex.strategy.RequestStrategy;

import reactor.core.publisher.Mono;

/**
 * Parses public slug forwarding requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class SlugRequestStrategy extends RequestStrategy {

    /**
     * Matcher used to decide whether the request belongs to public slug forwarding.
     */
    private final SlugRouteMatcher matcher;

    /**
     * Creates a slug request strategy.
     *
     * @param matcher slug route matcher
     */
    public SlugRequestStrategy(SlugRouteMatcher matcher) {
        this.matcher = matcher;
    }

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
     * Returns whether the current request matches a public slug asset.
     *
     * @param exchange current exchange
     * @return {@code true} when matched
     */
    @Override
    public boolean supports(ServerWebExchange exchange) {
        return this.matcher.match(exchange) != null;
    }

    /**
     * Initializes slug request metadata and continues the strategy chain.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            if (this.matcher.match(exchange) == null) {
                return Mono.error(new ValidateException(ErrorCode._100800));
            }
            final Context context = contextView.get(Context.class);
            ServerWebExchange prepared = prepare(exchange, context, false);
            context.getParameters().putAll(context.getQuery());
            Logger.debug(
                    true,
                    "Vortex",
                    "Public slug request parameters: clientIp={}, path={}, parameters={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters());
            return chain.apply(prepared);
        });
    }

}

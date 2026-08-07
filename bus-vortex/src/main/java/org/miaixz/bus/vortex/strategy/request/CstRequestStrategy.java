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

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.strategy.RequestStrategy;

import reactor.core.publisher.Mono;

/**
 * CST URL-based request parsing strategy.
 * <p>
 * CST currently uses the same request-parameter parsing behavior as REST while keeping a dedicated strategy type for
 * explicit CST chain composition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class CstRequestStrategy extends RequestStrategy {

    /**
     * Creates a CST request strategy.
     */
    public CstRequestStrategy() {
        super();
    }

    /**
     * Streams non-GET JSON bodies without consuming them because CST resolves the route from the URL.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return processing completion
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            Context context = contextView.get(Context.class);
            ServerWebExchange prepared = prepare(exchange, context, true);
            MediaType contentType = prepared.getRequest().getHeaders().getContentType();
            boolean rawJson = prepared.getRequest().getMethod() != HttpMethod.GET && contentType != null
                    && org.miaixz.bus.core.net.MediaType.isJson(contentType.toString());
            if (rawJson) {
                prepared.getAttributes().put(Context.RAW_JSON_PASSTHROUGH_ATTRIBUTE, Boolean.TRUE);
                return chain.apply(prepared);
            }
            return parse(prepared, chain, context);
        });
    }

}

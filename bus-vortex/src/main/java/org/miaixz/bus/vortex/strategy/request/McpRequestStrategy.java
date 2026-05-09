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

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.strategy.RequestStrategy;
import org.miaixz.bus.core.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Pass-through request strategy for MCP Streamable HTTP.
 * <p>
 * MCP request bodies are left untouched so JSON-RPC messages are consumed only by the MCP executor or the vetting
 * signature verifier.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class McpRequestStrategy extends RequestStrategy {

    /**
     * Creates an MCP request strategy.
     */
    public McpRequestStrategy() {
        super();
    }

    /**
     * Initializes metadata and continues without defaulting content type or parsing the body.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return pass-through completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            ServerWebExchange mutate = prepare(exchange, context, false);
            Logger.info(
                    true,
                    "Vortex",
                    "MCP request passed through without body parsing: strategy=mcp-request, clientIp={}, path={}",
                    context.getX_request_ip(),
                    mutate.getRequest().getURI().getPath());
            return chain.apply(mutate);
        });
    }

}

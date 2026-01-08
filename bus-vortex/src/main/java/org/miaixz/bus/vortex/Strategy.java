/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Defines a single, reusable step in the request processing pipeline, following the Chain of Responsibility pattern.
 * <p>
 * Each {@code Strategy} encapsulates a specific cross-cutting concern, such as request parsing, decryption,
 * authorization, or rate limiting. Implementations of this interface should be stateless and thread-safe, as they are
 * treated as singletons and reused for concurrent requests.
 * <p>
 * The state for a specific request is held in the {@link Context} object, which must be accessed from the Reactor
 * context. Implementations are composed into a dynamic chain by the
 * {@link org.miaixz.bus.vortex.strategy.StrategyFactory} and executed by the
 * {@link org.miaixz.bus.vortex.filter.PrimaryFilter}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Strategy {

    /**
     * Applies the strategy's logic to the given request and delegates to the next strategy in the chain.
     * <p>
     * An implementation of this method should perform its specific task. To access the request-specific state, it must
     * retrieve the {@link Context} object from the Reactor context, like so:
     *
     * <pre>{@code
     * return Mono.deferContextual(contextView -> {
     *     Context context = contextView.get(Context.class);
     *     // ... perform logic using the context ...
     *     return chain.apply(exchange);
     * });
     * }</pre>
     *
     * After completing its work, the strategy **must** call {@code chain.apply(exchange)} to delegate control to the
     * next strategy in the chain. Failure to do so will halt the request processing.
     *
     * @param exchange The current server exchange, which can be mutated by the strategy (e.g., by decorating the
     *                 request or response).
     * @param chain    The next link in the strategy chain, which must be invoked to continue processing.
     * @return A {@code Mono<Void>} that signals the completion of this strategy's execution.
     */
    Mono<Void> apply(ServerWebExchange exchange, Chain chain);

    /**
     * Represents the ongoing execution of the strategy chain, implementing the Chain of Responsibility pattern.
     * <p>
     * An instance of this interface is passed to each {@link Strategy#apply(ServerWebExchange, Chain)} method, allowing
     * a strategy to delegate control to the next strategy in the chain. The final link in the chain, delegates control
     * back to the main Spring WebFlux {@code WebFilterChain}.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    interface Chain {

        /**
         * Delegates control to the next strategy in the chain.
         * <p>
         * A {@link Strategy} must invoke this method to continue the processing of the request. Failure to do so will
         * effectively halt the request handling pipeline.
         *
         * @param exchange The current server exchange, which may have been mutated by the calling strategy.
         * @return A {@code Mono<Void>} that signals the completion of the rest of the chain.
         */
        Mono<Void> apply(ServerWebExchange exchange);

    }

}

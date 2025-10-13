/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.filter;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.strategy.Strategy;
import org.miaixz.bus.vortex.strategy.StrategyChain;
import org.miaixz.bus.vortex.strategy.StrategyFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The primary filter, acting as the main entry point and dispatcher for a dynamic chain of strategies.
 * <p>
 * This filter holds the highest precedence ({@link Ordered#HIGHEST_PRECEDENCE}) in the Spring WebFlux filter chain,
 * ensuring it intercepts all incoming requests first. Its sole responsibility is to dynamically organize and execute an
 * inner chain of responsibility composed of {@link Strategy} objects, without containing any business logic itself.
 * </p>
 * <p>
 * The core workflow is as follows:
 * <ol>
 * <li>Upon receiving a request, it immediately queries the {@link StrategyFactory}.</li>
 * <li>The factory returns a specific, ordered list of {@code Strategy} instances tailored to the current request
 * type.</li>
 * <li>This filter creates an inner {@link StrategyChain} with this dynamic list and executes it.</li>
 * <li>After the inner strategy chain completes successfully, control is passed back to the main {@link WebFilterChain},
 * allowing the request to proceed to subsequent handlers (like {@code VortexHandler}).</li>
 * </ol>
 * This design pattern provides extreme flexibility and maintainability, allowing different request types to undergo
 * varied validation and processing flows while keeping core components decoupled.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter extends AbstractFilter {

    /**
     * The strategy factory used to dynamically retrieve the appropriate strategy chain for the current request.
     */
    private final StrategyFactory factory;

    /**
     * Constructs a new {@code PrimaryFilter}.
     *
     * @param factory The strategy factory, which must not be {@code null}. It provides the appropriate chain of
     *                strategies for each request.
     */
    public PrimaryFilter(StrategyFactory factory) {
        Assert.notNull(factory, "FilterStrategyFactory must not be null");
        this.factory = factory;
    }

    /**
     * The standard {@code WebFilter} entry point.
     * <p>
     * This method serves as the starting point of the filter chain. It simply delegates the request, response, and the
     * context object (obtained via a helper method) to the core
     * {@link #doFilter(ServerWebExchange, WebFilterChain, Context)} method for processing.
     *
     * @param exchange The current server exchange, encapsulating the request and response.
     * @param chain    The main WebFlux filter chain.
     * @return A {@code Mono<Void>} that signals the completion of asynchronous processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.doFilter(exchange, chain, getContext(exchange));
    }

    /**
     * Implements the core dispatching logic.
     * <p>
     * This method is responsible for orchestrating the execution of the inner strategy chain. It does not perform any
     * filtering itself but delegates the work to the {@link StrategyChain} provided by the factory.
     *
     * @param exchange The current server exchange.
     * @param chain    The main WebFlux filter chain.
     * @param context  The context for the current request, prepared by a preceding filter (now created by getContext in
     *                 this filter).
     * @return A {@code Mono<Void>} that signals the completion of both the strategy chain and the main filter chain.
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        // 1. Get the specific list of strategies for the current request from the factory.
        List<Strategy> strategies = factory.getStrategiesFor(exchange);

        // 2. Create a new strategy chain for this request and execute it.
        // The original WebFilterChain is passed to the strategy chain to be called after all strategies are done.
        return new DefaultChain(strategies, chain, context).apply(exchange);
    }

    /**
     * The default implementation of {@link StrategyChain}, implemented as a private inner class of
     * {@code PrimaryFilter}.
     * <p>
     * It executes a list of strategies through recursive-like calls. Each step in the chain invokes the next, with the
     * final step delegating control back to the main Spring WebFlux {@link WebFilterChain}. This implements the Chain
     * of Responsibility design pattern.
     * </p>
     */
    private static class DefaultChain implements StrategyChain {

        /**
         * The current index position of the strategy in the list.
         */
        private final int index;
        /**
         * The complete list of strategies to be executed for the current request.
         */
        private final List<Strategy> list;
        /**
         * The original WebFlux filter chain, to be invoked after all strategies have been executed.
         */
        private final WebFilterChain chain;
        /**
         * The request context, shared across the entire strategy chain.
         */
        private final Context context;

        /**
         * The initial constructor for creating the first link in the strategy chain.
         *
         * @param list    The complete, ordered list of strategies to execute for the current request.
         * @param chain   The original WebFlux filter chain.
         * @param context The request context.
         */
        DefaultChain(List<Strategy> list, WebFilterChain chain, Context context) {
            this.list = list;
            this.chain = chain;
            this.context = context;
            this.index = 0;
        }

        /**
         * A private constructor used to advance to the next strategy in the chain.
         *
         * @param parent The instance of the previous link in the chain.
         * @param index  The new index, pointing to the next strategy to be executed.
         */
        private DefaultChain(DefaultChain parent, int index) {
            this.list = parent.list;
            this.chain = parent.chain;
            this.context = parent.context;
            this.index = index;
        }

        /**
         * Executes the current strategy in the chain or delegates to the main {@code WebFilterChain} if all strategies
         * are complete.
         *
         * @param exchange The current server exchange.
         * @return A {@code Mono<Void>} that signals the completion of asynchronous processing.
         */
        @Override
        public Mono<Void> apply(ServerWebExchange exchange) {
            if (this.index < this.list.size()) {
                Strategy strategy = this.list.get(this.index);
                // Create the next link in the chain and pass it to the current strategy.
                DefaultChain next = new DefaultChain(this, this.index + 1);
                return strategy.apply(exchange, next);
            }
            // If all strategies in the inner chain have been executed, invoke the original WebFlux filter chain
            // to proceed to the next WebFilter or, eventually, the VortexHandler.
            return this.chain.filter(exchange);
        }
    }

}

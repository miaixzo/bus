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

import java.util.List;

import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.strategy.StrategyChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * The private, inner implementation of {@link StrategyChain} used by {@link PrimaryFilter}.
 * <p>
 * This class implements the Chain of Responsibility pattern using a recursive-like delegation model. Each instance of
 * {@code PrimaryChain} represents one link in the chain, holding the complete list of strategies and the current
 * execution index.
 * <p>
 * When its {@link #apply} method is called, it executes the strategy at the current index and passes a <em>new</em>
 * {@code PrimaryChain} instance (with an incremented index) to it. This process continues until the end of the strategy
 * list is reached, at which point it delegates control back to the main Spring WebFlux {@link WebFilterChain}.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrimaryChain implements StrategyChain {

    /**
     * The current position in the strategy list that this chain link is responsible for executing.
     */
    private final int index;
    /**
     * The complete, ordered list of strategies to be executed for the current request.
     */
    private final List<Strategy> list;
    /**
     * The original WebFlux filter chain, to be invoked after all strategies in this primary chain have been executed.
     */
    private final WebFilterChain chain;

    /**
     * The initial constructor for creating the first link in the strategy chain.
     *
     * @param list  The complete, ordered list of strategies to execute for the current request.
     * @param chain The original WebFlux filter chain.
     */
    public PrimaryChain(List<Strategy> list, WebFilterChain chain) {
        this.list = list;
        this.chain = chain;
        this.index = 0;
    }

    /**
     * A private constructor used by a chain link to create the next link in the chain.
     *
     * @param parent The instance of the previous link in the chain.
     * @param index  The new index, pointing to the next strategy to be executed.
     */
    public PrimaryChain(PrimaryChain parent, int index) {
        this.list = parent.list;
        this.chain = parent.chain;
        this.index = index;
    }

    /**
     * Executes the current strategy in the chain or delegates to the main {@code WebFilterChain} if all strategies are
     * complete.
     *
     * @param exchange The current server exchange.
     * @return A {@code Mono<Void>} that signals the completion of asynchronous processing.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange) {
        if (this.index < this.list.size()) {
            Strategy strategy = this.list.get(this.index);
            // Create the next link in the chain and pass it to the current strategy.
            PrimaryChain next = new PrimaryChain(this, this.index + 1);
            return strategy.apply(exchange, next);
        }
        // If all strategies in the inner chain have been executed, invoke the original WebFlux filter chain
        // to proceed to the next WebFilter or, eventually, the VortexHandler.
        return this.chain.filter(exchange);
    }

}

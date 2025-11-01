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
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.strategy.StrategyFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The primary {@link WebFilter} that acts as the main entry point and orchestrator for the Vortex gateway.
 * <p>
 * Annotated with {@code @Order(Ordered.HIGHEST_PRECEDENCE)}, this filter intercepts all incoming requests before any
 * other Spring WebFilter. Its sole responsibility is to set up the request context and dispatch the request to a
 * dynamic, inner chain of responsibility composed of {@link Strategy} objects. It does not contain any business logic
 * itself.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter extends AbstractFilter {

    /**
     * Constructs a new {@code PrimaryFilter}.
     *
     * @param factory The strategy factory, which must not be {@code null}. It provides the appropriate chain of
     *                strategies for each request.
     */
    public PrimaryFilter(StrategyFactory factory) {
        Assert.notNull(factory, "StrategyFactory must not be null");
        this.factory = factory;
    }

    /**
     * Intercepts the incoming request to orchestrate the execution of the strategy chain.
     * <p>
     * This method performs four key steps:
     * <ol>
     * <li><b>Strategy Selection:</b> It queries the {@link StrategyFactory} to obtain the correct list of
     * {@link Strategy} instances for the current request.</li>
     * <li><b>Context Initialization:</b> It creates a new {@link Context} object and populates it with essential
     * initial data from the request, such as headers and the HTTP method.</li>
     * <li><b>Context Fallback Registration:</b> It stores the newly created {@code Context} in the
     * {@code ServerWebExchange} attributes. This serves as a "black box" fallback, ensuring the context is accessible
     * to the global {@link org.miaixz.bus.vortex.handler.ErrorsHandler} even if the reactive stream is disrupted by an
     * error.</li>
     * <li><b>Chain Execution:</b> It creates a new {@link Chain} and initiates its execution. Crucially, it also uses
     * {@code .contextWrite()} to inject the {@code Context} into the Reactor context, making it available to all
     * downstream reactive operators in a clean, functional way.</li>
     * </ol>
     *
     * @param exchange The current server exchange, provided by the WebFlux framework.
     * @param chain    The main WebFlux filter chain, to which control is eventually passed.
     * @return A {@code Mono<Void>} that signals the completion of the entire request processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. Get the request path. This method automatically excludes query parameters (?page=1).
        String path = exchange.getRequest().getPath().value();
        Logger.info("==>     Filter: Request to path: {} ", path);

        // 2. Check if the request path matches any known gateway prefixes (e.g., /router/rest, /router/cst).
        // This allows both exact matches (e.g., '/router/rest') and sub-paths (e.g., '/router/rest/v1/users').
        if (!Args.isKnownRequest(path)) {
            Logger.warn("==>     Filter: Blocked request to unknown path: {}", path);
            throw new ValidateException(ErrorCode._BLOCKED);
        }

        // 3. Check for path traversal attack patterns.
        if (isPathTraversalAttempt(path)) {
            Logger.warn("==>     Filter: Path traversal attempt detected: {}", path);
            throw new ValidateException(ErrorCode._LIMITER);
        }

        // 4. Get the specific list of strategies for the current request from the factory.
        List<Strategy> strategies = factory.getStrategiesFor(exchange);

        // 5. Create and initialize the context with essential request information.
        Context context = new Context();
        context.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
        context.setHttpMethod(exchange.getRequest().getMethod());

        // 6. Store the context in the exchange attributes for fallback access (e.g., in error handlers).
        exchange.getAttributes().put(Context.$, context);

        // 7. Create a new strategy chain for this request and execute it.
        // The context is written to the Reactor context for all downstream strategies and handlers.
        return new Chain(strategies, chain).apply(exchange).contextWrite(ctx -> ctx.put(Context.class, context));
    }

    /**
     * The private, inner implementation of {@link Strategy.Chain} used by {@link PrimaryFilter}.
     * <p>
     * This class implements the Chain of Responsibility pattern using a recursive-like delegation model. Each instance
     * of {@code PrimaryChain} represents one link in the chain, holding the complete list of strategies and the current
     * execution index.
     * <p>
     * When its {@link #apply} method is called, it executes the strategy at the current index and passes a <em>new</em>
     * {@code PrimaryChain} instance (with an incremented index) to it. This process continues until the end of the
     * strategy list is reached, at which point it delegates control back to the main Spring WebFlux
     * {@link WebFilterChain}.
     */
    public class Chain implements Strategy.Chain {

        /**
         * The current position in the strategy list that this chain link is responsible for executing.
         */
        private final int index;
        /**
         * The complete, ordered list of strategies to be executed for the current request.
         */
        private final List<Strategy> list;
        /**
         * The original WebFlux filter chain, to be invoked after all strategies in this primary chain have been
         * executed.
         */
        private final WebFilterChain chain;

        /**
         * The initial constructor for creating the first link in the strategy chain.
         *
         * @param list  The complete, ordered list of strategies to execute for the current request.
         * @param chain The original WebFlux filter chain.
         */
        Chain(List<Strategy> list, WebFilterChain chain) {
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
        private Chain(Chain parent, int index) {
            this.list = parent.list;
            this.chain = parent.chain;
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
                Chain next = new Chain(this, this.index + 1);
                return strategy.apply(exchange, next);
            }
            // If all strategies in the inner chain have been executed, invoke the original WebFlux filter chain
            // to proceed to the next WebFilter or, eventually, the VortexHandler.
            return this.chain.filter(exchange);
        }
    }

    /**
     * Checks if the given URL path contains patterns indicative of a path traversal attack.
     *
     * @param path The URL path string to check.
     * @return {@code true} if a potential traversal attempt is detected, {@code false} otherwise.
     */
    private boolean isPathTraversalAttempt(String path) {
        // Check for various characteristics of path traversal attacks, including plain text and URL-encoded forms.
        return path.contains("../") || path.contains("..\\") || path.contains("%2e%2e%2f") || path.contains("%2e%2e\\")
                || path.contains("..%2f") || path.contains("..%5c");
    }

}

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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.strategy.StrategyFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

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
     * The factory used to dynamically select the appropriate strategy chain for the current request.
     */
    private final StrategyFactory factory;

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
     * <li><b>Chain Execution:</b> It creates a new {@link PrimaryChain} and initiates its execution. Crucially, it also
     * uses {@code .contextWrite()} to inject the {@code Context} into the Reactor context, making it available to all
     * downstream reactive operators in a clean, functional way.</li>
     * </ol>
     *
     * @param exchange The current server exchange, provided by the WebFlux framework.
     * @param chain    The main WebFlux filter chain, to which control is eventually passed.
     * @return A {@code Mono<Void>} that signals the completion of the entire request processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. Get the specific list of strategies for the current request from the factory.
        List<Strategy> strategies = factory.getStrategiesFor(exchange);

        // 2. Create and initialize the context with essential request information.
        Context context = new Context();
        context.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
        context.setHttpMethod(exchange.getRequest().getMethod());

        // 3. Store the context in the exchange attributes for fallback access (e.g., in error handlers).
        exchange.getAttributes().put(Context.$, context);

        // 4. Create a new strategy chain for this request and execute it.
        // The context is written to the Reactor context for all downstream strategies and handlers.
        return new PrimaryChain(strategies, chain).apply(exchange).contextWrite(ctx -> ctx.put(Context.class, context));
    }

}

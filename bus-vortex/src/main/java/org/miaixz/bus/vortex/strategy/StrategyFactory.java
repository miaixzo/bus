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
package org.miaixz.bus.vortex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Strategy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * A factory that provides the correct, ordered chain of {@link Strategy} instances for a given request.
 * <p>
 * This class acts as a singleton service. Upon construction, it receives all {@code Strategy} beans from the Spring
 * context. It then pre-calculates and caches different strategy chains (e.g., for REST, MCP) based on their
 * applicability. This pre-calculation ensures that the selection of strategies for each request is highly efficient,
 * avoiding repeated computations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrategyFactory {

    /**
     * The default, complete list of all strategies, sorted by order.
     */
    private final List<Strategy> strategies;

    /**
     * Constructs a new {@code StrategyFactory} and pre-calculates the strategy chains.
     *
     * @param strategies A list of all available {@link Strategy} beans, injected by the Spring container.
     */
    public StrategyFactory(List<Strategy> strategies) {
        // Sort all strategies by their @Order annotation to establish a definitive execution order.
        strategies.sort(AnnotationAwareOrderComparator.INSTANCE);
        this.strategies = strategies;
    }

    /**
     * Checks if the given request is an MCP (Miaixz Communication Protocol) proxy request based on its path.
     *
     * @param request The incoming server request.
     * @return {@code true} if the request path starts with the MCP prefix, {@code false} otherwise.
     */
    public static boolean isRestRequest(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith(Args.REST_PATH_PREFIX);
    }

    /**
     * Checks if the given request is an MCP (Miaixz Communication Protocol) proxy request based on its path.
     *
     * @param request The incoming server request.
     * @return {@code true} if the request path starts with the MCP prefix, {@code false} otherwise.
     */
    public static boolean isMcpRequest(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith(Args.MCP_PATH_PREFIX);
    }

    /**
     * Checks if the given request is an MCP (Miaixz Communication Protocol) proxy request based on its path.
     *
     * @param request The incoming server request.
     * @return {@code true} if the request path starts with the MCP prefix, {@code false} otherwise.
     */
    public static boolean isMqRequest(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith(Args.MQ_PATH_PREFIX);
    }

    /**
     * Returns the appropriate, pre-calculated list of strategies for the given request.
     * <p>
     * This method efficiently selects a strategy chain by checking the request's path. It does not perform any
     * real-time computation, instead returning a cached list.
     *
     * @param exchange The current server exchange.
     * @return An ordered, unmodifiable list of {@link Strategy} instances to be executed.
     */
    public List<Strategy> getStrategiesFor(ServerWebExchange exchange) {
        // 1. MCP requests are identified by a unique path prefix and have a minimal, specialized chain.
        if (isMcpRequest(exchange.getRequest())) {
            return this.strategies.stream().filter(this::isApplicableToMcp).collect(Collectors.toUnmodifiableList());
        }

        // 2. Currently, MQ requests are not distinguished by path and fall through to the default.
        if (isMqRequest(exchange.getRequest())) {
            return this.strategies.stream().filter(this::isApplicableToMq).collect(Collectors.toUnmodifiableList());
        }

        // 3. For all other requests (e.g., REST), apply the full, default strategy chain.
        return this.strategies;
    }

    /**
     * Determines if a strategy is applicable to MCP requests.
     * <p>
     * MCP requests are typically simple, stateless proxies and should bypass most business-logic-heavy strategies to
     * remain lightweight and fast.
     *
     * @param strategy The strategy to check.
     * @return {@code false} if the strategy is one of the business-logic strategies to be skipped for MCP, {@code true}
     *         otherwise.
     */
    public boolean isApplicableToMcp(Strategy strategy) {
        // MCP requests are simple proxies; they don't need complex validation like authorization or licensing.
        return !(strategy instanceof AuthorizeStrategy || strategy instanceof CipherStrategy
                || strategy instanceof LimitStrategy);
    }

    /**
     * Determines if a strategy is applicable to MQ-based requests.
     * <p>
     * This method provides a hook to define a custom strategy chain for requests that will be routed to a message
     * queue. This allows for different processing logic, such as a different authorization mechanism.
     *
     * @param strategy The strategy to check.
     * @return {@code true} if the strategy should be part of the MQ chain, {@code false} otherwise.
     */
    public boolean isApplicableToMq(Strategy strategy) {
        // Example: MQ requests might have their own authorization logic but share others.
        // For now, we assume all strategies apply to MQ, but this can be customized.
        return true;
    }

}

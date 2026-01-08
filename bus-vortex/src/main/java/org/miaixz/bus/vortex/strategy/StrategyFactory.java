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
package org.miaixz.bus.vortex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Strategy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
     * The default, complete list of all strategies, sorted by order. (e.g., for REST)
     */
    private final List<Strategy> chain;

    /**
     * A specialized chain for MQ (message queue) requests.
     */
    private final List<Strategy> grpcChain;

    /**
     * A specialized, minimal chain for CST (Url-based) requests.
     */
    private final List<Strategy> cstChain;

    /**
     * A specialized, minimal chain for MCP (proxy) requests.
     */
    private final List<Strategy> mcpChain;

    /**
     * A specialized chain for MQ (message queue) requests.
     */
    private final List<Strategy> mqChain;

    /**
     * A specialized chain for WS (websocket) requests.
     */
    private final List<Strategy> wsChain;

    /**
     * Constructs a new {@code StrategyFactory} and pre-calculates the strategy chains.
     *
     * @param chain A list of all available {@link Strategy} beans, injected by the Spring container.
     */
    public StrategyFactory(List<Strategy> chain) {
        Logger.info(true, "Chain", "Initializing StrategyFactory...");
        // Sort all strategies by their @Order annotation to establish a definitive execution order.
        chain.sort(AnnotationAwareOrderComparator.INSTANCE);
        Logger.info(true, "Chain", "Found {} total strategies, sorting and caching chains...", chain.size());

        // Pre-calculate and cache all strategy chains.
        this.chain = List.copyOf(chain);
        Logger.info(
                true,
                "Chain",
                "Default Chain ({} strategies): {}",
                this.chain.size(),
                getStrategyNames(this.chain));

        this.grpcChain = chain.stream().filter(this::isApplicableToGrpc).collect(Collectors.toUnmodifiableList());
        Logger.info(
                true,
                "Chain",
                "gRPC Chain     ({} strategies): {}",
                this.grpcChain.size(),
                getStrategyNames(this.grpcChain));

        this.cstChain = chain.stream().filter(this::isApplicableToCst).collect(Collectors.toUnmodifiableList());
        Logger.info(
                true,
                "Chain",
                "CST Chain     ({} strategies): {}",
                this.cstChain.size(),
                getStrategyNames(this.cstChain));

        this.mcpChain = chain.stream().filter(this::isApplicableToMcp).collect(Collectors.toUnmodifiableList());
        Logger.info(
                true,
                "Chain",
                "MCP Chain     ({} strategies): {}",
                this.mcpChain.size(),
                getStrategyNames(this.mcpChain));

        this.mqChain = chain.stream().filter(this::isApplicableToMq).collect(Collectors.toUnmodifiableList());
        Logger.info(
                true,
                "Chain",
                "MQ Chain      ({} strategies): {}",
                this.mqChain.size(),
                getStrategyNames(this.mqChain));

        this.wsChain = chain.stream().filter(this::isApplicableToMq).collect(Collectors.toUnmodifiableList());
        Logger.info(
                true,
                "Chain",
                "WS Chain      ({} strategies): {}",
                this.wsChain.size(),
                getStrategyNames(this.wsChain));

        Logger.info(true, "Chain", "StrategyFactory initialization complete.");
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
        String path = exchange.getRequest().getPath().value();

        // This method is called before creating Context and IP, therefore using [N/A]
        final String ipTag = "[N/A]";

        if (Logger.isDebugEnabled()) {
            Logger.debug(true, "Chain", "{} Selecting strategy chain for path: {}", ipTag, path);
        }

        // 1. Url requests are identified by a unique path segment.
        if (Args.isCstRequest(path)) {
            Logger.debug(
                    true,
                    "Chain",
                    "{} Path matched CST. Selected CST Chain ({} strategies).",
                    ipTag,
                    this.cstChain.size());
            return this.cstChain;
        }

        // 2. MCP requests are identified by a unique path prefix.
        if (Args.isMcpRequest(path)) {
            Logger.debug(
                    true,
                    "Chain",
                    "{} Path matched MCP. Selected MCP Chain ({} strategies).",
                    ipTag,
                    this.mcpChain.size());
            return this.mcpChain;
        }

        // 3. MQ requests.
        if (Args.isMqRequest(path)) {
            Logger.debug(
                    true,
                    "Chain",
                    "{} Path matched MQ. Selected MQ Chain ({} strategies).",
                    ipTag,
                    this.mqChain.size());
            return this.mqChain;
        }

        // 3. grcp requests.
        if (Args.isGrpcRequest(path)) {
            Logger.debug(
                    true,
                    "Chain",
                    "{} Path matched gRPC. Selected gRPC Chain ({} strategies).",
                    ipTag,
                    this.grpcChain.size());
            return this.grpcChain;
        }

        // 4. grcp requests.
        if (Args.isWsRequest(path)) {
            Logger.debug(
                    true,
                    "Chain",
                    "{} Path matched WS. Selected WS Chain ({} strategies).",
                    ipTag,
                    this.wsChain.size());
            return this.wsChain;
        }

        // 5. For all other requests (e.g., REST), apply the full, default strategy chain.
        Logger.debug(
                true,
                "Chain",
                "{} Path matched no specific profile. Selected Default Chain ({} strategies).",
                ipTag,
                this.chain.size());
        return this.chain;
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
        return !(strategy instanceof QualifierStrategy || strategy instanceof LimiterStrategy);
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
        // This logic is identical to default, but is kept separate for future customization.
        return true;
    }

    /**
     * Determines if a strategy is applicable to CST (Url-based) requests.
     * <p>
     * This method provides a hook to define a custom strategy chain for simple URL-based requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} if the strategy should be part of the CST chain, {@code false} otherwise.
     */
    public boolean isApplicableToGrpc(Strategy strategy) {
        // For now, we assume all strategies apply to CST.
        // This logic is identical to default, but is kept separate for future customization.
        return true;
    }

    /**
     * Determines if a strategy is applicable to CST (Url-based) requests.
     * <p>
     * This method provides a hook to define a custom strategy chain for simple URL-based requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} if the strategy should be part of the CST chain, {@code false} otherwise.
     */
    public boolean isApplicableToCst(Strategy strategy) {
        // For now, we assume all strategies apply to CST.
        // This logic is identical to default, but is kept separate for future customization.
        return true;
    }

    /**
     * Helper method to get a clean list of class names for logging.
     *
     * @param strategies The list of strategies.
     * @return A comma-separated string of simple class names.
     */
    private String getStrategyNames(List<Strategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            return "[]";
        }
        return strategies.stream().map(s -> s.getClass().getSimpleName()).collect(Collectors.joining(", "));
    }

}

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
 * @since Java 21+
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
     * A specialized, minimal chain for LLM (Large Language Model) requests.
     */
    private final List<Strategy> llmChain;

    /**
     * Constructs a new {@code StrategyFactory} and pre-calculates the strategy chains.
     *
     * @param chain A list of all available {@link Strategy} beans, injected by the Spring container.
     */
    public StrategyFactory(List<Strategy> chain) {
        Logger.info(true, "Vortex", "Strategy chain initialization started: discoveredStrategies={}", chain.size());
        chain.sort(AnnotationAwareOrderComparator.INSTANCE);
        Logger.debug(true, "Vortex", "Strategy beans sorted by order: strategies={}", getStrategyNames(chain));

        this.chain = List.copyOf(chain);
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=default, strategyCount={}, strategies={}",
                this.chain.size(),
                getStrategyNames(this.chain));

        this.grpcChain = chain.stream().filter(this::isApplicableToGrpc).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=grpc, strategyCount={}, strategies={}",
                this.grpcChain.size(),
                getStrategyNames(this.grpcChain));

        this.cstChain = chain.stream().filter(this::isApplicableToCst).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=cst, strategyCount={}, strategies={}",
                this.cstChain.size(),
                getStrategyNames(this.cstChain));

        this.mcpChain = chain.stream().filter(this::isApplicableToMcp).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=mcp, strategyCount={}, strategies={}",
                this.mcpChain.size(),
                getStrategyNames(this.mcpChain));

        this.mqChain = chain.stream().filter(this::isApplicableToMq).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=mq, strategyCount={}, strategies={}",
                this.mqChain.size(),
                getStrategyNames(this.mqChain));

        this.wsChain = chain.stream().filter(this::isApplicableToMq).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=ws, strategyCount={}, strategies={}",
                this.wsChain.size(),
                getStrategyNames(this.wsChain));

        this.llmChain = chain.stream().filter(this::isApplicableToLlm).collect(Collectors.toUnmodifiableList());
        Logger.info(
                false,
                "Vortex",
                "Strategy chain built: route=llm, strategyCount={}, strategies={}",
                this.llmChain.size(),
                getStrategyNames(this.llmChain));

        Logger.info(
                false,
                "Vortex",
                "Strategy chain initialization completed: default={}, mcp={}, mq={}, grpc={}, ws={}, llm={}",
                this.chain.size(),
                this.mcpChain.size(),
                this.mqChain.size(),
                this.grpcChain.size(),
                this.wsChain.size(),
                this.llmChain.size());
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

        final String ipTag = "N/A";

        if (Logger.isDebugEnabled()) {
            Logger.debug(true, "Vortex", "Strategy chain selection started: clientIp={}, path={}", ipTag, path);
        }

        if (Args.isCstRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched CST chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.cstChain.size());
            return this.cstChain;
        }

        if (Args.isMcpRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched MCP chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.mcpChain.size());
            return this.mcpChain;
        }

        if (Args.isMqRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched MQ chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.mqChain.size());
            return this.mqChain;
        }

        if (Args.isGrpcRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched gRPC chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.grpcChain.size());
            return this.grpcChain;
        }

        if (Args.isWsRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched WebSocket chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.wsChain.size());
            return this.wsChain;
        }

        if (Args.isLlmRequest(path)) {
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched LLM chain: clientIp={}, strategyCount={}",
                    ipTag,
                    this.llmChain.size());
            return this.llmChain;
        }

        Logger.debug(
                false,
                "Vortex",
                "Path matched default chain: clientIp={}, strategyCount={}",
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
        return true;
    }

    /**
     * Determines if a strategy is applicable to LLM requests.
     * <p>
     * LLM requests are simple proxies similar to MCP; they don't need complex validation like method/version
     * qualification or rate limiting. Authentication is handled at the router level via project API keys.
     *
     * @param strategy The strategy to check.
     * @return {@code false} if the strategy is one of the business-logic strategies to be skipped for LLM, {@code true}
     *         otherwise.
     */
    public boolean isApplicableToLlm(Strategy strategy) {
        return !(strategy instanceof QualifierStrategy || strategy instanceof LimiterStrategy);
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

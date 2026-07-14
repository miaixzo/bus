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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.strategy.qualifier.CstQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.McpQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.RestQualifierStrategy;
import org.miaixz.bus.vortex.strategy.request.CstRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.McpRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.RestRequestStrategy;
import org.miaixz.bus.vortex.strategy.vetting.CstVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.McpVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.RestVettingStrategy;

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
     * Route specifications in selection order.
     */
    private final List<ChainSpec> chainSpecs;

    /**
     * Pre-built ordered strategy chains keyed by route name.
     */
    private final Map<String, List<Strategy>> chains;

    /**
     * Pre-built dynamic strategy chains keyed by protocol.
     */
    private final Map<Integer, List<Strategy>> dynamicChains;

    /**
     * Constructs a new {@code StrategyFactory} and pre-calculates the strategy chains.
     *
     * @param chain A list of all available {@link Strategy} beans, injected by the Spring container.
     */
    public StrategyFactory(List<Strategy> chain) {
        Logger.info(true, "Vortex", "Strategy chain initialization started: discoveredStrategies={}", chain.size());
        chain.sort(AnnotationAwareOrderComparator.INSTANCE);
        Logger.debug(true, "Vortex", "Strategy beans sorted by order: strategies={}", getStrategyNames(chain));

        this.chainSpecs = List.of(
                new ChainSpec(Args.REST_PATH_PREFIX, Args::isRestRequest, this::isRestStrategy),
                new ChainSpec(Args.CST_PATH_PREFIX, Args::isCstRequest, this::isCstStrategy),
                new ChainSpec(Args.MCP_PATH_PREFIX, Args::isMcpRequest, this::isMcpStrategy),
                new ChainSpec(Args.MQ_PATH_PREFIX, Args::isMqRequest, this::isMqStrategy),
                new ChainSpec(Args.GRPC_PATH_PREFIX, Args::isGrpcRequest, this::isGrpcStrategy),
                new ChainSpec(Args.WS_PATH_PREFIX, Args::isWsRequest, this::isWsStrategy),
                new ChainSpec(Args.LLM_PATH_PREFIX, Args::isLlmRequest, this::isLlmStrategy));

        Map<String, List<Strategy>> builtChains = new LinkedHashMap<>();
        for (ChainSpec spec : this.chainSpecs) {
            List<Strategy> routeChain = buildChain(chain, spec.strategyFilter());
            builtChains.put(spec.route(), routeChain);
            Logger.info(
                    false,
                    "Vortex",
                    "Strategy chain built: route={}, strategyCount={}, strategies={}",
                    spec.route(),
                    routeChain.size(),
                    getStrategyNames(routeChain));
        }
        this.chains = Map.copyOf(builtChains);
        this.dynamicChains = buildDynamicChains(chain);

        StringBuilder routeSummary = new StringBuilder();
        for (ChainSpec spec : this.chainSpecs) {
            if (routeSummary.length() > 0) {
                routeSummary.append(Symbol.COMMA).append(Symbol.SPACE);
            }
            routeSummary.append(spec.route()).append(Symbol.EQUAL).append(this.chains.get(spec.route()).size());
        }
        for (Map.Entry<Integer, List<Strategy>> entry : this.dynamicChains.entrySet()) {
            if (routeSummary.length() > 0) {
                routeSummary.append(Symbol.COMMA).append(Symbol.SPACE);
            }
            routeSummary.append("protocol-").append(entry.getKey()).append(Symbol.EQUAL)
                    .append(entry.getValue().size());
        }
        Logger.info(false, "Vortex", "Strategy chain initialization completed: routes={}", routeSummary);
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

        for (ChainSpec spec : this.chainSpecs) {
            if (spec.pathMatcher().test(path)) {
                List<Strategy> routeChain = this.chains.get(spec.route());
                Logger.debug(
                        false,
                        "Vortex",
                        "Path matched strategy chain: clientIp={}, route={}, strategyCount={}",
                        ipTag,
                        spec.route(),
                        routeChain.size());
                return routeChain;
            }
        }

        List<Strategy> routeChain = dynamicChain(exchange);
        if (!routeChain.isEmpty()) {
            return routeChain;
        }

        Logger.debug(false, "Vortex", "No strategy chain matched: clientIp={}, path={}", ipTag, path);
        return List.of();
    }

    /**
     * Determines if a strategy is applicable to REST/API requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} when the strategy belongs to the REST/default chain.
     */
    public boolean isRestStrategy(Strategy strategy) {
        return strategy.getClass() == RestRequestStrategy.class || strategy instanceof RestQualifierStrategy
                || strategy instanceof RestVettingStrategy || strategy instanceof LimiterStrategy
                || strategy instanceof ResponseStrategy;
    }

    /**
     * Determines if a strategy is applicable to CST URL-based requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} when the strategy belongs to the CST chain.
     */
    public boolean isCstStrategy(Strategy strategy) {
        return strategy.getClass() == CstRequestStrategy.class || strategy instanceof CstQualifierStrategy
                || strategy instanceof CstVettingStrategy || strategy instanceof LimiterStrategy
                || strategy instanceof ResponseStrategy;
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
    public boolean isMcpStrategy(Strategy strategy) {
        return strategy.getClass() == McpRequestStrategy.class || strategy instanceof McpQualifierStrategy
                || strategy instanceof McpVettingStrategy || strategy instanceof ResponseStrategy;
    }

    /**
     * Determines if a strategy is applicable to MQ requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} when the strategy belongs to the MQ chain.
     */
    public boolean isMqStrategy(Strategy strategy) {
        return strategy.getClass() == RequestStrategy.class || strategy.getClass() == QualifierStrategy.class
                || strategy.getClass() == VettingStrategy.class || strategy instanceof LimiterStrategy
                || strategy instanceof ResponseStrategy;
    }

    /**
     * Determines if a strategy is applicable to gRPC requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} when the strategy belongs to the gRPC chain.
     */
    public boolean isGrpcStrategy(Strategy strategy) {
        return strategy.getClass() == RequestStrategy.class || strategy.getClass() == QualifierStrategy.class
                || strategy.getClass() == VettingStrategy.class || strategy instanceof LimiterStrategy
                || strategy instanceof ResponseStrategy;
    }

    /**
     * Determines if a strategy is applicable to WebSocket requests.
     *
     * @param strategy The strategy to check.
     * @return {@code true} when the strategy belongs to the WebSocket chain.
     */
    public boolean isWsStrategy(Strategy strategy) {
        return strategy.getClass() == RequestStrategy.class || strategy.getClass() == QualifierStrategy.class
                || strategy.getClass() == VettingStrategy.class || strategy instanceof LimiterStrategy
                || strategy instanceof ResponseStrategy;
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
    public boolean isLlmStrategy(Strategy strategy) {
        return strategy.getClass() == RequestStrategy.class || strategy instanceof ResponseStrategy;
    }

    /**
     * Builds one immutable ordered chain from the sorted strategy list.
     *
     * @param strategies sorted strategy list
     * @param filter     route-specific strategy filter
     * @return immutable route chain
     */
    private List<Strategy> buildChain(List<Strategy> strategies, Predicate<Strategy> filter) {
        return strategies.stream().filter(filter).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Builds dynamic chains from strategies that declare one protocol.
     *
     * @param strategies sorted strategy list
     * @return immutable dynamic chains keyed by protocol
     */
    private Map<Integer, List<Strategy>> buildDynamicChains(List<Strategy> strategies) {
        Map<Integer, List<Strategy>> builtChains = new LinkedHashMap<>();
        for (Strategy strategy : strategies) {
            Integer protocol = strategy.protocol();
            if (protocol == null) {
                continue;
            }
            builtChains.computeIfAbsent(protocol, key -> new ArrayList<>()).add(strategy);
        }

        List<Strategy> shared = strategies.stream().filter(this::isDynamicSharedStrategy).toList();
        Map<Integer, List<Strategy>> immutableChains = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Strategy>> entry : builtChains.entrySet()) {
            List<Strategy> routeChain = new ArrayList<>(entry.getValue());
            for (Strategy strategy : shared) {
                if (!routeChain.contains(strategy)) {
                    routeChain.add(strategy);
                }
            }
            immutableChains.put(entry.getKey(), List.copyOf(routeChain));
            Logger.info(
                    false,
                    "Vortex",
                    "Strategy chain built: route=protocol-{}, strategyCount={}, strategies={}",
                    entry.getKey(),
                    routeChain.size(),
                    getStrategyNames(routeChain));
        }
        return Map.copyOf(immutableChains);
    }

    /**
     * Returns whether a strategy is shared by dynamic protocol chains.
     *
     * @param strategy candidate strategy
     * @return {@code true} when the strategy can be appended to dynamic chains
     */
    private boolean isDynamicSharedStrategy(Strategy strategy) {
        return strategy instanceof LimiterStrategy || strategy instanceof ResponseStrategy;
    }

    /**
     * Resolves one dynamic strategy chain for the current request.
     *
     * @param exchange current server exchange
     * @return matched chain, or an empty list
     */
    private List<Strategy> dynamicChain(ServerWebExchange exchange) {
        final String ipTag = "N/A";
        for (Map.Entry<Integer, List<Strategy>> entry : this.dynamicChains.entrySet()) {
            List<Strategy> routeChain = entry.getValue();
            boolean matched = routeChain.stream().filter(strategy -> !isDynamicSharedStrategy(strategy))
                    .anyMatch(strategy -> strategy.supports(exchange));
            if (!matched) {
                continue;
            }
            Logger.debug(
                    false,
                    "Vortex",
                    "Path matched strategy chain: clientIp={}, route=protocol-{}, strategyCount={}",
                    ipTag,
                    entry.getKey(),
                    routeChain.size());
            return routeChain;
        }
        return List.of();
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
        return strategies.stream().map(s -> s.getClass().getSimpleName())
                .collect(Collectors.joining(Symbol.COMMA + Symbol.SPACE));
    }

    /**
     * Defines how one route selects requests and filters strategies.
     *
     * @param route          route key used in logs and the chain map
     * @param pathMatcher    request path matcher
     * @param strategyFilter strategy filter for the route chain
     * @author Kimi Liu
     * @since Java 21+
     */
    private record ChainSpec(String route, Predicate<String> pathMatcher, Predicate<Strategy> strategyFilter) {

    }

}

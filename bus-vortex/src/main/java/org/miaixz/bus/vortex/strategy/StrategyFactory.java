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

import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * A factory responsible for providing the correct chain of {@link Strategy} instances based on the type of the incoming
 * request.
 * <p>
 * It holds all available strategies and composes a specific, ordered list of strategies for traditional REST, modern
 * MCP proxy, and MQ requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrategyFactory {

    private static final String MCP_PATH_PREFIX = "/router/mcp";

    private final List<Strategy> allStrategies;
    private final List<Strategy> mcpStrategies;
    private final List<Strategy> mqStrategies;

    /**
     * Constructs a new factory.
     *
     * @param strategies A list of all available {@code FilterStrategy} beans, injected by Spring.
     */
    public StrategyFactory(List<Strategy> strategies) {
        // Sort all strategies by their @Order annotation and store them.
        strategies.sort(AnnotationAwareOrderComparator.INSTANCE);
        this.allStrategies = strategies;

        // Pre-calculate the specific, smaller chain for MCP requests.
        this.mcpStrategies = this.allStrategies.stream().filter(this::isApplicableToMcp).collect(Collectors.toList());

        // Pre-calculate the specific, smaller chain for MQ requests.
        this.mqStrategies = this.allStrategies.stream().filter(this::isApplicableToMq).collect(Collectors.toList());
    }

    /**
     * Checks if the given request is an MCP proxy request.
     *
     * @param request The incoming server request.
     * @return {@code true} if the request path starts with the MCP prefix.
     */
    public static boolean isMcpRequest(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith(MCP_PATH_PREFIX);
    }

    /**
     * Returns the appropriate, ordered list of strategies for the given request.
     *
     * @param exchange The current server exchange.
     * @return A list of {@code FilterStrategy} to be executed.
     */
    public List<Strategy> getStrategiesFor(ServerWebExchange exchange) {
        // 1. MCP requests are identified by a unique path prefix.
        if (isMcpRequest(exchange.getRequest())) {
            return this.mcpStrategies;
        }

        // 2. For other requests (REST and MQ), they are distinguished by the 'mode' in Assets.
        // The Context object, which contains Assets, must have been prepared by ContextStrategy.
        Context context = Context.get(exchange);
        if (context == null || context.getAssets() == null) {
            // This can happen for requests that don't match any API, or before ContextStrategy has fully run.
            // Applying all strategies is a safe default for non-MCP paths.
            return this.allStrategies;
        }

        Assets assets = context.getAssets();
        int mode = assets.getMode();

        // 3. Dispatch based on mode.
        if (mode == 2) { // MQ mode
            return this.mqStrategies;
        } else { // Default to REST mode (mode 1 or others)
            return this.allStrategies;
        }
    }

    /**
     * Determines if a strategy should be applied to MCP requests. MCP requests are stateless proxies and bypass most
     * business logic filters.
     */
    private boolean isApplicableToMcp(Strategy strategy) {
        // MCP requests are simple proxies, they don't need complex validation like authorization or licensing.
        // They might still need foundational strategies like Context preparation and possibly decryption.
        return !(strategy instanceof AuthorizeStrategy || strategy instanceof LicenseStrategy
                || strategy instanceof LimitStrategy);
    }

    /**
     * Determines if a strategy should be applied to MQ requests. This provides flexibility to have a different filter
     * chain for MQ-based interactions.
     */
    private boolean isApplicableToMq(Strategy strategy) {
        // Example: MQ requests might have their own authorization logic but share others.
        // For now, we assume it's similar to REST, but this can be customized.
        return true; // Assume all strategies apply to MQ for now.
    }

}

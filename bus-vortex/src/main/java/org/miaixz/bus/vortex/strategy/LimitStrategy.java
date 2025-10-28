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

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.Limiter;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A strategy that applies rate limiting to incoming requests.
 * <p>
 * This strategy collaborates with a {@link LimiterRegistry} to find and apply one or more {@link Limiter} instances
 * (which typically use a token bucket algorithm) to the current request. It supports layered rate limiting by checking
 * for both global API limits and more specific per-IP limits.
 * <p>
 * It is ordered to run late in the chain, after authentication and authorization, to ensure that system resources are
 * spent on validating legitimate, authenticated traffic.
 *
 * @author Kimi Liu
 * @see Limiter
 * @see LimiterRegistry
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class LimitStrategy extends AbstractStrategy {

    /**
     * The registry that holds all configured rate limiter instances.
     */
    private final LimiterRegistry registry;

    /**
     * Constructs a new {@code LimitStrategy}.
     *
     * @param registry The registry containing all available {@link Limiter}s.
     */
    public LimitStrategy(LimiterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Applies the rate limiting logic to the current request.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Retrieves the {@link Context} from the reactive stream.</li>
     * <li>Asserts that the context and its nested {@code Assets} have been populated by preceding strategies.</li>
     * <li>Extracts the API method, version, and client IP address from the context.</li>
     * <li>Calls {@link #getLimiter} to find all applicable limiters (e.g., global and per-IP).</li>
     * <li>For each applicable limiter, it calls the {@link Limiter#acquire()} method. If a limiter has no available
     * tokens, this call will throw an exception, which is caught by the global error handler to produce a "Too Many
     * Requests" response.</li>
     * <li>If all limiters are successfully acquired, it proceeds to the next strategy in the chain.</li>
     * </ol>
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Assert.notNull(context, "Context must be initialized by a preceding strategy.");
            Assert.notNull(context.getAssets(), "Assets must be resolved by a preceding strategy.");

            String methodVersion = context.getAssets().getMethod() + context.getAssets().getVersion();
            String clientIp = context.getX_request_ipv4(); // Populated by RequestStrategy

            Set<Limiter> limiters = getLimiter(methodVersion, clientIp);
            for (Limiter limiter : limiters) {
                limiter.acquire();
            }

            if (!limiters.isEmpty()) {
                Logger.info(
                        "==>     Strategy: Rate limit applied - Path: {}, Method: {}",
                        exchange.getRequest().getURI().getPath(),
                        methodVersion);
            }

            return chain.apply(exchange);
        });
    }

    /**
     * Finds all applicable limiters for a given request based on a layered key structure.
     * <p>
     * This method implements a two-layer lookup:
     * <ol>
     * <li><b>Global API Limit:</b> It first looks for a limiter keyed by the method and version (e.g.,
     * "api.user.get:1.0"). This applies a global rate limit to the specific API.</li>
     * <li><b>Per-IP API Limit:</b> It then looks for a limiter keyed by the IP address plus the method and version
     * (e.g., "192.168.1.100:api.user.get:1.0"). This applies a stricter limit for a single user on that API.</li>
     * </ol>
     * This allows for flexible rules like, "Limit the user-get API to 1000 requests/minute globally, but only allow 10
     * requests/minute from any single IP address."
     *
     * @param methodVersion The combined method and version string for the API.
     * @param ip            The client's IP address.
     * @return A {@link Set} of all {@link Limiter} instances that should be applied to this request.
     */
    private Set<Limiter> getLimiter(String methodVersion, String ip) {
        String[] limitKeys = { methodVersion, ip + methodVersion };
        Set<Limiter> limiters = new HashSet<>();
        for (String limitKey : limitKeys) {
            Limiter limiter = registry.get(limitKey);
            if (null != limiter) {
                limiters.add(limiter);
            }
        }
        return limiters;
    }

}

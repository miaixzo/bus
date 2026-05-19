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

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.Limiter;
import org.miaixz.bus.vortex.registry.LimiterRegistry;

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
 * @see Limiter
 * @see LimiterRegistry
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.FOURTH)
public class LimiterStrategy extends AbstractStrategy {

    /**
     * The registry that holds all configured rate limiter instances.
     */
    private final LimiterRegistry registry;

    /**
     * Constructs a new {@code LimitStrategy}.
     *
     * @param registry The registry containing all available {@link Limiter}s.
     */
    public LimiterStrategy(LimiterRegistry registry) {
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
     * <li>Calls {@link #getLimiters} to find all applicable limiters (e.g., global and per-IP).</li>
     * <li>For each applicable limiter, it calls the non-blocking {@link Limiter#acquire()} method. If a limiter has no
     * available tokens, this call throws an exception, which is caught by the global error handler to produce a "Too
     * Many Requests" response.</li>
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
            String clientIp = context.getX_request_ip();

            Logger.debug(
                    true,
                    "Vortex",
                    "Rate limit check started: strategy=limiter, clientIp={}, methodVersion={}",
                    clientIp,
                    methodVersion);

            Set<Limiter> limiters = getLimiters(methodVersion, clientIp);
            if (limiters.isEmpty()) {
                Logger.debug(
                        true,
                        "Vortex",
                        "No matching limiters found; bypassing rate limit: strategy=limiter, clientIp={}, methodVersion={}",
                        clientIp,
                        methodVersion);
                return chain.apply(exchange);
            }

            Logger.debug(
                    true,
                    "Vortex",
                    "Limiters resolved; acquiring permits: strategy=limiter, clientIp={}, limiterCount={}",
                    clientIp,
                    limiters.size());

            return Mono.fromRunnable(() -> acquireAll(limiters)).doOnError(ex -> {
                Logger.warn(
                        false,
                        "Vortex",
                        ex,
                        "Rate limit exceeded: strategy=limiter, clientIp={}, methodVersion={}, exception={}",
                        clientIp,
                        methodVersion,
                        ex.getClass().getSimpleName());
            }).then(Mono.fromRunnable(() -> {
                Logger.info(
                        true,
                        "Vortex",
                        "Rate limit permits acquired: strategy=limiter, clientIp={}, path={}, methodVersion={}",
                        clientIp,
                        exchange.getRequest().getURI().getPath(),
                        methodVersion);
            })).then(chain.apply(exchange));
        });
    }

    /**
     * Finds all applicable limiters for a given request.
     * <p>
     * This method implements a two-layer lookup:
     * <ol>
     * <li><b>Global API Limit:</b> "api.user.get:1.0"</li>
     * <li><b>Per-IP API Limit:</b> "192.168.1.100:api.user.get:1.0"</li>
     * </ol>
     * Both lookups are in-memory registry reads and run in the current reactive chain.
     *
     * @param methodVersion The combined method and version string for the API.
     * @param ip            The client's IP address.
     * @return the set of all found {@link Limiter} instances
     */
    private Set<Limiter> getLimiters(String methodVersion, String ip) {
        Logger.debug(
                true,
                "Vortex",
                "Limiter key lookup started: strategy=limiter, clientIp={}, methodVersion={}",
                ip,
                methodVersion);

        Set<Limiter> limiters = new LinkedHashSet<>();
        addLimiter(limiters, registry.get(methodVersion));
        addLimiter(limiters, registry.get(ip + methodVersion));
        return limiters;
    }

    /**
     * Adds a limiter when it is present.
     *
     * @param limiters resolved limiter set
     * @param limiter  limiter candidate
     */
    private void addLimiter(Set<Limiter> limiters, Limiter limiter) {
        if (limiter != null) {
            limiters.add(limiter);
        }
    }

    /**
     * Acquires permits from all resolved limiters.
     *
     * @param limiters resolved limiter set
     */
    private void acquireAll(Set<Limiter> limiters) {
        for (Limiter limiter : limiters) {
            limiter.acquire();
        }
    }

}

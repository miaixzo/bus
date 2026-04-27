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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.Limiter;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
 * @since Java 21+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
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
     * <li>Calls {@link #getLimiter} to find all applicable limiters (e.g., global and per-IP).</li>
     * <li>For each applicable limiter, it asynchronously calls the {@link Limiter#acquire()} method. If a limiter has
     * no available tokens, this call will throw an exception, which is caught by the global error handler to produce a
     * "Too Many Requests" response.</li>
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

            Logger.debug(true, "Limiter", "[{}] Applying rate limits for: {}", clientIp, methodVersion);

            return getLimiter(methodVersion, clientIp).flatMap(limiters -> {
                if (limiters.isEmpty()) {
                    Logger.debug(
                            true,
                            "Limiter",
                            "[{}] No specific limiters found for {}. Bypassing.",
                            clientIp,
                            methodVersion);
                    return chain.apply(exchange);
                }

                Logger.debug(
                        true,
                        "Limiter",
                        "[{}] Found {} limiter(s). Attempting to acquire...",
                        clientIp,
                        limiters.size());

                List<Mono<Void>> acquireMonos = limiters.stream()
                        .map(
                                limiter -> Mono.fromRunnable(() -> limiter.acquire())
                                        .subscribeOn(Schedulers.boundedElastic()).then())
                        .collect(Collectors.toList());

                return Mono.when(acquireMonos).doOnError(ex -> {
                    Logger.warn(
                            false,
                            "Limiter",
                            "[{}] Rate limit EXCEEDED for {}. Error: {}",
                            clientIp,
                            methodVersion,
                            ex.getMessage());
                }).then(Mono.fromRunnable(() -> {
                    Logger.info(
                            true,
                            "Limiter",
                            "[{}] Rate limit(s) acquired successfully - Path: {}, Method: {}",
                            clientIp,
                            exchange.getRequest().getURI().getPath(),
                            methodVersion);
                })).then(chain.apply(exchange));
            });
        });
    }

    /**
     * Asynchronously finds all applicable limiters for a given request.
     * <p>
     * This method implements a two-layer lookup:
     * <ol>
     * <li><b>Global API Limit:</b> "api.user.get:1.0"</li>
     * <li><b>Per-IP API Limit:</b> "192.168.1.100:api.user.get:1.0"</li>
     * </ol>
     * It fetches all potential limiters in parallel from the registry, assuming the {@code registry.get()} call might
     * be blocking I/O (e.g., a cache or DB lookup).
     *
     * @param methodVersion The combined method and version string for the API.
     * @param ip            The client's IP address.
     * @return A {@code Mono<Set<Limiter>>} that emits the set of all found {@link Limiter} instances.
     */
    private Mono<Set<Limiter>> getLimiter(String methodVersion, String ip) {
        Stream<String> limitKeys = Stream.of(methodVersion, ip + methodVersion);

        Logger.debug(true, "Limiter", "[{}] Searching for limiter keys: [{}]", ip, methodVersion);

        List<Mono<Limiter>> limiterMonos = limitKeys
                .map(key -> Mono.fromCallable(() -> registry.get(key)).subscribeOn(Schedulers.boundedElastic()))
                .collect(Collectors.toList());

        return Flux.merge(limiterMonos).filter(Objects::nonNull).collect(Collectors.toSet());
    }

}

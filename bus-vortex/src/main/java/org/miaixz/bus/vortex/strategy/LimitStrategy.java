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
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
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
            // --- This initial setup is synchronous, non-blocking, and fast ---
            final Context context = contextView.get(Context.class);

            Assert.notNull(context, "Context must be initialized by a preceding strategy.");
            Assert.notNull(context.getAssets(), "Assets must be resolved by a preceding strategy.");

            String methodVersion = context.getAssets().getMethod() + context.getAssets().getVersion();
            String clientIp = context.getX_request_ipv4(); // Populated by a preceding strategy
            // --- End of sync setup ---

            // 1. Asynchronously fetch all applicable limiters in parallel
            return getLimiter(methodVersion, clientIp).flatMap(limiters -> {
                if (limiters.isEmpty()) {
                    // No limiters to apply, proceed immediately
                    return chain.apply(exchange);
                }

                // 2. Create a list of async tasks for acquiring each limiter
                // Each acquire() call is blocking and must be offloaded
                List<Mono<Void>> acquireMonos = limiters.stream()
                        .map(
                                limiter -> Mono.fromRunnable(() -> limiter.acquire())
                                        // Offload the blocking acquire() call
                                        .subscribeOn(Schedulers.boundedElastic()).then() // <-- **FIXED:** Add .then()
                                                                                         // to resolve generics error
                ).collect(Collectors.toList());

                // 3. Wait for all limiters to be acquired in parallel
                return Mono.when(acquireMonos).then(Mono.fromRunnable(() -> {
                    // This logging runs after all acquisitions are successful
                    Logger.info(
                            "==>     Strategy: Rate limit applied - Path: {}, Method: {}",
                            exchange.getRequest().getURI().getPath(),
                            methodVersion);
                }))
                        // 4. Proceed to the next strategy in the chain
                        .then(chain.apply(exchange));
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
        // Create a stream of keys to check
        Stream<String> limitKeys = Stream.of(methodVersion, ip + methodVersion);

        // For each key, create a Mono that fetches the limiter, offloading the
        // blocking registry.get() call to the boundedElastic scheduler.
        List<Mono<Limiter>> limiterMonos = limitKeys
                .map(key -> Mono.fromCallable(() -> registry.get(key)).subscribeOn(Schedulers.boundedElastic()))
                .collect(Collectors.toList());

        // Run all fetches in parallel and collect non-null results into a Set
        return Flux.merge(limiterMonos).filter(Objects::nonNull).collect(Collectors.toSet());
    }

}

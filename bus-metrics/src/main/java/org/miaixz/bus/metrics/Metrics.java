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
package org.miaixz.bus.metrics;

import java.util.function.ToDoubleFunction;

import org.miaixz.bus.metrics.metric.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Static facade for bus-metrics. The sole public entry point for application code.
 * <p>
 * Provider is resolved via SPI ({@code NativeProvider} by default). Call {@link #setProvider} to override at startup
 * (e.g. from Spring auto-configuration).
 * <p>
 * Usage examples:
 * 
 * <pre>{@code
 * // Counter
 * Metrics.counter("order.created", "region", "cn").increment();
 *
 * // Meter with in-process req/s
 * Meter qps = Metrics.meter("http.requests", "method", "GET");
 * qps.increment();
 * double rps = qps.oneMinuteRate();
 *
 * // Timer with SLA violation callback
 * Metrics.timer("payment.process").onViolation(0.99, 500, MILLIS, 100, e -> alert(e));
 *
 * // LLM timer
 * LlmSample s = Metrics.llmTimer("ai.chat").start("claude-opus-4-6", "anthropic", "chat");
 * s.recordFirstToken();
 * s.stop(inputTokens, outputTokens, stopReason);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Metrics {

    /**
     * Active metrics provider; lazily initialised via SPI on first access.
     */
    private static volatile Provider PROVIDER;

    /**
     * Returns the active provider, loading it via SPI on first call.
     *
     * @return the current {@link Provider}
     */
    public static Provider getProvider() {
        if (PROVIDER == null) {
            synchronized (Metrics.class) {
                if (PROVIDER == null) {
                    PROVIDER = Factory.get();
                }
            }
        }
        return PROVIDER;
    }

    /**
     * Override the provider. Typically called by Spring auto-configuration.
     */
    public static void setProvider(Provider provider) {
        PROVIDER = provider;
    }

    /**
     * Converts flat key-value string pairs into a {@link Tag} array.
     *
     * @param kvPairs alternating key, value strings (must be even length)
     * @return tag array
     * @throws IllegalArgumentException if {@code kvPairs} length is odd
     */
    public static Tag[] tags(String... kvPairs) {
        if (kvPairs == null || kvPairs.length == 0) {
            return new Tag[0];
        }
        if (kvPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Tags must be key-value pairs (even number of strings)");
        }
        Tag[] result = new Tag[kvPairs.length / 2];
        for (int i = 0; i < kvPairs.length; i += 2) {
            result[i / 2] = Tag.of(kvPairs[i], kvPairs[i + 1]);
        }
        return result;
    }

    /**
     * Returns a {@link Counter} for the given name and tags.
     *
     * @param name   metric name
     * @param kvTags alternating key-value tag pairs
     * @return counter instance
     */
    public static Counter counter(String name, String... kvTags) {
        return getProvider().counter(name, tags(kvTags));
    }

    /**
     * Returns a {@link Meter} (EWMA rate) for the given name and tags.
     *
     * @param name   metric name
     * @param kvTags alternating key-value tag pairs
     * @return meter instance
     */
    public static Meter meter(String name, String... kvTags) {
        return getProvider().meter(name, tags(kvTags));
    }

    /**
     * Returns a {@link RatePair} tracking success and error rates.
     *
     * @param name   metric name
     * @param kvTags alternating key-value tag pairs
     * @return rate pair instance
     */
    public static RatePair ratePair(String name, String... kvTags) {
        return getProvider().ratePair(name, tags(kvTags));
    }

    /**
     * Registers a pull-based {@link Gauge} backed by a state object.
     *
     * @param name   metric name
     * @param obj    state object to sample
     * @param fn     function extracting a double from the state object
     * @param kvTags alternating key-value tag pairs
     * @param <T>    type of the state object
     * @return gauge instance
     */
    public static <T> Gauge gauge(String name, T obj, ToDoubleFunction<T> fn, String... kvTags) {
        return getProvider().gauge(name, obj, fn, tags(kvTags));
    }

    /**
     * Returns a {@link Timer} for the given name and tags.
     *
     * @param name   metric name
     * @param kvTags alternating key-value tag pairs
     * @return timer instance
     */
    public static Timer timer(String name, String... kvTags) {
        return getProvider().timer(name, tags(kvTags));
    }

    /**
     * Returns a {@link Histogram} for the given name and tags.
     *
     * @param name   metric name
     * @param kvTags alternating key-value tag pairs
     * @return histogram instance
     */
    public static Histogram histogram(String name, String... kvTags) {
        return getProvider().histogram(name, tags(kvTags));
    }

    /**
     * Returns an {@link LlmTimer} for recording LLM call metrics (TTFT, ITL, tokens, cost).
     *
     * @param name   metric name prefix
     * @param kvTags alternating key-value tag pairs
     * @return LLM timer instance
     */
    public static LlmTimer llmTimer(String name, String... kvTags) {
        return getProvider().llmTimer(name, tags(kvTags));
    }

    /**
     * Returns the {@link SloTracker} from the active provider.
     *
     * @return SLO tracker instance
     */
    public static SloTracker slo() {
        return getProvider().sloTracker();
    }

}

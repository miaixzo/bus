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
 * SPI interface for metrics provider implementations. Matches the pattern of {@code JsonProvider} in bus-extra.
 * <p>
 * Two implementations are provided out of the box:
 * <ul>
 * <li>{@code NativeProvider} — zero-dependency, T-Digest percentiles, EWMA rates</li>
 * <li>{@code MicrometerProvider} — delegates to a Micrometer MeterRegistry</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider {

    /**
     * Returns a {@link Counter} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return counter instance
     */
    Counter counter(String name, Tag... tags);

    /**
     * Returns a {@link Meter} (EWMA rate tracker) for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return meter instance
     */
    Meter meter(String name, Tag... tags);

    /**
     * Returns a {@link RatePair} tracking success and error rates.
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return rate pair instance
     */
    RatePair ratePair(String name, Tag... tags);

    /**
     * Registers a pull-based {@link Gauge} backed by a state object.
     *
     * @param name     metric name
     * @param stateObj state object to sample
     * @param fn       function extracting a double from the state object
     * @param tags     optional tags
     * @param <T>      type of the state object
     * @return gauge instance
     */
    <T> Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags);

    /**
     * Returns a {@link Timer} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return timer instance
     */
    Timer timer(String name, Tag... tags);

    /**
     * Returns a {@link Histogram} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return histogram instance
     */
    Histogram histogram(String name, Tag... tags);

    /**
     * Returns an {@link LlmTimer} for recording LLM call metrics.
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return LLM timer instance
     */
    LlmTimer llmTimer(String name, Tag... tags);

    /**
     * Returns the {@link SloTracker} for this provider.
     *
     * @return SLO tracker instance
     */
    SloTracker sloTracker();

    /**
     * Returns all registered counters.
     *
     * @return iterable of counters
     */
    Iterable<Counter> counters();

    /**
     * Returns all registered meters.
     *
     * @return iterable of meters
     */
    Iterable<Meter> meters();

    /**
     * Returns all registered gauges.
     *
     * @return iterable of gauges
     */
    Iterable<Gauge> gauges();

    /**
     * Returns all registered timers.
     *
     * @return iterable of timers
     */
    Iterable<Timer> timers();

    /**
     * Returns all registered histograms.
     *
     * @return iterable of histograms
     */
    Iterable<Histogram> histograms();

    /**
     * Returns all registered LLM timers.
     *
     * @return iterable of LLM timers
     */
    Iterable<LlmTimer> llmTimers();

}

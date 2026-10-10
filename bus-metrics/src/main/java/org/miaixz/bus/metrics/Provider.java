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

import org.miaixz.bus.metrics.nimble.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Attributes;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * SPI interface for metrics provider implementations. Matches the pattern of {@code JsonProvider} in bus-extra.
 * <p>
 * Four implementations are provided out of the box:
 * <ul>
 * <li>{@code NativeProvider} — zero-dependency, T-Digest percentiles, EWMA rates</li>
 * <li>{@code MicrometerProvider} — delegates to a Micrometer MeterRegistry</li>
 * <li>{@code OpenTelemetryProvider} — delegates to the OpenTelemetry API</li>
 * <li>{@code PrometheusProvider} — delegates to a Prometheus registry</li>
 * </ul>
 *
 * @author Kimi Liu
 */
public interface Provider extends AutoCloseable {

    /**
     * Validates a typed active-instrument call against its declared family.
     *
     * @param descriptor family descriptor
     * @param attributes active series attributes
     * @param kind       required instrument kind
     * @param numberKind required numeric representation
     */
    private static void validateActive(
            MetricDescriptor descriptor,
            Attributes attributes,
            InstrumentKind kind,
            NumberKind numberKind) {
        if (descriptor.kind() != kind || descriptor.numberKind() != numberKind) {
            throw new IllegalArgumentException(
                    "Metric descriptor is incompatible with active " + kind + " API: " + descriptor.name());
        }
        descriptor.validateAttributes(attributes);
    }

    /**
     * Returns a {@link Counter} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return counter instance
     */
    Counter counter(String name, Tag... tags);

    /**
     * Returns a counter using the typed family contract.
     *
     * @param descriptor metric descriptor; must be a long counter
     * @param attributes typed attributes matching the descriptor schema
     * @return counter instance
     */
    default Counter counter(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, attributes, InstrumentKind.COUNTER, NumberKind.LONG);
        return counter(descriptor.name(), attributes.toTags());
    }

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
     * Registers a pull-based gauge using the typed family contract.
     *
     * @param descriptor metric descriptor; must be a double gauge
     * @param attributes typed attributes matching the descriptor schema
     * @param stateObj   state object to sample
     * @param fn         function extracting a value from the state object
     * @param <T>        state object type
     * @return gauge instance
     */
    default <T> Gauge gauge(MetricDescriptor descriptor, Attributes attributes, T stateObj, ToDoubleFunction<T> fn) {
        validateActive(descriptor, attributes, InstrumentKind.GAUGE, NumberKind.DOUBLE);
        return gauge(descriptor.name(), stateObj, fn, attributes.toTags());
    }

    /**
     * Returns a {@link Timer} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return timer instance
     */
    Timer timer(String name, Tag... tags);

    /**
     * Returns a timer using the typed family contract.
     *
     * @param descriptor metric descriptor; must be a double timer
     * @param attributes typed attributes matching the descriptor schema
     * @return timer instance
     */
    default Timer timer(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, attributes, InstrumentKind.TIMER, NumberKind.DOUBLE);
        return timer(descriptor.name(), attributes.toTags());
    }

    /**
     * Returns a {@link Histogram} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return histogram instance
     */
    Histogram histogram(String name, Tag... tags);

    /**
     * Returns a histogram using the typed family contract.
     *
     * @param descriptor metric descriptor; must be a double histogram
     * @param attributes typed attributes matching the descriptor schema
     * @return histogram instance
     */
    default Histogram histogram(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, attributes, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE);
        return histogram(descriptor.name(), attributes.toTags());
    }

    /**
     * Registers one observable metric family.
     *
     * @param descriptor family descriptor
     * @param callback   collection callback
     * @return closeable registration
     * @throws UnsupportedOperationException when observable metrics are not supported
     */
    default MetricRegistration registerObservable(MetricDescriptor descriptor, ObservableCallback callback) {
        throw new UnsupportedOperationException("Observable metrics are not supported by this Provider");
    }

    /**
     * Returns the provider's fixed-cardinality diagnostics entry point.
     *
     * @return diagnostics implementation
     */
    default MetricDiagnostics diagnostics() {
        return MetricDiagnostics.noop();
    }

    /**
     * Returns stable capabilities for this provider instance.
     *
     * @return provider capabilities
     */
    default ProviderCapabilities capabilities() {
        return ProviderCapabilities.none();
    }

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

    /**
     * Releases resources owned by this provider. The default keeps legacy implementations source and binary compatible.
     */
    @Override
    default void close() {
    }

}

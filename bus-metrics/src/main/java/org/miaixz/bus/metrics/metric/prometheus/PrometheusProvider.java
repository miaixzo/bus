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
package org.miaixz.bus.metrics.metric.prometheus;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.metric.*;
import org.miaixz.bus.metrics.metric.indigenous.NativeMeter;
import org.miaixz.bus.metrics.metric.indigenous.NativeSloTracker;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

/**
 * Provider implementation backed by the Prometheus Java client SDK.
 * <p>
 * Uses {@link PrometheusRegistry} as the underlying registry. Counter, Gauge, Histogram, and Summary (for
 * timers/percentiles) are mapped to their native Prometheus equivalents.
 * <p>
 * Meter (EWMA rates) and LlmTimer are implemented locally since the Prometheus SDK has no equivalent types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrometheusProvider implements Provider {

    /** The Prometheus registry used to register all metric instruments. */
    private final PrometheusRegistry registry;

    /** Creates a PrometheusProvider backed by the default Prometheus registry. */
    public PrometheusProvider() {
        this(PrometheusRegistry.defaultRegistry);
    }

    /**
     * Creates a PrometheusProvider backed by the given registry.
     *
     * @param registry the Prometheus registry to register metrics into
     */
    public PrometheusProvider(PrometheusRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns the underlying Prometheus registry.
     *
     * @return the PrometheusRegistry used by this provider
     */
    public PrometheusRegistry getRegistry() {
        return registry;
    }

    /**
     * Extracts label names from a tag array.
     *
     * @param tags the tags to extract keys from
     * @return array of tag key strings
     */
    private String[] labelNames(Tag[] tags) {
        if (tags == null || tags.length == 0)
            return new String[0];
        String[] names = new String[tags.length];
        for (int i = 0; i < tags.length; i++)
            names[i] = tags[i].key();
        return names;
    }

    /**
     * Extracts label values from a tag array.
     *
     * @param tags the tags to extract values from
     * @return array of tag value strings
     */
    private String[] labelValues(Tag[] tags) {
        if (tags == null || tags.length == 0)
            return new String[0];
        String[] values = new String[tags.length];
        for (int i = 0; i < tags.length; i++)
            values[i] = tags[i].value();
        return values;
    }

    /**
     * Converts a metric name to a valid Prometheus metric name by replacing dots and hyphens with underscores.
     *
     * @param name the original metric name
     * @return Prometheus-compatible metric name
     */
    private String prometheusName(String name) {
        return name.replace('.', '_').replace('-', '_');
    }

    /**
     * Creates or retrieves a Prometheus-backed counter.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Counter backed by a Prometheus Counter
     */
    @Override
    public org.miaixz.bus.metrics.metric.Counter counter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        Counter c = Counter.builder().name(prometheusName(name)).labelNames(labelNames(tags)).register(registry);
        return new org.miaixz.bus.metrics.metric.Counter() {

            /** Increments the counter by 1. */
            @Override
            public void increment() {
                c.labelValues(labelValues(finalTags)).inc();
            }

            /**
             * Increments the counter by the given amount.
             *
             * @param amount the amount to add
             */
            @Override
            public void increment(long amount) {
                c.labelValues(labelValues(finalTags)).inc(amount);
            }

            /**
             * Returns the current counter value.
             *
             * @return current count
             */
            @Override
            public long count() {
                return (long) c.labelValues(labelValues(finalTags)).get();
            }
        };
    }

    /**
     * Creates or retrieves a Prometheus-backed meter (counter + local EWMA rates).
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Meter backed by a Prometheus Counter with local EWMA rate tracking
     */
    @Override
    public org.miaixz.bus.metrics.metric.Meter meter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        Counter c = Counter.builder().name(prometheusName(name)).labelNames(labelNames(tags)).register(registry);
        NativeMeter nm = new NativeMeter();
        return new org.miaixz.bus.metrics.metric.Meter() {

            /** Increments the meter by 1. */
            @Override
            public void increment() {
                increment(1);
            }

            /**
             * Increments the meter by the given amount.
             *
             * @param amount the amount to add
             */
            @Override
            public void increment(long amount) {
                c.labelValues(labelValues(finalTags)).inc(amount);
                nm.increment(amount);
            }

            /**
             * Returns the current total count.
             *
             * @return current count
             */
            @Override
            public long count() {
                return (long) c.labelValues(labelValues(finalTags)).get();
            }

            /**
             * Returns the 1-minute EWMA rate.
             *
             * @return 1-minute rate
             */
            @Override
            public double oneMinuteRate() {
                return nm.oneMinuteRate();
            }

            /**
             * Returns the 5-minute EWMA rate.
             *
             * @return 5-minute rate
             */
            @Override
            public double fiveMinuteRate() {
                return nm.fiveMinuteRate();
            }

            /**
             * Returns the 15-minute EWMA rate.
             *
             * @return 15-minute rate
             */
            @Override
            public double fifteenMinuteRate() {
                return nm.fifteenMinuteRate();
            }

            /**
             * Returns the mean rate since creation.
             *
             * @return mean rate
             */
            @Override
            public double meanRate() {
                return nm.meanRate();
            }
        };
    }

    /**
     * Creates a RatePair backed by three Prometheus counters (total, errors, successes).
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return a RatePair tracking success/error rates
     */
    @Override
    public org.miaixz.bus.metrics.metric.RatePair ratePair(String name, Tag... tags) {
        org.miaixz.bus.metrics.metric.Meter total = meter(name + ".total", tags);
        org.miaixz.bus.metrics.metric.Meter errors = meter(name + ".errors", tags);
        org.miaixz.bus.metrics.metric.Meter successes = meter(name + ".successes", tags);
        return new org.miaixz.bus.metrics.metric.RatePair() {

            /** Records a successful operation, incrementing both total and success meters. */
            @Override
            public void recordSuccess() {
                total.increment();
                successes.increment();
            }

            /** Records a failed operation, incrementing both total and error meters. */
            @Override
            public void recordError() {
                total.increment();
                errors.increment();
            }

            /**
             * Returns the 1-minute error rate as a fraction of total rate.
             *
             * @return error rate [0.0, 1.0]
             */
            @Override
            public double errorRate() {
                double t = total.oneMinuteRate();
                return t <= 0 ? 0.0 : errors.oneMinuteRate() / t;
            }

            /**
             * Returns the 1-minute success rate as a fraction of total rate.
             *
             * @return success rate [0.0, 1.0]
             */
            @Override
            public double successRate() {
                double t = total.oneMinuteRate();
                return t <= 0 ? 1.0 : successes.oneMinuteRate() / t;
            }

            /**
             * Returns the total meter.
             *
             * @return total meter
             */
            @Override
            public org.miaixz.bus.metrics.metric.Meter total() {
                return total;
            }

            /**
             * Returns the errors meter.
             *
             * @return errors meter
             */
            @Override
            public org.miaixz.bus.metrics.metric.Meter errors() {
                return errors;
            }

            /**
             * Returns the successes meter.
             *
             * @return successes meter
             */
            @Override
            public org.miaixz.bus.metrics.metric.Meter successes() {
                return successes;
            }
        };
    }

    /**
     * Creates a Prometheus-backed gauge that reads from the given state object.
     *
     * @param name     metric name
     * @param stateObj object whose state is sampled on each read
     * @param fn       function to extract a double value from the state object
     * @param tags     optional tags
     * @return a Gauge backed by a Prometheus Gauge
     */
    @Override
    public <T> org.miaixz.bus.metrics.metric.Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        Gauge g = Gauge.builder().name(prometheusName(name)).labelNames(labelNames(tags)).register(registry);
        return () -> {
            double v = fn.applyAsDouble(stateObj);
            g.labelValues(labelValues(finalTags)).set(v);
            return v;
        };
    }

    /**
     * Creates a Prometheus-backed timer using a Summary with P50/P95/P99/P999 quantiles.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Timer backed by a Prometheus Summary
     */
    @Override
    public org.miaixz.bus.metrics.metric.Timer timer(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        Summary s = Summary.builder().name(prometheusName(name) + "_seconds").labelNames(labelNames(tags))
                .quantile(0.5, 0.05).quantile(0.95, 0.01).quantile(0.99, 0.001).quantile(0.999, 0.0001)
                .register(registry);
        return new org.miaixz.bus.metrics.metric.Timer() {

            /**
             * Starts a timer sample and returns a handle that records the duration on stop.
             *
             * @return a new in-flight timing sample
             */
            @Override
            public Sample start() {
                long t0 = System.nanoTime();
                return () -> {
                    long nanos = System.nanoTime() - t0;
                    s.labelValues(labelValues(finalTags)).observe(nanos / 1e9);
                    return nanos;
                };
            }

            /**
             * Records a duration directly.
             *
             * @param amount duration value
             * @param unit   time unit of {@code amount}
             */
            @Override
            public void record(long amount, TimeUnit unit) {
                s.labelValues(labelValues(finalTags)).observe(unit.toNanos(amount) / 1e9);
            }

            /**
             * Returns the total number of recordings.
             *
             * @return recording count
             */
            @Override
            public long count() {
                return s.collect().getDataPoints().stream().mapToLong(dp -> dp.getCount()).sum();
            }

            /**
             * Returns the total time accumulated; not supported by Prometheus Summary.
             *
             * @param unit desired time unit
             * @return 0 (not supported)
             */
            @Override
            public double totalTime(TimeUnit unit) {
                return 0;
            }

            /**
             * Returns the maximum recorded duration; not supported by Prometheus Summary.
             *
             * @param unit desired time unit
             * @return 0 (not supported)
             */
            @Override
            public double max(TimeUnit unit) {
                return 0;
            }

            /**
             * Returns the estimated percentile value from the Prometheus Summary.
             *
             * @param p    percentile 0.0–1.0
             * @param unit desired time unit
             * @return estimated percentile value
             */
            @Override
            public double percentile(double p, TimeUnit unit) {
                return s.collect().getDataPoints().stream()
                        .flatMap(dp -> java.util.stream.StreamSupport.stream(dp.getQuantiles().spliterator(), false))
                        .filter(q -> Double.compare(q.getQuantile(), p) == 0)
                        .mapToDouble(q -> q.getValue() * unit.toNanos(1) / 1e9)
                        .findFirst()
                        .orElse(0);
            }

            /**
             * Returns the estimated percentile value; window parameter is ignored for Prometheus Summary.
             *
             * @param p      percentile 0.0–1.0
             * @param unit   desired time unit
             * @param window rolling window (ignored)
             * @return estimated percentile value
             */
            @Override
            public double percentile(double p, TimeUnit unit, Window window) {
                return percentile(p, unit);
            }

            /**
             * SLA violation callbacks are not supported by Prometheus Summary; returns this timer unchanged.
             *
             * @param percentile percentile to monitor
             * @param threshold  threshold value
             * @param unit       threshold unit
             * @param checkEvery check interval in recordings
             * @param callback   violation callback
             * @return this timer
             */
            @Override
            public org.miaixz.bus.metrics.metric.Timer onViolation(
                    double percentile,
                    long threshold,
                    TimeUnit unit,
                    int checkEvery,
                    Consumer<ViolationEvent> callback) {
                return this;
            }

            /**
             * Returns a minimal snapshot with count only; histogram buckets are not available from Prometheus Summary.
             *
             * @return timer snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, count(), 0, 0, new long[0], new double[0]);
            }
        };
    }

    /**
     * Creates a Prometheus-backed histogram.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Histogram backed by a Prometheus Histogram
     */
    @Override
    public org.miaixz.bus.metrics.metric.Histogram histogram(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        Histogram h = Histogram.builder().name(prometheusName(name)).labelNames(labelNames(tags)).register(registry);
        return new org.miaixz.bus.metrics.metric.Histogram() {

            /**
             * Records a single observed value.
             *
             * @param value the value to record
             */
            @Override
            public void record(double value) {
                h.labelValues(labelValues(finalTags)).observe(value);
            }

            /**
             * Returns the total number of observations.
             *
             * @return observation count
             */
            @Override
            public long count() {
                return h.collect().getDataPoints().stream().mapToLong(dp -> dp.getCount()).sum();
            }

            /**
             * Returns the sum of all observed values.
             *
             * @return total amount
             */
            @Override
            public double totalAmount() {
                return h.collect().getDataPoints().stream().mapToDouble(dp -> dp.getSum()).sum();
            }

            /**
             * Returns the maximum observed value; not supported by Prometheus Histogram.
             *
             * @return 0 (not supported)
             */
            @Override
            public double max() {
                return 0;
            }

            /**
             * Returns the estimated percentile; not supported by Prometheus Histogram.
             *
             * @param p percentile 0.0–1.0
             * @return 0 (not supported)
             */
            @Override
            public double percentile(double p) {
                return 0;
            }

            /**
             * Returns a snapshot with count and total amount; bucket details are not available.
             *
             * @return histogram snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, count(), totalAmount(), 0, new long[0], new double[0]);
            }
        };
    }

    /**
     * Creates an LlmTimer backed by this provider's timers and counters.
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return an LlmTimer for tracking LLM request latency, tokens, and errors
     */
    @Override
    public org.miaixz.bus.metrics.metric.LlmTimer llmTimer(String name, Tag... tags) {
        Tag[] finalTags = tags;
        return (model, provider_, operation) -> {
            long startNs = System.nanoTime();
            return new org.miaixz.bus.metrics.metric.LlmSample() {

                /** Nanosecond timestamp of the first token; -1 if not yet recorded. */
                private volatile long firstTokenNs = -1;

                /** Records the nanosecond timestamp of the first token received. */
                @Override
                public void recordFirstToken() {
                    firstTokenNs = System.nanoTime();
                }

                /**
                 * Stops the sample and records duration, TTFT, ITL, and token counts.
                 *
                 * @param inputTokens  number of input tokens consumed
                 * @param outputTokens number of output tokens generated
                 * @param finishReason reason the generation stopped
                 */
                @Override
                public void stop(int inputTokens, int outputTokens, String finishReason) {
                    long totalNs = System.nanoTime() - startNs;
                    timer(
                            name + Builder.LLM_SUFFIX_DURATION,
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, provider_),
                            Tag.of(Builder.TAG_OPERATION, operation),
                            Tag.of(Builder.TAG_FINISH_REASON, finishReason)).record(totalNs, TimeUnit.NANOSECONDS);
                    if (firstTokenNs > 0) {
                        timer(
                                name + Builder.LLM_SUFFIX_TTFT,
                                Tag.of(Builder.TAG_MODEL, model),
                                Tag.of(Builder.TAG_PROVIDER, provider_))
                                        .record(firstTokenNs - startNs, TimeUnit.NANOSECONDS);
                        if (outputTokens > 1) {
                            timer(
                                    name + Builder.LLM_SUFFIX_ITL,
                                    Tag.of(Builder.TAG_MODEL, model),
                                    Tag.of(Builder.TAG_PROVIDER, provider_)).record(
                                            (totalNs - (firstTokenNs - startNs)) / (outputTokens - 1),
                                            TimeUnit.NANOSECONDS);
                        }
                    }
                    counter(
                            name + Builder.LLM_SUFFIX_TOKENS,
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, provider_),
                            Tag.of(Builder.TAG_TYPE, "input")).increment(inputTokens);
                    counter(
                            name + Builder.LLM_SUFFIX_TOKENS,
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, provider_),
                            Tag.of(Builder.TAG_TYPE, "output")).increment(outputTokens);
                }

                /**
                 * Records an error counter and delegates to {@link #stop} with zero tokens.
                 *
                 * @param t the throwable that caused the error
                 */
                @Override
                public void error(Throwable t) {
                    counter(
                            name + Builder.LLM_SUFFIX_ERRORS,
                            Tag.of(Builder.TAG_MODEL, model),
                            Tag.of(Builder.TAG_PROVIDER, provider_),
                            Tag.of(Builder.TAG_ERROR_TYPE, t.getClass().getSimpleName())).increment();
                    stop(0, 0, "error");
                }
            };
        };
    }

    /**
     * Returns a new NativeSloTracker for SLO compliance tracking.
     *
     * @return a new SloTracker instance
     */
    @Override
    public SloTracker sloTracker() {
        return new NativeSloTracker();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Counter> counters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Meter> meters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Gauge> gauges() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Timer> timers() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Histogram> histograms() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Prometheus registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.LlmTimer> llmTimers() {
        return Collections.emptyList();
    }

}

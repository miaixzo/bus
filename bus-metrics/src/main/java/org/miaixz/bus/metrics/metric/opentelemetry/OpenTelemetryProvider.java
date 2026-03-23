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
package org.miaixz.bus.metrics.metric.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

/**
 * Provider implementation backed by OpenTelemetry SDK.
 * <p>
 * Maps bus-metrics types to OTel instruments:
 * <ul>
 * <li>Counter → {@link LongCounter}</li>
 * <li>Gauge → {@link ObservableDoubleGauge}</li>
 * <li>Timer → {@link DoubleHistogram} (seconds)</li>
 * <li>Histogram → {@link DoubleHistogram}</li>
 * <li>Meter → LongCounter + local EWMA (OTel has no rate type)</li>
 * <li>LlmTimer → OTel GenAI SIG 2025 conventions</li>
 * </ul>
 * <p>
 * The {@code instrumentationScope} defaults to {@code "bus-metrics"}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OpenTelemetryProvider implements Provider {

    /**
     * Default instrumentation scope name used when none is specified.
     */
    private static final String DEFAULT_SCOPE = Builder.OTEL_SCOPE;

    /**
     * The OpenTelemetry instance used to obtain the OTel Meter.
     */
    private final OpenTelemetry openTelemetry;
    /**
     * OTel Meter used to create all instrument instances.
     */
    private final Meter otelMeter;

    /**
     * Creates an OpenTelemetryProvider using the default instrumentation scope {@code "bus-metrics"}.
     *
     * @param openTelemetry the OpenTelemetry instance to obtain meters from
     */
    public OpenTelemetryProvider(OpenTelemetry openTelemetry) {
        this(openTelemetry, DEFAULT_SCOPE);
    }

    /**
     * Creates an OpenTelemetryProvider with a custom instrumentation scope.
     *
     * @param openTelemetry        the OpenTelemetry instance to obtain meters from
     * @param instrumentationScope instrumentation scope name used when building the OTel Meter
     */
    public OpenTelemetryProvider(OpenTelemetry openTelemetry, String instrumentationScope) {
        this.openTelemetry = openTelemetry;
        this.otelMeter = openTelemetry.getMeter(instrumentationScope);
    }

    /**
     * Converts a bus-metrics tag array to OTel {@link Attributes}.
     *
     * @param tags bus-metrics tag array, may be null or empty
     * @return equivalent OTel Attributes
     */
    private Attributes toAttributes(Tag[] tags) {
        if (tags == null || tags.length == 0)
            return Attributes.empty();
        AttributesBuilder b = Attributes.builder();
        for (Tag tag : tags) {
            b.put(tag.key(), tag.value());
        }
        return b.build();
    }

    /**
     * Creates an OTel-backed counter using a {@link LongCounter}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Counter backed by an OTel LongCounter
     */
    @Override
    public org.miaixz.bus.metrics.metric.Counter counter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        LongCounter c = otelMeter.counterBuilder(name).build();
        return new org.miaixz.bus.metrics.metric.Counter() {

            /** Cumulative event count; updated on each increment call. */
            private long total = 0;

            /** Increments the counter by one. */
            @Override
            public void increment() {
                increment(1);
            }

            /**
             * Increments the counter by the given amount, forwarding to the OTel LongCounter.
             *
             * @param amount the number to add
             */
            @Override
            public synchronized void increment(long amount) {
                c.add(amount, toAttributes(finalTags));
                total += amount;
            }

            /**
             * Returns the current cumulative count.
             *
             * @return total count as a long
             */
            @Override
            public synchronized long count() {
                return total;
            }
        };
    }

    /**
     * Creates an OTel-backed meter (LongCounter + local EWMA rates).
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Meter backed by an OTel LongCounter with local EWMA rate tracking
     */
    @Override
    public org.miaixz.bus.metrics.metric.Meter meter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        LongCounter c = otelMeter.counterBuilder(name).build();
        NativeMeter nm = new NativeMeter();
        return new org.miaixz.bus.metrics.metric.Meter() {

            /** Cumulative event count; updated on each increment call. */
            private long total = 0;

            /** Increments the meter by one. */
            @Override
            public void increment() {
                increment(1);
            }

            /**
             * Increments the meter by the given amount, updating both the OTel counter and local EWMA.
             *
             * @param amount the number to add
             */
            @Override
            public synchronized void increment(long amount) {
                c.add(amount, toAttributes(finalTags));
                nm.increment(amount);
                total += amount;
            }

            /**
             * Returns the current cumulative count.
             *
             * @return total count as a long
             */
            @Override
            public synchronized long count() {
                return total;
            }

            /**
             * Returns the one-minute exponentially weighted moving average rate.
             *
             * @return one-minute EWMA rate
             */
            @Override
            public double oneMinuteRate() {
                return nm.oneMinuteRate();
            }

            /**
             * Returns the five-minute exponentially weighted moving average rate.
             *
             * @return five-minute EWMA rate
             */
            @Override
            public double fiveMinuteRate() {
                return nm.fiveMinuteRate();
            }

            /**
             * Returns the fifteen-minute exponentially weighted moving average rate.
             *
             * @return fifteen-minute EWMA rate
             */
            @Override
            public double fifteenMinuteRate() {
                return nm.fifteenMinuteRate();
            }

            /**
             * Returns the mean rate since the meter was created.
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
     * Creates a RatePair backed by three OTel counters (total, errors, successes).
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

            /** Records a successful event, incrementing both total and successes meters. */
            @Override
            public void recordSuccess() {
                total.increment();
                successes.increment();
            }

            /** Records an error event, incrementing both total and errors meters. */
            @Override
            public void recordError() {
                total.increment();
                errors.increment();
            }

            /**
             * Returns the one-minute error rate as a fraction of total rate.
             *
             * @return error rate between 0.0 and 1.0
             */
            @Override
            public double errorRate() {
                double t = total.oneMinuteRate();
                return t <= 0 ? 0.0 : errors.oneMinuteRate() / t;
            }

            /**
             * Returns the one-minute success rate as a fraction of total rate.
             *
             * @return success rate between 0.0 and 1.0
             */
            @Override
            public double successRate() {
                double t = total.oneMinuteRate();
                return t <= 0 ? 1.0 : successes.oneMinuteRate() / t;
            }

            /**
             * Returns the total meter tracking all events.
             *
             * @return total meter
             */
            @Override
            public org.miaixz.bus.metrics.metric.Meter total() {
                return total;
            }

            /**
             * Returns the meter tracking error events.
             *
             * @return errors meter
             */
            @Override
            public org.miaixz.bus.metrics.metric.Meter errors() {
                return errors;
            }

            /**
             * Returns the meter tracking successful events.
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
     * Creates an OTel-backed gauge using an {@link ObservableDoubleGauge} callback.
     *
     * @param name     metric name
     * @param stateObj object whose state is sampled on each OTel collection cycle
     * @param fn       function to extract a double value from the state object
     * @param tags     optional tags
     * @return a Gauge that also caches the last observed value for local reads
     */
    @Override
    public <T> org.miaixz.bus.metrics.metric.Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        AtomicReference<Double> last = new AtomicReference<>(0.0);
        otelMeter.gaugeBuilder(name).buildWithCallback(obs -> {
            double v = fn.applyAsDouble(stateObj);
            last.set(v);
            obs.record(v, toAttributes(finalTags));
        });
        return () -> last.get();
    }

    /**
     * Creates an OTel-backed timer using a {@link DoubleHistogram} with unit {@code "s"}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Timer backed by an OTel DoubleHistogram
     */
    @Override
    public org.miaixz.bus.metrics.metric.Timer timer(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        DoubleHistogram h = otelMeter.histogramBuilder(name).setUnit("s").build();
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
                    h.record(nanos / 1e9, toAttributes(finalTags));
                    return nanos;
                };
            }

            /**
             * Records a duration directly to the OTel histogram.
             *
             * @param amount the duration amount
             * @param unit   the time unit of the amount
             */
            @Override
            public void record(long amount, TimeUnit unit) {
                h.record(unit.toNanos(amount) / 1e9, toAttributes(finalTags));
            }

            /**
             * Returns zero; OTel push-based histograms do not support local count reads.
             *
             * @return 0
             */
            @Override
            public long count() {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local total reads.
             *
             * @param unit the time unit for the result
             * @return 0
             */
            @Override
            public double totalTime(TimeUnit unit) {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local max reads.
             *
             * @param unit the time unit for the result
             * @return 0
             */
            @Override
            public double max(TimeUnit unit) {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local percentile reads.
             *
             * @param p    percentile between 0.0 and 1.0
             * @param unit the time unit for the result
             * @return 0
             */
            @Override
            public double percentile(double p, TimeUnit unit) {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support rolling window percentile reads.
             *
             * @param p      percentile between 0.0 and 1.0
             * @param unit   the time unit for the result
             * @param window the rolling window (ignored)
             * @return 0
             */
            @Override
            public double percentile(double p, TimeUnit unit, Window window) {
                return 0;
            }

            /**
             * SLA violation callbacks are not supported by OTel; returns this timer unchanged.
             *
             * @param percentile the percentile to monitor
             * @param threshold  the threshold value
             * @param unit       the time unit for the threshold
             * @param checkEvery check interval in seconds
             * @param callback   callback to invoke on violation
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
             * Returns an empty snapshot; OTel push-based histograms do not support local reads.
             *
             * @return empty timer snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, 0, 0, 0, new long[0], new double[0]);
            }
        };
    }

    /**
     * Creates an OTel-backed histogram using a {@link DoubleHistogram}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Histogram backed by an OTel DoubleHistogram
     */
    @Override
    public org.miaixz.bus.metrics.metric.Histogram histogram(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        DoubleHistogram h = otelMeter.histogramBuilder(name).build();
        return new org.miaixz.bus.metrics.metric.Histogram() {

            /**
             * Records a single observation value to the OTel histogram.
             *
             * @param value the observed value
             */
            @Override
            public void record(double value) {
                h.record(value, toAttributes(finalTags));
            }

            /**
             * Returns zero; OTel push-based histograms do not support local count reads.
             *
             * @return 0
             */
            @Override
            public long count() {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local total reads.
             *
             * @return 0
             */
            @Override
            public double totalAmount() {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local max reads.
             *
             * @return 0
             */
            @Override
            public double max() {
                return 0;
            }

            /**
             * Returns zero; OTel push-based histograms do not support local percentile reads.
             *
             * @param p percentile between 0.0 and 1.0
             * @return 0
             */
            @Override
            public double percentile(double p) {
                return 0;
            }

            /**
             * Returns an empty snapshot; OTel push-based histograms do not support local reads.
             *
             * @return empty histogram snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, 0, 0, 0, new long[0], new double[0]);
            }
        };
    }

    /**
     * Creates an LlmTimer following OTel GenAI SIG 2025 naming conventions.
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return an LlmTimer for tracking LLM request latency, tokens, and errors
     */
    @Override
    public org.miaixz.bus.metrics.metric.LlmTimer llmTimer(String name, Tag... tags) {
        // OTel GenAI SIG 2025 conventions
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
                 * Stops the sample and records duration, TTFT, ITL, and token counts using OTel GenAI SIG 2025
                 * conventions.
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
                            Tag.of("gen_ai.system", provider_),
                            Tag.of("gen_ai.request.model", model),
                            Tag.of("gen_ai.operation.name", operation),
                            Tag.of("gen_ai.response.finish_reasons", finishReason))
                                    .record(totalNs, TimeUnit.NANOSECONDS);
                    if (firstTokenNs > 0) {
                        timer(
                                name + ".server.time_to_first_token",
                                Tag.of("gen_ai.system", provider_),
                                Tag.of("gen_ai.request.model", model))
                                        .record(firstTokenNs - startNs, TimeUnit.NANOSECONDS);
                        if (outputTokens > 1) {
                            timer(
                                    name + ".server.time_per_output_token",
                                    Tag.of("gen_ai.system", provider_),
                                    Tag.of("gen_ai.request.model", model)).record(
                                            (totalNs - (firstTokenNs - startNs)) / (outputTokens - 1),
                                            TimeUnit.NANOSECONDS);
                        }
                    }
                    counter(
                            name + ".client.token.usage",
                            Tag.of("gen_ai.system", provider_),
                            Tag.of("gen_ai.request.model", model),
                            Tag.of("gen_ai.token.type", "input")).increment(inputTokens);
                    counter(
                            name + ".client.token.usage",
                            Tag.of("gen_ai.system", provider_),
                            Tag.of("gen_ai.request.model", model),
                            Tag.of("gen_ai.token.type", "output")).increment(outputTokens);
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
                            Tag.of("gen_ai.system", provider_),
                            Tag.of("gen_ai.request.model", model),
                            Tag.of("error.type", t.getClass().getSimpleName())).increment();
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

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Counter> counters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Meter> meters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Gauge> gauges() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Timer> timers() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.Histogram> histograms() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; OTel registry enumeration is not supported. */
    @Override
    public Iterable<org.miaixz.bus.metrics.metric.LlmTimer> llmTimers() {
        return Collections.emptyList();
    }

}

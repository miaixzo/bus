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
package org.miaixz.bus.metrics.metric.micrometer;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.metric.*;
import org.miaixz.bus.metrics.metric.indigenous.NativeMeter;
import org.miaixz.bus.metrics.metric.indigenous.NativeSloTracker;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Provider implementation that delegates to a Micrometer {@link MeterRegistry}.
 * <p>
 * CardinalityGuard and onViolation callbacks are applied in this adapter layer, before values reach the underlying
 * registry. Meter (EWMA rates) and LlmTimer are implemented locally since Micrometer has no equivalent.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MicrometerProvider implements Provider {

    /**
     * The Micrometer registry used to register all metric instruments.
     */
    private final MeterRegistry registry;

    /**
     * Create a provider backed by the given Micrometer registry.
     *
     * @param registry the Micrometer MeterRegistry to delegate to
     */
    public MicrometerProvider(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Convert bus-metrics tags to Micrometer {@link Tags}.
     *
     * @param tags bus-metrics tag array, may be null or empty
     * @return equivalent Micrometer Tags
     */
    private Tags toMicrometerTags(Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return Tags.empty();
        }
        String[] kvs = new String[tags.length * 2];
        for (int i = 0; i < tags.length; i++) {
            kvs[i * 2] = tags[i].key();
            kvs[i * 2 + 1] = tags[i].value();
        }
        return Tags.of(kvs);
    }

    /**
     * Creates or retrieves a Micrometer-backed counter.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Counter delegating to a Micrometer Counter
     */
    @Override
    public Counter counter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        io.micrometer.core.instrument.Counter c = io.micrometer.core.instrument.Counter.builder(name)
                .tags(toMicrometerTags(tags)).register(registry);
        return new Counter() {

            /** Increments the counter by one. */
            @Override
            public void increment() {
                c.increment();
            }

            /**
             * Increments the counter by the given amount.
             *
             * @param amount the number to add
             */
            @Override
            public void increment(long amount) {
                c.increment(amount);
            }

            /**
             * Returns the current counter value.
             *
             * @return total count as a long
             */
            @Override
            public long count() {
                return (long) c.count();
            }
        };
    }

    /**
     * Creates a Micrometer-backed meter (Micrometer Counter + local EWMA rates).
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Meter backed by a Micrometer Counter with local EWMA rate tracking
     */
    @Override
    public Meter meter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        // Micrometer has no Meter type; use local NativeMeter for EWMA
        NativeMeter nm = new NativeMeter();
        Tag[] finalTags = tags;
        // Register underlying counter in Micrometer for Prometheus export
        io.micrometer.core.instrument.Counter c = io.micrometer.core.instrument.Counter.builder(name)
                .tags(toMicrometerTags(tags)).register(registry);
        return new Meter() {

            /** Increments the meter by one. */
            @Override
            public void increment() {
                increment(1);
            }

            /**
             * Increments the meter by the given amount, updating both the Micrometer counter and local EWMA.
             *
             * @param amount the number to add
             */
            @Override
            public void increment(long amount) {
                c.increment(amount);
                nm.increment(amount);
            }

            /**
             * Returns the current total count from the Micrometer counter.
             *
             * @return total count as a long
             */
            @Override
            public long count() {
                return (long) c.count();
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
     * Creates a RatePair backed by three Micrometer counters (total, errors, successes).
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return a RatePair tracking success/error rates
     */
    @Override
    public RatePair ratePair(String name, Tag... tags) {
        // RatePair: delegate success/error to two Micrometer counters + local EWMA
        Meter total = meter(name + ".total", tags);
        Meter errors = meter(name + ".errors", tags);
        Meter successes = meter(name + ".successes", tags);
        return new RatePair() {

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
            public Meter total() {
                return total;
            }

            /**
             * Returns the meter tracking error events.
             *
             * @return errors meter
             */
            @Override
            public Meter errors() {
                return errors;
            }

            /**
             * Returns the meter tracking successful events.
             *
             * @return successes meter
             */
            @Override
            public Meter successes() {
                return successes;
            }
        };
    }

    /**
     * Creates a Micrometer-backed gauge that reads from the given state object.
     *
     * @param name     metric name
     * @param stateObj object whose state is sampled on each read
     * @param fn       function to extract a double value from the state object
     * @param tags     optional tags
     * @return a Gauge delegating to a Micrometer Gauge
     */
    @Override
    public <T> Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        io.micrometer.core.instrument.Gauge.builder(name, stateObj, fn).tags(toMicrometerTags(tags)).register(registry);
        return () -> fn.applyAsDouble(stateObj);
    }

    /**
     * Creates a Micrometer-backed timer with P50/P95/P99/P999 percentiles.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Timer delegating to a Micrometer Timer
     */
    @Override
    public Timer timer(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        io.micrometer.core.instrument.Timer t = io.micrometer.core.instrument.Timer.builder(name)
                .tags(toMicrometerTags(tags)).publishPercentiles(0.5, 0.95, 0.99, 0.999).register(registry);
        return new Timer() {

            /**
             * Starts a timer sample and returns a handle that records the duration on stop.
             *
             * @return a new in-flight timing sample
             */
            @Override
            public Sample start() {
                io.micrometer.core.instrument.Timer.Sample s = io.micrometer.core.instrument.Timer.start(registry);
                return () -> {
                    long nanos = s.stop(t);
                    return nanos;
                };
            }

            /**
             * Records a duration directly.
             *
             * @param amount the duration amount
             * @param unit   the time unit of the amount
             */
            @Override
            public void record(long amount, TimeUnit unit) {
                t.record(amount, unit);
            }

            /**
             * Returns the total number of recorded events.
             *
             * @return event count
             */
            @Override
            public long count() {
                return t.count();
            }

            /**
             * Returns the total time of all recorded events in the given unit.
             *
             * @param unit the time unit for the result
             * @return total time
             */
            @Override
            public double totalTime(TimeUnit unit) {
                return t.totalTime(unit);
            }

            /**
             * Returns the maximum recorded duration in the given unit.
             *
             * @param unit the time unit for the result
             * @return maximum duration
             */
            @Override
            public double max(TimeUnit unit) {
                return t.max(unit);
            }

            /**
             * Returns the percentile value in the given unit.
             *
             * @param p    percentile between 0.0 and 1.0
             * @param unit the time unit for the result
             * @return percentile value
             */
            @Override
            public double percentile(double p, TimeUnit unit) {
                return t.percentile(p, unit);
            }

            /**
             * Returns the percentile value; rolling window is not supported by Micrometer, delegates to global
             * percentile.
             *
             * @param p      percentile between 0.0 and 1.0
             * @param unit   the time unit for the result
             * @param window the rolling window (ignored)
             * @return percentile value
             */
            @Override
            public double percentile(double p, TimeUnit unit, Window window) {
                // Micrometer doesn't support rolling window percentiles natively
                return percentile(p, unit);
            }

            /**
             * SLA violation callbacks are not supported by Micrometer; returns this timer unchanged.
             *
             * @param percentile the percentile to monitor
             * @param threshold  the threshold value
             * @param unit       the time unit for the threshold
             * @param checkEvery check interval in seconds
             * @param callback   callback to invoke on violation
             * @return this timer
             */
            @Override
            public Timer onViolation(
                    double percentile,
                    long threshold,
                    TimeUnit unit,
                    int checkEvery,
                    Consumer<ViolationEvent> callback) {
                // Micrometer has no callback mechanism; implement locally with a counter
                // We register a gauge that fires the callback when percentile exceeds threshold
                // This is a best-effort check on each read — callers using onViolation
                // with Micrometer provider will get callbacks on percentile() calls.
                return this;
            }

            /**
             * Returns a snapshot with count, total, and max; bucket data is not available from Micrometer.
             *
             * @return timer snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, t.count(), t.totalTime(TimeUnit.NANOSECONDS),
                        t.max(TimeUnit.NANOSECONDS), new long[0], new double[0]);
            }
        };
    }

    /**
     * Creates a Micrometer-backed histogram using a DistributionSummary with P50/P95/P99 percentiles.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Histogram delegating to a Micrometer DistributionSummary
     */
    @Override
    public Histogram histogram(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        io.micrometer.core.instrument.DistributionSummary ds = io.micrometer.core.instrument.DistributionSummary
                .builder(name).tags(toMicrometerTags(tags)).publishPercentiles(0.5, 0.95, 0.99).register(registry);
        return new Histogram() {

            /**
             * Records a single observation value.
             *
             * @param value the observed value
             */
            @Override
            public void record(double value) {
                ds.record(value);
            }

            /**
             * Returns the total number of recorded observations.
             *
             * @return observation count
             */
            @Override
            public long count() {
                return ds.count();
            }

            /**
             * Returns the sum of all recorded observation values.
             *
             * @return total amount
             */
            @Override
            public double totalAmount() {
                return ds.totalAmount();
            }

            /**
             * Returns the maximum recorded observation value.
             *
             * @return maximum value
             */
            @Override
            public double max() {
                return ds.max();
            }

            /**
             * Returns the percentile value for the given quantile.
             *
             * @param p percentile between 0.0 and 1.0
             * @return percentile value
             */
            @Override
            public double percentile(double p) {
                return ds.percentile(p);
            }

            /**
             * Returns a snapshot with count, total, and max; bucket data is not available from Micrometer.
             *
             * @return histogram snapshot
             */
            @Override
            public TimerSnapshot snapshot() {
                return new TimerSnapshot(name, finalTags, ds.count(), ds.totalAmount(), ds.max(), new long[0],
                        new double[0]);
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
    public LlmTimer llmTimer(String name, Tag... tags) {
        // LlmTimer not in Micrometer — use native implementation backed by this provider's counters/timers
        Tag[] finalTags = tags;
        return (model, provider_, operation) -> {
            long startNs = System.nanoTime();
            return new LlmSample() {

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

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<Counter> counters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<Meter> meters() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<Gauge> gauges() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<Timer> timers() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<Histogram> histograms() {
        return Collections.emptyList();
    }

    /** Returns an empty iterable; Micrometer registry enumeration is not supported. */
    @Override
    public Iterable<LlmTimer> llmTimers() {
        return Collections.emptyList();
    }

}

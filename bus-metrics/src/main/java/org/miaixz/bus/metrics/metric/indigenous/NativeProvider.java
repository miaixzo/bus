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
package org.miaixz.bus.metrics.metric.indigenous;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.metric.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Zero-dependency native metrics provider.
 * <p>
 * Features:
 * <ul>
 * <li>T-Digest accurate tail percentiles (P99 error &lt; 1%)</li>
 * <li>EWMA 1m/5m/15m rates on all Meter/RatePair instances</li>
 * <li>Multi-window rolling percentiles (1m/5m/lifetime)</li>
 * <li>CardinalityGuard enforced on every metric registration</li>
 * <li>Single daemon ScheduledExecutorService for all background ticks</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeProvider implements Provider {

    /**
     * Registry of all counters keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeCounter> counters = new ConcurrentHashMap<>();
    /**
     * Registry of all meters keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeMeter> meters = new ConcurrentHashMap<>();
    /**
     * Registry of all rate pairs keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeRatePair> ratePairs = new ConcurrentHashMap<>();
    /**
     * Registry of all gauges keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeGauge<?>> gauges = new ConcurrentHashMap<>();
    /**
     * Registry of all timers keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeTimer> timers = new ConcurrentHashMap<>();
    /**
     * Registry of all histograms keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeHistogram> histograms = new ConcurrentHashMap<>();
    /**
     * Registry of all LLM timers keyed by canonical metric key.
     */
    private final ConcurrentHashMap<String, NativeLlmTimer> llmTimers = new ConcurrentHashMap<>();

    /** Shared SLO tracker instance for this provider. */
    private final NativeSloTracker sloTracker = new NativeSloTracker();

    /** Shared daemon scheduler: EWMA tick every 5s, timer rotation every 60s/300s. */
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, Builder.THREAD_NAME_TICK);
        t.setDaemon(true);
        return t;
    });

    /** Counts 5-second ticks; used to derive 60s (12 ticks) and 300s (60 ticks) rotation intervals. */
    private final AtomicInteger tickCount = new AtomicInteger(0);

    /** Creates a new NativeProvider and starts the shared background tick scheduler. */
    public NativeProvider() {
        SCHEDULER.scheduleAtFixedRate(
                this::tick,
                Builder.TICK_INTERVAL_SECONDS,
                Builder.TICK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    /** Advances EWMA tick counters and rotates rolling digest windows on schedule. */
    private void tick() {
        int n = tickCount.incrementAndGet();
        meters.values().forEach(NativeMeter::tick);
        ratePairs.values().forEach(NativeRatePair::tick);
        if (n % Builder.TICKS_PER_1M == 0) {
            timers.values().forEach(NativeTimer::rotate1m);
        }
        if (n % Builder.TICKS_PER_5M == 0) {
            timers.values().forEach(NativeTimer::rotate5m);
        }
    }

    /**
     * Returns or creates a {@link Counter} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the counter instance
     */
    @Override
    public Counter counter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        return counters.computeIfAbsent(key(name, tags), k -> new NativeCounter());
    }

    /**
     * Returns or creates a {@link Meter} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the meter instance
     */
    @Override
    public Meter meter(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        return meters.computeIfAbsent(key(name, tags), k -> new NativeMeter());
    }

    /**
     * Returns or creates a {@link RatePair} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the rate pair instance
     */
    @Override
    public RatePair ratePair(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        return ratePairs.computeIfAbsent(key(name, tags), k -> new NativeRatePair());
    }

    /**
     * Returns or creates a {@link Gauge} backed by a weak reference to {@code stateObj}.
     *
     * @param name     metric name
     * @param stateObj the object whose state is observed
     * @param fn       function that extracts the gauge value from {@code stateObj}
     * @param tags     optional tags
     * @param <T>      state object type
     * @return the gauge instance
     */
    @Override
    public <T> Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        return gauges.computeIfAbsent(key(name, tags), k -> new NativeGauge<>(stateObj, fn));
    }

    /**
     * Returns or creates a {@link Timer} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the timer instance
     */
    @Override
    public Timer timer(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        return timers.computeIfAbsent(key(name, tags), k -> new NativeTimer(name, finalTags));
    }

    /**
     * Returns or creates a {@link Histogram} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the histogram instance
     */
    @Override
    public Histogram histogram(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        return histograms.computeIfAbsent(key(name, tags), k -> new NativeHistogram(name, finalTags));
    }

    /**
     * Returns or creates a {@link LlmTimer} for the given name and tags.
     *
     * @param name metric name
     * @param tags optional tags
     * @return the LLM timer instance
     */
    @Override
    public LlmTimer llmTimer(String name, Tag... tags) {
        tags = CardinalityGuard.enforce(name, tags);
        Tag[] finalTags = tags;
        NativeProvider self = this;
        return llmTimers.computeIfAbsent(key(name, tags), k -> new NativeLlmTimer(name, finalTags, self));
    }

    /**
     * Returns the shared {@link org.miaixz.bus.metrics.observe.slo.SloTracker} instance.
     */
    @Override
    public SloTracker sloTracker() {
        return sloTracker;
    }

    /**
     * Returns all registered counters.
     */
    @Override
    public Iterable<Counter> counters() {
        return (Collection) counters.values();
    }

    /**
     * Returns all registered meters.
     */
    @Override
    public Iterable<Meter> meters() {
        return (Collection) meters.values();
    }

    /**
     * Returns all registered gauges.
     */
    @Override
    public Iterable<Gauge> gauges() {
        return (Collection) gauges.values();
    }

    /**
     * Returns all registered timers.
     */
    @Override
    public Iterable<Timer> timers() {
        return (Collection) timers.values();
    }

    /**
     * Returns all registered histograms.
     */
    @Override
    public Iterable<Histogram> histograms() {
        return (Collection) histograms.values();
    }

    /**
     * Returns all registered LLM timers.
     */
    @Override
    public Iterable<LlmTimer> llmTimers() {
        return (Collection) llmTimers.values();
    }

    // ── Registry key ──────────────────────────────────────────────────────

    /**
     * Builds a canonical registry key: {@code name{k1="v1",k2="v2"}} with tags sorted by key.
     */
    static String key(String name, Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return name + "{}";
        }
        Tag[] sorted = tags.clone();
        Arrays.sort(sorted, Comparator.comparing(Tag::key));
        StringBuilder sb = new StringBuilder(name).append('{');
        for (int i = 0; i < sorted.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(sorted[i]);
        }
        return sb.append('}').toString();
    }

}

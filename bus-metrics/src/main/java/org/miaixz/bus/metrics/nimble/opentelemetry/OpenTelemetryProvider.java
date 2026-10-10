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
package org.miaixz.bus.metrics.nimble.opentelemetry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityViolation;
import org.miaixz.bus.metrics.guard.MetricFamilyKey;
import org.miaixz.bus.metrics.guard.MetricFamilyRegistry;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.nimble.*;
import org.miaixz.bus.metrics.nimble.ObservableMeasurement;
import org.miaixz.bus.metrics.nimble.Timer;
import org.miaixz.bus.metrics.nimble.indigenous.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.*;
import io.opentelemetry.api.metrics.Meter;

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
 */
public class OpenTelemetryProvider implements Provider {

    /**
     * Default instrumentation scope name used when none is specified.
     */
    private static final String DEFAULT_SCOPE = "org.miaixz.bus.metrics";

    /**
     * The OpenTelemetry instance used to obtain the OTel Meter.
     */
    private final OpenTelemetry openTelemetry;

    /**
     * OTel Meter used to create all instrument instances.
     */
    private final Meter otelMeter;

    /**
     * Provider-local cardinality policies and observed values.
     */
    private final CardinalityGuard.Scope cardinalityGuard;

    /**
     * Backend-independent family identity registry.
     */
    private final MetricFamilyRegistry familyRegistry = new MetricFamilyRegistry();

    /**
     * Family leases owned by typed active instruments.
     */
    private final ConcurrentHashMap<String, MetricFamilyRegistry.Lease> activeLeases = new ConcurrentHashMap<>();

    /**
     * Active counter adapters indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, Counter> activeCounters = new ConcurrentHashMap<>();

    /**
     * Active gauge adapters indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, Gauge> activeGauges = new ConcurrentHashMap<>();

    /**
     * Active timer adapters indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, Timer> activeTimers = new ConcurrentHashMap<>();

    /**
     * Active histogram adapters indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, Histogram> activeHistograms = new ConcurrentHashMap<>();

    /**
     * Legacy meters indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, MeterAdapter> meters = new ConcurrentHashMap<>();

    /**
     * Shared rate pairs indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, RatePair> ratePairs = new ConcurrentHashMap<>();

    /**
     * Shared LLM timers indexed by canonical series key.
     */
    private final ConcurrentHashMap<String, LlmTimer> llmTimers = new ConcurrentHashMap<>();

    /**
     * OpenTelemetry callback handles owned by this adapter.
     */
    private final Set<AutoCloseable> callbacks = ConcurrentHashMap.newKeySet();

    /**
     * Observable registrations owned by this adapter.
     */
    private final Set<MetricRegistration> observableRegistrations = ConcurrentHashMap.newKeySet();

    /**
     * Whether this adapter has released its registrations.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Capabilities exposed to binders and endpoints.
     */
    private final ProviderCapabilities capabilities = new ProviderCapabilities(true, Optional.empty(),
            Optional.empty());

    /**
     * Provider self-diagnostics implementation.
     */
    private final MetricDiagnostics diagnostics;
    /**
     * Reentrancy guard preventing a diagnostic metric from recursively reporting its own cardinality decision.
     */
    private final ThreadLocal<Boolean> cardinalityDiagnosticInProgress = ThreadLocal.withInitial(() -> false);

    /**
     * Stable service-level objective tracker.
     */
    private final SloTracker sloTracker = new NativeSloTracker();

    /**
     * Provider-owned scheduler for legacy rate ticks.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Listener that publishes cardinality decisions through provider diagnostics.
     */
    private final Consumer<CardinalityViolation> cardinalityListener;

    /**
     * Creates an OpenTelemetryProvider using the default instrumentation scope {@code "bus-metrics"}.
     *
     * @param openTelemetry the OpenTelemetry instance to obtain meters from
     */
    public OpenTelemetryProvider(OpenTelemetry openTelemetry) {
        this(openTelemetry, DEFAULT_SCOPE, CardinalityGuard.globalScope());
    }

    /**
     * Creates an OpenTelemetryProvider with a custom instrumentation scope.
     *
     * @param openTelemetry        the OpenTelemetry instance to obtain meters from
     * @param instrumentationScope instrumentation scope name used when building the OTel Meter
     */
    public OpenTelemetryProvider(OpenTelemetry openTelemetry, String instrumentationScope) {
        this(openTelemetry, instrumentationScope, CardinalityGuard.globalScope());
    }

    /**
     * Creates an API-only adapter with isolated cardinality state and the standard instrumentation scope.
     *
     * @param openTelemetry    caller-owned OpenTelemetry API instance
     * @param cardinalityGuard provider-local cardinality scope
     */
    public OpenTelemetryProvider(OpenTelemetry openTelemetry, CardinalityGuard.Scope cardinalityGuard) {
        this(openTelemetry, DEFAULT_SCOPE, cardinalityGuard);
    }

    /**
     * Creates the fully specified API adapter.
     *
     * @param openTelemetry        caller-owned OpenTelemetry API instance
     * @param instrumentationScope instrumentation scope name
     * @param cardinalityGuard     provider-local cardinality scope
     */
    private OpenTelemetryProvider(OpenTelemetry openTelemetry, String instrumentationScope,
            CardinalityGuard.Scope cardinalityGuard) {
        Logger.info(
                true,
                "Metrics",
                "OpenTelemetry metrics provider initialization started: openTelemetryClass={}, instrumentationScope={}",
                null == openTelemetry ? null : openTelemetry.getClass().getName(),
                instrumentationScope);
        this.openTelemetry = Objects.requireNonNull(openTelemetry, "OpenTelemetry must not be null");
        this.cardinalityGuard = Objects.requireNonNull(cardinalityGuard, "Cardinality scope must not be null");
        this.otelMeter = openTelemetry.getMeter(instrumentationScope);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, Builder.THREAD_NAME_TICK);
            thread.setDaemon(true);
            return thread;
        });
        this.diagnostics = MetricDiagnostics.create(this);
        this.cardinalityListener = violation -> {
            if (cardinalityDiagnosticInProgress.get()) {
                return;
            }
            cardinalityDiagnosticInProgress.set(true);
            try {
                diagnostics.resourceDropped(
                        "cardinality",
                        "denied".equals(violation.replacedWith()) ? "denied" : "replaced");
            } finally {
                cardinalityDiagnosticInProgress.remove();
            }
        };
        cardinalityGuard.addViolationListener(cardinalityListener);
        scheduler.scheduleAtFixedRate(
                this::tickMeters,
                Builder.TICK_INTERVAL_SECONDS,
                Builder.TICK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
        Logger.info(
                false,
                "Metrics",
                "OpenTelemetry metrics provider initialization finished: openTelemetryClass={}, instrumentationScope={}",
                null == openTelemetry ? null : openTelemetry.getClass().getName(),
                instrumentationScope);
    }

    /**
     * Creates an API adapter from a late-bound optional instance without exposing OpenTelemetry in the caller's
     * signature.
     *
     * @param openTelemetry    caller-owned OpenTelemetry API instance
     * @param cardinalityGuard provider-local cardinality scope
     * @return OpenTelemetry-backed provider
     * @throws NullPointerException if the API instance is {@code null}
     * @throws ClassCastException   if the object does not implement the OpenTelemetry API
     */
    public static Provider fromOpenTelemetry(Object openTelemetry, CardinalityGuard.Scope cardinalityGuard) {
        Object checked = Objects.requireNonNull(openTelemetry, "OpenTelemetry must not be null");
        return new OpenTelemetryProvider(OpenTelemetry.class.cast(checked), cardinalityGuard);
    }

    /**
     * Builds the canonical key for one typed active series.
     *
     * @param descriptor family descriptor
     * @param attributes series attributes
     * @return canonical series key
     */
    private static String activeKey(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        return MetricFamilyKey.identity(descriptor).logical() + attributes.toString();
    }

    /**
     * Validates that a descriptor matches an active instrument API.
     *
     * @param descriptor descriptor to validate
     * @param kind       expected instrument kind
     * @param numberKind expected numeric kind
     */
    private static void validate(MetricDescriptor descriptor, InstrumentKind kind, NumberKind numberKind) {
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        if (descriptor.kind() != kind || descriptor.numberKind() != numberKind) {
            throw new IllegalArgumentException("Metric descriptor is incompatible with active " + kind + " API");
        }
    }

    /**
     * Closes an OpenTelemetry callback handle and normalizes checked failures.
     *
     * @param callback callback handle to close
     */
    private static void closeCallback(AutoCloseable callback) {
        try {
            callback.close();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to close OpenTelemetry metric callback", exception);
        }
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
     * Converts typed Bus attributes to OpenTelemetry attributes.
     *
     * @param attributes Bus attributes
     * @return OpenTelemetry attributes
     */
    private Attributes toAttributes(org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        AttributesBuilder builder = Attributes.builder();
        for (org.miaixz.bus.metrics.observe.tag.Attributes.Value<?> entry : attributes.values()) {
            String key = entry.descriptor().key();
            switch (entry.descriptor().type()) {
                case STRING -> builder.put(key, (String) entry.value());
                case LONG -> builder.put(key, (Long) entry.value());
                case DOUBLE -> builder.put(key, (Double) entry.value());
                case BOOLEAN -> builder.put(key, (Boolean) entry.value());
            }
        }
        return builder.build();
    }

    /**
     * Validates and cardinality-guards a series attribute set.
     *
     * @param descriptor family descriptor
     * @param attributes proposed attributes
     * @return guarded attributes
     */
    private org.miaixz.bus.metrics.observe.tag.Attributes guard(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        descriptor.validateAttributes(attributes);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = cardinalityGuard.enforce(descriptor.name(), attributes);
        descriptor.validateAttributes(guarded);
        return guarded;
    }

    /**
     * Acquires one family lease for an active series when it is first created.
     *
     * @param key        canonical series key
     * @param descriptor family descriptor
     */
    private void acquireActive(String key, MetricDescriptor descriptor) {
        activeLeases.computeIfAbsent(key, ignored -> familyRegistry.acquire(descriptor));
    }

    /**
     * Creates an OTel-backed counter using a {@link LongCounter}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Counter backed by an OTel LongCounter
     */
    @Override
    public Counter counter(String name, Tag... tags) {
        tags = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(tags);
        return counter(MetricDescriptor.counter(name, attributes), attributes);
    }

    @Override
    public Counter counter(MetricDescriptor descriptor, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.COUNTER, NumberKind.LONG);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeCounters.computeIfAbsent(key, ignored -> {
            acquireActive(key, descriptor);
            LongCounter counter = otelMeter.counterBuilder(descriptor.name()).setDescription(descriptor.description())
                    .setUnit(descriptor.unit()).build();
            return new Counter() {

                private long total;

                @Override
                public void increment() {
                    increment(1);
                }

                @Override
                public synchronized void increment(long amount) {
                    if (amount < 0) {
                        throw new IllegalArgumentException("Counter increment must be non-negative");
                    }
                    counter.add(amount, toAttributes(guarded));
                    total += amount;
                }

                @Override
                public synchronized long count() {
                    return total;
                }
            };
        });
    }

    /**
     * Creates an OTel-backed meter (LongCounter + local EWMA rates).
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Meter backed by an OTel LongCounter with local EWMA rate tracking
     */
    @Override
    public org.miaixz.bus.metrics.nimble.Meter meter(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        String key = name + org.miaixz.bus.metrics.observe.tag.Attributes.fromTags(guarded);
        return meters.computeIfAbsent(
                key,
                ignored -> new MeterAdapter(otelMeter.counterBuilder(name).build(), toAttributes(guarded)));
    }

    /**
     * Creates a RatePair backed by three OTel counters (total, errors, successes).
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return a RatePair tracking success/error rates
     */
    @Override
    public RatePair ratePair(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        String key = name + org.miaixz.bus.metrics.observe.tag.Attributes.fromTags(guarded);
        return ratePairs.computeIfAbsent(key, ignored -> RatePair.create(this, name, guarded));
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
    public <T> Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        tags = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(tags);
        return gauge(MetricDescriptor.gauge(name, attributes), attributes, stateObj, fn);
    }

    @Override
    public <T> Gauge gauge(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes,
            T stateObj,
            ToDoubleFunction<T> fn) {
        validate(descriptor, InstrumentKind.GAUGE, NumberKind.DOUBLE);
        Objects.requireNonNull(stateObj, "Gauge state must not be null");
        Objects.requireNonNull(fn, "Gauge function must not be null");
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeGauges.computeIfAbsent(key, ignored -> {
            acquireActive(key, descriptor);
            Gauge local = () -> {
                return fn.applyAsDouble(stateObj);
            };
            ObservableDoubleGauge callback = otelMeter.gaugeBuilder(descriptor.name())
                    .setDescription(descriptor.description()).setUnit(descriptor.unit())
                    .buildWithCallback(observation -> {
                        double value = local.value();
                        if (Double.isFinite(value)) {
                            observation.record(value, toAttributes(guarded));
                        }
                    });
            callbacks.add(callback);
            return local;
        });
    }

    /**
     * Creates an OTel-backed timer using a {@link DoubleHistogram} with unit {@code "s"}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Timer backed by an OTel DoubleHistogram
     */
    @Override
    public Timer timer(String name, Tag... tags) {
        tags = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(tags);
        return timer(MetricDescriptor.timer(name, attributes), attributes);
    }

    @Override
    public Timer timer(MetricDescriptor descriptor, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.TIMER, NumberKind.DOUBLE);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeTimers.computeIfAbsent(key, ignored -> {
            acquireActive(key, descriptor);
            DoubleHistogram h = otelMeter.histogramBuilder(descriptor.name()).setDescription(descriptor.description())
                    .setUnit(descriptor.unit()).build();
            NativeTimer mirror = new NativeTimer(descriptor.name(), guarded.toTags());
            return new Timer() {

                @Override
                public Sample start() {
                    long t0 = System.nanoTime();
                    return () -> {
                        long nanos = System.nanoTime() - t0;
                        record(nanos, TimeUnit.NANOSECONDS);
                        return nanos;
                    };
                }

                @Override
                public void record(long amount, TimeUnit unit) {
                    if (amount < 0) {
                        throw new IllegalArgumentException("Timer amount must be non-negative");
                    }
                    Objects.requireNonNull(unit, "Timer unit must not be null");
                    h.record(unit.toNanos(amount) / 1e9, toAttributes(guarded));
                    mirror.record(amount, unit);
                }

                @Override
                public long count() {
                    return mirror.count();
                }

                @Override
                public double totalTime(TimeUnit unit) {
                    return mirror.totalTime(unit);
                }

                @Override
                public double max(TimeUnit unit) {
                    return mirror.max(unit);
                }

                @Override
                public double percentile(double p, TimeUnit unit) {
                    return mirror.percentile(p, unit);
                }

                @Override
                public double percentile(double p, TimeUnit unit, Window window) {
                    return mirror.percentile(p, unit, window);
                }

                @Override
                public Timer onViolation(
                        double percentile,
                        long threshold,
                        TimeUnit unit,
                        int checkEvery,
                        ConsumerX<ViolationEvent> callback) {
                    mirror.onViolation(percentile, threshold, unit, checkEvery, callback);
                    return this;
                }

                @Override
                public TimerSnapshot snapshot() {
                    return mirror.snapshot();
                }
            };
        });
    }

    /**
     * Creates an OTel-backed histogram using a {@link DoubleHistogram}.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Histogram backed by an OTel DoubleHistogram
     */
    @Override
    public Histogram histogram(String name, Tag... tags) {
        tags = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(tags);
        return histogram(MetricDescriptor.histogram(name, attributes), attributes);
    }

    @Override
    public Histogram histogram(MetricDescriptor descriptor, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeHistograms.computeIfAbsent(key, ignored -> {
            acquireActive(key, descriptor);
            DoubleHistogram h = otelMeter.histogramBuilder(descriptor.name()).setDescription(descriptor.description())
                    .setUnit(descriptor.unit()).build();
            NativeHistogram mirror = new NativeHistogram(descriptor.name(), guarded.toTags());
            return new Histogram() {

                @Override
                public void record(double value) {
                    if (!Double.isFinite(value)) {
                        throw new IllegalArgumentException("Histogram value must be finite");
                    }
                    h.record(value, toAttributes(guarded));
                    mirror.record(value);
                }

                @Override
                public long count() {
                    return mirror.count();
                }

                @Override
                public double totalAmount() {
                    return mirror.totalAmount();
                }

                @Override
                public double max() {
                    return mirror.max();
                }

                @Override
                public double percentile(double p) {
                    return mirror.percentile(p);
                }

                @Override
                public TimerSnapshot snapshot() {
                    return mirror.snapshot();
                }
            };
        });
    }

    /**
     * Creates an LlmTimer following OTel GenAI SIG 2025 naming conventions.
     *
     * @param name metric name prefix
     * @param tags optional tags
     * @return an LlmTimer for tracking LLM request latency, tokens, and errors
     */
    @Override
    public LlmTimer llmTimer(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        String key = name + org.miaixz.bus.metrics.observe.tag.Attributes.fromTags(guarded);
        return llmTimers.computeIfAbsent(key, ignored -> new NativeLlmTimer(name, guarded, this));
    }

    @Override
    public MetricRegistration registerObservable(MetricDescriptor descriptor, ObservableCallback callback) {
        ensureOpen();
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        Objects.requireNonNull(callback, "Observable callback must not be null");
        if (descriptor.kind() == InstrumentKind.TIMER || descriptor.kind() == InstrumentKind.HISTOGRAM) {
            throw new IllegalArgumentException("Observable distributions are not supported: " + descriptor.name());
        }
        MetricFamilyRegistry.Lease lease = familyRegistry.acquire(descriptor);
        OtelRegistration registration = new OtelRegistration(descriptor, callback, lease);
        try {
            registration.initialize();
            observableRegistrations.add(registration);
            return registration;
        } catch (RuntimeException exception) {
            registration.close();
            throw exception;
        }
    }

    @Override
    public MetricDiagnostics diagnostics() {
        return diagnostics;
    }

    @Override
    public ProviderCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cardinalityGuard.removeViolationListener(cardinalityListener);
        scheduler.shutdownNow();
        new ArrayList<>(observableRegistrations).forEach(MetricRegistration::close);
        new ArrayList<>(callbacks).forEach(OpenTelemetryProvider::closeCallback);
        callbacks.clear();
        activeLeases.values().forEach(MetricFamilyRegistry.Lease::close);
        activeLeases.clear();
        activeCounters.clear();
        activeGauges.clear();
        activeTimers.clear();
        activeHistograms.clear();
        meters.clear();
        ratePairs.clear();
        llmTimers.clear();
    }

    /**
     * Ensures that the provider still accepts operations.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("OpenTelemetry metrics provider is closed");
        }
    }

    /**
     * Returns the stable provider-local SLO tracker.
     *
     * @return provider-local SLO tracker
     */
    @Override
    public SloTracker sloTracker() {
        return sloTracker;
    }

    /**
     * Returns the provider-owned legacy meters.
     */
    @Override
    public Iterable<Counter> counters() {
        return List.copyOf(activeCounters.values());
    }

    /**
     * Returns an empty iterable; OTel registry enumeration is not supported.
     */
    @Override
    public Iterable<org.miaixz.bus.metrics.nimble.Meter> meters() {
        return List.copyOf(meters.values());
    }

    /**
     * Returns the provider-owned LLM timers.
     */
    @Override
    public Iterable<Gauge> gauges() {
        return List.copyOf(activeGauges.values());
    }

    /**
     * Returns an empty iterable; OTel registry enumeration is not supported.
     */
    @Override
    public Iterable<Timer> timers() {
        return List.copyOf(activeTimers.values());
    }

    /**
     * Returns an empty iterable; OTel registry enumeration is not supported.
     */
    @Override
    public Iterable<Histogram> histograms() {
        return List.copyOf(activeHistograms.values());
    }

    /**
     * Returns an empty iterable; OTel registry enumeration is not supported.
     */
    @Override
    public Iterable<LlmTimer> llmTimers() {
        return List.copyOf(llmTimers.values());
    }

    /**
     * Advances every provider-owned rate meter.
     */
    private void tickMeters() {
        if (!closed.get()) {
            meters.values().forEach(MeterAdapter::tick);
        }
    }

    /**
     * Bus rate meter backed by an OpenTelemetry counter and native rate mirror.
     */
    private static final class MeterAdapter implements org.miaixz.bus.metrics.nimble.Meter {

        /**
         * OpenTelemetry counter receiving cumulative updates.
         */
        private final LongCounter counter;
        /**
         * Attributes attached to counter updates.
         */
        private final Attributes attributes;
        /**
         * Native meter providing cumulative and rolling reads.
         */
        private final NativeMeter rates = new NativeMeter();

        /**
         * Creates a rate adapter.
         *
         * @param counter    backing OpenTelemetry counter
         * @param attributes attributes attached to updates
         */
        private MeterAdapter(LongCounter counter, Attributes attributes) {
            this.counter = counter;
            this.attributes = attributes;
        }

        @Override
        public void increment() {
            increment(1);
        }

        @Override
        public void increment(long amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Meter increment must be non-negative");
            }
            counter.add(amount, attributes);
            rates.increment(amount);
        }

        @Override
        public long count() {
            return rates.count();
        }

        @Override
        public double oneMinuteRate() {
            return rates.oneMinuteRate();
        }

        @Override
        public double fiveMinuteRate() {
            return rates.fiveMinuteRate();
        }

        @Override
        public double fifteenMinuteRate() {
            return rates.fifteenMinuteRate();
        }

        @Override
        public double meanRate() {
            return rates.meanRate();
        }

        /**
         * Advances the EWMA windows by one five-second interval.
         */
        private void tick() {
            rates.tick();
        }
    }

    /**
     * Provider-owned asynchronous OpenTelemetry instrument registration.
     */
    private final class OtelRegistration implements MetricRegistration {

        /**
         * Registered family descriptor.
         */
        private final MetricDescriptor descriptor;
        /**
         * User callback invoked by the OpenTelemetry SDK.
         */
        private final ObservableCallback callback;
        /**
         * Lease protecting the registered family identity.
         */
        private final MetricFamilyRegistry.Lease lease;
        /**
         * Whether this registration has been closed.
         */
        private final AtomicBoolean registrationClosed = new AtomicBoolean();

        /**
         * Attribute sets established during initialization.
         */
        private volatile Set<org.miaixz.bus.metrics.observe.tag.Attributes> seeded = Set.of();
        /**
         * OpenTelemetry callback handle owned by this registration.
         */
        private AutoCloseable handle;

        /**
         * Creates an asynchronous instrument registration.
         *
         * @param descriptor family descriptor
         * @param callback   observation callback
         * @param lease      family identity lease
         */
        private OtelRegistration(MetricDescriptor descriptor, ObservableCallback callback,
                MetricFamilyRegistry.Lease lease) {
            this.descriptor = descriptor;
            this.callback = callback;
            this.lease = lease;
        }

        /**
         * Seeds resource identities and builds the asynchronous instrument.
         */
        private void initialize() {
            BusMeasurement seed = new BusMeasurement(descriptor, null, null, null);
            callback.observe(seed);
            seeded = Set.copyOf(seed.values.keySet());
            handle = buildObservable();
            callbacks.add(handle);
        }

        /**
         * Builds the OpenTelemetry asynchronous instrument matching the descriptor.
         *
         * @return closeable callback handle
         */
        private AutoCloseable buildObservable() {
            return switch (descriptor.kind()) {
                case COUNTER -> descriptor.numberKind() == NumberKind.LONG
                        ? otelMeter.counterBuilder(descriptor.name()).setDescription(descriptor.description())
                                .setUnit(descriptor.unit()).buildWithCallback(this::collectLong)
                        : otelMeter.counterBuilder(descriptor.name()).ofDoubles()
                                .setDescription(descriptor.description()).setUnit(descriptor.unit())
                                .buildWithCallback(this::collectDouble);
                case UP_DOWN_COUNTER -> descriptor.numberKind() == NumberKind.LONG
                        ? otelMeter.upDownCounterBuilder(descriptor.name()).setDescription(descriptor.description())
                                .setUnit(descriptor.unit()).buildWithCallback(this::collectLong)
                        : otelMeter.upDownCounterBuilder(descriptor.name()).ofDoubles()
                                .setDescription(descriptor.description()).setUnit(descriptor.unit())
                                .buildWithCallback(this::collectDouble);
                case GAUGE -> descriptor.numberKind() == NumberKind.LONG
                        ? otelMeter.gaugeBuilder(descriptor.name()).ofLongs().setDescription(descriptor.description())
                                .setUnit(descriptor.unit()).buildWithCallback(this::collectLong)
                        : otelMeter.gaugeBuilder(descriptor.name()).setDescription(descriptor.description())
                                .setUnit(descriptor.unit()).buildWithCallback(this::collectDouble);
                case TIMER, HISTOGRAM -> throw new IllegalArgumentException("Observable distributions are unsupported");
            };
        }

        /**
         * Collects long-valued observations into OpenTelemetry.
         *
         * @param measurement OpenTelemetry long measurement
         */
        private void collectLong(ObservableLongMeasurement measurement) {
            try {
                callback.observe(new BusMeasurement(descriptor, seeded, measurement, null));
            } catch (RuntimeException exception) {
                diagnostics.collectionError("opentelemetry", "callback");
            }
        }

        /**
         * Collects double-valued observations into OpenTelemetry.
         *
         * @param measurement OpenTelemetry double measurement
         */
        private void collectDouble(ObservableDoubleMeasurement measurement) {
            try {
                callback.observe(new BusMeasurement(descriptor, seeded, null, measurement));
            } catch (RuntimeException exception) {
                diagnostics.collectionError("opentelemetry", "callback");
            }
        }

        @Override
        public void close() {
            if (registrationClosed.compareAndSet(false, true)) {
                observableRegistrations.remove(this);
                if (handle != null) {
                    callbacks.remove(handle);
                    closeCallback(handle);
                }
                lease.close();
            }
        }
    }

    /**
     * Measurement adapter supplied to one Bus observable callback.
     */
    private final class BusMeasurement implements ObservableMeasurement {

        /**
         * Descriptor that determines valid point shape.
         */
        private final MetricDescriptor descriptor;
        /**
         * Seeded resource identities, or {@code null} while seeding.
         */
        private final Set<org.miaixz.bus.metrics.observe.tag.Attributes> allowed;
        /**
         * OpenTelemetry destination for long values, when applicable.
         */
        private final ObservableLongMeasurement longMeasurement;
        /**
         * OpenTelemetry destination for double values, when applicable.
         */
        private final ObservableDoubleMeasurement doubleMeasurement;
        /**
         * Values observed in the current callback, indexed by attributes.
         */
        private final Map<org.miaixz.bus.metrics.observe.tag.Attributes, Number> values = new LinkedHashMap<>();

        /**
         * Creates an observable measurement adapter.
         *
         * @param descriptor        family descriptor
         * @param allowed           seeded resource identities, or {@code null} while seeding
         * @param longMeasurement   OpenTelemetry long destination, or {@code null}
         * @param doubleMeasurement OpenTelemetry double destination, or {@code null}
         */
        private BusMeasurement(MetricDescriptor descriptor, Set<org.miaixz.bus.metrics.observe.tag.Attributes> allowed,
                ObservableLongMeasurement longMeasurement, ObservableDoubleMeasurement doubleMeasurement) {
            this.descriptor = descriptor;
            this.allowed = allowed;
            this.longMeasurement = longMeasurement;
            this.doubleMeasurement = doubleMeasurement;
        }

        @Override
        public void recordLong(long value, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.LONG) {
                diagnostics.collectionError("opentelemetry", "number_kind");
                return;
            }
            if (accept(attributes, value) && longMeasurement != null) {
                longMeasurement.record(value, toAttributes(attributes));
            }
        }

        @Override
        public void recordDouble(double value, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.DOUBLE || !Double.isFinite(value)) {
                diagnostics.collectionError("opentelemetry", "invalid_double");
                return;
            }
            if (accept(attributes, value) && doubleMeasurement != null) {
                doubleMeasurement.record(value, toAttributes(attributes));
            }
        }

        /**
         * Validates a point and reserves its attribute identity for this callback.
         *
         * @param attributes point attributes
         * @param value      numeric point value
         * @return whether the point may be forwarded
         */
        private boolean accept(org.miaixz.bus.metrics.observe.tag.Attributes attributes, Number value) {
            try {
                descriptor.validateAttributes(attributes);
                if (allowed != null && !allowed.contains(attributes)) {
                    diagnostics.resourceDropped("opentelemetry", "inventory_changed");
                    return false;
                }
                if (values.putIfAbsent(attributes, value) != null) {
                    diagnostics.collectionError("opentelemetry", "duplicate_attributes");
                    return false;
                }
                return true;
            } catch (RuntimeException exception) {
                diagnostics.collectionError("opentelemetry", "invalid_point");
                return false;
            }
        }
    }

}

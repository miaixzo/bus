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
package org.miaixz.bus.metrics.nimble.prometheus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityViolation;
import org.miaixz.bus.metrics.guard.MetricFamilyRegistry;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.nimble.*;
import org.miaixz.bus.metrics.nimble.Timer;
import org.miaixz.bus.metrics.nimble.indigenous.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;

/**
 * Provider implementation backed by the Prometheus Java client SDK.
 * <p>
 * Uses {@link PrometheusRegistry} as the underlying registry. Counter, Gauge, Histogram, and Summary (for
 * timers/percentiles) are mapped to their native Prometheus equivalents.
 * <p>
 * Meter (EWMA rates) and LlmTimer are implemented locally since the Prometheus SDK has no equivalent types.
 *
 * @author Kimi Liu
 */
public class PrometheusProvider implements Provider {

    /**
     * The Prometheus registry used to register all metric instruments.
     */
    private final PrometheusRegistry registry;

    /**
     * Provider-local cardinality policies and observed values.
     */
    private final CardinalityGuard.Scope cardinalityGuard;

    /**
     * Backend-independent family identity registry with Prometheus normalization.
     */
    private final MetricFamilyRegistry familyRegistry = new MetricFamilyRegistry(
            NativePrometheusTextEncoder::exportName, NativePrometheusTextEncoder::normalizeName);

    /**
     * Counter collectors indexed by normalized family identity.
     */
    private final ConcurrentHashMap<String, Counter> counterFamilies = new ConcurrentHashMap<>();

    /**
     * Callback gauge collectors indexed by canonical logical family.
     */
    private final ConcurrentHashMap<org.miaixz.bus.metrics.guard.MetricFamilyKey.Logical, GaugeFamily> callbackGaugeFamilies = new ConcurrentHashMap<>();

    /**
     * Summary collectors indexed by normalized family identity.
     */
    private final ConcurrentHashMap<String, Summary> summaryFamilies = new ConcurrentHashMap<>();

    /**
     * Histogram collectors indexed by normalized family identity.
     */
    private final ConcurrentHashMap<String, Histogram> histogramFamilies = new ConcurrentHashMap<>();

    /**
     * Family leases owned by active and legacy instruments.
     */
    private final ConcurrentHashMap<String, MetricFamilyRegistry.Lease> activeLeases = new ConcurrentHashMap<>();

    /**
     * Collectors created and therefore unregisterable by this adapter.
     */
    private final Set<Collector> ownedCollectors = ConcurrentHashMap.newKeySet();

    /**
     * Live observable collector registrations.
     */
    private final Set<MetricRegistration> observableRegistrations = ConcurrentHashMap.newKeySet();

    /**
     * Active Bus counters indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, org.miaixz.bus.metrics.nimble.Counter> activeCounters = new ConcurrentHashMap<>();
    /**
     * Active Bus gauges indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, org.miaixz.bus.metrics.nimble.Gauge> activeGauges = new ConcurrentHashMap<>();
    /**
     * Active Bus timers indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, Timer> activeTimers = new ConcurrentHashMap<>();
    /**
     * Active Bus histograms indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, org.miaixz.bus.metrics.nimble.Histogram> activeHistograms = new ConcurrentHashMap<>();
    /**
     * Legacy meters indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, MeterAdapter> meters = new ConcurrentHashMap<>();
    /**
     * Shared rate pairs indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, RatePair> ratePairs = new ConcurrentHashMap<>();
    /**
     * Shared LLM timers indexed by canonical series identity.
     */
    private final ConcurrentHashMap<String, LlmTimer> llmTimers = new ConcurrentHashMap<>();

    /**
     * Whether this adapter has released its registrations.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

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
     * Capabilities exposed to binders and endpoints.
     */
    private final ProviderCapabilities capabilities;

    /**
     * Creates a PrometheusProvider backed by the default Prometheus registry.
     */
    public PrometheusProvider() {
        this(PrometheusRegistry.defaultRegistry, CardinalityGuard.globalScope());
    }

    /**
     * Creates a PrometheusProvider backed by the given registry.
     *
     * @param registry the Prometheus registry to register metrics into
     */
    public PrometheusProvider(PrometheusRegistry registry) {
        this(registry, CardinalityGuard.globalScope());
    }

    /**
     * Creates a provider with caller-owned registry and isolated cardinality state.
     *
     * @param registry         caller-owned registry
     * @param cardinalityGuard provider-local cardinality scope
     */
    public PrometheusProvider(PrometheusRegistry registry, CardinalityGuard.Scope cardinalityGuard) {
        Logger.info(
                true,
                "Metrics",
                "Prometheus metrics provider initialization started: registryClass={}",
                null == registry ? null : registry.getClass().getName());
        this.registry = Objects.requireNonNull(registry, "PrometheusRegistry must not be null");
        this.cardinalityGuard = Objects.requireNonNull(cardinalityGuard, "Cardinality scope must not be null");
        PrometheusSnapshotTextEncoder encoder = new PrometheusSnapshotTextEncoder();
        ScrapeSupport scrape = new ScrapeSupport() {

            @Override
            public String scrape() {
                return encoder.encode(PrometheusProvider.this.registry.scrape());
            }

            @Override
            public String contentType() {
                return Builder.PROMETHEUS_CONTENT_TYPE;
            }
        };
        this.capabilities = new ProviderCapabilities(true, Optional.of(scrape), Optional.empty());
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
                "Prometheus metrics provider initialization finished: registryClass={}",
                null == registry ? null : registry.getClass().getName());
    }

    /**
     * Creates an adapter from a late-bound optional registry without exposing Prometheus in the caller's signature.
     *
     * @param registry         caller-owned Prometheus registry
     * @param cardinalityGuard provider-local cardinality scope
     * @return Prometheus-backed provider
     * @throws NullPointerException if the registry is {@code null}
     * @throws ClassCastException   if the object is not a Prometheus registry
     */
    public static Provider fromRegistry(Object registry, CardinalityGuard.Scope cardinalityGuard) {
        Object checked = Objects.requireNonNull(registry, "PrometheusRegistry must not be null");
        return new PrometheusProvider(PrometheusRegistry.class.cast(checked), cardinalityGuard);
    }

    /**
     * Creates an adapter with a provider-private registry for an explicitly selected Prometheus backend.
     *
     * @param cardinalityGuard provider-local cardinality scope
     * @return Prometheus-backed provider with a private registry
     */
    public static Provider withPrivateRegistry(CardinalityGuard.Scope cardinalityGuard) {
        return new PrometheusProvider(new PrometheusRegistry(), cardinalityGuard);
    }

    /**
     * Returns a stable copy of tags sorted by label name.
     *
     * @param tags source tags
     * @return sorted tag copy
     */
    private static Tag[] sortedTags(Tag[] tags) {
        Tag[] sorted = tags.clone();
        java.util.Arrays.sort(sorted, java.util.Comparator.comparing(Tag::key));
        return sorted;
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
     * Builds the canonical key for one typed active series.
     *
     * @param descriptor family descriptor
     * @param attributes series attributes
     * @return canonical series key
     */
    private static String activeKey(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        return org.miaixz.bus.metrics.guard.MetricFamilyKey.identity(descriptor).logical() + attributes.toString();
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
        if (tags == null || tags.length == 0) {
            return Normal.EMPTY_STRING_ARRAY;
        }
        Tag[] sorted = sortedTags(tags);
        String[] names = new String[sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            names[i] = sorted[i].key();
        }
        return names;
    }

    /**
     * Extracts label values from a tag array.
     *
     * @param tags the tags to extract values from
     * @return array of tag value strings
     */
    private String[] labelValues(Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return Normal.EMPTY_STRING_ARRAY;
        }
        Tag[] sorted = sortedTags(tags);
        String[] values = new String[sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            values[i] = sorted[i].value();
        }
        return values;
    }

    /**
     * Builds a family key from the normalized name and sorted label names.
     *
     * @param name metric name
     * @param tags metric tags
     * @return normalized family key
     */
    private String familyKey(String name, Tag[] tags) {
        return prometheusName(name) + java.util.Arrays.toString(labelNames(tags));
    }

    /**
     * Converts a metric name to a valid Prometheus metric name by replacing dots and hyphens with underscores.
     *
     * @param name the original metric name
     * @return Prometheus-compatible metric name
     */
    private String prometheusName(String name) {
        return name.replace(Symbol.C_DOT, Symbol.C_UNDERLINE).replace(Symbol.C_MINUS, Symbol.C_UNDERLINE);
    }

    /**
     * Acquires one family lease when a descriptor is first observed.
     *
     * @param descriptor family descriptor
     */
    private void track(MetricDescriptor descriptor) {
        String key = descriptor.scope() + '|' + descriptor.name() + '|' + descriptor.kind() + '|'
                + descriptor.numberKind() + '|' + descriptor.unit() + '|' + descriptor.attributes();
        activeLeases.computeIfAbsent(key, ignored -> familyRegistry.acquire(descriptor));
    }

    /**
     * Validates and cardinality-guards a typed attribute set.
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
     * Creates or retrieves a Prometheus-backed counter.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Counter backed by a Prometheus Counter
     */
    @Override
    public org.miaixz.bus.metrics.nimble.Counter counter(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(guarded);
        return counter(MetricDescriptor.counter(name, attributes), attributes);
    }

    @Override
    public org.miaixz.bus.metrics.nimble.Counter counter(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.COUNTER, NumberKind.LONG);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeCounters.computeIfAbsent(key, ignored -> {
            ensureOpen();
            track(descriptor);
            Tag[] finalTags = guarded.toTags();
            Counter collector = counterFamilies.computeIfAbsent(familyKey(descriptor.name(), finalTags), family -> {
                Counter created = Counter.builder().name(NativePrometheusTextEncoder.exportName(descriptor))
                        .help(descriptor.description()).labelNames(labelNames(finalTags)).register(registry);
                ownedCollectors.add(created);
                return created;
            });
            return new org.miaixz.bus.metrics.nimble.Counter() {

                @Override
                public void increment() {
                    increment(1);
                }

                @Override
                public void increment(long amount) {
                    if (amount < 0) {
                        throw new IllegalArgumentException("Counter increment must be non-negative");
                    }
                    collector.labelValues(labelValues(finalTags)).inc(amount);
                }

                @Override
                public long count() {
                    return collector.labelValues(labelValues(finalTags)).getLongValue();
                }
            };
        });
    }

    /**
     * Creates or retrieves a Prometheus-backed meter (counter + local EWMA rates).
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Meter backed by a Prometheus Counter with local EWMA rate tracking
     */
    @Override
    public Meter meter(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        String key = name + org.miaixz.bus.metrics.observe.tag.Attributes.fromTags(guarded);
        return meters.computeIfAbsent(key, ignored -> {
            org.miaixz.bus.metrics.nimble.Counter counter = counter(name, guarded);
            return new MeterAdapter(counter);
        });
    }

    /**
     * Creates a RatePair backed by three Prometheus counters (total, errors, successes).
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
     * Creates a Prometheus-backed gauge that reads from the given state object.
     *
     * @param name     metric name
     * @param stateObj object whose state is sampled on each read
     * @param fn       function to extract a double value from the state object
     * @param tags     optional tags
     * @return a Gauge backed by a Prometheus Gauge
     */
    @Override
    public <T> org.miaixz.bus.metrics.nimble.Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(guarded);
        return gauge(MetricDescriptor.gauge(name, attributes), attributes, stateObj, fn);
    }

    @Override
    public <T> org.miaixz.bus.metrics.nimble.Gauge gauge(
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
            ensureOpen();
            track(descriptor);
            org.miaixz.bus.metrics.guard.MetricFamilyKey.Logical logical = org.miaixz.bus.metrics.guard.MetricFamilyKey
                    .identity(descriptor).logical();
            GaugeFamily family = callbackGaugeFamilies.computeIfAbsent(logical, unused -> {
                GaugeFamily created = new GaugeFamily(descriptor);
                registry.register(created);
                ownedCollectors.add(created);
                return created;
            });
            org.miaixz.bus.metrics.nimble.Gauge result = () -> fn.applyAsDouble(stateObj);
            family.add(guarded, result);
            return result;
        });
    }

    /**
     * Creates a Prometheus-backed timer using a Summary with P50/P95/P99/P999 quantiles.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Timer backed by a Prometheus Summary
     */
    @Override
    public Timer timer(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(guarded);
        return timer(MetricDescriptor.timer(name, attributes), attributes);
    }

    @Override
    public Timer timer(MetricDescriptor descriptor, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.TIMER, NumberKind.DOUBLE);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeTimers.computeIfAbsent(key, ignored -> {
            ensureOpen();
            track(descriptor);
            Tag[] finalTags = guarded.toTags();
            Summary summary = summaryFamilies.computeIfAbsent(
                    NativePrometheusTextEncoder.exportName(descriptor) + Arrays.toString(labelNames(finalTags)),
                    family -> {
                        Summary created = Summary.builder().name(NativePrometheusTextEncoder.exportName(descriptor))
                                .help(descriptor.description()).labelNames(labelNames(finalTags)).quantile(0.5, 0.05)
                                .quantile(0.95, 0.01).quantile(0.99, 0.001).quantile(0.999, 0.0001).register(registry);
                        ownedCollectors.add(created);
                        return created;
                    });
            NativeTimer mirror = new NativeTimer(descriptor.name(), finalTags);
            return new Timer() {

                @Override
                public Sample start() {
                    long started = System.nanoTime();
                    return () -> {
                        long duration = System.nanoTime() - started;
                        record(duration, TimeUnit.NANOSECONDS);
                        return duration;
                    };
                }

                @Override
                public void record(long amount, TimeUnit unit) {
                    if (amount < 0) {
                        throw new IllegalArgumentException("Timer amount must be non-negative");
                    }
                    Objects.requireNonNull(unit, "Timer unit must not be null");
                    summary.labelValues(labelValues(finalTags)).observe(unit.toNanos(amount) / 1e9);
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
                public double percentile(double percentile, TimeUnit unit) {
                    return mirror.percentile(percentile, unit);
                }

                @Override
                public double percentile(double percentile, TimeUnit unit, Window window) {
                    return mirror.percentile(percentile, unit, window);
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
     * Creates a Prometheus-backed histogram.
     *
     * @param name metric name
     * @param tags optional tags
     * @return a Histogram backed by a Prometheus Histogram
     */
    @Override
    public org.miaixz.bus.metrics.nimble.Histogram histogram(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        org.miaixz.bus.metrics.observe.tag.Attributes attributes = org.miaixz.bus.metrics.observe.tag.Attributes
                .fromTags(guarded);
        return histogram(MetricDescriptor.histogram(name, attributes), attributes);
    }

    @Override
    public org.miaixz.bus.metrics.nimble.Histogram histogram(
            MetricDescriptor descriptor,
            org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        validate(descriptor, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE);
        org.miaixz.bus.metrics.observe.tag.Attributes guarded = guard(descriptor, attributes);
        String key = activeKey(descriptor, guarded);
        return activeHistograms.computeIfAbsent(key, ignored -> {
            ensureOpen();
            track(descriptor);
            Tag[] finalTags = guarded.toTags();
            Histogram collector = histogramFamilies.computeIfAbsent(
                    NativePrometheusTextEncoder.exportName(descriptor) + Arrays.toString(labelNames(finalTags)),
                    family -> {
                        Histogram created = Histogram.builder().name(NativePrometheusTextEncoder.exportName(descriptor))
                                .help(descriptor.description()).labelNames(labelNames(finalTags)).register(registry);
                        ownedCollectors.add(created);
                        return created;
                    });
            NativeHistogram mirror = new NativeHistogram(descriptor.name(), finalTags);
            return new org.miaixz.bus.metrics.nimble.Histogram() {

                @Override
                public void record(double value) {
                    if (!Double.isFinite(value)) {
                        throw new IllegalArgumentException("Histogram value must be finite");
                    }
                    collector.labelValues(labelValues(finalTags)).observe(value);
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
                public double percentile(double percentile) {
                    return mirror.percentile(percentile);
                }

                @Override
                public TimerSnapshot snapshot() {
                    return mirror.snapshot();
                }
            };
        });
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
        ObservableCollector registration = new ObservableCollector(descriptor, callback, lease);
        try {
            registration.initialize();
            registry.register(registration);
            ownedCollectors.add(registration);
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
        new ArrayList<>(ownedCollectors).forEach(registry::unregister);
        ownedCollectors.clear();
        activeLeases.values().forEach(MetricFamilyRegistry.Lease::close);
        activeLeases.clear();
        counterFamilies.clear();
        summaryFamilies.clear();
        histogramFamilies.clear();
        callbackGaugeFamilies.clear();
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
            throw new IllegalStateException("Prometheus metrics provider is closed");
        }
    }

    /**
     * Converts typed Bus attributes to Prometheus labels.
     *
     * @param attributes Bus attributes
     * @return Prometheus labels
     */
    private Labels labels(org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
        List<String> names = new ArrayList<>(attributes.values().size());
        List<String> values = new ArrayList<>(attributes.values().size());
        for (org.miaixz.bus.metrics.observe.tag.Attributes.Value<?> entry : attributes.values()) {
            names.add(entry.descriptor().key());
            values.add(String.valueOf(entry.value()));
        }
        return Labels.of(names, values);
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
     * Returns the provider-owned counters.
     */
    @Override
    public Iterable<org.miaixz.bus.metrics.nimble.Counter> counters() {
        return List.copyOf(activeCounters.values());
    }

    /**
     * Returns the provider-owned legacy meters.
     */
    @Override
    public Iterable<Meter> meters() {
        return List.copyOf(meters.values());
    }

    /**
     * Returns the provider-owned gauges.
     */
    @Override
    public Iterable<org.miaixz.bus.metrics.nimble.Gauge> gauges() {
        return List.copyOf(activeGauges.values());
    }

    /**
     * Returns the provider-owned timers.
     */
    @Override
    public Iterable<Timer> timers() {
        return List.copyOf(activeTimers.values());
    }

    /**
     * Returns the provider-owned histograms.
     */
    @Override
    public Iterable<org.miaixz.bus.metrics.nimble.Histogram> histograms() {
        return List.copyOf(activeHistograms.values());
    }

    /**
     * Returns the provider-owned LLM timers.
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
     * Bus rate meter backed by a Prometheus counter and native rate mirror.
     */
    private static final class MeterAdapter implements Meter {

        /**
         * Counter receiving cumulative updates.
         */
        private final org.miaixz.bus.metrics.nimble.Counter counter;
        /**
         * Native meter providing rolling rates.
         */
        private final NativeMeter rates = new NativeMeter();

        /**
         * Creates a rate adapter.
         *
         * @param counter backing counter
         */
        private MeterAdapter(org.miaixz.bus.metrics.nimble.Counter counter) {
            this.counter = counter;
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
            counter.increment(amount);
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
     * Prometheus callback collector that reads all current series in one gauge family at scrape time.
     */
    private final class GaugeFamily implements Collector {

        /**
         * Canonical descriptor for this family.
         */
        private final MetricDescriptor descriptor;
        /**
         * Live gauge series indexed by attributes.
         */
        private final ConcurrentHashMap<org.miaixz.bus.metrics.observe.tag.Attributes, org.miaixz.bus.metrics.nimble.Gauge> series = new ConcurrentHashMap<>();

        /**
         * Creates an empty callback family.
         *
         * @param descriptor canonical family descriptor
         */
        private GaugeFamily(MetricDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        /**
         * Adds one live series to the family.
         *
         * @param attributes series attributes
         * @param gauge      live value supplier
         */
        private void add(
                org.miaixz.bus.metrics.observe.tag.Attributes attributes,
                org.miaixz.bus.metrics.nimble.Gauge gauge) {
            series.putIfAbsent(attributes, gauge);
        }

        @Override
        public MetricSnapshot collect() {
            GaugeSnapshot.Builder builder = GaugeSnapshot.builder()
                    .name(NativePrometheusTextEncoder.exportName(descriptor)).help(descriptor.description());
            series.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Object::toString)))
                    .forEach(entry -> {
                        double value = entry.getValue().value();
                        if (Double.isFinite(value)) {
                            builder.dataPoint(
                                    GaugeSnapshot.GaugeDataPointSnapshot.builder().labels(labels(entry.getKey()))
                                            .value(value).build());
                        } else {
                            diagnostics.collectionError("prometheus", "invalid_gauge");
                        }
                    });
            return builder.build();
        }
    }

    /**
     * Observable Bus family exposed as a Prometheus custom collector.
     */
    private final class ObservableCollector implements Collector, MetricRegistration {

        /**
         * Registered family descriptor.
         */
        private final MetricDescriptor descriptor;
        /**
         * User callback invoked during collection.
         */
        private final ObservableCallback callback;
        /**
         * Lease protecting the registered family identity.
         */
        private final MetricFamilyRegistry.Lease lease;
        /**
         * Whether this collector has been closed.
         */
        private final AtomicBoolean registrationClosed = new AtomicBoolean();

        /**
         * Attribute sets established during initialization.
         */
        private volatile Set<org.miaixz.bus.metrics.observe.tag.Attributes> seeded = Set.of();

        /**
         * Creates an observable collector.
         *
         * @param descriptor family descriptor
         * @param callback   observation callback
         * @param lease      family identity lease
         */
        private ObservableCollector(MetricDescriptor descriptor, ObservableCallback callback,
                MetricFamilyRegistry.Lease lease) {
            this.descriptor = descriptor;
            this.callback = callback;
            this.lease = lease;
        }

        /**
         * Runs the callback once to freeze the allowed resource inventory.
         */
        private void initialize() {
            Measurement measurement = observe(null);
            seeded = Set.copyOf(measurement.values.keySet());
        }

        @Override
        public MetricSnapshot collect() {
            Measurement measurement;
            try {
                measurement = observe(seeded);
            } catch (RuntimeException exception) {
                diagnostics.collectionError("prometheus", "callback");
                measurement = new Measurement(descriptor, seeded);
            }
            if (descriptor.kind() == InstrumentKind.COUNTER) {
                CounterSnapshot.Builder builder = CounterSnapshot.builder().name(prometheusName(descriptor.name()))
                        .help(descriptor.description());
                measurement.values.forEach(
                        (attributes, value) -> builder.dataPoint(
                                CounterSnapshot.CounterDataPointSnapshot.builder().labels(labels(attributes))
                                        .value(value.doubleValue()).build()));
                return builder.build();
            }
            GaugeSnapshot.Builder builder = GaugeSnapshot.builder().name(prometheusName(descriptor.name()))
                    .help(descriptor.description());
            measurement.values.forEach(
                    (attributes, value) -> builder.dataPoint(
                            GaugeSnapshot.GaugeDataPointSnapshot.builder().labels(labels(attributes))
                                    .value(value.doubleValue()).build()));
            return builder.build();
        }

        /**
         * Invokes the callback into a fresh measurement buffer.
         *
         * @param allowed allowed resource identities, or {@code null} while seeding
         * @return populated measurement buffer
         */
        private Measurement observe(Set<org.miaixz.bus.metrics.observe.tag.Attributes> allowed) {
            Measurement measurement = new Measurement(descriptor, allowed);
            callback.observe(measurement);
            return measurement;
        }

        @Override
        public void close() {
            if (registrationClosed.compareAndSet(false, true)) {
                observableRegistrations.remove(this);
                if (ownedCollectors.remove(this)) {
                    registry.unregister(this);
                }
                lease.close();
            }
        }
    }

    /**
     * Measurement buffer supplied to one Prometheus observable callback.
     */
    private final class Measurement implements ObservableMeasurement {

        /**
         * Descriptor that determines valid point shape.
         */
        private final MetricDescriptor descriptor;
        /**
         * Seeded resource identities, or {@code null} while seeding.
         */
        private final Set<org.miaixz.bus.metrics.observe.tag.Attributes> allowed;
        /**
         * Values observed in the current callback, indexed by attributes.
         */
        private final Map<org.miaixz.bus.metrics.observe.tag.Attributes, Number> values = new LinkedHashMap<>();

        /**
         * Creates an observable measurement buffer.
         *
         * @param descriptor family descriptor
         * @param allowed    seeded resource identities, or {@code null} while seeding
         */
        private Measurement(MetricDescriptor descriptor, Set<org.miaixz.bus.metrics.observe.tag.Attributes> allowed) {
            this.descriptor = descriptor;
            this.allowed = allowed;
        }

        @Override
        public void recordLong(long value, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.LONG) {
                diagnostics.collectionError("prometheus", "number_kind");
                return;
            }
            record(attributes, value);
        }

        @Override
        public void recordDouble(double value, org.miaixz.bus.metrics.observe.tag.Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.DOUBLE || !Double.isFinite(value)) {
                diagnostics.collectionError("prometheus", "invalid_double");
                return;
            }
            record(attributes, value);
        }

        /**
         * Validates and records one observable value.
         *
         * @param attributes point attributes
         * @param value      numeric point value
         */
        private void record(org.miaixz.bus.metrics.observe.tag.Attributes attributes, Number value) {
            try {
                descriptor.validateAttributes(attributes);
                if (allowed != null && !allowed.contains(attributes)) {
                    diagnostics.resourceDropped("prometheus", "inventory_changed");
                    return;
                }
                if (values.putIfAbsent(attributes, value) != null) {
                    diagnostics.collectionError("prometheus", "duplicate_attributes");
                }
            } catch (RuntimeException exception) {
                diagnostics.collectionError("prometheus", "invalid_point");
            }
        }
    }

}

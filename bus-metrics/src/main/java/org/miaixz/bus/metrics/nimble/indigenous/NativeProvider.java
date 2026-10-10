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
package org.miaixz.bus.metrics.nimble.indigenous;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityViolation;
import org.miaixz.bus.metrics.guard.MetricFamilyKey;
import org.miaixz.bus.metrics.guard.MetricFamilyRegistry;
import org.miaixz.bus.metrics.magic.*;
import org.miaixz.bus.metrics.nimble.*;
import org.miaixz.bus.metrics.nimble.Timer;
import org.miaixz.bus.metrics.nimble.prometheus.NativePrometheusTextEncoder;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Attributes;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Zero-third-party-dependency metrics provider with active instruments, typed observable families, snapshots and text
 * exposition.
 *
 * @author Kimi Liu
 */
public class NativeProvider implements Provider {

    /**
     * Provider-local cardinality policies and observed values.
     */
    private final CardinalityGuard.Scope cardinalityGuard;
    /**
     * Backend-independent family identity registry.
     */
    private final MetricFamilyRegistry familyRegistry = new MetricFamilyRegistry(
            NativePrometheusTextEncoder::exportName, NativePrometheusTextEncoder::normalizeName);
    /**
     * Provider-owned scheduler for rate and rolling-window updates.
     */
    private final ScheduledExecutorService scheduler;
    /**
     * Monotonic identifier source for observable registrations.
     */
    private final AtomicLong observableSequence = new AtomicLong();
    /**
     * Whether this provider has released its owned resources.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Active counter series indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Counter>> counters = new ConcurrentHashMap<>();
    /**
     * Legacy meter series indexed by their canonical key.
     */
    private final ConcurrentHashMap<String, NativeMeter> meters = new ConcurrentHashMap<>();
    /**
     * Legacy rate-pair series indexed by their canonical key.
     */
    private final ConcurrentHashMap<String, RatePair> ratePairs = new ConcurrentHashMap<>();
    /**
     * Active gauge series indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Gauge>> gauges = new ConcurrentHashMap<>();
    /**
     * Active timer series indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Timer>> timers = new ConcurrentHashMap<>();
    /**
     * Active histogram series indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Histogram>> histograms = new ConcurrentHashMap<>();
    /**
     * Legacy LLM timer series indexed by their canonical key.
     */
    private final ConcurrentHashMap<String, NativeLlmTimer> llmTimers = new ConcurrentHashMap<>();
    /**
     * Live observable registrations indexed by their provider-local identifiers.
     */
    private final ConcurrentHashMap<Long, ObservableRegistration> observables = new ConcurrentHashMap<>();

    /**
     * Legacy service-level objective tracker.
     */
    private final NativeSloTracker sloTracker = new NativeSloTracker();
    /**
     * Encoder used by the provider's scrape capability.
     */
    private final NativePrometheusTextEncoder textEncoder = new NativePrometheusTextEncoder();
    /**
     * Provider self-diagnostics implementation.
     */
    private final MetricDiagnostics diagnostics;
    /**
     * Reentrancy guard preventing a diagnostic metric from recursively reporting its own cardinality decision.
     */
    private final ThreadLocal<Boolean> cardinalityDiagnosticInProgress = ThreadLocal.withInitial(() -> false);
    /**
     * Listener that publishes cardinality decisions through provider diagnostics.
     */
    private final Consumer<CardinalityViolation> cardinalityListener;
    /**
     * Capabilities exposed to binders and exporters.
     */
    private final ProviderCapabilities capabilities;

    /**
     * Creates a provider using the legacy global cardinality scope.
     */
    public NativeProvider() {
        this(CardinalityGuard.globalScope());
    }

    /**
     * Creates a provider using isolated cardinality state.
     *
     * @param cardinalityGuard provider-local cardinality scope
     */
    public NativeProvider(CardinalityGuard.Scope cardinalityGuard) {
        this.cardinalityGuard = Objects.requireNonNull(cardinalityGuard, "Cardinality scope must not be null");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, Builder.THREAD_NAME_TICK);
            thread.setDaemon(true);
            return thread;
        });
        ScrapeSupport scrape = new ScrapeSupport() {

            @Override
            public String scrape() {
                return textEncoder.encode(snapshot());
            }

            @Override
            public String contentType() {
                return Builder.PROMETHEUS_CONTENT_TYPE;
            }
        };
        SnapshotSupport snapshot = this::snapshot;
        this.capabilities = new ProviderCapabilities(true, Optional.of(scrape), Optional.of(snapshot));
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
                this::tick,
                Builder.TICK_INTERVAL_SECONDS,
                Builder.TICK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    /**
     * Builds the legacy canonical registry key with tags sorted by key.
     *
     * @param name metric name
     * @param tags metric tags
     * @return canonical key
     */
    static String key(String name, Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return name + "{}";
        }
        Tag[] sorted = tags.clone();
        Arrays.sort(sorted, Comparator.comparing(Tag::key));
        StringBuilder result = new StringBuilder(name).append(Symbol.C_BRACE_LEFT);
        for (int i = 0; i < sorted.length; i++) {
            if (i > 0) {
                result.append(Symbol.C_COMMA);
            }
            result.append(sorted[i]);
        }
        return result.append(Symbol.C_BRACE_RIGHT).toString();
    }

    /**
     * Validates that a descriptor matches an active instrument API.
     *
     * @param descriptor descriptor to validate
     * @param kind       expected instrument kind
     * @param numberKind expected numeric kind
     */
    private static void validateActive(MetricDescriptor descriptor, InstrumentKind kind, NumberKind numberKind) {
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        if (descriptor.kind() != kind || descriptor.numberKind() != numberKind) {
            throw new IllegalArgumentException(
                    "Metric descriptor is incompatible with active " + kind + " API: " + descriptor.name());
        }
    }

    /**
     * Returns the divisor that converts nanoseconds to the descriptor unit.
     *
     * @param unit descriptor unit
     * @return nanosecond divisor
     */
    private static double nanosDivisor(String unit) {
        return switch (unit) {
            case "s" -> 1_000_000_000.0;
            case "ms" -> 1_000_000.0;
            case "us" -> 1_000.0;
            default -> 1.0;
        };
    }

    /**
     * Releases every family lease in an active registry.
     *
     * @param metrics active metrics to release
     */
    private static void closeActive(Collection<? extends ActiveMetric<?>> metrics) {
        metrics.forEach(value -> value.lease().close());
    }

    /**
     * Adds a point using the descriptor owned by an active metric.
     *
     * @param families snapshot families under construction
     * @param active   active metric metadata
     * @param point    point to add
     */
    private static void add(
            Map<MetricFamilyKey.Logical, MutableFamily> families,
            ActiveMetric<?> active,
            MetricPoint point) {
        add(families, active.descriptor(), point);
    }

    /**
     * Adds a point to its logical snapshot family.
     *
     * @param families   snapshot families under construction
     * @param descriptor family descriptor
     * @param point      point to add
     */
    private static void add(
            Map<MetricFamilyKey.Logical, MutableFamily> families,
            MetricDescriptor descriptor,
            MetricPoint point) {
        MetricFamilyKey.Logical key = MetricFamilyKey.identity(descriptor).logical();
        families.computeIfAbsent(key, ignored -> new MutableFamily(descriptor)).add(point);
    }

    @Override
    public Counter counter(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return counter(MetricDescriptor.counter(name, attributes), attributes);
    }

    @Override
    public Counter counter(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, InstrumentKind.COUNTER, NumberKind.LONG);
        Attributes guarded = guard(descriptor, attributes);
        return active(descriptor, guarded, counters, () -> new ValidatingCounter(new NativeCounter()));
    }

    @Override
    public Meter meter(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        return meters.computeIfAbsent(key(name, guarded), ignored -> new NativeMeter());
    }

    @Override
    public RatePair ratePair(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        return ratePairs.computeIfAbsent(key(name, guarded), ignored -> RatePair.create(this, name, guarded));
    }

    @Override
    public <T> Gauge gauge(String name, T stateObj, ToDoubleFunction<T> fn, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return gauge(MetricDescriptor.gauge(name, attributes), attributes, stateObj, fn);
    }

    @Override
    public <T> Gauge gauge(MetricDescriptor descriptor, Attributes attributes, T stateObj, ToDoubleFunction<T> fn) {
        validateActive(descriptor, InstrumentKind.GAUGE, NumberKind.DOUBLE);
        Objects.requireNonNull(stateObj, "Gauge state must not be null");
        Objects.requireNonNull(fn, "Gauge function must not be null");
        Attributes guarded = guard(descriptor, attributes);
        return active(descriptor, guarded, gauges, () -> new NativeGauge<>(stateObj, fn));
    }

    @Override
    public Timer timer(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return timer(MetricDescriptor.timer(name, attributes), attributes);
    }

    @Override
    public Timer timer(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, InstrumentKind.TIMER, NumberKind.DOUBLE);
        Attributes guarded = guard(descriptor, attributes);
        return active(
                descriptor,
                guarded,
                timers,
                () -> new ValidatingTimer(new NativeTimer(descriptor.name(), guarded.toTags())));
    }

    @Override
    public Histogram histogram(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return histogram(MetricDescriptor.histogram(name, attributes), attributes);
    }

    @Override
    public Histogram histogram(MetricDescriptor descriptor, Attributes attributes) {
        validateActive(descriptor, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE);
        Attributes guarded = guard(descriptor, attributes);
        return active(
                descriptor,
                guarded,
                histograms,
                () -> new ValidatingHistogram(new NativeHistogram(descriptor.name(), guarded.toTags())));
    }

    @Override
    public LlmTimer llmTimer(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        return llmTimers.computeIfAbsent(key(name, guarded), ignored -> new NativeLlmTimer(name, guarded, this));
    }

    @Override
    public MetricRegistration registerObservable(MetricDescriptor descriptor, ObservableCallback callback) {
        ensureOpen();
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        Objects.requireNonNull(callback, "Observable callback must not be null");
        if (descriptor.kind() == InstrumentKind.TIMER || descriptor.kind() == InstrumentKind.HISTOGRAM) {
            throw new IllegalArgumentException("Observable distributions are not supported: " + descriptor.name());
        }
        MetricFamilyRegistry.Lease familyLease = familyRegistry.acquire(descriptor);
        long id = observableSequence.incrementAndGet();
        ObservableRegistration registration = new ObservableRegistration(id, descriptor, callback, familyLease);
        try {
            registration.seed();
            observables.put(id, registration);
            return registration;
        } catch (RuntimeException exception) {
            familyLease.close();
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
    public SloTracker sloTracker() {
        return sloTracker;
    }

    @Override
    public Iterable<Counter> counters() {
        return counters.values().stream().map(ActiveMetric::instrument).toList();
    }

    @Override
    public Iterable<Meter> meters() {
        return List.copyOf(meters.values());
    }

    @Override
    public Iterable<Gauge> gauges() {
        return gauges.values().stream().map(ActiveMetric::instrument).toList();
    }

    @Override
    public Iterable<Timer> timers() {
        return timers.values().stream().map(ActiveMetric::instrument).toList();
    }

    @Override
    public Iterable<Histogram> histograms() {
        return histograms.values().stream().map(ActiveMetric::instrument).toList();
    }

    @Override
    public Iterable<LlmTimer> llmTimers() {
        return List.copyOf(llmTimers.values());
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cardinalityGuard.removeViolationListener(cardinalityListener);
        scheduler.shutdownNow();
        new ArrayList<>(observables.values()).forEach(ObservableRegistration::close);
        closeActive(counters.values());
        closeActive(gauges.values());
        closeActive(timers.values());
        closeActive(histograms.values());
        counters.clear();
        meters.clear();
        ratePairs.clear();
        gauges.clear();
        timers.clear();
        histograms.clear();
        llmTimers.clear();
    }

    /**
     * Advances provider-owned legacy meters.
     */
    private void tick() {
        if (closed.get()) {
            return;
        }
        meters.values().forEach(NativeMeter::tick);
    }

    /**
     * Collects a deterministic immutable snapshot of every native family.
     *
     * @return current metric snapshot
     */
    private MetricSnapshot snapshot() {
        ensureOpen();
        Map<MetricFamilyKey.Logical, MutableFamily> families = new LinkedHashMap<>();
        counters.values().forEach(
                value -> add(families, value, new LongMetricPoint(value.attributes(), value.instrument().count())));
        gauges.values().forEach(value -> {
            double current = value.instrument().value();
            if (Double.isFinite(current)) {
                add(families, value, new DoubleMetricPoint(value.attributes(), current));
            } else {
                diagnostics.collectionError("native", "invalid_gauge");
            }
        });
        timers.values().forEach(value -> add(families, value, timerPoint(value)));
        histograms.values().forEach(value -> add(families, value, histogramPoint(value)));

        List<ObservableRegistration> registrations = new ArrayList<>(observables.values());
        registrations.sort(Comparator.comparingLong(ObservableRegistration::id));
        for (ObservableRegistration registration : registrations) {
            for (MetricPoint point : registration.collect()) {
                add(families, registration.descriptor, point);
            }
        }

        List<MetricFamilySnapshot> snapshots = families.values().stream().map(MutableFamily::snapshot)
                .sorted(Comparator.comparing(value -> value.descriptor().name())).toList();
        return new MetricSnapshot(System.currentTimeMillis(), snapshots);
    }

    /**
     * Converts a native timer into a distribution point.
     *
     * @param active active timer and its metadata
     * @return immutable distribution point
     */
    private DistributionMetricPoint timerPoint(ActiveMetric<Timer> active) {
        TimerSnapshot snapshot = active.instrument().snapshot();
        double divisor = nanosDivisor(active.descriptor().unit());
        List<HistogramBucket> buckets = new ArrayList<>(snapshot.bucketBounds().length);
        for (int i = 0; i < snapshot.bucketBounds().length; i++) {
            double bound = snapshot.bucketBounds()[i] * 1_000_000_000.0 / divisor;
            buckets.add(new HistogramBucket(bound, snapshot.bucketCounts()[i]));
        }
        double max = snapshot.count() == 0 ? 0 : snapshot.maxNanos() / divisor;
        return new DistributionMetricPoint(active.attributes(), snapshot.count(), snapshot.totalNanos() / divisor, max,
                buckets);
    }

    /**
     * Converts a native histogram into a distribution point.
     *
     * @param active active histogram and its metadata
     * @return immutable distribution point
     */
    private DistributionMetricPoint histogramPoint(ActiveMetric<Histogram> active) {
        Histogram histogram = active.instrument();
        double max = histogram.count() == 0 ? 0 : histogram.max();
        return new DistributionMetricPoint(active.attributes(), histogram.count(), histogram.totalAmount(), max,
                List.of());
    }

    /**
     * Returns or atomically creates one active instrument series.
     *
     * @param descriptor family descriptor
     * @param attributes series attributes
     * @param registry   instrument registry
     * @param factory    instrument factory
     * @param <T>        instrument type
     * @return existing or newly created instrument
     */
    private <T> T active(
            MetricDescriptor descriptor,
            Attributes attributes,
            ConcurrentHashMap<String, ActiveMetric<T>> registry,
            Supplier<T> factory) {
        ensureOpen();
        String activeKey = MetricFamilyKey.identity(descriptor).logical() + attributes.toString();
        return registry.computeIfAbsent(activeKey, ignored -> {
            MetricFamilyRegistry.Lease lease = familyRegistry.acquire(descriptor);
            try {
                return new ActiveMetric<>(descriptor, attributes, factory.get(), lease);
            } catch (RuntimeException exception) {
                lease.close();
                throw exception;
            }
        }).instrument();
    }

    /**
     * Validates and cardinality-guards a series attribute set.
     *
     * @param descriptor family descriptor
     * @param attributes proposed attributes
     * @return guarded attributes
     */
    private Attributes guard(MetricDescriptor descriptor, Attributes attributes) {
        descriptor.validateAttributes(attributes);
        Attributes guarded = cardinalityGuard.enforce(descriptor.name(), attributes);
        descriptor.validateAttributes(guarded);
        return guarded;
    }

    /**
     * Ensures that the provider still accepts operations.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Native metrics provider is closed");
        }
    }

    /**
     * Active instrument and its family lease.
     *
     * @param descriptor family descriptor
     * @param attributes series attributes
     * @param instrument backend instrument
     * @param lease      family identity lease
     * @param <T>        instrument type
     */
    private record ActiveMetric<T>(MetricDescriptor descriptor, Attributes attributes, T instrument,
            MetricFamilyRegistry.Lease lease) {
    }

    /**
     * Mutable family accumulator used while building one snapshot.
     */
    private static final class MutableFamily {

        /**
         * Canonical family descriptor.
         */
        private final MetricDescriptor descriptor;
        /**
         * First point observed for each unique attribute set.
         */
        private final Map<Attributes, MetricPoint> points = new LinkedHashMap<>();

        /**
         * Creates an empty family accumulator.
         *
         * @param descriptor canonical family descriptor
         */
        private MutableFamily(MetricDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        /**
         * Adds a point when its attributes have not already been recorded.
         *
         * @param point point to add
         */
        private void add(MetricPoint point) {
            points.putIfAbsent(point.attributes(), point);
        }

        /**
         * Freezes the accumulated family.
         *
         * @return immutable family snapshot
         */
        private MetricFamilySnapshot snapshot() {
            return new MetricFamilySnapshot(descriptor, List.copyOf(points.values()));
        }
    }

    /**
     * Counter wrapper that enforces the non-negative increment contract.
     */
    private static final class ValidatingCounter implements Counter {

        /**
         * Native counter receiving validated updates.
         */
        private final NativeCounter delegate;

        /**
         * Creates a validating counter wrapper.
         *
         * @param delegate native counter
         */
        private ValidatingCounter(NativeCounter delegate) {
            this.delegate = delegate;
        }

        @Override
        public void increment() {
            delegate.increment();
        }

        @Override
        public void increment(long amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Counter increment must be non-negative");
            }
            delegate.increment(amount);
        }

        @Override
        public long count() {
            return delegate.count();
        }
    }

    /**
     * Timer wrapper that rejects negative recorded durations.
     */
    private static final class ValidatingTimer implements Timer {

        /**
         * Native timer receiving validated updates.
         */
        private final NativeTimer delegate;

        /**
         * Creates a validating timer wrapper.
         *
         * @param delegate native timer
         */
        private ValidatingTimer(NativeTimer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Sample start() {
            return delegate.start();
        }

        @Override
        public void record(long amount, TimeUnit unit) {
            if (amount < 0) {
                throw new IllegalArgumentException("Timer amount must be non-negative");
            }
            delegate.record(amount, unit);
        }

        @Override
        public long count() {
            return delegate.count();
        }

        @Override
        public double totalTime(TimeUnit unit) {
            return delegate.totalTime(unit);
        }

        @Override
        public double max(TimeUnit unit) {
            return delegate.max(unit);
        }

        @Override
        public double percentile(double percentile, TimeUnit unit) {
            return delegate.percentile(percentile, unit);
        }

        @Override
        public double percentile(double percentile, TimeUnit unit, Window window) {
            return delegate.percentile(percentile, unit, window);
        }

        @Override
        public Timer onViolation(
                double percentile,
                long threshold,
                TimeUnit unit,
                int checkEvery,
                ConsumerX<ViolationEvent> callback) {
            delegate.onViolation(percentile, threshold, unit, checkEvery, callback);
            return this;
        }

        @Override
        public TimerSnapshot snapshot() {
            return delegate.snapshot();
        }
    }

    /**
     * Histogram wrapper that rejects non-finite recorded values.
     */
    private static final class ValidatingHistogram implements Histogram {

        /**
         * Native histogram receiving validated updates.
         */
        private final NativeHistogram delegate;

        /**
         * Creates a validating histogram wrapper.
         *
         * @param delegate native histogram
         */
        private ValidatingHistogram(NativeHistogram delegate) {
            this.delegate = delegate;
        }

        @Override
        public void record(double value) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Histogram value must be finite");
            }
            delegate.record(value);
        }

        @Override
        public long count() {
            return delegate.count();
        }

        @Override
        public double totalAmount() {
            return delegate.totalAmount();
        }

        @Override
        public double max() {
            return delegate.max();
        }

        @Override
        public double percentile(double percentile) {
            return delegate.percentile(percentile);
        }

        @Override
        public TimerSnapshot snapshot() {
            return delegate.snapshot();
        }
    }

    /**
     * Provider-owned observable callback registration.
     */
    private final class ObservableRegistration implements MetricRegistration {

        /**
         * Provider-local registration identifier.
         */
        private final long id;
        /**
         * Registered family descriptor.
         */
        private final MetricDescriptor descriptor;
        /**
         * User callback invoked for each collection.
         */
        private final ObservableCallback callback;
        /**
         * Lease protecting the registered family identity.
         */
        private final MetricFamilyRegistry.Lease familyLease;
        /**
         * Whether this registration has been closed.
         */
        private final AtomicBoolean registrationClosed = new AtomicBoolean();

        /**
         * Attribute sets established by the seed observation.
         */
        private volatile Set<Attributes> seededAttributes = Set.of();

        /**
         * Creates an observable registration.
         *
         * @param id          provider-local identifier
         * @param descriptor  family descriptor
         * @param callback    observation callback
         * @param familyLease family identity lease
         */
        private ObservableRegistration(long id, MetricDescriptor descriptor, ObservableCallback callback,
                MetricFamilyRegistry.Lease familyLease) {
            this.id = id;
            this.descriptor = descriptor;
            this.callback = callback;
            this.familyLease = familyLease;
        }

        /**
         * Returns the provider-local identifier used for deterministic ordering.
         *
         * @return registration identifier
         */
        private long id() {
            return id;
        }

        /**
         * Runs the callback once to freeze the allowed resource inventory.
         */
        private void seed() {
            Measurement measurement = observe(true);
            seededAttributes = Set.copyOf(measurement.points.keySet());
        }

        /**
         * Collects this observable family without propagating callback failures.
         *
         * @return collected points, or an empty list on failure or closure
         */
        private List<MetricPoint> collect() {
            if (registrationClosed.get()) {
                return List.of();
            }
            try {
                return List.copyOf(observe(false).points.values());
            } catch (RuntimeException exception) {
                diagnostics.collectionError("native", "callback");
                return List.of();
            }
        }

        /**
         * Invokes the callback into a fresh measurement buffer.
         *
         * @param seed whether this invocation establishes the resource inventory
         * @return populated measurement buffer
         */
        private Measurement observe(boolean seed) {
            Measurement measurement = new Measurement(descriptor, seed ? null : seededAttributes);
            callback.observe(measurement);
            return measurement;
        }

        @Override
        public void close() {
            if (registrationClosed.compareAndSet(false, true)) {
                observables.remove(id, this);
                familyLease.close();
            }
        }
    }

    /**
     * Measurement buffer supplied to one observable callback invocation.
     */
    private final class Measurement implements ObservableMeasurement {

        /**
         * Descriptor that determines valid point shape.
         */
        private final MetricDescriptor descriptor;
        /**
         * Seeded resource identities, or {@code null} while seeding.
         */
        private final Set<Attributes> allowedAttributes;
        /**
         * Collected points indexed by their unique attributes.
         */
        private final Map<Attributes, MetricPoint> points = new LinkedHashMap<>();

        /**
         * Creates a callback measurement buffer.
         *
         * @param descriptor        observable family descriptor
         * @param allowedAttributes seeded resource identities, or {@code null} while seeding
         */
        private Measurement(MetricDescriptor descriptor, Set<Attributes> allowedAttributes) {
            this.descriptor = descriptor;
            this.allowedAttributes = allowedAttributes;
        }

        @Override
        public void recordLong(long value, Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.LONG) {
                diagnostics.collectionError("native", "number_kind");
                return;
            }
            record(attributes, checked -> new LongMetricPoint(checked, value));
        }

        @Override
        public void recordDouble(double value, Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.DOUBLE || !Double.isFinite(value)) {
                diagnostics.collectionError("native", "invalid_double");
                return;
            }
            record(attributes, checked -> new DoubleMetricPoint(checked, value));
        }

        /**
         * Validates and records one observable point.
         *
         * @param attributes point attributes
         * @param factory    numeric point factory
         */
        private void record(Attributes attributes, Function<Attributes, MetricPoint> factory) {
            try {
                descriptor.validateAttributes(attributes);
                if (allowedAttributes != null && !allowedAttributes.contains(attributes)) {
                    diagnostics.resourceDropped("native", "inventory_changed");
                    return;
                }
                MetricPoint point = factory.apply(attributes);
                if (points.putIfAbsent(attributes, point) != null) {
                    diagnostics.collectionError("native", "duplicate_attributes");
                }
            } catch (RuntimeException exception) {
                diagnostics.collectionError("native", "invalid_point");
            }
        }
    }

}

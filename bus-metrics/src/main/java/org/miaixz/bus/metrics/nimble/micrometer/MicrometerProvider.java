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
package org.miaixz.bus.metrics.nimble.micrometer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityViolation;
import org.miaixz.bus.metrics.guard.MetricFamilyKey;
import org.miaixz.bus.metrics.guard.MetricFamilyRegistry;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.nimble.*;
import org.miaixz.bus.metrics.nimble.Timer;
import org.miaixz.bus.metrics.nimble.indigenous.*;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Attributes;
import org.miaixz.bus.metrics.observe.tag.Tag;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Provider adapter for a caller-owned Micrometer {@link MeterRegistry}.
 *
 * @author Kimi Liu
 */
public class MicrometerProvider implements Provider {

    /**
     * Largest integer that a {@code double} represents exactly.
     */
    private static final double MAX_EXACT_DOUBLE_INTEGER = 9_007_199_254_740_992.0;

    /**
     * Caller-owned Micrometer registry.
     */
    private final MeterRegistry registry;
    /**
     * Provider-local cardinality policies and observed values.
     */
    private final CardinalityGuard.Scope cardinalityGuard;
    /**
     * Backend-independent family identity registry.
     */
    private final MetricFamilyRegistry familyRegistry = new MetricFamilyRegistry();
    /**
     * Micrometer meters created and therefore removable by this adapter.
     */
    private final Set<io.micrometer.core.instrument.Meter> ownedMeters = ConcurrentHashMap.newKeySet();
    /**
     * Active counters indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Counter>> counters = new ConcurrentHashMap<>();
    /**
     * Legacy meters indexed by their canonical keys.
     */
    private final ConcurrentHashMap<String, MeterAdapter> meters = new ConcurrentHashMap<>();
    /**
     * Shared rate pairs indexed by their canonical keys.
     */
    private final ConcurrentHashMap<String, RatePair> ratePairs = new ConcurrentHashMap<>();
    /**
     * Legacy and typed gauges indexed by their canonical keys.
     */
    private final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<>();
    /**
     * Active timers indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Timer>> timers = new ConcurrentHashMap<>();
    /**
     * Active histograms indexed by canonical family and attributes.
     */
    private final ConcurrentHashMap<String, ActiveMetric<Histogram>> histograms = new ConcurrentHashMap<>();
    /**
     * Legacy LLM timers indexed by their canonical keys.
     */
    private final ConcurrentHashMap<String, LlmTimer> llmTimers = new ConcurrentHashMap<>();
    /**
     * Live observable callback registrations.
     */
    private final Set<ObservableRegistration> observables = ConcurrentHashMap.newKeySet();
    /**
     * Legacy service-level objective tracker.
     */
    private final NativeSloTracker sloTracker = new NativeSloTracker();
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
     * Creates an adapter using the legacy global cardinality scope.
     *
     * @param registry caller-owned registry
     */
    public MicrometerProvider(MeterRegistry registry) {
        this(registry, CardinalityGuard.globalScope());
    }

    /**
     * Creates an adapter using isolated cardinality state.
     *
     * @param registry         caller-owned registry
     * @param cardinalityGuard provider-local cardinality scope
     */
    public MicrometerProvider(MeterRegistry registry, CardinalityGuard.Scope cardinalityGuard) {
        this.registry = Objects.requireNonNull(registry, "MeterRegistry must not be null");
        this.cardinalityGuard = Objects.requireNonNull(cardinalityGuard, "Cardinality scope must not be null");
        this.capabilities = new ProviderCapabilities(true, scrapeCapability(registry), Optional.empty());
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
    }

    /**
     * Creates an adapter from a late-bound optional registry without exposing Micrometer in the caller's signature.
     *
     * @param registry         caller-owned Micrometer registry
     * @param cardinalityGuard provider-local cardinality scope
     * @return Micrometer-backed provider
     * @throws NullPointerException if the registry is {@code null}
     * @throws ClassCastException   if the object is not a Micrometer registry
     */
    public static Provider fromRegistry(Object registry, CardinalityGuard.Scope cardinalityGuard) {
        Object checked = Objects.requireNonNull(registry, "MeterRegistry must not be null");
        return new MicrometerProvider(MeterRegistry.class.cast(checked), cardinalityGuard);
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
    private static String activeKey(MetricDescriptor descriptor, Attributes attributes) {
        return MetricFamilyKey.identity(descriptor).logical() + attributes.toString();
    }

    /**
     * Builds the legacy canonical key for one tagged series.
     *
     * @param name metric name
     * @param tags metric tags
     * @return canonical series key
     */
    private static String key(String name, Tag[] tags) {
        return name + Attributes.fromTags(tags).toString();
    }

    /**
     * Converts typed attributes to Micrometer tags.
     *
     * @param attributes typed attributes
     * @return Micrometer tags
     */
    private static Tags tags(Attributes attributes) {
        List<io.micrometer.core.instrument.Tag> tags = attributes.values().stream().map(
                value -> io.micrometer.core.instrument.Tag.of(value.descriptor().key(), String.valueOf(value.value())))
                .toList();
        return Tags.of(tags);
    }

    /**
     * Converts an empty base unit to Micrometer's absent-unit representation.
     *
     * @param value descriptor unit
     * @return unit value, or {@code null} when empty
     */
    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    /**
     * Detects an optional no-argument scrape method without linking an exporter module.
     *
     * @param registry caller-owned registry
     * @return scrape capability when the registry exposes it
     */
    private static Optional<ScrapeSupport> scrapeCapability(MeterRegistry registry) {
        try {
            Method method = registry.getClass().getMethod("scrape");
            if (method.getParameterCount() != 0 || method.getReturnType() != String.class
                    || !method.trySetAccessible()) {
                return Optional.empty();
            }
            return Optional.of(new ScrapeSupport() {

                @Override
                public String scrape() {
                    try {
                        return (String) method.invoke(registry);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        throw new IllegalStateException("Micrometer registry scrape failed", exception);
                    }
                }

                @Override
                public String contentType() {
                    return Builder.PROMETHEUS_CONTENT_TYPE;
                }
            });
        } catch (NoSuchMethodException exception) {
            return Optional.empty();
        }
    }

    /**
     * Adapts a Micrometer timer to the Bus timer contract.
     *
     * @param name  metric name
     * @param tags  metric tags
     * @param meter Micrometer timer
     * @return Bus timer adapter
     */
    private static Timer timerAdapter(String name, Tag[] tags, io.micrometer.core.instrument.Timer meter) {
        NativeTimer mirror = new NativeTimer(name, tags);
        return new Timer() {

            @Override
            public Sample start() {
                long start = System.nanoTime();
                return () -> {
                    long duration = System.nanoTime() - start;
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
                meter.record(amount, unit);
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
    }

    /**
     * Adapts a Micrometer distribution summary to the Bus histogram contract.
     *
     * @param name  metric name
     * @param tags  metric tags
     * @param meter Micrometer distribution summary
     * @return Bus histogram adapter
     */
    private static Histogram histogramAdapter(String name, Tag[] tags, DistributionSummary meter) {
        NativeHistogram mirror = new NativeHistogram(name, tags);
        return new Histogram() {

            @Override
            public void record(double value) {
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("Histogram value must be finite");
                }
                meter.record(value);
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
    }

    @Override
    public Counter counter(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return counter(MetricDescriptor.counter(name, attributes), attributes);
    }

    @Override
    public Counter counter(MetricDescriptor descriptor, Attributes attributes) {
        validate(descriptor, InstrumentKind.COUNTER, NumberKind.LONG);
        Attributes guarded = guard(descriptor, attributes);
        return active(descriptor, guarded, counters, () -> {
            io.micrometer.core.instrument.Counter meter = io.micrometer.core.instrument.Counter
                    .builder(descriptor.name()).description(descriptor.description())
                    .baseUnit(emptyToNull(descriptor.unit())).tags(tags(guarded)).register(registry);
            ownedMeters.add(meter);
            Counter counter = new Counter() {

                @Override
                public void increment() {
                    meter.increment();
                }

                @Override
                public void increment(long amount) {
                    if (amount < 0) {
                        throw new IllegalArgumentException("Counter increment must be non-negative");
                    }
                    meter.increment(amount);
                }

                @Override
                public long count() {
                    return (long) meter.count();
                }
            };
            return new Created<>(counter, meter);
        });
    }

    @Override
    public Meter meter(String name, Tag... tags) {
        ensureOpen();
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        String key = key(name, guarded);
        return meters.computeIfAbsent(key, ignored -> {
            io.micrometer.core.instrument.Counter counter = io.micrometer.core.instrument.Counter.builder(name)
                    .tags(tags(Attributes.fromTags(guarded))).register(registry);
            ownedMeters.add(counter);
            return new MeterAdapter(counter);
        });
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
        validate(descriptor, InstrumentKind.GAUGE, NumberKind.DOUBLE);
        Objects.requireNonNull(stateObj, "Gauge state must not be null");
        Objects.requireNonNull(fn, "Gauge function must not be null");
        Attributes guarded = guard(descriptor, attributes);
        String activeKey = activeKey(descriptor, guarded);
        return gauges.computeIfAbsent(activeKey, ignored -> {
            MetricFamilyRegistry.Lease lease = familyRegistry.acquire(descriptor);
            io.micrometer.core.instrument.Gauge meter = io.micrometer.core.instrument.Gauge
                    .builder(descriptor.name(), stateObj, fn).description(descriptor.description())
                    .baseUnit(emptyToNull(descriptor.unit())).tags(tags(guarded)).register(registry);
            ownedMeters.add(meter);
            return new LeasedGauge(meter, lease);
        });
    }

    @Override
    public Timer timer(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return timer(MetricDescriptor.timer(name, attributes), attributes);
    }

    @Override
    public Timer timer(MetricDescriptor descriptor, Attributes attributes) {
        validate(descriptor, InstrumentKind.TIMER, NumberKind.DOUBLE);
        Attributes guarded = guard(descriptor, attributes);
        return active(descriptor, guarded, timers, () -> {
            io.micrometer.core.instrument.Timer meter = io.micrometer.core.instrument.Timer.builder(descriptor.name())
                    .description(descriptor.description()).tags(tags(guarded)).publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry);
            ownedMeters.add(meter);
            Timer timer = timerAdapter(descriptor.name(), guarded.toTags(), meter);
            return new Created<>(timer, meter);
        });
    }

    @Override
    public Histogram histogram(String name, Tag... tags) {
        Tag[] guarded = cardinalityGuard.enforce(name, tags);
        Attributes attributes = Attributes.fromTags(guarded);
        return histogram(MetricDescriptor.histogram(name, attributes), attributes);
    }

    @Override
    public Histogram histogram(MetricDescriptor descriptor, Attributes attributes) {
        validate(descriptor, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE);
        Attributes guarded = guard(descriptor, attributes);
        return active(descriptor, guarded, histograms, () -> {
            DistributionSummary meter = DistributionSummary.builder(descriptor.name())
                    .description(descriptor.description()).baseUnit(emptyToNull(descriptor.unit())).tags(tags(guarded))
                    .publishPercentiles(0.5, 0.95, 0.99).register(registry);
            ownedMeters.add(meter);
            Histogram histogram = histogramAdapter(descriptor.name(), guarded.toTags(), meter);
            return new Created<>(histogram, meter);
        });
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
        Objects.requireNonNull(callback, "Observable callback must not be null");
        if (descriptor.kind() == InstrumentKind.TIMER || descriptor.kind() == InstrumentKind.HISTOGRAM) {
            throw new IllegalArgumentException("Observable distributions are not supported: " + descriptor.name());
        }
        MetricFamilyRegistry.Lease lease = familyRegistry.acquire(descriptor);
        ObservableRegistration registration = new ObservableRegistration(descriptor, callback, lease);
        try {
            registration.initialize();
            observables.add(registration);
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
        return List.copyOf(gauges.values());
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
        new ArrayList<>(observables).forEach(ObservableRegistration::close);
        counters.values().forEach(value -> value.lease().close());
        gauges.values().stream().filter(LeasedGauge.class::isInstance).map(LeasedGauge.class::cast)
                .forEach(LeasedGauge::close);
        timers.values().forEach(value -> value.lease().close());
        histograms.values().forEach(value -> value.lease().close());
        new ArrayList<>(ownedMeters).forEach(registry::remove);
        ownedMeters.clear();
        counters.clear();
        meters.clear();
        ratePairs.clear();
        gauges.clear();
        timers.clear();
        histograms.clear();
        llmTimers.clear();
    }

    /**
     * Returns or atomically creates one active instrument series.
     *
     * @param descriptor family descriptor
     * @param attributes series attributes
     * @param values     active series registry
     * @param creator    backend instrument creator
     * @param <T>        instrument type
     * @return existing or newly created instrument
     */
    private <T> T active(
            MetricDescriptor descriptor,
            Attributes attributes,
            ConcurrentHashMap<String, ActiveMetric<T>> values,
            java.util.function.Supplier<Created<T>> creator) {
        ensureOpen();
        return values.computeIfAbsent(activeKey(descriptor, attributes), ignored -> {
            MetricFamilyRegistry.Lease lease = familyRegistry.acquire(descriptor);
            try {
                Created<T> created = creator.get();
                return new ActiveMetric<>(created.instrument(), created.meter(), lease);
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
            throw new IllegalStateException("Micrometer metrics provider is closed");
        }
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
     * Bus rate meter backed by a Micrometer counter and a native rate mirror.
     */
    private static final class MeterAdapter implements Meter {

        /**
         * Micrometer counter receiving cumulative updates.
         */
        private final io.micrometer.core.instrument.Counter counter;
        /**
         * Native meter providing rolling rate reads.
         */
        private final NativeMeter rates = new NativeMeter();

        /**
         * Creates a rate adapter.
         *
         * @param counter backing Micrometer counter
         */
        private MeterAdapter(io.micrometer.core.instrument.Counter counter) {
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
     * Newly created Bus instrument and its Micrometer meter.
     *
     * @param instrument Bus instrument adapter
     * @param meter      registered Micrometer meter
     * @param <T>        instrument type
     */
    private record Created<T>(T instrument, io.micrometer.core.instrument.Meter meter) {
    }

    /**
     * Active instrument, backing meter, and family lease.
     *
     * @param instrument Bus instrument adapter
     * @param meter      registered Micrometer meter
     * @param lease      family identity lease
     * @param <T>        instrument type
     */
    private record ActiveMetric<T>(T instrument, io.micrometer.core.instrument.Meter meter,
            MetricFamilyRegistry.Lease lease) {
    }

    /**
     * Gauge adapter that also owns a family identity lease.
     */
    private static final class LeasedGauge implements Gauge {

        /**
         * Registered Micrometer gauge.
         */
        private final io.micrometer.core.instrument.Gauge meter;
        /**
         * Family identity lease released with this gauge.
         */
        private final MetricFamilyRegistry.Lease lease;

        /**
         * Creates a leased gauge adapter.
         *
         * @param meter registered Micrometer gauge
         * @param lease family identity lease
         */
        private LeasedGauge(io.micrometer.core.instrument.Gauge meter, MetricFamilyRegistry.Lease lease) {
            this.meter = meter;
            this.lease = lease;
        }

        @Override
        public double value() {
            return meter.value();
        }

        /**
         * Releases the family identity lease.
         */
        private void close() {
            lease.close();
        }
    }

    /**
     * Provider-owned observable callback registration and its Micrometer meters.
     */
    private final class ObservableRegistration implements MetricRegistration {

        /**
         * Registered family descriptor.
         */
        private final MetricDescriptor descriptor;
        /**
         * User callback invoked during refresh.
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
         * Latest values indexed by their resource attributes.
         */
        private final Map<Attributes, Double> values = new ConcurrentHashMap<>();
        /**
         * Micrometer meters created for the seeded resource inventory.
         */
        private final List<io.micrometer.core.instrument.Meter> meters = new ArrayList<>();

        /**
         * Attribute sets established during initialization.
         */
        private volatile Set<Attributes> seeded = Set.of();
        /**
         * Monotonic time of the most recent callback refresh.
         */
        private volatile long lastRefreshNanos;

        /**
         * Creates an observable registration.
         *
         * @param descriptor family descriptor
         * @param callback   observation callback
         * @param lease      family identity lease
         */
        private ObservableRegistration(MetricDescriptor descriptor, ObservableCallback callback,
                MetricFamilyRegistry.Lease lease) {
            this.descriptor = descriptor;
            this.callback = callback;
            this.lease = lease;
        }

        /**
         * Seeds resource identities and registers the corresponding Micrometer meters.
         */
        private void initialize() {
            Measurement seed = observe(null);
            values.putAll(seed.values);
            seeded = Set.copyOf(seed.values.keySet());
            for (Attributes attributes : seeded) {
                io.micrometer.core.instrument.Meter meter;
                if (descriptor.kind() == InstrumentKind.COUNTER) {
                    meter = FunctionCounter.builder(descriptor.name(), attributes, this::value)
                            .description(descriptor.description()).baseUnit(emptyToNull(descriptor.unit()))
                            .tags(tags(attributes)).register(registry);
                } else {
                    meter = io.micrometer.core.instrument.Gauge.builder(descriptor.name(), attributes, this::value)
                            .description(descriptor.description()).baseUnit(emptyToNull(descriptor.unit()))
                            .tags(tags(attributes)).register(registry);
                }
                meters.add(meter);
                ownedMeters.add(meter);
            }
        }

        /**
         * Refreshes and returns one seeded resource value.
         *
         * @param attributes seeded resource attributes
         * @return most recently observed value
         */
        private double value(Attributes attributes) {
            refresh();
            return values.getOrDefault(attributes, 0.0);
        }

        /**
         * Refreshes observable values at most once per coalescing interval.
         */
        private synchronized void refresh() {
            long now = System.nanoTime();
            if (now - lastRefreshNanos < 1_000_000L) {
                return;
            }
            try {
                Measurement measurement = observe(seeded);
                values.putAll(measurement.values);
                lastRefreshNanos = now;
            } catch (RuntimeException exception) {
                diagnostics.collectionError("micrometer", "callback");
            }
        }

        /**
         * Invokes the callback into a fresh measurement buffer.
         *
         * @param allowed allowed resource identities, or {@code null} while seeding
         * @return populated measurement buffer
         */
        private Measurement observe(Set<Attributes> allowed) {
            Measurement measurement = new Measurement(descriptor, allowed);
            callback.observe(measurement);
            return measurement;
        }

        @Override
        public void close() {
            if (registrationClosed.compareAndSet(false, true)) {
                observables.remove(this);
                meters.forEach(meter -> {
                    registry.remove(meter);
                    ownedMeters.remove(meter);
                });
                lease.close();
            }
        }
    }

    /**
     * Measurement buffer supplied to one Micrometer observable callback.
     */
    private final class Measurement implements ObservableMeasurement {

        /**
         * Descriptor that determines valid point shape.
         */
        private final MetricDescriptor descriptor;
        /**
         * Seeded resource identities, or {@code null} while seeding.
         */
        private final Set<Attributes> allowed;
        /**
         * Collected numeric values indexed by attributes.
         */
        private final Map<Attributes, Double> values = new ConcurrentHashMap<>();

        /**
         * Creates a callback measurement buffer.
         *
         * @param descriptor observable family descriptor
         * @param allowed    seeded resource identities, or {@code null} while seeding
         */
        private Measurement(MetricDescriptor descriptor, Set<Attributes> allowed) {
            this.descriptor = descriptor;
            this.allowed = allowed;
        }

        @Override
        public void recordLong(long value, Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.LONG) {
                diagnostics.collectionError("micrometer", "number_kind");
                return;
            }
            if (Math.abs((double) value) > MAX_EXACT_DOUBLE_INTEGER) {
                diagnostics.precisionLoss("micrometer", descriptor.name());
            }
            record(attributes, value);
        }

        @Override
        public void recordDouble(double value, Attributes attributes) {
            if (descriptor.numberKind() != NumberKind.DOUBLE || !Double.isFinite(value)) {
                diagnostics.collectionError("micrometer", "invalid_double");
                return;
            }
            record(attributes, value);
        }

        /**
         * Validates and records one observable value.
         *
         * @param attributes point attributes
         * @param value      numeric value
         */
        private void record(Attributes attributes, double value) {
            try {
                descriptor.validateAttributes(attributes);
                if (allowed != null && !allowed.contains(attributes)) {
                    diagnostics.resourceDropped("micrometer", "inventory_changed");
                    return;
                }
                if (values.putIfAbsent(attributes, value) != null) {
                    diagnostics.collectionError("micrometer", "duplicate_attributes");
                }
            } catch (RuntimeException exception) {
                diagnostics.collectionError("micrometer", "invalid_point");
            }
        }
    }

}

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
package org.miaixz.bus.metrics.bridge;

import java.time.Duration;
import java.util.*;

import org.miaixz.bus.health.Collector;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.builtin.*;
import org.miaixz.bus.metrics.nimble.MetricBinder;

/**
 * Composes independently cached, bus-health-backed host metric binders.
 * <p>
 * This compatibility class no longer registers JVM metrics, computes percentages, or owns a background scheduler.
 * Collection occurs lazily through observable callbacks and stale-while-refresh caches.
 *
 * @author Kimi Liu
 */
public class HealthMetrics implements MetricBinder {

    /**
     * Dependency-neutral source of immutable host snapshots.
     */
    private final HostMetricSource source;
    /**
     * Host categories, limits, and cache settings.
     */
    private final HostMetricsOptions options;
    /**
     * Successfully bound category binders in creation order.
     */
    private final List<MetricBinder> bound = new ArrayList<>();

    /**
     * Guards reentrant binding while categories are being installed.
     */
    private boolean binding;
    /**
     * Prevents rebinding even after an optional initialization failure.
     */
    private boolean boundAttempted;

    /**
     * Creates a host binder using the default collector and options.
     */
    public HealthMetrics() {
        this(new Collector(), HostMetricsOptions.defaults());
    }

    /**
     * Creates a host binder using an explicit collector and Bus-owned options.
     *
     * @param collector bus-health collector
     * @param options   host metric options
     */
    public HealthMetrics(Collector collector, HostMetricsOptions options) {
        this(new HealthMetricSource(collector, options), options);
    }

    /**
     * Creates a host binder using a dependency-neutral source, primarily for deterministic integration tests.
     *
     * @param source  host snapshot source
     * @param options host metric options
     */
    public HealthMetrics(HostMetricSource source, HostMetricsOptions options) {
        this.source = Objects.requireNonNull(source, "Host metric source must not be null");
        this.options = Objects.requireNonNull(options, "Host metric options must not be null");
    }

    /**
     * Creates an inert lifecycle-compatible binder for an optional host integration that could not initialize.
     *
     * @return disabled host binder
     */
    public static HealthMetrics disabled() {
        HostMetricsOptions defaults = HostMetricsOptions.defaults();
        HostMetricsOptions.Categories disabled = new HostMetricsOptions.Categories(false, false, false, false, false,
                false, false, false, false);
        HostMetricsOptions options = new HostMetricsOptions(false, defaults.convention(), defaults.cacheTtl(),
                defaults.processStateCacheTtl(), disabled, defaults.cpu(), defaults.process(), defaults.disk(),
                defaults.fileSystem(), defaults.network());
        return new HealthMetrics(DisabledSource.INSTANCE, options);
    }

    /**
     * Registers against the globally selected compatibility provider.
     */
    public void register() {
        bind(Metrics.getProvider());
    }

    @Override
    public synchronized void bind(Provider provider) {
        Objects.requireNonNull(provider, "Metrics provider must not be null");
        if (binding || boundAttempted) {
            throw new IllegalStateException("Host metrics are already bound");
        }
        boundAttempted = true;
        if (!provider.capabilities().observable()) {
            String message = "Host metrics require observable metric support from the selected provider";
            if (options.required()) {
                throw new IllegalStateException(message);
            }
            Logger.warn(false, "Metrics", message + "; optional host metrics are disabled");
            return;
        }
        binding = true;
        try {
            bindEnabled(provider);
        } catch (RuntimeException exception) {
            closeBound();
            if (options.required()) {
                throw exception;
            }
            Logger.warn(
                    false,
                    "Metrics",
                    exception,
                    "Optional host metrics disabled after initialization failure: {}",
                    exception.getMessage());
        } finally {
            binding = false;
        }
    }

    /**
     * Creates binders for every enabled and platform-supported host category.
     *
     * @param provider target provider
     */
    private void bindEnabled(Provider provider) {
        HostMetricsOptions.Categories categories = options.categories();
        if (categories.general()) {
            bindCategory(
                    provider,
                    "general",
                    () -> new GeneralHostMetrics(cache(
                            options.process().stateCounts() ? options.processStateCacheTtl() : options.cacheTtl(),
                            source::general), options.process().stateCounts()));
        }
        if (categories.cpu()) {
            bindCategory(provider, "cpu", () -> new CpuHostMetrics(cache(options.cacheTtl(), source::cpu)));
        }
        if (categories.memory()) {
            bindCategory(provider, "memory", () -> new MemoryHostMetrics(cache(options.cacheTtl(), source::memory)));
        }
        if (categories.paging()) {
            bindCategory(provider, "paging", () -> new PagingHostMetrics(cache(options.cacheTtl(), source::paging)));
        }
        if (categories.disk()) {
            bindCategory(provider, "disk", () -> new DiskHostMetrics(cache(options.cacheTtl(), source::disks)));
        }
        if (categories.filesystem()) {
            bindCategory(
                    provider,
                    "filesystem",
                    () -> new FileSystemHostMetrics(cache(options.cacheTtl(), source::fileSystems),
                            options.fileSystem().mountpointMode()));
        }
        if (categories.network()) {
            bindCategory(
                    provider,
                    "network",
                    () -> new NetworkHostMetrics(cache(options.cacheTtl(), source::network),
                            options.network().connections() && source.capabilities().networkConnections()));
        }
        if (categories.process()) {
            bindCategory(
                    provider,
                    "process",
                    () -> new ProcessHostMetrics(cache(options.cacheTtl(), source::currentProcess),
                            source.capabilities()));
        }
        if (categories.container() && source.capabilities().container()) {
            bindCategory(
                    provider,
                    "container",
                    () -> new ContainerHostMetrics(cache(options.cacheTtl(), source::container)));
        }
    }

    /**
     * Creates and primes a snapshot cache so initialization failures are deterministic.
     *
     * @param ttl      cache interval
     * @param supplier snapshot supplier
     * @param <T>      snapshot type
     * @return primed cache
     */
    private <T> SnapshotCache<T> cache(Duration ttl, java.util.function.Supplier<T> supplier) {
        SnapshotCache<T> cache = new SnapshotCache<>(ttl, supplier);
        if (cache.get().isEmpty()) {
            RuntimeException cause = cache.lastFailure()
                    .orElse(new IllegalStateException("Host inventory unavailable"));
            throw new IllegalStateException("Host inventory could not be initialized", cause);
        }
        return cache;
    }

    /**
     * Creates one category binder and applies required versus optional failure policy.
     *
     * @param provider target provider
     * @param category fixed category name
     * @param supplier category binder supplier
     */
    private void bindCategory(Provider provider, String category, java.util.function.Supplier<MetricBinder> supplier) {
        try {
            add(provider, supplier.get());
        } catch (RuntimeException exception) {
            if (options.required()) {
                throw new IllegalStateException("Required host metric category failed: " + category, exception);
            }
            Logger.warn(
                    false,
                    "Metrics",
                    exception,
                    "Optional host metric category disabled: category={}, reason={}",
                    category,
                    exception.getMessage());
        }
    }

    /**
     * Binds and retains one category transactionally.
     *
     * @param provider target provider
     * @param binder   category binder
     */
    private void add(Provider provider, MetricBinder binder) {
        try {
            binder.bind(provider);
            bound.add(binder);
        } catch (RuntimeException exception) {
            binder.close();
            throw exception;
        }
    }

    /**
     * Stops compatibility usage by delegating to {@link #close()}.
     */
    public void stop() {
        close();
    }

    @Override
    public synchronized void close() {
        closeBound();
    }

    /**
     * Releases successfully bound categories in reverse order.
     */
    private void closeBound() {
        Collections.reverse(bound);
        for (MetricBinder binder : bound) {
            try {
                binder.close();
            } catch (RuntimeException exception) {
                Logger.warn(
                        false,
                        "Metrics",
                        exception,
                        "Host metric binder close failed: {}",
                        exception.getClass().getSimpleName());
            }
        }
        bound.clear();
    }

    /**
     * Inert source used when optional host integration is unavailable.
     */
    private enum DisabledSource implements HostMetricSource {

        /**
         * Shared inert source instance.
         */
        INSTANCE;

        @Override
        public HostMetricCapabilities capabilities() {
            return new HostMetricCapabilities(HostMetricCapabilities.PagingFaultShape.NONE, false, false, false, false,
                    false);
        }

        @Override
        public GeneralSnapshot general() {
            return new GeneralSnapshot(0, Map.of());
        }

        @Override
        public CpuSnapshot cpu() {
            return new CpuSnapshot(0, 0, List.of(), List.of());
        }

        @Override
        public MemorySnapshot memory() {
            return new MemorySnapshot(0, 0, 0);
        }

        @Override
        public PagingSnapshot paging() {
            return new PagingSnapshot(0, 0, 0, java.util.OptionalLong.empty(), java.util.OptionalLong.empty());
        }

        @Override
        public List<DiskSnapshot> disks() {
            return List.of();
        }

        @Override
        public List<FileSystemSnapshot> fileSystems() {
            return List.of();
        }

        @Override
        public NetworkSnapshot network() {
            return new NetworkSnapshot(List.of(), List.of());
        }

        @Override
        public Optional<ProcessSnapshot> currentProcess() {
            return Optional.empty();
        }

        @Override
        public Optional<ContainerSnapshot> container() {
            return Optional.empty();
        }
    }

}

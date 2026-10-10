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
package org.miaixz.bus.metrics.nimble;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Fixed-cardinality self-observation events emitted by metrics integrations.
 *
 * @author Kimi Liu
 */
public interface MetricDiagnostics {

    /**
     * Shared validation-only diagnostics implementation.
     */
    MetricDiagnostics NOOP = new MetricDiagnostics() {

        @Override
        public void collectionError(String category, String reason) {
            requireToken("category", category);
            requireToken("reason", reason);
        }

        @Override
        public void recordCollectionDuration(String category, long durationNanos) {
            requireToken("category", category);
            requireNonNegative("durationNanos", durationNanos);
        }

        @Override
        public void resourcesRegistered(String category, long count) {
            requireToken("category", category);
            requireNonNegative("count", count);
        }

        @Override
        public void resourceDropped(String category, String reason) {
            requireToken("category", category);
            requireToken("reason", reason);
        }

        @Override
        public void snapshotAge(String category, double seconds) {
            requireToken("category", category);
            requireNonNegativeFinite("seconds", seconds);
        }

        @Override
        public void precisionLoss(String provider, String metric) {
            requireToken("provider", provider);
            requireToken("metric", metric);
        }
    };

    /**
     * Returns an inert implementation that still validates caller input.
     *
     * @return no-op diagnostics
     */
    static MetricDiagnostics noop() {
        return NOOP;
    }

    /**
     * Creates a provider-backed diagnostics implementation with fixed metric families.
     *
     * @param provider provider that owns the diagnostic instruments
     * @return provider-backed diagnostics
     */
    static MetricDiagnostics create(Provider provider) {
        Provider checkedProvider = Objects.requireNonNull(provider, "Metrics provider must not be null");
        ConcurrentHashMap<String, AtomicLong> registered = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AtomicReference<Double>> ages = new ConcurrentHashMap<>();
        return new MetricDiagnostics() {

            @Override
            public void collectionError(String category, String reason) {
                requireToken("category", category);
                requireToken("reason", reason);
                checkedProvider.counter(
                        "bus.metrics.collection.errors",
                        Tag.of("category", category),
                        Tag.of("reason", reason)).increment();
            }

            @Override
            public void recordCollectionDuration(String category, long durationNanos) {
                requireToken("category", category);
                requireNonNegative("durationNanos", durationNanos);
                checkedProvider.timer("bus.metrics.collection.duration", Tag.of("category", category))
                        .record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
            }

            @Override
            public void resourcesRegistered(String category, long count) {
                requireToken("category", category);
                requireNonNegative("count", count);
                AtomicLong state = registered.computeIfAbsent(category, value -> {
                    AtomicLong created = new AtomicLong();
                    checkedProvider.gauge(
                            "bus.metrics.resources.registered",
                            created,
                            AtomicLong::doubleValue,
                            Tag.of("category", value));
                    return created;
                });
                state.set(count);
            }

            @Override
            public void resourceDropped(String category, String reason) {
                requireToken("category", category);
                requireToken("reason", reason);
                checkedProvider.counter(
                        "bus.metrics.resources.dropped",
                        Tag.of("category", category),
                        Tag.of("reason", reason)).increment();
            }

            @Override
            public void snapshotAge(String category, double seconds) {
                requireToken("category", category);
                requireNonNegativeFinite("seconds", seconds);
                AtomicReference<Double> state = ages.computeIfAbsent(category, value -> {
                    AtomicReference<Double> created = new AtomicReference<>(0.0);
                    checkedProvider.gauge(
                            "bus.metrics.snapshot.age",
                            created,
                            current -> current.get(),
                            Tag.of("category", value));
                    return created;
                });
                state.set(seconds);
            }

            @Override
            public void precisionLoss(String providerName, String metric) {
                requireToken("provider", providerName);
                requireToken("metric", metric);
                checkedProvider.counter(
                        "bus.metrics.precision.loss",
                        Tag.of("provider", providerName),
                        Tag.of("metric", metric)).increment();
            }
        };
    }

    /**
     * Requires a non-blank bounded diagnostic token.
     *
     * @param label diagnostic field name
     * @param value diagnostic token
     */
    private static void requireToken(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
    }

    /**
     * Requires a non-negative integral diagnostic value.
     *
     * @param label diagnostic field name
     * @param value diagnostic value
     */
    private static void requireNonNegative(String label, long value) {
        if (value < 0) {
            throw new IllegalArgumentException(label + " must be non-negative");
        }
    }

    /**
     * Requires a finite, non-negative floating-point diagnostic value.
     *
     * @param label diagnostic field name
     * @param value diagnostic value
     */
    private static void requireNonNegativeFinite(String label, double value) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative");
        }
    }

    /**
     * Records one collection error.
     *
     * @param category fixed collection category
     * @param reason   fixed error reason
     */
    void collectionError(String category, String reason);

    /**
     * Records one collection duration.
     *
     * @param category      fixed collection category
     * @param durationNanos non-negative duration in nanoseconds
     */
    void recordCollectionDuration(String category, long durationNanos);

    /**
     * Sets the currently registered resource count.
     *
     * @param category fixed resource category
     * @param count    non-negative resource count
     */
    void resourcesRegistered(String category, long count);

    /**
     * Records one dropped resource.
     *
     * @param category fixed resource category
     * @param reason   fixed drop reason
     */
    void resourceDropped(String category, String reason);

    /**
     * Sets the age of the most recently successful snapshot.
     *
     * @param category fixed collection category
     * @param seconds  finite, non-negative age in seconds
     */
    void snapshotAge(String category, double seconds);

    /**
     * Records a lossy long-to-double backend conversion.
     *
     * @param provider fixed provider name
     * @param metric   metric family name
     */
    void precisionLoss(String provider, String metric);

}

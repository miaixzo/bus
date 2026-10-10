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

import java.util.*;
import java.util.function.Supplier;

import org.miaixz.bus.metrics.Provider;

/**
 * Deterministic, framework-neutral selection of one metrics provider.
 *
 * @author Kimi Liu
 */
public final class ProviderSelection {

    /**
     * Expected implementation class for each built-in provider key.
     */
    private static final Map<Type, String> EXPECTED_TYPES = Map.of(
            Type.NATIVE,
            "org.miaixz.bus.metrics.nimble.indigenous.NativeProvider",
            Type.MICROMETER,
            "org.miaixz.bus.metrics.nimble.micrometer.MicrometerProvider",
            Type.OPENTELEMETRY,
            "org.miaixz.bus.metrics.nimble.opentelemetry.OpenTelemetryProvider",
            Type.PROMETHEUS,
            "org.miaixz.bus.metrics.nimble.prometheus.PrometheusProvider");

    /**
     * Prevents utility-class instantiation.
     */
    private ProviderSelection() {
        // No initialization required.
    }

    /**
     * Selects and creates exactly one provider. The selected supplier is invoked once.
     *
     * @param requested  requested provider type or automatic selection policy
     * @param candidates available and unavailable backend candidates
     * @return selected provider
     * @throws IllegalStateException if candidates are duplicated, unavailable, ambiguous, absent, or fail to create
     */
    public static Provider select(Type requested, Collection<Candidate> candidates) {
        Objects.requireNonNull(requested, "Requested provider must not be null");
        Objects.requireNonNull(candidates, "Provider candidates must not be null");
        EnumMap<Type, Candidate> indexed = new EnumMap<>(Type.class);
        for (Candidate candidate : candidates) {
            Candidate checked = Objects.requireNonNull(candidate, "Provider candidate must not be null");
            if (indexed.putIfAbsent(checked.type(), checked) != null) {
                throw new IllegalStateException(
                        "Duplicate metrics provider candidate: " + checked.type().name().toLowerCase(Locale.ROOT));
            }
        }
        Candidate selected = requested == Type.AUTO ? automatic(indexed) : indexed.get(requested);
        Type selectedType = selected == null ? requested == Type.AUTO ? Type.NATIVE : requested : selected.type();
        if (selected == null) {
            throw new IllegalStateException(
                    "No candidate was registered for metrics provider " + selectedType.name().toLowerCase(Locale.ROOT)
                            + " (expected " + EXPECTED_TYPES.get(selectedType) + ")");
        }
        if (!selected.available()) {
            throw new IllegalStateException("Metrics provider " + selectedType.name().toLowerCase(Locale.ROOT)
                    + " is unavailable: " + selected.reason() + " (expected " + EXPECTED_TYPES.get(selectedType) + ")");
        }
        try {
            return Objects.requireNonNull(selected.factory().orElseThrow().get(), "Provider factory returned null");
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Metrics provider construction failed for " + selected.type().name().toLowerCase(Locale.ROOT),
                    exception);
        }
    }

    /**
     * Selects Native when no external backend is available, or the sole available external backend.
     *
     * @param indexed candidates indexed by provider type
     * @return selected candidate
     */
    private static Candidate automatic(Map<Type, Candidate> indexed) {
        List<Candidate> external = indexed.values().stream()
                .filter(candidate -> candidate.external() && candidate.available())
                .sorted(java.util.Comparator.comparing(candidate -> candidate.type().name())).toList();
        if (external.size() > 1) {
            String names = external.stream().map(candidate -> candidate.type().name().toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalStateException("Multiple external metrics providers are available: " + names
                    + "; configure bus.metrics.provider explicitly");
        }
        if (external.size() == 1) {
            return external.get(0);
        }
        return indexed.get(Type.NATIVE);
    }

    /**
     * Supported provider selections.
     */
    public enum Type {

        /**
         * Built-in dependency-free provider.
         */
        NATIVE,
        /**
         * Micrometer registry adapter.
         */
        MICROMETER,
        /**
         * OpenTelemetry API adapter.
         */
        OPENTELEMETRY,
        /**
         * Prometheus core registry adapter.
         */
        PROMETHEUS,
        /**
         * Select exactly one available external candidate, otherwise Native.
         */
        AUTO;

        /**
         * Parses a configuration value without aliases.
         *
         * @param value configured provider name
         * @return parsed provider type
         * @throws IllegalArgumentException if the value is blank or is not a supported provider name
         */
        public static Type parse(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("bus.metrics.provider must not be blank");
            }
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unsupported bus.metrics.provider: " + value
                        + "; expected native, micrometer, opentelemetry, prometheus or auto", exception);
            }
        }
    }

    /**
     * Lazy description of one provider candidate.
     *
     * @param type      provider type represented by the candidate
     * @param external  whether the candidate depends on an external telemetry runtime
     * @param available whether the candidate can be selected
     * @param reason    stable explanation when the candidate is unavailable
     * @param factory   lazy provider factory when the candidate is available
     */
    public record Candidate(Type type, boolean external, boolean available, String reason,
            Optional<Supplier<Provider>> factory) {

        /**
         * Validates candidate availability and lazy factory consistency.
         *
         * @throws IllegalArgumentException if the candidate represents {@link Type#AUTO} or its availability and
         *                                  factory state disagree
         */
        public Candidate {
            type = Objects.requireNonNull(type, "Provider candidate type must not be null");
            if (type == Type.AUTO) {
                throw new IllegalArgumentException("AUTO is a selection policy, not a provider candidate");
            }
            reason = reason == null ? "" : reason;
            factory = Objects.requireNonNull(factory, "Provider candidate factory must not be null");
            if (available != factory.isPresent()) {
                throw new IllegalArgumentException("Available provider candidates must have exactly one factory");
            }
        }

        /**
         * Creates an available candidate.
         *
         * @param type     provider type
         * @param external whether the provider depends on an external telemetry runtime
         * @param factory  lazy provider factory
         * @return available candidate
         */
        public static Candidate available(Type type, boolean external, Supplier<Provider> factory) {
            return new Candidate(type, external, true, "",
                    Optional.of(Objects.requireNonNull(factory, "Provider candidate factory must not be null")));
        }

        /**
         * Creates an unavailable candidate with a stable explanation.
         *
         * @param type     provider type
         * @param external whether the provider depends on an external telemetry runtime
         * @param reason   stable unavailability explanation
         * @return unavailable candidate
         * @throws IllegalArgumentException if the reason is blank
         */
        public static Candidate unavailable(Type type, boolean external, String reason) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("Unavailable provider candidate requires a reason");
            }
            return new Candidate(type, external, false, reason, Optional.empty());
        }
    }

}

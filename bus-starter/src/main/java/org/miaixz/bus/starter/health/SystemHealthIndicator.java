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
package org.miaixz.bus.starter.health;

import java.util.*;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import org.miaixz.bus.health.Provider;
import org.miaixz.bus.health.builtin.TID;

/**
 * Read-only Spring Boot health indicator for explicitly selected Bus system details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SystemHealthIndicator implements HealthIndicator {

    /**
     * Canonical allowlist of supported system health detail identifiers.
     */
    private static final Map<String, String> ALLOWED_DETAILS = allowedDetails();

    /**
     * Provider used to obtain system health metrics.
     */
    private final Provider provider;

    /**
     * Allowlist controlling which health details are exposed.
     */
    private final List<String> details;

    /**
     * Creates an indicator for the selected Bus health detail identifiers.
     *
     * @param provider system information provider
     * @param details  selected detail identifiers
     */
    public SystemHealthIndicator(Provider provider, Collection<String> details) {
        this.provider = provider;
        this.details = normalize(details);
    }

    /**
     * Reports system health without changing application availability state.
     *
     * @return current health result
     */
    @Override
    public Health health() {
        if (details.isEmpty()) {
            return Health.up().build();
        }
        try {
            return Health.up().withDetails(provider.get(details)).build();
        } catch (RuntimeException ignored) {
            return Health.down().withDetail("error", "system health data unavailable").build();
        }
    }

    /**
     * Builds the case-insensitive mapping of supported health detail identifiers.
     *
     * @return immutable map from lowercase identifiers to canonical identifiers
     */
    private static Map<String, String> allowedDetails() {
        Map<String, String> values = new LinkedHashMap<>();
        for (String value : TID.ALL_TID) {
            values.put(value.toLowerCase(Locale.ROOT), value);
        }
        return Map.copyOf(values);
    }

    /**
     * Validates, canonicalizes, and de-duplicates requested health detail identifiers.
     *
     * @param details requested detail identifiers
     * @return normalized detail name, or an empty string for blank input
     */
    private static List<String> normalize(Collection<String> details) {
        if (details == null || details.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>(details.size());
        for (String detail : details) {
            if (detail == null || detail.isBlank()) {
                throw new IllegalArgumentException("Health detail identifier must not be blank");
            }
            String canonical = ALLOWED_DETAILS.get(detail.trim().toLowerCase(Locale.ROOT));
            if (canonical == null) {
                throw new IllegalArgumentException("Unknown health detail identifier");
            }
            if (!normalized.contains(canonical)) {
                normalized.add(canonical);
            }
        }
        return List.copyOf(normalized);
    }

}

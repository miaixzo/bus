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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.health.Collector;
import org.miaixz.bus.health.builtin.TID;

/**
 * Coordinates application availability queries and state changes with explicitly allowed Bus Health details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HealthService {

    /**
     * Configuration controlling whether optional Bus Health details may be exposed.
     */
    private final HealthProperties properties;

    /**
     * Collector used to obtain explicitly requested system and hardware details.
     */
    private final Collector collector;

    /**
     * Publisher used to broadcast liveness and readiness state changes.
     */
    private final ApplicationEventPublisher publisher;

    /**
     * Source of the application's current liveness and readiness states.
     */
    private final ApplicationAvailability availability;

    /**
     * Creates the service used to query health data and publish application availability changes.
     *
     * @param properties   health endpoint properties
     * @param collector    system and hardware information collector
     * @param publisher    application availability event publisher
     * @param availability current application availability state
     */
    public HealthService(HealthProperties properties, Collector collector, ApplicationEventPublisher publisher,
            ApplicationAvailability availability) {
        this.properties = properties;
        this.collector = collector;
        this.publisher = publisher;
        this.availability = availability;
    }

    /**
     * Builds the current liveness and readiness data with optional allowlisted system details.
     *
     * @param tid optional comma-separated detail identifiers
     * @return mutable health data containing status, source, availability states, and any requested details
     * @throws ValidateException when a requested detail is invalid or not allowed
     * @throws InternalException when requested system health details cannot be collected
     */
    public Map<String, Object> healthz(String tid) {
        LivenessState liveness = this.availability.getLivenessState();
        ReadinessState readiness = this.availability.getReadinessState();
        boolean healthy = LivenessState.CORRECT.equals(liveness) && ReadinessState.ACCEPTING_TRAFFIC.equals(readiness);
        List<String> details = resolveDetails(tid);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(TID.LIVENESS, state(liveness));
        result.put(TID.READINESS, state(readiness));
        result.put("source", details.isEmpty() ? "lightweight" : "bus-health");
        if (!details.isEmpty()) {
            try {
                Map<String, Object> values = details.contains(TID.ALL) ? this.collector.getAll()
                        : this.collector.get(details);
                if (values != null && !values.isEmpty()) {
                    result.put("details", values);
                }
            } catch (RuntimeException e) {
                throw new InternalException(ErrorCode._503.getKey(), "System health data unavailable", e);
            }
        }
        result.put("status", healthy ? "UP" : "DOWN");
        return result;
    }

    /**
     * Marks application liveness as broken.
     *
     * @return immutable result containing the new state and change timestamp
     */
    public Map<String, Object> broken() {
        AvailabilityChangeEvent.publish(this.publisher, this, LivenessState.BROKEN);
        return builder(EnumValue.Probe.BROKEN);
    }

    /**
     * Marks application liveness as correct.
     *
     * @return immutable result containing the new state and change timestamp
     */
    public Map<String, Object> correct() {
        AvailabilityChangeEvent.publish(this.publisher, this, LivenessState.CORRECT);
        return builder(EnumValue.Probe.CORRECT);
    }

    /**
     * Marks application readiness as accepting traffic.
     *
     * @return immutable result containing the new state and change timestamp
     */
    public Map<String, Object> accept() {
        AvailabilityChangeEvent.publish(this.publisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        return builder(EnumValue.Probe.ACCEPT);
    }

    /**
     * Marks application readiness as refusing traffic.
     *
     * @return immutable result containing the new state and change timestamp
     */
    public Map<String, Object> refuse() {
        AvailabilityChangeEvent.publish(this.publisher, this, ReadinessState.REFUSING_TRAFFIC);
        return builder(EnumValue.Probe.REFUSE);
    }

    /**
     * Builds an immutable availability state-change result.
     *
     * @param probe availability transition represented in the result
     * @return immutable map containing the state value and current timestamp
     */
    public Map<String, Object> builder(EnumValue.Probe probe) {
        return Map.of("state", probe.getValue(), "timestamp", DateKit.current());
    }

    /**
     * Appends one availability state or collected Bus Health detail to an existing result map.
     *
     * @param type availability or Bus Health detail identifier
     * @param map  mutable target map receiving the resolved value
     */
    public void append(String type, Map<String, Object> map) {
        if (TID.LIVENESS.equals(type)) {
            map.put(type, state(this.availability.getLivenessState()));
        } else if (TID.READINESS.equals(type)) {
            map.put(type, state(this.availability.getReadinessState()));
        } else {
            this.collector.append(type, map);
        }
    }

    /**
     * Resolves and validates requested details against {@code bus.health.details}.
     *
     * @param tid optional comma-separated detail identifiers
     * @return immutable validated system detail identifiers; liveness and readiness are omitted because they are always
     *         included in the health result
     * @throws ValidateException when a detail is unknown or not enabled
     */
    private List<String> resolveDetails(String tid) {
        if (tid == null || tid.isBlank()) {
            return List.of();
        }
        Set<String> requested = new LinkedHashSet<>();
        for (String value : tid.split(",")) {
            String detail = value.trim().toLowerCase(Locale.ROOT);
            if (detail.isEmpty()) {
                continue;
            }
            if (!TID.ALL.equals(detail) && !TID.ALL_TID.contains(detail)) {
                throw new ValidateException(ErrorCode._400, "Unknown health detail: " + detail);
            }
            if (!TID.LIVENESS.equals(detail) && !TID.READINESS.equals(detail)) {
                requested.add(detail);
            }
        }
        if (requested.isEmpty()) {
            return List.of();
        }
        List<String> allowed = this.properties.getDetails();
        boolean allowAll = allowed.contains(TID.ALL);
        for (String detail : requested) {
            if (!allowAll && !allowed.contains(detail)) {
                throw new ValidateException(ErrorCode._400, "Health detail is not enabled: " + detail);
            }
        }
        return List.copyOf(requested);
    }

    /**
     * Converts an availability state to its response representation.
     *
     * @param state current availability state, possibly {@code null}
     * @return the state name, or {@code UNKNOWN} when no state is available
     */
    private static String state(Object state) {
        return state == null ? "UNKNOWN" : state.toString();
    }

}

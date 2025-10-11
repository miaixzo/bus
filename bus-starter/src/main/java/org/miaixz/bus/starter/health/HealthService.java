/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.health;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Provider;
import org.miaixz.bus.health.builtin.TID;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Service class for managing and monitoring the system's health status and hardware information.
 * <p>
 * This service integrates with Spring Boot's availability management to control liveness and readiness probes, and it
 * uses the {@code bus-health} module to gather detailed system and hardware metrics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HealthService {

    /**
     * Configuration properties for health monitoring.
     */
    private final HealthProperties properties;

    /**
     * The system information provider for fetching hardware and OS details.
     */
    private final Provider provider;

    /**
     * The Spring ApplicationEventPublisher for publishing availability state changes.
     */
    private final ApplicationEventPublisher publisher;

    /**
     * The Spring ApplicationAvailability interface for checking the current liveness and readiness states.
     */
    private final ApplicationAvailability availability;

    /**
     * Constructs a new HealthService.
     *
     * @param properties   The health configuration properties.
     * @param provider     The system information provider.
     * @param publisher    The Spring application event publisher.
     * @param availability The Spring application availability interface.
     */
    public HealthService(HealthProperties properties, Provider provider, ApplicationEventPublisher publisher,
            ApplicationAvailability availability) {
        this.properties = properties;
        this.provider = provider;
        this.publisher = publisher;
        this.availability = availability;
    }

    /**
     * Retrieves system health status information based on the requested type identifiers.
     *
     * @param tid A comma-separated string of type identifiers (e.g., "liveness,readiness,cpu"). If empty or null, it
     *            defaults to the value from properties, or "liveness,readiness". The special value "all" retrieves all
     *            available metrics.
     * @return A map containing the requested health information on success, or a {@link Message} object on failure.
     */
    public Object healthz(String tid) {
        try {
            // Determine the TIDs to use: prioritize input, then properties, then default.
            String defaultTids = TID.LIVENESS + Symbol.COMMA + TID.READINESS;
            String effectiveTid = StringKit.isEmpty(tid)
                    ? (properties == null || StringKit.isEmpty(properties.getType()) ? defaultTids
                            : properties.getType().toLowerCase())
                    : tid.toLowerCase();

            List<String> tidList = Arrays.asList(effectiveTid.split(Symbol.COMMA));
            // Validate TIDs; if none are valid, default to liveness and readiness.
            if (tidList.stream().noneMatch(TID.ALL_TID::contains)) {
                tidList = Arrays.asList(TID.LIVENESS, TID.READINESS);
                Logger.debug("Invalid tid '{}', defaulting to liveness,readiness", effectiveTid);
            }

            // Gather monitoring information.
            Map<String, Object> result = new HashMap<>();
            result.put("requestId", ID.objectId());
            try {
                result.putAll(TID.ALL.equals(effectiveTid) ? provider.getAll() : provider.get(tidList));
            } catch (NumberFormatException e) {
                Logger.warn("Invalid number format in provider data for tid '{}': {}", effectiveTid, e.getMessage());
                // On error, fall back to appending individually.
                tidList.forEach(type -> append(type, result));
            }

            return result;
        } catch (Exception e) {
            Logger.error("Failed to retrieve health information for tid '{}': {}", tid, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                    .errmsg("Failed to retrieve health information: " + e.getMessage()).build();
        }
    }

    /**
     * Sets the liveness state to {@link LivenessState#BROKEN}, signaling that the application is unhealthy and should
     * be restarted by the orchestrator (e.g., Kubernetes).
     *
     * @return A map indicating the new state and the current timestamp.
     */
    public Object broken() {
        AvailabilityChangeEvent.publish(publisher, this, LivenessState.BROKEN);
        return builder(EnumValue.Probe.BROKEN);
    }

    /**
     * Sets the liveness state to {@link LivenessState#CORRECT}, signaling that the application is running correctly.
     *
     * @return A map indicating the new state and the current timestamp.
     */
    public Object correct() {
        AvailabilityChangeEvent.publish(publisher, this, LivenessState.CORRECT);
        return builder(EnumValue.Probe.CORRECT);
    }

    /**
     * Sets the readiness state to {@link ReadinessState#ACCEPTING_TRAFFIC}, signaling that the application is ready to
     * receive requests.
     *
     * @return A map indicating the new state and the current timestamp.
     */
    public Object accept() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        return builder(EnumValue.Probe.ACCEPT);
    }

    /**
     * Sets the readiness state to {@link ReadinessState#REFUSING_TRAFFIC}, signaling that the application should not
     * receive new requests (e.g., during startup or shutdown).
     *
     * @return A map indicating the new state and the current timestamp.
     */
    public Object refuse() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.REFUSING_TRAFFIC);
        return builder(EnumValue.Probe.REFUSE);
    }

    /**
     * Creates a result map for a health probe state change.
     *
     * @param probe The type of probe action.
     * @return A map containing the state and timestamp.
     */
    public Object builder(EnumValue.Probe probe) {
        return Map.of("state", probe.getValue(), "timestamp:", DateKit.current());
    }

    /**
     * Appends system or hardware information to the result map based on the specified type.
     *
     * @param type The type identifier (e.g., "liveness", "cpu").
     * @param map  The result map to append to.
     */
    public void append(String type, Map<String, Object> map) {
        switch (type.toLowerCase()) {
            case TID.LIVENESS:
                map.put(type, availability.getLivenessState());
                break;

            case TID.READINESS:
                map.put(type, availability.getReadinessState());
                break;

            default:
                try {
                    provider.append(type, map);
                } catch (Exception e) {
                    Logger.error("Failed to append health data for type {}: {}", type, e.getMessage(), e);
                    map.put(type, "Error: " + e.getMessage());
                }
                break;
        }
    }

}

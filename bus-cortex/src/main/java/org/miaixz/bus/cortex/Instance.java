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
package org.miaixz.bus.cortex;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.cortex.health.HealthInfo;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Shared application process coordinates and server observations. The inherited integer status is the sole lifecycle
 * and health value defined by {@link Status.Health}; registry owns service associations and the host owns sessions.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Instance extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558865000001L;

    /**
     * Application identifier to which the runtime instance belongs.
     */
    private String app_id;

    /**
     * Human-readable process name.
     */
    private String name;

    /**
     * Hostname or IP address of the runtime instance.
     */
    private String host;

    /**
     * Network port of the runtime instance.
     */
    private Integer port;

    /**
     * Load-balancing weight reported for this runtime registration.
     */
    private Integer weight;

    /**
     * Stable process checksum, unique within its tenant and owning application.
     */
    private String checksum;

    /**
     * Program version reported by this process; service versions belong to ApiAssets.
     */
    private String version;

    /**
     * JSON text containing health-check configuration, the runtime session and server observations. Members are scheme,
     * path, time, origin, session, beat and lease; status remains inherited from Tenant.
     */
    @Lob
    private String health;

    /**
     * JSON object text containing string-valued routing labels.
     */
    @Lob
    private String labels;

    /**
     * JSON object text containing additional runtime metadata.
     */
    @Lob
    private String metadata;

    /**
     * Creates an empty runtime instance descriptor.
     */
    public Instance() {
        // No initialization required.
    }

    /**
     * Returns whether the instance is currently healthy.
     *
     * @return {@code true} when the instance is marked healthy
     */
    public boolean isHealthyNow() {
        return Status.Health.UP.matches(getStatus());
    }

    /**
     * Returns whether this lifecycle status permits a runtime session.
     *
     * @return whether the instance is neither disabled nor deleted and has a known status
     */
    public boolean isActive() {
        return Status.Health.UP.matches(getStatus()) || Status.Health.DOWN.matches(getStatus())
                || Status.Health.UNKNOWN.matches(getStatus()) || Status.Health.STARTING.matches(getStatus())
                || Status.Health.MAINTENANCE.matches(getStatus());
    }

    /**
     * Combines active instance status with the recorded session lifetime without conflating reachability with expiry.
     *
     * @param now evaluation time in epoch milliseconds
     * @return whether a known active status and an unexpired session are present
     */
    public boolean isOnline(long now) {
        HealthInfo value = healthView();
        return isActive() && value.isLeaseValid(now);
    }

    /**
     * Parses the health JSON text into a detached, validated value without caching a second representation.
     *
     * @return complete health value, with absent members represented as null
     * @throws IllegalArgumentException if the document contains an unknown member or invalid value
     */
    public HealthInfo healthView() {
        return HealthInfo.parse(health);
    }

    /**
     * Validates and serializes a complete health snapshot. Callers must read the current value, change only the members
     * they own and fence persistence using the existing concurrency boundary. Null members clear values.
     *
     * @param value complete health snapshot
     * @throws IllegalArgumentException if the supplied snapshot is invalid
     */
    public void health(HealthInfo value) {
        if (value == null)
            throw new IllegalArgumentException("Health value required");
        health = value.toJson();
    }

    /**
     * Parses routing labels without retaining a second mutable representation.
     *
     * @return string-valued routing labels
     * @throws IllegalArgumentException if labels are not a JSON object of strings
     */
    public Map<String, String> labelView() {
        Map<String, String> result = new LinkedHashMap<>();
        object(labels).values().forEach((key, value) -> {
            if (!(value instanceof JsonValue.StringValue text))
                throw new IllegalArgumentException("Instance labels must contain strings");
            result.put(key, text.value());
        });
        return result;
    }

    /**
     * Parses extension metadata without retaining a second mutable representation.
     *
     * @return structured metadata object
     * @throws IllegalArgumentException if metadata is not a JSON object
     */
    public JsonValue.ObjectValue metadataView() {
        return object(metadata);
    }

    /**
     * Parses one optional JSON object using the shared bounded JSON reader.
     *
     * @param text JSON object text, or null for an empty object
     * @return parsed object
     * @throws IllegalArgumentException if the supplied text is not a JSON object
     */
    private static JsonValue.ObjectValue object(String text) {
        if (text == null)
            return new JsonValue.ObjectValue(Map.of());
        JsonValue value;
        try {
            value = JsonKit.readValue(text.getBytes(Charset.UTF_8), 64, true);
        } catch (InternalException error) {
            throw new IllegalArgumentException("Invalid instance JSON object", error);
        }
        if (!(value instanceof JsonValue.ObjectValue object))
            throw new IllegalArgumentException("Instance attributes must be JSON objects");
        return object;
    }

}

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
package org.miaixz.bus.cortex.health;

import java.util.Arrays;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Shared seven-member health JSON value for probe configuration, runtime sessions and server observations. This value
 * owns protocol parsing, validation, serialization and lease evaluation without persistence mapping, scheduling or
 * session execution.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
public class HealthInfo {

    /**
     * Protocol used by the health check, such as HTTP or TCP.
     */
    private String scheme;

    /**
     * HTTP health-check path relative to the instance endpoint.
     */
    private String path;

    /**
     * Source that produced the most recent health observation.
     */
    private String origin;

    /**
     * Host-managed identifier of the current runtime session.
     */
    private String session;

    /**
     * Most recent probe time in epoch milliseconds.
     */
    private Long time;

    /**
     * Server-observed heartbeat time in epoch milliseconds.
     */
    private Long beat;

    /**
     * Server-approved session lifetime in seconds.
     */
    private Integer lease;

    /**
     * Creates an empty health JSON value.
     */
    public HealthInfo() {
        // No initialization required.
    }

    /**
     * Evaluates the recorded session lifetime without consulting instance status or reading a clock.
     *
     * @param now evaluation time in epoch milliseconds
     * @return whether a nonblank session and positive heartbeat remain within the positive lease duration
     */
    public boolean isLeaseValid(long now) {
        return session != null && !session.isBlank() && lease != null && lease > 0 && beat != null && beat > 0
                && now >= beat && now - beat < lease * 1000L;
    }

    /**
     * Parses the fixed health protocol without coercing strings or fractional numbers into numeric members.
     *
     * @param text optional health JSON object text
     * @return complete validated health value
     * @throws IllegalArgumentException if a member, type or numeric range is invalid
     */
    public static HealthInfo parse(String text) {
        HealthInfo result = new HealthInfo();
        object(text).values().forEach((key, value) -> {
            switch (key) {
                case "scheme" -> result.setScheme(text(value));
                case "path" -> result.setPath(text(value));
                case "origin" -> result.setOrigin(text(value));
                case "session" -> result.setSession(text(value));
                case "time" -> result.setTime(number(value));
                case "beat" -> result.setBeat(number(value));
                case "lease" -> {
                    Long lease = number(value);
                    if (lease != null && (lease < 1 || lease > Integer.MAX_VALUE))
                        throw new IllegalArgumentException("Health lease out of range");
                    result.setLease(lease == null ? null : lease.intValue());
                }
                default -> throw new IllegalArgumentException("Unknown health member");
            }
        });
        validate(result);
        return result;
    }

    /**
     * Reads one optional JSON string without scalar coercion.
     *
     * @param value JSON member value
     * @return string value, or null for a JSON null
     * @throws IllegalArgumentException if the value is not a string or null
     */
    private static String text(JsonValue value) {
        if (value instanceof JsonValue.NullValue)
            return null;
        if (!(value instanceof JsonValue.StringValue string))
            throw new IllegalArgumentException("Invalid health text member");
        return string.value();
    }

    /**
     * Reads one optional integral JSON number without rounding or narrowing.
     *
     * @param value JSON member value
     * @return exact numeric value, or null for a JSON null
     * @throws IllegalArgumentException if the value is not an integer in the Long range
     */
    private static Long number(JsonValue value) {
        if (value instanceof JsonValue.NullValue)
            return null;
        if (!(value instanceof JsonValue.NumberValue number))
            throw new IllegalArgumentException("Invalid health numeric member");
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("Health member must be an integer in range", error);
        }
    }

    /**
     * Applies the same member constraints to parsed JSON and typed snapshots before persistence.
     *
     * @param value complete health value
     * @throws IllegalArgumentException if a text length, path, timestamp or lease is invalid
     */
    private static void validate(HealthInfo value) {
        if (value == null)
            throw new IllegalArgumentException("Health value required");
        for (String text : Arrays.asList(value.getScheme(), value.getPath(), value.getOrigin(), value.getSession()))
            if (text != null && text.length() > 1024)
                throw new IllegalArgumentException("Health text too long");
        if (value.getPath() != null && (!value.getPath().startsWith("/") || value.getPath().startsWith("//"))
                || value.getTime() != null && value.getTime() < 0 || value.getBeat() != null && value.getBeat() < 0
                || value.getLease() != null && value.getLease() < 1)
            throw new IllegalArgumentException("Invalid health value");
    }

    /**
     * Validates and serializes this complete snapshot using the existing health JSON structure. Null members clear
     * values; callers retain ownership of concurrency fencing and of the individual members they change.
     *
     * @return health JSON object text
     * @throws IllegalArgumentException if a text length, path, timestamp or lease is invalid
     */
    public String toJson() {
        validate(this);
        return JsonKit.toJsonString(this);
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
            throw new IllegalArgumentException("Invalid health JSON object", error);
        }
        if (!(value instanceof JsonValue.ObjectValue object))
            throw new IllegalArgumentException("Health attributes must be JSON objects");
        return object;
    }

}

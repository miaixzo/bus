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

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Result of a health check probe.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Status {

    /**
     * Whether the instance is considered healthy.
     */
    private boolean healthy;
    /**
     * Probe round-trip latency in milliseconds.
     */
    private long latencyMs;
    /**
     * Human-readable status or error description.
     */
    private String message;
    /**
     * Logical probe source or component name.
     */
    private String source;
    /**
     * Probe timestamp in epoch milliseconds.
     */
    private long timestamp;
    /**
     * Logical lifecycle state derived from the probe result.
     */
    private String state;
    /**
     * Optional structured probe details.
     */
    @Builder.Default
    private Map<String, String> details = new LinkedHashMap<>();

    /**
     * Creates a new Status.
     */
    private Status() {

    }

    /**
     * Creates a successful health result.
     *
     * @param latencyMs probe latency in milliseconds
     * @return healthy result
     */
    public static Status ok(long latencyMs) {
        return ok(latencyMs, "probe");
    }

    /**
     * Creates a successful health result.
     *
     * @param latencyMs probe latency in milliseconds
     * @param source    probe source
     * @return healthy result
     */
    public static Status ok(long latencyMs, String source) {
        Status r = new Status();
        r.healthy = true;
        r.latencyMs = latencyMs;
        r.message = "OK";
        r.source = source;
        r.state = "UP";
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    /**
     * Creates a failed health result.
     *
     * @param message failure description
     * @return unhealthy result
     */
    public static Status fail(String message) {
        return fail(message, "probe");
    }

    /**
     * Creates a failed health result.
     *
     * @param message failure description
     * @param source  probe source
     * @return unhealthy result
     */
    public static Status fail(String message, String source) {
        Status r = new Status();
        r.healthy = false;
        r.latencyMs = 0L;
        r.message = message;
        r.source = source;
        r.state = "DOWN";
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    /**
     * Adds one structured detail item to the status payload.
     *
     * @param key   detail key
     * @param value detail value
     * @return current status
     */
    public Status detail(String key, String value) {
        if (key != null && value != null) {
            if (details == null) {
                details = new LinkedHashMap<>();
            }
            details.put(key, value);
        }
        return this;
    }

}

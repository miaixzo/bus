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
 */
@Getter
@Setter
@SuperBuilder
public class Status {

    /**
     * Health lifecycle reported by trusted probes.
     */
    public enum Health {
        /**
         * The service instance is reachable and healthy.
         */
        UP,
        /**
         * The service instance is unreachable or unhealthy.
         */
        DOWN,
        /**
         * No reliable probe result is currently available.
         */
        UNKNOWN,
        /**
         * The service instance is still starting and is not ready for normal traffic.
         */
        STARTING,
        /**
         * The service instance has intentionally been removed from normal traffic for maintenance.
         */
        MAINTENANCE
    }

    /**
     * Lifecycle state exposed by managed resources.
     */
    public enum Resource {
        /**
         * The resource is enabled and may participate in normal operations.
         */
        ACTIVE,
        /**
         * The resource is retained but disabled for normal operations.
         */
        DISABLED,
        /**
         * The resource has been archived and is no longer active.
         */
        ARCHIVED
    }

    /**
     * Result of applying a revision to a target.
     */
    public enum Apply {
        /**
         * The target has not yet acknowledged the active revision.
         */
        PENDING,
        /**
         * The target acknowledged and applied the active revision successfully.
         */
        APPLIED,
        /**
         * The target reported that applying the active revision failed.
         */
        FAILED,
        /**
         * No reliable target evidence is available for the active revision.
         */
        UNKNOWN
    }

    /**
     * Availability of a projected capability or target.
     */
    public enum Availability {
        /**
         * The requested capability or projection is available.
         */
        AVAILABLE,
        /**
         * The requested capability or projection is unavailable.
         */
        UNAVAILABLE,
        /**
         * Availability cannot be established from reliable evidence.
         */
        UNKNOWN
    }

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
    public Status() {
        // No initialization required.
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
        r.state = Health.UP.name();
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
        r.state = Health.DOWN.name();
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

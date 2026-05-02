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

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Runtime API instance information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Instance {

    /**
     * Creates an empty runtime instance descriptor.
     */
    public Instance() {

    }

    /**
     * Namespace containing the runtime instance.
     */
    private String namespace_id;
    /**
     * Application identifier to which the runtime instance belongs.
     */
    private String app_id;
    /**
     * Service identifier to which the instance belongs.
     */
    private String serviceId;
    /**
     * Service method name exposed by the instance.
     */
    private String method;
    /**
     * Service version exposed by the instance.
     */
    private String version;
    /**
     * Hostname or IP address of the runtime instance.
     */
    private String host;
    /**
     * Network port of the runtime instance.
     */
    private Integer port;
    /**
     * Load-balancing weight assigned to the instance.
     */
    @Builder.Default
    private Integer weight = 100;
    /**
     * Health flag where {@code 1} means healthy and {@code 0} means unhealthy.
     */
    @Builder.Default
    private Integer healthy = 1;
    /**
     * Human-readable health state of the instance.
     */
    private String state;
    /**
     * Unique fingerprint identifying the runtime instance.
     */
    private String fingerprint;
    /**
     * Operating-system process identifier when available.
     */
    private Long pid;
    /**
     * Access scheme such as HTTP or TCP.
     */
    private String scheme;
    /**
     * Health-check path used for active probes.
     */
    private String healthPath;
    /**
     * Lease duration in seconds used by active registries and self-registration heartbeats.
     */
    @Builder.Default
    private Integer leaseSeconds = 30;
    /**
     * Timestamp of the most recent heartbeat observed for the instance.
     */
    private Long lastHeartbeatAt;
    /**
     * Timestamp of the most recent active health probe.
     */
    private Long lastProbeAt;
    /**
     * Timestamp of the most recent state transition.
     */
    private Long stateChangedAt;
    /**
     * Source that produced the current health state, for example heartbeat, http-probe or tcp-probe.
     */
    private String healthSource;
    /**
     * Detailed status payload from the most recent probe cycle.
     */
    private Status lastStatus;
    /**
     * Structured labels attached to the runtime instance.
     */
    private Map<String, String> labels;
    /**
     * Additional instance metadata published for routing or discovery.
     */
    private Map<String, String> metadata;

    /**
     * Returns whether the instance is currently healthy.
     *
     * @return {@code true} when the instance is marked healthy
     */
    public boolean isHealthyNow() {
        return healthy != null && healthy > 0;
    }

}

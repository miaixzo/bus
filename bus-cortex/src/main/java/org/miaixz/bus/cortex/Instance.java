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

import lombok.Getter;
import lombok.Setter;

/**
 * Runtime API instance information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class Instance {

    /**
     * Namespace containing the runtime instance.
     */
    private String namespace;
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
    private Integer weight = 100;
    /**
     * Health flag where {@code 1} means healthy and {@code 0} means unhealthy.
     */
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
     * Additional instance metadata published for routing or discovery.
     */
    private Map<String, String> metadata;

}

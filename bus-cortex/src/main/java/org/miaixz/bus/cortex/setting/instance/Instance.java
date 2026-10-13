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
package org.miaixz.bus.cortex.setting.instance;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Configuration client instance owned directly by an application within a tenant.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Instance extends Tenant {

    /**
     * Identifier of the application that owns this instance.
     */
    private String app_id;

    /**
     * Human-readable instance name.
     */
    private String name;

    /**
     * Stable instance fingerprint, unique together with the tenant and application identifiers.
     */
    private String fingerprint;

    /**
     * Trusted process identity used to aggregate instances.
     */
    private String process;

    /**
     * Instance hostname or IP address.
     */
    private String host;

    /**
     * Instance port.
     */
    private Integer port;

    /**
     * Instance communication protocol.
     */
    private String scheme;

    /**
     * Optional operating-system process identifier.
     */
    private Long pid;

    /**
     * Optional relative health-probe path.
     */
    private String endpoint;

    /**
     * Instance health status.
     */
    private String health;

    /**
     * Database timestamp of the most recent valid heartbeat.
     */
    private Long heartbeat;

    /**
     * Database timestamp of the most recent health probe.
     */
    private Long probed;

    /**
     * Configuration release version successfully applied by the instance.
     */
    private String version;

    /**
     * Server-approved session lifetime in seconds.
     */
    private Integer duration;

    /**
     * Server-generated session identifier.
     */
    private String session;

    /**
     * Normalized labels in JSON format.
     */
    private String labels;

    /**
     * Creates an empty configuration client instance.
     */
    public Instance() {
        // No initialization required.
    }

}

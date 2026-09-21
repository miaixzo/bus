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

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.cortex.Type;

/**
 * Persistent runtime-instance attributes shared by configuration-center implementations.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Instance extends Tenant {

    /**
     * Registered asset that owns the runtime endpoint.
     */
    private String asset_id;

    /**
     * Stable endpoint fingerprint within the owning asset.
     */
    private String fingerprint;

    /**
     * Trusted process identity used for endpoint aggregation.
     */
    private String process;

    /**
     * Runtime host.
     */
    private String host;

    /**
     * Runtime port.
     */
    private Integer port;

    /**
     * Runtime transport scheme.
     */
    private String scheme;

    /**
     * Operating-system process identifier when available.
     */
    private Long pid;

    /**
     * Relative health-probe endpoint when available.
     */
    private String endpoint;

    /**
     * Persisted health state.
     */
    private String health;

    /**
     * Database timestamp of the latest accepted heartbeat.
     */
    private Long heartbeat;

    /**
     * Database timestamp of the latest health probe.
     */
    private Long probed;

    /**
     * Runtime version when supplied.
     */
    private String version;

    /**
     * Approved session duration in seconds.
     */
    private Integer duration;

    /**
     * Server-generated session identifier.
     */
    private String session;

    /**
     * Persisted normalized labels JSON.
     */
    @Ignore
    private String labels_json;

    /**
     * Persisted adapter metadata JSON.
     */
    @Ignore
    private String metadata_json;

    /**
     * Creates an empty runtime-instance entity.
     */
    public Instance() {
        // No initialization required.
    }

    /**
     * Returns the fixed Cortex resource type without persisting a redundant column.
     *
     * @return stable runtime-instance type key
     */
    @Transient
    public Integer getType() {
        return Type.INSTANCE.key();
    }

    /**
     * Accepts the fixed Cortex resource type for bean compatibility.
     *
     * @param type supplied Cortex type key
     * @throws IllegalArgumentException when a different non-null type is supplied
     */
    @Transient
    public void setType(Integer type) {
        if (type != null && type.intValue() != Type.INSTANCE.key()) {
            throw new IllegalArgumentException("Unsupported type for Instance: " + type);
        }
    }

}

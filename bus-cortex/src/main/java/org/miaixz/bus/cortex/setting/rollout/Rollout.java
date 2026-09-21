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
package org.miaixz.bus.cortex.setting.rollout;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.cortex.Type;

/**
 * Persistent rollout attributes shared by configuration-center implementations.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Rollout extends Tenant {

    /**
     * Environment fixed for the rollout.
     */
    private String profile_id;

    /**
     * Persisted rollout mode.
     */
    private String mode;

    /**
     * Persisted rollout state.
     */
    private String state;

    /**
     * Human-readable rollout title.
     */
    private String title;

    /**
     * Human-readable rollout description.
     */
    private String description;

    /**
     * Frozen approval policy JSON.
     */
    @Ignore
    private String policy_json;

    /**
     * Digest of the normalized rollout plan and policy.
     */
    private String checksum;

    /**
     * Creator-scoped idempotency key.
     */
    private String request;

    /**
     * Digest of the original semantic command.
     */
    private String digest;

    /**
     * Active gray selector JSON when present.
     */
    @Ignore
    private String rule_json;

    /**
     * Gray-rule business edition.
     */
    private Long edition;

    /**
     * Terminal completion timestamp when present.
     */
    private Long finished;

    /**
     * Creates an empty rollout entity.
     */
    public Rollout() {
        // No initialization required.
    }

    /**
     * Returns the fixed Cortex rollout type.
     *
     * @return stable rollout type key
     */
    @Transient
    public Integer getType() {
        return Type.ROLLOUT.key();
    }

    /**
     * Accepts the fixed Cortex rollout type for bean compatibility.
     *
     * @param type supplied Cortex type key
     * @throws IllegalArgumentException when a different non-null type is supplied
     */
    @Transient
    public void setType(Integer type) {
        if (type != null && type.intValue() != Type.ROLLOUT.key()) {
            throw new IllegalArgumentException("Unsupported type for Rollout: " + type);
        }
    }

}

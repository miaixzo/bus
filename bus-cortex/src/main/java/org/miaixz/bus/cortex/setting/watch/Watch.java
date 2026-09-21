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
package org.miaixz.bus.cortex.setting.watch;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.cortex.Type;

/**
 * Persistent runtime-watch attributes shared by configuration-center implementations.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Watch extends Tenant {

    /**
     * Runtime instance that owns the watch.
     */
    private String instance_id;

    /**
     * Configuration item observed by the watch.
     */
    private String item_id;

    /**
     * Runtime session captured when the watch was established.
     */
    private String session;

    /**
     * Revision currently authorized for this watch.
     */
    private String target_revision_id;

    /**
     * Current authorized target generation.
     */
    private Long generation;

    /**
     * Highest generation with committed fetch evidence.
     */
    private Long fetched;

    /**
     * Highest successfully applied generation.
     */
    private Long applied;

    /**
     * Revision confirmed by the latest successful acknowledgement.
     */
    private String last_applied_revision_id;

    /**
     * Database timestamp of the latest acknowledgement.
     */
    private Long acknowledged;

    /**
     * Latest redacted application error.
     */
    private String error;

    /**
     * Creates an empty runtime-watch entity.
     */
    public Watch() {
        // No initialization required.
    }

    /**
     * Returns the fixed Cortex runtime-watch type.
     *
     * @return stable runtime-watch type key
     */
    @Transient
    public Integer getType() {
        return Type.WATCH.key();
    }

    /**
     * Accepts the fixed Cortex runtime-watch type for bean compatibility.
     *
     * @param type supplied Cortex type key
     * @throws IllegalArgumentException when a different non-null type is supplied
     */
    @Transient
    public void setType(Integer type) {
        if (type != null && type.intValue() != Type.WATCH.key()) {
            throw new IllegalArgumentException("Unsupported type for Watch: " + type);
        }
    }

}

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

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Status;

/**
 * Persistent runtime-watch attributes shared by configuration-center implementations.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Watch extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558929921288L;

    /**
     * Configuration item observed by the watch.
     */
    private String item_id;

    /**
     * Runtime instance that owns the watch.
     */
    private String instance_id;

    /**
     * Exact Release ID confirmed by the latest successful acknowledgement.
     */
    private String applied_id;

    /**
     * Exact Release ID currently selected for this watch; item_id still identifies the source Item.
     */
    private String target_id;

    /**
     * Runtime session captured when the watch was established.
     */
    private String session;

    /**
     * Current selected target generation.
     */
    private Long generation;

    /**
     * Database timestamp of the latest acknowledgement.
     */
    private Long acknowledged;

    /**
     * Latest redacted application error.
     */
    private String error;

    /**
     * Highest generation with committed fetch evidence.
     */
    private Long fetched;

    /**
     * Highest successfully applied generation.
     */
    private Long applied;

    /**
     * Creates an empty runtime-watch entity.
     */
    public Watch() {
        // No initialization required.
    }

    /**
     * Derives application evidence for exactly one watch; transport delivery is not application success.
     *
     * @param instance trusted runtime instance whose tenant, identifier and session must match this watch
     * @return UNKNOWN for invalid evidence, FAILED for a recorded error, APPLIED for matching acknowledgments,
     *         otherwise PENDING
     */
    public Status.Apply synchronization(Instance instance) {
        if (instance == null || getTenant_id() == null || !getTenant_id().equals(instance.getTenant_id())
                || instance_id == null || !instance_id.equals(instance.getId()) || session == null
                || !session.equals(instance.healthView().getSession()) || generation == null || generation < 1
                || target_id == null || target_id.isBlank()) {
            return Status.Apply.UNKNOWN;
        }
        if (error != null && !error.isBlank()) {
            return Status.Apply.FAILED;
        }
        return generation.equals(applied) && generation.equals(fetched) && target_id.equals(applied_id)
                && acknowledged != null && acknowledged > 0 ? Status.Apply.APPLIED : Status.Apply.PENDING;
    }

}

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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.cortex.setting.instance.Instance;

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
    private String target_id;

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
    private String applied_id;

    /**
     * Database timestamp of the latest acknowledgement.
     */
    private Long acknowledged;

    /**
     * Latest redacted application error.
     */
    private String error;

    /**
     * Configuration application state, independent from process health.
     */
    public enum Synchronization {
        /**
         * Application evidence is incomplete or does not match this watch.
         */
        UNKNOWN,
        /**
         * The target revision has not yet been fully fetched and applied.
         */
        PENDING,
        /**
         * The target revision was fetched and applied successfully.
         */
        APPLIED,
        /**
         * The application reported an error for the target revision.
         */
        FAILED
    }

    /**
     * Derives application evidence for exactly one watch; transport delivery is not application success.
     */
    public Synchronization synchronization(Instance instance) {
        if (instance == null || getTenant_id() == null || !getTenant_id().equals(instance.getTenant_id())
                || instance_id == null || !instance_id.equals(instance.getId()) || session == null
                || !session.equals(instance.getSession()) || generation == null || generation < 1 || target_id == null
                || target_id.isBlank()) {
            return Synchronization.UNKNOWN;
        }
        if (error != null && !error.isBlank()) {
            return Synchronization.FAILED;
        }
        return generation.equals(applied) && generation.equals(fetched) && target_id.equals(applied_id)
                && acknowledged != null && acknowledged > 0 ? Synchronization.APPLIED : Synchronization.PENDING;
    }

    /**
     * Creates an empty runtime-watch entity.
     */
    public Watch() {
        // No initialization required.
    }

}

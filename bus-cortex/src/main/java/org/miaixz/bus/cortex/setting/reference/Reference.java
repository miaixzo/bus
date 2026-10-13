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
package org.miaixz.bus.cortex.setting.reference;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Directed relationship between service-center resources within one tenant.
 * <p>
 * Shared configuration references use {@link Type#ITEM_APP}; application-owned items use their own application
 * identifier instead of an additional relationship. Rollout application selection uses {@link Type#ROLLOUT_APP},
 * while configuration membership uses {@link Type#ROLLOUT_ITEM}.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Reference extends Tenant {

    /**
     * Source resource identifier: a shared item for ITEM_APP, or a rollout for ROLLOUT_APP and ROLLOUT_ITEM.
     */
    private String source_id;
    /**
     * Target resource identifier: an application for ITEM_APP and ROLLOUT_APP, or an item for ROLLOUT_ITEM.
     */
    private String target_id;
    /**
     * Relationship type name from {@link Type}.
     */
    private String type;

    /** Application-local name of the referenced shared configuration. */
    private String alias;

    /** Follow policy for ITEM_APP; absent values mean LATEST. */
    private String strategy;

    /** Immutable revision identifier required for FIXED references. */
    private String revision_id;

    /** Monotonic relationship edition used for compare-and-set updates. */
    private Long edition;

    /** Supported shared-reference follow policies. */
    public enum Strategy {
        LATEST, FIXED
    }

    /** Validates coordinates and prevents release membership from carrying reference-only options. */
    public Reference validate() {
        org.miaixz.bus.cortex.setting.item.ItemNormalizer.requireTenant(getTenant_id());
        if (source_id == null || source_id.isBlank() || target_id == null || target_id.isBlank()) {
            throw new IllegalArgumentException("Relationship endpoints are required");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Relationship type is required");
        }
        Type relation = Type.valueOf(type);
        if (relation == Type.ITEM_APP) {
            Strategy policy = strategy == null ? Strategy.LATEST : Strategy.valueOf(strategy);
            if (alias == null || alias.isBlank()) {
                throw new IllegalArgumentException("Shared references require an application-local alias");
            }
            if ((policy == Strategy.FIXED) != (revision_id != null && !revision_id.isBlank())) {
                throw new IllegalArgumentException("Only FIXED references require a revision identifier");
            }
        } else if (alias != null || strategy != null || revision_id != null) {
            throw new IllegalArgumentException("Release membership cannot contain shared-reference options");
        }
        return this;
    }

    /**
     * Creates an empty relationship.
     */
    public Reference() {
        // No initialization required.
    }

    /**
     * Supported relationship types.
     *
     * @author Kimi Liu
     */
    public enum Type {
        /**
         * Associates a shared configuration item with an application that references it.
         */
        ITEM_APP,
        /**
         * Associates a rollout with an application explicitly selected for that rollout.
         */
        ROLLOUT_APP,
        /**
         * Associates a rollout with an item.
         */
        ROLLOUT_ITEM
    }

}

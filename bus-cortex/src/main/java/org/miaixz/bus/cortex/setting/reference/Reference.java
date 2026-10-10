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
 * Directed relationship between two service-center resources.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Reference extends Tenant {

    /**
     * Source resource identifier.
     */
    private String source_id;
    /**
     * Target resource identifier.
     */
    private String target_id;
    /**
     * Relationship type name.
     */
    private String type;

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
        /** Associates a space with an application. */
        SPACE_APP,
        /** Associates a space with a profile. */
        SPACE_PROFILE,
        /** Associates an application with a profile. */
        APP_PROFILE,
        /** Associates an item with an application. */
        ITEM_APP,
        /** Associates an item with another item dependency. */
        ITEM_REFERENCE,
        /** Associates a rollout with an item. */
        ROLLOUT_ITEM
    }

}

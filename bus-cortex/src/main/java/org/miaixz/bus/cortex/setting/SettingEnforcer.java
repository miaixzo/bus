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
package org.miaixz.bus.cortex.setting;

import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemQuery;
import org.miaixz.bus.cortex.setting.item.ItemScope;

/**
 * Centralized enforcer for namespace/app/profile relationship validation within the setting domain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SettingEnforcer {

    /**
     * Validates a setting item before mutation.
     *
     * @param entry setting item
     * @return validated setting item
     */
    default Item validateItem(Item entry) {
        return entry;
    }

    /**
     * Validates a setting query before resolution.
     *
     * @param query setting query
     * @return validated setting query
     */
    default ItemQuery validateQuery(ItemQuery query) {
        return query;
    }

    /**
     * Validates a setting scope before export or maintenance operations.
     *
     * @param scope setting scope
     * @return validated setting scope
     */
    default ItemScope validateScope(ItemScope scope) {
        return scope;
    }

    /**
     * Returns whether a namespace, application, and profile relation is allowed.
     *
     * @param namespace_id namespace identifier
     * @param app_id       application identifier
     * @param profile_id   profile identifier
     * @return {@code true} when the relation is allowed
     */
    default boolean allows(String namespace_id, String app_id, String profile_id) {
        return true;
    }

}

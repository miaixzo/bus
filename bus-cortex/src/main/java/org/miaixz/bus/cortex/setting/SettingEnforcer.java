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
 * Business validation contract for configuration tenants, applications, and ownership scopes.
 */
public interface SettingEnforcer {

    /**
     * Validates or normalizes an item before it is staged.
     *
     * @param entry item supplied by the caller
     * @return validated item
     */
    default Item validateItem(Item entry) {
        return entry;
    }

    /**
     * Validates or normalizes a configuration query.
     *
     * @param query query supplied by the caller
     * @return validated query
     */
    default ItemQuery validateQuery(ItemQuery query) {
        return query;
    }

    /**
     * Validates or normalizes a configuration scope.
     *
     * @param scope scope supplied by the caller
     * @return validated scope
     */
    default ItemScope validateScope(ItemScope scope) {
        return scope;
    }

    /**
     * Checks access to an application and ownership scope within the specified tenant.
     */
    default boolean allows(String tenant_id, String app_id, String scope) {
        return true;
    }

}

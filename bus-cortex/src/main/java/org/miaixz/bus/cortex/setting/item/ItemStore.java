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
package org.miaixz.bus.cortex.setting.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Durable store contract for current setting state.
 *
 * @author Kimi Liu
 */
public interface ItemStore {

    /**
     * Persists the current state of one item.
     *
     * @param entry item state to persist
     * @return persisted item state
     */
    Item save(Item entry);

    /**
     * Persists each non-null item in encounter order.
     *
     * @param entries item states to persist
     * @return persisted item states
     */
    default List<Item> saveAll(List<Item> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> result = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Deletes one item by its tenant and identifier.
     *
     * @param tenant_id tenant identifier, when available
     * @param id        item identifier
     * @return deleted item, or {@code null} when absent
     */
    Item delete(String tenant_id, String id);

    /**
     * Finds one item by its immutable business coordinates.
     *
     * @param tenant_id  tenant identifier, when available
     * @param space_id   space identifier
     * @param profile_id profile identifier, when applicable
     * @param group      item group
     * @param code       item code
     * @return matching item, or {@code null} when absent
     */
    Item find(String tenant_id, String space_id, String profile_id, String group, String code);

    /**
     * Queries items using the supplied business criteria.
     *
     * @param query item query
     * @return matching items
     */
    List<Item> query(ItemQuery query);
}

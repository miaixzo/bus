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

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable source-of-truth contract for current-state setting entries.
 *
 * <p>
 * This mirrors {@code RegistryStore}: implementations persist the durable model only. {@link StoreBackedItemStore}
 * coordinates this store with {@code CacheX} for hot reads and cache rebuilds.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ItemStore {

    /**
     * Creates or updates one current-state setting entry in durable storage.
     *
     * @param entry entry to store
     * @return stored entry snapshot
     */
    Item save(Item entry);

    /**
     * Saves a batch of current-state setting entries.
     *
     * @param entries setting entries
     * @return stored entry snapshots
     */
    default List<Item> saveAll(List<Item> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<Item> result = new java.util.ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Deletes one current-state setting entry from durable storage.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return deleted entry snapshot, or {@code null} when absent
     */
    Item delete(String namespace, String group, String data_id, String profile);

    /**
     * Finds one current-state setting entry in durable storage.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return matching entry or {@code null}
     */
    Item find(String namespace, String group, String data_id, String profile);

    /**
     * Queries current-state setting entries from durable storage.
     *
     * @param query query filter
     * @return matching entries
     */
    List<Item> query(ItemQuery query);

    /**
     * Returns strongly typed durable-store capability hints.
     *
     * @return capability flags
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE);
    }

    /**
     * Returns durable-store capability hints using legacy string keys.
     *
     * @return capability flags
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}

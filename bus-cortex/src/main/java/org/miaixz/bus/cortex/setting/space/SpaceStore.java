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
package org.miaixz.bus.cortex.setting.space;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Directory contract for {@code setting.space}.
 *
 * @author Kimi Liu
 */
public interface SpaceStore {

    /**
     * Saves one space entry.
     *
     * @param entry space entry
     * @return stored space entry
     */
    Space save(Space entry);

    /**
     * Saves a batch of space entries.
     *
     * @param entries space entries
     * @return stored snapshots
     */
    default List<Space> saveAll(List<Space> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<Space> result = new java.util.ArrayList<>(entries.size());
        for (Space entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Finds one space entry.
     *
     * @param id space identifier
     * @return matching space entry or {@code null}
     */
    default Space find(String id) {
        return null;
    }

    /**
     * Lists all space entries.
     *
     * @return space entries
     */
    default List<Space> query() {
        return List.of();
    }

    /**
     * Deletes one space entry.
     *
     * @param id space identifier
     * @return deleted snapshot or {@code null} when absent
     */
    default Space delete(String id) {
        return null;
    }

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

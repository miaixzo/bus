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

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Durable directory contract for shared logical spaces.
 *
 * @author Kimi Liu
 */
public interface SpaceStore {

    /**
     * Saves one workspace or namespace record.
     *
     * @param entry space entity with explicit integer variant and visibility codes
     * @return stored space entity
     */
    Space save(Space entry);

    /**
     * Saves a batch of space records.
     *
     * @param entries space entities
     * @return stored snapshots
     */
    default List<Space> saveAll(List<Space> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Space> result = new ArrayList<>(entries.size());
        for (Space entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Finds one space by its permanent identifier.
     *
     * @param id space identifier
     * @return matching entity or {@code null}
     */
    default Space find(String id) {
        return null;
    }

    /**
     * Finds one space and verifies its persisted variant.
     *
     * @param id      space identifier
     * @param variant required integer {@link EnumValue.Variant} code
     * @return matching entity or {@code null}
     */
    default Space find(String id, Integer variant) {
        Space entry = find(id);
        return entry != null && (variant == null || variant.equals(entry.getVariant())) ? entry : null;
    }

    /**
     * Lists spaces, optionally restricted to one persisted variant.
     *
     * @param variant integer {@link EnumValue.Variant} code, or {@code null} for all variants
     * @return matching space entities
     */
    default List<Space> query(Integer variant) {
        return List.of();
    }

    /**
     * Lists workspace and namespace records together.
     *
     * @return all visible space entities
     */
    default List<Space> query() {
        return List.of();
    }

    /**
     * Deletes one space by its permanent identifier.
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

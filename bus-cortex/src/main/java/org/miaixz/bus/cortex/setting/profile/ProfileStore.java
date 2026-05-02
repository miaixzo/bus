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
package org.miaixz.bus.cortex.setting.profile;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for {@code setting.profile} directory entries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ProfileStore {

    /**
     * Saves one profile directory entry.
     *
     * @param entry profile entry
     * @return stored snapshot
     */
    Profile save(Profile entry);

    /**
     * Saves a batch of profile entries.
     *
     * @param entries profile entries
     * @return stored snapshots
     */
    default List<Profile> saveAll(List<Profile> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<Profile> result = new java.util.ArrayList<>(entries.size());
        for (Profile entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Deletes one profile entry.
     *
     * @param namespace_id namespace identifier
     * @param profile_id   profile identifier
     * @return deleted snapshot or {@code null} when absent
     */
    Profile delete(String namespace_id, String profile_id);

    /**
     * Finds one profile entry.
     *
     * @param namespace_id namespace identifier
     * @param profile_id   profile identifier
     * @return stored entry or {@code null}
     */
    Profile find(String namespace_id, String profile_id);

    /**
     * Queries profile entries within one namespace.
     *
     * @param namespace_id namespace identifier
     * @return matching entries
     */
    List<Profile> query(String namespace_id);

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

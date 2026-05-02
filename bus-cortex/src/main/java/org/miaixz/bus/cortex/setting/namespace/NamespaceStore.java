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
package org.miaixz.bus.cortex.setting.namespace;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Directory contract for {@code setting.namespace}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface NamespaceStore {

    /**
     * Saves one namespace entry.
     *
     * @param entry namespace entry
     * @return stored namespace entry
     */
    Namespace save(Namespace entry);

    /**
     * Saves a batch of namespace entries.
     *
     * @param entries namespace entries
     * @return stored snapshots
     */
    default List<Namespace> saveAll(List<Namespace> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<Namespace> result = new java.util.ArrayList<>(entries.size());
        for (Namespace entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Finds one namespace entry.
     *
     * @param id namespace identifier
     * @return matching namespace entry or {@code null}
     */
    default Namespace find(String id) {
        return null;
    }

    /**
     * Lists all namespace entries.
     *
     * @return namespace entries
     */
    default List<Namespace> query() {
        return List.of();
    }

    /**
     * Deletes one namespace entry.
     *
     * @param id namespace identifier
     * @return deleted snapshot or {@code null} when absent
     */
    default Namespace delete(String id) {
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

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
package org.miaixz.bus.cortex.setting.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for setting binding rows.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface BindingStore {

    /**
     * Creates or updates one binding row.
     *
     * @param binding binding row
     * @return stored binding snapshot
     */
    Binding save(Binding binding);

    /**
     * Saves a batch of binding rows.
     *
     * @param bindings binding rows
     * @return stored binding snapshots
     */
    default List<Binding> saveAll(List<Binding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        List<Binding> result = new ArrayList<>(bindings.size());
        for (Binding binding : bindings) {
            if (binding != null) {
                result.add(save(binding));
            }
        }
        return result;
    }

    /**
     * Deletes one binding row.
     *
     * @param ownerId owning resource identifier
     * @param type    binding target type
     * @param refId   binding target identifier
     * @return deleted binding snapshot, or {@code null} when absent
     */
    Binding delete(String ownerId, String type, String refId);

    /**
     * Finds one binding row.
     *
     * @param ownerId owning resource identifier
     * @param type    binding target type
     * @param refId   binding target identifier
     * @return matching binding row, or {@code null}
     */
    Binding find(String ownerId, String type, String refId);

    /**
     * Queries binding rows for one owner and optional target type.
     *
     * @param ownerId owning resource identifier
     * @param type    optional binding target type
     * @return matching binding rows
     */
    List<Binding> query(String ownerId, String type);

    /**
     * Returns strongly typed durable-store capability hints.
     *
     * @return capability flags
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE, Trait.DELETE);
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

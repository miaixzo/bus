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
package org.miaixz.bus.cortex.setting.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for runtime-instance entities.
 *
 * @author Kimi Liu
 */
public interface InstanceStore {

    /**
     * Saves one runtime instance.
     *
     * @param instance runtime instance
     * @return saved runtime instance
     */
    Instance save(Instance instance);

    /**
     * Saves a batch of runtime instances.
     *
     * @param instances runtime instances
     * @return saved non-null runtime instances
     */
    default List<Instance> saveAll(List<Instance> instances) {
        if (instances == null || instances.isEmpty()) {
            return List.of();
        }
        List<Instance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            if (instance != null) {
                result.add(save(instance));
            }
        }
        return result;
    }

    /**
     * Finds one runtime instance by identifier.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id runtime instance identifier
     * @return matched runtime instance, or {@code null}
     */
    Instance find(String tenant_id, String instance_id);

    /**
     * Finds one runtime instance by asset and fingerprint.
     *
     * @param tenant_id   tenant identifier
     * @param asset_id    registered asset identifier
     * @param fingerprint endpoint fingerprint
     * @return matched runtime instance, or {@code null}
     */
    Instance find(String tenant_id, String asset_id, String fingerprint);

    /**
     * Queries runtime instances belonging to an asset.
     *
     * @param tenant_id tenant identifier
     * @param asset_id  registered asset identifier
     * @return matching runtime instances
     */
    List<Instance> query(String tenant_id, String asset_id);

    /**
     * Deletes one runtime instance.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id runtime instance identifier
     * @return deleted runtime instance, or {@code null}
     */
    Instance delete(String tenant_id, String instance_id);

    /**
     * Returns strongly typed durable-store capabilities.
     *
     * @return fixed runtime-instance store capabilities
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE, Trait.DELETE);
    }

    /**
     * Returns legacy string-keyed durable-store capabilities.
     *
     * @return fixed runtime-instance store capabilities
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}

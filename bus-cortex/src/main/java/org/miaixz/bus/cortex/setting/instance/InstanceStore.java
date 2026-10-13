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

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Persistence contract for configuration client instances.
 *
 * @author Kimi Liu
 */
public interface InstanceStore {

    /**
     * Saves a configuration client instance.
     *
     * @param instance configuration client instance
     * @return the saved instance
     */
    Instance save(Instance instance);

    /**
     * Saves a batch of configuration client instances.
     *
     * @param instances configuration client instances
     * @return non-null saved instances
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
     * Finds an instance by tenant and instance identifier.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id instance identifier
     * @return the matching instance, or {@code null} when absent
     */
    Instance find(String tenant_id, String instance_id);

    /**
     * Finds a unique instance by tenant, application, and fingerprint.
     *
     * @param tenant_id   tenant identifier
     * @param app_id      application identifier
     * @param fingerprint instance fingerprint
     * @return the matching instance, or {@code null} when absent
     */
    Instance find(String tenant_id, String app_id, String fingerprint);

    /**
     * Queries instances owned by an application within the specified tenant.
     *
     * @param tenant_id tenant identifier
     * @param app_id    application identifier
     * @return matching instances
     */
    List<Instance> query(String tenant_id, String app_id);

    /**
     * Deletes an instance by tenant and instance identifier.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id instance identifier
     * @return the deleted instance, or {@code null} when absent
     */
    Instance delete(String tenant_id, String instance_id);

    /**
     * Returns strongly typed persistence capabilities.
     *
     * @return capabilities supported by the instance store
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE, Trait.DELETE);
    }


}

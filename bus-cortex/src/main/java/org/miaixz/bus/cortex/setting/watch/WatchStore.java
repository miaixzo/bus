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
package org.miaixz.bus.cortex.setting.watch;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Runtime target and acknowledgement storage with session, target and generation compare-and-set.
 *
 * @author Kimi Liu
 */
public interface WatchStore {

    /**
     * Saves a tenant-qualified runtime watch. Target transitions and acknowledgements use their dedicated
     * compare-and-set operations.
     *
     * @param watch runtime watch to save
     * @return saved runtime watch
     */
    Watch save(Watch watch);

    /**
     * Finds a runtime watch by its exact tenant-qualified identity.
     *
     * @param tenant_id required tenant identity
     * @param watch_id  exact subscription identifier
     * @return the matching detached tenant-scoped record, or null when absent
     */
    Watch find(String tenant_id, String watch_id);

    /**
     * Finds the runtime watch for an exact tenant, instance and source item.
     *
     * @param tenant_id   required tenant identity
     * @param instance_id exact runtime instance identifier
     * @param item_id     exact source Item ID
     * @return the matching detached tenant-scoped record, or null when absent
     */
    Watch find(String tenant_id, String instance_id, String item_id);

    /**
     * Lists runtime watches owned by the specified instance within the tenant.
     *
     * @param tenant_id   required tenant identity
     * @param instance_id exact runtime instance identifier
     * @return watches belonging to the specified instance
     */
    List<Watch> query(String tenant_id, String instance_id);

    /**
     * Deletes the runtime watch identified by tenant and watch identifier.
     *
     * @param tenant_id required tenant identity
     * @param watch_id  exact subscription identifier
     * @return deleted runtime watch, or null when absent
     */
    Watch delete(String tenant_id, String watch_id);

    /**
     * Atomically compares session, target and generation; advances the generation only when the target changes.
     *
     * @param next     desired authorized target state
     * @param expected complete expected state for compare-and-set
     * @return committed target state
     */
    Watch target(Watch next, Watch expected);

    /**
     * Compares the current authorized session, target and generation; rejects stale acknowledgements.
     *
     * @param acknowledgement runtime application evidence for the expected target
     * @param expected        complete expected state for compare-and-set
     * @return committed runtime acknowledgement
     */
    Watch acknowledge(Watch acknowledgement, Watch expected);

    /**
     * Saves runtime watch records through the existing single-record contract.
     *
     * @param watches runtime watch records; null records are skipped
     * @return results of saving each non-null watch in input order
     */
    default List<Watch> saveAll(List<Watch> watches) {
        if (watches == null || watches.isEmpty()) {
            return List.of();
        }
        ArrayList<Watch> result = new ArrayList<>(watches.size());
        for (Watch value : watches) {
            if (value != null) {
                result.add(save(value));
            }
        }
        return result;
    }

    /**
     * Reports the persistence operations required by this store contract.
     *
     * @return the required persistence capabilities
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE, Trait.DELETE);
    }

}

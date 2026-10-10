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
package org.miaixz.bus.cortex.setting.rollout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for rollout entities.
 *
 * @author Kimi Liu
 */
public interface RolloutStore {

    /**
     * Saves one rollout.
     *
     * @param rollout rollout entity
     * @return saved rollout
     */
    Rollout save(Rollout rollout);

    /**
     * Saves a batch of rollouts.
     *
     * @param rollouts rollout entities
     * @return saved non-null rollouts
     */
    default List<Rollout> saveAll(List<Rollout> rollouts) {
        if (rollouts == null || rollouts.isEmpty()) {
            return List.of();
        }
        List<Rollout> result = new ArrayList<>(rollouts.size());
        for (Rollout rollout : rollouts) {
            if (rollout != null) {
                result.add(save(rollout));
            }
        }
        return result;
    }

    /**
     * Finds one rollout by identifier.
     *
     * @param tenant_id  tenant identifier
     * @param rollout_id rollout identifier
     * @return matched rollout, or {@code null}
     */
    Rollout find(String tenant_id, String rollout_id);

    /**
     * Queries rollouts by environment and optional state.
     *
     * @param tenant_id  tenant identifier
     * @param profile_id environment identifier
     * @param state      optional rollout state
     * @return matching rollouts
     */
    List<Rollout> query(String tenant_id, String profile_id, String state);

    /**
     * Returns strongly typed durable-store capabilities.
     *
     * @return fixed rollout store capabilities
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE);
    }

    /**
     * Returns legacy string-keyed durable-store capabilities.
     *
     * @return fixed rollout store capabilities
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}

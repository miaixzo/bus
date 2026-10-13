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

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for tenant-scoped rollout entities. Tenant identifiers are mandatory and must match the
 * authenticated tenant. Application and configuration membership belongs to Reference relationships, not rollout
 * fields. Publication intent is persisted on the rollout itself; no separate task entity or in-memory queue is
 * required. Recovery resumes PUBLISHING rollouts using the frozen plan and an atomic tenant-scoped activation
 * transaction. Client fetch/application evidence belongs to Watch, and audit history belongs to Event.
 *
 * @author Kimi Liu
 */
public interface RolloutStore {

    /**
     * Inserts a new draft rollout without overwriting any existing identifier or idempotency key.
     *
     * @param rollout new rollout with tenant, identifier, mode, DRAFT state, and initial edition 1
     * @return saved rollout
     */
    Rollout save(Rollout rollout);

    /**
     * Applies a selective update using an atomic tenant, identifier, edition, and state comparison. Implementations
     * increment the edition by one and reject stale or mismatched coordinates. Workflow changes must satisfy
     * State.canTransitionTo and cannot bypass approval. Approval policy, mode, rule, and frozen plan must not change
     * after draft submission. Creator identity and original request identity must never be replaced. Starting
     * publication requires a new publication idempotency key and an authenticated publisher. The publication identity
     * must remain unchanged throughout execution and completion.
     *
     * @param rollout  changes with matching tenant and identifier; null fields remain unchanged
     * @param expected tenant, identifier, positive previous edition, and previous workflow state
     * @return updated rollout
     * @throws IllegalStateException if the expected state or edition no longer matches
     */
    Rollout update(Rollout rollout, Rollout expected);

    /**
     * Inserts a batch of new rollouts.
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
     * @param rollout mandatory tenant and rollout identifiers
     * @return matched rollout, or {@code null}
     */
    Rollout find(Rollout rollout);

    /**
     * Queries rollouts within one tenant, optionally narrowed by workflow state and publication mode.
     *
     * @param rollout mandatory tenant identifier with optional supported state and mode
     * @return matching rollouts
     */
    List<Rollout> query(Rollout rollout);

    /**
     * Returns strongly typed durable-store capabilities.
     *
     * @return fixed rollout store capabilities
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE);
    }

}

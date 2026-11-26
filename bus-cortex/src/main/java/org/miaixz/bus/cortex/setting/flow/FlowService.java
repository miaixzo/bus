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
package org.miaixz.bus.cortex.setting.flow;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.rollout.Rollout;

/**
 * Reads and records flow stage decisions against frozen publication plans.
 *
 * @author Kimi Liu
 */
public final class FlowService {

    /**
     * Host storage for flow records and atomic rollout transitions.
     */
    private final FlowStore store;

    /**
     * Creates the flow service.
     *
     * @param store flow records and decision transactions
     */
    public FlowService(FlowStore store) {
        this.store = Objects.requireNonNull(store);
    }

    /**
     * Reads the recorded stage decisions for a tenant's rollout.
     *
     * @param context   operation coordinates
     * @param rolloutId rollout identifier
     * @return immutable list of flow records
     */
    public List<Flow> query(SettingProvider.Context context, String rolloutId) {
        return List.copyOf(store.query(context.tenant(), rolloutId));
    }

    /**
     * Records a stage decision for the frozen plan. The host transaction rechecks the policy and rollout state.
     *
     * @param context        operation coordinates
     * @param rolloutId      rollout identifier
     * @param edition        expected rollout edition
     * @param frozenChecksum checksum of the frozen publication plan
     * @param stage          stage identifier in the flow policy
     * @param decision       decision for the stage
     * @param target         optional assignee for the stage
     * @param comment        actor's comment
     * @return rollout after recording the decision
     */
    public Rollout decide(
            SettingProvider.Context context,
            String rolloutId,
            long edition,
            String frozenChecksum,
            String stage,
            String decision,
            String target,
            String comment) {
        context.requireRequest();
        context.requireActor();
        if (frozenChecksum == null || stage == null || decision == null)
            throw new IllegalArgumentException("Flow decision requires a frozen checksum, stage, and outcome");
        Flow flow = Flow.builder().tenant_id(context.tenant()).rollout_id(rolloutId).target_id(target).stage(stage)
                .checksum(frozenChecksum).decision(decision).comment(comment).build();
        return store.decide(context, rolloutId, edition, frozenChecksum, flow);
    }

}

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
package org.miaixz.bus.cortex.setting.approval;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.rollout.Rollout;

/**
 * Approval decisions against exact frozen input, using the host's existing policy.
 *
 * @author Kimi Liu
 */
public final class ApprovalService {

    /**
     * Atomic decision/workflow transaction.
     */
    private final ApprovalStore store;
    /**
     * Optional host access policy.
     */
    private final CortexGuard guard;

    /**
     * Creates approval handling.
     *
     * @param store atomic decisions
     * @param guard optional host access policy
     */
    public ApprovalService(ApprovalStore store, CortexGuard guard) {
        this.store = Objects.requireNonNull(store);
        this.guard = guard;
    }

    /**
     * Reads authorized approval evidence.
     *
     * @param context operation coordinates
     * @param rollout workflow
     * @return immutable evidence
     */
    public List<Approval> query(SettingProvider.Context context, String rollout) {
        context.check(guard, "read", "approval", rollout);
        return List.copyOf(store.query(context.tenant(), rollout));
    }

    /**
     * Applies a decision only to the frozen checksum; host transaction repeats policy/state checks.
     *
     * @param context        operation coordinates
     * @param rollout        workflow
     * @param edition        expected CAS
     * @param frozenChecksum reviewed digest
     * @param stage          policy stage
     * @param decision       policy decision
     * @param recipient      optional delegation subject
     * @param comment        opinion
     * @return resulting workflow
     */
    public Rollout decide(
            SettingProvider.Context context,
            String rollout,
            long edition,
            String frozenChecksum,
            String stage,
            String decision,
            String recipient,
            String comment) {
        context.requireRequest();
        context.requireActor();
        context.check(guard, "approve", "rollout", rollout);
        if (frozenChecksum == null || stage == null || decision == null)
            throw new IllegalArgumentException("Exact approval input required");
        Approval evidence = Approval.builder().tenant_id(context.tenant()).rollout_id(rollout).target_id(recipient)
                .stage(stage).checksum(frozenChecksum).decision(decision).comment(comment).build();
        return store.decide(context, rollout, edition, frozenChecksum, evidence);
    }

}

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

import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.rollout.Rollout;

/**
 * Approval evidence and workflow decision share one host entity transaction.
 *
 * @author Kimi Liu
 */
public interface ApprovalStore {

    /**
     * Reads immutable approval evidence for the workflow.
     *
     * @param tenant  exact tenant identifier
     * @param rollout publication workflow identifier
     * @return immutable decisions recorded for the workflow
     */
    List<Approval> query(String tenant, String rollout);

    /**
     * Atomically rechecks reviewer, stage, policy, edition and frozen checksum, writes the decision and advances
     * REVIEWING to the correct next state; identical request is idempotent.
     *
     * @param context        authenticated reviewer and request identity
     * @param rollout        exact workflow identifier
     * @param edition        expected workflow concurrency counter
     * @param frozenChecksum checksum of the reviewed frozen plan
     * @param decision       policy-stage decision to record
     * @return workflow state resulting from the decision
     */
    Rollout decide(
            SettingEnforcer.Context context,
            String rollout,
            long edition,
            String frozenChecksum,
            Approval decision);

}

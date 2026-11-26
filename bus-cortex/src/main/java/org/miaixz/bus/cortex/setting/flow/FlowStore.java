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

import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.rollout.Rollout;

/**
 * Persists flow stage decisions and rollout transitions in the same host transaction.
 *
 * @author Kimi Liu
 */
public interface FlowStore {

    /**
     * Rejects an unknown or unsupported flow policy independently of caller authentication.
     *
     * @param context operation coordinates
     * @param policy  selected flow policy
     */
    void validatePolicy(SettingProvider.Context context, String policy);

    /**
     * Reads the immutable history of stage decisions for a rollout.
     *
     * @param tenant    exact tenant identifier
     * @param rolloutId exact rollout identifier
     * @return stage decisions recorded for the rollout
     */
    List<Flow> query(String tenant, String rolloutId);

    /**
     * Atomically rechecks the actor, stage, flow policy, edition, and frozen checksum before recording the decision.
     * The policy determines whether the rollout stays in REVIEWING or moves to APPROVED or REJECTED. Repeating the same
     * request is idempotent.
     *
     * @param context        operation-scoped actor and request identity
     * @param rolloutId      exact rollout identifier
     * @param edition        expected rollout edition
     * @param frozenChecksum checksum of the reviewed frozen plan
     * @param flow           stage decision to record
     * @return rollout after recording the decision
     */
    Rollout decide(SettingProvider.Context context, String rolloutId, long edition, String frozenChecksum, Flow flow);

}

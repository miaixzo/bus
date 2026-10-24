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

import java.util.List;

import org.miaixz.bus.cortex.setting.SettingEnforcer;

/**
 * Atomic workflow persistence; host entity transactions enforce state and read-set invariants.
 *
 * @author Kimi Liu
 */
public interface RolloutStore {

    /**
     * Reads one tenant-qualified workflow.
     *
     * @param tenant  exact tenant identifier
     * @param rollout exact workflow identifier
     * @return requested workflow, or null if absent
     */
    Rollout find(String tenant, String rollout);

    /**
     * Queries bounded workflow records.
     *
     * @param tenant exact tenant identifier
     * @param offset zero-based page offset
     * @param limit  maximum number of workflows to return
     * @return page of tenant-qualified workflow records
     */
    List<Rollout> query(String tenant, int offset, int limit);

    /**
     * Creates or CAS-updates DRAFT only; submitted records are immutable.
     *
     * @param draft           editable workflow in the DRAFT state
     * @param expectedEdition expected current edition, or null for insert only
     * @return saved draft with its resulting edition
     */
    Rollout saveDraft(Rollout draft, Long expectedEdition);

    /**
     * Deletes only DRAFT with exact edition; submitted records cannot be deleted.
     *
     * @param tenant  exact tenant identifier
     * @param rollout exact draft workflow identifier
     * @param edition expected workflow concurrency counter
     */
    void deleteDraft(String tenant, String rollout, long edition);

    /**
     * Atomically rechecks complete Item/Reference/App memberships, editions, application states and active baselines;
     * freezes the plan and transitions DRAFT to REVIEWING.
     *
     * @param context  authenticated submitter and request identity
     * @param rollout  exact draft workflow identifier
     * @param edition  expected workflow concurrency counter
     * @param frozen   protected candidate plan to freeze for approval
     * @param expected complete source graph and active baseline expectations
     * @return workflow containing the frozen plan in the REVIEWING state
     */
    Rollout submit(
            SettingEnforcer.Context context,
            String rollout,
            long edition,
            Plan.Frozen frozen,
            Plan.ReadSet expected);

    /**
     * Atomically verifies approvals and identical intent, claims or reuses the exact publication receipt and enters
     * PUBLISHING. Same request returns the original intent/result; different input conflicts.
     *
     * @param context        authenticated publisher and idempotent request identity
     * @param rollout        exact approved workflow identifier
     * @param edition        expected workflow concurrency counter
     * @param frozenChecksum checksum of the approved frozen plan
     * @return workflow bound to the exact claimed or reused publication receipt
     */
    Rollout beginPublication(SettingEnforcer.Context context, String rollout, long edition, String frozenChecksum);

    /**
     * Records failure only for the matching unfinished receipt; cannot overwrite a successful publication.
     *
     * @param context   authenticated publisher and request identity
     * @param rollout   exact workflow identifier
     * @param receipt   exact unfinished publication receipt identifier
     * @param safeError failure description containing no sensitive values
     * @return workflow after recording the matching publication failure
     */
    Rollout failPublication(SettingEnforcer.Context context, String rollout, String receipt, String safeError);

}

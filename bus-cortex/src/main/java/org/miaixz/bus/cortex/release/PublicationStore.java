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
package org.miaixz.bus.cortex.release;

import org.miaixz.bus.cortex.setting.SettingEnforcer;

/**
 * Host atomic publication boundary; no partial or cache-only emulation is permitted.
 *
 * @author Kimi Liu
 */
public interface PublicationStore {

    /**
     * In one entity transaction rechecks authorization, approved frozen input, exact intent, workflow edition, app
     * eligibility and active baselines; allocates the unique server version; inserts all snapshots, self-contained
     * Release, active track pointer, workflow completion, receipt result and durable outbox. Failure rolls back all
     * visible writes; identical receipt returns the original Release. Must never reload mutable Item or Reference for
     * approved content.
     *
     * @param context         authenticated tenant, actor and idempotent request identity
     * @param rollout         exact approved workflow identifier
     * @param expectedEdition expected workflow concurrency counter
     * @param receipt         exact claimed publication receipt identifier
     * @param frozenChecksum  checksum of the approved frozen plan
     * @return original or newly committed formal release
     */
    Release commit(
            SettingEnforcer.Context context,
            String rollout,
            long expectedEdition,
            String receipt,
            String frozenChecksum);

}

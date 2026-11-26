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

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Records a flow stage decision against a frozen publication plan.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Flow extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558902800001L;

    /**
     * Identifier of the rollout associated with this decision.
     */
    private String rollout_id;

    /**
     * Principal assigned to the flow stage, independent of application relationships.
     */
    private String target_id;

    /**
     * Stage identifier in the flow policy.
     */
    private String stage;

    /**
     * Checksum of the frozen plan bound to this decision.
     */
    private String checksum;

    /**
     * Decision permitted by the applicable flow policy.
     */
    private String decision;

    /**
     * Actor's comment on the stage decision.
     */
    private String comment;

    /**
     * Creates an empty flow record.
     */
    public Flow() {
        // No initialization required.
    }

}

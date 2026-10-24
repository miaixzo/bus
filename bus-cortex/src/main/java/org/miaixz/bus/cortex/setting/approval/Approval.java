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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Immutable policy-stage decision bound to one frozen publication checksum.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Approval extends Tenant {

    /**
     * Identifier of the publication workflow under review.
     */
    private String rollout_id;

    /**
     * Principal receiving the approval assignment, independent of application relationships.
     */
    private String target_id;

    /**
     * Approval policy stage.
     */
    private String stage;

    /**
     * Checksum of the frozen plan bound to this decision.
     */
    private String checksum;

    /**
     * Stage decision permitted by the applicable approval policy.
     */
    private String decision;

    /**
     * Reviewer comment for the approval decision.
     */
    private String comment;

    /**
     * Creates an empty approval decision.
     */
    public Approval() {
        // No initialization required.
    }

}

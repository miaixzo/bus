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

import java.io.Serial;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Publication workflow selection or frozen approval material, concurrency state and exact execution receipt. The
 * inherited status property stores the explicit code returned by {@link State#key()}.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Rollout extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558926680001L;

    /**
     * Display title of the publication workflow.
     */
    private String title;

    /**
     * Checksum of the candidate material, immutable after submission.
     */
    private String checksum;

    /**
     * Selection parameters in {@link State#DRAFT}; the protected frozen plan after submission.
     */
    @Lob
    private String manifest;

    /**
     * Optimistic concurrency counter for the workflow.
     */
    private Long edition;

    /**
     * Execution error message containing no sensitive values.
     */
    private String error;

    /**
     * Workflow completion time in epoch milliseconds.
     */
    private Long finished;

    /**
     * Identifier of the exact publication event receipt, independent of the formal release version.
     */
    private String publication;

    /**
     * Description of the publication workflow.
     */
    private String description;

    /**
     * Creates an empty publication workflow.
     */
    public Rollout() {
        // No initialization required.
    }

    /**
     * Approval and publication workflow states.
     *
     * @author Kimi Liu
     */
    public enum State {

        /**
         * Publication draft that may be edited or deleted.
         */
        DRAFT,
        /**
         * Frozen candidate awaiting approval.
         */
        REVIEWING,
        /**
         * Approved candidate awaiting publication.
         */
        APPROVED,
        /**
         * Rejected candidate; further changes require a new draft.
         */
        REJECTED,
        /**
         * Publication in progress, bound to an exact publication receipt.
         */
        PUBLISHING,
        /**
         * Failed publication; retries must use the original approved material.
         */
        FAILED,
        /**
         * Formal release committed successfully in an atomic transaction.
         */
        PUBLISHED;

        /**
         * Returns the stable workflow code stored in the inherited status property.
         *
         * @return explicit workflow status code
         */
        public int key() {
            return switch (this) {
                case DRAFT -> 0;
                case REVIEWING -> 1;
                case APPROVED -> 2;
                case REJECTED -> 3;
                case PUBLISHING -> 4;
                case FAILED -> 5;
                case PUBLISHED -> 6;
            };
        }

        /**
         * Checks whether the state graph permits a transition. Services must still verify request identity for
         * same-state retries.
         *
         * @param target requested workflow state
         * @return whether the transition is permitted by the state graph
         */
        public boolean canTransitionTo(State target) {
            if (target == this) {
                return true;
            }
            return switch (this) {
                case DRAFT -> target == REVIEWING;
                case REVIEWING -> target == APPROVED || target == REJECTED;
                case APPROVED, FAILED -> target == PUBLISHING;
                case PUBLISHING -> target == FAILED || target == PUBLISHED;
                case REJECTED, PUBLISHED -> false;
            };
        }

    }

}

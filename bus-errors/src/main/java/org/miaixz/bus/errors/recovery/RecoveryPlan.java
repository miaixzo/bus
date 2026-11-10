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
package org.miaixz.bus.errors.recovery;

import java.time.Duration;
import java.util.Objects;

/**
 * Authorization inputs for one recovery action performed by its existing owner.
 *
 * @param owner    stable identifier of the component responsible for execution
 * @param attempts maximum additional attempts allowed
 * @param budget   remaining operation budget at the time this plan is created
 *
 * @author Kimi Liu
 */
public record RecoveryPlan(String owner, int attempts, Duration budget) {

    /**
     * Validates the execution owner and the budget.
     *
     * @param owner    stable identifier of the component responsible for execution
     * @param attempts maximum additional attempts allowed
     * @param budget   remaining operation budget at the time this plan is created
     */
    public RecoveryPlan {
        if (Objects.requireNonNull(owner, "owner").isBlank() || attempts < 0
                || Objects.requireNonNull(budget, "budget").isNegative()) {
            throw new IllegalArgumentException("Invalid recovery plan");
        }
    }

    /**
     * What the execution owner knows about the request's delivery.
     *
     * @author Kimi Liu
     */
    public enum Delivery {
        /**
         * Request was not sent or the peer confirmed it was unprocessed.
         */
        UNPROCESSED,
        /**
         * Request may have been processed.
         */
        UNCERTAIN,
        /**
         * Replaying this delivery is explicitly supported by the existing operation contract.
         */
        REPLAY_SAFE
    }

}

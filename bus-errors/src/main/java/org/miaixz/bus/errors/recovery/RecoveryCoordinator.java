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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enforces one owner and one retry budget without installing another retry loop.
 *
 * @author Kimi Liu
 */
public final class RecoveryCoordinator {

    /**
     * Owner, retry allowance and elapsed-time budget for this operation.
     */
    private final RecoveryPlan plan;
    /**
     * Monotonic start time used to compute elapsed recovery time.
     */
    private final long started = System.nanoTime();
    /**
     * Additional attempts still available to the designated owner.
     */
    private final AtomicInteger remaining;

    /**
     * Creates an operation-local coordinator.
     *
     * @param plan owner and budgets for this operation
     */
    public RecoveryCoordinator(RecoveryPlan plan) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.remaining = new AtomicInteger(plan.attempts());
    }

    /**
     * Checks replay preconditions while leaving scheduling and retry accounting to the original execution owner.
     *
     * @param idempotent operation can be repeated under its contract
     * @param repeatable input can be replayed
     * @param delivery   verified delivery semantics
     * @return whether the inputs permit replay
     */
    public static boolean canReplay(boolean idempotent, boolean repeatable, RecoveryPlan.Delivery delivery) {
        return idempotent && repeatable
                && (delivery == RecoveryPlan.Delivery.UNPROCESSED || delivery == RecoveryPlan.Delivery.REPLAY_SAFE);
    }

    /**
     * Claims one approved attempt. The owner must still observe cancellation at its scheduling boundary.
     *
     * @param owner      component claiming this attempt
     * @param idempotent operation can be retried under its existing contract
     * @param repeatable request input can be replayed
     * @param delivery   delivery facts known to the owner
     * @param cancelled  whether the operation was cancelled
     * @return whether another attempt is authorized
     */
    public boolean tryRetry(
            String owner,
            boolean idempotent,
            boolean repeatable,
            RecoveryPlan.Delivery delivery,
            boolean cancelled) {
        if (!this.plan.owner().equals(owner) || cancelled || Thread.currentThread().isInterrupted()
                || !canReplay(idempotent, repeatable, delivery)
                || Duration.ofNanos(System.nanoTime() - this.started).compareTo(this.plan.budget()) >= 0) {
            return false;
        }
        return this.remaining.getAndUpdate(value -> Math.max(0, value - 1)) > 0;
    }

}

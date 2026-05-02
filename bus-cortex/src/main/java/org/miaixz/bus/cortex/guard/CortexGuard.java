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
package org.miaixz.bus.cortex.guard;

import java.util.List;

/**
 * Orchestrates guard strategies for Cortex mutation paths.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CortexGuard {

    /**
     * Ordered guard strategies participating in mutation policy checks.
     */
    private final List<GuardStrategy> strategies;

    /**
     * Creates a Cortex guard from the supplied strategies.
     *
     * @param strategies guard strategies
     */
    public CortexGuard(List<GuardStrategy> strategies) {
        this.strategies = strategies == null ? List.of() : List.copyOf(strategies);
    }

    /**
     * Evaluates the first denying guard strategy for the supplied context.
     *
     * @param context guard evaluation context
     * @return guard decision
     */
    public GuardDecision check(GuardContext context) {
        for (GuardStrategy strategy : strategies) {
            if (strategy != null && strategy.supports(context)) {
                GuardDecision decision = strategy.evaluate(context);
                if (decision != null && !decision.isAllowed()) {
                    return decision;
                }
            }
        }
        return GuardDecision.allow();
    }

    /**
     * Enforces guard policy and raises a security exception on denial.
     *
     * @param context guard evaluation context
     */
    public void enforce(GuardContext context) {
        GuardDecision decision = check(context);
        if (!decision.isAllowed()) {
            throw new SecurityException(decision.getCode() + ": " + decision.getMessage());
        }
    }

}

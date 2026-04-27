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
package org.miaixz.bus.cortex.setting;

import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.guard.GuardDecision;
import org.miaixz.bus.cortex.guard.GuardStrategy;

/**
 * Adapts the setting relation enforcer into the shared Cortex guard chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SettingEnforcerGuardStrategy implements GuardStrategy {

    /**
     * Setting relation enforcer used to validate namespace, application, and profile scope.
     */
    private final SettingEnforcer enforcer;

    /**
     * Creates a guard strategy from the setting relation enforcer.
     *
     * @param enforcer setting relation enforcer
     */
    public SettingEnforcerGuardStrategy(SettingEnforcer enforcer) {
        this.enforcer = enforcer;
    }

    /**
     * Returns whether this strategy supports the supplied guard context.
     *
     * @param context guard context
     * @return {@code true} for setting-domain contexts
     */
    @Override
    public boolean supports(GuardContext context) {
        return enforcer != null && context != null && "setting".equalsIgnoreCase(context.getDomain());
    }

    /**
     * Evaluates setting relation policy for the supplied context.
     *
     * @param context guard context
     * @return guard decision
     */
    @Override
    public GuardDecision evaluate(GuardContext context) {
        return enforcer.allows(context.getNamespace_id(), context.getApp_id(), context.getProfile_id())
                ? GuardDecision.allow()
                : GuardDecision.deny("SETTING_SCOPE_DENIED", "Setting scope is not allowed");
    }

}

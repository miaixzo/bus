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

import java.util.Objects;

import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.guard.GuardDecision;
import org.miaixz.bus.cortex.guard.GuardStrategy;

/**
 * Bridges the shared guard to mandatory authenticated setting policy only.
 *
 * @author Kimi Liu
 */
public final class SettingEnforcerStrategy implements GuardStrategy {

    /**
     * Mandatory setting policy.
     */
    private final SettingEnforcer enforcer;

    /**
     * Creates the authorization bridge.
     *
     * @param enforcer trusted host policy
     */
    public SettingEnforcerStrategy(SettingEnforcer enforcer) {
        this.enforcer = Objects.requireNonNull(enforcer);
    }

    /**
     * Selects setting resources only.
     *
     * @param context guard input
     * @return whether supported
     */
    @Override
    public boolean supports(GuardContext context) {
        return context != null && "setting".equalsIgnoreCase(context.getDomain());
    }

    /**
     * Evaluates exact trusted tenant/resource permissions.
     *
     * @param context guard input
     * @return permission decision
     */
    @Override
    public GuardDecision evaluate(GuardContext context) {
        SettingEnforcer.Context trusted = enforcer.current();
        if (!supports(context) || !trusted.tenant().equals(context.getTenant_id()))
            return GuardDecision.deny("SETTING_SCOPE_DENIED", "Setting tenant mismatch");
        try {
            enforcer.require(trusted, context.getAction(), context.getResourceType(), context.getResourceId());
            return GuardDecision.allow();
        } catch (SecurityException | ForbiddenException denied) {
            return GuardDecision.deny("SETTING_SCOPE_DENIED", "Setting permission denied");
        }
    }

}

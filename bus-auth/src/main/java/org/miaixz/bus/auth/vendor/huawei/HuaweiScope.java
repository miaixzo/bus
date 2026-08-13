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
package org.miaixz.bus.auth.vendor.huawei;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * Identity scopes retained by the Huawei authentication client contract.
 *
 * @author Kimi Liu
 */
public enum HuaweiScope implements AuthorizeScope {

    /**
     * Associates the authorization with the user's Huawei identity.
     */
    OPENID("openid", "Basic scope, required for V3", true),

    /**
     * Grants access to the user's basic Huawei account profile.
     */
    BASE_PROFILE("https://www.huawei.com/auth/account/base.profile", "Retrieves the user's basic information", true);

    /**
     * Huawei wire scope.
     */
    private final String scope;

    /**
     * Human-readable permission description.
     */
    private final String description;

    /**
     * Default-selection flag.
     */
    private final boolean isDefault;

    /**
     * Creates immutable Huawei scope metadata.
     *
     * @param scope       non-null wire scope
     * @param description non-null permission description
     * @param isDefault   whether the scope is selected by default
     */
    HuaweiScope(final String scope, final String description, final boolean isDefault) {
        this.scope = scope;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Returns the immutable Huawei wire scope.
     *
     * @return non-null scope text
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Returns the immutable human-readable permission description.
     *
     * @return non-null permission description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether this scope is selected when no explicit registration scopes exist.
     *
     * @return configured default-selection flag
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

}

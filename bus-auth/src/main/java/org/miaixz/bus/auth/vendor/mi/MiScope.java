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
package org.miaixz.bus.auth.vendor.mi;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * Immutable Xiaomi authorization-scope metadata.
 *
 * @author Kimi Liu
 */
public enum MiScope implements AuthorizeScope {

    /**
     * Grants access to the user's basic profile.
     */
    profile("user/profile", "Retrieves the user's basic information", true),

    /**
     * Grants access to the user's OpenID.
     */
    OPENID("user/openIdV2", "Retrieves the user's OpenID", true),

    /**
     * Grants access to the user's phone number and email address.
     */
    PHONE_EMAIL("user/phoneAndEmail", "Retrieves the user's phone number and email", true);

    /**
     * Xiaomi wire scope.
     */
    private final String scope;

    /**
     * Human-readable scope description.
     */
    private final String description;

    /**
     * Whether the scope is selected by default.
     */
    private final boolean defaultScope;

    /**
     * Creates immutable Xiaomi scope metadata.
     *
     * @param scope        non-null Xiaomi wire scope
     * @param description  non-null human-readable description
     * @param defaultScope whether the scope is selected by default
     */
    MiScope(final String scope, final String description, final boolean defaultScope) {
        this.scope = scope;
        this.description = description;
        this.defaultScope = defaultScope;
    }

    /**
     * Returns the Xiaomi wire scope.
     *
     * @return non-null immutable scope text
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Returns the human-readable scope description.
     *
     * @return non-null immutable description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether this scope is selected by default.
     *
     * @return true for every frozen Xiaomi scope
     */
    @Override
    public boolean isDefault() {
        return defaultScope;
    }

}

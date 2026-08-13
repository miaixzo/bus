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
package org.miaixz.bus.auth.vendor.line;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * LINE authorization scopes.
 *
 * @author Kimi Liu
 */
public enum LineScope implements AuthorizeScope {

    /**
     * Get profile details. The meaning of {@code scope} is subject to {@code description}.
     */
    PROFILE("profile", "Get profile details", true),
    /**
     * Get ID token.
     */
    OPENID("openid", "Get id token", true),
    /**
     * Get email (separate authorization required).
     */
    EMAIL("email", "Get email (separate authorization required)", false);

    /**
     * LINE wire scope.
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
     * Constructs an authorization scope.
     *
     * @param scope       the scope value
     * @param description the scope description
     * @param isDefault   whether the scope is enabled by default
     */
    LineScope(final String scope, final String description, final boolean isDefault) {
        this.scope = scope;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Returns the immutable LINE wire scope.
     *
     * @return non-null scope text
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Returns the immutable permission description.
     *
     * @return non-null permission description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether this scope is selected when registration scopes are absent.
     *
     * @return configured default-selection flag
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

}

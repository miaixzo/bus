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
package org.miaixz.bus.auth.vendor.google;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * Google identity authorization scopes retained by the authentication client contract.
 *
 * @author Kimi Liu
 */
public enum GoogleScope implements AuthorizeScope {

    /**
     * Associates the authorization with the user's Google identity.
     */
    USER_OPENID("openid", "Associate you with your personal info on GoogleScope", true),

    /**
     * Grants access to the user's primary email address.
     */
    USER_EMAIL("email", "View your email address", true),

    /**
     * Grants access to the user's basic profile information.
     */
    USER_PROFILE("profile", "View your basic profile info", true),

    /**
     * Grants access to the user's phone numbers.
     */
    USER_PHONENUMBERS_READ("https://www.googleapis.com/auth/user.phonenumbers.read", "View your phone numbers", false);

    /**
     * Google wire scope.
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
     * Creates immutable Google scope metadata.
     *
     * @param scope       non-null wire scope
     * @param description non-null permission description
     * @param isDefault   whether the scope is selected by default
     */
    GoogleScope(final String scope, final String description, final boolean isDefault) {
        this.scope = scope;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Returns the immutable Google wire scope.
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

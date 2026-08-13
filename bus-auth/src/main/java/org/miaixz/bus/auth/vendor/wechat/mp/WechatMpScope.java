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
package org.miaixz.bus.auth.vendor.wechat.mp;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * WeChat Official Account authorization scopes.
 *
 * @author Kimi Liu
 */
public enum WeChatMpScope implements AuthorizeScope {

    /**
     * Grants access to the authorized user's public profile.
     */
    SNSAPI_USERINFO("snsapi_userinfo",
            "Pop up the authorization page, through which the nickname, gender, and location of the user can be obtained. Even if the user is not followed, their information can be obtained as long as they authorize",
            true),

    /**
     * Grants silent access to the user's OpenID only.
     */
    SNSAPI_BASE("snsapi_base",
            "Do not pop up the authorization page, directly jump, only the user's openid can be obtained", false);

    /**
     * Immutable WeChat wire scope.
     */
    private final String scope;

    /**
     * Immutable human-readable scope description.
     */
    private final String description;

    /**
     * Default-selection marker.
     */
    private final boolean isDefault;

    /**
     * Creates an immutable scope descriptor.
     *
     * @param scope       non-null WeChat wire scope
     * @param description non-null human-readable description
     * @param isDefault   whether the scope is selected by default
     */
    WeChatMpScope(final String scope, final String description, final boolean isDefault) {
        this.scope = scope;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Returns the immutable WeChat wire scope.
     *
     * @return non-null scope value
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Returns the immutable human-readable description.
     *
     * @return non-null scope description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether this scope is selected when registration scopes are empty.
     *
     * @return {@code true} when selected by default
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

}

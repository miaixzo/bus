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
package org.miaixz.bus.auth.vendor.wechat.ee;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * WeChat Enterprise authorization scopes.
 *
 * @author Kimi Liu
 */
public enum WeChatEeWebScope implements AuthorizeScope {

    /**
     * Application authorization scope. For self-built enterprise applications, it is fixed to: snsapi_base. The meaning
     * of {@code scope} is subject to {@code description}.
     */
    SNSAPI_BASE("snsapi_base",
            "Application authorization scope. For self-built enterprise applications, it is fixed to: snsapi_base",
            true);

    /**
     * The scope string as defined by WeChat Enterprise.
     */
    private final String scope;

    /**
     * A description of what the scope grants access to.
     */
    private final String description;

    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

    /**
     * Constructs an authorization scope.
     *
     * @param scope       the scope value
     * @param description the scope description
     * @param isDefault   whether the scope is enabled by default
     */
    WeChatEeWebScope(final String scope, final String description, final boolean isDefault) {
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
     * Returns the immutable human-readable scope description.
     *
     * @return non-null scope description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether this scope is selected when no registration scopes are supplied.
     *
     * @return {@code true} when selected by default
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

}

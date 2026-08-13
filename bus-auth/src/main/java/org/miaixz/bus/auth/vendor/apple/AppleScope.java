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
package org.miaixz.bus.auth.vendor.apple;

import org.miaixz.bus.auth.vendor.AuthorizeScope;

/**
 * Apple authorization scopes.
 *
 * @author Kimi Liu
 * @see <a href="https://developer.apple.com/documentation/sign_in_with_apple/clientconfigi/3230955-scope/">Apple
 *      Sign-in Scope Documentation</a>
 */
public enum AppleScope implements AuthorizeScope {

    /**
     * User's email address.
     */
    EMAIL("email", "User's email address", true),
    /**
     * User's name.
     */
    NAME("name", "User's name", true);

    /**
     * The scope string as defined by Apple.
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
    AppleScope(final String scope, final String description, final boolean isDefault) {
        this.scope = scope;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Returns the exact Apple authorization scope value.
     *
     * @return non-null immutable scope text
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Returns the human-readable scope purpose.
     *
     * @return non-null immutable description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether the scope is requested when no explicit registration scopes exist.
     *
     * @return the immutable default-selection flag
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.gitee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Gitee authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum GiteeScope implements AuthorizeScope {

    /**
     * Access to user's personal information, recent activities, etc. The meaning of {@code scope} is subject to
     * {@code description}.
     */
    USER_INFO("user_info", "Access to user's personal information, recent activities, etc.", true),
    /**
     * View, create, and update user's projects.
     */
    PROJECTS("projects", "View, create, and update user's projects", false),
    /**
     * View, publish, and update user's Pull Requests.
     */
    PULL_REQUESTS("pull_requests", "View, publish, and update user's Pull Request", false),
    /**
     * View, publish, and update user's Issues.
     */
    ISSUES("issues", "View, publish, and update user's Issue", false),
    /**
     * View, publish, and manage user's comments in projects and code snippets.
     */
    NOTES("notes", "View, publish, and manage user's comments in projects, code snippets", false),
    /**
     * View, deploy, and delete user's public keys.
     */
    KEYS("keys", "View, deploy, and delete user's public keys", false),
    /**
     * View, deploy, and update user's Webhooks.
     */
    HOOK("hook", "View, deploy, and update user's Webhook", false),
    /**
     * View and manage user's organizations and members.
     */
    GROUPS("groups", "View and manage user's organizations and members", false),
    /**
     * View, delete, and update user's code snippets.
     */
    GISTS("gists", "View, delete, and update user's code snippets", false),
    /**
     * View and manage user's enterprises and members.
     */
    ENTERPRISES("enterprises", "View and manage user's enterprises and members", false),
    /**
     * View user's personal email information.
     */
    EMAILS("emails", "View user's personal email information", false);

    /**
     * The scope string as defined by Gitee.
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

}

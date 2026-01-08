/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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

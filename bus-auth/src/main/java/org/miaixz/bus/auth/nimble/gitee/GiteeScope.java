/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * Gitee 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum GiteeScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    USER_INFO("user_info", "访问用户的个人信息、最新动态等", true), PROJECTS("projects", "查看、创建、更新用户的项目", false),
    PULL_REQUESTS("pull_requests", "查看、发布、更新用户的 Pull Request", false), ISSUES("issues", "查看、发布、更新用户的 Issue", false),
    NOTES("notes", "查看、发布、管理用户在项目、代码片段中的评论", false), KEYS("keys", "查看、部署、删除用户的公钥", false),
    HOOK("hook", "查看、部署、更新用户的 Webhook", false), GROUPS("groups", "查看、管理用户的组织以及成员", false),
    GISTS("gists", "查看、删除、更新用户的代码片段", false), ENTERPRISES("enterprises", "查看、管理用户的企业以及成员", false),
    EMAILS("emails", "查看用户的个人邮箱信息", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

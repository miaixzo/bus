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
package org.miaixz.bus.auth.nimble.gitlab;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Gitlab 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum GitlabScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    READ_USER("read_user",
            "Grants read-only access to the authenticated user's profile through the /user API endpoint, which includes username, public email, and full name. Also grants access to read-only API endpoints under /users.",
            true),
    OPENID("openid",
            "Grants permission to authenticate with GitLab using OpenID Connect. Also gives read-only access to the user's profile and group memberships.",
            true),
    PROFILE("profile", "Grants read-only access to the user's profile data using OpenID Connect.", true),
    EMAIL("email", "Grants read-only access to the user's primary email address using OpenID Connect.", true),
    READ_API("read_api",
            "Grants read access to the API, including all groups and projects, the container registry, and the package registry.",
            false),
    READ_REPOSITORY("read_repository",
            "Grants read-only access to repositories on private projects using Git-over-HTTP or the Repository Files API.",
            false),
    WRITE_REPOSITORY("write_repository",
            "Grants read-write access to repositories on private projects using Git-over-HTTP (not using the API).",
            false),
    READ_REGISTRY("read_registry", "Grants read-only access to container registry images on private projects.", false),
    WRITE_REGISTRY("write_registry",
            "<span title=\"translation missing: en.doorkeeper.scope_desc.write_registry\">Write Registry</span>",
            false),
    SUDO("sudo",
            "Grants permission to perform API actions as any user in the system, when authenticated as an admin user.",
            false),
    API("api",
            "Grants complete read/write access to the API, including all groups and projects, the container registry, and the package registry.",
            false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

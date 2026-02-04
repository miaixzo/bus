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
package org.miaixz.bus.auth.nimble.gitlab;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * GitLab authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum GitlabScope implements AuthorizeScope {

    /**
     * Grants read-only access to the authenticated user's profile through the /user API endpoint, which includes
     * username, public email, and full name. Also grants access to read-only API endpoints under /users. The meaning of
     * {@code scope} is subject to {@code description}.
     */
    READ_USER("read_user",
            "Grants read-only access to the authenticated user's profile through the /user API endpoint, which includes username, public email, and full name. Also grants access to read-only API endpoints under /users.",
            true),
    /**
     * Grants permission to authenticate with GitLab using OpenID Connect. Also gives read-only access to the user's
     * profile and group memberships.
     */
    OPENID("openid",
            "Grants permission to authenticate with GitLab using OpenID Connect. Also gives read-only access to the user's profile and group memberships.",
            true),
    /**
     * Grants read-only access to the user's profile data using OpenID Connect.
     */
    PROFILE("profile", "Grants read-only access to the user's profile data using OpenID Connect.", true),
    /**
     * Grants read-only access to the user's primary email address using OpenID Connect.
     */
    EMAIL("email", "Grants read-only access to the user's primary email address using OpenID Connect.", true),
    /**
     * Grants read access to the API, including all groups and projects, the container registry, and the package
     * registry.
     */
    READ_API("read_api",
            "Grants read access to the API, including all groups and projects, the container registry, and the package registry.",
            false),
    /**
     * Grants read-only access to repositories on private projects using Git-over-HTTP or the Repository Files API.
     */
    READ_REPOSITORY("read_repository",
            "Grants read-only access to repositories on private projects using Git-over-HTTP or the Repository Files API.",
            false),
    /**
     * Grants read-write access to repositories on private projects using Git-over-HTTP (not using the API).
     */
    WRITE_REPOSITORY("write_repository",
            "Grants read-write access to repositories on private projects using Git-over-HTTP (not using the API).",
            false),
    /**
     * Grants read-only access to container registry images on private projects.
     */
    READ_REGISTRY("read_registry", "Grants read-only access to container registry images on private projects.", false),
    /**
     * Grants read-write access to container registry images on private projects.
     */
    WRITE_REGISTRY("write_registry",
            "<span title=\"translation missing: en.doorkeeper.scope_desc.write_registry\">Write Registry</span>",
            false),
    /**
     * Grants permission to perform API actions as any user in the system, when authenticated as an admin user.
     */
    SUDO("sudo",
            "Grants permission to perform API actions as any user in the system, when authenticated as an admin user.",
            false),
    /**
     * Grants complete read/write access to the API, including all groups and projects, the container registry, and the
     * package registry.
     */
    API("api",
            "Grants complete read/write access to the API, including all groups and projects, the container registry, and the package registry.",
            false);

    /**
     * The scope string as defined by GitLab.
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

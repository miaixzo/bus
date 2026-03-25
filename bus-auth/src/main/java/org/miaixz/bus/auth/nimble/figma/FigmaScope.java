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
package org.miaixz.bus.auth.nimble.figma;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Figma authorization scopes.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum FigmaScope implements AuthorizeScope {

    /**
     * Read files, projects, users, versions, comments &amp; styles, and webhooks.
     */
    FILE_CONTENT("files:read", "Read files, projects, users, versions, comments &amp; styles, and webhooks", true),
    /**
     * Read and write to variables in Figma file. Note: this is only available to members in Enterprise organizations.
     */
    VARIABLES("file_variables:read,file_variables:write",
            "Read and write to variables in Figma file. Note: this is only available to members in Enterprise organizations",
            false),
    /**
     * Post and delete comments and comment reactions in files.
     */
    COMMENTS("file_comments:write", "Post and delete comments and comment reactions in files", false),
    /**
     * Read and write to dev resources in files.
     */
    DEV_RESOURCES("file_dev_resources:read,file_dev_resources:write", "Read and write to dev resources in files",
            false),
    /**
     * Read your design system analytics.
     */
    LIBRARY_ANALYTICS("library_analytics:read", "Read your design system analytics", false),
    /**
     * Create and manage webhooks.
     */
    WEBHOOKS("webhooks:write", "Create and manage webhooks", false);

    /**
     * The scope string as defined by Figma.
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

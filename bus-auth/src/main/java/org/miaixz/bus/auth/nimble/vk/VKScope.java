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
package org.miaixz.bus.auth.nimble.vk;

import org.miaixz.bus.auth.nimble.AuthorizeScope;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * VK authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum VKScope implements AuthorizeScope {

    /**
     * Personal information scope. Includes last name, first name, gender, profile photo, and date of birth. This is the
     * basic permission used by default for all apps. The meaning of {@code scope} is subject to {@code description}.
     */
    PERSONAL("vkid.personal_info",
            "Last name, first name, gender, profile photo and date of birth. The basic permission used by default for all apps",
            true),
    /**
     * Access to the user's email address.
     */
    EMAIL("email", "Access to the user's email", true),
    /**
     * Access to the user's phone number.
     */
    PHONE("phone", "Access to the user's phone number", false),
    /**
     * Access to friends.
     */
    FRIENDS("friends", "Access to friends", false),
    /**
     * Access to standard and advanced wall methods.
     */
    WALL("wall", "Access to standard and advanced wall methods", false),
    /**
     * Access to the user's groups.
     */
    GROUPS("groups", "Access to the user's groups", false),
    /**
     * Access to stories.
     */
    STORIES("stories", "Access to stories", false),
    /**
     * Access to documents.
     */
    DOCS("docs", "Access to documents", false),
    /**
     * Access to photos.
     */
    PHOTOS("photos", "Access to photos", false),
    /**
     * Access to advanced methods of the advertising API.
     */
    ADS("ads", "Access to advanced methods of the advertising API", false),
    /**
     * Access to videos.
     */
    VIDEO("video", "Access to videos", false),
    /**
     * Access to the user's status.
     */
    STATUS("status", "Access to the user's status", false),
    /**
     * Access to products in the market.
     */
    MARKET("market", "Access to products", false),
    /**
     * Access to wiki pages.
     */
    PAGES("pages", "Access to wiki pages", false),
    /**
     * Access to notifications about responses to the user.
     */
    NOTIFICATIONS("notifications", "Access to notifications about responses to the user", false),
    /**
     * Access to statistics of the user's groups and apps for which they are an administrator.
     */
    STATS("stats", "Access to statistics of the user's groups and apps for which they are an administrator", false),
    /**
     * Access to notes.
     */
    NOTES("notes", "Access to notes", false);

    /**
     * The scope string as defined by VK.
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

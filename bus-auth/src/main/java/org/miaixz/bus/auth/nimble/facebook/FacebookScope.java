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
package org.miaixz.bus.auth.nimble.facebook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Facebook authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum FacebookScope implements AuthorizeScope {

    /**
     * Allows the app to read the user's default public profile. The meaning of {@code scope} is subject to
     * {@code description}.
     */
    PUBLIC_PROFILE("public_profile", "Permission allows the app to read the user's default public profile", true),
    /**
     * Get user's email.
     */
    EMAIL("email", "Get user's email", false),
    /**
     * Allows the application to access the user's age range.
     */
    USER_AGE_RANGE("user_age_range", "Allows the application to access the user's age range", false),
    /**
     * Get user's birthday.
     */
    USER_BIRTHDAY("user_birthday", "Get user's birthday", false),
    /**
     * Get user's friend list.
     */
    USER_FRIENDS("user_friends", "Get user's friend list", false),
    /**
     * Get user's gender.
     */
    USER_GENDER("user_gender", "Get user's gender", false),
    /**
     * Get user's hometown information.
     */
    USER_HOMETOWN("user_hometown", "Get user's hometown information", false),
    /**
     * Get user's likes list.
     */
    USER_LIKES("user_likes", "Get user's likes list", false),
    /**
     * Get user's personal link.
     */
    USER_LINK("user_link", "Get user's personal link", true),
    /**
     * Get user's location information.
     */
    USER_LOCATION("user_location", "Get user's location information", false),
    /**
     * Get user's photo album information.
     */
    USER_PHOTOS("user_photos", "Get user's photo album information", false),
    /**
     * Get user's published content.
     */
    USER_POSTS("user_posts", "Get user's published content", false),
    /**
     * Get user's uploaded video information.
     */
    USER_VIDEOS("user_videos", "Get user's uploaded video information", false),
    /**
     * Get public group member information.
     */
    GROUPS_ACCESS_MEMBER_INFO("groups_access_member_info", "Get public group member information", false),
    /**
     * Authorizes your application to publish content to groups on someone's behalf, provided they have already granted
     * your application access.
     */
    PUBLISH_TO_GROUPS("publish_to_groups",
            "Authorizes your application to publish content to groups on someone's behalf, provided they have already granted your application access",
            false);

    /**
     * The scope string as defined by Facebook.
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

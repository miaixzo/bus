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

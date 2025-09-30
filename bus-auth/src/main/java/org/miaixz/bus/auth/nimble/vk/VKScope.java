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
package org.miaixz.bus.auth.nimble.vk;

import org.miaixz.bus.auth.nimble.AuthorizeScope;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * VK 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum VKScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    PERSONAL("vkid.personal_info",
            "Last name, first name, gender, profile photo and date of birth. The basic permission used by default for all apps",
            true),
    EMAIL("email", "Access to the user's email", true), PHONE("phone", "Access to the user's phone number", false),
    FRIENDS("friends", "Access to friends", false), WALL("wall", "Access to standard and advanced wall methods", false),
    GROUPS("groups", "Access to the user's groups", false), STORIES("stories", "Access to stories", false),
    DOCS("docs", "Access to documents", false), PHOTOS("photos", "Access to photos", false),
    ADS("ads", "Access to advanced methods of the advertising API", false), VIDEO("video", "Access to videos", false),
    STATUS("status", "Access to the user's status", false), MARKET("market", "Access to products", false),
    PAGES("pages", "Access to wiki pages", false),
    NOTIFICATIONS("notifications", "Access to notifications about responses to the user", false),
    STATS("stats", "Access to statistics of the user's groups and apps for which they are an administrator", false),
    NOTES("notes", "Access to notes", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

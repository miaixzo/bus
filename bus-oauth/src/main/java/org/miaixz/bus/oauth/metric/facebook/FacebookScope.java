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
package org.miaixz.bus.oauth.metric.facebook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.oauth.metric.AuthorizeScope;

/**
 * Facebook 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum FacebookScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    PUBLIC_PROFILE("public_profile", "权限允许应用读取用户默认的公开资料", true), EMAIL("email", "获取用户的邮箱", false),
    USER_AGE_RANGE("user_age_range", "允许应用程序访问用户的年龄范围", false), USER_BIRTHDAY("user_birthday", "获取用户的生日", false),
    USER_FRIENDS("user_friends", "获取用户的好友列表", false), USER_GENDER("user_gender", "获取用户的性别", false),
    USER_HOMETOWN("user_hometown", "获取用户的家乡信息", false), USER_LIKES("user_likes", "获取用户的喜欢列表", false),
    USER_LINK("user_link", "获取用户的个人链接", true), USER_LOCATION("user_location", "获取用户的位置信息", false),
    USER_PHOTOS("user_photos", "获取用户的相册信息", false), USER_POSTS("user_posts", "获取用户发布的内容", false),
    USER_VIDEOS("user_videos", "获取用户上传的视频信息", false),
    GROUPS_ACCESS_MEMBER_INFO("groups_access_member_info", "获取公开的群组成员信息", false),
    PUBLISH_TO_GROUPS("publish_to_groups", "授权您的应用程序代表某人将内容发布到组中，前提是他们已经授予您的应用程序访问权限", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

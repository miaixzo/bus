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
package org.miaixz.bus.auth.nimble.renren;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Renren authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum RenrenScope implements AuthorizeScope {

    /**
     * Requires user permission to access user logs. The meaning of {@code scope} is subject to {@code description}.
     */
    READ_USER_BLOG("read_user_blog", "Requires user permission to access user logs.", false),
    /**
     * Requires user permission to access user check-in information.
     */
    READ_USER_CHECKIN("read_user_checkin", "Requires user permission to access user check-in information.", false),
    /**
     * Requires user permission to access user feeds.
     */
    READ_USER_FEED("read_user_feed", "Requires user permission to access user feeds.", false),
    /**
     * Requires user permission to access user guestbook.
     */
    READ_USER_GUESTBOOK("read_user_guestbook", "Requires user permission to access user guestbook.", false),
    /**
     * Requires user permission to access user invitation status.
     */
    READ_USER_INVITATION("read_user_invitation", "Requires user permission to access user invitation status.", false),
    /**
     * Requires user permission to access user's like history.
     */
    READ_USER_LIKE_HISTORY("read_user_like_history", "Requires user permission to access user's like history.", false),
    /**
     * Requires user permission to access user's internal messages.
     */
    READ_USER_MESSAGE("read_user_message", "Requires user permission to access user's internal messages.", false),
    /**
     * Requires user permission to access user's received notifications.
     */
    READ_USER_NOTIFICATION("read_user_notification",
            "Requires user permission to access user's received notifications.", false),
    /**
     * Requires user permission to access user's photo album related information.
     */
    READ_USER_PHOTO("read_user_photo", "Requires user permission to access user's photo album related information.",
            false),
    /**
     * Requires user permission to access user's status related information.
     */
    READ_USER_STATUS("read_user_status", "Requires user permission to access user's status related information.",
            false),
    /**
     * Requires user permission to access user's album related information.
     */
    READ_USER_ALBUM("read_user_album", "Requires user permission to access user's album related information.", false),
    /**
     * Requires user permission to access user's comment related information.
     */
    READ_USER_COMMENT("read_user_comment", "Requires user permission to access user's comment related information.",
            false),
    /**
     * Requires user permission to access user's share related information.
     */
    READ_USER_SHARE("read_user_share", "Requires user permission to access user's share related information.", false),
    /**
     * Requires user permission to access user's friend requests, circle requests, etc.
     */
    READ_USER_REQUEST("read_user_request",
            "Requires user permission to access user's friend requests, circle requests, etc.", false),
    /**
     * Requires user permission to publish logs as the user.
     */
    PUBLISH_BLOG("publish_blog", "Requires user permission to publish logs as the user.", false),
    /**
     * Requires user permission to publish check-ins as the user.
     */
    PUBLISH_CHECKIN("publish_checkin", "Requires user permission to publish check-ins as the user.", false),
    /**
     * Requires user permission to send feeds as the user.
     */
    PUBLISH_FEED("publish_feed", "Requires user permission to send feeds as the user.", false),
    /**
     * Requires user permission to send shares as the user.
     */
    PUBLISH_SHARE("publish_share", "Requires user permission to send shares as the user.", false),
    /**
     * Requires user permission to leave messages in the guestbook as the user.
     */
    WRITE_GUESTBOOK("write_guestbook", "Requires user permission to leave messages in the guestbook as the user.",
            false),
    /**
     * Requires user permission to send invitations as the user.
     */
    SEND_INVITATION("send_invitation", "Requires user permission to send invitations as the user.", false),
    /**
     * Requires user permission to send friend requests, circle requests, etc., as the user.
     */
    SEND_REQUEST("send_request",
            "Requires user permission to send friend requests, circle requests, etc., as the user.", false),
    /**
     * Requires user permission to send internal messages as the user.
     */
    SEND_MESSAGE("send_message", "Requires user permission to send internal messages as the user.", false),
    /**
     * Requires user permission to send notifications (user_to_user) as the user.
     */
    SEND_NOTIFICATION("send_notification", "Requires user permission to send notifications (user_to_user) as the user.",
            false),
    /**
     * Requires user permission to upload photos as the user.
     */
    PHOTO_UPLOAD("photo_upload", "Requires user permission to upload photos as the user.", false),
    /**
     * Requires user permission to update status as the user.
     */
    STATUS_UPDATE("status_update", "Requires user permission to update status as the user.", false),
    /**
     * Requires user permission to create albums as the user.
     */
    CREATE_ALBUM("create_album", "Requires user permission to create albums as the user.", false),
    /**
     * Requires user permission to publish comments as the user.
     */
    PUBLISH_COMMENT("publish_comment", "Requires user permission to publish comments as the user.", false),
    /**
     * Requires user permission to perform like operations as the user.
     */
    OPERATE_LIKE("operate_like", "Requires user permission to perform like operations as the user.", false),
    /**
     * Requires user permission to manage public pages that the user can manage.
     */
    ADMIN_PAGE("admin_page", "Requires user permission to manage public pages that the user can manage.", false);

    /**
     * The scope string as defined by Renren.
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

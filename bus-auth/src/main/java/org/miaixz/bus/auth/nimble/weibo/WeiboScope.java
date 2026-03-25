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
package org.miaixz.bus.auth.nimble.weibo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Weibo authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum WeiboScope implements AuthorizeScope {

    /**
     * Get all permissions. The meaning of {@code scope} is subject to {@code description}.
     */
    ALL("all", "Get all permissions", true),
    /**
     * User's contact email. <a href="http://open.weibo.com/wiki/2/account/profile/email">Interface documentation</a>.
     */
    EMAIL("email",
            "User's contact email, <a href=\"http://open.weibo.com/wiki/2/account/profile/email\">interface documentation</a>",
            false),
    /**
     * Private message sending interface. <a href="http://open.weibo.com/wiki/C/2/direct_messages/send">Interface
     * documentation</a>.
     */
    DIRECT_MESSAGES_WRITE("direct_messages_write",
            "Private message sending interface, <a href=\"http://open.weibo.com/wiki/C/2/direct_messages/send\">interface documentation</a>",
            false),
    /**
     * Private message reading interface. <a href="http://open.weibo.com/wiki/C/2/direct_messages">Interface
     * documentation</a>.
     */
    DIRECT_MESSAGES_READ("direct_messages_read",
            "Private message reading interface, <a href=\"http://open.weibo.com/wiki/C/2/direct_messages\">interface documentation</a>",
            false),
    /**
     * Invitation sending interface.
     * <a href="http://open.weibo.com/wiki/Messages#.E5.A5.BD.E5.8F.8B.E9.82.80.E8.AF.B7">Interface documentation</a>.
     */
    INVITATION_WRITE("invitation_write",
            "Invitation sending interface, <a href=\"http://open.weibo.com/wiki/Messages#.E5.A5.BD.E5.8F.8B.E9.82.80.E8.AF.B7\">interface documentation</a>",
            false),
    /**
     * Friend group reading interface group.
     * <a href= "http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84">Interface
     * documentation</a>.
     */
    FRIENDSHIPS_GROUPS_READ("friendships_groups_read",
            "Friend group reading interface group, <a href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84\">interface documentation</a>",
            false),
    /**
     * Friend group writing interface group.
     * <a href= "http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84">Interface
     * documentation</a>.
     */
    FRIENDSHIPS_GROUPS_WRITE("friendships_groups_write",
            "Friend group writing interface group, <a href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84\">interface documentation</a>",
            false),
    /**
     * Directed Weibo reading interface group.
     * <a href="http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.BE.AE.E5.8D.9A">Interface documentation</a>.
     */
    STATUSES_TO_ME_READ("statuses_to_me_read",
            "Directed Weibo reading interface group, <a href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.BE.AE.E5.8D.9A\">interface documentation</a>",
            false),
    /**
     * Follow the application's official microblog. This parameter does not correspond to a specific interface; you only
     * need to fill in the official account in the application console. The path to fill in is: My Applications - Select
     * your application - Application Information - Basic Information - Official Operation Account (default value is
     * application developer account).
     */
    FOLLOW_APP_OFFICIAL_MICROBLOG("follow_app_official_microblog",
            "Follow the application's official microblog. This parameter does not correspond to a specific interface; you only need to fill in the official account in the application console. The path to fill in is: My Applications - Select your application - Application Information - Basic Information - Official Operation Account (default value is application developer account)",
            false);

    /**
     * The scope string as defined by Weibo.
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

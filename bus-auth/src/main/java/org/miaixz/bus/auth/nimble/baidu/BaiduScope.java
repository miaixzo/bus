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
package org.miaixz.bus.auth.nimble.baidu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Baidu authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum BaiduScope implements AuthorizeScope {

    /**
     * User basic permissions, allowing access to basic user information. The meaning of {@code scope} is subject to
     * {@code description}.
     */
    BASIC("basic", "User basic permissions, allowing access to basic user information.", true),
    /**
     * Send message reminders to the user's Baidu homepage. Related APIs can be used by any application, but to display
     * message reminders on the Baidu homepage, third parties need to fill in additional information when registering
     * the application.
     */
    SUPER_MSG("super_msg",
            "Send message reminders to the user's Baidu homepage. Related APIs can be used by any application, but to display message reminders on the Baidu homepage, third parties need to fill in additional information when registering the application.",
            false),
    /**
     * Access to user data stored in personal cloud storage.
     */
    NETDISK("netdisk", "Access to user data stored in personal cloud storage.", false),
    /**
     * Access to public open APIs.
     */
    PUBLIC("public", "Access to public open APIs.", false),
    /**
     * Access to open API interfaces provided by Hao123. This permission requires application and approval. Please send
     * specific reasons and uses via email to tuangou@baidu.com.
     */
    HAO123("hao123",
            "Access to open API interfaces provided by Hao123. This permission requires application and approval. Please send specific reasons and uses via email to tuangou@baidu.com.",
            false);

    /**
     * The scope string as defined by Baidu.
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

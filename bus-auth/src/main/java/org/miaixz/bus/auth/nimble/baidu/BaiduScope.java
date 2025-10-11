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

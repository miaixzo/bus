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
package org.miaixz.bus.auth.nimble.huawei;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Huawei authorization scopes (V3).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum HuaweiScope implements AuthorizeScope {

    /**
     * Basic scope, required for V3. The meaning of {@code scope} is subject to {@code description}.
     */
    OPENID("openid", "Basic scope, required for V3", true),
    /**
     * Retrieves the user's basic information.
     */
    BASE_PROFILE("https://www.huawei.com/auth/account/base.profile", "Retrieves the user's basic information", true),
    /**
     * Retrieves the user's mobile number.
     */
    MOBILE_NUMBER("https://www.huawei.com/auth/account/mobile.number", "Retrieves the user's mobile number", false),
    /**
     * Retrieves the user's bill list.
     */
    ACCOUNTLIST("https://www.huawei.com/auth/account/accountlist", "Retrieves the user's bill list", false),

    /**
     * The following two scopes do not require Huawei assessment and verification.
     */
    SCOPE_DRIVE_FILE("https://www.huawei.com/auth/drive.file",
            "Only allows access to files created or opened by the application", false),
    SCOPE_DRIVE_APPDATA("https://www.huawei.com/auth/drive.appdata",
            "Only allows access to files created or opened by the application", false),
    /**
     * The following four scopes require an application to drivekit@huawei.com before use.
     * <p>
     * Reference:
     * https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides-V5/server-dev-0000001050039664-V5#ZH-CN_TOPIC_0000001050039664__section1618418855716
     */
    SCOPE_DRIVE("https://www.huawei.com/auth/drive", "Only allows access to files created or opened by the application",
            false),
    SCOPE_DRIVE_READONLY("https://www.huawei.com/auth/drive.readonly",
            "Only allows access to files created or opened by the application", false),
    SCOPE_DRIVE_METADATA("https://www.huawei.com/auth/drive.metadata",
            "Only allows access to files created or opened by the application", false),
    SCOPE_DRIVE_METADATA_READONLY("https://www.huawei.com/auth/drive.metadata.readonly",
            "Only allows access to files created or opened by the application", false),

    ;

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

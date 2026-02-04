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

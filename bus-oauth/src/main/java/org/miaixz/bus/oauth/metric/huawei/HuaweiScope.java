/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.metric.huawei;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.oauth.metric.AuthorizeScope;

/**
 * 华为 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum HuaweiScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    BASE_PROFILE("https://www.huawei.com/auth/account/base.profile", "获取用户的基本信息", true),
    MOBILE_NUMBER("https://www.huawei.com/auth/account/mobile.number", "获取用户的手机号", false),
    ACCOUNTLIST("https://www.huawei.com/auth/account/accountlist", "获取用户的账单列表", false),

    /**
     * 以下两个 scope 不需要经过华为评估和验证
     */
    SCOPE_DRIVE_FILE("https://www.huawei.com/auth/drive.file", "只允许访问由应用程序创建或打开的文件", false),
    SCOPE_DRIVE_APPDATA("https://www.huawei.com/auth/drive.appdata", "只允许访问由应用程序创建或打开的文件", false),
    /**
     * 以下四个 scope 使用前需要向drivekit@huawei.com提交申请
     * <p>
     * 参考：https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides-V5/server-dev-0000001050039664-V5#ZH-CN_TOPIC_0000001050039664__section1618418855716
     */
    SCOPE_DRIVE("https://www.huawei.com/auth/drive", "只允许访问由应用程序创建或打开的文件", false),
    SCOPE_DRIVE_READONLY("https://www.huawei.com/auth/drive.readonly", "只允许访问由应用程序创建或打开的文件", false),
    SCOPE_DRIVE_METADATA("https://www.huawei.com/auth/drive.metadata", "只允许访问由应用程序创建或打开的文件", false),
    SCOPE_DRIVE_METADATA_READONLY("https://www.huawei.com/auth/drive.metadata.readonly", "只允许访问由应用程序创建或打开的文件", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}

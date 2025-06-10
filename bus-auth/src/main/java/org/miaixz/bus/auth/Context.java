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
package org.miaixz.bus.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 上下文配置类，支持OAuth2、SAML、LDAP等协议
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    /**
     * 对应各平台的appKey (OAuth2: Client ID)
     */
    private String appKey;

    /**
     * 对应各平台的appSecret (OAuth2: Client Secret)
     */
    private String appSecret;

    /**
     * 1. 支付宝 publicKey 2. 企业微信，授权方的网页应用agentId 3. OktaScope 授权服务器的 ID，默认为 default 4. MicrosoftScope Entra
     * ID（原微软AAD）中的租户 ID 5. 喜马拉雅客户端包名，如果 {@link Context#type} 为1或2时必填。1-对Android客户端是包名，2-对IOS客户端是Bundle ID
     */
    private String unionId;

    /**
     * 扩展ID
     */
    private String extId;

    /**
     * 设备ID, 设备唯一标识ID
     */
    private String deviceId;

    /**
     * 类型 1. 企业微信第三方授权用户类型，member|admin 2. 喜马拉雅客户端操作系统，1-iOS系统，2-Android系统，3-Web
     */
    private String type;

    /**
     * 目前只针对QQ登录
     */
    private boolean flag;

    /**
     * PKCE 模式 (OAuth2)
     */
    private boolean pkce;

    /**
     * 域名前缀 (CodingScope, OktaScope)
     */
    private String prefix;

    /**
     * 回调地址 (OAuth2)
     */
    private String redirectUri;

    /**
     * 自定义授权平台的 scope 内容 (OAuth2)
     */
    private List<String> scopes;

    /**
     * 忽略校验 {@code state}
     */
    private boolean ignoreState;

    /**
     * 忽略校验 {@code redirectUri}
     */
    private boolean ignoreRedirectUri;

    /**
     * 苹果开发者账号中的密钥标识符
     */
    private String kid;

    /**
     * 苹果开发者账号中的团队ID
     */
    private String teamId;

    /**
     * 新版企业微信 Web 登录时的参数
     */
    private String loginType = "CorpApp";

    /**
     * 企业微信平台的语言编码
     */
    private String lang = "zh";

}
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

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.auth.nimble.AbstractProvider;

/**
 * OAuth 及其他协议平台的 API 配置接口。 为 OAuth2、SAML、LDAP 等协议提供特定配置和提供者类，实现统一认证框架。 实现类需提供以下功能：
 * 
 * <pre>
 * 1) getConfig(): 返回协议特定的配置（如 OAuth2 的端点、LDAP 的服务器信息、SAML 的 SSO 端点）。
 * 2) getProtocol(): 返回协议类型（如 OAUTH2、SAML、LDAP）。
 * 3) getTargetClass(): 返回对应的提供者实现类。
 * </pre>
 * 
 * 注意事项： - 扩展第三方授权时，需实现此接口并在 Registry 枚举中注册。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Complex {

    /**
     * 获取协议特定配置。 配置内容根据协议类型定义，例如： - OAuth2：包含 AUTHORIZE、ACCESSTOKEN、USERINFO 等端点 URL。 - SAML：包含 ssoEndpoint、metadataUrl
     * 等。 - LDAP：通常返回空映射，使用 Context 配置。
     *
     * @return 配置键值对映射，默认返回空映射
     */
    default Map<String, String> getConfig() {
        return new HashMap<>();
    }

    /**
     * 获取协议类型。 用于标识认证协议，例如 OAUTH2、SAML、LDAP。
     *
     * @return 协议类型
     */
    Protocol getProtocol();

    /**
     * 获取对应的提供者实现类。 提供者类必须继承自 AbstractProvider，用于处理协议特定的认证逻辑。
     *
     * @return 提供者类
     */
    Class<? extends AbstractProvider> getTargetClass();

    /**
     * 获取 Source 的字符串名称。 通常为枚举名称，用于标识认证来源（如 TWITTER、SAML_EXAMPLE）。 若非枚举实现，则返回类简单名称。
     *
     * @return 来源名称
     */
    default String getName() {
        if (this instanceof Enum) {
            return String.valueOf(this);
        }
        return this.getClass().getSimpleName();
    }

}
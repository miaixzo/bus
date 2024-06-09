/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.oauth;

import lombok.Builder;
import lombok.*;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.List;

/**
 * 上下文配置类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    /**
     * 对应各平台的appKey
     */
    private String appKey;

    /**
     * 对应各平台的appSecret
     */
    private String appSecret;

    /**
     * 1. 支付宝 publicKey
     * 1. 企业微信，授权方的网页应用agentId
     * 2. OktaScope 授权服务器的 ID， 默认为 default
     * 3. MicrosoftScope Entra ID（原微软 AAD）中的租户 ID
     * 4. 喜马拉雅客户端包名，如果 {@link Context#type} 为1或2时必填。1-对Android客户端是包名，2-对IOS客户端是Bundle ID
     */
    private String unionId;

    /**
     * 设备ID, 设备唯一标识ID
     */
    private String deviceId;

    /**
     * 类型
     * 1. 企业微信第三方授权用户类型，member|admin
     * 2. 喜马拉雅客户端操作系统，1-iOS系统，2-Android系统，3-Web
     */
    private String type;

    /**
     * 标记
     * 1. 是否需要申请unionid，目前只针对qq登录
     * 注：QQ授权登录时，获取unionid需要单独发送邮件申请权限。如果个人开发者账号中申请了该权限，可以将该值置为true，在获取openId时就会同步获取unionId
     * 参考链接：http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D
     */
    private boolean flag;

    /**
     * PKCE 模式
     * 该配置仅用于支持 PKCE 模式的平台，针对无服务应用，不推荐使用隐式授权，推荐使用 PKCE 模式
     */
    private boolean pkce;

    /**
     * 域名前缀
     * 使用 CodingScope 登录和 OktaScope 登录时，需要传该值
     */
    private String prefix;

    /**
     * 回调地址
     */
    private String redirectUri;

    /**
     * 自定义授权平台的 scope 内容
     */
    private List<String> scopes;

    /**
     * 忽略校验 {@code state}
     * 默认不开启。当 {@code ignoreCheckState} 为 {@code true} 时，
     * {@link DefaultProvider#login(Callback)} 将不会校验 {@code state} 的合法性。
     * <p>
     * 使用场景：当且仅当使用自实现 {@code state} 校验逻辑时开启
     * <p>
     * 以下场景使用方案仅作参考：
     * 1. 授权、登录为同端，并且全部使用实现时，该值建议设为 {@code false};
     * 2. 授权和登录为不同端实现时，比如前端页面拼装 {@code authorizeUrl}，并且前端自行对{@code state}进行校验，
     * 后端只负责使用{@code code}获取用户信息时，该值建议设为 {@code true};
     *
     * <strong>如非特殊需要，不建议开启这个配置</strong>
     */
    private boolean ignoreState;

    /**
     * 忽略校验 {@code redirectUri}
     * {@code redirectUri} 参数，默认不开启。当 {@code ignoreCheckRedirectUri} 为 {@code true} 时，
     * {@link Checker#checkConfig(Context, Complex)} 将不会校验 {@code redirectUri} 的合法性。
     */
    private boolean ignoreRedirectUri;

}

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
package org.miaixz.bus.auth.nimble;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;

/**
 * 抽象授权处理基类，支持 OAuth2、SAML、LDAP 等多种协议。 提供通用的授权、令牌获取和用户信息查询逻辑，协议特定实现由子类完成。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements Provider {

    /**
     * 包含协议特定配置的上下文对象
     */
    protected Context context;
    /**
     * 定义协议端点或配置的协议对象
     */
    protected Complex complex;
    /**
     * 用于存储状态或其他临时数据的缓存
     */
    protected ExtendCache cache;

    /**
     * 使用指定的上下文和协议配置构造 AbstractProvider。
     *
     * @param context 上下文配置
     * @param complex 协议配置
     */
    public AbstractProvider(Context context, Complex complex) {
        this(context, complex, AuthCache.INSTANCE);
    }

    /**
     * 使用自定义缓存构造 AbstractProvider。
     *
     * @param context 上下文配置
     * @param complex 协议配置
     * @param cache   缓存实现
     * @throws AuthorizedException 如果配置不完整
     */
    public AbstractProvider(Context context, Complex complex, ExtendCache cache) {
        this.context = context;
        this.complex = complex;
        this.cache = cache;
        // 验证授权支持
        if (!Checker.isSupportedAuth(this.context, this.complex)) {
            throw new AuthorizedException(ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 验证配置
        check(this.context);
    }

    /**
     * 从授权范围数组中获取标记为默认的范围。
     *
     * @param scopes 授权范围数组
     * @return 默认范围名称列表，若无则返回 null
     */
    public static List<String> getDefaultScopes(AuthorizeScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).filter(AuthorizeScope::isDefault).map(AuthorizeScope::getScope)
                .collect(Collectors.toList());
    }

    /**
     * 从授权范围数组中获取范围名称。
     *
     * @param scopes 可变数量的授权范围
     * @return 范围名称列表，若无则返回 null
     */
    public static List<String> getScopes(AuthorizeScope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).map(AuthorizeScope::getScope).collect(Collectors.toList());
    }

    /**
     * 处理登录流程，验证回调数据，获取访问令牌并查询用户信息。
     *
     * @param callback 包含授权数据的回调对象（例如代码、状态）
     * @return 包含用户信息或错误的 Message 对象
     */
    @Override
    public Message login(Callback callback) {
        try {
            // 验证回调数据
            check(callback);
            // 对于 OAuth2，验证状态参数（若未忽略）
            if (!context.isIgnoreState() && complex.getProtocol() == Protocol.OIDC) {
                Checker.check(callback.getState(), complex, cache);
            }

            // 获取访问令牌
            AccToken accToken = this.getAccessToken(callback);
            // 查询用户信息
            Material user = this.getUserInfo(accToken);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            Logger.error("使用授权登录失败。", e);
            return this.responseError(e);
        }
    }

    /**
     * 验证回调数据，主要用于 OAuth2 协议。
     *
     * @param callback 包含授权数据的回调对象
     * @throws AuthorizedException 如果 OAuth2 验证失败
     */
    protected void check(Callback callback) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(complex, callback);
        }
    }

    /**
     * 构造登录过程中异常的错误响应。
     *
     * @param e 发生的异常
     * @return 包含错误详情的 Message 对象
     */
    protected Message responseError(Exception e) {
        String errorCode = ErrorCode.FAILURE.getCode();
        String errorMsg = e.getMessage();
        if (e instanceof AuthorizedException) {
            AuthorizedException authException = ((AuthorizedException) e);
            errorCode = authException.getErrcode();
            if (StringKit.isNotEmpty(authException.getErrmsg())) {
                errorMsg = authException.getErrmsg();
            }
        }
        return Message.builder().errcode(errorCode).errmsg(errorMsg).build();
    }

    /**
     * 生成用于启动认证流程的授权 URL。
     *
     * @param state 用于防止 CSRF 攻击的状态参数
     * @return 授权 URL，对于 LDAP 等协议返回 null
     */
    @Override
    public String authorize(String state) {
        if (complex.getProtocol() == Protocol.OIDC) {
            // 构建 OAuth2 授权 URL
            return Builder.fromUrl(complex.getConfig().get("authorize")).queryParam("response_type", "code")
                    .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                    .queryParam("state", getRealState(state))
                    .queryParam("scope", getScopes(Symbol.SPACE, true, getDefaultScopes(null))).build();
        } else if (complex.getProtocol() == Protocol.SAML) {
            // 构建 SAML 单点登录 URL
            return Builder.fromUrl(complex.getConfig().get("ssoEndpoint")).queryParam("RelayState", getRealState(state))
                    .build();
        }
        return null; // LDAP 不使用授权 URL
    }

    /**
     * 构造 OAuth2 的访问令牌 URL。
     *
     * @param code 授权代码
     * @return 访问令牌 URL
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.getConfig().get("accessToken")).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 构造 OAuth2 的刷新令牌 URL。
     *
     * @param refreshToken 刷新令牌
     * @return 刷新令牌 URL
     */
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.getConfig().get("refresh")).queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret()).queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token").queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * 构造 OAuth2 的用户信息 URL。
     *
     * @param accToken 访问令牌
     * @return 用户信息 URL
     */
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.getConfig().get("userInfo"))
                .queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 构造 OAuth2 的撤销授权 URL。
     *
     * @param accToken 访问令牌
     * @return 撤销授权 URL
     */
    protected String revokeUrl(AccToken accToken) {
        return Builder.fromUrl(complex.getConfig().get("revoke")).queryParam("access_token", accToken.getAccessToken())
                .build();
    }

    /**
     * 生成或缓存用于防止 CSRF 攻击的状态参数。
     *
     * @param state 提供的状态值，若为空则生成新值
     * @return 非空的狀態值
     */
    protected String getRealState(String state) {
        if (StringKit.isEmpty(state)) {
            state = ID.objectId();
        }
        cache.cache(state, state);
        return state;
    }

    /**
     * 执行 POST 请求以获取 OAuth2 访问令牌。
     *
     * @param code 授权代码
     * @return 响应内容
     */
    protected String doPostAuthorizationCode(String code) {
        return Httpx.post(accessTokenUrl(code));
    }

    /**
     * 执行 GET 请求以获取 OAuth2 访问令牌。
     *
     * @param code 授权代码
     * @return 响应内容
     */
    protected String doGetAuthorizationCode(String code) {
        return Httpx.get(accessTokenUrl(code));
    }

    /**
     * 执行 GET 请求以获取 OAuth2 用户信息。
     *
     * @param accToken 访问令牌
     * @return 响应内容
     */
    protected String doGetUserInfo(AccToken accToken) {
        return Httpx.get(userInfoUrl(accToken));
    }

    /**
     * 执行 GET 请求以撤销 OAuth2 授权。
     *
     * @param accToken 访问令牌
     * @return 响应内容
     */
    protected String doGetRevoke(AccToken accToken) {
        return Httpx.get(revokeUrl(accToken));
    }

    /**
     * 从配置或默认范围构造范围字符串。
     *
     * @param separator     多个范围的分隔符
     * @param encode        是否对范围字符串进行 URL 编码
     * @param defaultScopes 默认范围（若未配置范围时使用）
     * @return 范围字符串，若无则返回空字符串
     */
    protected String getScopes(String separator, boolean encode, List<String> defaultScopes) {
        List<String> scopes = context.getScopes();
        if (null == scopes || scopes.isEmpty()) {
            if (null == defaultScopes || defaultScopes.isEmpty()) {
                return Normal.EMPTY;
            }
            scopes = defaultScopes;
        }
        if (null == separator) {
            separator = Symbol.SPACE;
        }
        String scope = String.join(separator, scopes);
        return encode ? UrlEncoder.encodeAll(scope) : scope;
    }

    /**
     * 验证上下文配置的完整性，根据协议类型检查所需字段。
     *
     * @param context 上下文配置
     * @throws AuthorizedException 如果配置不完整
     */
    protected void check(Context context) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(context, this.complex);
        }
    }

}
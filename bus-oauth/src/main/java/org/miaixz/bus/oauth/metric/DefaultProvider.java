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
package org.miaixz.bus.oauth.metric;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.key.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.UriKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Checker;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.cache.OauthCache;
import org.miaixz.bus.oauth.magic.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认授权处理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class DefaultProvider implements AuthorizeProvider {

    protected Context context;
    protected Complex complex;
    protected ExtendCache authorizeCache;

    public DefaultProvider(Context context, Complex complex) {
        this(context, complex, OauthCache.INSTANCE);
    }

    public DefaultProvider(Context context, Complex complex, ExtendCache authorizeCache) {
        this.context = this.context;
        this.complex = complex;
        this.authorizeCache = authorizeCache;
        if (!Checker.isSupportedAuth(this.context, complex)) {
            throw new AuthorizedException(ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        Checker.checkConfig(this.context, complex);
    }

    /**
     * 获取 {@link AuthorizeScope} 数组中所有的被标记为 {@code default} 的 scope
     *
     * @param scopes scopes
     * @return List
     */
    public static List<String> getDefaultScopes(AuthorizeScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes)
                .filter((AuthorizeScope::isDefault))
                .map(AuthorizeScope::getScope)
                .collect(Collectors.toList());
    }

    /**
     * 从 {@link AuthorizeScope} 数组中获取实际的 scope 字符串
     *
     * @param scopes 可变参数，支持传任意 {@link AuthorizeScope}
     * @return List
     */
    public static List<String> getScopes(AuthorizeScope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).map(AuthorizeScope::getScope).collect(Collectors.toList());
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see DefaultProvider#authorize(String)
     */
    protected abstract AccToken getAccessToken(Callback authCallback);

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see DefaultProvider#getAccessToken(Callback)
     */
    protected abstract Property getUserInfo(AccToken accToken);

    /**
     * 统一的登录入口。当通过{@link DefaultProvider#authorize(String)}授权成功后，会跳转到调用方的相关回调方法中
     * 方法的入参可以使用{@code Callback}，{@code Callback}类中封装好了OAuth2授权回调所需要的参数
     *
     * @param authCallback 用于接收回调参数的实体
     * @return Message
     */
    @Override
    public Message login(Callback authCallback) {
        try {
            checkCode(authCallback);
            if (!context.isIgnoreCheckState()) {
                Checker.checkState(authCallback.getState(), complex, authorizeCache);
            }

            AccToken accToken = this.getAccessToken(authCallback);
            Property user = this.getUserInfo(accToken);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            Logger.error("Failed to login with oauth authorization.", e);
            return this.responseError(e);
        }
    }

    protected void checkCode(Callback authCallback) {
        Checker.checkCode(complex, authCallback);
    }

    /**
     * 处理{@link DefaultProvider#login(Callback)} 发生异常的情况，统一响应参数
     *
     * @param e 具体的异常
     * @return Message
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
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken refreshToken
     * @return 返回获取accessToken的url
     */
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh())
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 返回获取revoke authorization的url
     *
     * @param accToken token
     * @return 返回获取revoke authorization的url
     */
    protected String revokeUrl(AccToken accToken) {
        return Builder.fromUrl(complex.revoke()).queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 获取state，如果为空， 则默认取当前日期的时间戳
     *
     * @param state 原始的state
     * @return 返回不为null的state
     */
    protected String getRealState(String state) {
        if (StringKit.isEmpty(state)) {
            state = ID.objectId();
        }
        // 缓存state
        authorizeCache.cache(state, state);
        return state;
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return Response
     */
    protected String doPostAuthorizationCode(String code) {
        return Httpx.post(accessTokenUrl(code));
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return Response
     */
    protected String doGetAuthorizationCode(String code) {
        return Httpx.get(accessTokenUrl(code));
    }

    /**
     * 通用的 用户信息
     *
     * @param accToken token封装
     * @return Response
     */
    protected String doGetUserInfo(AccToken accToken) {
        return Httpx.get(userInfoUrl(accToken));
    }

    /**
     * 通用的post形式的取消授权方法
     *
     * @param accToken token封装
     * @return Response
     */
    protected String doGetRevoke(AccToken accToken) {
        return Httpx.get(revokeUrl(accToken));
    }

    /**
     * 获取以 {@code separator}分割过后的 scope 信息
     *
     * @param separator     多个 {@code scope} 间的分隔符
     * @param encode        是否 encode 编码
     * @param defaultScopes 默认的 scope， 当客户端没有配置 {@code scopes} 时启用
     * @return String
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
            // 默认为空格
            separator = Symbol.SPACE;
        }
        String scope = String.join(separator, scopes);
        return encode ? UriKit.encode(scope) : scope;
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.microsoft;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 微软 登录抽象类,负责处理使用微软国际和微软中国账号登录第三方网站的登录方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMicrosoftProvider extends AbstractProvider {

    public AbstractMicrosoftProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public AbstractMicrosoftProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        return getToken(accessTokenUrl(callback.getCode()));
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(accessTokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        String response = Httpx.post(accessTokenUrl, form);
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in")).scope(accessTokenObject.getString("scope"))
                .tokenType(accessTokenObject.getString("token_type"))
                .refreshToken(accessTokenObject.getString("refresh_token")).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", accToken.getTokenType() + Symbol.SPACE + accToken.getAccessToken());

        String userInfo = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        return Material.builder().rawJson(object).uuid(object.getString("id"))
                .username(object.getString("userPrincipalName")).nickname(object.getString("displayName"))
                .location(object.getString("officeLocation")).email(object.getString("mail")).gender(Gender.UNKNOWN)
                .token(accToken).source(complex.toString()).build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(accToken.getRefreshToken()))).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        // 兼容 MicrosoftScope Entra ID 登录（原微软 AAD）
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(complex.authorize(), tenantId)).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).queryParam("response_mode", "query").queryParam("scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权code
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(complex.accessToken(), tenantId)).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken 用户授权后的token
     * @return 返回获取accessToken的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(complex.refresh(), tenantId)).queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret()).queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}

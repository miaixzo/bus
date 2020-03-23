/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 微软登录
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public class MicrosoftProvider extends DefaultProvider {

    public MicrosoftProvider(Context context) {
        super(context, Registry.MICROSOFT);
    }

    public MicrosoftProvider(Context context, StateCache stateCache) {
        super(context, Registry.MICROSOFT, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return getToken(accessTokenUrl(Callback.getCode()));
    }

    /**
     * 获取token,适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "https://login.microsoftonline.com");

        String response = Httpx.post(accessTokenUrl, parseQueryToMap(accessTokenUrl), header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .scope(object.getString("scope"))
                .tokenType(object.getString("token_type"))
                .refreshToken(object.getString("refresh_token"))
                .build();
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
    protected Property getUserInfo(AccToken oauthToken) {
        String token = oauthToken.getAccessToken();
        String tokenType = oauthToken.getTokenType();
        String jwt = tokenType + Symbol.SPACE + token;

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", jwt);

        String response = Httpx.get(userInfoUrl(oauthToken), null, header);

        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        return Property.builder()
                .uuid(object.getString("id"))
                .username(object.getString("userPrincipalName"))
                .nickname(object.getString("displayName"))
                .location(object.getString("officeLocation"))
                .email(object.getString("mail"))
                .gender(Normal.Gender.UNKNOWN)
                .token(oauthToken)
                .source(source.toString())
                .build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken token) {
        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(token.getRefreshToken())))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("response_mode", "query")
                .queryParam("scope", "offline_access%20user.read%20mail.read")
                .queryParam("state", getRealState(state))
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
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("scope", "user.read%20mail.read")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromUrl(source.userInfo()).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken 用户授权后的token
     * @return 返回获取accessToken的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(source.refresh())
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("scope", "user.read%20mail.read")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

}

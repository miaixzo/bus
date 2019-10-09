/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;


/**
 * 抖音登录
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class DouyinProvider extends DefaultProvider {

    public DouyinProvider(Context config) {
        super(config, Registry.DOUYIN);
    }

    public DouyinProvider(Context config, StateCache stateCache) {
        super(config, Registry.DOUYIN, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return this.getToken(accessTokenUrl(Callback.getCode()));
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));
        this.checkResponse(object);
        return Property.builder()
                .uuid(object.getString("union_id"))
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("avatar"))
                .remark(object.getString("description"))
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(oldToken.getRefreshToken())))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        String message = object.getString("message");
        JSONObject data = object.getJSONObject("data");
        int errorCode = data.getIntValue("error_code");
        if ("error".equals(message) || errorCode != 0) {
            throw new InstrumentException("" + errorCode, data.getString("description"));
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        JSONObject object = JSONObject.parseObject(HttpClient.post(accessTokenUrl));
        this.checkResponse(object);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .openId(object.getString("open_id"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .scope(object.getString("scope"))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_key", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", "user_info")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code oauth的授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_key", config.getClientId())
                .queryParam("client_secret", config.getClientSecret())
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token oauth返回的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("open_id", token.getOpenId())
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken oauth返回的refreshtoken
     * @return 返回获取accessToken的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromBaseUrl(source.refresh())
                .queryParam("client_key", config.getClientId())
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .build();
    }

}

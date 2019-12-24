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
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
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
 * 华为授权登录
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class HuaweiProvider extends DefaultProvider {

    public HuaweiProvider(Context context) {
        super(context, Registry.HUAWEI);
    }

    public HuaweiProvider(Context context, StateCache stateCache) {
        super(context, Registry.HUAWEI, stateCache);
    }

    /**
     * 获取access token
     *
     * @param Callback 授权成功后的回调参数
     * @return token
     * @see DefaultProvider#authorize(String)
     */
    @Override
    protected AccToken getAccessToken(Callback Callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", Callback.getAuthorization_code());
        params.put("client_id", context.getClientId());
        params.put("client_secret", context.getClientSecret());
        params.put("redirect_uri", context.getRedirectUri());

        Httpx.post(source.accessToken(), params);

        return getAuthToken(params);
    }

    /**
     * 使用token换取用户信息
     *
     * @param token token信息
     * @return 用户信息
     * @see DefaultProvider#getAccessToken(Callback)
     */
    @Override
    protected Property getUserInfo(AccToken token) {
        Map<String, Object> params = new HashMap<>();
        params.put("nsp_ts", System.currentTimeMillis());
        params.put("access_token", token.getAccessToken());
        params.put("nsp_fmt", "JS");
        params.put("nsp_svc", "OpenUP.User.getInfo");

        String response = Httpx.post(source.userInfo(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        Normal.Gender gender = getRealGender(object);

        return Property.builder()
                .uuid(object.getString("userID"))
                .username(object.getString("userName"))
                .nickname(object.getString("userName"))
                .gender(gender)
                .avatar(object.getString("headPictureURL"))
                .token(token)
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
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", context.getClientId());
        params.put("client_secret", context.getClientSecret());
        params.put("refresh_token", token.getRefreshToken());
        params.put("grant_type", "refresh_token");
        Httpx.post(source.accessToken(), params);
        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(getAuthToken(params))
                .build();
    }

    private AccToken getAuthToken(Map<String, Object> params) {
        String response = Httpx.post(source.accessToken(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
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
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("access_type", "offline")
                .queryParam("scope", "https%3A%2F%2Fwww.huawei.com%2Fauth%2Faccount%2Fbase.profile")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(source.accessToken())
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", code)
                .queryParam("client_id", context.getClientId())
                .queryParam("client_secret", context.getClientSecret())
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("nsp_ts", System.currentTimeMillis())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("nsp_fmt", "JS")
                .queryParam("nsp_svc", "OpenUP.User.getInfo")
                .build();
    }

    /**
     * 获取用户的实际性别 华为系统中,用户的性别：1表示女,0表示男
     *
     * @param object obj
     * @return AuthUserGender
     */
    private Normal.Gender getRealGender(JSONObject object) {
        int genderCodeInt = object.getIntValue("gender");
        String genderCode = genderCodeInt == 1 ? "0" : (genderCodeInt == 0) ? "1" : genderCodeInt + "";
        return Normal.Gender.getGender(genderCode);
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("NSP_STATUS")) {
            throw new InstrumentException(object.getString("error"));
        }
        if (object.containsKey("error")) {
            throw new InstrumentException(object.getString("sub_error") + ":" + object.getString("error_description"));
        }
    }
}

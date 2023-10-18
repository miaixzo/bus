/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.util.HashMap;
import java.util.Map;

/**
 * 华为授权登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HuaweiProvider extends AbstractProvider {

    public HuaweiProvider(Context context) {
        super(context, Registry.HUAWEI);
    }

    public HuaweiProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.HUAWEI, extendCache);
    }

    /**
     * 获取access token
     *
     * @param callback 授权成功后的回调参数
     * @return token
     * @see AbstractProvider#authorize(String)
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", callback.getAuthorization_code());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        params.put("redirect_uri", context.getRedirectUri());

        Httpx.post(source.accessToken(), params);

        return getAuthToken(params);
    }

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see AbstractProvider#getAccessToken(Callback)
     */
    @Override
    public Property getUserInfo(AccToken accToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("nsp_ts", System.currentTimeMillis());
        params.put("access_token", accToken.getAccessToken());
        params.put("nsp_fmt", "JS");
        params.put("nsp_svc", "OpenUP.User.getInfo");

        String response = Httpx.post(source.userInfo(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("userID"))
                .username(object.getString("userName"))
                .nickname(object.getString("userName"))
                .gender(Normal.Gender.of(object.getString("gender")))
                .avatar(object.getString("headPictureURL"))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    /**
     * 刷新access token (续期)
     *
     * @param accToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken accToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        params.put("refresh_token", accToken.getRefreshToken());
        params.put("grant_type", "refresh_token");
        Httpx.post(source.accessToken(), params);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(getAuthToken(params))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("access_type", "offline")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, getScopes(true, OauthScope.Huawei.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("nsp_ts", System.currentTimeMillis())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("nsp_fmt", "JS")
                .queryParam("nsp_svc", "OpenUP.User.getInfo")
                .build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("NSP_STATUS")) {
            throw new AuthorizedException(object.getString("error"));
        }
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("sub_error") + Symbol.COLON + object.getString("error_description"));
        }
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

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           ~
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
package org.miaixz.bus.oauth.metric.huawei;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 华为 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HuaweiProvider extends DefaultProvider {

    public HuaweiProvider(Context context) {
        super(context, Registry.HUAWEI);
    }

    public HuaweiProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.HUAWEI, authorizeCache);
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see DefaultProvider#authorize(String)
     */
    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, String> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", authCallback.getAuthorization_code());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("redirect_uri", context.getRedirectUri());

        String response = Httpx.post(complex.accessToken(), form);
        return getAuthToken(response);
    }

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see DefaultProvider#getAccessToken(Callback)
     */
    @Override
    protected Property getUserInfo(AccToken accToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("nsp_ts", System.currentTimeMillis() + "");
        form.put("access_token", accToken.getAccessToken());
        form.put("nsp_fmt", "JS");
        form.put("nsp_svc", "OpenUP.User.getInfo");

        String response = Httpx.post(complex.userInfo(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        Gender gender = getRealGender(object);

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("userID"))
                .username(object.getString("userName"))
                .nickname(object.getString("userName"))
                .gender(gender)
                .avatar(object.getString("headPictureURL"))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("refresh_token", accToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        String response = Httpx.post(complex.refresh(), form);
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

    private AccToken getAuthToken(String response) {
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
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
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(HuaweiScope.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("nsp_ts", System.currentTimeMillis())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("nsp_fmt", "JS")
                .queryParam("nsp_svc", "OpenUP.User.getInfo")
                .build();
    }

    /**
     * 获取用户的实际性别。华为系统中，用户的性别：1表示女，0表示男
     *
     * @param object obj
     * @return Authorize
     */
    private Gender getRealGender(JSONObject object) {
        int genderCodeInt = object.getIntValue("gender");
        String genderCode = genderCodeInt == 1 ? "0" : (genderCodeInt == 0) ? "1" : genderCodeInt + "";
        return Gender.of(genderCode);
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

}

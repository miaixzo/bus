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
package org.miaixz.bus.oauth.metric.renren;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.Objects;

/**
 * 人人网 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RenrenProvider extends DefaultProvider {

    public RenrenProvider(Context context) {
        super(context, Registry.RENREN);
    }

    public RenrenProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.RENREN, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        return this.getToken(accessTokenUrl(authCallback.getCode()));
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        JSONObject userObj = JSONObject.parseObject(response).getJSONObject("response");

        return Property.builder()
                .rawJson(userObj)
                .uuid(userObj.getString("id"))
                .avatar(getAvatarUrl(userObj))
                .nickname(userObj.getString("name"))
                .company(getCompany(userObj))
                .gender(getGender(userObj))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(this.refreshTokenUrl(accToken.getRefreshToken())))
                .build();
    }

    private AccToken getToken(String url) {
        String response = Httpx.post(url);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.containsKey("error")) {
            throw new AuthorizedException("Failed to get token from RenrenScope: " + jsonObject);
        }

        return AccToken.builder()
                .tokenType(jsonObject.getString("token_type"))
                .expireIn(jsonObject.getIntValue("expires_in"))
                .accessToken(UrlEncoder.encodeAll(jsonObject.getString("access_token")))
                .refreshToken(UrlEncoder.encodeAll(jsonObject.getString("refresh_token")))
                .openId(jsonObject.getJSONObject("user").getString("id"))
                .build();
    }

    private String getAvatarUrl(JSONObject userObj) {
        JSONArray jsonArray = userObj.getJSONArray("avatar");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.getJSONObject(0).getString("url");
    }

    private Gender getGender(JSONObject userObj) {
        JSONObject basicInformation = userObj.getJSONObject("basicInformation");
        if (Objects.isNull(basicInformation)) {
            return Gender.UNKNOWN;
        }
        return Gender.of(basicInformation.getString("sex"));
    }

    private String getCompany(JSONObject userObj) {
        JSONArray jsonArray = userObj.getJSONArray("work");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.getJSONObject(0).getString("name");
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("userId", accToken.getOpenId())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(RenrenScope.values())))
                .build();
    }

}

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

import java.util.Objects;

/**
 * 人人登录
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class RenrenProvider extends DefaultProvider {

    public RenrenProvider(Context context) {
        super(context, Registry.RENREN);
    }

    public RenrenProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.RENREN, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return this.getToken(accessTokenUrl(Callback.getCode()));
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token)).getJSONObject("response");

        return Property.builder()
                .uuid(object.getString("id"))
                .avatar(getAvatarUrl(object))
                .nickname(object.getString("name"))
                .company(getCompany(object))
                .gender(getGender(object))
                .token(token)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken token) {
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(getToken(this.refreshTokenUrl(token.getRefreshToken())))
                .build();
    }

    private AccToken getToken(String url) {
        JSONObject object = JSONObject.parseObject(Httpx.post(url));
        if (object.containsKey("error")) {
            throw new AuthorizedException("Failed to get token from Renren: " + object);
        }

        return AccToken.builder()
                .tokenType(object.getString("token_type"))
                .expireIn(object.getIntValue("expires_in"))
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .openId(object.getJSONObject("user").getString("id"))
                .build();
    }

    private String getAvatarUrl(JSONObject userObj) {
        JSONArray jsonArray = userObj.getJSONArray("avatar");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.getJSONObject(0).getString("url");
    }

    private Normal.Gender getGender(JSONObject userObj) {
        JSONObject object = userObj.getJSONObject("basicInformation");
        if (Objects.isNull(object)) {
            return Normal.Gender.UNKNOWN;
        }
        return Normal.Gender.getGender(object.getString("sex"));
    }

    private String getCompany(JSONObject userObj) {
        JSONArray array = userObj.getJSONArray("work");
        if (Objects.isNull(array) || array.isEmpty()) {
            return null;
        }
        return array.getJSONObject(0).getString("name");
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("userId", token.getOpenId())
                .build();
    }

}

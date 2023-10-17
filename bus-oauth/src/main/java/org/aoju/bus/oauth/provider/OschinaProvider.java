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
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;

/**
 * oschina登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OschinaProvider extends AbstractProvider {

    public OschinaProvider(Context context) {
        super(context, Registry.OSCHINA);
    }

    public OschinaProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.OSCHINA, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject object = JSONObject.parseObject(doPostAuthorizationCode(callback.getCode()));
        this.checkResponse(object);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .uid(object.getString("uid"))
                .expireIn(object.getIntValue("expires_in"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(accToken));
        this.checkResponse(object);
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("id"))
                .username(object.getString("name"))
                .nickname(object.getString("name"))
                .avatar(object.getString("avatar"))
                .blog(object.getString("url"))
                .location(object.getString("location"))
                .gender(Normal.Gender.of(object.getString("gender")))
                .email(object.getString("email"))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权回调时带回的授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("dataType", "json")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("dataType", "json")
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

}

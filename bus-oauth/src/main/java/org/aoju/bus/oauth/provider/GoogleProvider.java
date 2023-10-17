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
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Google登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GoogleProvider extends AbstractProvider {

    public GoogleProvider(Context context) {
        super(context, Registry.GOOGLE);
    }

    public GoogleProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.GOOGLE, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject object = JSONObject.parseObject(doPostAuthorizationCode(callback.getCode()));
        this.checkResponse(object);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .scope(object.getString("scope"))
                .tokenType(object.getString("token_type"))
                .idToken(object.getString("id_token"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.AUTHORIZATION, "Bearer " + accToken.getAccessToken());

        String response = Httpx.post(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("sub"))
                .username(object.getString("email"))
                .avatar(object.getString("picture"))
                .nickname(object.getString("name"))
                .location(object.getString("locale"))
                .email(object.getString("email"))
                .gender(Normal.Gender.UNKNOWN)
                .token(accToken)
                .source(source.toString())
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("access_type", "offline")
                .queryParam("scope", this.getScopes(Symbol.SPACE, false, getScopes(true, OauthScope.Google.values())))
                .queryParam("prompt", "select_account")
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
        return Builder.fromUrl(source.userInfo()).queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error") || object.containsKey("error_description")) {
            throw new AuthorizedException(object.containsKey("error") + Symbol.COLON + object.getString("error_description"));
        }
    }

}

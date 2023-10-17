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
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.util.Objects;

/**
 * Pinterest登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinterestProvider extends AbstractProvider {

    private static final String FAILURE = "failure";

    public PinterestProvider(Context context) {
        super(context, Registry.PINTEREST);
    }

    public PinterestProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.PINTEREST, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject accessTokenObject = JSONObject.parseObject(doPostAuthorizationCode(callback.getCode()));
        this.checkResponse(accessTokenObject);
        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .tokenType(accessTokenObject.getString("token_type"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String userinfoUrl = userInfoUrl(accToken);
        JSONObject jsonObject = JSONObject.parseObject(Httpx.get(userinfoUrl));
        this.checkResponse(jsonObject);
        JSONObject object = jsonObject.getJSONObject("data");
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("id"))
                .avatar(getAvatarUrl(object))
                .username(object.getString("username"))
                .nickname(object.getString("first_name") + Symbol.SPACE + object.getString("last_name"))
                .gender(Normal.Gender.UNKNOWN)
                .remark(object.getString("bio"))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    private String getAvatarUrl(JSONObject jsonObject) {
        JSONObject object = jsonObject.getJSONObject("image");
        if (Objects.isNull(object)) {
            return null;
        }
        return object.getJSONObject("60x60").getString("url");
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
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, getScopes(true, OauthScope.Pinterest.values())))
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
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("fields", "id,username,first_name,last_name,bio,image")
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (!object.containsKey("status") && FAILURE.equals(object.getString("status"))) {
            throw new AuthorizedException(object.getString("message"));
        }
    }

}
